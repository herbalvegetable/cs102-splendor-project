package src.com.splendor.model;

public class Token {

    private String gemType;


    public Token(String gemType) {
        this.gemType = gemType;
    }


    // added by vg 14/2 //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static boolean checkGemType(String gemType){
        switch(gemType.toLowerCase()){
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

    public String getGemType(){
        return this.gemType;
    }
}
