package src.com.splendor.game;

import java.util.ArrayList;
import java.util.Scanner;
import src.com.splendor.model.Player;
import src.com.splendor.model.PlayerTurn;

/**
 * Orchestrates the game loop, turn progression, and win condition checking.
 */
public class GameState {

    private final ArrayList<Player> players;
    private int currentPlayerIndex = 0;
    private boolean gameOver = false;
    private boolean finalRound = false;
    private final Scanner sc;
    private final int pointsToWin;

    public GameState(ArrayList<Player> players, Scanner sc) {
        this.players = players;
        this.sc = sc;
        DataLoader dloader = new DataLoader();
        String pointsStr = dloader.getProperty("game.pointsToWin");
        this.pointsToWin = pointsStr != null ? Integer.parseInt(pointsStr) : 15;
    }

    public void playGame() {
        System.out.println("\n===== GAME START =====\n");

        while (!gameOver) {
            playTurn();
            checkGameEnd();
            nextPlayer();
        }

        System.out.println("\n===== GAME OVER =====\n");
        calculateWinner();
        showPostGameMenu();
    }

    private void playTurn() {
        GameDisplay.clearScreen();
        Player currentPlayer = players.get(currentPlayerIndex);

        System.out.println("\n========================================");
        System.out.println((currentPlayer.isHuman() ? "👨‍🦲 PLAYER " : "🤖 CPU PLAYER ") + currentPlayer.getPlayerID() + "'s TURN");
        System.out.println("========================================");

        GameDisplay.displayPlayerState(currentPlayer);
        GameDisplay.displayCurrentBoardState();

        TurnInput turnInput = currentPlayer.isHuman()
                ? new HumanTurnInput(sc)
                : new CPUTurnInput(new CPUStrategy(), currentPlayer.getPlayerID());
        PlayerTurn currentTurn = new PlayerTurn(turnInput);
        currentTurn.executeAction(currentPlayer);

        GameDisplay.displayChangeInBoard(players.get(currentPlayerIndex));
    }

    private void nextPlayer() {
        System.out.println("\n========================================");
        System.out.println("Press Enter to continue to the next turn");
        System.out.println("========================================");
        sc.nextLine();
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    private void checkGameEnd() {
        for (Player player : players) {
            if (player.getPrestigePoints() >= pointsToWin) {
                finalRound = true;
                break;
            }
        }
        if (finalRound && currentPlayerIndex == players.size() - 1) {
            gameOver = true;
        }
    }

    private void calculateWinner() {
        int maxPrestige = Integer.MIN_VALUE;
        for (Player player : players) {
            if (player.getPrestigePoints() > maxPrestige) {
                maxPrestige = player.getPrestigePoints();
            }
        }

        ArrayList<Player> tiedPlayers = new ArrayList<>();
        for (Player player : players) {
            if (player.getPrestigePoints() == maxPrestige) {
                tiedPlayers.add(player);
            }
        }

        if (tiedPlayers.size() == 1) {
            Player winnerPlayer = tiedPlayers.get(0);
            System.out.println("The Winner of Splendor is Player " + winnerPlayer.getPlayerID() + " with " + winnerPlayer.getPrestigePoints() + " points!");
        } else {
            int minBoughtCards = Integer.MAX_VALUE;
            for (Player player : tiedPlayers) {
                if (player.getBoughtCards().size() < minBoughtCards) {
                    minBoughtCards = player.getBoughtCards().size();
                }
            }

            ArrayList<Player> winners = new ArrayList<>();
            for (Player player : tiedPlayers) {
                if (player.getBoughtCards().size() == minBoughtCards) {
                    winners.add(player);
                }
            }

            if (winners.size() == 1) {
                Player winnerPlayer = winners.get(0);
                System.out.println("The Winner of Splendor is Player " + winnerPlayer.getPlayerID() + " with " + winnerPlayer.getPrestigePoints() + " points!");
            } else {
                System.out.print("It's a tie! The Winners of Splendor are Players ");
                for (int i = 0; i < winners.size(); i++) {
                    System.out.print(winners.get(i).getPlayerID());
                    if (i < winners.size() - 1) System.out.print(", ");
                }
                System.out.println(" with " + winners.get(0).getPrestigePoints() + " points.");
            }
        }
    }

    private void showPostGameMenu() {
        System.out.println("Do you want to play again?");
        System.out.println("\n 1.Restart the Game");
        System.out.println("2. Return to Main Menu");
        System.out.println("3. Quit");

        int playerOption = InputHelper.getInt("Enter your choice: ", "Invalid input. Please enter 1, 2, or 3", sc);
        sc.nextLine();

        if (playerOption == 1) {
            MenuManager.startGame(sc);
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

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        MenuManager.runMainMenu(sc);
    }
}
