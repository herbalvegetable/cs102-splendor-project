package src.com.splendor.web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import src.com.splendor.web.game.model.Player;
import src.com.splendor.web.game.GameService;
import src.com.splendor.web.game.GameSession;

import java.util.Arrays;
import java.util.List;

@Controller
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/game")
    public String game(HttpSession session, Model model, RedirectAttributes ra) {
        GameSession gs = (GameSession) session.getAttribute("gameSession");
        if (gs == null) {
            ra.addFlashAttribute("error", "No game in progress.");
            return "redirect:/";
        }
        if (gs.isGameOver()) {
            model.addAttribute("gameSession", gs);
            model.addAttribute("winnerIds", gameService.computeWinnerPlayerIds(gs));
            return "game-over";
        }
        Player current = gs.getCurrentPlayer();
        if (current.isHuman()) {
            model.addAttribute("gameSession", gs);
            model.addAttribute("overTokenLimit", current.getTokens().size() > gs.getMaxTokensPerPlayer());
            if (Boolean.TRUE.equals(session.getAttribute("showTour"))) {
                model.addAttribute("showTour", true);
                session.removeAttribute("showTour");
            }
            return "game";
        }
        List<String> log = gameService.executeOneCPUTurn(gs);
        model.addAttribute("gameSession", gs);
        model.addAttribute("cpuTurnLog", log);
        model.addAttribute("cpuPlayerName", current.getDisplayName());
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

    private GameSession getSession(HttpSession session, RedirectAttributes ra) {
        GameSession gs = (GameSession) session.getAttribute("gameSession");
        if (gs == null) ra.addFlashAttribute("error", "No game in progress.");
        return gs;
    }
}
