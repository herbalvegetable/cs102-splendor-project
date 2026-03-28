package src.com.splendor.web.game;

import org.springframework.stereotype.Service;
import src.com.splendor.game.DataLoader;
import src.com.splendor.model.Card;
import src.com.splendor.model.Noble;

import java.util.ArrayList;
import java.util.Collections;

@Service
public class WebGameSetupService {

    private final DataLoader dataLoader = new DataLoader();

    public GameSession newGame(int humanCount, int cpuCount) {
        int pointsToWin = Integer.parseInt(dataLoader.getProperty("game.pointsToWin"));
        int maxTokens = Integer.parseInt(dataLoader.getProperty("game.maxTokensPerPlayer"));
        int playerCount = humanCount + cpuCount;
        int tokenPerGem = Integer.parseInt(dataLoader.getProperty("game.tokenCount." + playerCount + "players"));
        int goldCount = Integer.parseInt(dataLoader.getProperty("game.tokenCount.gold"));

        GameSession session = new GameSession(humanCount, cpuCount, pointsToWin, maxTokens, tokenPerGem, goldCount);

        loadCards(session);
        loadNobles(session, playerCount);

        return session;
    }

    private void loadCards(GameSession session) {
        String path = dataLoader.getProperty("file.cards");
        if (path == null || path.isBlank()) {
            path = "/cards.csv";
        }
        String content = dataLoader.readResourceFile(path);
        String[] lines = content.split("\n");
        ArrayList<Card> closed1 = session.getClosedLevel1();
        ArrayList<Card> closed2 = session.getClosedLevel2();
        ArrayList<Card> closed3 = session.getClosedLevel3();

        for (int i = 1; i < lines.length; i++) {
            String[] p = lines[i].split(",");
            int level = Integer.parseInt(p[0]);
            String color = p[1];
            int prestige = Integer.parseInt(p[2]);
            StringBuilder price = new StringBuilder();
            for (int j = 3; j <= 7; j++) price.append(p[j]);
            Card card = new Card(level, color, prestige, price.toString());
            switch (level) {
                case 1 -> closed1.add(card);
                case 2 -> closed2.add(card);
                case 3 -> closed3.add(card);
            }
        }

        CardSupply.transferRandomFaceUp(session.getClosedLevel1(), session.getOpenLevel1());
        CardSupply.transferRandomFaceUp(session.getClosedLevel2(), session.getOpenLevel2());
        CardSupply.transferRandomFaceUp(session.getClosedLevel3(), session.getOpenLevel3());
    }

    private void loadNobles(GameSession session, int playerCount) {
        String path = dataLoader.getProperty("file.nobles");
        if (path == null || path.isBlank()) {
            path = "/nobles.csv";
        }
        String content = dataLoader.readResourceFile(path);
        String[] lines = content.split("\n");
        ArrayList<Noble> all = new ArrayList<>();
        for (int i = 1; i < lines.length; i++) {
            String[] p = lines[i].split(",");
            String name = p[0];
            int prestige = Integer.parseInt(p[1]);
            StringBuilder price = new StringBuilder();
            for (int j = 2; j <= 6; j++) price.append(p[j]);
            all.add(new Noble(name, prestige, price.toString()));
        }
        Collections.shuffle(all);
        int count = Math.min(playerCount + 1, all.size());
        for (int i = 0; i < count; i++) {
            session.getAvailNobles().add(all.get(i));
        }
    }
}
