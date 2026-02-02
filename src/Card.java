public class Card{
    private String gemType;
    private String purchasePrice; // maybe something like "white,6|green,2|"
    private int prestigePoints;
    private int level;

    public Card(String gemType, String purchasePrice, int prestigePoints,int level){
        this.gemType = gemType;
        this.purchasePrice = purchasePrice;
        this.prestigePoints = prestigePoints;
        this.level = level;
    }

    public String getPurchasePrice(){
        return purchasePrice;
    }

    public int getPrestigePoints(){
        return prestigePoints;
    }

    public String getGemType(){
        return gemType;
    }

    public int getLevel(){
        return level;
    }


}