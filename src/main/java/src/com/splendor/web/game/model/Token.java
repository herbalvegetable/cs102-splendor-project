package src.com.splendor.web.game.model;

import java.io.Serializable;

public class Token implements Serializable {

    private static final long serialVersionUID = 1L;

    private String gemType;

    public Token(String gemType) {
        this.gemType = gemType;
    }

    public static boolean checkGemType(String gemType) {
        switch (gemType.toLowerCase()) {
            case "white":
            case "blue":
            case "green":
            case "red":
            case "black":
                return true;
            default:
                return false;
        }
    }

    public String getGemType() {
        return this.gemType;
    }

    public String toString() {
        return gemType;
    }
}
