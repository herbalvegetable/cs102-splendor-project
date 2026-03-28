package src.com.splendor.web.game;

import org.springframework.stereotype.Service;
import src.com.splendor.model.Noble;
import src.com.splendor.model.Player;

import java.util.ArrayList;
import java.util.List;

/** Noble tiles (automatic visits from bonus gems). Separate from {@link WebCardRulesService} development cards. */
@Service
public class NobleAcquisitionService {

    /** Returns the noble acquired, or null if none. */
    public Noble processNobleAcquisition(GameSession session, Player player) {
        List<Noble> qualifying = new ArrayList<>();
        for (Noble n : session.getAvailNobles()) {
            if (playerHasEnoughBonuses(player, n)) qualifying.add(n);
        }
        if (qualifying.isEmpty()) return null;
        Noble chosen = qualifying.get(0);
        player.addNoble(chosen);
        session.removeNoble(chosen);
        return chosen;
    }

    private boolean playerHasEnoughBonuses(Player player, Noble noble) {
        for (int i = 0; i < 5; i++) {
            int bonus = player.getBoughtCardsGemValueCount(i);
            int req = noble.getPurchasePrice().charAt(i) - '0';
            if (bonus < req) return false;
        }
        return true;
    }
}
