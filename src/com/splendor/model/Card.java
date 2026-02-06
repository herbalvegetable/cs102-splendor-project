package src.com.splendor.model;

import java.util.*;
import src.com.splendor.game.DataLoader;
import src.com.splendor.game.GameState;

public class Card {

    private static ArrayList<Card> closedLevel1;
    private static ArrayList<Card> closedLevel2;
    private static ArrayList<Card> closedLevel3;

    private static ArrayList<Card> openLevel1;
    private static ArrayList<Card> openLevel2;
    private static ArrayList<Card> openLevel3;

    public static void initialise() {
        // init values of closed and open lists
        Card.closedLevel1 = new ArrayList<>();
        Card.closedLevel2 = new ArrayList<>();
        Card.closedLevel3 = new ArrayList<>();
        Card.openLevel1 = new ArrayList<>();
        Card.openLevel2 = new ArrayList<>();
        Card.openLevel3 = new ArrayList<>();

        // Step 1: init closed cards - read from cards.csv
        Card.initClosedCards();

        // Step 2: transfer cards from closed deck to open deck for all 3 levels
        Card.transferFromClosedToOpen(Card.closedLevel1, Card.openLevel1);
        Card.transferFromClosedToOpen(Card.closedLevel2, Card.openLevel2);
        Card.transferFromClosedToOpen(Card.closedLevel3, Card.openLevel3);
    }

    public static void initClosedCards() {
        ArrayList<ArrayList<Card>> closedLists = new ArrayList<ArrayList<Card>>();
        closedLists.add(Card.closedLevel1);
        closedLists.add(Card.closedLevel2);
        closedLists.add(Card.closedLevel3);

        DataLoader dloader = new DataLoader();
        String cardsContent = dloader.readResourceFile("/cards.csv"); //classpath includes resources folder
        // System.out.println(cardsContent);

        String[] lines = cardsContent.split("\n");

        // populate closed lists with card info
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];

            String[] cardProps = line.split(",");
            // cards.csv header columns:
            // level, color, prestigeValue, Black, Blue, Green, Red, White
            int level = Integer.parseInt(cardProps[0]);
            String color = new String(cardProps[1]);
            int prestigePoints = Integer.parseInt(cardProps[2]);

            String purchasePriceString = new String("");
            for (int j = 3; j <= 7; j++) {
                String gemPriceStr = cardProps[j];
                purchasePriceString += String.format("%s", gemPriceStr);
            }

            // add card to closed list of same level
            // System.out.println("" + (level - 1));
            closedLists.get(level - 1).add(new Card(level, color, prestigePoints, purchasePriceString));
        }
    }

    // transfer cards from closed deck to open deck UNTIL EITHER:
    // 1. no. of open cards = 4
    // 2. closed deck runs out of cards
    public static void transferFromClosedToOpen(ArrayList<Card> closedCards, ArrayList<Card> openCards) {
        // only can have 4 open cards on the table at a time
        // must have cards in closed cards list to transfer
        while (openCards.size() < 4 && closedCards.size() > 0) {
            Card randCard = GameState.getRandomItemFromArray(closedCards);

            closedCards.remove(randCard);
            openCards.add(randCard);
        }
    }

    public static ArrayList<Card> getClosedLevel1() {
        return closedLevel1;
    }

    public static ArrayList<Card> getClosedLevel2() {
        return closedLevel2;
    }

    public static ArrayList<Card> getClosedLevel3() {
        return closedLevel3;
    }

    public static ArrayList<Card> getOpenLevel1() {
        return openLevel1;
    }

    public static ArrayList<Card> getOpenLevel2() {
        return openLevel2;
    }

    public static ArrayList<Card> getOpenLevel3() {
        return openLevel3;
    }

    private String gemType;
    private String purchasePrice;
    // purchasePrice: five digit number
    // order of colour prices: black, blue, green, red, white
    private int prestigePoints;
    private int level;

    public Card(int level, String gemType, int prestigePoints, String purchasePrice) {
        this.level = level;
        this.gemType = gemType;
        this.prestigePoints = prestigePoints;
        this.purchasePrice = purchasePrice;
    }

    public int getLevel() {
        return level;
    }

    public String getGemType() {
        return gemType;
    }

    public int getPrestigePoints() {
        return prestigePoints;
    }

    public String getPurchasePrice() {
        return purchasePrice;
    }

}
