package src.com.splendor.web.game;

import org.springframework.stereotype.Service;
import src.com.splendor.model.Card;
import src.com.splendor.model.Player;
import src.com.splendor.model.Token;

import java.util.ArrayList;
import java.util.List;

/** Validates and applies token, reserve, and purchase moves for the web session. */
@Service
public class WebGameRulesService {

    private static final String[] GEM_COLORS = {"black", "blue", "green", "red", "white"};

    public boolean take3Tokens(GameSession session, List<String> colors) {
        if (colors == null || colors.size() != 3) return false;
        if (new java.util.HashSet<>(colors).size() != 3) return false;
        for (String c : colors) {
            if (!Token.checkGemType(c) || session.getTokenCount(c) < 1) return false;
        }
        Player p = session.getCurrentPlayer();
        for (String c : colors) {
            session.removeTokens(c, 1);
            p.addToken(new Token(c));
        }
        return true;
    }

    public boolean take2Tokens(GameSession session, String color) {
        if (!Token.checkGemType(color) || session.getTokenCount(color) < 4) return false;
        Player player = session.getCurrentPlayer();
        session.removeTokens(color, 2);
        player.addToken(new Token(color));
        player.addToken(new Token(color));
        return true;
    }

    public boolean reserveCardFromTable(GameSession session, int level, int index) {
        Player player = session.getCurrentPlayer();
        if (player.getReservedCards().size() >= 3) return false;
        ArrayList<Card> open = session.getOpenCardsByLevel(level);
        if (open == null || index < 0 || index >= open.size()) return false;

        Card card = open.remove(index);
        player.addReservedCard(card);
        if (session.getTokenCount("gold") > 0) {
            session.removeTokens("gold", 1);
            player.addToken(new Token("gold"));
        }
        refillOpen(session, level);
        return true;
    }

    public boolean reserveCardFromDeck(GameSession session, int level) {
        Player player = session.getCurrentPlayer();
        if (player.getReservedCards().size() >= 3) return false;
        ArrayList<Card> closed = session.getClosedCardsByLevel(level);
        if (closed == null || closed.isEmpty()) return false;

        Card card = closed.remove(0);
        player.addReservedCard(card);
        if (session.getTokenCount("gold") > 0) {
            session.removeTokens("gold", 1);
            player.addToken(new Token("gold"));
        }
        return true;
    }

    public boolean buyCardFromTable(GameSession session, int level, int index) {
        ArrayList<Card> open = session.getOpenCardsByLevel(level);
        if (open == null || index < 0 || index >= open.size()) return false;
        Card card = open.get(index);
        return buyCard(session, card, open, index, false);
    }

    public boolean buyReservedCard(GameSession session, int reservedIndex) {
        Player player = session.getCurrentPlayer();
        if (reservedIndex < 0 || reservedIndex >= player.getReservedCards().size()) return false;
        Card card = player.getReservedCards().get(reservedIndex);
        return buyCard(session, card, null, -1, true);
    }

    private boolean buyCard(GameSession session, Card card, ArrayList<Card> openList, int index, boolean fromReserved) {
        Player player = session.getCurrentPlayer();
        if (!canAfford(player, card)) return false;

        processPurchase(session, player, card);
        if (fromReserved) {
            player.removeReservedCard(card);
        } else {
            openList.remove(index);
            refillOpen(session, card.getLevel());
        }
        player.addBoughtCard(card);
        return true;
    }

    public void executeReturnTokens(GameSession session, List<String> tokensToReturn) {
        Player player = session.getCurrentPlayer();
        for (String gem : tokensToReturn) {
            if (player.getTokens().size() <= session.getMaxTokensPerPlayer()) break;
            player.deductTokens(gem, 1);
            session.addTokens(gem, 1);
        }
    }

    private boolean canAfford(Player player, Card card) {
        int gold = player.getGoldTokenCount();
        for (int i = 0; i < 5; i++) {
            int cost = card.getPurchasePrice().charAt(i) - '0';
            int bonus = player.getBoughtCardsGemValueCount(i);
            int needed = Math.max(0, cost - bonus);
            int has = player.getGemTokenCount(i);
            if (has < needed) {
                if (gold < needed - has) return false;
                gold -= (needed - has);
            }
        }
        return true;
    }

    private void processPurchase(GameSession session, Player player, Card card) {
        for (int i = 0; i < 5; i++) {
            int cost = card.getPurchasePrice().charAt(i) - '0';
            int bonus = player.getBoughtCardsGemValueCount(i);
            int needed = Math.max(0, cost - bonus);
            int toRemove = Math.min(player.getGemTokenCount(i), needed);
            if (toRemove > 0) {
                player.deductTokens(GEM_COLORS[i], toRemove);
                session.addTokens(GEM_COLORS[i], toRemove);
            }
            int goldNeeded = needed - toRemove;
            if (goldNeeded > 0) {
                player.deductTokens("gold", goldNeeded);
                session.addTokens("gold", goldNeeded);
            }
        }
    }

    private void refillOpen(GameSession session, int level) {
        ArrayList<Card> closed = session.getClosedCardsByLevel(level);
        ArrayList<Card> open = session.getOpenCardsByLevel(level);
        if (closed == null || open == null) return;
        CardSupply.transferRandomFaceUp(closed, open);
    }
}
