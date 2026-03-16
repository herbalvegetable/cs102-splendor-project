package src.com.splendor.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    private final GameService gameService = new GameService();

    @GetMapping("/")
    public String index(Model model) {
        return "index";
    }

    @PostMapping("/new-game")
    public String newGame(@RequestParam(defaultValue = "1") int humanCount,
                          @RequestParam(defaultValue = "0") int cpuCount,
                          HttpSession session,
                          RedirectAttributes ra) {
        int total = humanCount + cpuCount;
        if (total < 2 || total > 4) {
            ra.addFlashAttribute("error", "Total players must be 2–4.");
            return "redirect:/";
        }
        GameSession gameSession = gameService.newGame(humanCount, cpuCount);
        session.setAttribute("gameSession", gameSession);
        session.setAttribute("showTour", true);
        return "redirect:/game";
    }
}
