package src.com.splendor.game;

import java.util.*;
import src.com.splendor.model.*;

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

    // returns item of type based on the input arraylist's type
    public static <T> T getRandomItemFromArray(ArrayList<T> arr) {
        Random rand = new Random();
        int max = arr.size() - 1;
        return arr.get(rand.nextInt(max + 1));
    }

    // display current board state
    public static void displayCurrentBoardState() {

    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // introductory text
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
        // init cards
        Card.initialise();
        // init nobles
        Noble.initialise(totalPlayerCount);
        // init tokens
        Token.initialise(totalPlayerCount);
    }
}
