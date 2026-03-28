package src.com.splendor.web.game;

import org.springframework.stereotype.Service;
import src.com.splendor.model.Player;

import java.util.List;

@Service
public class GameOutcomeService {

    /** Tie-break: among max prestige, fewest bought cards wins. */
    public List<Integer> computeWinnerPlayerIds(GameSession gs) {
        int maxPts = gs.getPlayers().stream().mapToInt(Player::getPrestigePoints).max().orElse(0);
        var tied = gs.getPlayers().stream().filter(p -> p.getPrestigePoints() == maxPts).toList();
        int minCards = tied.stream().mapToInt(p -> p.getBoughtCards().size()).min().orElse(0);
        return tied.stream().filter(p -> p.getBoughtCards().size() == minCards).map(Player::getPlayerID).toList();
    }
}
