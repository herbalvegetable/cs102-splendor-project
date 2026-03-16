# Splendor – Board Game reimagined

## Project Premise

**Splendor** is a digital adaptation of the award-winning board game by Marc André, a strategy game set during the Renaissance where players compete as gem merchants. The objective is to collect gem tokens, purchase development cards that provide discounts and prestige points, attract nobles to your court, and be the first to reach **15 prestige points** (configurable).

This project implements Splendor in two modes:

1. **Web application** – A browser-based multiplayer game supporting human and CPU players, built with Spring Boot and Thymeleaf.
2. **Command-line interface (CLI)** – A console-based version for local play, sharing the same core game logic and domain models.

Key rules implemented include:

- **Turn actions**: Take 3 different-colour tokens, take 2 same-colour tokens (when 4+ available), reserve a card from the table or deck (receiving gold if available), or buy a card from the table or from reserved hand.
- **Token limit**: Maximum 10 tokens per player; excess must be returned when over the limit.
- **Gold tokens**: Act as wildcards, usable for any colour when purchasing cards.
- **Noble tiles**: Visit automatically when a player’s bought cards satisfy the noble’s gem bonus requirements (no tokens spent).
- **Game end**: First player to reach the prestige target triggers the final round; the player with the most prestige wins, with ties broken by fewer bought cards.

---

## Structural Components

```
cs102-splendor-project/
├── pom.xml                          # Maven build & dependencies
├── src/
│   ├── com/splendor/                 # CLI & domain (src root)
│   │   ├── model/                    # Domain entities
│   │   │   ├── Card.java
│   │   │   ├── Noble.java
│   │   │   ├── Player.java
│   │   │   ├── Token.java
│   │   │   ├── TokenPile.java
│   │   │   ├── CardMarket.java
│   │   │   ├── NoblePool.java
│   │   │   └── PlayerTurn.java
│   │   └── game/                     # CLI game logic
│   │       ├── DataLoader.java
│   │       ├── GameState.java
│   │       ├── GameDisplay.java
│   │       ├── MenuManager.java
│   │       ├── CPUStrategy.java
│   │       ├── CPUTurnInput.java
│   │       ├── HumanTurnInput.java
│   │       ├── TurnInput.java
│   │       ├── InputHelper.java
│   │       └── NobleAcquisition.java
│   └── main/
│       ├── java/src/com/splendor/    # Web layer
│       │   ├── SplendorApplication.java
│       │   └── web/
│       │       ├── HomeController.java
│       │       ├── GameController.java
│       │       ├── GameService.java
│       │       ├── GameSession.java
│       │       └── WebCPUStrategy.java
│       └── resources/
│           ├── config.properties     # Game configuration
│           ├── cards.csv             # Development card data
│           ├── nobles.csv            # Noble tile data
│           ├── static/
│           │   ├── css/game.css      # Styles
│           │   └── media/            # Images (cards, nobles, backgrounds)
│           └── templates/
│               ├── index.html        # New game setup
│               ├── game.html         # Main game view
│               ├── cpu-turn.html     # CPU turn animation
│               └── game-over.html    # End screen
```

---

## Technologies Used

| Technology | Version / Notes |
|------------|-----------------|
| **Java** | 17 |
| **Spring Boot** | 3.2.0 |
| **Spring Boot Starter Web** | REST/MVC, embedded Tomcat |
| **Spring Boot Starter Thymeleaf** | Server-side HTML templating |
| **Maven** | Build & dependency management |
| **build-helper-maven-plugin** | 3.4.0 – adds `src` as source root |

Thymeleaf templates use standard attributes (`th:if`, `th:each`, `th:text`, `th:href`, etc.) for dynamic content. Static assets (CSS, images) live under `src/main/resources/static/` and are served from the application root.

---

## Java Classes – Game Logic

### Domain Model (`com.splendor.model`)

| Class | Description |
|-------|-------------|
| **Card** | Represents a single development card. Holds `level` (1–3), `gemType` (Black, Blue, Green, Red, White), `prestigePoints` (0–5), and `purchasePrice` (5-character string encoding cost per colour). Provides `getPrice(tokenColor)` for cost lookup. |
| **Noble** | Represents a noble tile. Holds `name`, `prestigePoints` (3), and `purchasePrice` (5-character string for required gem bonuses). `getImageSlug()` maps the name to the image filename used in the web UI. |
| **Token** | Represents a single gem token. Holds `gemType` and provides `checkGemType()` for validation. |
| **Player** | Represents a player’s state. Holds `playerID`, `isHuman`, token list, bought cards, reserved cards, nobles, and prestige. Methods include `getGemTokenCount()`, `getGoldTokenCount()`, `getBoughtCardsGemValueCount()`, `addBoughtCard()`, `addReservedCard()`, `addNoble()`, and `deductTokens()` for purchases and token returns. |
| **TokenPile** | Static token bank for the CLI. Exposes counts per colour via `getTokenCount(color)` and adds/removes tokens. Used by `PlayerTurn` and `CPUStrategy`. |
| **CardMarket** | Static card market for the CLI. Loads from `cards.csv`, shuffles decks, manages closed/open stacks, and refills open slots. Exposes `getOpenLevel1/2/3()`, `getOpenCardsByLevel(level)`, and methods to take cards from table or deck. |
| **NoblePool** | Static noble pool for the CLI. Loads from `nobles.csv`, selects `playerCount + 1` nobles, and provides methods to claim nobles. |
| **PlayerTurn** | Executes a single turn for the CLI. Uses `TurnInput` to obtain the player’s choice (action type, token colours, reserve/buy parameters, token return, noble choice). Handles take-3, take-2, reserve, and buy actions; enforces token limit and noble acquisition. |

### CLI Game Logic (`com.splendor.game`)

| Class | Description |
|-------|-------------|
| **DataLoader** | Loads `config.properties` (points to win, max tokens, token counts by player count) and CSV files. Exposes `getProperty(key)` and methods to load cards and nobles. |
| **GameState** | Main CLI game loop. `playGame()` alternates turns via `playTurn()`, checks `checkGameEnd()`, and advances `nextPlayer()`. Uses `HumanTurnInput` or `CPUTurnInput` and delegates turn execution to `PlayerTurn`. |
| **GameDisplay** | Outputs the game state to the console (token bank, open cards, nobles, player tokens and cards). |
| **MenuManager** | Main menu (Start Game, Rules, Quit). Handles player count and human/CPU setup, initializes `CardMarket`, `NoblePool`, and `TokenPile`, and launches `GameState`. |
| **TurnInput** | Interface for all turn decisions: action type, token colours for take-3/take-2, reserve parameters (source, level, index), buy parameters (source, level/index), token to return, noble choice. |
| **HumanTurnInput** | Implements `TurnInput` using `Scanner` for user input. |
| **CPUTurnInput** | Implements `TurnInput` using `CPUStrategy`. Adds a 1-second delay between CPU decisions. |
| **CPUStrategy** | CLI CPU decision logic. Uses static `TokenPile`, `CardMarket`, `NoblePool`. See CPU Game Logic section. |
| **InputHelper** | Validates integer input (`getInt()`) for menu and setup. |
| **NobleAcquisition** | Finds nobles the player qualifies for, prompts for choice when multiple qualify, and assigns the noble via `NoblePool`. |

### Web Layer (`com.splendor.web`)

| Class | Description |
|-------|-------------|
| **SplendorApplication** | Spring Boot entry point; runs `SpringApplication.run()`. |
| **HomeController** | Serves `GET /` (index) and `POST /new-game`. Validates human + CPU count (2–4 total), creates `GameSession`, stores it in `HttpSession`, and redirects to `/game`. |
| **GameController** | Handles game flow. `GET /game` renders the game view, redirects to game-over or cpu-turn as needed. POST handlers: `/game/take3`, `/game/take2`, `/game/reserve-table`, `/game/reserve-deck`, `/game/buy-table`, `/game/buy-reserved`, `/game/return-tokens`. Validates session, delegates to `GameService`, and manages token return flow when over limit. For CPU turns, calls `GameService.getCPUAction()`, executes the action, and renders `cpu-turn.html`. |
| **GameService** | Core web game logic. `newGame()` builds `GameSession` with tokens, shuffled cards, nobles. Implements `take3Tokens`, `take2Tokens`, `reserveCardFromTable`, `reserveCardFromDeck`, `buyCardFromTable`, `buyReservedCard`, `processNobleAcquisition`, `finishTurn`, and `getCPUAction()`. |
| **GameSession** | Serializable session state. Holds token counts, closed/open card decks, available nobles, players, `currentPlayerIndex`, `gameOver`, `finalRound`, and config values. Replaces static `TokenPile`, `CardMarket`, `NoblePool` for the web. |
| **WebCPUStrategy** | CPU strategy for the web game. Uses `GameSession` instead of static state. See CPU Game Logic section. |

---

## CPU Game Logic

The project includes two CPU implementations:

- **CLI**: `CPUStrategy` + `CPUTurnInput` (uses static `TokenPile`, `CardMarket`, `NoblePool`)
- **Web**: `WebCPUStrategy` (uses `GameSession`)

Both follow the same decision logic; only the data source differs.

### Action Priority

The CPU chooses actions in this order:

1. **Buy (4)** – If any affordable card exists (table or reserved).
2. **Reserve (3)** – If the player has fewer than 3 reserved cards and at least one card is reservable.
3. **Take 2 same (2)** – If any colour has ≥4 tokens in the bank.
4. **Take 3 different (1)** – If ≥3 colours have ≥1 token each (or fallback).

### Take-Token Choices

- **chooseThreeTokenColors** – Selects three different colours that have tokens; shuffled randomly.
- **chooseTwoTokenColor** – Picks the first colour with ≥4 tokens in the bank.

### Reserve Choices

- **chooseReserveSource** – Prefers face-up cards on the table (1) over deck (2).
- **chooseReserveLevel** – First non-empty level (1 → 2 → 3).
- **chooseReserveCardIndex** – Random index among open cards at the chosen level.

### Buy Choices

- **chooseBuySource** – Prefers buying from reserved hand if an affordable reserved card exists; otherwise buys from the table.
- **chooseBuyLevel** – Highest level (3 → 2 → 1) where an affordable card exists.
- **chooseBuyCardIndex** – Among affordable cards at that level, picks the one with the highest prestige.
- **chooseReservedCardIndex** – First affordable reserved card by index.

### Token Return

- **chooseTokenToReturn** – Returns the colour the player holds the most of (excluding gold), to reduce duplicate colours when over the 10-token limit.

### Noble Choice

- **chooseNoble** – When multiple nobles qualify, selects one at random.

### Affordability Check

Both strategies use the same logic:

- For each gem colour (Black, Blue, Green, Red, White):
  - `needed = max(0, cardCost - bonusFromBoughtCards)`
  - If `playerTokens < needed`, use gold tokens to cover the gap.
- The player can afford the card if gold is sufficient for all shortages.

### Web Integration

`GameController` calls `GameService.getCPUAction(gameSession)`, which:

1. Instantiates `WebCPUStrategy` with the session.
2. Calls `chooseAction()` to get the action type (1–4).
3. Retrieves parameters based on action: `chooseThreeTokenColors`, `chooseTwoTokenColor`, `getReserveParams`, or `getBuyParams`.
4. Executes the action via `GameService`.
5. If over the token limit, repeatedly calls `chooseTokenToReturn()` until at or below 10 tokens.
6. Calls `finishTurn()` for noble acquisition, game-end check, and advancing to the next player.

---

## Data Files

### `config.properties`

| Key | Description |
|----|-------------|
| `game.pointsToWin` | Prestige needed to win (default: 15) |
| `game.maxTokensPerPlayer` | Maximum tokens per player (default: 10) |
| `game.tokenCount.2players` | Tokens per colour for 2 players (4) |
| `game.tokenCount.3players` | Tokens per colour for 3 players (5) |
| `game.tokenCount.4players` | Tokens per colour for 4 players (7) |
| `game.tokenCount.gold` | Gold tokens available (5) |
| `file.cards` | Path to cards CSV |
| `file.nobles` | Path to nobles CSV |

### `cards.csv`

Columns: `Level`, `Color`, `PV`, `Black`, `Blue`, `Green`, `Red`, `White`.

- **Level**: 1–3 (card tier)
- **Color**: Gem bonus (Black, Blue, Green, Red, White)
- **PV**: Prestige points (0–5)
- Last 5 columns: Cost in each colour (0–7 per colour)

Example: `1,Black,0,0,1,1,1,1` = Level 1 Black card, 0 prestige, costs 0 Black, 1 Blue, 1 Green, 1 Red, 1 White.

The deck has 40 Level 1, 30 Level 2, and 22 Level 3 cards.

### `nobles.csv`

Columns: `Name`, `PV`, `Black`, `Blue`, `Green`, `Red`, `White`.

Each noble requires exactly 4 total gem bonuses across 3 colours (no tokens spent). Example: `Catherine de'Medici,3,0,3,3,3,0` requires 3 Blue, 3 Green, 3 Red from bought cards.

The game selects `playerCount + 1` nobles from the pool.

---

## Running the Application

### Web version

```bash
mvn spring-boot:run
```

Then open http://localhost:8080 in a browser. Use the index page to set human and CPU player counts (2–4 total) and start a game.

### CLI version

Run the main method in `MenuManager` or `GameState` (depending on how the project is configured to launch the CLI). Follow the menu to start a game and choose human vs CPU players.
