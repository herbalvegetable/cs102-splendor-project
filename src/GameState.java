
import java.util.*;

public class GameState {

    private static int getInt(String questionText, String errorText, Scanner sc) {
        while (true) {
            System.out.print(questionText);
            if (sc.hasNextInt()) {
                int outputInt = sc.nextInt();
                return outputInt;
            }
            // not an int, dont break out of while loop
            System.out.println(errorText);
            sc.next();
        }
    }

    private static int getNumberOfHumanPlayers(Scanner sc){
        int humanPlayers = -1;
        while (true) {
            humanPlayers = getInt("Enter number of Human players(1/2/3/4): ", "Not a valid int", sc);
            if (humanPlayers >= 1 && humanPlayers <= 4) {
                break;
            }
            // outside of range
            System.out.println("Outside of range, enter within 1/2/3/4");
        }
        return humanPlayers;
    }

    private static int getNumberOfComputerPlayers(int humanPlayers, Scanner sc){
        int computerPlayers = -1;
        int maxComputerPlayers = 4 - humanPlayers;
        while (true) {
            computerPlayers = getInt(String.format("Enter number of Computer players(%d to %d): ", 0 , maxComputerPlayers), 
            "Not a valid int", sc);
            if (computerPlayers >= 0 && computerPlayers <= maxComputerPlayers) {
                break;
            }
            // outside of range
            System.out.printf("Outside of range, enter within (%d to %d)\n", 0 , maxComputerPlayers);
        }
        return computerPlayers;
    }

    public static void main(String[] args) {
        System.out.println("===== SPLENDOR =====");
        Scanner sc = new Scanner(System.in);
        int humanPlayers = getNumberOfHumanPlayers(sc);
        int computerPlayers = getNumberOfComputerPlayers(humanPlayers, sc);
        
        System.out.printf("Human players: %d\n", humanPlayers);
        System.out.printf("Computer players: %d\n", computerPlayers);
        
        
    }
}
