package src.com.splendor.game;

import java.util.*;
import src.com.splendor.model.*;

public class GameState {

    // added by vg 14/2 ///////////////////////////////////////////////////////////////////////////////////////////////
    //ATTRIBUTES
    private ArrayList<Player> players;
    private int currentPlayerIndex = 0;
    private boolean gameOver = false;
    private Scanner sc;
    //constructor for gamestate, i doing this so that i can use the game as an object
    public GameState(ArrayList<Player> players, Scanner sc) {
        this.players = players;
        this.sc = sc;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

    // added by vg 14/2 ///////////////////////////////////////////////////////////////////////////////////////////////
    // display current board state
    public static void displayCurrentBoardState() { 
        System.out.println("\n--- GAME BOARD ---");

         // display available tokens
        System.out.println("\nAvailable Tokens:");
        System.out.println("  White: " + TokenPile.getTokenCount("white") + " | Blue: " + TokenPile.getTokenCount("blue") + 
                        " | Green: " + TokenPile.getTokenCount("green") + " | Red: " + TokenPile.getTokenCount("red") + 
                        " | Black: " + TokenPile.getTokenCount("black") + " | Gold: " + TokenPile.getTokenCount("gold"));


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
            System.out.println("  (None available)"); // idt this will ever happen in game but lets just keep a check here
        } else {
            for (int i = 0; i < cards.size(); i++) {
                System.out.println("  [" + i + "] " + cards.get(i)); // reminder to go and add a toString method in card class
            }
        }
    }

    // method to display nobles
    private static void displayNobles(ArrayList<Noble> nobles) {
        if (nobles.isEmpty()) { 
            System.out.println("  (None available)"); // idt this will ever happen in game but lets just keep a check here
        } else {
            for (int i = 0; i < nobles.size(); i++) {
                System.out.println("  [" + i + "] " + nobles.get(i)); // reminder to go and add a toString method in nobles class
            }
        }
    }

    // a method to print out player's inventory eg. tokens, points, nobles //reminder to create
    private void displayPlayerState(Player player){



    }

    private void checkGameEnd(){
        if (currentPlayerIndex == 3) { // THIS IS JUST TEMPORARY CODE SO THAT I CAN TEST IT, NEED TO WRITE A REAL 
            gameOver = true;            // CHECK END GAME FUNCTION
        }
    };
    

    // MAIN GAME LOOP, CRUCIAL
    public void playGame() {
        System.out.println("\n===== GAME START =====\n");
    
        // Main game loop
        while (!gameOver) {
            playTurn();
            nextPlayer();
            checkGameEnd();
        }
        
        System.out.println("\n===== GAME OVER =====\n");
    }

    // rotate to next player
    private void nextPlayer() {
        System.out.println("\n========================================");
        System.out.println("READY FOR THE NEXT PLAYER'S TURN? ENTER ANY BUTTON");
        System.out.println("========================================");
        sc.next();
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }


    // activate player's turn
    private void playTurn() {
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

    private void displayChangeInBoard(){

        
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // introductory text
        System.out.println("===== SPLENDOR =====");
        System.out.println("Welcome to the world of Splendor!\n");

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


        // added by vg 14/2//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // initialise players
        ArrayList<Player> players = new ArrayList<>();
        for (int i = 1; i <= totalPlayerCount; i++) { // player ID starts from 1
            players.add(new Player(i));
        }

        // start the game
        GameState game = new GameState(players, sc);
        game.playGame();
    }
}
