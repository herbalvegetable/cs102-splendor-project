package src.com.splendor.game;

import java.util.ArrayList;
import src.com.splendor.model.Card;
import src.com.splendor.model.Noble;
import src.com.splendor.model.Player;

/**
 * CPU turn input - uses CPUStrategy for decisions, prints descriptions with 1 second delay.
 */
public class CPUTurnInput implements TurnInput {

    private static final int DELAY_MS = 1000;
    private final CPUStrategy strategy;
    private final int playerId;

    public CPUTurnInput(CPUStrategy strategy, int playerId) {
        this.strategy = strategy;
        this.playerId = playerId;
    }

    @Override
    public boolean isCPU() {
        return true;
    }

    private void printAndDelay(String message) {
        System.out.println("[CPU Player " + playerId + "] " + message);
        try {
            Thread.sleep(DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public int getActionChoice(Player player) {
        int choice = strategy.chooseAction(player);
        String desc = switch (choice) {
            case 1 -> "Taking 3 different gem tokens";
            case 2 -> "Taking 2 tokens of the same color";
            case 3 -> "Reserving a card";
            case 4 -> "Buying a card";
            default -> "Choosing action " + choice;
        };
        printAndDelay(desc);
        return choice;
    }

    @Override
    public String getThreeTokenColors(Player player) {
        String colors = strategy.chooseThreeTokenColors(player);
        printAndDelay("Selecting colors: " + colors);
        return colors;
    }

    @Override
    public String getTwoTokenColor(Player player) {
        String color = strategy.chooseTwoTokenColor(player);
        printAndDelay("Selecting 2 " + color + " tokens");
        return color;
    }

    @Override
    public int getReserveChoice(Player player) {
        int choice = strategy.chooseReserveSource(player);
        String desc = choice == 1 ? "Reserving from the table" : "Drawing blindly from deck";
        printAndDelay(desc);
        return choice;
    }

    @Override
    public int getReserveLevel(Player player) {
        int level = strategy.chooseReserveLevel(player);
        printAndDelay("Choosing Level " + level + " cards");
        return level;
    }

    @Override
    public int getReserveCardIndex(Player player, int level) {
        int index = strategy.chooseReserveCardIndex(player, level);
        printAndDelay("Selecting card at index " + index);
        return index;
    }

    @Override
    public int getBuySource(Player player) {
        int choice = strategy.chooseBuySource(player);
        String desc = choice == 1 ? "Buying from marketplace" : "Buying from reserved cards";
        printAndDelay(desc);
        return choice;
    }

    @Override
    public int getBuyLevel(Player player) {
        int level = strategy.chooseBuyLevel(player);
        printAndDelay("Choosing Level " + level + " cards");
        return level;
    }

    @Override
    public int getBuyCardIndex(Player player, int level) {
        int index = strategy.chooseBuyCardIndex(player, level);
        printAndDelay("Selecting card at index " + index);
        return index;
    }

    @Override
    public int getReservedCardIndex(Player player) {
        int index = strategy.chooseReservedCardIndex(player);
        printAndDelay("Selecting reserved card at index " + index);
        return index;
    }

    @Override
    public String getTokenToReturn(Player player) {
        String token = strategy.chooseTokenToReturn(player);
        printAndDelay("Returning 1 " + token + " token (over limit)");
        return token;
    }

    @Override
    public boolean confirmPurchase(Player player, Card card) {
        printAndDelay("Confirming purchase of card (Level " + card.getLevel() + ", " + card.getGemType() + ")");
        return true;
    }

    @Override
    public int getNobleChoice(Player player, ArrayList<Noble> nobles) {
        int choice = strategy.chooseNoble(player, nobles);
        if (!nobles.isEmpty() && choice >= 0 && choice < nobles.size()) {
            printAndDelay("Choosing noble: " + nobles.get(choice).getName());
        }
        return choice;
    }
}
