package com.telepathicgrunt.loadnbtblock.blocks;

import com.telepathicgrunt.loadnbtblock.utils.StructureNbtDataFixer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StructureBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.server.SChunkDataPacket;
import net.minecraft.state.properties.StructureMode;
import net.minecraft.tileentity.StructureBlockTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.palette.PalettedContainer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadNbtBlock extends Block {

    public LoadNbtBlock() {
        super(Properties.of(Material.METAL, MaterialColor.COLOR_LIGHT_GRAY).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).noDrops());
    }

    // source: https://github.com/williambl/explosivessquared/blob/master/src/main/kotlin/com/williambl/explosivessquared/util/actions/MassBlockActionManager.kt
    @FunctionalInterface
    interface task<One, Two, Three> {
        void apply(One one, Two two, Three three);
    }
    private final Map<Long, Pair<Integer, task<Chunk, World, Integer>>> chunkJobs = new HashMap<>();

    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if(!(world instanceof ServerWorld) || hand == Hand.MAIN_HAND) return ActionResultType.PASS;

        String mainPath = Minecraft.getInstance().gameDirectory.getAbsoluteFile().toPath().getParent().getParent().toString();
        String resourcePath = mainPath+"\\src\\main\\resources\\data";

        player.displayClientMessage(new TranslationTextComponent(" Working.... "), true);

        // Finds and gets all identifiers for pieces
        List<File> files = new ArrayList<>();
        List<ResourceLocation> identifiers = new ArrayList<>();
        StructureNbtDataFixer.setAllNbtFilesToList(resourcePath, files);
        for(File file : files){
            String modifiedFileName = file.getAbsolutePath().replace(resourcePath+"\\","").replace("\\structures\\",":").replace(".nbt","").replace('\\','/');
            identifiers.add(new ResourceLocation(modifiedFileName));
        }

        // Size of area we will need
        int columnCount = 13;
        int rowCount = (int) Math.max(Math.ceil(identifiers.size()) / columnCount, 1);
        int spacing = 48;
        BlockPos bounds = new BlockPos(spacing * (rowCount+2), spacing, spacing * columnCount);

        BlockState structureVoid = Blocks.STRUCTURE_VOID.defaultBlockState();
        BlockState barrier = Blocks.BARRIER.defaultBlockState();
        short nonAir = 255;
        short zero = 0;

        // Fill/clear area with structure void
        BlockPos.Mutable mutableChunk = new BlockPos.Mutable().set(pos.getX() >> 4, pos.getY(), pos.getZ() >> 4);
        mutableChunk.move(1,0,0);
        int endChunkX = (pos.getX() + bounds.getX()) >> 4;
        int endChunkZ = (pos.getZ() + bounds.getZ()) >> 4;

        int maxChunks = (endChunkX - mutableChunk.getX()) * (endChunkZ - mutableChunk.getZ());
        int currentSection = 0;
        for(; mutableChunk.getX() < endChunkX; mutableChunk.move(1,0,0)) {
            for (; mutableChunk.getZ() < endChunkZ; mutableChunk.move(0, 0, 1)) {

                Chunk chunk = world.getChunk(mutableChunk.getX(), mutableChunk.getZ());
                ChunkSection[] sections = chunk.getSections();
                sections[1] = new ChunkSection(16, nonAir, zero, zero);
                sections[2] = new ChunkSection(32, nonAir, zero, zero);
                PalettedContainer<BlockState> bottomSection = sections[0].getStates();
                PalettedContainer<BlockState> middleSection = sections[1].getStates();
                PalettedContainer<BlockState> topSection = sections[2].getStates();
                for(int x = 0; x < 16; x++){
                    for(int z = 0; z < 16; z++){
                        for(int y = 4; y < 16; y++){
                            if(y == 4){
                                bottomSection.getAndSetUnchecked(x, y, z, barrier);
                            }
                            else{
                                bottomSection.getAndSetUnchecked(x, y, z, structureVoid);
                            }
                        }
                        for(int y = 0; y < 16; y++){
                            middleSection.getAndSetUnchecked(x, y, z, structureVoid);
                            topSection.getAndSetUnchecked(x, y, z, structureVoid);
                        }
                    }
                }

                currentSection++;
                chunk.markUnsaved();
                // Send changes to client to see
                ((ServerChunkProvider) world.getChunkSource()).chunkMap
                        .getPlayers(chunk.getPos(), false)
                        .forEach(s -> s.connection.send(new SChunkDataPacket(chunk, 65535)));
                player.displayClientMessage(new TranslationTextComponent("Working: %" +  Math.round(((float)currentSection / maxChunks) * 100f) / 100f), true);
            }
            mutableChunk.set(mutableChunk.getX(), mutableChunk.getY(), pos.getZ() >> 4); // Set back to start of row
        }

        generateStructurePieces(world, pos, player, identifiers, columnCount, spacing, mutableChunk);
        return ActionResultType.SUCCESS;
    }


    private void generateStructurePieces(World world, BlockPos pos, PlayerEntity player, List<ResourceLocation> identifiers, int columnCount, int spacing, BlockPos.Mutable mutableChunk) {
        mutableChunk.set(((pos.getX() >> 4) + 1) << 4, pos.getY(), (pos.getZ() >> 4) << 4);

        for(int pieceIndex = 1; pieceIndex <= identifiers.size(); pieceIndex++){
            player.displayClientMessage(new TranslationTextComponent(" Working making structure: "+ identifiers.get(pieceIndex-1)), true);

            world.setBlock(mutableChunk, Blocks.STRUCTURE_BLOCK.defaultBlockState().setValue(StructureBlock.MODE, StructureMode.LOAD), 3);
            TileEntity be = world.getBlockEntity(mutableChunk);
            if(be instanceof StructureBlockTileEntity){
                StructureBlockTileEntity structureBlockTileEntity = (StructureBlockTileEntity)be;
                structureBlockTileEntity.setStructureName(identifiers.get(pieceIndex-1)); // set identifier

                structureBlockTileEntity.setMode(StructureMode.LOAD);
                structureBlockTileEntity.loadStructure((ServerWorld) world,false); // load structure

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
}