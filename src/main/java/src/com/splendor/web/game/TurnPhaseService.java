package src.com.splendor.web.game;

import org.springframework.stereotype.Service;
import src.com.splendor.web.game.model.Noble;
import src.com.splendor.web.game.model.Player;

@Service
public class TurnPhaseService {

    private final NobleAcquisitionService nobleAcquisition;

    public TurnPhaseService(NobleAcquisitionService nobleAcquisition) {
        this.nobleAcquisition = nobleAcquisition;
    }

    /**
     * Finishes the turn: process noble acquisition, check game end, advance to next player.
     * Returns the noble acquired (if any) for logging.
     */
    public Noble finishTurn(GameSession session) {
        Player player = session.getCurrentPlayer();
        Noble acquired = nobleAcquisition.processNobleAcquisition(session, player);
        checkGameEnd(session);
        session.advanceToNextPlayer();
        return acquired;
    }

    public void checkGameEnd(GameSession session) {
        for (Player p : session.getPlayers()) {
            if (p.getPrestigePoints() >= session.getPointsToWin()) {
                session.setFinalRound(true);
                break;
            }
        }
        if (session.isFinalRound() && session.getCurrentPlayerIndex() == session.getPlayers().size() - 1) {
            session.setGameOver(true);
        }
    }
}
