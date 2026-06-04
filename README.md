# Hex Strategy Game

A 4X strategy game with hexagonal grid maps built in Java using Swing.

## Project Structure

```
hex-strategy-game/
├── pom.xml                          # Maven configuration
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/hexgame/
│   │   │       ├── Main.java        # Entry point
│   │   │       ├── HexGrid.java     # Main game grid & rendering
│   │   │       ├── Tile.java        # Tile/hexagon representation
│   │   │       └── Scout.java       # Unit class
│   │   └── resources/               # Game assets (if any)
│   └── test/
│       └── java/                    # Unit tests
├── target/                          # Build output (generated)
└── README.md
```

## Features

- **Hexagonal Grid**: 50x32 hex grid with flat-top layout
- **Fog of War**: Vision system with explore/visible states
- **Pathfinding**: A* pathfinding for unit movement
- **Resource System**: Resource yields and building efficiency per terrain type
- **Turn-based Gameplay**: Player and AI turns with 4 players
- **Units**: Scout units with movement range and vision radius
- **Terrain Types**: Ocean, Plains, Forest, Mountain, Desert, Tundra, Base
- **Resource Economy**: Coal, Gold, Iron, Wood, Uranium, Energy tracking

## Build & Run

### Prerequisites
- Java 11+
- Maven 3.6+

### Compile
```bash
mvn clean compile
```

### Run
```bash
mvn exec:java@run
```

Or build and run JAR:
```bash
mvn clean package
java -jar target/hex-strategy-game-1.0-SNAPSHOT.jar
```

## Controls

- **Arrow Keys**: Scroll camera around the map
- **Mouse**: Click to select tiles and units
- **Mouse Motion**: Hover to see pathfinding preview
- **Enter**: End current player's turn

## Terrain & Resources

| Terrain    | Yields                          | Efficiency |
|------------|--------------------------------|------------|
| Plains     | None                           | 100%       |
| Forest     | Wood (3.0-5.5)                 | 75%        |
| Mountain   | Coal (1.0-2.5), Iron (0.7-1.0) | 50%        |
| Desert     | Solar Energy (100%)            | 70%        |
| Tundra     | Gold (9.0-12.0), U-235 (0.15-0.50) | 60% |
| Ocean      | None                           | 0%         |
| Base       | None                           | 100%       |

## Game Flow

1. Each player controls a base and scout units
2. Scouts can move across the map (3 moves per turn)
3. Fog of war reveals terrain and resources
4. AI automatically moves on their turns
5. Resources are tracked in player economy banks
6. Building efficiency affects resource production

## Architecture

- **HexGrid**: Main game logic and rendering engine
- **Tile**: Represents hexagonal cells with terrain, yields, and visibility states
- **Scout**: Unit class with movement, vision, and ownership tracking
- **PlayerState**: Manages per-player economy and resources
