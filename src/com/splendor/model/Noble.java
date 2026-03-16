package src.com.splendor.model;

import java.util.*;
import src.com.splendor.game.DataLoader;
import src.com.splendor.game.GameState;

public class Noble {

    private static ArrayList<Noble> allNobles;
    private static ArrayList<Noble> availNobles;

    public static void initialise(int playerCount) {
        Noble.allNobles = new ArrayList<>();
        Noble.availNobles = new ArrayList<>();

        // STEP 1: init all nobles - read from nobles.csv
        Noble.initAllNobles();

        // STEP 2: init only nobles available during the game
        Noble.initAvailNobles(playerCount);
    }

    public static void initAllNobles() {
        DataLoader dloader = new DataLoader();
        String noblesContent = dloader.readResourceFile("/nobles.csv");

        // System.out.println(noblesContent);
        String[] lines = noblesContent.split("\n");

        // nobles.csv header columns
        // level, prestigeValue, black, blue, green, red, white
        // populate nobles list with nobles.csv info
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            String[] nobleProps = line.split(",");
            String name = new String(nobleProps[0]);
            int prestigePoints = Integer.parseInt(nobleProps[1]);

            String purchasePriceString = new String("");
            for (int j = 2; j <= 6; j++) {
                purchasePriceString += nobleProps[j];
            }

            Noble.allNobles.add(new Noble(name, prestigePoints, purchasePriceString));
        }
    }

    public static void initAvailNobles(int playerCount) {
        // no. of available nobles = playerCount + 1
        for (int i = 0; i < playerCount + 1; i++) {
            Noble.availNobles.add(GameState.getRandomItemFromArray(Noble.allNobles));
        }
    }

    public static ArrayList<Noble> getAllNobles() {
        return allNobles;
    }

    public static ArrayList<Noble> getAvailNobles() {
        return availNobles;
    }

    private String name;
    private String purchasePrice;
    // purchasePrice: five digit number
    // order of colour prices: black, blue, green, red, white
    private int prestigePoints;

    public Noble(String name, int prestigePoints, String purchasePrice) {
        this.name = name;
        this.prestigePoints = prestigePoints;
        this.purchasePrice = purchasePrice;
    }

    public String getName() {
        return name;
    }

    public String getPurchasePrice() {
        return purchasePrice;
    }

    public int getPrestigePoints() {
        return prestigePoints;
    }

    // remove nobles from the board
    public static void removeNoble(Noble noble) {
        availNobles.remove(noble); 
    }

    public int getPrice(String tokenColor) {
        switch(tokenColor.toLowerCase()) {
            case "black": return purchasePrice.charAt(0) - 
            '0';
            case "blue": return purchasePrice.charAt(1) - 
            '0';
            case "green": return purchasePrice.charAt(2) - 
            '0';
            case "red": return purchasePrice.charAt(3)  - 
            '0';
            case "white": return purchasePrice.charAt(4) - 
            '0';
            default:return 0; 
        }
    }
    

    //to Display Noble 
    public String toString() {
    
        return "Noble: " + name + " | " + "Prestige Points: " + prestigePoints + 
        "\n      Price -> Black: " + getPrice("black") + " | Blue: " + getPrice("blue") + " | Green: "
                + getPrice("green") + " | Red: " + getPrice("red") + " | White: " + getPrice("white");
    }

}
