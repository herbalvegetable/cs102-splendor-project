package src.com.splendor.game;

import java.util.*;
import src.com.splendor.model.*;

public class GameState {

    // added by vg 14/2
    // ///////////////////////////////////////////////////////////////////////////////////////////////
    // ATTRIBUTES
    private ArrayList<Player> players;
    private int currentPlayerIndex = 0;
    private boolean gameOver = false;
    private boolean finalRound = false;
    private Scanner sc;

    // constructor for gamestate, i doing this so that i can use the game as an
    // object
    public GameState(ArrayList<Player> players, Scanner sc) {
        this.players = players;
        this.sc = sc;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////

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

    // added by vg 14/2
    // ///////////////////////////////////////////////////////////////////////////////////////////////
    // display current board state
    public static void displayCurrentBoardState() {
        System.out.println("\n--- GAME BOARD ---");

        // display available tokens
        System.out.println("\nAvailable Tokens:");
        System.out.println("  ⚪ White: " + TokenPile.getTokenCount("white") + " | 🔵 Blue: "
                + TokenPile.getTokenCount("blue") +
                " | 🟢 Green: " + TokenPile.getTokenCount("green") + " | 🔴 Red: " + TokenPile.getTokenCount("red") +
                " | ⚫ Black: " + TokenPile.getTokenCount("black") + " | 🟡 Gold: " + TokenPile.getTokenCount("gold"));

        // display available cards
        System.out.println("\nLevel 3 Cards:");
        displayCardLevel(Card.getOpenLevel3());

        System.out.println("\nLevel 2 Cards:");
        displayCardLevel(Card.getOpenLevel2());

        System.out.println("\nLevel 1 Cards:");
        displayCardLevel(Card.getOpenLevel1());

        // display nobles
        System.out.println("\nAvailable Nobles:");
        displayNobles(Noble.getAvailNobles());
    }

    // method to display cards of a level
    private static void displayCardLevel(ArrayList<Card> cards) {
        if (cards.isEmpty()) {
            System.out.println("  (None available)"); // idt this will ever happen in game but lets just keep a check
                                                      // here
        } else {
            for (int i = 0; i < cards.size(); i++) {
                System.out.println("  [" + i + "] " + cards.get(i)); // reminder to go and add a toString method in card
                                                                     // class
            }
        }
    }

    // method to display nobles
    private static void displayNobles(ArrayList<Noble> nobles) {
        if (nobles.isEmpty()) {
            System.out.println("  (None available)"); // idt this will ever happen in game but lets just keep a check
                                                      // here
        } else {
            for (int i = 0; i < nobles.size(); i++) {
                System.out.println("  [" + i + "] " + nobles.get(i)); // reminder to go and add a toString method in
                                                                      // nobles class
            }
        }
    }

    // method to print out player's inventory eg. tokens, points, nobles
    private void displayPlayerState(Player player) {

        Player currentPlayer = players.get(currentPlayerIndex);

        System.out.println("\n--- PLAYER " + currentPlayer.getPlayerID() + " INVENTORY ---");

        // Display Player Tokens
        System.out.println("\nCurrent Tokens:\n");

        System.out.printf(" ⚫ Black: %d | 🔵 Blue: %d | 🟢 Green: %d | 🔴 Red: %d | ⚪ White: %d | 🟡 Gold: %d%n",
                currentPlayer.getGemTokenCount(0),
                currentPlayer.getGemTokenCount(1),
                currentPlayer.getGemTokenCount(2),
                currentPlayer.getGemTokenCount(3),
                currentPlayer.getGemTokenCount(4),
                currentPlayer.getGoldTokenCount());

        // Display Player points

        System.out.println("\nPrestige Points: " + currentPlayer.getPrestigePoints());

        // Display Player nobles
        System.out.println("\nPlayer Nobles: ");
        for (Noble noble : currentPlayer.getNobles()) {
            System.out.println("\n" + noble);
        }

    }

    // Checks if anyone reaches 15 prestige points at end of each round
    private void checkGameEnd() {
        for (Player player : players) {
            // test by reducing 15 to 1 
            if (player.getPrestigePoints() >= 15) {
                finalRound = true;
            }
        }
        // game only ends when prestige points reach 15 which indicates final round 
        // and only when ALL players got their turn 
        if (finalRound && currentPlayerIndex == players.size() - 1) {
            gameOver = true;
        }
        // if (currentPlayerIndex == 3) { // THIS IS JUST TEMPORARY CODE SO THAT I CAN
        // TEST IT, NEED TO WRITE A REAL
        // gameOver = true; // CHECK END GAME FUNCTION
        // }
    }

    // Need to Complete: Calculate Winner
    public void calculateWinner() {
        int maxBoughtCards = Integer.MIN_VALUE;
        int minBoughtCards = Integer.MAX_VALUE;
    
        for (Player player : players) {
            if (player.getPrestigePoints() > maxBoughtCards) {
                maxBoughtCards = player.getPrestigePoints();
            }
        }

        ArrayList<Player> tiedPlayers = new ArrayList<>();
        for (Player player : players) {
            if (player.getPrestigePoints() == maxBoughtCards) {
                tiedPlayers.add(player);
            }
        }

        if (tiedPlayers.size() == 1) {
            Player winnerPlayer = tiedPlayers.get(0);
            System.out.println("The Winner of Splendor is Player " + winnerPlayer.getPlayerID() + " with " + winnerPlayer.getPrestigePoints() + " points!");
        } else {
            // because the prestige is a tie, check players with fewest bought cards
            for (Player firstTidePlayer : tiedPlayers) {
                if (firstTidePlayer.getBoughtCards().size() < minBoughtCards) {
                    minBoughtCards = firstTidePlayer.getBoughtCards().size();
                }
            }

            ArrayList<Player> playerLowestBoughtCards = new ArrayList<>();
            for (Player firstTiedPlayer : tiedPlayers) {
                if (firstTiedPlayer.getBoughtCards().size() == minBoughtCards) {
                    playerLowestBoughtCards.add(firstTiedPlayer);
                }
            }

            if (playerLowestBoughtCards.size() == 1) {
                Player winnerPlayer = playerLowestBoughtCards.get(0);
                System.out.println("The Winner of Splendor is Player " + winnerPlayer.getPlayerID() + " with " + winnerPlayer.getPrestigePoints() + " points!");
            } else {
                // if the amount of fewest bought cards tied AGAIN, find players with the fewest reserved cards  
                for (Player firstPlayerReserved: playerLowestBoughtCards) {
                    if (firstPlayerReserved.getReservedCards().size() < minBoughtCards) {
                        minBoughtCards = firstPlayerReserved.getReservedCards().size();
                    }
                }

                ArrayList<Player> playerLowestReservedCards = new ArrayList<>();
                for (Player firstPlayer : playerLowestBoughtCards) {
                    if (firstPlayer.getReservedCards().size() == minBoughtCards) {
                        playerLowestReservedCards.add(firstPlayer);
                    }
                }

                if (playerLowestReservedCards.size() == 1) {
                    Player winnerPlayer = playerLowestReservedCards.get(0); 
                    System.out.println("The Winner of Splendor is Player " + winnerPlayer.getPlayerID() + " with " + winnerPlayer.getPrestigePoints() + " points!");
                } else {
                    // if still tie, then they win together 
                    System.out.println("It's a tie! The Winner of Splenor are Player ");
                    for (Player winner : playerLowestReservedCards) {
                        System.out.println(winner.getPlayerID() + " ");
                    }
                    System.out.println("with " + playerLowestReservedCards.get(0).getPrestigePoints() + " points");
                }
            }
        }

    }

    // MAIN GAME LOOP, CRUCIAL
    public void playGame() {
        System.out.println("\n===== GAME START =====\n");

        // Main game loop
        while (!gameOver) {
            playTurn();
            checkGameEnd();
            nextPlayer();
        }

        System.out.println("\n===== GAME OVER =====\n");
        calculateWinner();

        // if want to stop right away
        // System.exit(0); 

        // if want to have options to continue or not 
        System.out.println("Do you want to play again?");
        System.out.println("\n 1.Restart the Game");
        System.out.println("2. Return to Main Menu");
        System.out.println("3. Quit");

        int playerOption = getInt("Enter your choice: ", "Invalid input. Please enter 1, 2, or 3", sc);
            sc.nextLine();

            if (playerOption == 1) {
                startGame(sc);
            } else if (playerOption == 2) {
                return;
            } else if (playerOption == 3) {
                System.out.println("Thank You for Playing. See You in Another Time");
                sc.close();
                System.exit(0);
            } else {
                System.out.println("Invalid input. Please enter 1, 2, or 3");
            }
    }

    // rotate to next player
    private void nextPlayer() {
        System.out.println("\n========================================");
        System.out.println("READY FOR THE NEXT PLAYER'S TURN? ENTER ANY BUTTON");
        System.out.println("========================================");
        sc.next();
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    // clear player's terminal
    public static void clearScreen() {
        System.out.println("\033c");
        System.out.flush();
    }

    // activate player's turn
    private void playTurn() {
        clearScreen();
        Player currentPlayer = players.get(currentPlayerIndex);

        System.out.println("\n========================================");
        System.out.println("PLAYER " + currentPlayer.getPlayerID() + "'s TURN");
        System.out.println("========================================");

        // Display player state
        displayPlayerState(currentPlayer);

        // Display game board
        displayCurrentBoardState();

        // Execute player's action
        PlayerTurn currentTurn = new PlayerTurn(sc);
        currentTurn.executeAction(currentPlayer);

        displayChangeInBoard();
    }

    // isnt this repeat of current?
    private void displayChangeInBoard() {

    }

    private static void showRules(Scanner sc) {

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

    private static void startGame(Scanner sc) {
        // init number of human & CPU players
        int humanPlayerCount = getNumberOfHumanPlayers(sc);
        int computerPlayerCount = getNumberOfComputerPlayers(humanPlayerCount, sc);
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
        TokenPile.initialise(totalPlayerCount);

        // added by vg
        // 14/2//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // initialise players
        ArrayList<Player> players = new ArrayList<>();
        for (int i = 1; i <= totalPlayerCount; i++) { // player ID starts from 1
            players.add(new Player(i));
        }

        // start the game
        GameState game = new GameState(players, sc);
        game.playGame();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        // introductory text
        System.out.println("===== SPLENDOR =====");
        System.out.println("Welcome to the world of Splendor!\n");

        while (true) {
            System.out.println("===== SPLENDOR MENU =====");
            System.out.println("1. Start game");
            System.out.println("2. Show Rules");
            System.out.println("3. Quit Game");

            int playerOption = getInt("Enter your choice: ", "Invalid input. Please enter 1, 2, or 3", sc);
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
