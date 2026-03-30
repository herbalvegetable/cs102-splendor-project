package src.com.splendor.web.game;

import src.com.splendor.web.game.model.Card;

import java.util.ArrayList;
import java.util.Random;

/** Fills face-up rows from closed decks (shared by setup and refills after buys). */
final class CardSupply {

    private static final Random RAND = new Random();

    private CardSupply() {}

    static void transferRandomFaceUp(ArrayList<Card> closed, ArrayList<Card> open) {
        while (open.size() < 4 && !closed.isEmpty()) {
            Card c = closed.remove(RAND.nextInt(closed.size()));
            open.add(c);
        }
    }
}
