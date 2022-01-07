package com.telepathicgrunt.loadnbtblock;

import com.telepathicgrunt.loadnbtblock.client.packet.LoadNbtBlockPacket;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.telepathicgrunt.loadnbtblock.ModBlocks.LOAD_NBT_BLOCK;

@Mod(LoadNbtBlock.MODID)
public class LoadNbtBlock {
    public static final String MODID = "loadnbtblock";
    public static final Logger LOGGER = LogManager.getLogger();

    private static final String CHANNEL_PROTOCOL = "0";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MODID, "main"),
            () -> CHANNEL_PROTOCOL,
            CHANNEL_PROTOCOL::equals,
            CHANNEL_PROTOCOL::equals);


    public LoadNbtBlock() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::onClientSetupEvent);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntityTypes.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);

        this.registerPackets();
    }

    public void onClientSetupEvent(FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(LOAD_NBT_BLOCK.get(), RenderType.solid());
    }

    void registerPackets()
    {
        int id = 0;
        CHANNEL.registerMessage(id++, LoadNbtBlockPacket.class,
                LoadNbtBlockPacket::encode,
                LoadNbtBlockPacket::decode,
                LoadNbtBlockPacket::onPacketReceived);
    }

}
