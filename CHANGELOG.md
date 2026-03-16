14/2 Voon Guo
I added 2 new classes, Token Pile, and PlayerTurn.

PlayerTurn consist of just the possible actions to be done by player,
its actually quite finalised except for the action "buyCard"

TokenPile class is created because we previously agreed that tokens were objects,
to be added to an array of Tokens as an attribute of Player.
However the version i was given used the Token class as just a counter for tokens.
I got confused so i separated these 2.

Token class - actual token objects, can create instances of them and add to array (owned by players)
TokenPile class - a counter for the available tokens on the board for players to take

Therefore, I have made it such that when a player draws a token,
the player enters a string of the gemType (eg. blue)
Then, an instance of the gemType Token is created, (eg. player.getTokens().add(new Token("blue")))
and the count of that respective gemType Token is reduced. (eg. TokenPile.removeToken("blue", 1))
