package src.com.splendor.web.game;

import org.springframework.stereotype.Service;
import src.com.splendor.web.game.model.Noble;

import java.util.List;

/**
 * Web game façade: delegates to setup, token rules, card rules, turn flow, CPU, and outcome services
 * so controllers depend on one bean while logic stays split by responsibility.
 */
@Service
public class GameService {

    private final WebGameSetupService setup;
    private final WebTokenRulesService tokenRules;
    private final WebCardRulesService cardRules;
    private final TurnPhaseService turnPhase;
    private final CpuTurnService cpuTurn;
    private final GameOutcomeService outcomes;

    public GameService(WebGameSetupService setup,
                       WebTokenRulesService tokenRules,
                       WebCardRulesService cardRules,
                       TurnPhaseService turnPhase,
                       CpuTurnService cpuTurn,
                       GameOutcomeService outcomes) {
        this.setup = setup;
        this.tokenRules = tokenRules;
        this.cardRules = cardRules;
        this.turnPhase = turnPhase;
        this.cpuTurn = cpuTurn;
        this.outcomes = outcomes;
    }

    public GameSession newGame(List<PlayerSetup> playersInTurnOrder) {
        return setup.newGame(playersInTurnOrder);
    }

    public boolean take3Tokens(GameSession session, List<String> colors) {
        return tokenRules.take3Tokens(session, colors);
    }

    public boolean take2Tokens(GameSession session, String color) {
        return tokenRules.take2Tokens(session, color);
    }

    public boolean reserveCardFromTable(GameSession session, int level, int index) {
        return cardRules.reserveCardFromTable(session, level, index);
    }

    public boolean reserveCardFromDeck(GameSession session, int level) {
        return cardRules.reserveCardFromDeck(session, level);
    }

    public boolean buyCardFromTable(GameSession session, int level, int index) {
        return cardRules.buyCardFromTable(session, level, index);
    }

    public boolean buyReservedCard(GameSession session, int reservedIndex) {
        return cardRules.buyReservedCard(session, reservedIndex);
    }

    public void executeReturnTokens(GameSession session, List<String> tokensToReturn) {
        tokenRules.executeReturnTokens(session, tokensToReturn);
    }

    public Noble finishTurn(GameSession session) {
        return turnPhase.finishTurn(session);
    }

    public List<Integer> computeWinnerPlayerIds(GameSession gs) {
        return outcomes.computeWinnerPlayerIds(gs);
    }

    public List<String> executeOneCPUTurn(GameSession gs) {
        return cpuTurn.executeOneCPUTurn(gs);
    }
}
