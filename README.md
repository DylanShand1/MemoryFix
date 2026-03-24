# MemoryFix

MemoryFix is a focused Fabric mod for Minecraft 1.7.10 that fixes the singleplayer reset path so repeated world loads return close to the title-screen memory baseline instead of steadily retaining shutdown state.

## What It Fixes

- waits for the integrated server to fully stop before save storage is cleared
- drains pending chunk IO before region handles are released
- clears stale client and server world references during disconnect
- recycles `WorldRenderer` OpenGL display lists and occlusion queries on world teardown
- closes orphaned title-screen and texture-manager GL resources
- hard-resets `IntArrayCache` between world loads
