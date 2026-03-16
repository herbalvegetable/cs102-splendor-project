package src.com.splendor.web;

import src.com.splendor.model.Card;
import src.com.splendor.model.Noble;
import src.com.splendor.model.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * CPU strategy that reads from GameSession instead of static TokenPile/CardMarket/NoblePool.
 */
public class WebCPUStrategy {

    private static final String[] GEM_COLORS = {"black", "blue", "green", "red", "white"};
    private static final Random RAND = new Random();
    private final GameSession session;

    public WebCPUStrategy(GameSession session) {
        this.session = session;
    }

    public int chooseAction(Player player) {
        if (findAffordableCard(player) >= 0) return 4;
        if (player.getReservedCards().size() < 3 && hasReservableCard()) return 3;
        if (canTake2Same()) return 2;
        if (canTake3Different()) return 1;
        return 1;
    }

    public String chooseThreeTokenColors(Player player) {
        List<String> available = new ArrayList<>();
        for (String color : GEM_COLORS) {
            if (session.getTokenCount(color) >= 1) available.add(color);
        }
        Collections.shuffle(available);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(3, available.size()); i++) {
            if (i > 0) sb.append(" ");
            sb.append(available.get(i));
        }
        return sb.toString();
    }

    public String chooseTwoTokenColor(Player player) {
        for (String color : GEM_COLORS) {
            if (session.getTokenCount(color) >= 4) return color;
        }
        return GEM_COLORS[0];
    }

    public int chooseReserveSource(Player player) {
        return 1; // Prefer face-up
    }

    public int chooseReserveLevel(Player player) {
        if (!session.getOpenLevel1().isEmpty()) return 1;
        if (!session.getOpenLevel2().isEmpty()) return 2;
        if (!session.getOpenLevel3().isEmpty()) return 3;
        return 1;
    }

    public int chooseReserveCardIndex(Player player, int level) {
        ArrayList<Card> cards = session.getOpenCardsByLevel(level);
        if (cards == null || cards.isEmpty()) return 0;
        return RAND.nextInt(cards.size());
    }

    public int chooseBuySource(Player player) {
        for (Card c : player.getReservedCards()) {
            if (canAfford(player, c)) return 2;
        }
        return 1;
    }

    public int chooseBuyLevel(Player player) {
        for (int level = 3; level >= 1; level--) {
            ArrayList<Card> cards = session.getOpenCardsByLevel(level);
            if (cards != null) {
                for (Card c : cards) {
                    if (canAfford(player, c)) return level;
                }
            }
        }
        return 1;
    }

    public int chooseBuyCardIndex(Player player, int level) {
        ArrayList<Card> cards = session.getOpenCardsByLevel(level);
        if (cards == null || cards.isEmpty()) return 0;
        int bestIdx = 0;
        int bestPrestige = -1;
        for (int i = 0; i < cards.size(); i++) {
            if (canAfford(player, cards.get(i))) {
                int p = cards.get(i).getPrestigePoints();
                if (p > bestPrestige) {
                    bestPrestige = p;
                    bestIdx = i;
                }
            }
        }
        return bestIdx;
    }

    public int chooseReservedCardIndex(Player player) {
        ArrayList<Card> reserved = player.getReservedCards();
        if (reserved.isEmpty()) return 0;
        for (int i = 0; i < reserved.size(); i++) {
            if (canAfford(player, reserved.get(i))) return i;
        }
        return 0;
    }

    public String chooseTokenToReturn(Player player) {
        int[] counts = new int[6];
        String[] names = {"black", "blue", "green", "red", "white", "gold"};
        for (int i = 0; i < 5; i++) counts[i] = player.getGemTokenCount(i);
        counts[5] = player.getGoldTokenCount();
        int maxIdx = 0;
        for (int i = 1; i < 6; i++) {
            if (counts[i] > counts[maxIdx]) maxIdx = i;
        }
        return names[maxIdx];
    }

    public int chooseNoble(Player player, ArrayList<Noble> nobles) {
        return RAND.nextInt(nobles.size());
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

    private int findAffordableCard(Player player) {
        for (Card c : player.getReservedCards()) {
            if (canAfford(player, c)) return 1;
        }
        for (int level = 1; level <= 3; level++) {
            ArrayList<Card> cards = session.getOpenCardsByLevel(level);
            if (cards != null) {
                for (Card c : cards) {
                    if (canAfford(player, c)) return 1;
                }
            }
        }
        return -1;
    }

    private boolean hasReservableCard() {
        return !session.getOpenLevel1().isEmpty()
                || !session.getOpenLevel2().isEmpty()
                || !session.getOpenLevel3().isEmpty();
    }

    private boolean canTake2Same() {
        for (String c : GEM_COLORS) {
            if (session.getTokenCount(c) >= 4) return true;
        }
        return false;
    }

    private boolean canTake3Different() {
        int count = 0;
        for (String c : GEM_COLORS) {
            if (session.getTokenCount(c) >= 1) count++;
        }
        return count >= 3;
    }
}
