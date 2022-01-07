package com.telepathicgrunt.loadnbtblock.client.packet;

import com.telepathicgrunt.loadnbtblock.blocks.entity.LoadNbtBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class LoadNbtBlockPacket {
    private final BlockPos pos;
    private final ResourceLocation fillBlock;
    private final ResourceLocation floorBlock;
    private final String modid;
    private final String filter;

    public LoadNbtBlockPacket(BlockPos pos, ResourceLocation fillBlock, ResourceLocation floorBlock, String modid, String filter) {
        this.pos = pos;
        this.fillBlock = fillBlock;
        this.floorBlock = floorBlock;
        this.modid = modid;
        this.filter = filter;
    }

    public static LoadNbtBlockPacket decode(FriendlyByteBuf byteBuffer) {
        return new LoadNbtBlockPacket(byteBuffer.readBlockPos(), byteBuffer.readResourceLocation(), byteBuffer.readResourceLocation(), byteBuffer.readUtf(), byteBuffer.readUtf());
    }

    public void encode(FriendlyByteBuf byteBuffer) {
        byteBuffer.writeBlockPos(this.pos);
        byteBuffer.writeResourceLocation(this.fillBlock);
        byteBuffer.writeResourceLocation(this.floorBlock);
        byteBuffer.writeUtf(this.modid);
        byteBuffer.writeUtf(this.filter);
    }

    public void onPacketReceived(Supplier<NetworkEvent.Context> contextGetter) {
        NetworkEvent.Context context = contextGetter.get();
        context.enqueueWork(() -> this.handlePacketOnMainThread(context.getSender()));
        context.setPacketHandled(true);
    }

    public void handlePacketOnMainThread(ServerPlayer player) {
        if (player.canUseGameMasterBlocks()) {
            BlockPos blockpos = this.getPos();
            BlockState blockstate = player.level.getBlockState(blockpos);
            BlockEntity blockentity = player.level.getBlockEntity(blockpos);
            if (blockentity instanceof LoadNbtBlockEntity loadNbtBlockEntity) {
                loadNbtBlockEntity.setFillBlock(this.getFillBlock());
                loadNbtBlockEntity.setFloorBlock(this.getFloorBlock());
                loadNbtBlockEntity.setModid(this.getModid());
                loadNbtBlockEntity.setFilter(this.getFilter());
                loadNbtBlockEntity.setChanged();
                player.level.sendBlockUpdated(blockpos, blockstate, blockstate, 3);
                loadNbtBlockEntity.handle(player.level, blockpos, player);
            }
        }
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public ResourceLocation getFillBlock() {
        return this.fillBlock;
    }

    public ResourceLocation getFloorBlock() {
        return this.floorBlock;
    }

    public String getModid() {
        return this.modid;
    }

    public String getFilter() {
        return this.filter;
    }

}
