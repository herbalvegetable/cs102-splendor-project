
public class Noble{
    private String name;
    private String purchasePrice; // maybe something like "white,6|green,2|"
    private int prestigePoints;

    public Noble(String purchasePrice, int prestigePoints, String name){
        this.purchasePrice = purchasePrice;
        this.prestigePoints = prestigePoints;
        this.name = name;
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