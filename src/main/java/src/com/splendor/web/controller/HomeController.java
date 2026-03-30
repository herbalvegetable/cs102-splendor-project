package src.com.splendor.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import src.com.splendor.web.game.GameService;
import src.com.splendor.web.game.GameSession;
import src.com.splendor.web.game.PlayerSetup;

import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {

    private final GameService gameService;
    private final ObjectMapper objectMapper;

    public HomeController(GameService gameService, ObjectMapper objectMapper) {
        this.gameService = gameService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/")
    public String index(Model model) {
        return "index";
    }

    @PostMapping("/new-game")
    public String newGame(@RequestParam("playersJson") String playersJson,
                          HttpSession session,
                          RedirectAttributes ra) {
        if (playersJson == null || playersJson.isBlank()) {
            ra.addFlashAttribute("error", "Add 2–4 players with names and types.");
            return "redirect:/";
        }
        List<PlayerSetupPayload> payloads;
        try {
            payloads = objectMapper.readValue(playersJson, new TypeReference<List<PlayerSetupPayload>>() {});
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Invalid player data. Please try again.");
            return "redirect:/";
        }
        if (payloads == null || payloads.size() < 2 || payloads.size() > 4) {
            ra.addFlashAttribute("error", "You need between 2 and 4 players.");
            return "redirect:/";
        }

        List<PlayerSetup> slots = new ArrayList<>(payloads.size());
        for (PlayerSetupPayload p : payloads) {
            slots.add(new PlayerSetup(p.getName() != null ? p.getName() : "", p.isHuman()));
        }

        GameSession gameSession = gameService.newGame(slots);
        session.setAttribute("gameSession", gameSession);
        session.setAttribute("showTour", true);
        return "redirect:/game";
    }
}
