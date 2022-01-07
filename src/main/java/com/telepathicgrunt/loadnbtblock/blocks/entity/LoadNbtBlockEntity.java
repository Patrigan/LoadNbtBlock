package com.telepathicgrunt.loadnbtblock.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StructureBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.telepathicgrunt.loadnbtblock.ModBlockEntityTypes.LOAD_NBT_BLOCK_ENTITY;
import static net.minecraftforge.registries.ForgeRegistries.BLOCKS;

public class LoadNbtBlockEntity extends BlockEntity implements MenuProvider {
    private ResourceLocation fillBlock = new ResourceLocation("minecraft:structure_void");
    private ResourceLocation floorBlock = new ResourceLocation("minecraft:barrier");
    private String modid = "";
    private String filter = "";

    public LoadNbtBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(LOAD_NBT_BLOCK_ENTITY.get(), blockPos, blockState);
    }

    public ResourceLocation getFillBlock() {
        return fillBlock;
    }

    public ResourceLocation getFloorBlock() {
        return floorBlock;
    }

    public String getModid() {
        return modid;
    }

    public String getFilter() {
        return filter;
    }

    public void setFillBlock(ResourceLocation fillBlock) {
        this.fillBlock = fillBlock;
    }

    public void setFloorBlock(ResourceLocation floorBlock) {
        this.floorBlock = floorBlock;
    }

    public void setModid(String modid) {
        this.modid = modid;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    // source: https://github.com/williambl/explosivessquared/blob/master/src/main/kotlin/com/williambl/explosivessquared/util/actions/MassBlockActionManager.kt
    @FunctionalInterface
    interface task<One, Two, Three> {
        void apply(One one, Two two, Three three);
    }
    private final Map<Long, Pair<Integer, LoadNbtBlockEntity.task<LevelChunk, Level, Integer>>> chunkJobs = new HashMap<>();

    public boolean handle(Level level, BlockPos pos, Player player) {
        if(!(level instanceof ServerLevel)) return false;

        List<ResourceLocation> identifiers = getResourceLocations(player, (ServerLevel) level, modid, filter);

        // Size of area we will need
        int columnCount = 13;
        int rowCount = (int) Math.max(Math.ceil(identifiers.size()) / columnCount, 1);
        int spacing = 48;
        BlockPos bounds = new BlockPos(spacing * (rowCount+2), spacing, spacing * columnCount);

        BlockState fillBlockState = BLOCKS.getValue(fillBlock).defaultBlockState();
        BlockState floorBlockState = BLOCKS.getValue(floorBlock).defaultBlockState();

        // Fill/clear area with structure void
        BlockPos.MutableBlockPos mutableChunk = clearAreaNew(level, pos, player, bounds, fillBlockState, floorBlockState);

        generateStructurePieces(level, pos, player, identifiers, columnCount, spacing, mutableChunk);
        return true;
    }

    private BlockPos.MutableBlockPos clearArea(Level world, BlockPos pos, Player player, BlockPos bounds, BlockState fillBlock, BlockState floorBlock) {
        BlockPos.MutableBlockPos mutableChunk = new BlockPos.MutableBlockPos().set(pos.getX() >> 4, pos.getY(), pos.getZ() >> 4);
        mutableChunk.move(1,0,0);
        int endChunkX = (pos.getX() + bounds.getX()) >> 4;
        int endChunkZ = (pos.getZ() + bounds.getZ()) >> 4;

        int maxChunks = (endChunkX - mutableChunk.getX()) * (endChunkZ - mutableChunk.getZ());
        int currentSection = 0;
        for(; mutableChunk.getX() < endChunkX; mutableChunk.move(1,0,0)) {
            for (; mutableChunk.getZ() < endChunkZ; mutableChunk.move(0, 0, 1)) {

                LevelChunk chunk = world.getChunk(mutableChunk.getX(), mutableChunk.getZ());
                LevelChunkSection[] sections = chunk.getSections();
                sections[1] = new LevelChunkSection(16, world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY));
                sections[2] = new LevelChunkSection(32, world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY));
                PalettedContainer<BlockState> bottomSection = sections[0].getStates();
                PalettedContainer<BlockState> middleSection = sections[1].getStates();
                PalettedContainer<BlockState> topSection = sections[2].getStates();
                for(int x = 0; x < 16; x++){
                    for(int z = 0; z < 16; z++){
                        for(int y = 4; y < 16; y++){
                            if(y == 4){
                                bottomSection.getAndSetUnchecked(x, y, z, floorBlock);
                            }
                            else{
                                bottomSection.getAndSetUnchecked(x, y, z, fillBlock);
                            }
                        }
                        for(int y = 0; y < 16; y++){
                            middleSection.getAndSetUnchecked(x, y, z, fillBlock);
                            topSection.getAndSetUnchecked(x, y, z, fillBlock);
                        }
                    }
                }

                currentSection++;
                chunk.setUnsaved(true);
                // Send changes to client to see
                ClientboundLevelChunkWithLightPacket packet = new ClientboundLevelChunkWithLightPacket(chunk, world.getLightEngine(), null, null, true);
                ((ServerChunkCache) world.getChunkSource()).chunkMap
                        .getPlayers(chunk.getPos(), false)
                        .forEach(s -> s.trackChunk(chunk.getPos(), packet));
                player.displayClientMessage(new TranslatableComponent("Working: %" +  Math.round(((float)currentSection / maxChunks) * 100f)), true);
            }
            mutableChunk.set(mutableChunk.getX(), mutableChunk.getY(), pos.getZ() >> 4); // Set back to start of row
        }
        return mutableChunk;
    }
    private BlockPos.MutableBlockPos clearAreaNew(Level world, BlockPos pos, Player player, BlockPos bounds, BlockState fillBlock, BlockState floorBlock) {
        BlockPos.MutableBlockPos mutableChunk = new BlockPos.MutableBlockPos().set(pos.getX() >> 4, pos.getY(), pos.getZ() >> 4);
        mutableChunk.move(1,0,0);
        int endChunkX = (pos.getX() + bounds.getX()) >> 4;
        int endChunkZ = (pos.getZ() + bounds.getZ()) >> 4;

        int maxChunks = (endChunkX - mutableChunk.getX()) * (endChunkZ - mutableChunk.getZ());
        int currentSection = 0;
        for(; mutableChunk.getX() < endChunkX; mutableChunk.move(1,0,0)) {
            for (; mutableChunk.getZ() < endChunkZ; mutableChunk.move(0, 0, 1)) {
//                LevelChunk chunk = world.getChunk(mutableChunk.getX(), mutableChunk.getZ());
                BlockPos.MutableBlockPos mutable = new BlockPos(mutableChunk.getX() << 4, pos.getY(), mutableChunk.getZ() << 4).mutable();
                mutable.move(-1, 0, 0);
                for(int x = 0; x < 16; x++){
                    mutable.setZ(mutableChunk.getZ() << 4);
                    mutable.move(1, 0, -1);
                    for(int z = 0; z < 16; z++){
                        mutable.move(0, 0, 1);
                        mutable.setY(pos.getY());
                        world.setBlockAndUpdate(mutable, floorBlock);
                        for(int y = pos.getY()+1; y < pos.getY()+64; y++){
                            mutable.setY(y);
                            world.setBlockAndUpdate(mutable, fillBlock);
                        }
                    }
                }
                currentSection++;
                player.displayClientMessage(new TranslatableComponent("Working: %" +  Math.round(((float)currentSection / maxChunks) * 100f)), true);
            }
            mutableChunk.set(mutableChunk.getX(), mutableChunk.getY(), pos.getZ() >> 4); // Set back to start of row
        }
        return mutableChunk;
    }



    private List<ResourceLocation> getResourceLocations(Player player, ServerLevel world, String modId, String filter) {
        player.displayClientMessage(new TranslatableComponent(" Working.... "), true);
        ResourceManager resourceManager = world.getServer().getResourceManager();
        return resourceManager.listResources("structures", (filename) -> filename.endsWith(".nbt"))
                .stream()
                .filter(resourceLocation -> resourceLocation.getNamespace().equals(modId))
                .filter(resourceLocation -> resourceLocation.getPath().contains(filter))
                .map(resourceLocation -> new ResourceLocation(resourceLocation.getNamespace(), resourceLocation.getPath().replaceAll("structures/", "").replaceAll(".nbt", "")))
                .toList();
    }


    private void generateStructurePieces(Level world, BlockPos pos, Player player, List<ResourceLocation> identifiers, int columnCount, int spacing, BlockPos.MutableBlockPos mutableChunk) {
        mutableChunk.set(((pos.getX() >> 4) + 1) << 4, pos.getY(), (pos.getZ() >> 4) << 4);

        for(int pieceIndex = 1; pieceIndex <= identifiers.size(); pieceIndex++){
            player.displayClientMessage(new TranslatableComponent(" Working making structure: "+ identifiers.get(pieceIndex-1)), true);

            world.setBlock(mutableChunk, Blocks.STRUCTURE_BLOCK.defaultBlockState().setValue(StructureBlock.MODE, StructureMode.LOAD), 3);
            BlockEntity be = world.getBlockEntity(mutableChunk);
            if(be instanceof StructureBlockEntity){
                StructureBlockEntity structureBlockTileEntity = (StructureBlockEntity)be;
                structureBlockTileEntity.setStructureName(identifiers.get(pieceIndex-1)); // set identifier

                structureBlockTileEntity.setMode(StructureMode.LOAD);
                structureBlockTileEntity.setIgnoreEntities(false);
                structureBlockTileEntity.loadStructure((ServerLevel) world,false); // load structure

                structureBlockTileEntity.setMode(StructureMode.SAVE);
                //structureBlockTileEntity.saveStructure(true); //save structure
                //structureBlockTileEntity.setShowAir(true);
                structureBlockTileEntity.setIgnoreEntities(false);
            }

            mutableChunk.move(0,0, spacing);


            // Move back to start of row
            if(pieceIndex % columnCount == 0){
                mutableChunk.move(spacing,0, (-spacing * columnCount));
            }
        }
    }

    @Override
    public Component getDisplayName() {
        return null;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int p_39954_, Inventory p_39955_, Player p_39956_) {
        return null;
    }
}
