package src.com.splendor.web.game;

import org.springframework.stereotype.Service;
import src.com.splendor.model.Noble;

import java.util.List;

/**
 * Web game façade: delegates to setup, rules, turn flow, CPU, and outcome services
 * so controllers depend on one bean while logic stays split by responsibility.
 */
@Service
public class GameService {

    private final WebGameSetupService setup;
    private final WebGameRulesService rules;
    private final TurnPhaseService turnPhase;
    private final CpuTurnService cpuTurn;
    private final GameOutcomeService outcomes;

    public GameService(WebGameSetupService setup,
                       WebGameRulesService rules,
                       TurnPhaseService turnPhase,
                       CpuTurnService cpuTurn,
                       GameOutcomeService outcomes) {
        this.setup = setup;
        this.rules = rules;
        this.turnPhase = turnPhase;
        this.cpuTurn = cpuTurn;
        this.outcomes = outcomes;
    }

    public GameSession newGame(int humanCount, int cpuCount) {
        return setup.newGame(humanCount, cpuCount);
    }

    public boolean take3Tokens(GameSession session, List<String> colors) {
        return rules.take3Tokens(session, colors);
    }

    public boolean take2Tokens(GameSession session, String color) {
        return rules.take2Tokens(session, color);
    }

    public boolean reserveCardFromTable(GameSession session, int level, int index) {
        return rules.reserveCardFromTable(session, level, index);
    }

    public boolean reserveCardFromDeck(GameSession session, int level) {
        return rules.reserveCardFromDeck(session, level);
    }

    public boolean buyCardFromTable(GameSession session, int level, int index) {
        return rules.buyCardFromTable(session, level, index);
    }

    public boolean buyReservedCard(GameSession session, int reservedIndex) {
        return rules.buyReservedCard(session, reservedIndex);
    }

    public void executeReturnTokens(GameSession session, List<String> tokensToReturn) {
        rules.executeReturnTokens(session, tokensToReturn);
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
