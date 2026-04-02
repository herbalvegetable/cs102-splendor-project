# Splendor -- Board Game Web Application

A browser-based digital adaptation of the award-winning board game **Splendor** by Marc Andre. Players compete as Renaissance gem merchants, collecting tokens, purchasing development cards for discounts and prestige points, attracting nobles, and racing to be the first to reach **15 prestige points**. The application supports 2--4 players in any mix of human and CPU opponents, all served from a single Spring Boot web server.

---

## Project Structure

```
cs102-splendor-project/
├── pom.xml                                        Maven build configuration and dependencies
├── README.md                                      This file
│
├── src/
│   └── main/
│       ├── java/com/splendor/
│       │   ├── SplendorApplication.java           Spring Boot entry point
│       │   │
│       │   └── web/
│       │       ├── controller/                    HTTP request handlers
│       │       │   ├── HomeController.java        GET / (new-game form) and POST /new-game
│       │       │   ├── GameController.java        All in-game routes: /game, take/buy/reserve/return
│       │       │   └── PlayerSetupPayload.java    JSON DTO for player config sent from the form
│       │       │
│       │       └── game/                          Core game logic (services and models)
│       │           ├── GameService.java           Facade that delegates to specialised services
│       │           ├── GameSession.java           Serializable per-game state stored in HttpSession
│       │           ├── PlayerSetup.java           Record describing one seat (name + human/CPU)
│       │           ├── CardSupply.java            Utility for drawing random cards into open rows
│       │           ├── WebGameSetupService.java   Builds a new GameSession from config and CSV data
│       │           ├── WebTokenRulesService.java  Take-3, take-2, and return-token rules
│       │           ├── WebCardRulesService.java   Reserve and purchase rules, payment with gold
│       │           ├── TurnPhaseService.java      End-of-turn: noble visit, final-round, advance
│       │           ├── NobleAcquisitionService.java  Automatic noble assignment when bonuses qualify
│       │           ├── CpuTurnService.java        Executes one CPU turn and produces a log
│       │           ├── WebCPUStrategy.java        Heuristic AI decision-making for CPU players
│       │           ├── GameOutcomeService.java    Winner calculation with tie-break rules
│       │           │
│       │           ├── data/
│       │           │   └── DataLoader.java        Reads config.properties and classpath CSV files
│       │           │
│       │           └── model/                     Domain entities (all Serializable)
│       │               ├── Card.java              Development card: level, gem type, cost, prestige
│       │               ├── Noble.java             Noble tile: name, prestige, bonus requirements
│       │               ├── Token.java             Single gem chip with a colour
│       │               └── Player.java            Player state: tokens, cards, nobles, prestige
│       │
│       └── resources/
│           ├── config.properties                  Game parameters (points to win, token counts, file paths)
│           ├── cards.csv                          90 development cards (level, colour, prestige, costs)
│           ├── nobles.csv                         10 noble tiles (name, prestige, requirements)
│           │
│           ├── static/
│           │   └── css/
│           │       ├── index.css                  Styles for the home / new-game page
│           │       └── game.css                   Styles for in-game, CPU turn, and game-over views
│           │
│           └── templates/                         Thymeleaf HTML templates
│               ├── index.html                     New-game setup form (player names, types, drag-reorder)
│               ├── game.html                      Main board: token bank, card rows, nobles, player sidebar
│               ├── cpu-turn.html                  Animated play-by-play for CPU actions
│               └── game-over.html                 Final scores and winner announcement
│
└── target/                                        Maven build output (generated, not committed)
```

---

## Technologies Used

| Technology | Purpose |
|---|---|
| **Java 17** | Application language. Uses modern features such as records, switch expressions, and text blocks. |
| **Spring Boot 3.2.0** | Application framework providing auto-configuration, dependency injection, and an embedded Tomcat server so the game runs as a standalone JAR with no external setup. |
| **Spring Web MVC** (`spring-boot-starter-web`) | HTTP routing via `@Controller` / `@GetMapping` / `@PostMapping`, form parameter binding, redirect-with-flash-attributes (PRG pattern), and `HttpSession` for stateful game storage. |
| **Jackson** (transitive via Spring Web) | JSON serialization used by `HomeController` to parse the `playersJson` form field into `PlayerSetupPayload` objects with `ObjectMapper`. |
| **Thymeleaf** (`spring-boot-starter-thymeleaf`) | Server-side HTML templating engine. Templates use `th:each`, `th:if`, `th:text`, `th:href`, and `th:action` to render game state into pages without any client-side framework. |
| **Jakarta Servlet API** | `HttpSession` stores `GameSession` across requests so each browser tab holds its own independent game. |
| **Apache Maven** | Build tool and dependency manager. The Spring Boot Maven plugin packages the project into an executable JAR. |
| **HTML / CSS / JavaScript** | Front end is plain static assets and inline scripts in Thymeleaf templates -- no npm, React, or Vue. CSS custom properties and vanilla JS handle the animated background on the home page, drag-and-drop player reordering, toast notifications, and the guided tour overlay. |

---

## Game Logic

### Starting a New Game

1. The player visits `/` and fills in a setup form (2--4 seats, each with a name and human/CPU toggle). Players are drag-reordered so that the topmost player goes first (youngest) and the last goes last (oldest).
2. The form posts a JSON array to `POST /new-game`. `HomeController` deserializes it, validates the count, and calls `GameService.newGame()`.
3. `WebGameSetupService` reads `config.properties` for point threshold, token counts (scaled to player count), and gold count. It then:
   - Creates a `GameSession` with the bank tokens and empty card/noble lists.
   - Parses `cards.csv` into three closed decks (levels 1--3) and draws four random face-up cards per level.
   - Parses `nobles.csv`, shuffles, and selects `playerCount + 1` nobles.
4. The session is stored in `HttpSession` and the browser is redirected to `GET /game`.

### The Gameplay Loop

Every request to `GET /game` follows the same decision tree in `GameController`:

1. **No session?** Redirect to `/` with an error flash.
2. **Game over?** Show `game-over.html` with final scores and winner(s).
3. **Current player is human?** Render `game.html` with the full board. The human picks one of:
   - **Take 3 different tokens** (`POST /game/take3`) -- three distinct gem colours, each with at least one chip in the bank.
   - **Take 2 same tokens** (`POST /game/take2`) -- one colour that has four or more in the bank.
   - **Reserve a card** from the table (`POST /game/reserve-table`) or from the top of a closed deck (`POST /game/reserve-deck`) -- up to three reserves allowed; the player receives one gold token if available.
   - **Buy a card** from the table (`POST /game/buy-table`) or from reserved hand (`POST /game/buy-reserved`) -- the player pays with gem tokens minus bought-card discounts, using gold as a wildcard for any shortfall.
4. **Current player is CPU?** `CpuTurnService.executeOneCPUTurn()` runs a complete turn (see CPU Logic below), generates log lines, and renders `cpu-turn.html` with an animated play-by-play.

### After Every Action

After a valid human or CPU action:

1. **Token limit check:** if the player holds more than 10 tokens, the game enters a return-tokens sub-phase. The human selects which to give back (`POST /game/return-tokens`); the CPU returns whichever colour it holds the most of.
2. **Noble acquisition:** `NobleAcquisitionService` scans available nobles; if the player's purchased-card bonuses meet a noble's requirements, that noble is automatically assigned (prestige added).
3. **Final-round check:** `TurnPhaseService.checkGameEnd()` scans all players; if anyone has reached the prestige threshold (default 15), the `finalRound` flag is set. When the final round flag is on and the last player in turn order finishes, `gameOver` is set to true.
4. **Advance turn:** `currentPlayerIndex` wraps around to the next player.

### Winning

`GameOutcomeService.computeWinnerPlayerIds()` finds the highest prestige among all players, then breaks ties by fewest purchased cards. The result is a list of IDs (multiple in case of a true tie).

---

## CPU Logic

CPU decisions are handled by `WebCPUStrategy`, which reads the current `GameSession` bank and market. `CpuTurnService` creates a fresh strategy instance per turn, asks it what to do, executes the action through the same rules services that humans use, and collects log lines.

### Action Priority

The CPU evaluates actions top-down and picks the first one that is possible:

| Priority | Action | Condition |
|---|---|---|
| 1st | **Buy a card** | Any card on the table or in reserves is affordable (gems + discounts + gold). |
| 2nd | **Reserve a card** | Fewer than 3 reserved cards and at least one face-up card exists. |
| 3rd | **Take 2 same-colour tokens** | At least one colour has 4 or more chips in the bank. |
| 4th | **Take 3 different tokens** | At least 3 colours have 1 or more chips each. |

If none of these conditions are met the CPU passes.

### How Each Action is Chosen

- **Buy:** the CPU checks reserved cards first (buying from reserves if any is affordable); otherwise it scans open rows from level 3 down to level 1 and picks the affordable card with the highest prestige.
- **Reserve:** always reserves from the face-up table (not the deck). It picks the lowest level that has open cards and selects a random card within that row.
- **Take 2:** picks the first colour (in fixed order: black, blue, green, red, white) that still has 4 or more tokens in the bank.
- **Take 3:** shuffles the list of colours that have at least one token in the bank and picks the first three.

### Token Return

When the CPU ends up over the 10-token limit after taking gems, it returns the colour it holds the most of (including gold), repeating until within the limit.

### Noble Choice

If multiple nobles qualify simultaneously, the CPU picks one at random.

---

## How to Run

### Prerequisites

- **Java 17** or newer (`java -version` should report 17+)
- **Apache Maven** 3.8+ (`mvn -version`)

### Steps

1. **Clone the repository**

```bash
git clone <repository-url>
cd cs102-splendor-project
```

2. **Build and run**

```bash
mvn spring-boot:run
```

Maven will download dependencies on first run, compile the project, and start the embedded Tomcat server.

3. **Open in a browser**

Navigate to [http://localhost:8080](http://localhost:8080). The new-game setup page will appear.

4. **Play**

   - Choose 2--4 players, assign names and human/CPU types, drag to set turn order (top goes first), and press **Start Game**.
   - On your turn, pick an action from the board. CPU turns play out automatically with a step-by-step log.
   - The game ends once the final round completes; the winner screen shows scores and a link back to the menu.

### Alternative: Run as a JAR

```bash
mvn clean package -DskipTests
java -jar target/splendor-1.0.0.jar
```

Then open [http://localhost:8080](http://localhost:8080) as above.
