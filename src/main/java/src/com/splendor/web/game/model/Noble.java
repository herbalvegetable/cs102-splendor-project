package src.com.splendor.web.game.model;

import java.io.Serializable;

/**
 * Represents a single noble tile in Splendor.
 */
public class Noble implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final String purchasePrice;
    private final int prestigePoints;

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

    /** Returns the image filename slug for this noble (no extension). */
    public String getImageSlug() {
        switch (name) {
            case "Catherine de'Medici": return "catherine_demedici";
            case "Elisabeth Of Austria": return "elisabeth_austria";
            case "Isabella I of Castile": return "isabella_castile";
            case "Niccolo Machiavelli": return "niccolo_machiavelli";
            case "Suleiman The Magnificent": return "suleiman_magnificent";
            case "Anne Of Brittany": return "anne_brittany";
            case "Charles V": return "charles_v";
            case "Francis I Of France": return "francis_france";
            case "Henry VII": return "henry_vii";
            case "Mary Stuart": return "mary_stuart";
            default: return "catherine_demedici";
        }
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
        return "Noble: " + name + " | Prestige Points: " + prestigePoints +
                "\n      Price -> Black: " + getPrice("black") + " | Blue: " + getPrice("blue") + " | Green: "
                + getPrice("green") + " | Red: " + getPrice("red") + " | White: " + getPrice("white");
    }
}
