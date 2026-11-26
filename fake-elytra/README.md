# Fake Elytra (Fabric, Minecraft 1.21.11, client-side only)

- Wear a **copper chestplate**: on your screen your player shows **elytra wings** instead of the chestplate (press F5 to see).
- **Double-tap jump** while in the air: your player goes into the **elytra glide pose** with wings spread, and the elytra wind sound plays.
- Double-tap again, land, or touch water to stop.

It's 100% visual and only happens on your client. You fall at normal speed, the server never knows, and no item is created.
Tip: jump off something tall and use F5 (third person) for screenshots or a clip.

## Build the .jar

You need **Java 21** (JDK) installed, e.g. from https://adoptium.net

**Windows:** open this folder, click the address bar, type `cmd`, press Enter, then run:

    gradlew build

**Mac/Linux:**

    ./gradlew build

The first build downloads Minecraft + Fabric, so it takes a few minutes.
Your mod will be at `build/libs/fake-elytra-1.0.0.jar`.

(No Java? Put this folder on GitHub and the included Actions workflow builds the jar for you; download it from the Actions tab.)

## Install

1. Install **Fabric Loader** for 1.21.11 from https://fabricmc.net/use/installer/
2. Put `fake-elytra-1.0.0.jar` in your `.minecraft/mods` folder.
3. Launch the Fabric 1.21.11 profile. (Fabric API is **not** required.)
