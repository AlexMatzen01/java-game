# Java Game V0.0.1a

# Voxel World (essentially just a Minecraft clone)

A voxel-based sandbox game with procedurally generated terrain, dynamic lighting, and a day/night cycle. Built in Java with LWJGL 3 and OpenGL 3.3.

## Features

- **Infinite procedural world** — layered value noise generates varied terrain with mountains, valleys, caves, and trees
- **Block rotation** — blocks with distinct textures (logs, grass) can be oriented along any axis; logs auto-orient to face the player when placed
- **OpenGL 3.3 renderer** — chunk-based mesh building with face culling, per-vertex ambient occlusion, and texture atlas
- **Dynamic lighting** — directional sun/moon light with time-of-day color blending
- **Cascaded shadow maps** — 4-level CSM at 4096x4096 per cascade for high-quality shadows
- **Volumetric light scattering** — god rays when looking toward the sun
- **Procedural sky** — dynamic cloud layer driven by FBM noise, sun disk with glow, moon with glow, star field
- **Day/night cycle** — 24-minute full cycle, sky/lighting smoothly transitions
- **Tone mapping & post-processing** — exposure, contrast, ambient boost, fog
- **Procedural GI volume** — 3D probe grid for indirect light approximation
- **3 game modes** — Survival (gravity, sprint, jump, void death), Creative (flight), Spectator (no interaction)
- **Binary save/load** — atomic saves with versioned format, persists all world state and player data

## Getting Started

### Prerequisites

- Java 17 or later
- A GPU with OpenGL 3.3 support

### Build & Run

```bash
# Build
./gradlew build

# Run
./gradlew run
```

### Command-line arguments

| Argument | Description |
|----------|-------------|
| `--fps-cap=<n>` or `--fps=<n>` | Limit FPS (`0` or `off`/`unlimited`/`none` = uncapped) |
| `--v-sync` or `--vsync` | Enable vertical sync |
| `--no-v-sync` or `--no-vsync` | Disable vertical sync |

## Controls

### Movement & Camera

| Key | Action |
|-----|--------|
| W / A / S / D | Move forward / left / backward / right |
| Space | Jump (Survival) / Fly up (Creative/Spectator) |
| Left Shift | Sprint (Survival) / Fly down (Creative/Spectator) |
| Mouse | Look around |
| Left Alt / Right Alt | Toggle cursor lock |

### Block Interaction

| Key | Action |
|-----|--------|
| Left mouse | Break targeted block (6m range) |
| Right mouse | Place selected block on targeted face |
| 1 | Select dirt |
| 2 | Select cobblestone |
| 3 | Select grass |
| 4 | Select leaves |

### Game Mode

| Key | Action |
|-----|--------|
| F6 | Cycle: Survival → Creative → Spectator |

### Menu & UI

| Key | Action |
|-----|--------|
| Escape | Open menu / close graphics GUI / quit |
| Up / Down (menu) | Navigate world list |
| Up / Down (graphics GUI) | Select setting |
| Left / Right (graphics GUI) | Adjust setting |
| Enter (menu) | Load selected world |
| N (menu) | Create new world |
| F5 (menu) | Refresh world list |
| G (menu) | Open graphics settings |
| F3 (in-game) | Toggle graphics settings GUI |

## Graphics Settings

Press **F3** in-game to open the graphics settings panel:

| Setting | Range | Description |
|---------|-------|-------------|
| Lighting | ON / OFF | Toggles realistic lighting |
| Exposure | 55% – 170% | Overall brightness |
| Ambient | 45% – 145% | Ambient light boost |
| Contrast | 75% – 145% | Image contrast |
| Shadows | 20% – 100% | Shadow visibility |
| Shadow Radius | LOW / MED / HIGH | Shadow filter quality |
| Fog | 0% – 100% | Fog density |
| Sky Saturation | 60% – 140% | Sky color intensity |
| Render Distance | 2 – 8 chunks | View distance |
| Build Budget | 2 – 32 chunks/frame | Mesh rebuild rate |

## Project Structure

```
src/main/java/
├── engine/          Game loop, entry point, save/load, settings
├── player/          Player movement, physics, camera, block interaction
├── input/           Keyboard/mouse input handling
├── world/           Chunks, terrain generation, meshing, raycasting
├── blocks/          Block definitions and registry
├── render/          OpenGL renderer, texture atlas
└── util/            Math helpers, tick system
```

## Tech Stack

- **Language:** Java 17
- **Graphics:** LWJGL 3.3.4 (OpenGL 3.3, GLFW, STB image)
- **Math:** JOML 1.10.5
- **Build:** Gradle 9.5.1

## License

This project is provided for educational and reference purposes. The Gradle wrapper scripts are licensed under the Apache License 2.0. See the Gradle license at `https://gradle.org/license/` for details.

The `assets/assets/` directory contains resources derived from Minecraft and may be subject to Mojang's asset licensing terms.
