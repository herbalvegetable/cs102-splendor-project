package src.com.splendor.game;

import java.util.ArrayList;
import java.util.Scanner;
import src.com.splendor.model.Card;
import src.com.splendor.model.Noble;
import src.com.splendor.model.Player;

/**
 * Human turn input - reads from Scanner with prompts.
 */
public class HumanTurnInput implements TurnInput {

    private final Scanner sc;

    public HumanTurnInput(Scanner sc) {
        this.sc = sc;
    }

    @Override
    public boolean isCPU() {
        return false;
    }

    @Override
    public int getActionChoice(Player player) {
        if (!sc.hasNextInt()) {
            sc.next();
            return -1;
        }
        int choice = sc.nextInt();
        sc.nextLine();
        return choice;
    }

    @Override
    public String getThreeTokenColors(Player player) {
        return sc.nextLine().toLowerCase().trim();
    }

    @Override
    public String getTwoTokenColor(Player player) {
        return sc.nextLine().toLowerCase().trim();
    }

    @Override
    public int getReserveChoice(Player player) {
        if (!sc.hasNextInt()) {
            sc.next();
            return -1;
        }
        int choice = sc.nextInt();
        sc.nextLine();
        return choice;
    }

    @Override
    public int getReserveLevel(Player player) {
        if (!sc.hasNextInt()) {
            sc.next();
            return -1;
        }
        int level = sc.nextInt();
        sc.nextLine();
        return level;
    }

    @Override
    public int getReserveCardIndex(Player player, int level) {
        if (!sc.hasNextInt()) {
            sc.next();
            return -1;
        }
        int index = sc.nextInt();
        sc.nextLine();
        return index;
    }

    @Override
    public int getBuySource(Player player) {
        if (!sc.hasNextInt()) {
            sc.next();
            return -1;
        }
        int choice = sc.nextInt();
        sc.nextLine();
        return choice;
    }

    @Override
    public int getBuyLevel(Player player) {
        if (!sc.hasNextInt()) {
            sc.next();
            return -1;
        }
        int level = sc.nextInt();
        sc.nextLine();
        return level;
    }

    @Override
    public int getBuyCardIndex(Player player, int level) {
        if (!sc.hasNextInt()) {
            sc.next();
            return -1;
        }
        int index = sc.nextInt();
        sc.nextLine();
        return index;
    }

    @Override
    public int getReservedCardIndex(Player player) {
        if (!sc.hasNextInt()) {
            sc.next();
            return -1;
        }
        int index = sc.nextInt();
        sc.nextLine();
        return index;
    }

    @Override
    public String getTokenToReturn(Player player) {
        return sc.nextLine().trim();
    }

    @Override
    public boolean confirmPurchase(Player player, Card card) {
        String input = sc.nextLine();
        return !input.equalsIgnoreCase("N");
    }

    @Override
    public int getNobleChoice(Player player, ArrayList<Noble> nobles) {
        if (!sc.hasNextInt()) {
            sc.next();
            return -1;
        }
        int choice = sc.nextInt();
        sc.nextLine();
        return choice;
    }
}
