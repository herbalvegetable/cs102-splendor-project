package src.com.splendor.model;

import java.util.*;
import src.com.splendor.game.DataLoader;
import src.com.splendor.game.NobleAcquisition;
import src.com.splendor.game.TurnInput;

public class PlayerTurn {
    private TurnInput input;
    private int maxTokenCount;

    public PlayerTurn(TurnInput input) {
        DataLoader dloader = new DataLoader();
        this.maxTokenCount = Integer.parseInt(dloader.getProperty("game.maxTokensPerPlayer"));
        this.input = input;
    }

    public void executeAction(Player player) {
        int validAction = -1;

        while (validAction < 0) {
            if (!input.isCPU()) displayActionMenu();

            int choice = input.getActionChoice(player);

            try {
                switch (choice) {
                    case 1:
                        validAction = take3DifferentTokens(player);
                        break;
                    case 2:
                        validAction = take2SameTokens(player);
                        break;
                    case 3:
                        validAction = reserveCard(player);
                        break;
                    case 4:
                        validAction = buyCard(player);
                        break;
                    default:
                        if (!input.isCPU()) System.out.println("Invalid choice. Please enter 1-4.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        if (player.getTokens().size() > this.maxTokenCount) {
            if (!input.isCPU()) {
                System.out.printf("You have %d tokens over the limit of 10\n",
                        player.getTokens().size() - this.maxTokenCount);
                System.out.println("You need to discard them.");
            }
            while (player.getTokens().size() > this.maxTokenCount) {
                returnTokens(player);
            }
        }

        NobleAcquisition.processNobleAcquisition(player, input);
    }

    private void returnTokens(Player player) {
        String[] gemTypes = {"black", "blue", "green", "red", "white", "gold"};
        while (true) {
            if (!input.isCPU()) {
                System.out.println("\nAvailable colors: black, blue, green, red, white, gold");
                System.out.print("\nChoose Token to Return: ");
            }
            String returntoken = input.getTokenToReturn(player);
            int gemIndex = -1;
            for (int i = 0; i < gemTypes.length; i++) {
                if (gemTypes[i].equals(returntoken)) {
                    gemIndex = i;
                    break;
                }
            }
            if (gemIndex == -1) {
                if (!input.isCPU()) System.out.println("\nIncorrect Gem Type. Try again.");
                continue;
            }
            if (gemIndex == 5) {
                if (player.getGoldTokenCount() > 0) {
                    player.removePlayerTokens(gemIndex, 1);
                    return;
                }
            } else {
                if (player.getGemTokenCount(gemIndex) > 0) {
                    player.removePlayerTokens(gemIndex, 1);
                    return;
                }
            }
            if (!input.isCPU()) System.out.println("Insufficient token. Try Again.");
        }
    }

    private void displayActionMenu() {
        System.out.println("\n--- CHOOSE ACTION ---");
        System.out.println("1. Take 3 different gem type tokens ");
        System.out.println("2. Take 2 tokens of the same gem type");
        System.out.println("3. Reserve a card");
        System.out.println("4. Buy a card");
        System.out.print("Enter choice (1-4): ");
    }

    private int take3DifferentTokens(Player player) {
        if (!input.isCPU()) {
            System.out.println("\nAvailable colors: black, blue, green, red, white");
            System.out.print("Enter 3 different colors (separated by spaces): ");
        }
        String inputStr = input.getThreeTokenColors(player);
        String[] gemTypes = inputStr.split("\\s+");

        if (gemTypes.length != 3) {
            if (!input.isCPU()) System.out.println("Please select exactly 3 gem types.");
            return -1;
        }
        Set<String> uniqueTypes = new HashSet<>(Arrays.asList(gemTypes));
        if (uniqueTypes.size() != 3) {
            if (!input.isCPU()) System.out.println("All 3 gem types must be different.");
            return -1;
        }
        for (String gemType : gemTypes) {
            if (!Token.checkGemType(gemType)) {
                if (!input.isCPU()) System.out.println("Please enter valid gem types");
                return -1;
            }
        }
        for (String gemType : gemTypes) {
            if (TokenPile.getTokenCount(gemType) < 1) {
                if (!input.isCPU()) System.out.println("Not enough " + gemType + " tokens available.");
                return -1;
            }
        }
        for (String gemType : gemTypes) {
            TokenPile.removeToken(gemType, 1);
            player.addToken(new Token(gemType));
        }
        if (input.isCPU()) {
            System.out.println("[CPU Player " + player.getPlayerID() + "] Tokens taken successfully!");
            try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        } else {
            System.out.println("Tokens taken successfully!");
        }
        return 1;
    }

    private int take2SameTokens(Player player) {
        if (!input.isCPU()) {
            System.out.println("\nAvailable gemType: black, blue, green, red, white");
            System.out.println("You can choose ONE ONLY");
            System.out.print("Enter gemType: ");
        }
        String gemType = input.getTwoTokenColor(player);

        if (TokenPile.getTokenCount(gemType) < 4) {
            if (!input.isCPU()) System.out.println("There must be at least 4 " + gemType + " tokens available to take 2.");
            return -1;
        }
        TokenPile.removeToken(gemType, 2);
        player.addToken(new Token(gemType));
        player.addToken(new Token(gemType));
        if (input.isCPU()) {
            System.out.println("[CPU Player " + player.getPlayerID() + "] 2 " + gemType + " tokens taken successfully!");
            try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        } else {
            System.out.println("2 " + gemType + " tokens taken successfully!");
        }
        return 2;
    }

    private int reserveCard(Player player) {
        if (player.getReservedCards().size() >= 3) {
            if (!input.isCPU()) System.out.println("You already have 3 reserved cards (maximum).");
            return -1;
        }
        if (!input.isCPU()) {
            System.out.println("\n--- RESERVE OPTIONS ---");
            System.out.println("1. Reserve a face-up card from the table");
            System.out.println("2. Draw blindly from a deck (if you're feeling lucky)");
            System.out.print("Enter choice (1 or 2): ");
        }
        int reserveChoice = input.getReserveChoice(player);
        if (reserveChoice == 2) return reserveCardFromDeck(player);
        if (reserveChoice != 1) {
            if (!input.isCPU()) System.out.println("Invalid choice. Please enter 1 or 2.");
            return -1;
        }
        if (!input.isCPU()) {
            System.out.println("\nReserve from which level?");
            System.out.print("Enter level (1/2/3): ");
        }
        int level = input.getReserveLevel(player);
        ArrayList<Card> openCards = getOpenCardsByLevel(level);
        if (openCards == null || openCards.isEmpty()) {
            if (!input.isCPU()) System.out.println("No cards available at this level.");
            return -1;
        }
        if (!input.isCPU()) System.out.print("Enter card index (0-" + (openCards.size() - 1) + "): ");
        int index = input.getReserveCardIndex(player, level);
        if (index < 0 || index >= openCards.size()) {
            if (!input.isCPU()) System.out.println("Invalid card index.");
            return -1;
        }
        Card card = openCards.remove(index);
        player.addReservedCard(card);
        boolean gotGold = false;
        if (TokenPile.getTokenCount("gold") > 0) {
            TokenPile.removeToken("gold", 1);
            player.addToken(new Token("gold"));
            gotGold = true;
        }
        if (input.isCPU()) {
            System.out.println("[CPU Player " + player.getPlayerID() + "] Card reserved" + (gotGold ? " + 1 gold" : ""));
            try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        } else {
            System.out.println(gotGold ? "Card reserved and you receive 1 gold token." : "Card reserved but no gold tokens available.");
        }
        CardMarket.transferFromClosedToOpen(getClosedCardsByLevel(level), openCards);
        return 3;
    }

    private int reserveCardFromDeck(Player player) {
        if (!input.isCPU()) {
            System.out.println("\nDraw from which deck level?");
            System.out.print("Enter level (1/2/3): ");
        }
        int level = input.getReserveLevel(player);
        if (level < 1 || level > 3) {
            if (!input.isCPU()) System.out.println("Invalid level. Enter 1, 2, or 3.");
            return -1;
        }
        ArrayList<Card> closedCards = getClosedCardsByLevel(level);
        if (closedCards == null || closedCards.isEmpty()) {
            if (!input.isCPU()) System.out.println("No cards left in this deck.");
            return -1;
        }
        Card card = CardMarket.drawFromClosedDeck(level);
        if (card == null) {
            if (!input.isCPU()) System.out.println("No cards left in this deck.");
            return -1;
        }
        player.addReservedCard(card);
        if (TokenPile.getTokenCount("gold") > 0) {
            TokenPile.removeToken("gold", 1);
            player.addToken(new Token("gold"));
        }
        if (input.isCPU()) {
            System.out.println("[CPU Player " + player.getPlayerID() + "] Drew blindly from Level " + level + " deck and reserved");
            try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        } else {
            System.out.println("You drew a card blindly from the Level " + level + " deck and reserved it.");
        }
        return 3;
    }

    private ArrayList<Card> getOpenCardsByLevel(int level) {
        return CardMarket.getOpenCardsByLevel(level);
    }

    private ArrayList<Card> getClosedCardsByLevel(int level) {
        return CardMarket.getClosedCardsByLevel(level);
    }

    private int buyCard(Player player) {
        if (!input.isCPU()) {
            System.out.println("\n--- CHOOSE BUY ACTION ---");
            System.out.println("1. Buy from Marketplace");
            System.out.println("2. Buy from Reserved Cards");
            System.out.print("Enter choice (1 or 2): ");
        }
        int buyAction = input.getBuySource(player);

        if (buyAction == 1) {
            if (!input.isCPU()) {
                System.out.println("\n--- SELECT WHICH CARD LEVEL TO BUY FROM---");
                System.out.print("Enter level (1/2/3): ");
            }
            int level = input.getBuyLevel(player);
            ArrayList<Card> openCards = getOpenCardsByLevel(level);
            if (openCards == null || openCards.isEmpty()) {
                if (!input.isCPU()) System.out.println("No cards available at this level.");
                return -1;
            }
            if (!input.isCPU()) System.out.print("Enter card index (0-" + (openCards.size() - 1) + "): ");
            int index = input.getBuyCardIndex(player, level);
            if (index < 0 || index >= openCards.size()) {
                if (!input.isCPU()) System.out.println("Invalid card index.");
                return -1;
            }
            Card card = openCards.get(index);
            if (!canAfford(player, card)) {
                if (!input.isCPU()) System.out.println("Not enough Gems to purchase. Please select another card.");
                return -1;
            }
            if (!input.isCPU()) {
                System.out.println("Purchase possible. Enter any key to confirm. Enter \"N\" to cancel.");
            }
            if (!input.confirmPurchase(player, card)) {
                if (!input.isCPU()) System.out.println("Purchase Cancelled.");
                return -1;
            }
            processPurchase(player, card);
            openCards.remove(index);
            player.addBoughtCard(card);
            CardMarket.transferFromClosedToOpen(getClosedCardsByLevel(level), openCards);
            if (input.isCPU()) {
                System.out.println("[CPU Player " + player.getPlayerID() + "] Purchased card (Level " + card.getLevel() + ", " + card.getGemType() + ")");
                try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
            return 4;
        }

        if (buyAction == 2) {
            if (player.getReservedCards().isEmpty()) {
                if (!input.isCPU()) System.out.println("\nYou have no reserved cards at the moment.");
                return -1;
            }
            if (!input.isCPU()) System.out.printf("Enter card index (0-%d)\n", player.getReservedCards().size() - 1);
            int index = input.getReservedCardIndex(player);
            if (index < 0 || index >= player.getReservedCards().size()) {
                if (!input.isCPU()) System.out.println("Invalid index.");
                return -1;
            }
            Card card = player.getReservedCards().get(index);
            if (!canAfford(player, card)) {
                if (!input.isCPU()) System.out.println("Not enough Gems to purchase. Please select another card.");
                return -1;
            }
            if (!input.isCPU()) System.out.println("Purchase possible. Enter any key to confirm. Enter \"N\" to cancel.");
            if (!input.confirmPurchase(player, card)) {
                if (!input.isCPU()) System.out.println("Purchase Cancelled.");
                return -1;
            }
            processPurchase(player, card);
            player.removeReservedCard(card);
            player.addBoughtCard(card);
            if (input.isCPU()) {
                System.out.println("[CPU Player " + player.getPlayerID() + "] Purchased reserved card");
                try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
            return 4;
        }

        if (!input.isCPU()) System.out.println("Invalid Buy Action number. Try Again.");
        return -1;
    }

    private boolean canAfford(Player player, Card card) {
        int gold = player.getGoldTokenCount();
        for (int i = 0; i < 5; i++) {
            int cost = card.getPurchasePrice().charAt(i) - '0';
            int bonus = player.getBoughtCardsGemValueCount(i);
            int needed = Math.max(0, cost - bonus);
            int has = player.getGemTokenCount(i);
            if (has < needed) {
                if (gold < needed - has) return false;
                gold -= (needed - has);
            }
        }
        return true;
    }

    private void processPurchase(Player player, Card card) {
        for (int i = 0; i < 5; i++) {
            int cost = card.getPurchasePrice().charAt(i) - '0';
            int bonus = player.getBoughtCardsGemValueCount(i);
            int needed = Math.max(0, cost - bonus);
            int toRemove = Math.min(player.getGemTokenCount(i), needed);
            player.removePlayerTokens(i, toRemove);
            if (needed - toRemove > 0) {
                player.removePlayerTokens(5, needed - toRemove);
            }
        }
    }
}
