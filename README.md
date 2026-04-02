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

## Design Considerations

- **Layered responsibilities:** HTTP handling lives in `web.controller` classes; domain state and rules live in `web.game` (facade, focused services, and `model`). That keeps request/response concerns separate from Splendor rules and makes the flow easier to follow and change.
- **Facade over a split rule set:** `GameService` is the single entry point controllers use. It delegates to small services (`WebTokenRulesService`, `WebCardRulesService`, `TurnPhaseService`, `CpuTurnService`, and others) so each class has one main job instead of one monolithic game class.
- **Stateful games without a database:** Each browser’s `HttpSession` holds one `GameSession` (Serializable). That matches a course-scale deployment (no DB setup) while still isolating one game per tab.
- **One rules path for humans and CPUs:** CPU turns call the same token and card rule services as human actions, so behaviour stays consistent and fixes apply to both paths.
- **Configuration and data outside code:** `config.properties` and CSV files drive counts, paths, cards, and nobles, so balancing and content tweaks rarely require recompiling Java.
- **PRG and flash messages:** After `POST` actions, controllers redirect back to `GET` with flash attributes where appropriate, which avoids duplicate submits and keeps URLs bookmarkable for the main board view.
- **Server-rendered UI:** Thymeleaf builds HTML on the server from the current `GameSession`, which simplifies state sync compared to a separate SPA talking to many JSON endpoints.

---

## Adherence to the Four OOP Principles

### Encapsulation

Domain classes (`Player`, `Card`, `Noble`, `Token`) keep fields **private** and expose behaviour through **getters** and methods that enforce invariants (for example token counts, prestige, and collections managed inside `Player`). `GameSession` bundles bank, market, and turn data behind accessors so controllers work with intentional operations instead of reaching into arbitrary fields. Services encapsulate **how** rules are enforced; controllers only orchestrate HTTP and call `GameService`.

### Abstraction

Controllers and `GameService` callers depend on **high-level operations** (`newGame`, take tokens, buy/reserve cards, finish turns) without needing to know every internal step. The **facade** (`GameService`) hides which underlying service handles each case. **Strategy-style** CPU decision logic in `WebCPUStrategy` abstracts “what should the CPU do?” from the mechanics in `CpuTurnService` that apply the chosen action through the shared rules services.

### Inheritance

The domain layer uses **little class extension**; game types are mostly concrete classes tailored to Splendor. The main shared **supertype contract** is **`Serializable`**: `GameSession` and model classes implement it so game state can live in the HTTP session as a single serialized graph. Application classes also participate in Spring’s component model (`@SpringBootApplication`, `@Controller`, `@Service`), which relies on the framework’s class hierarchy and metadata—our code focuses on **composition** (constructor-injected collaborators) rather than deep `extends` trees.

### Polymorphism

**Runtime dispatch** appears wherever the same API is used for different situations: human and CPU players are both represented as **`Player`** instances, and turn flow branches on `isHuman()` while reusing the same rule services. **Method overriding** (for example `toString()` on `Token`) lets logging and templates treat objects uniformly. **Spring dependency injection** resolves concrete `@Service` implementations behind constructor types, so the façade and controllers depend on abstractions at the API level while the container supplies the active implementations at runtime.

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
