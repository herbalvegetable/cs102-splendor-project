package src.com.splendor.game;

import src.com.splendor.model.*;
import java.util.*;

public class GameState {

    // get user input for an integer
    private static int getInt(String questionText, String errorText, Scanner sc) {
        while (true) {
            System.out.print(questionText);
            if (sc.hasNextInt()) {
                int outputInt = sc.nextInt();
                return outputInt;
            }
            // not an int, dont break out of while loop
            System.out.println(errorText);
            sc.next();
        }
    }

    // get user input for no. of human players
    private static int getNumberOfHumanPlayers(Scanner sc) {
        int humanPlayers = -1;
        while (true) {
            humanPlayers = getInt("Enter number of Human players (1/2/3/4): ", "Not a valid int", sc);
            if (humanPlayers >= 1 && humanPlayers <= 4) {
                break;
            }
            // outside of range
            System.out.println("Outside of range, enter within 1/2/3/4");
        }
        return humanPlayers;
    }

    // get user input for no. of computer players
    private static int getNumberOfComputerPlayers(int humanPlayers, Scanner sc) {
        int computerPlayers = -1;
        int maxComputerPlayers = 4 - humanPlayers;
        while (true) {
            computerPlayers = getInt(String.format("Enter number of CPU players (%d to %d): ", 0, maxComputerPlayers),
                    "Not a valid int", sc);
            if (computerPlayers >= 0 && computerPlayers <= maxComputerPlayers) {
                break;
            }
            // outside of range
            System.out.printf("Outside of range, enter within (%d to %d)\n", 0, maxComputerPlayers);
        }
        return computerPlayers;
    }

    // init deck of closed cards
    public static void initClosedCards(ArrayList<Card> closed1, ArrayList<Card> closed2, ArrayList<Card> closed3) {

        ArrayList<ArrayList<Card>> closedLists = new ArrayList<ArrayList<Card>>();
        closedLists.add(closed1);
        closedLists.add(closed2);
        closedLists.add(closed3);

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
            closedLists.get(level - 1).add(new Card(level, color, prestigePoints, purchasePriceString));
        }
    }

    // returns item of type based on the input arraylist's type
    public static <T> T getRandomItemFromArray(ArrayList<T> arr) {
        Random rand = new Random();
        int max = arr.size() - 1;
        return arr.get(rand.nextInt(max + 1));
    }

    // transfer cards from closed desk to open desk UNTIL EITHER:
    // 1. no. of open cards = 4
    // 2. closed deck runs out of cards
    public static void transferCardsFromClosedToOpen(ArrayList<Card> closedCards, ArrayList<Card> openCards) {
        // only can have 4 open cards on the table at a time
        // must have cards in closed cards list to transfer
        while (openCards.size() < 4 && closedCards.size() > 0) {
            Card randCard = getRandomItemFromArray(closedCards);

            closedCards.remove(randCard);
            openCards.add(randCard);
        }
    }

    // init nobles
    public static void initNobles(ArrayList<Noble> nobles) {
        DataLoader dloader = new DataLoader();
        String noblesContent = dloader.readResourceFile("/nobles.csv");

        // System.out.println(noblesContent);
        String[] lines = noblesContent.split("\n");

        // nobles.csv header columns
        // level, prestigeValue, black, blue, green, red, white

        // populate nobles list with nobles.csv info
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            String[] nobleProps = line.split(",");
            String name = new String(nobleProps[0]);
            int prestigePoints = Integer.parseInt(nobleProps[1]);
    
            String purchasePriceString = new String("");
            for (int j = 2; j <= 6; j++) {
                purchasePriceString += nobleProps[j];
            }
    
            nobles.add(new Noble(name, prestigePoints, purchasePriceString));
        }
    }
    // init nobles available during the game
    public static void initAvailNobles(ArrayList<Noble> totalNobles, ArrayList<Noble> availNobles, int playerCount){
        for (int i = 0; i < playerCount + 1; i++) {
            availNobles.add(getRandomItemFromArray(totalNobles));
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // introductory game text
        System.out.println("===== SPLENDOR =====");
        System.out.println("Welcome to the world of Splendor!\n");

        // init number of human & CPU players
        int humanPlayerCount = getNumberOfHumanPlayers(scanner);
        int computerPlayerCount = getNumberOfComputerPlayers(humanPlayerCount, scanner);
        int totalPlayerCount = humanPlayerCount + computerPlayerCount;

        System.out.printf("No. of Human players: %d\n", humanPlayerCount);
        System.out.printf("No. of Computer players: %d\n", computerPlayerCount);

        // init game objects
        // objects to initialise: cards, nobles, tokens

        // init cards (levels 1, 2, 3)
        ArrayList<Card> closedCardsLevel1 = new ArrayList<Card>();
        ArrayList<Card> closedCardsLevel2 = new ArrayList<Card>();
        ArrayList<Card> closedCardsLevel3 = new ArrayList<Card>();

        ArrayList<Card> openCardsLevel1 = new ArrayList<Card>();
        ArrayList<Card> openCardsLevel2 = new ArrayList<Card>();
        ArrayList<Card> openCardsLevel3 = new ArrayList<Card>();

        initClosedCards(closedCardsLevel1, closedCardsLevel2, closedCardsLevel3);
        transferCardsFromClosedToOpen(closedCardsLevel1, openCardsLevel1);
        transferCardsFromClosedToOpen(closedCardsLevel2, openCardsLevel2);
        transferCardsFromClosedToOpen(closedCardsLevel3, openCardsLevel3);

        // init nobles
        ArrayList<Noble> totalNobles = new ArrayList<Noble>();
        ArrayList<Noble> availNobles = new ArrayList<Noble>();
        initNobles(totalNobles);
        initAvailNobles(totalNobles, availNobles, totalPlayerCount);

        // init tokens
        // 2 players: 4x tokens per gem type
        // 3 players: 5x tokens per gem type
        // 4 players: 7x tokens per gem type
        // NOTE: these default settings can be changed in config.properties file 
        Token tokenData = new Token(totalPlayerCount);
    }
}
