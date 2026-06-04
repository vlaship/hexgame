# HexGame — Claude Instructions

## Spec file

**Always update the spec after each session:**
`docs/superpowers/specs/2026-06-03-game-brainstorm-notes.md`

Update when anything changes:
- New features implemented or planned
- Bugs found or fixed
- Tech stack decisions made
- Distribution / CI changes

## Project overview

Turn-based 4X strategy, Java + Swing + Maven. See the spec file for full details.

## Key files

| File | Purpose |
|---|---|
| `src/main/java/com/hexgame/Main.java` | Entry point |
| `src/main/java/com/hexgame/HexGrid.java` | Map, rendering, game logic |
| `src/main/java/com/hexgame/Scout.java` | Scout unit |
| `src/main/java/com/hexgame/Tile.java` | Tile model |
| `config.properties` | Screen size, key bindings |
| `pom.xml` | Maven build, `finalName=hex-strategy-game` |
| `.github/workflows/build.yml` | Native installers via jpackage |

## Git conventions

- Feature branches, PR to master
- Tags trigger installer builds: `v0.0.x`, `v1.x.x`, etc.
- No Claude co-author in commits
