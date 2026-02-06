package src.com.splendor.model;

import src.com.splendor.game.DataLoader;

public class Token {

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
        Token.blackCount = tokenCount;
        Token.blueCount = tokenCount;
        Token.greenCount = tokenCount;
        Token.redCount = tokenCount;
        Token.whiteCount = tokenCount;
        Token.goldCount = Integer.parseInt(dloader.getProperty("game.tokenCount.gold"));
    }

    public Token() {

    }
}
