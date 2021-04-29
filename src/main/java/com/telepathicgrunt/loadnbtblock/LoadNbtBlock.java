package com.telepathicgrunt.loadnbtblock;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(LoadNbtBlock.MODID)
public class LoadNbtBlock {
    public static final String MODID = "loadnbtblock";
    public static final Logger LOGGER = LogManager.getLogger();


    public LoadNbtBlock() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
    }

}
