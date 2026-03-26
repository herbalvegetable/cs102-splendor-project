package src.com.splendor.web;

import src.com.splendor.game.DataLoader;
import src.com.splendor.model.Card;
import src.com.splendor.model.Noble;
import src.com.splendor.model.Player;
import src.com.splendor.model.Token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Core game logic for the web version. Loads data, initializes GameSession, and executes actions.
 */
public class GameService {

    private static final String[] GEM_COLORS = {"black", "blue", "green", "red", "white"};
    private final DataLoader dataLoader = new DataLoader();

    public GameSession newGame(int humanCount, int cpuCount) {
        int pointsToWin = Integer.parseInt(dataLoader.getProperty("game.pointsToWin"));
        int maxTokens = Integer.parseInt(dataLoader.getProperty("game.maxTokensPerPlayer"));
        int playerCount = humanCount + cpuCount;
        int tokenPerGem = Integer.parseInt(dataLoader.getProperty("game.tokenCount." + playerCount + "players"));
        int goldCount = Integer.parseInt(dataLoader.getProperty("game.tokenCount.gold"));

        GameSession session = new GameSession(humanCount, cpuCount, pointsToWin, maxTokens, tokenPerGem, goldCount);

        loadCards(session);
        loadNobles(session, playerCount);

        return session;
    }

    private void loadCards(GameSession session) {
        String content = dataLoader.readResourceFile("/cards.csv");
        String[] lines = content.split("\n");
        ArrayList<Card> closed1 = session.getClosedLevel1();
        ArrayList<Card> closed2 = session.getClosedLevel2();
        ArrayList<Card> closed3 = session.getClosedLevel3();

        for (int i = 1; i < lines.length; i++) {
            String[] p = lines[i].split(",");
            int level = Integer.parseInt(p[0]);
            String color = p[1];
            int prestige = Integer.parseInt(p[2]);
            StringBuilder price = new StringBuilder();
            for (int j = 3; j <= 7; j++) price.append(p[j]);
            Card card = new Card(level, color, prestige, price.toString());
            switch (level) {
                case 1 -> closed1.add(card);
                case 2 -> closed2.add(card);
                case 3 -> closed3.add(card);
            }
        }

        transferToOpen(session.getClosedLevel1(), session.getOpenLevel1());
        transferToOpen(session.getClosedLevel2(), session.getOpenLevel2());
        transferToOpen(session.getClosedLevel3(), session.getOpenLevel3());
    }

    private void transferToOpen(ArrayList<Card> closed, ArrayList<Card> open) {
        Random rand = new Random();
        while (open.size() < 4 && !closed.isEmpty()) {
            Card c = closed.remove(rand.nextInt(closed.size()));
            open.add(c);
        }
    }

    private void loadNobles(GameSession session, int playerCount) {
        String content = dataLoader.readResourceFile("/nobles.csv");
        String[] lines = content.split("\n");
        ArrayList<Noble> all = new ArrayList<>();
        for (int i = 1; i < lines.length; i++) {
            String[] p = lines[i].split(",");
            String name = p[0];
            int prestige = Integer.parseInt(p[1]);
            StringBuilder price = new StringBuilder();
            for (int j = 2; j <= 6; j++) price.append(p[j]);
            all.add(new Noble(name, prestige, price.toString()));
        }
        Collections.shuffle(all);
        int count = Math.min(playerCount + 1, all.size());
        for (int i = 0; i < count; i++) {
            session.getAvailNobles().add(all.get(i));
        }
    }

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

    public boolean returnTokens(GameSession session, String gemType, int count) {
        Player player = session.getCurrentPlayer();
        if (player.getTokens().size() <= session.getMaxTokensPerPlayer()) return false;
        String[] valid = {"black", "blue", "green", "red", "white", "gold"};
        if (!Arrays.asList(valid).contains(gemType)) return false;
        int have = gemType.equals("gold") ? player.getGoldTokenCount() : player.getGemTokenCount(Arrays.asList(valid).indexOf(gemType));
        if (have < count) return false;

        player.deductTokens(gemType, count);
        session.addTokens(gemType, count);
        return true;
    }

    /** Returns the noble acquired, or null if none. */
    public Noble processNobleAcquisition(GameSession session, Player player) {
        List<Noble> qualifying = new ArrayList<>();
        for (Noble n : session.getAvailNobles()) {
            if (playerHasEnoughBonuses(player, n)) qualifying.add(n);
        }
        if (qualifying.isEmpty()) return null;
        Noble chosen = qualifying.get(0);
        player.addNoble(chosen);
        session.removeNoble(chosen);
        return chosen;
    }

    private boolean playerHasEnoughBonuses(Player player, Noble noble) {
        for (int i = 0; i < 5; i++) {
            int bonus = player.getBoughtCardsGemValueCount(i);
            int req = noble.getPurchasePrice().charAt(i) - '0';
            if (bonus < req) return false;
        }
        return true;
    }

    private boolean canAfford(Player player, Card card) {
        int gold = player.getGoldTokenCount();
        for (int i = 0; i < 5; i++) {
            int cost = card.getPurchasePrice().charAt(i) - '0';
            int bonus = player.getBoughtCardsGemValueCount(i);
            System.out.println("Cost: " + cost + ", Bonus: " + bonus + ", GemType: " + GEM_COLORS[i]);
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
        transferToOpen(closed, open);
    }

    /**
     * Finishes the turn: process noble acquisition, check game end, advance to next player.
     * Returns the noble acquired (if any) for logging.
     */
    public Noble finishTurn(GameSession session) {
        Player player = session.getCurrentPlayer();
        Noble acquired = processNobleAcquisition(session, player);
        checkGameEnd(session);
        session.advanceToNextPlayer();
        return acquired;
    }

    public void checkGameEnd(GameSession session) {
        for (Player p : session.getPlayers()) {
            if (p.getPrestigePoints() >= session.getPointsToWin()) {
                session.setFinalRound(true);
                break;
            }
        }
        if (session.isFinalRound() && session.getCurrentPlayerIndex() == session.getPlayers().size() - 1) {
            session.setGameOver(true);
        }
    }

    public void executeReturnTokens(GameSession session, List<String> tokensToReturn) {
        Player player = session.getCurrentPlayer();
        for (String gem : tokensToReturn) {
            if (player.getTokens().size() <= session.getMaxTokensPerPlayer()) break;
            player.deductTokens(gem, 1);
            session.addTokens(gem, 1);
        }
    }

    /** Returns CPU action params: [actionType, level?, index?]. actionType: 1=take3, 2=take2, 3=reserve, 4=buy */
    public Object[] getCPUAction(GameSession session) {
        WebCPUStrategy strategy = new WebCPUStrategy(session);
        Player player = session.getCurrentPlayer();
        int action = strategy.chooseAction(player);
        return switch (action) {
            case 1 -> new Object[]{1, strategy.chooseThreeTokenColors(player)};
            case 2 -> new Object[]{2, strategy.chooseTwoTokenColor(player)};
            case 3 -> getReserveParams(strategy, player);
            case 4 -> getBuyParams(strategy, player);
            default -> new Object[]{1, strategy.chooseThreeTokenColors(player)};
        };
    }

    private Object[] getReserveParams(WebCPUStrategy strategy, Player player) {
        int fromDeck = strategy.chooseReserveSource(player);
        int level = strategy.chooseReserveLevel(player);
        if (fromDeck == 2) return new Object[]{3, "deck", level, -1};
        int idx = strategy.chooseReserveCardIndex(player, level);
        return new Object[]{3, "table", level, idx};
    }

    private Object[] getBuyParams(WebCPUStrategy strategy, Player player) {
        int src = strategy.chooseBuySource(player);
        if (src == 2) {
            int idx = strategy.chooseReservedCardIndex(player);
            return new Object[]{4, "reserved", idx};
        }
        int level = strategy.chooseBuyLevel(player);
        int idx = strategy.chooseBuyCardIndex(player, level);
        return new Object[]{4, "table", level, idx};
    }
}
