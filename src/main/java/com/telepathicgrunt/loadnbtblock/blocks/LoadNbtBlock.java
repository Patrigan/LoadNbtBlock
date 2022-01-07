package com.telepathicgrunt.loadnbtblock.blocks;

import com.telepathicgrunt.loadnbtblock.blocks.entity.LoadNbtBlockEntity;
import com.telepathicgrunt.loadnbtblock.client.gui.screens.inventory.LoadNbtBlockEditScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;

public class LoadNbtBlock extends BaseEntityBlock implements GameMasterBlock {

    public LoadNbtBlock() {
        super(Properties.of(Material.METAL, MaterialColor.COLOR_LIGHT_GRAY).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).noDrops());
    }

    public RenderShape getRenderShape(BlockState p_49653_) {
        return RenderShape.MODEL;
    }

    public BlockEntity newBlockEntity(BlockPos p_153448_, BlockState p_153449_) {
        return new LoadNbtBlockEntity(p_153448_, p_153449_);
    }

    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        BlockEntity blockentity = level.getBlockEntity(blockPos);
        if (blockentity instanceof LoadNbtBlockEntity && player.canUseGameMasterBlocks()) {
            if(level.isClientSide && player instanceof LocalPlayer) {
                Minecraft.getInstance().setScreen(new LoadNbtBlockEditScreen((LoadNbtBlockEntity) blockentity));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else {
            return InteractionResult.PASS;
        }

    }

}