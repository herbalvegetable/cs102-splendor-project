package src.com.splendor.model;

/**
 * Represents a single development card in Splendor.
 * Contains only card attributes - deck management is in CardMarket.
 */
public class Card {

    private final String gemType;
    private final String purchasePrice;
    private final int prestigePoints;
    private final int level;

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

    public int getPrice(String tokenColor) {
        switch (tokenColor.toLowerCase()) {
            case "black": return purchasePrice.charAt(0) - '0';
            case "blue": return purchasePrice.charAt(1) - '0';
            case "green": return purchasePrice.charAt(2) - '0';
            case "red": return purchasePrice.charAt(3) - '0';
            case "white": return purchasePrice.charAt(4) - '0';
            default: return 0;
        }
    }

    @Override
    public String toString() {
        return "Level: " + level + " | Gem Type: " + gemType + " | Points: " + prestigePoints +
                "\n      Price -> Black: " + getPrice("black") + " | Blue: " + getPrice("blue") + " | Green: "
                + getPrice("green") + " | Red: " + getPrice("red") + " | White: " + getPrice("white");
    }
}
