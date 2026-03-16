package src.com.splendor.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import src.com.splendor.model.Card;
import src.com.splendor.model.Noble;
import src.com.splendor.model.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
public class GameController {

    private final GameService gameService = new GameService();

    @GetMapping("/game")
    public String game(HttpSession session, Model model, RedirectAttributes ra) {
        GameSession gs = (GameSession) session.getAttribute("gameSession");
        if (gs == null) {
            ra.addFlashAttribute("error", "No game in progress.");
            return "redirect:/";
        }
        if (gs.isGameOver()) {
            model.addAttribute("gameSession", gs);
            int maxPts = gs.getPlayers().stream().mapToInt(Player::getPrestigePoints).max().orElse(0);
            var tied = gs.getPlayers().stream().filter(p -> p.getPrestigePoints() == maxPts).toList();
            int minCards = tied.stream().mapToInt(p -> p.getBoughtCards().size()).min().orElse(0);
            var winners = tied.stream().filter(p -> p.getBoughtCards().size() == minCards).map(Player::getPlayerID).toList();
            model.addAttribute("winnerIds", winners);
            return "game-over";
        }
        Player current = gs.getCurrentPlayer();
        if (current.isHuman()) {
            model.addAttribute("gameSession", gs);
            model.addAttribute("overTokenLimit", current.getTokens().size() > gs.getMaxTokensPerPlayer());
            return "game";
        }
        List<String> log = executeOneCPUTurn(gs);
        model.addAttribute("gameSession", gs);
        model.addAttribute("cpuTurnLog", log);
        model.addAttribute("cpuPlayerId", current.getPlayerID());
        return "cpu-turn";
    }

    @PostMapping("/game/take3")
    public String take3(@RequestParam String color1, @RequestParam String color2, @RequestParam String color3,
                        HttpSession session, RedirectAttributes ra) {
        GameSession gs = getSession(session, ra);
        if (gs == null) return "redirect:/";
        List<String> colors = Arrays.asList(color1.toLowerCase(), color2.toLowerCase(), color3.toLowerCase());
        if (!gameService.take3Tokens(gs, colors)) {
            ra.addFlashAttribute("error", "Invalid take-3 selection. Pick 3 different colors with tokens available.");
            return "redirect:/game";
        }
        return finishOrReturnTokens(gs, ra, "Tokens taken.");
    }

    @PostMapping("/game/take2")
    public String take2(@RequestParam String color, HttpSession session, RedirectAttributes ra) {
        GameSession gs = getSession(session, ra);
        if (gs == null) return "redirect:/";
        if (!gameService.take2Tokens(gs, color.toLowerCase())) {
            ra.addFlashAttribute("error", "Invalid take-2 selection. That color needs 4+ tokens in the pile.");
            return "redirect:/game";
        }
        return finishOrReturnTokens(gs, ra, "Tokens taken.");
    }

    @PostMapping("/game/reserve-table")
    public String reserveTable(@RequestParam int level, @RequestParam int index, HttpSession session, RedirectAttributes ra) {
        GameSession gs = getSession(session, ra);
        if (gs == null) return "redirect:/";
        if (!gameService.reserveCardFromTable(gs, level, index)) {
            ra.addFlashAttribute("error", "Invalid reserve selection. You may already have 3 reserved cards.");
            return "redirect:/game";
        }
        return finishOrReturnTokens(gs, ra, "Card reserved.");
    }

    @PostMapping("/game/reserve-deck")
    public String reserveDeck(@RequestParam int level, HttpSession session, RedirectAttributes ra) {
        GameSession gs = getSession(session, ra);
        if (gs == null) return "redirect:/";
        if (!gameService.reserveCardFromDeck(gs, level)) {
            ra.addFlashAttribute("error", "Invalid reserve from deck. You may already have 3 reserved cards.");
            return "redirect:/game";
        }
        return finishOrReturnTokens(gs, ra, "Card reserved.");
    }

    @PostMapping("/game/buy-table")
    public String buyTable(@RequestParam int level, @RequestParam int index, HttpSession session, RedirectAttributes ra) {
        GameSession gs = getSession(session, ra);
        if (gs == null) return "redirect:/";
        if (!gameService.buyCardFromTable(gs, level, index)) {
            ra.addFlashAttribute("error", "Cannot buy that card. Check you have enough tokens (including gold as wildcards).");
            return "redirect:/game";
        }
        return finishOrReturnTokens(gs, ra, "Card purchased.");
    }

    @PostMapping("/game/buy-reserved")
    public String buyReserved(@RequestParam int index, HttpSession session, RedirectAttributes ra) {
        GameSession gs = getSession(session, ra);
        if (gs == null) return "redirect:/";
        if (!gameService.buyReservedCard(gs, index)) {
            ra.addFlashAttribute("error", "Cannot buy that reserved card. Check you have enough tokens.");
            return "redirect:/game";
        }
        return finishOrReturnTokens(gs, ra, "Card purchased.");
    }

    @PostMapping("/game/return-tokens")
    public String returnTokens(@RequestParam(required = false) List<String> tokens, HttpSession session, RedirectAttributes ra) {
        GameSession gs = getSession(session, ra);
        if (gs == null) return "redirect:/";
        int excess = gs.getCurrentPlayer().getTokens().size() - gs.getMaxTokensPerPlayer();
        if (tokens == null || tokens.size() < excess) {
            ra.addFlashAttribute("error", "Select exactly " + excess + " token(s) to return.");
            return "redirect:/game";
        }
        gameService.executeReturnTokens(gs, tokens.subList(0, excess));
        if (gs.getCurrentPlayer().getTokens().size() <= gs.getMaxTokensPerPlayer()) {
            gameService.finishTurn(gs);
            ra.addFlashAttribute("success", "Turn complete. Next player's turn.");
        }
        return "redirect:/game";
    }

    private String finishOrReturnTokens(GameSession gs, RedirectAttributes ra, String successMsg) {
        Player p = gs.getCurrentPlayer();
        if (p.getTokens().size() > gs.getMaxTokensPerPlayer()) {
            ra.addFlashAttribute("returnRequired", true);
            return "redirect:/game";
        }
        gameService.finishTurn(gs);
        ra.addFlashAttribute("success", successMsg + " Turn complete.");
        return "redirect:/game";
    }

    /** Runs one CPU turn, builds a play-by-play log. */
    private List<String> executeOneCPUTurn(GameSession gs) {
        List<String> log = new ArrayList<>();
        int cpuId = gs.getCurrentPlayer().getPlayerID();

        Object[] action = gameService.getCPUAction(gs);
        int type = (Integer) action[0];
        boolean ok = false;

        if (type == 1) {
            String colors = (String) action[1];
            ok = gameService.take3Tokens(gs, Arrays.asList(colors.split("\\s+")));
            if (ok) log.add("CPU Player " + cpuId + ": Took 3 tokens (" + colors.replace(" ", ", ") + ")");
        } else if (type == 2) {
            String color = (String) action[1];
            ok = gameService.take2Tokens(gs, color);
            if (ok) log.add("CPU Player " + cpuId + ": Took 2 " + color + " tokens");
        } else if (type == 3) {
            String src = (String) action[1];
            int level = (Integer) action[2];
            if ("deck".equals(src)) {
                ok = gameService.reserveCardFromDeck(gs, level);
                if (ok) log.add("CPU Player " + cpuId + ": Reserved a card from Level " + level + " deck");
            } else {
                int idx = (Integer) action[3];
                ok = gameService.reserveCardFromTable(gs, level, idx);
                if (ok) log.add("CPU Player " + cpuId + ": Reserved a Level " + level + " card from the table");
            }
        } else if (type == 4) {
            String src = (String) action[1];
            if ("reserved".equals(src)) {
                int idx = (Integer) action[2];
                ok = gameService.buyReservedCard(gs, idx);
                if (ok) log.add("CPU Player " + cpuId + ": Bought a reserved card");
            } else {
                int level = (Integer) action[2];
                int idx = (Integer) action[3];
                var cards = gs.getOpenCardsByLevel(level);
                Card card = (cards != null && idx < cards.size()) ? cards.get(idx) : null;
                ok = gameService.buyCardFromTable(gs, level, idx);
                if (ok && card != null) log.add("CPU Player " + cpuId + ": Bought a Level " + level + " " + card.getGemType() + " card (" + card.getPrestigePoints() + " pts)");
                else if (ok) log.add("CPU Player " + cpuId + ": Bought a Level " + level + " card");
            }
        }

        if (!ok) {
            log.add("CPU Player " + cpuId + ": Passed (no valid action)");
            Noble acquired = gameService.finishTurn(gs);
            if (acquired != null) log.add("CPU Player " + cpuId + ": Acquired Noble: " + acquired.getName() + " (" + acquired.getPrestigePoints() + " pts)");
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
            gameService.executeReturnTokens(gs, toReturn);
        }

        Noble acquired = gameService.finishTurn(gs);
        if (acquired != null) {
            log.add("CPU Player " + cpuId + ": Acquired Noble: " + acquired.getName() + " (" + acquired.getPrestigePoints() + " pts)");
        }

        return log;
    }

    private GameSession getSession(HttpSession session, RedirectAttributes ra) {
        GameSession gs = (GameSession) session.getAttribute("gameSession");
        if (gs == null) ra.addFlashAttribute("error", "No game in progress.");
        return gs;
    }
}
