package src.com.splendor.model;

import java.util.*;

public class PlayerTurn {
    private Scanner sc;
    
    public PlayerTurn(Scanner sc) {
        this.sc = sc;
    }
    
    public void executeAction(Player player) {
        int validAction = -1;
        
        while (validAction < 0) {
            displayActionMenu(); // prompt user to choose the action he wants to do
            
            if(!sc.hasNextInt()){
                System.out.println("Invalid input. Please enter a number (1 - 4):"); // catch of the user enters a NON number
                sc.next();
                continue;
            }
            
            int choice = sc.nextInt();
            sc.nextLine();
            
            // once user enters a number
            try {
                switch(choice) {
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
                        System.out.println("Invalid choice. Please enter 1-4."); // check if between 1 to 4
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        
        // Handle token limit (max 10)
        if(player.getTokens().size() > 10){
            System.out.printf("You have %d tokens over the limit of 10\n", player.getTokens().size() % 10);
            System.out.printf("You need to discard them");
            while (player.getTokens().size() > 10) {
                returnTokens(player);
            }
        }
        
    }

    // Handle token limit (max 10)
    private static void returnTokens(Player player){
        /* need to write a function that will
            1) make player choose what gemType token he wants to return
            2) check if player actually has that gemType
            3) if no, reprompt.
            4) if yes, minus that gemType from player & return to TokenPile
            5) repeat until player has 10 or less 

            note: no need to loop, this function is to discard ONE token ONLY
        */

    }
    
    private void displayActionMenu() {
        System.out.println("\n--- CHOOSE ACTION ---");
        System.out.println("1. Take 3 different gem type tokens ");
        System.out.println("2. Take 2 tokens of the same gem type");
        System.out.println("3. Reserve a card");
        System.out.println("4. Buy a card");
        System.out.print("Enter choice (1-4): ");
    }
    

    // Action 1: take 3 diff tokens
    private int take3DifferentTokens(Player player) {
        System.out.println("\nAvailable colors: black, blue, green, red, white");
        System.out.print("Enter 3 different colors (separated by spaces): ");
        String input = sc.nextLine();
        String[] gemTypes = input.toLowerCase().trim().split("\\s+"); //btw \\s+ means any white space, i found out online
        
        // Validate input
        if (gemTypes.length != 3) {
            System.out.println("Please select exactly 3 gem types.");
            return -1;
        }
        
        // Check for duplicates
        Set<String> uniqueTypes = new HashSet<>(Arrays.asList(gemTypes));
        if (uniqueTypes.size() != 3) {
            System.out.println("All 3 gem types must be different.");
            return -1;
        }

        // Check if gemtypes are valid
        for (String gemType : gemTypes) {
            if (!Token.checkGemType(gemType)){   //checkGemType is in Token.java
                System.out.println("Please enter valid gem types");
                return -1;
            }
        }
        
        // Check if tokens are available
        for (String gemType : gemTypes) {
            if (TokenPile.getTokenCount(gemType) < 1) {
                System.out.println("Not enough " + gemType + " tokens available.");
                return -1;
            }
        }
        
        // Take the tokens
        for (String gemType : gemTypes) {
            TokenPile.removeToken(gemType, 1);
            player.addToken(new Token(gemType));
        }
        
        System.out.println("Tokens taken successfully!");
        return 1;
    }
    
    // Action 2: take 2 of the same tokens
    private int take2SameTokens(Player player) {
        System.out.println("\nAvailable gemType: black, blue, green, red, white");
        System.out.println("You can choose ONE ONLY");
        System.out.print("Enter gemType: ");
        String gemType = sc.nextLine().toLowerCase().trim();
        
        // Check if 4 tokens of that gemType are available
        if (TokenPile.getTokenCount(gemType) < 4) {
            System.out.println("There must be at least 4 " + gemType + " tokens available to take 2.");
            return -1;
        }
        
        // Take 2 tokens
        TokenPile.removeToken(gemType, 2);
        player.addToken(new Token(gemType));
        player.addToken(new Token(gemType));
        
        System.out.println("2 " + gemType + " tokens taken successfully!");
        return 2;
    }


    // Action 3: reserve card 
    private int reserveCard(Player player) {
        // Check if player can reserve (max 3)
        if (player.getReservedCards().size() >= 3) {
            System.out.println("You already have 3 reserved cards (maximum).");
            return -1;
        }
        
        System.out.println("\nReserve from which level?");
        System.out.print("Enter level (1/2/3): ");
        
        if(!sc.hasNextInt()){ // check if user input an int
            System.out.println("Invalid input.");
            sc.next();
            return -1;
        }
        
        int level = sc.nextInt();
        sc.nextLine();
        
        ArrayList<Card> openCards = getOpenCardsByLevel(level); /////
        if (openCards == null || openCards.isEmpty()) {
            System.out.println("No cards available at this level."); // incase the level runs out, shouldnt be the case
            return -1;
        }
        
        System.out.print("Enter card index (0-" + (openCards.size()-1) + "): "); //index of card is printed, see gamestate
        
        if(!sc.hasNextInt()){
            System.out.println("Invalid input.");
            sc.next();
            return -1;
        }
        
        int index = sc.nextInt();
        sc.nextLine();
        
        if (index < 0 || index >= openCards.size()) {
            System.out.println("Invalid card index.");
            return -1;
        }
        
        // Reserve the card
        Card card = openCards.remove(index);
        player.addReservedCard(card);
        
        // Give gold token if available
        if (TokenPile.getTokenCount("gold") > 0) {
            TokenPile.removeToken("gold", 1);
            player.addToken(new Token("gold"));
            System.out.println("Card reserved and you receive 1 gold token.");
        } else {
            System.out.println("Card reserved but no gold tokens available");
        }
        
        // Refill the card slot
        Card.transferFromClosedToOpen(getClosedCardsByLevel(level), openCards); // refill the open cards
        
        return 3;
    }

    private ArrayList<Card> getOpenCardsByLevel(int level) {
        switch(level) {
            case 1: return Card.getOpenLevel1();
            case 2: return Card.getOpenLevel2();
            case 3: return Card.getOpenLevel3();
            default: return null;
        }
    }

    private ArrayList<Card> getClosedCardsByLevel(int level) {
        switch(level) {
            case 1: return Card.getClosedLevel1();
            case 2: return Card.getClosedLevel2();
            case 3: return Card.getClosedLevel3();
            default: return null;
        }
    }
    

    // Action 4: Buy card //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private int buyCard(Player player) {
        return -1;
        /* this one is tough, good luck
            function should return true if successfully buy card,
            in any case where player inputs something wrong/ not enough tokens, 
            just return false, the player turn will auto go back to
            prompt player to choose 1 of 4 actions
        */
    }
}
    
  
