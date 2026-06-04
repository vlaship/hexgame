# HexGame — Design Notes (2026-06-03)

## Concept

Turn-based 4X strategy set in a sci-fi space theme. 4 players (1 human + 3 AI) compete for dominance
on a hexagonal map of 4 islands. Victory condition: military (eliminate all enemies).
No technology tree; depth comes from resource management and economy.

---

## Tech Stack (locked by code)

| Parameter | Value |
|---|---|
| Language | Java |
| UI | Swing + Graphics2D (`javax.swing`, `java.awt`) |
| Build | Maven (`pom.xml`) |
| Package | `com.hexgame` |
| Resolution | set via `config.properties` (currently 2000×1300) |
| Distribution | `jpackage` — native installer per platform (bundled JRE, no Java install needed) |

---

## Configuration (`config.properties`)

| Key | Current Value | Description |
|---|---|---|
| `screen.width` | 2000 | Window width in pixels |
| `screen.height` | 1300 | Window height in pixels |
| `camera.up` | `W UP` | Scroll up keys |
| `camera.down` | `S DOWN` | Scroll down keys |
| `camera.left` | `A LEFT` | Scroll left keys |
| `camera.right` | `D RIGHT` | Scroll right keys |

Keys are specified as `KeyEvent.VK_*` names without the prefix, space-separated (e.g. `W UP NUMPAD8`).

---

## Already Implemented

### Map
- Hexagonal grid 50×32, flat-top
- Tile types: `OCEAN`, `PLAINS`, `FOREST`, `MOUNTAIN`, `DESERT`, `TUNDRA`, `BASE`
- Generation: 4 islands (procedural, distance-based from island centers)
- Camera scrolling via keyboard and edge-scroll with mouse

### Camera Controls
- Keyboard: bindings from `config.properties` (default: WASD + arrow keys)
- Edge scroll: mouse within 40px of edge → camera moves at 8px/tick (~60fps)
- Scrollbars in HUD (visual indicator of camera position)

### Fog of War
- Two states per tile: `isExplored` (revealed) / `isVisible` (currently in sight)
- Player base reveals 6 hexes around it
- Scouts reveal `visionRadius = 4` hexes
- Dark / dimmed tiles render differently

### Resources (6 types)
Bound to tiles via `yieldXxx` fields:

| Resource | Terrain | Range |
|---|---|---|
| Coal | MOUNTAIN | 1.0–2.5 / tile |
| Iron | MOUNTAIN | 0.7–1.0 / tile |
| Wood | FOREST | 3.0–5.5 / tile |
| Gold | TUNDRA | 9.0–12.0 / tile |
| Uranium | TUNDRA | 0.15–0.50 / tile |
| Energy | DESERT | 1.0 (solar) / tile |

Resource bank stored in `PlayerState` (fields: `coal`, `gold`, `iron`, `wood`, `uranium`, `energy`).
Building efficiency (`buildingEfficiency`) is defined per tile.

**! Resource collection not yet implemented** — yields are not accumulated each turn.

### Units
- Only type: `Scout` — `maxMoves=3`, `visionRadius=4`, `dirIndex` for circular AI movement
- HUD: shows `movesLeft / maxMoves` above the selected scout
- Pathfinding: BFS, limited by `movesLeft`, cannot cross OCEAN or unexplored tiles
- Path visualization: dashed line with dots to cursor

### Players
- 4 players: Player 1 (Human), Player 2–4 (AI)
- Colors: yellow, red, green, blue
- Bases placed at the four corners of the map

### Turn System
- ENTER — end human turn
- AI takes turn via `javax.swing.Timer` (1 second delay)
- AI movement: scout moves in a circle (6 directions, `dirIndex` increments each turn; skips blocked directions)
- HUD banner "Enemy Phase..." during AI turn
- `completeTurnCycle()` — resets moves, updates fog of war

### HUD
- Turn panel (Turn N | player name)
- Economy bar (Bank: Coal / Gold / Iron / Wood / U-235 / Energy)
- Tile info panel (terrain type, yields, efficiency) on selection
- Camera scrollbars

---

## Not Yet Implemented

| Feature | Priority | Notes |
|---|---|---|
| **Resource collection** | High | Yields not accumulated. Logic needed in `completeTurnCycle()` |
| **Combat system** | High | No attack / damage / base capture |
| **Victory condition** | High | No check for "all enemies eliminated" |
| **AI** | Medium | Circular movement is a stub; real strategy needed |
| **Additional units** | Medium | Only Scout exists; military units needed |
| **Buildings** | Medium | `buildingEfficiency` defined but nothing to build |
| **Base capture** | High | No logic for transferring BASE ownership between players |
| **AI move reset** | Bug | `completeTurnCycle` only resets moves for Player 1 |
| **Victory screen** | Low | No end-game UI |

---

## Known Bugs

1. `completeTurnCycle()` calls `u.resetMoves()` only for `ownerIndex == 0` — AI scouts
   never have their moves restored.
2. The AI timer `while` loop processes all AI players in a single tick, giving no animation between them.

---

## Distribution

- **Tool:** `jpackage` (bundled in JDK 14+), no Maven plugin needed
- **CI:** GitHub Actions (`.github/workflows/build.yml`), triggered on `v*` tags
- **Windows:** `.exe` installer with Start Menu entry (`--win-menu --win-dir-chooser`)
- **macOS:** DMG — disabled for versions `0.x.x` (Apple requires first version number ≥ 1)
- **Linux:** DEB — disabled for versions `0.x.x` (same policy as macOS for now)
- **Versioning:** tag `v0.0.3` → installer version `0.0.3`; macOS/Linux require `v1.0.0+`
- **Actions versions:** `checkout@v6`, `setup-java@v5`, `upload-artifact@v7`

---

## Next Steps (to discuss)

1. Resource collection mechanic (which tile, how much per turn, does it require a building)
2. Combat system (Scout vs Scout attacks, BASE capture)
3. New unit types (warrior? worker?)
4. AI strategy (map exploration → attack)
5. Enable macOS + Linux builds when version reaches `v1.0.0`
