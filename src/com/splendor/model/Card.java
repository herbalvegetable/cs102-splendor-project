package src.com.splendor.model;

public class Card {

    private String gemType;
    private String purchasePrice;
    // purchasePrice: five digit number
    // order of colour prices: black, blue, green, red, white
    private int prestigePoints;
    private int level;

    public Card(int level, String gemType, int prestigePoints, String purchasePrice) {
        this.level = level;
        this.gemType = gemType;
        this.prestigePoints = prestigePoints;
        this.purchasePrice = purchasePrice;
    }

    public int getLevel() {
        return level;
    }

    public String getGemType() {
        return gemType;
    }

    public int getPrestigePoints() {
        return prestigePoints;
    }

    public String getPurchasePrice() {
        return purchasePrice;
    }

}
