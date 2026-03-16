package src.com.splendor.game;

import java.util.ArrayList;
import src.com.splendor.model.Card;
import src.com.splendor.model.Noble;
import src.com.splendor.model.Player;

/**
 * Abstraction for turn input - supports both human (Scanner) and CPU (strategy-based) input.
 */
public interface TurnInput {

    int getActionChoice(Player player);

    String getThreeTokenColors(Player player);

    String getTwoTokenColor(Player player);

    int getReserveChoice(Player player);

    int getReserveLevel(Player player);

    int getReserveCardIndex(Player player, int level);

    int getBuySource(Player player);

    int getBuyLevel(Player player);

    int getBuyCardIndex(Player player, int level);

    int getReservedCardIndex(Player player);

    String getTokenToReturn(Player player);

    boolean confirmPurchase(Player player, Card card);

    int getNobleChoice(Player player, ArrayList<Noble> nobles);

    boolean isCPU();
}
