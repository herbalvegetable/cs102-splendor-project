package src.com.splendor.web.game;

import org.springframework.stereotype.Service;
import src.com.splendor.web.game.model.Card;
import src.com.splendor.web.game.model.Player;
import src.com.splendor.web.game.model.Token;

import java.util.ArrayList;

/**
 * Rules for development cards: reserve, buy, and paying with tokens.
 * Noble visits after a turn are handled by {@link NobleAcquisitionService} and {@link TurnPhaseService}.
 */
@Service
public class WebCardRulesService {

    private static final String[] GEM_COLORS = {"black", "blue", "green", "red", "white"};

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
