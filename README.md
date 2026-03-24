# MemoryFix

MemoryFix is a focused Fabric mod for Minecraft 1.7.10 that fixes the singleplayer reset path so repeated world loads return close to the title-screen memory baseline instead of steadily retaining shutdown state.

## What It Fixes

- waits for the integrated server to fully stop before save storage is cleared
- drains pending chunk IO before region handles are released
- clears stale client and server world references during disconnect
- recycles `WorldRenderer` OpenGL display lists and occlusion queries on world teardown
- closes orphaned title-screen and texture-manager GL resources
- hard-resets `IntArrayCache` between world loads

# Testing

Testing was performed on the following:

- Ryzen 9 9950x
- RTX 3060
- 64GB RAM 6000mhz

Mods used:

- Atum
- Extra Options
- Force Port Mod
- Hermes Core
- Legacy Crash Fix
- Legacy Planifolia
- MemoryFix (this mod)
- Optifabric
- Optifine
- SleepBackground
- SpeedRunIGT
- StandardSettings
- StateOutput
- TabFocus
- ZBufferFog

All testing was done with 100 world resets.

## No Memory Fix (before)

<img width="489" height="257" alt="image" src="https://github.com/user-attachments/assets/a533b09d-005d-4938-ab81-43dcfd32bc6f" />

## No Memory Fix (after)

<img width="485" height="253" alt="image" src="https://github.com/user-attachments/assets/f0ae33f1-04e7-404b-9598-16cabf227397" />

## Using Memory Fix (before)

<img width="489" height="253" alt="image" src="https://github.com/user-attachments/assets/dbde9496-0a8a-4c4c-b58a-6d1922664e4a" />

## Using Memory Fix (after)

<img width="494" height="226" alt="image" src="https://github.com/user-attachments/assets/39660073-7c18-414e-8043-5cc095bba483" />
