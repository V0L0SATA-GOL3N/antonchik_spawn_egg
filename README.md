# Antonchik

A small Minecraft **1.12.2** (Forge) mod that adds the **Antonchik** — a towering, hostile creature that wanders every biome day and night — and a bottle of **Jameson** you can use to calm it down... or drink yourself, at your own risk.

---

## Features

### 🧌 The Antonchik
- A custom mob rendered from a hand-loaded **OBJ model** (with its own `.obj` / `.mtl` interpreter), scaled to roughly twice player height (`1.2 × 3.6` hitbox).
- Spawns naturally like a monster — **in all biomes, in daylight and at night** — and is hostile on sight.
- **Pacify it with a Jameson:** feed the mob a bottle and it stops attacking and becomes leashable.
- **Custom lead handling:** right-click a pacified Antonchik with a lead to leash it and again to unleash it — the lead is **never consumed or dropped**. Tying to a fence works as in vanilla.

### 🥃 Jameson (whiskey bottle)
- **Craftable:** sugar stacked on top of a water bottle (a 1-wide, 2-tall shaped recipe).
- **Feed it** to an Antonchik to pacify the mob, **or drink it yourself**.
- Drinking returns an empty **glass bottle**.

### 🍺 Drunkenness (the `Drunk` effect)
- Each bottle adds **60 seconds** of drunkenness; effects **stack** the more you drink.
- Drunkenness is tracked as a **blood-alcohol level (per mille)** and shown on a **HUD readout** while you're under the influence.
- The more drunk you are, the stronger the slowness (scales in severity as you keep drinking).
- **Know your limit:** drinking past a blood-alcohol level of **0.8‰** is **fatal** — `<player> was too drunk`.

### 🥚 Spawn egg
- An **Antonchik spawn egg** for spawning the mob directly.

---

## Installation

1. Install **Minecraft Forge for 1.12.2**.
2. Drop the mod `.jar` into your `mods/` folder.
3. Launch the game.

## Building from source

This is a ForgeGradle project for 1.12.2, which runs on **Gradle 5.6.4 / Java 8**.

```bash
./gradlew build       # build the mod jar (in build/libs)
./gradlew runClient   # launch a dev client
```

> **Note:** the build is pinned to Java 8 via `org.gradle.java.home` in `gradle.properties`, since Gradle 5.6.4 does not run on newer JDKs.

---

## License & credits

A personal/learning project. Minecraft and Forge are the property of their respective owners.
