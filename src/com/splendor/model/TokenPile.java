package src.com.splendor.model;

import src.com.splendor.game.DataLoader;

public class TokenPile{

    private static int blackCount;
    private static int blueCount;
    private static int greenCount;
    private static int redCount;
    private static int whiteCount;
    private static int goldCount;

    

    public static void initialise(int playerCount) {
        // init token counts

        // 2 players: 4x tokens per gem type
        // 3 players: 5x tokens per gem type
        // 4 players: 7x tokens per gem type
        // NOTE: these default settings can be changed in config.properties file 
        DataLoader dloader = new DataLoader();

        String key = String.format("game.tokenCount.%dplayers", playerCount);
        int tokenCount = Integer.parseInt(dloader.getProperty(key));
        TokenPile.blackCount = tokenCount;
        TokenPile.blueCount = tokenCount;
        TokenPile.greenCount = tokenCount;
        TokenPile.redCount = tokenCount;
        TokenPile.whiteCount = tokenCount;
        TokenPile.goldCount = Integer.parseInt(dloader.getProperty("game.tokenCount.gold"));
    }



    // added by vg 14/2 //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static int getTokenCount(String gemType){
        switch(gemType.toLowerCase()){
            case "white":
                return whiteCount;
                
            case "blue":
                return blueCount;
                
            case "green":
                return greenCount;
                
            case "red":
                return redCount;
                
            case "black":
                return blackCount;
                
            case "gold":
                return goldCount;
                
        }
        return -1;
    }


    //when player takes a token, update token pile count
    public static void removeToken(String gemType, int amount){
        switch(gemType.toLowerCase()){
            case "white":
                whiteCount -= amount;
                break;
            case "blue":
                blueCount -= amount;
                break;
            case "green":
                greenCount -= amount;
                break;
            case "red":
                redCount -= amount;
                break;
            case "black":
                blackCount -= amount;
                break;
            case "gold":
                goldCount -= amount;
                break;
        }
    }


    //when player returns a token, update token pile count
    public static void addToken(String gemType, int amount){
        switch(gemType.toLowerCase()){
            case "white":
                whiteCount += amount;
                break;
            case "blue":
                blueCount += amount;
                break;
            case "green":
                greenCount += amount;
                break;
            case "red":
                redCount += amount;
                break;
            case "black":
                blackCount += amount;
                break;
            case "gold":
                goldCount += amount;
                break;
        }
    }



}
