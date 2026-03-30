package src.com.splendor.web.game.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a player's game state: tokens, cards, nobles, and prestige.
 */
public class Player implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String[] TOKEN_ORDER = {"black", "blue", "green", "red", "white", "gold"};
    private final int playerID;
    private final boolean isHuman;
    private final String displayName;
    private int prestigePoints = 0;
    private ArrayList<Token> tokens = new ArrayList<>();
    private ArrayList<Card> boughtCards = new ArrayList<>();
    private ArrayList<Card> reservedCards = new ArrayList<>();
    private ArrayList<Noble> nobles = new ArrayList<>();

    public Player(int playerID, boolean isHuman, String displayName) {
        this.playerID = playerID;
        this.isHuman = isHuman;
        String t = displayName == null ? "" : displayName.trim();
        if (t.length() > 48) {
            t = t.substring(0, 48);
        }
        this.displayName = t.isEmpty() ? "Player " + playerID : t;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isHuman() {
        return isHuman;
    }

    public int getPlayerID() {
        return playerID;
    }

    public int getPrestigePoints() {
        return prestigePoints;
    }

    public ArrayList<Token> getTokens() {
        return tokens;
    }

    /** Returns tokens sorted by color: black, blue, green, red, white, gold. */
    public List<Token> getTokensSorted() {
        ArrayList<Token> sorted = new ArrayList<>(tokens);
        sorted.sort(Comparator.comparingInt(t -> {
            for (int i = 0; i < TOKEN_ORDER.length; i++) {
                if (TOKEN_ORDER[i].equals(t.getGemType().toLowerCase())) return i;
            }
            return TOKEN_ORDER.length;
        }));
        return sorted;
    }

    public ArrayList<Card> getBoughtCards() {
        return boughtCards;
    }

    public ArrayList<Card> getReservedCards() {
        return reservedCards;
    }

    public ArrayList<Noble> getNobles() {
        return nobles;
    }

    public void addToken(Token token) {
        tokens.add(token);
    }

    public void addReservedCard(Card card) {
        this.reservedCards.add(card);
    }

    public void removeReservedCard(Card card) {
        this.reservedCards.remove(card);
    }

    public void addBoughtCard(Card card) {
        this.boughtCards.add(card);
        this.prestigePoints += card.getPrestigePoints();
    }

    public void addNoble(Noble noble) {
        this.nobles.add(noble);
        this.prestigePoints += noble.getPrestigePoints();
    }

    public int getGemTokenCount(int gemTypeIndex) {
        String[] gemTypes = {"black", "blue", "green", "red", "white"};
        String targetGem = gemTypes[gemTypeIndex];
        int count = 0;
        for (Token token : tokens) {
            if (token.getGemType().equals(targetGem)) {
                count++;
            }
        }
        return count;
    }

    public int getGoldTokenCount() {
        int count = 0;
        for (Token token : tokens) {
            if (token.getGemType().equals("gold")) {
                count++;
            }
        }
        return count;
    }

    public int getBoughtCardsGemValueCount(int gemTypeIndex) {
        String[] gemTypes = {"black", "blue", "green", "red", "white"};
        String targetGem = gemTypes[gemTypeIndex];
        int count = 0;

        for (Card card : boughtCards) {
            if (card.getGemType().toLowerCase().equals(targetGem)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Removes tokens from the player by gem index. Does not update the bank;
     * the web layer returns chips via GameSession when needed.
     */
    public void removePlayerTokens(int gemTypeIndex, int numberOfTokens) {
        String[] gemTypes = {"black", "blue", "green", "red", "white", "gold"};
        String targetGem = gemTypes[gemTypeIndex];

        int removed = 0;

        for (int i = getTokens().size() - 1; i >= 0 && removed < numberOfTokens; i--) {
            if (getTokens().get(i).getGemType().equals(targetGem)) {
                getTokens().remove(i);
                removed++;
            }
        }
    }

    /**
     * Removes tokens from player only (no addition to pile).
     * Used by web rules services which add to session separately.
     */
    public void deductTokens(String gemType, int numberOfTokens) {
        int removed = 0;
        for (int i = getTokens().size() - 1; i >= 0 && removed < numberOfTokens; i--) {
            if (getTokens().get(i).getGemType().equals(gemType)) {
                getTokens().remove(i);
                removed++;
            }
        }
    }
}
