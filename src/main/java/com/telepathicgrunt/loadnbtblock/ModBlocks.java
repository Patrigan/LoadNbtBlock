package com.telepathicgrunt.loadnbtblock;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

import static com.telepathicgrunt.loadnbtblock.ModItems.registerBlockItem;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, LoadNbtBlock.MODID);

    public static final RegistryObject<Block> LOAD_NBT_BLOCK = registerBlock("load_nbt_block", com.telepathicgrunt.loadnbtblock.blocks.LoadNbtBlock::new);

    private static RegistryObject<Block> registerBlock(String id, Supplier<Block> sup) {
        RegistryObject<Block> blockRegistryObject = BLOCKS.register(id, sup);
        registerBlockItem(id, blockRegistryObject, blockSupplier -> new BlockItem(blockSupplier.get(), new Item.Properties().tab(CreativeModeTab.TAB_REDSTONE)));
        return blockRegistryObject;
    }
}
