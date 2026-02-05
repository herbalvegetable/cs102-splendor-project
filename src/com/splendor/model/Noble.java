package src.com.splendor.model;

public class Noble{
    private String name;
    private String purchasePrice;
    // purchasePrice: five digit number
    // order of colour prices: black, blue, green, red, white
    private int prestigePoints;

    public Noble(String name, int prestigePoints, String purchasePrice){
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


    
}