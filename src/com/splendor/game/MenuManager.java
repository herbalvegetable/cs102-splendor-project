package src.com.splendor.game;

import java.util.ArrayList;
import java.util.Scanner;
import src.com.splendor.model.CardMarket;
import src.com.splendor.model.NoblePool;
import src.com.splendor.model.Player;
import src.com.splendor.model.TokenPile;

/**
 * Handles main menu, rules display, and game setup.
 */
public class MenuManager {

    public static void showRules(Scanner sc) {
        System.out.println("\n==== RULES ====");
        System.out.println();
        System.out.println("1. Youtube Video Link");
        System.out.println("https://youtu.be/zDPU00R4Txk?si=qHuNYjaaXeOcSFeF");
        System.out.println();
        System.out.println("2. PDF Rule Book");
        System.out.println("https://officialgamerules.org/wp-content/uploads/2025/02/Splendor-rulebook.pdf");
        System.out.println("\n========================================");
        System.out.println("Press ENTER to return to menu");
        System.out.println("========================================");
        sc.nextLine();
    }

    public static void startGame(Scanner sc) {
        int humanPlayerCount = getNumberOfHumanPlayers(sc);
        int computerPlayerCount = getNumberOfComputerPlayers(humanPlayerCount, sc);
        int totalPlayerCount = humanPlayerCount + computerPlayerCount;

        System.out.printf("No. of Human players: %d\n", humanPlayerCount);
        System.out.printf("No. of Computer players: %d\n", computerPlayerCount);

        CardMarket.initialise();
        NoblePool.initialise(totalPlayerCount);
        TokenPile.initialise(totalPlayerCount);

        ArrayList<Player> players = new ArrayList<>();
        for (int i = 1; i <= totalPlayerCount; i++) {
            boolean isHuman = i <= humanPlayerCount;
            players.add(new Player(i, isHuman));
        }

        GameState game = new GameState(players, sc);
        game.playGame();
    }

    public static int getNumberOfHumanPlayers(Scanner sc) {
        int humanPlayers;
        while (true) {
            humanPlayers = InputHelper.getInt("Enter number of Human players (1/2/3/4): ", "Not a valid int", sc);
            if (humanPlayers >= 1 && humanPlayers <= 4) {
                break;
            }
            System.out.println("Outside of range, enter within 1/2/3/4");
        }
        return humanPlayers;
    }

    public static int getNumberOfComputerPlayers(int humanPlayers, Scanner sc) {
        int computerPlayers;
        int maxComputerPlayers = 4 - humanPlayers;
        while (true) {
            computerPlayers = InputHelper.getInt(
                    String.format("Enter number of CPU players (%d to %d): ", 0, maxComputerPlayers),
                    "Not a valid int", sc);
            if (computerPlayers >= 0 && computerPlayers <= maxComputerPlayers) {
                break;
            }
            System.out.printf("Outside of range, enter within (%d to %d)\n", 0, maxComputerPlayers);
        }
        return computerPlayers;
    }

    public static void runMainMenu(Scanner sc) {
        System.out.println("===== SPLENDOR =====");
        System.out.println("Welcome to the world of Splendor!\n");

        while (true) {
            System.out.println("===== SPLENDOR MENU =====");
            System.out.println("1. Start game");
            System.out.println("2. Show Rules");
            System.out.println("3. Quit Game");

            int playerOption = InputHelper.getInt("Enter your choice: ", "Invalid input. Please enter 1, 2, or 3", sc);
            sc.nextLine();

            if (playerOption == 1) {
                startGame(sc);
            } else if (playerOption == 2) {
                showRules(sc);
            } else if (playerOption == 3) {
                System.out.println("Thank You for Playing. See You in Another Time");
                sc.close();
                System.exit(0);
            } else {
                System.out.println("Invalid input. Please enter 1, 2, or 3");
            }
        }
    }
}
