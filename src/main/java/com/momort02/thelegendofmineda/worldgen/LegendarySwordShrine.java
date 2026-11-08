package com.momort02.thelegendofmineda.worldgen;

import com.momort02.thelegendofmineda.TheLegendOfMineda;
import com.momort02.thelegendofmineda.block.entity.SwordPedestalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public final class LegendarySwordShrine {
    private static final int MAX_SPAWN_DISTANCE = 1000;
    private static final int MIN_SPAWN_DISTANCE = 128;
    private static final int SYLVAN_FOREST_CHUNK_RADIUS = 2;
    private static final Identifier SHRINE_TEMPLATE_ID =
            Identifier.fromNamespaceAndPath(TheLegendOfMineda.MODID, "legendary_shrine");
    private static final ResourceKey<Biome> SYLVAN_FOREST = ResourceKey.create(
            Registries.BIOME, Identifier.fromNamespaceAndPath(TheLegendOfMineda.MODID, "sylvan_forest"));

    private LegendarySwordShrine() {
    }

    public static void generate(ServerLevel level, BlockPos worldSpawn) {
        StructureTemplate template = level.getStructureTemplateManager().get(SHRINE_TEMPLATE_ID)
                .orElseThrow(() -> new IllegalStateException("Missing shrine structure template " + SHRINE_TEMPLATE_ID));
        StructurePlaceSettings settings = new StructurePlaceSettings().setIgnoreEntities(true);
        var pedestalBlocks = template.filterBlocks(
                BlockPos.ZERO, new StructurePlaceSettings(), TheLegendOfMineda.SWORD_PEDESTAL.get(), false);
        if (pedestalBlocks.size() != 1) {
            throw new IllegalStateException("Shrine template must contain exactly one sword pedestal, found " + pedestalBlocks.size());
        }

        BlockPos pedestalOffset = pedestalBlocks.getFirst().pos();
        BlockPos pedestalPos = findCenter(level, worldSpawn, pedestalOffset, template.getSize().getY());
        BlockPos origin = pedestalPos.subtract(pedestalOffset);
        if (origin.getY() < level.getMinY() || origin.getY() + template.getSize().getY() > level.getMaxY()) {
            throw new IllegalStateException("Cannot place the legendary sword shrine at " + origin);
        }

        loadTemplateChunks(level, origin, template);
        applySylvanForest(level, pedestalPos);
        boolean placed = template.placeInWorld(
                level, origin, origin, settings, RandomSource.create(level.getSeed() ^ origin.asLong()), 3);
        if (!placed) {
            throw new IllegalStateException("Failed to place the legendary sword shrine structure at " + origin);
        }

        if (!(level.getBlockEntity(pedestalPos) instanceof SwordPedestalBlockEntity pedestal)) {
            throw new IllegalStateException("Sword pedestal block entity was not created at " + pedestalPos);
        }
        pedestal.setSword(TheLegendOfMineda.LEGENDARY_SWORD.get().getDefaultInstance());

        TheLegendOfMineda.LOGGER.info("Generated the legendary sword shrine at {}", pedestalPos);
    }

    private static void loadTemplateChunks(ServerLevel level, BlockPos origin, StructureTemplate template) {
        int minChunkX = origin.getX() >> 4;
        int minChunkZ = origin.getZ() >> 4;
        int maxChunkX = (origin.getX() + template.getSize().getX() - 1) >> 4;
        int maxChunkZ = (origin.getZ() + template.getSize().getZ() - 1) >> 4;
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                level.getChunk(chunkX, chunkZ);
            }
        }
    }

    private static void applySylvanForest(ServerLevel level, BlockPos center) {
        Holder<Biome> biome = level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(SYLVAN_FOREST);
        int centerChunkX = center.getX() >> 4;
        int centerChunkZ = center.getZ() >> 4;

        for (int xOffset = -SYLVAN_FOREST_CHUNK_RADIUS; xOffset <= SYLVAN_FOREST_CHUNK_RADIUS; xOffset++) {
            for (int zOffset = -SYLVAN_FOREST_CHUNK_RADIUS; zOffset <= SYLVAN_FOREST_CHUNK_RADIUS; zOffset++) {
                LevelChunk chunk = level.getChunk(centerChunkX + xOffset, centerChunkZ + zOffset);
                for (LevelChunkSection section : chunk.getSections()) {
                    section.fillBiomesFromNoise((quartX, quartY, quartZ) -> biome, 0, 0, 0);
                }
                chunk.markUnsaved();
            }
        }
    }

    private static BlockPos findCenter(ServerLevel level, BlockPos worldSpawn, BlockPos pedestalOffset, int templateHeight) {
        RandomSource random = RandomSource.create(level.getSeed() ^ worldSpawn.asLong() ^ 0x4C4547454E444152L);
        int minDistanceSquared = MIN_SPAWN_DISTANCE * MIN_SPAWN_DISTANCE;
        int maxDistanceSquared = MAX_SPAWN_DISTANCE * MAX_SPAWN_DISTANCE;

        BlockPos fallback = null;
        for (int attempt = 0; attempt < 32; attempt++) {
            int xOffset = random.nextInt(MAX_SPAWN_DISTANCE * 2 + 1) - MAX_SPAWN_DISTANCE;
            int zOffset = random.nextInt(MAX_SPAWN_DISTANCE * 2 + 1) - MAX_SPAWN_DISTANCE;
            int distanceSquared = xOffset * xOffset + zOffset * zOffset;
            if (distanceSquared < minDistanceSquared || distanceSquared > maxDistanceSquared) {
                continue;
            }

            int x = worldSpawn.getX() + xOffset;
            int z = worldSpawn.getZ() + zOffset;
            level.getChunk(x >> 4, z >> 4);
            int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            int pedestalY = surfaceY + pedestalOffset.getY();
            int pedestalX = x + pedestalOffset.getX();
            int pedestalZ = z + pedestalOffset.getZ();
            int shrineXOffset = pedestalX - worldSpawn.getX();
            int shrineZOffset = pedestalZ - worldSpawn.getZ();
            int shrineDistanceSquared = shrineXOffset * shrineXOffset + shrineZOffset * shrineZOffset;
            if (shrineDistanceSquared < minDistanceSquared
                    || shrineDistanceSquared > maxDistanceSquared
                    || surfaceY <= level.getMinY()
                    || surfaceY + templateHeight > level.getMaxY()
                    || pedestalY >= level.getMaxY()) {
                continue;
            }

            BlockPos candidate = new BlockPos(pedestalX, pedestalY, pedestalZ);
            if (fallback == null) {
                fallback = candidate;
            }
            if (level.getFluidState(new BlockPos(x, surfaceY - 1, z)).isEmpty()) {
                return candidate;
            }
        }

        if (fallback != null) {
            return fallback;
        }

        int spawnX = worldSpawn.getX();
        int spawnZ = worldSpawn.getZ();
        level.getChunk(spawnX >> 4, spawnZ >> 4);
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawnX, spawnZ);
        if (surfaceY <= level.getMinY() || surfaceY + pedestalOffset.getY() >= level.getMaxY()) {
            surfaceY = Math.max(level.getMinY() + 1, Math.min(worldSpawn.getY(), level.getMaxY() - pedestalOffset.getY() - 1));
        }
        TheLegendOfMineda.LOGGER.warn("No dry shrine location found within 1000 blocks of spawn; placing it at the world spawn");
        return new BlockPos(spawnX + pedestalOffset.getX(), surfaceY + pedestalOffset.getY(), spawnZ + pedestalOffset.getZ());
    }
}
