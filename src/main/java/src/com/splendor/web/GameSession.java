package src.com.splendor.web;

import src.com.splendor.model.Card;
import src.com.splendor.model.Noble;
import src.com.splendor.model.Player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds per-game state for web sessions. Replaces static TokenPile, CardMarket, NoblePool.
 */
public class GameSession implements Serializable {

    private final Map<String, Integer> tokenCounts = new HashMap<>();
    private final ArrayList<Card> closedLevel1 = new ArrayList<>();
    private final ArrayList<Card> closedLevel2 = new ArrayList<>();
    private final ArrayList<Card> closedLevel3 = new ArrayList<>();
    private final ArrayList<Card> openLevel1 = new ArrayList<>();
    private final ArrayList<Card> openLevel2 = new ArrayList<>();
    private final ArrayList<Card> openLevel3 = new ArrayList<>();
    private final ArrayList<Noble> availNobles = new ArrayList<>();

    private final ArrayList<Player> players = new ArrayList<>();
    private int currentPlayerIndex = 0;
    private boolean gameOver = false;
    private boolean finalRound = false;
    private final int pointsToWin;
    private final int maxTokensPerPlayer;

    public GameSession(int humanCount, int cpuCount, int pointsToWin, int maxTokensPerPlayer,
                      int tokenPerGem, int goldCount) {
        this.pointsToWin = pointsToWin;
        this.maxTokensPerPlayer = maxTokensPerPlayer;

        for (String color : new String[]{"black", "blue", "green", "red", "white"}) {
            tokenCounts.put(color, tokenPerGem);
        }
        tokenCounts.put("gold", goldCount);

        for (int i = 1; i <= humanCount + cpuCount; i++) {
            players.add(new Player(i, i <= humanCount));
        }
    }

    public int getTokenCount(String gemType) {
        return tokenCounts.getOrDefault(gemType.toLowerCase(), 0);
    }

    public void removeTokens(String gemType, int amount) {
        tokenCounts.merge(gemType.toLowerCase(), amount, (a, b) -> a - b);
    }

    public void addTokens(String gemType, int amount) {
        tokenCounts.merge(gemType.toLowerCase(), amount, Integer::sum);
    }

    public ArrayList<Card> getOpenLevel1() { return openLevel1; }
    public ArrayList<Card> getOpenLevel2() { return openLevel2; }
    public ArrayList<Card> getOpenLevel3() { return openLevel3; }

    public ArrayList<Card> getClosedLevel1() { return closedLevel1; }
    public ArrayList<Card> getClosedLevel2() { return closedLevel2; }
    public ArrayList<Card> getClosedLevel3() { return closedLevel3; }

    public ArrayList<Card> getOpenCardsByLevel(int level) {
        return switch (level) {
            case 1 -> openLevel1;
            case 2 -> openLevel2;
            case 3 -> openLevel3;
            default -> null;
        };
    }

    public ArrayList<Card> getClosedCardsByLevel(int level) {
        return switch (level) {
            case 1 -> closedLevel1;
            case 2 -> closedLevel2;
            case 3 -> closedLevel3;
            default -> null;
        };
    }

    public ArrayList<Noble> getAvailNobles() { return availNobles; }

    public void removeNoble(Noble noble) {
        availNobles.remove(noble);
    }

    public ArrayList<Player> getPlayers() { return players; }

    public int getCurrentPlayerIndex() { return currentPlayerIndex; }

    public void setCurrentPlayerIndex(int index) { this.currentPlayerIndex = index; }

    public void advanceToNextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public boolean isGameOver() { return gameOver; }

    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }

    public boolean isFinalRound() { return finalRound; }

    public void setFinalRound(boolean finalRound) { this.finalRound = finalRound; }

    public int getPointsToWin() { return pointsToWin; }

    public int getMaxTokensPerPlayer() { return maxTokensPerPlayer; }
}
