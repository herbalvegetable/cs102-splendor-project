package src.com.splendor.game;

import java.util.ArrayList;
import src.com.splendor.model.Card;
import src.com.splendor.model.CardMarket;
import src.com.splendor.model.Noble;
import src.com.splendor.model.NoblePool;
import src.com.splendor.model.Player;
import src.com.splendor.model.TokenPile;

/**
 * Handles all display/presentation logic for the game.
 */
public class GameDisplay {

    public static void displayCurrentBoardState() {
        System.out.println("\n--- GAME BOARD ---");

        System.out.println("\nAvailable Tokens:");
        System.out.println("  ⚪ White: " + TokenPile.getTokenCount("white") + " | 🔵 Blue: "
                + TokenPile.getTokenCount("blue") +
                " | 🟢 Green: " + TokenPile.getTokenCount("green") + " | 🔴 Red: " + TokenPile.getTokenCount("red") +
                " | ⚫ Black: " + TokenPile.getTokenCount("black") + " | 🟡 Gold: " + TokenPile.getTokenCount("gold"));

        System.out.println("\nLevel 3 Cards:");
        displayCardLevel(CardMarket.getOpenLevel3());

        System.out.println("\nLevel 2 Cards:");
        displayCardLevel(CardMarket.getOpenLevel2());

        System.out.println("\nLevel 1 Cards:");
        displayCardLevel(CardMarket.getOpenLevel1());

        System.out.println("\nAvailable Nobles:");
        displayNobles(NoblePool.getAvailNobles());
    }

    public static void displayCardLevel(ArrayList<Card> cards) {
        if (cards.isEmpty()) {
            System.out.println("  (None available)");
        } else {
            for (int i = 0; i < cards.size(); i++) {
                System.out.println("  [" + i + "] " + cards.get(i));
            }
        }
    }

    public static void displayNobles(ArrayList<Noble> nobles) {
        if (nobles.isEmpty()) {
            System.out.println("  (None available)");
        } else {
            for (int i = 0; i < nobles.size(); i++) {
                System.out.println("  [" + i + "] " + nobles.get(i));
            }
        }
    }

    public static void displayPlayerState(Player player) {
        System.out.println("\n--- PLAYER " + player.getPlayerID() + " INVENTORY ---");

        System.out.println("\nCurrent Tokens:\n");
        System.out.printf(" ⚫ Black: %d | 🔵 Blue: %d | 🟢 Green: %d | 🔴 Red: %d | ⚪ White: %d | 🟡 Gold: %d%n",
                player.getGemTokenCount(0),
                player.getGemTokenCount(1),
                player.getGemTokenCount(2),
                player.getGemTokenCount(3),
                player.getGemTokenCount(4),
                player.getGoldTokenCount());

        System.out.println("\nPrestige Points: " + player.getPrestigePoints());
        System.out.println("\nPlayer Nobles: ");
        for (Noble noble : player.getNobles()) {
            System.out.println("\n" + noble);
        }
    }

    public static void displayChangeInBoard(Player currentPlayer) {
        System.out.println("\n--- BOARD AFTER YOUR ACTION ---");
        displayCurrentBoardState();
        displayPlayerState(currentPlayer);
    }

    public static void clearScreen() {
        System.out.print("\033c");
        System.out.flush();
    }
}
