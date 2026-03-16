package src.com.splendor.game;

import java.util.ArrayList;
import java.util.List;
import src.com.splendor.model.Noble;
import src.com.splendor.model.NoblePool;
import src.com.splendor.model.Player;

/**
 * Handles the game logic for noble acquisition when a player qualifies.
 */
public class NobleAcquisition {

    public static void processNobleAcquisition(Player player, TurnInput input) {
        List<Noble> qualifyingNobles = getQualifyingNobles(player);

        if (qualifyingNobles.isEmpty()) {
            return;
        }

        Noble chosenNoble;
        if (qualifyingNobles.size() == 1) {
            chosenNoble = qualifyingNobles.get(0);
            if (input.isCPU()) {
                System.out.println("[CPU Player " + player.getPlayerID() + "] Acquired Noble: " + chosenNoble.getName());
                try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            } else {
                System.out.println("Congratulations! You acquired a Noble: " + chosenNoble.getName());
            }
        } else {
            ArrayList<Noble> list = new ArrayList<>(qualifyingNobles);
            int choice;
            do {
                if (!input.isCPU()) {
                    System.out.println("\nYou qualify for more than one noble! Choose which to receive:");
                    for (int i = 0; i < list.size(); i++) {
                        System.out.println("  [" + i + "] " + list.get(i));
                    }
                    System.out.print("Enter choice (0-" + (list.size() - 1) + "): ");
                }
                choice = input.getNobleChoice(player, list);
                if (choice >= 0 && choice < list.size()) break;
                if (!input.isCPU()) System.out.println("Invalid choice. Try again.");
            } while (true);

            chosenNoble = list.get(choice);
            if (input.isCPU()) {
                System.out.println("[CPU Player " + player.getPlayerID() + "] Acquired Noble: " + chosenNoble.getName());
                try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            } else {
                System.out.println("Congratulations! You acquired a Noble: " + chosenNoble.getName());
            }
        }

        player.addNoble(chosenNoble);
        NoblePool.removeNoble(chosenNoble);
    }

    private static List<Noble> getQualifyingNobles(Player player) {
        List<Noble> qualifyingNobles = new ArrayList<>();

        for (Noble noble : NoblePool.getAvailNobles()) {
            if (playerHasEnoughBonuses(player, noble)) {
                qualifyingNobles.add(noble);
            }
        }
        return qualifyingNobles;
    }

    private static boolean playerHasEnoughBonuses(Player player, Noble noble) {
        for (int i = 0; i < 5; i++) {
            int permanentBonus = player.getBoughtCardsGemValueCount(i);
            int nobleRequirement = noble.getPurchasePrice().charAt(i) - '0';
            if (nobleRequirement > permanentBonus) {
                return false;
            }
        }
        return true;
    }
}
