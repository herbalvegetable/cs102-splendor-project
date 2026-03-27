package src.com.splendor.game;

import java.util.Scanner;

/**
 * Handles user input validation and prompting.
 */
public class InputHelper {

    public static int getInt(String questionText, String errorText, Scanner sc) {
        while (true) {
            System.out.print(questionText);
            if (sc.hasNextInt()) {
                return sc.nextInt();
            }
            System.out.println(errorText);
            sc.next();
        }
    }
}
