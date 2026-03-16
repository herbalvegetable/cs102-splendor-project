package src.com.splendor.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import src.com.splendor.game.DataLoader;

/**
 * Manages the card marketplace: closed decks, open display, loading, and deck operations.
 */
public class CardMarket {

    private static ArrayList<Card> closedLevel1;
    private static ArrayList<Card> closedLevel2;
    private static ArrayList<Card> closedLevel3;

    private static ArrayList<Card> openLevel1;
    private static ArrayList<Card> openLevel2;
    private static ArrayList<Card> openLevel3;

    public static void initialise() {
        closedLevel1 = new ArrayList<>();
        closedLevel2 = new ArrayList<>();
        closedLevel3 = new ArrayList<>();
        openLevel1 = new ArrayList<>();
        openLevel2 = new ArrayList<>();
        openLevel3 = new ArrayList<>();

        initClosedCards();
        transferFromClosedToOpen(closedLevel1, openLevel1);
        transferFromClosedToOpen(closedLevel2, openLevel2);
        transferFromClosedToOpen(closedLevel3, openLevel3);
    }

    private static void initClosedCards() {
        ArrayList<ArrayList<Card>> closedLists = new ArrayList<>();
        closedLists.add(closedLevel1);
        closedLists.add(closedLevel2);
        closedLists.add(closedLevel3);

        DataLoader dloader = new DataLoader();
        String cardsContent = dloader.readResourceFile("/cards.csv");
        String[] lines = cardsContent.split("\n");

        for (int i = 1; i < lines.length; i++) {
            String[] cardProps = lines[i].split(",");
            int level = Integer.parseInt(cardProps[0]);
            String color = cardProps[1];
            int prestigePoints = Integer.parseInt(cardProps[2]);

            StringBuilder purchasePriceString = new StringBuilder();
            for (int j = 3; j <= 7; j++) {
                purchasePriceString.append(cardProps[j]);
            }

            closedLists.get(level - 1).add(new Card(level, color, prestigePoints, purchasePriceString.toString()));
        }
    }

    public static Card drawFromClosedDeck(int level) {
        ArrayList<Card> closedCards = getClosedDeck(level);
        if (closedCards == null || closedCards.isEmpty()) {
            return null;
        }
        return closedCards.remove(0);
    }

    public static void transferFromClosedToOpen(ArrayList<Card> closedCards, ArrayList<Card> openCards) {
        Random rand = new Random();
        while (openCards.size() < 4 && closedCards.size() > 0) {
            Card randCard = closedCards.remove(rand.nextInt(closedCards.size()));
            openCards.add(randCard);
        }
    }

    public static ArrayList<Card> getOpenLevel1() { return openLevel1; }
    public static ArrayList<Card> getOpenLevel2() { return openLevel2; }
    public static ArrayList<Card> getOpenLevel3() { return openLevel3; }

    public static ArrayList<Card> getClosedLevel1() { return closedLevel1; }
    public static ArrayList<Card> getClosedLevel2() { return closedLevel2; }
    public static ArrayList<Card> getClosedLevel3() { return closedLevel3; }

    public static ArrayList<Card> getOpenCardsByLevel(int level) {
        switch (level) {
            case 1: return openLevel1;
            case 2: return openLevel2;
            case 3: return openLevel3;
            default: return null;
        }
    }

    public static ArrayList<Card> getClosedCardsByLevel(int level) {
        switch (level) {
            case 1: return closedLevel1;
            case 2: return closedLevel2;
            case 3: return closedLevel3;
            default: return null;
        }
    }

    private static ArrayList<Card> getClosedDeck(int level) {
        return getClosedCardsByLevel(level);
    }
}
