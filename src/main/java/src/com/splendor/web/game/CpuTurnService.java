package src.com.splendor.web.game;

import org.springframework.stereotype.Service;
import src.com.splendor.model.Card;
import src.com.splendor.model.Noble;
import src.com.splendor.model.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Executes one CPU turn and builds a human-readable play-by-play log. */
@Service
public class CpuTurnService {

    private final WebTokenRulesService tokenRules;
    private final WebCardRulesService cardRules;
    private final TurnPhaseService turnPhase;

    public CpuTurnService(WebTokenRulesService tokenRules, WebCardRulesService cardRules, TurnPhaseService turnPhase) {
        this.tokenRules = tokenRules;
        this.cardRules = cardRules;
        this.turnPhase = turnPhase;
    }

    public List<String> executeOneCPUTurn(GameSession gs) {
        List<String> log = new ArrayList<>();
        int cpuId = gs.getCurrentPlayer().getPlayerID();

        Object[] action = getCPUAction(gs);
        int type = (Integer) action[0];
        boolean ok = false;

        if (type == 1) {
            String colors = (String) action[1];
            ok = tokenRules.take3Tokens(gs, Arrays.asList(colors.split("\\s+")));
            if (ok) log.add("CPU Player " + cpuId + ": Took 3 tokens (" + colors.replace(" ", ", ") + ")");
        } else if (type == 2) {
            String color = (String) action[1];
            ok = tokenRules.take2Tokens(gs, color);
            if (ok) log.add("CPU Player " + cpuId + ": Took 2 " + color + " tokens");
        } else if (type == 3) {
            String src = (String) action[1];
            int level = (Integer) action[2];
            if ("deck".equals(src)) {
                ok = cardRules.reserveCardFromDeck(gs, level);
                if (ok) log.add("CPU Player " + cpuId + ": Reserved a card from Level " + level + " deck");
            } else {
                int idx = (Integer) action[3];
                ok = cardRules.reserveCardFromTable(gs, level, idx);
                if (ok) log.add("CPU Player " + cpuId + ": Reserved a Level " + level + " card from the table");
            }
        } else if (type == 4) {
            String src = (String) action[1];
            if ("reserved".equals(src)) {
                int idx = (Integer) action[2];
                ok = cardRules.buyReservedCard(gs, idx);
                if (ok) log.add("CPU Player " + cpuId + ": Bought a reserved card");
            } else {
                int level = (Integer) action[2];
                int idx = (Integer) action[3];
                var cards = gs.getOpenCardsByLevel(level);
                Card card = (cards != null && idx < cards.size()) ? cards.get(idx) : null;
                ok = cardRules.buyCardFromTable(gs, level, idx);
                if (ok && card != null) {
                    log.add("CPU Player " + cpuId + ": Bought a Level " + level + " " + card.getGemType() + " card (" + card.getPrestigePoints() + " pts)");
                } else if (ok) {
                    log.add("CPU Player " + cpuId + ": Bought a Level " + level + " card");
                }
            }
        }

        if (!ok) {
            log.add("CPU Player " + cpuId + ": Passed (no valid action)");
            Noble acquired = turnPhase.finishTurn(gs);
            if (acquired != null) {
                log.add("CPU Player " + cpuId + ": Acquired Noble: " + acquired.getName() + " (" + acquired.getPrestigePoints() + " pts)");
            }
            return log;
        }

        Player p = gs.getCurrentPlayer();
        if (p.getTokens().size() > gs.getMaxTokensPerPlayer()) {
            WebCPUStrategy strat = new WebCPUStrategy(gs);
            List<String> toReturn = new ArrayList<>();
            while (p.getTokens().size() > gs.getMaxTokensPerPlayer()) {
                toReturn.add(strat.chooseTokenToReturn(p));
            }
            for (String gem : toReturn) {
                log.add("CPU Player " + cpuId + ": Returned 1 " + gem + " token (over limit)");
            }
            tokenRules.executeReturnTokens(gs, toReturn);
        }

        Noble acquired = turnPhase.finishTurn(gs);
        if (acquired != null) {
            log.add("CPU Player " + cpuId + ": Acquired Noble: " + acquired.getName() + " (" + acquired.getPrestigePoints() + " pts)");
        }

        return log;
    }

    /** Returns CPU action params: [actionType, ...]. actionType: 1=take3, 2=take2, 3=reserve, 4=buy */
    private Object[] getCPUAction(GameSession session) {
        WebCPUStrategy strategy = new WebCPUStrategy(session);
        Player player = session.getCurrentPlayer();
        int action = strategy.chooseAction(player);
        return switch (action) {
            case 1 -> new Object[]{1, strategy.chooseThreeTokenColors(player)};
            case 2 -> new Object[]{2, strategy.chooseTwoTokenColor(player)};
            case 3 -> getReserveParams(strategy, player);
            case 4 -> getBuyParams(strategy, player);
            default -> new Object[]{1, strategy.chooseThreeTokenColors(player)};
        };
    }

    private Object[] getReserveParams(WebCPUStrategy strategy, Player player) {
        int fromDeck = strategy.chooseReserveSource(player);
        int level = strategy.chooseReserveLevel(player);
        if (fromDeck == 2) return new Object[]{3, "deck", level, -1};
        int idx = strategy.chooseReserveCardIndex(player, level);
        return new Object[]{3, "table", level, idx};
    }

    private Object[] getBuyParams(WebCPUStrategy strategy, Player player) {
        int src = strategy.chooseBuySource(player);
        if (src == 2) {
            int idx = strategy.chooseReservedCardIndex(player);
            return new Object[]{4, "reserved", idx};
        }
        int level = strategy.chooseBuyLevel(player);
        int idx = strategy.chooseBuyCardIndex(player, level);
        return new Object[]{4, "table", level, idx};
    }
}
