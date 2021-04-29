package com.telepathicgrunt.loadnbtblock;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

import static com.telepathicgrunt.loadnbtblock.ModItems.registerBlockItem;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, LoadNbtBlock.MODID);

    public static final RegistryObject<Block> LOAD_NBT_BLOCK = registerBlock("load_nbt_block", com.telepathicgrunt.loadnbtblock.blocks.LoadNbtBlock::new);

    private static RegistryObject<Block> registerBlock(String id, Supplier<Block> sup) {
        RegistryObject<Block> blockRegistryObject = BLOCKS.register(id, sup);
        registerBlockItem(id, blockRegistryObject, blockSupplier -> new BlockItem(blockSupplier.get(), new Item.Properties().tab(ItemGroup.TAB_REDSTONE)));
        return blockRegistryObject;
    }
}
