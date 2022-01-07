package com.telepathicgrunt.loadnbtblock;

import com.telepathicgrunt.loadnbtblock.blocks.entity.LoadNbtBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.telepathicgrunt.loadnbtblock.ModBlocks.LOAD_NBT_BLOCK;


public class ModBlockEntityTypes {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, LoadNbtBlock.MODID);

    public static final RegistryObject<BlockEntityType<LoadNbtBlockEntity>> LOAD_NBT_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("load_nbt_block",
            () -> BlockEntityType.Builder.of(LoadNbtBlockEntity::new, LOAD_NBT_BLOCK.get()).build(null));

}
