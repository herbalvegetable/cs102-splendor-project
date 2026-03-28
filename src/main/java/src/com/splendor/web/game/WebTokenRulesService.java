package src.com.splendor.web.game;

import org.springframework.stereotype.Service;
import src.com.splendor.model.Player;
import src.com.splendor.model.Token;

import java.util.List;

/** Rules for taking gems from the bank and returning excess tokens (web session). */
@Service
public class WebTokenRulesService {

    public boolean take3Tokens(GameSession session, List<String> colors) {
        if (colors == null || colors.size() != 3) return false;
        if (new java.util.HashSet<>(colors).size() != 3) return false;
        for (String c : colors) {
            if (!Token.checkGemType(c) || session.getTokenCount(c) < 1) return false;
        }
        Player p = session.getCurrentPlayer();
        for (String c : colors) {
            session.removeTokens(c, 1);
            p.addToken(new Token(c));
        }
        return true;
    }

    public boolean take2Tokens(GameSession session, String color) {
        if (!Token.checkGemType(color) || session.getTokenCount(color) < 4) return false;
        Player player = session.getCurrentPlayer();
        session.removeTokens(color, 2);
        player.addToken(new Token(color));
        player.addToken(new Token(color));
        return true;
    }

    public void executeReturnTokens(GameSession session, List<String> tokensToReturn) {
        Player player = session.getCurrentPlayer();
        for (String gem : tokensToReturn) {
            if (player.getTokens().size() <= session.getMaxTokensPerPlayer()) break;
            player.deductTokens(gem, 1);
            session.addTokens(gem, 1);
        }
    }
}
