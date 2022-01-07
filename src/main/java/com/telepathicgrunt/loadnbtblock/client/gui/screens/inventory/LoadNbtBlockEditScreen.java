package com.telepathicgrunt.loadnbtblock.client.gui.screens.inventory;

import com.mojang.blaze3d.vertex.PoseStack;
import com.telepathicgrunt.loadnbtblock.blocks.entity.LoadNbtBlockEntity;
import com.telepathicgrunt.loadnbtblock.client.packet.LoadNbtBlockPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundSetJigsawBlockPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.network.PacketDistributor;

import static com.telepathicgrunt.loadnbtblock.LoadNbtBlock.CHANNEL;

@OnlyIn(Dist.CLIENT)
public class LoadNbtBlockEditScreen extends Screen {
   private static final int MAX_LEVELS = 7;
   private static final Component FILL_BLOCK_LABEL = new TranslatableComponent("loadnbtblock.fill_label");
   private static final Component FLOOR_BLOCK_LABEL = new TranslatableComponent("loadnbtblock.floor_label");
   private static final Component MODID_LABEL = new TranslatableComponent("loadnbtblock.modid_label");
   private static final Component FILTER_LABEL = new TranslatableComponent("loadnbtblock.filter_label");
   private final LoadNbtBlockEntity loadNbtBlockEntity;
   private EditBox fillEdit;
   private EditBox floorEdit;
   private EditBox modidEdit;
   private EditBox filterEdit;
   private Button doneButton;

   public LoadNbtBlockEditScreen(LoadNbtBlockEntity p_98949_) {
      super(NarratorChatListener.NO_TITLE);
      this.loadNbtBlockEntity = p_98949_;
   }

   public void tick() {
      this.fillEdit.tick();
      this.floorEdit.tick();
      this.modidEdit.tick();
      this.filterEdit.tick();
   }

   private void onDone() {
      this.sendToServer();
      this.minecraft.setScreen((Screen)null);
   }

   private void onCancel() {
      this.minecraft.setScreen((Screen)null);
   }

   private void sendToServer() {
      CHANNEL.send(PacketDistributor.SERVER.noArg(), new LoadNbtBlockPacket(this.loadNbtBlockEntity.getBlockPos(), new ResourceLocation(this.fillEdit.getValue()), new ResourceLocation(this.floorEdit.getValue()), this.modidEdit.getValue(), this.filterEdit.getValue()));
   }

   @Override
   public void onClose() {
      this.onCancel();
   }

   protected void init() {
      this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
      this.fillEdit = new EditBox(this.font, this.width / 2 - 152, 20, 300, 20, new TranslatableComponent("loadnbtblock.fill"));
      this.fillEdit.setMaxLength(128);
      this.fillEdit.setValue(this.loadNbtBlockEntity.getFillBlock().toString());
      this.fillEdit.setResponder((p_98981_) -> {
         this.updateValidity();
      });
      this.addWidget(this.fillEdit);
      this.floorEdit = new EditBox(this.font, this.width / 2 - 152, 55, 300, 20, new TranslatableComponent("loadnbtblock.floor"));
      this.floorEdit.setMaxLength(128);
      this.floorEdit.setValue(this.loadNbtBlockEntity.getFloorBlock().toString());
      this.floorEdit.setResponder((p_98986_) -> {
         this.updateValidity();
      });
      this.addWidget(this.floorEdit);
      this.modidEdit = new EditBox(this.font, this.width / 2 - 152, 90, 300, 20, new TranslatableComponent("loadnbtblock.modid"));
      this.modidEdit.setMaxLength(128);
      this.modidEdit.setValue(this.loadNbtBlockEntity.getModid());
      this.modidEdit.setResponder((p_98977_) -> {
         this.updateValidity();
      });
      this.addWidget(this.modidEdit);
      this.filterEdit = new EditBox(this.font, this.width / 2 - 152, 125, 300, 20, new TranslatableComponent("loadnbtblock.filter"));
      this.filterEdit.setMaxLength(128);
      this.filterEdit.setValue(this.loadNbtBlockEntity.getFilter());
      this.filterEdit.setResponder((p_98977_) -> {
         this.updateValidity();
      });
      this.addWidget(this.filterEdit);
      this.doneButton = this.addRenderableWidget(new Button(this.width / 2 - 4 - 150, 210, 150, 20, CommonComponents.GUI_DONE, (p_98973_) -> {
         this.onDone();
      }));
      this.addRenderableWidget(new Button(this.width / 2 + 4, 210, 150, 20, CommonComponents.GUI_CANCEL, (p_98964_) -> {
         this.onCancel();
      }));
      this.setInitialFocus(this.modidEdit);
      this.updateValidity();
   }

   private void updateValidity() {
      boolean flag = ResourceLocation.isValidResourceLocation(this.fillEdit.getValue()) && ResourceLocation.isValidResourceLocation(this.floorEdit.getValue()) && ModList.get().isLoaded(this.modidEdit.getValue());
      this.doneButton.active = flag;
   }

   public void resize(Minecraft p_98960_, int p_98961_, int p_98962_) {
      String s = this.fillEdit.getValue();
      String s1 = this.floorEdit.getValue();
      String s2 = this.modidEdit.getValue();
      String s3 = this.filterEdit.getValue();
      this.init(p_98960_, p_98961_, p_98962_);
      this.fillEdit.setValue(s);
      this.floorEdit.setValue(s1);
      this.modidEdit.setValue(s2);
      this.filterEdit.setValue(s3);
   }

   public void removed() {
      this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
   }

   public boolean keyPressed(int p_98951_, int p_98952_, int p_98953_) {
      if (super.keyPressed(p_98951_, p_98952_, p_98953_)) {
         return true;
      } else if (!this.doneButton.active || p_98951_ != 257 && p_98951_ != 335) {
         return false;
      } else {
         this.onDone();
         return true;
      }
   }

   public void render(PoseStack p_98955_, int p_98956_, int p_98957_, float p_98958_) {
      this.renderBackground(p_98955_);
      drawString(p_98955_, this.font, FILL_BLOCK_LABEL, this.width / 2 - 153, 10, 10526880);
      this.fillEdit.render(p_98955_, p_98956_, p_98957_, p_98958_);
      drawString(p_98955_, this.font, FLOOR_BLOCK_LABEL, this.width / 2 - 153, 45, 10526880);
      this.floorEdit.render(p_98955_, p_98956_, p_98957_, p_98958_);
      drawString(p_98955_, this.font, MODID_LABEL, this.width / 2 - 153, 80, 10526880);
      this.modidEdit.render(p_98955_, p_98956_, p_98957_, p_98958_);
      drawString(p_98955_, this.font, FILTER_LABEL, this.width / 2 - 153, 115, 10526880);
      this.filterEdit.render(p_98955_, p_98956_, p_98957_, p_98958_);
      super.render(p_98955_, p_98956_, p_98957_, p_98958_);
   }
}