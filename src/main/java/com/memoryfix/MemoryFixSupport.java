package com.memoryfix;

import com.memoryfix.mixin.ClientChunkProviderAccessor;
import com.memoryfix.mixin.ClientPlayerEntityAccessor;
import com.memoryfix.mixin.ClientPlayNetworkHandlerAccessor;
import com.memoryfix.mixin.ClientPlayerInteractionManagerAccessor;
import com.memoryfix.mixin.ClientWorldAccessor;
import com.memoryfix.mixin.ChunkAccessor;
import com.memoryfix.mixin.ControllablePlayerEntityAccessor;
import com.memoryfix.mixin.EntityAccessor;
import com.memoryfix.mixin.EntityTrackerAccessor;
import com.memoryfix.mixin.PersistentStateManagerAccessor;
import com.memoryfix.mixin.PlayerWorldManagerAccessor;
import com.memoryfix.mixin.PortalTeleporterAccessor;
import com.memoryfix.mixin.ScoreboardAccessor;
import com.memoryfix.mixin.ServerNetworkIoAccessor;
import com.memoryfix.mixin.ServerChunkProviderAccessor;
import com.memoryfix.mixin.ServerWorldAccessor;
import com.memoryfix.mixin.WorldRendererAccessor;
import com.memoryfix.mixin.WorldAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.util.GlAllocationUtils;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.PortalTeleporter;
import net.minecraft.entity.player.ControllablePlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.ServerNetworkIo;
import net.minecraft.server.PlayerWorldManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.IntArrayCache;
import net.minecraft.util.collection.IntObjectStorage;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ClientChunkProvider;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ServerChunkProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.ARBOcclusionQuery;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class MemoryFixSupport {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int WORLD_RENDER_OCCLUSION_QUERY_COUNT = 34 * 34 * 16;
    private static final int WORLD_RENDER_DISPLAY_LIST_COUNT = WORLD_RENDER_OCCLUSION_QUERY_COUNT * 3;
    private static final Field INT_ARRAY_SIZE_FIELD = findStaticField(IntArrayCache.class, int.class);
    private static final List<Field> INT_ARRAY_LIST_FIELDS = findStaticFields(IntArrayCache.class, List.class);

    private MemoryFixSupport() {
    }

    public static void resetIntArrayCache() {
        if (INT_ARRAY_SIZE_FIELD == null || INT_ARRAY_LIST_FIELDS.isEmpty()) {
            return;
        }

        synchronized (IntArrayCache.class) {
            for (Field field : INT_ARRAY_LIST_FIELDS) {
                try {
                    Object value = field.get(null);
                    if (value instanceof List) {
                        ((List<?>) value).clear();
                    }
                } catch (IllegalAccessException ignored) {
                }
            }

            try {
                INT_ARRAY_SIZE_FIELD.setInt(null, 256);
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    public static void releaseMemory() {
        System.runFinalization();
        System.gc();
        System.runFinalization();
        System.gc();
    }

    public static void clearWorldRendererState(Object renderer) {
        if (renderer == null) {
            return;
        }

        MinecraftClient client = findFieldValue(renderer, MinecraftClient.class);
        for (Field field : renderer.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);

            try {
                Class<?> type = field.getType();
                Object value = field.get(renderer);
                if (value == null) {
                    continue;
                }

                if (type.isArray() && BufferBuilder.class.isAssignableFrom(type.getComponentType())) {
                    clearBufferBuilders(value);
                    field.set(renderer, null);
                    continue;
                }

                if (value instanceof Map) {
                    clearWorldRendererMap(client, (Map<?, ?>) value);
                    continue;
                }

                if (value instanceof List) {
                    ((List<?>) value).clear();
                }
            } catch (IllegalAccessException ignored) {
            }
        }

        recycleWorldRendererGlResources(renderer);
    }

    private static void recycleWorldRendererGlResources(Object renderer) {
        if (!(renderer instanceof WorldRendererAccessor)) {
            return;
        }

        WorldRendererAccessor accessor = (WorldRendererAccessor) renderer;
        recycleDisplayListBlock(accessor.memoryfix$getChunkRenderListBase(), WORLD_RENDER_DISPLAY_LIST_COUNT);
        recycleDisplayListBlock(accessor.memoryfix$getStaticEntityRenderList(), 1);
        int queryCount = 0;
        IntBuffer oldQueryIds = accessor.memoryfix$getOcclusionQueryIds();
        if (oldQueryIds != null) {
            queryCount = recycleOcclusionQueries(oldQueryIds);
        } else if (accessor.memoryfix$usesOcclusionQueries()) {
            queryCount = WORLD_RENDER_OCCLUSION_QUERY_COUNT;
        }

        accessor.memoryfix$setChunkRenderListBase(GlAllocationUtils.genLists(WORLD_RENDER_DISPLAY_LIST_COUNT));
        accessor.memoryfix$setStaticEntityRenderList(GlAllocationUtils.genLists(1));
        if (queryCount > 0) {
            IntBuffer queryIds = GlAllocationUtils.allocateIntBuffer(queryCount);
            queryIds.clear();
            queryIds.limit(queryCount);
            ARBOcclusionQuery.glGenQueriesARB(queryIds);
            accessor.memoryfix$setOcclusionQueryIds(queryIds);
        }

        GL11.glFinish();
    }

    private static int recycleDisplayListBlock(int baseId, int count) {
        if (baseId <= 0) {
            return 0;
        }

        try {
            GlAllocationUtils.deleteSingletonList(baseId);
        } catch (Throwable throwable) {
            LOGGER.warn("[MemoryFix] Failed to recycle GL display list block {}", Integer.valueOf(baseId), throwable);
        }

        return count;
    }

    private static int recycleOcclusionQueries(IntBuffer queryIds) {
        IntBuffer copy = queryIds.duplicate();
        copy.clear();
        int count = copy.remaining();
        try {
            ARBOcclusionQuery.glDeleteQueriesARB(copy);
        } catch (Throwable throwable) {
            LOGGER.warn("[MemoryFix] Failed to recycle occlusion queries", throwable);
        }
        return count;
    }

    private static void clearBufferBuilders(Object buildersArray) {
        int length = Array.getLength(buildersArray);
        for (int i = 0; i < length; i++) {
            Object entry = Array.get(buildersArray, i);
            if (entry instanceof BufferBuilder) {
                clearBufferBuilder((BufferBuilder) entry);
            }
        }

        if (buildersArray instanceof Object[]) {
            Arrays.fill((Object[]) buildersArray, null);
        }
    }

    private static void clearBufferBuilder(BufferBuilder builder) {
        for (Field field : builder.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || !List.class.isAssignableFrom(field.getType())) {
                continue;
            }

            field.setAccessible(true);
            try {
                Object value = field.get(builder);
                if (value instanceof List) {
                    ((List<?>) value).clear();
                }
            } catch (IllegalAccessException ignored) {
            }
        }

        builder.method_1313();
    }

    private static void clearWorldRendererMap(MinecraftClient client, Map<?, ?> map) {
        if (client != null) {
            for (Object value : map.values()) {
                if (value instanceof SoundInstance) {
                    client.getSoundManager().stop((SoundInstance) value);
                }
            }
        }

        map.clear();
    }

    public static void clearClientWorldState(Object clientWorld) {
        if (!(clientWorld instanceof ClientWorld)) {
            return;
        }

        ClientWorldAccessor clientWorldAccessor = (ClientWorldAccessor) clientWorld;
        WorldAccessor worldAccessor = (WorldAccessor) clientWorld;

        invokeNoArg(clientWorld, "clearEntities");
        worldAccessor.memoryfix$setLoadedEntities(new ArrayList());
        worldAccessor.memoryfix$setUnloadedEntities(new ArrayList());
        worldAccessor.memoryfix$setBlockEntities(new ArrayList());
        worldAccessor.memoryfix$setPendingBlockEntities(new ArrayList());
        worldAccessor.memoryfix$setUnloadedBlockEntities(new ArrayList());
        worldAccessor.memoryfix$setPlayerEntities(new ArrayList());
        worldAccessor.memoryfix$setEntities(new ArrayList());
        worldAccessor.memoryfix$setEventListeners(new ArrayList());
        worldAccessor.memoryfix$setActiveChunks(new HashSet());
        worldAccessor.memoryfix$setCollisionBoxes(new ArrayList());
        clientWorldAccessor.memoryfix$setTrackedEntities(new HashSet());
        clientWorldAccessor.memoryfix$setPendingSpawnEntities(new HashSet());
        safeClearCollection(clientWorldAccessor.memoryfix$getPreviousChunkPos());
        clearPersistentStateManager(worldAccessor.memoryfix$getPersistentStateManager());
        clearScoreboard(worldAccessor.memoryfix$getScoreboard());
        clearStateValue(clientWorldAccessor.memoryfix$getEntityIndex());
        clientWorldAccessor.memoryfix$setEntityIndex(new IntObjectStorage());
        clearClientChunkProviderState(clientWorldAccessor.memoryfix$getClientChunkCache());
        clearClientChunkProviderState(worldAccessor.memoryfix$getChunkProvider());
        clientWorldAccessor.memoryfix$setClientChunkCache(null);
        worldAccessor.memoryfix$setChunkProvider(null);
        clientWorldAccessor.memoryfix$setClientNetHandler(null);
    }

    public static void clearClientNetworkHandlerState(Object networkHandler) {
        if (!(networkHandler instanceof ClientPlayNetworkHandler)) {
            return;
        }

        ClientPlayNetworkHandlerAccessor accessor = (ClientPlayNetworkHandlerAccessor) networkHandler;

        accessor.memoryfix$setWorld(null);
        safeClearMap(accessor.memoryfix$getPlayerListEntries());
        safeClearCollection(accessor.memoryfix$getTabEntries());
        accessor.memoryfix$setInventoryAchievementHintShown(false);
        accessor.memoryfix$setPositionLookSetup(false);
        clearPersistentStateManager(accessor.memoryfix$getStateManager());
    }

    public static void clearClientPlayerState(Object player) {
        if (!(player instanceof ControllablePlayerEntity)) {
            return;
        }

        EntityAccessor entityAccessor = (EntityAccessor) player;
        ClientPlayerEntityAccessor clientPlayerAccessor = (ClientPlayerEntityAccessor) player;
        ControllablePlayerEntityAccessor controllablePlayerAccessor = (ControllablePlayerEntityAccessor) player;
        entityAccessor.memoryfix$setVehicle(null);
        entityAccessor.memoryfix$setRider(null);
        entityAccessor.memoryfix$setWorld(null);
        clientPlayerAccessor.memoryfix$setInput(null);
        clientPlayerAccessor.memoryfix$setClient(null);
        controllablePlayerAccessor.memoryfix$setNetworkHandler(null);
    }

    public static void clearClientInteractionManagerState(Object interactionManager) {
        if (!(interactionManager instanceof ClientPlayerInteractionManager)) {
            return;
        }

        ClientPlayerInteractionManagerAccessor accessor = (ClientPlayerInteractionManagerAccessor) interactionManager;
        accessor.memoryfix$setSelectedStack(null);
        accessor.memoryfix$setBreakingBlock(false);
        accessor.memoryfix$setCurrentBreakingProgress(0.0F);
        accessor.memoryfix$setBlockBreakingSoundCooldown(0.0F);
        accessor.memoryfix$setBlockBreakingCooldown(0);
        accessor.memoryfix$setField1648(-1);
        accessor.memoryfix$setField1649(-1);
        accessor.memoryfix$setField1650(-1);
        accessor.memoryfix$setLastSelectedSlot(-1);
    }

    public static void clearServerWorldState(Object serverWorld) {
        if (serverWorld == null) {
            return;
        }

        if (serverWorld instanceof ServerWorld) {
            WorldAccessor worldAccessor = (WorldAccessor) serverWorld;
            ServerWorldAccessor serverWorldAccessor = (ServerWorldAccessor) serverWorld;
            worldAccessor.memoryfix$setLoadedEntities(new ArrayList());
            worldAccessor.memoryfix$setUnloadedEntities(new ArrayList());
            worldAccessor.memoryfix$setBlockEntities(new ArrayList());
            worldAccessor.memoryfix$setPendingBlockEntities(new ArrayList());
            worldAccessor.memoryfix$setUnloadedBlockEntities(new ArrayList());
            worldAccessor.memoryfix$setPlayerEntities(new ArrayList());
            worldAccessor.memoryfix$setEntities(new ArrayList());
            worldAccessor.memoryfix$setEventListeners(new ArrayList());
            worldAccessor.memoryfix$setActiveChunks(new HashSet());
            worldAccessor.memoryfix$setCollisionBoxes(new ArrayList());
            clearPersistentStateManager(worldAccessor.memoryfix$getPersistentStateManager());
            clearScoreboard(worldAccessor.memoryfix$getScoreboard());
            clearEntityTracker(serverWorldAccessor.memoryfix$getEntityTracker());
            clearPlayerWorldManager(serverWorldAccessor.memoryfix$getPlayerWorldManager());
            clearPortalTeleporter(serverWorldAccessor.memoryfix$getPortalTeleporter());
            clearStateValue(serverWorldAccessor.memoryfix$getEntityIdIndex());
            serverWorldAccessor.memoryfix$setEntityIdIndex(new IntObjectStorage());
            detachChunkProvider(serverWorldAccessor.memoryfix$getChunkCache());
            detachChunkProvider(worldAccessor.memoryfix$getChunkProvider());
            worldAccessor.memoryfix$setChunkProvider(null);
            worldAccessor.memoryfix$setPersistentStateManager(null);
            worldAccessor.memoryfix$setScoreboard(null);
            serverWorldAccessor.memoryfix$setChunkCache(null);
        }

        // Replace the main world-owned containers outright so old contents
        // cannot survive behind custom collection implementations.
        replaceFieldValue(serverWorld, "field_2811", new HashSet());
        replaceFieldValue(serverWorld, "scheduledTicks", new TreeSet());
        replaceFieldValue(serverWorld, "field_6729", new ArrayList());
        clearNamedState(serverWorld, "field_2815", "field_2818");
        clearOwnedFieldState(serverWorld, "field_6728", "field_4593");

        clearNestedOwnedFieldState(serverWorld, "chunkProvider", "chunkWriter", "field_4779", "chunksBeingSaved");
        clearNestedOwnedFieldState(serverWorld, "chunkCache", "chunkWriter", "field_4779", "chunksBeingSaved");
        clearNestedOwnedFieldState(serverWorld, "chunkProvider", "chunkGenerator", "biomes", "field_4861");
        clearNestedOwnedFieldState(serverWorld, "chunkCache", "chunkGenerator", "biomes", "field_4861");
        clearOwnedFieldState(serverWorld, "villageState", "positions", "doors", "villages");
        clearOwnedFieldState(
                serverWorld,
                "scoreboard",
                "objectives",
                "objectivesByCriterion",
                "playerObjectives",
                "objectivesArray",
                "teams",
                "teamsByPlayer",
                "state"
        );
        detachDimension(findFieldValueByName(serverWorld, "dimension"));
    }

    public static void clearServerNetworkIo(ServerNetworkIo networkIo) {
        if (networkIo == null) {
            return;
        }

        ServerNetworkIoAccessor accessor = (ServerNetworkIoAccessor) networkIo;
        safeClearCollection(accessor.memoryfix$getConnections());
        safeClearCollection(accessor.memoryfix$getChannels());
        accessor.memoryfix$setServer(null);
    }

    private static void clearOwnedFieldState(Object owner, String fieldName, String... nestedFieldNames) {
        Field field = findField(owner.getClass(), fieldName);
        if (field == null) {
            return;
        }

        try {
            Object value = field.get(owner);
            clearNamedState(value, nestedFieldNames);
        } catch (IllegalAccessException ignored) {
        }
    }

    private static void clearNestedOwnedFieldState(Object owner, String fieldName, String nestedFieldName, String... nestedFieldNames) {
        Object value = findFieldValueByName(owner, fieldName);
        if (value == null) {
            return;
        }

        clearOwnedFieldState(value, nestedFieldName, nestedFieldNames);
    }

    private static void detachChunkProvider(Object chunkProvider) {
        if (chunkProvider == null) {
            return;
        }

        if (chunkProvider instanceof ServerChunkProvider) {
            ServerChunkProviderAccessor accessor = (ServerChunkProviderAccessor) chunkProvider;
            scrubChunkCollection(accessor.memoryfix$getChunks());
            clearStateValue(accessor.memoryfix$getChunksToUnload());
            clearStateValue(accessor.memoryfix$getChunkStorage());
            safeClearCollection(accessor.memoryfix$getChunks());
            accessor.memoryfix$setChunkGenerator(null);
            accessor.memoryfix$setChunkWriter(null);
            accessor.memoryfix$setWorld(null);
            scrubChunk(accessor.memoryfix$getEmptyChunk());
            accessor.memoryfix$setEmptyChunk(null);
            return;
        }

        scrubChunkCollection(findFieldValueByName(chunkProvider, "chunks"));
        clearStateValue(findFieldValueByName(chunkProvider, "chunksToUnload"));
        clearStateValue(findFieldValueByName(chunkProvider, "chunkStorage"));
        clearStateValue(findFieldValueByName(chunkProvider, "chunks"));
        Object chunkGenerator = findFieldValueByName(chunkProvider, "chunkGenerator");
        if (chunkGenerator != null) {
            replaceFieldValue(chunkGenerator, "world", null);
            replaceFieldValue(chunkGenerator, "biomes", null);
            replaceFieldValue(chunkGenerator, "field_4861", null);
        }

        replaceFieldValue(chunkProvider, "chunkGenerator", null);
        replaceFieldValue(chunkProvider, "chunkWriter", null);
        replaceFieldValue(chunkProvider, "world", null);
        replaceFieldValue(chunkProvider, "empty", null);
    }

    private static void clearClientChunkProviderState(Object chunkProvider) {
        if (!(chunkProvider instanceof ClientChunkProvider)) {
            return;
        }

        ClientChunkProviderAccessor accessor = (ClientChunkProviderAccessor) chunkProvider;
        scrubChunkCollection(accessor.memoryfix$getChunks());
        clearStateValue(accessor.memoryfix$getChunkStorage());
        accessor.memoryfix$setChunks(new ArrayList());
        accessor.memoryfix$setWorld(null);
        scrubChunk(accessor.memoryfix$getEmptyChunk());
        accessor.memoryfix$setEmptyChunk(null);
    }

    private static void scrubChunkCollection(Object chunks) {
        if (!(chunks instanceof Collection)) {
            return;
        }

        for (Object chunk : (Collection<?>) chunks) {
            scrubChunk(chunk);
        }
    }

    private static void scrubChunk(Object chunk) {
        if (!(chunk instanceof Chunk)) {
            return;
        }

        ChunkAccessor accessor = (ChunkAccessor) chunk;
        accessor.memoryfix$setBlockEntities(new HashMap());
        accessor.memoryfix$setWorld(null);
        accessor.memoryfix$setLoaded(false);
        accessor.memoryfix$setModified(false);
        accessor.memoryfix$setContainsEntities(false);
        accessor.memoryfix$setChunkSections(new ChunkSection[16]);
        accessor.memoryfix$setEntities(new List[16]);
        accessor.memoryfix$setHeightmap(new int[256]);
        accessor.memoryfix$setSurfaceCache(new int[256]);
        accessor.memoryfix$setColumnSkyLightOutdated(new boolean[256]);
        accessor.memoryfix$setBiomeArray(new byte[256]);
    }

    private static void detachDimension(Object dimension) {
        if (dimension == null) {
            return;
        }

        Object biomeSource = findFieldValueByName(dimension, "biomeSource");
        if (biomeSource != null) {
            clearOwnedFieldState(biomeSource, "field_4715", "field_4667", "slots");
            replaceFieldValue(biomeSource, "field_4715", null);
            replaceFieldValue(biomeSource, "layer", null);
            replaceFieldValue(biomeSource, "field_4714", null);
            replaceFieldValue(biomeSource, "biomes", new ArrayList());
        }

        replaceFieldValue(dimension, "biomeSource", null);
        replaceFieldValue(dimension, "world", null);
        replaceFieldValue(dimension, "generatorOptions", null);
        replaceFieldValue(dimension, "generatorType", null);
    }

    private static void clearNamedState(Object target, String... fieldNames) {
        if (target == null || fieldNames == null || fieldNames.length == 0) {
            return;
        }

        for (Field field : getFields(target.getClass())) {
            if (Modifier.isStatic(field.getModifiers()) || !matchesFieldName(field.getName(), fieldNames)) {
                continue;
            }

            clearFieldValue(target, field);
        }
    }

    private static boolean matchesFieldName(String actualName, String[] expectedNames) {
        for (String expectedName : expectedNames) {
            if (expectedName.equals(actualName)) {
                return true;
            }
        }

        return false;
    }

    private static void clearFieldValue(Object owner, Field field) {
        field.setAccessible(true);

        try {
            clearStateValue(field.get(owner));
        } catch (IllegalAccessException ignored) {
        }
    }

    private static void replaceFieldValue(Object owner, String fieldName, Object value) {
        if (owner == null) {
            return;
        }

        Field field = findField(owner.getClass(), fieldName);
        if (field == null || Modifier.isFinal(field.getModifiers())) {
            return;
        }

        try {
            field.set(owner, value);
        } catch (IllegalAccessException ignored) {
        }
    }

    private static void clearPersistentStateManager(PersistentStateManager persistentStateManager) {
        if (persistentStateManager == null) {
            return;
        }

        PersistentStateManagerAccessor accessor = (PersistentStateManagerAccessor) persistentStateManager;
        safeClearMap(accessor.memoryfix$getStateMap());
        safeClearCollection(accessor.memoryfix$getStates());
        safeClearMap(accessor.memoryfix$getIdCounts());
    }

    private static void clearScoreboard(Scoreboard scoreboard) {
        if (scoreboard == null) {
            return;
        }

        ScoreboardAccessor accessor = (ScoreboardAccessor) scoreboard;
        safeClearMap(accessor.memoryfix$getObjectives());
        safeClearMap(accessor.memoryfix$getObjectivesByCriterion());
        safeClearMap(accessor.memoryfix$getPlayerObjectives());
        if (accessor.memoryfix$getObjectivesArray() != null) {
            Arrays.fill(accessor.memoryfix$getObjectivesArray(), null);
        }
        safeClearMap(accessor.memoryfix$getTeams());
        safeClearMap(accessor.memoryfix$getTeamsByPlayer());
    }

    private static void clearEntityTracker(EntityTracker entityTracker) {
        if (entityTracker == null) {
            return;
        }

        EntityTrackerAccessor accessor = (EntityTrackerAccessor) entityTracker;
        safeClearCollection(accessor.memoryfix$getTrackedEntities());
        accessor.memoryfix$setTrackedEntities(new HashSet());
        clearStateValue(accessor.memoryfix$getTrackedEntityIds());
        accessor.memoryfix$setTrackedEntityIds(new IntObjectStorage());
    }

    private static void clearPlayerWorldManager(PlayerWorldManager playerWorldManager) {
        if (playerWorldManager == null) {
            return;
        }

        PlayerWorldManagerAccessor accessor = (PlayerWorldManagerAccessor) playerWorldManager;
        safeClearCollection(accessor.memoryfix$getPlayers());
        clearStateValue(accessor.memoryfix$getPlayerInstancesById());
        safeClearCollection(accessor.memoryfix$getPendingPlayerInstances());
        safeClearCollection(accessor.memoryfix$getPlayerInstances());
    }

    private static void clearPortalTeleporter(PortalTeleporter portalTeleporter) {
        if (portalTeleporter == null) {
            return;
        }

        PortalTeleporterAccessor accessor = (PortalTeleporterAccessor) portalTeleporter;
        clearStateValue(accessor.memoryfix$getPortalCache());
        safeClearCollection(accessor.memoryfix$getPortalPositions());
    }

    private static Object invokeNoArg(Object owner, String methodName) {
        if (owner == null) {
            return null;
        }

        for (Class<?> current = owner.getClass(); current != null; current = current.getSuperclass()) {
            try {
                Method method = current.getDeclaredMethod(methodName);
                method.setAccessible(true);
                return method.invoke(owner);
            } catch (Throwable ignored) {
            }
        }

        return null;
    }

    private static void clearStateValue(Object value) {
        if (value == null) {
            return;
        }

        if (value instanceof Map) {
            safeClearMap((Map<?, ?>) value);
            return;
        }

        if (value instanceof List) {
            safeClearCollection((List<?>) value);
            return;
        }

        if (value instanceof Set) {
            safeClearCollection((Set<?>) value);
            return;
        }

        if (value instanceof Collection) {
            safeClearCollection((Collection<?>) value);
            return;
        }

        Class<?> type = value.getClass();
        if (type.isArray() && !type.getComponentType().isPrimitive() && value instanceof Object[]) {
            Arrays.fill((Object[]) value, null);
            return;
        }

        String className = type.getName();
        if ("net.minecraft.util.collection.IntObjectStorage".equals(className)) {
            clearIntObjectStorage(value);
            return;
        }

        if ("net.minecraft.util.collection.LongObjectStorage".equals(className)) {
            clearLongObjectStorage(value);
        }
    }

    private static void safeClearMap(Map<?, ?> map) {
        try {
            map.clear();
        } catch (Throwable ignored) {
        }
    }

    private static void safeClearCollection(Collection<?> collection) {
        try {
            collection.clear();
        } catch (Throwable ignored) {
        }
    }

    private static void clearIntObjectStorage(Object storage) {
        for (Field field : getFields(storage.getClass())) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);

            try {
                Object value = field.get(storage);
                if (value != null && value.getClass().isArray()) {
                    int length = Array.getLength(value);
                    for (int i = 0; i < length; i++) {
                        Array.set(value, i, null);
                    }
                } else if (value instanceof Set) {
                    safeClearCollection((Set<?>) value);
                } else if (field.getType() == int.class) {
                    field.setInt(storage, 0);
                }
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    private static void clearLongObjectStorage(Object storage) {
        for (Field field : getFields(storage.getClass())) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);

            try {
                Object value = field.get(storage);
                if (value != null && value.getClass().isArray()) {
                    int length = Array.getLength(value);
                    for (int i = 0; i < length; i++) {
                        Array.set(value, i, null);
                    }
                } else if (field.getType() == int.class) {
                    field.setInt(storage, 0);
                }
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    private static <T> T findFieldValue(Object owner, Class<T> type) {
        for (Field field : owner.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || !type.isAssignableFrom(field.getType())) {
                continue;
            }

            field.setAccessible(true);
            try {
                Object value = field.get(owner);
                if (type.isInstance(value)) {
                    return type.cast(value);
                }
            } catch (IllegalAccessException ignored) {
            }
        }

        return null;
    }

    private static Object findFieldValueByName(Object owner, String fieldName) {
        if (owner == null) {
            return null;
        }

        Field field = findField(owner.getClass(), fieldName);
        if (field == null) {
            return null;
        }

        try {
            return field.get(owner);
        } catch (IllegalAccessException ignored) {
            return null;
        }
    }

    private static Field findField(Class<?> owner, String name) {
        for (Class<?> current = owner; current != null; current = current.getSuperclass()) {
            try {
                Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
            }
        }

        return null;
    }

    private static List<Field> getFields(Class<?> owner) {
        List<Field> fields = new ArrayList<Field>();
        for (Class<?> current = owner; current != null; current = current.getSuperclass()) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
        }

        return fields;
    }

    private static Field findStaticField(Class<?> owner, Class<?> type) {
        for (Field field : owner.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || field.getType() != type) {
                continue;
            }

            field.setAccessible(true);
            return field;
        }

        return null;
    }

    private static List<Field> findStaticFields(Class<?> owner, Class<?> type) {
        List<Field> fields = new ArrayList<Field>();
        for (Field field : owner.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || field.getType() != type) {
                continue;
            }

            field.setAccessible(true);
            fields.add(field);
        }

        return fields;
    }
}
