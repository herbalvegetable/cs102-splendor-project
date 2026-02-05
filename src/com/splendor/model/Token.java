package src.com.splendor.model;

import src.com.splendor.game.DataLoader;

public class Token {

    private static int blackCount;
    private static int blueCount;
    private static int greenCount;
    private static int redCount;
    private static int whiteCount;
    private static int goldCount;

    public Token(int totalPlayerCount) {
        DataLoader dloader = new DataLoader();

        String key = String.format("game.tokenCount.%dplayers", totalPlayerCount);
        int tokenCount = Integer.parseInt(dloader.getProperty(key));
        this.blackCount = tokenCount;
        this.blueCount = tokenCount;
        this.greenCount = tokenCount;
        this.redCount = tokenCount;
        this.whiteCount = tokenCount;
        this.goldCount = Integer.parseInt(dloader.getProperty("game.tokenCount.gold"));
    }
}
