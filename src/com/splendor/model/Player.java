package src.com.splendor.model;

import java.util.*;

public class Player{
    private int playerID;
    private int prestigePoints = 0;
    private ArrayList<Token> tokens = new ArrayList<Token>();
    private ArrayList<Card> boughtCards = new ArrayList<Card>();
    private ArrayList<Card> reservedCards = new ArrayList<Card>();
    private ArrayList<Noble> nobles = new ArrayList<Noble>();

    public Player(int playerID){
        this.playerID = playerID;
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

    public void addNoble(Noble noble){
        this.nobles.add(noble);
        this.prestigePoints += noble.getPrestigePoints();
    }

    // check token count
    public int getTokenCount(){
        return 0;
    }



}