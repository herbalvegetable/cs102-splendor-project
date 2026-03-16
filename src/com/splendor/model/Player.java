package src.com.splendor.model;

import java.util.*;

import src.com.splendor.game.GameState;

public class Player{
    private int playerID;
    private int prestigePoints = 0;
    private ArrayList<Token> tokens = new ArrayList<Token>();
    private ArrayList<Card> boughtCards = new ArrayList<Card>();
    private ArrayList<Card> reservedCards = new ArrayList<Card>();
    private ArrayList<Noble> nobles = new ArrayList<Noble>();

    public Player(int playerID){
        this.playerID = playerID;

        // DEBUG
        // String[] gemTypes = {"black", "blue", "green", "red", "white"};
        // for (int i = 0; i < 5; i++) {
        //     for (int j = 0; j < 10; j++) {
        //         tokens.add(new Token(gemTypes[i]));
        //     }
        // }


        // its kinda messy
        // String[] gemTypes = {"black", "blue", "green", "red", "white"};
        // for (int i = 0; i < 5; i++) {
        //     for (int j = 0; j < 3; j++) {
        //         addBoughtCard(new Card(1, gemTypes[i], 0, "00000"));
        //     }
        // }
        // System.out.println("Nobles BEFORE noblesToPlayer: " + Noble.getAvailNobles().size());
        // noblesToPlayer();
        // System.out.println("Nobles AFTER noblesToPlayer: " + Noble.getAvailNobles().size());

    }

    public int getPlayerID() {
        return playerID;
    }

    public int getPrestigePoints() {
        return prestigePoints;
    }

    public ArrayList<Token> getTokens() {
        return tokens;
    }

    public ArrayList<Card> getBoughtCards() {
        return boughtCards;
    }

    public ArrayList<Card> getReservedCards() {
        return reservedCards;
    }

    public ArrayList<Noble> getNobles() {
        return nobles;
    }



    // added by vg 14/2


    public void addToken(Token token){
        tokens.add(token);
    }

    public void addReservedCard(Card card){
        this.reservedCards.add(card);
    }

    public void removeReservedCard(Card card){
        this.reservedCards.remove(card);
    }

    public void addBoughtCard(Card card){
        this.boughtCards.add(card);
        this.prestigePoints += card.getPrestigePoints();
    }

    //To-do: Noble must be automatically gained once enough bonus is received 
    public void addNoble(Noble noble){
        this.nobles.add(noble);
        this.prestigePoints += noble.getPrestigePoints();
    }
    // automatically give noble to player if permanent bonus gems are enough
     public void noblesToPlayer() {
        List<Noble> checkNobles = new ArrayList<>(Noble.getAvailNobles());

        for (Noble firstNoble : checkNobles ) {
            boolean enoughPermanentBonus = true; 
            for (int i = 0; i < 5; i++) {
                int permanentBonus = getBoughtCardsGemValueCount(i); 
                int nobleRequirement = firstNoble.getPurchasePrice().charAt(i) - '0';
                
    
                if (nobleRequirement > permanentBonus) {
                    enoughPermanentBonus = false; 
                    break;
                }
                
            }

            if (enoughPermanentBonus) {
                addNoble(firstNoble); 
                Noble.removeNoble(firstNoble); 
                System.out.println("Congratulations! You acquired a Noble: " + firstNoble.getName());
                break; 
            }

        }
    }


// Added by Raymond 18/2 ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // check token count
    public int getGemTokenCount(int gemTypeIndex){ // index is the index of the specific gemType in the String array below
        String[] gemTypes = {"black", "blue", "green", "red", "white"};
        String targetGem = gemTypes[gemTypeIndex];
        int count = 0;
        for (Token token : tokens) {
            if (token.getGemType().equals(targetGem)) {
                count++;
            }
        }
        return count;
    }

    // check gold token count, kept seperate from getGemTokenCount
    public int getGoldTokenCount(){
        int count = 0;
        for (Token token : tokens) {
            if (token.getGemType().equals("gold")) {
                count++;
            }
        }
        return count;
    }

    public int getBoughtCardsGemValueCount(int gemTypeIndex) {
        String[] gemTypes = {"black", "blue", "green", "red", "white"};
        String targetGem = gemTypes[gemTypeIndex];
        int count = 0;

        for (Card card : boughtCards) {
            if (card.getGemType().toLowerCase().equals(targetGem)) {
                count++;
            }
        }
        return count;
    }

    public void removePlayerTokens(int gemTypeIndex, int numberOfTokens) {
        String[] gemTypes = {"black", "blue", "green", "red", "white", "gold"};
        String targetGem = gemTypes[gemTypeIndex];

        int removed = 0;

        for (int i = getTokens().size() - 1; i >= 0 && removed < numberOfTokens; i--) {
            if (getTokens().get(i).getGemType().equals(targetGem)) {
                getTokens().remove(i);
                TokenPile.addToken(targetGem, 1);
                removed++;
            }
        }
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}