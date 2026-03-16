package src.com.splendor.model;

import java.util.ArrayList;
import java.util.Collections;
import src.com.splendor.game.DataLoader;

/**
 * Manages the noble pool: loading, initialization, and availability.
 */
public class NoblePool {

    private static ArrayList<Noble> allNobles;
    private static ArrayList<Noble> availNobles;

    public static void initialise(int playerCount) {
        allNobles = new ArrayList<>();
        availNobles = new ArrayList<>();

        initAllNobles();
        initAvailNobles(playerCount);
    }

    private static void initAllNobles() {
        DataLoader dloader = new DataLoader();
        String noblesContent = dloader.readResourceFile("/nobles.csv");
        String[] lines = noblesContent.split("\n");

        for (int i = 1; i < lines.length; i++) {
            String[] nobleProps = lines[i].split(",");
            String name = nobleProps[0];
            int prestigePoints = Integer.parseInt(nobleProps[1]);

            StringBuilder purchasePriceString = new StringBuilder();
            for (int j = 2; j <= 6; j++) {
                purchasePriceString.append(nobleProps[j]);
            }

            allNobles.add(new Noble(name, prestigePoints, purchasePriceString.toString()));
        }
    }

    private static void initAvailNobles(int playerCount) {
        Collections.shuffle(allNobles);
        int count = Math.min(playerCount + 1, allNobles.size());
        for (int i = 0; i < count; i++) {
            availNobles.add(allNobles.get(i));
        }
        allNobles.subList(0, count).clear();
    }

    public static ArrayList<Noble> getAvailNobles() {
        return availNobles;
    }

    public static void removeNoble(Noble noble) {
        availNobles.remove(noble);
    }
}
