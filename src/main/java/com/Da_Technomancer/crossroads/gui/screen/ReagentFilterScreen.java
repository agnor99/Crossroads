package com.Da_Technomancer.crossroads.gui.screen;

import com.Da_Technomancer.crossroads.API.templates.TileEntityGUI;
import com.Da_Technomancer.crossroads.Crossroads;
import com.Da_Technomancer.crossroads.gui.container.ReagentFilterContainer;
import com.Da_Technomancer.crossroads.tileentities.alchemy.ReagentFilterTileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public class ReagentFilterScreen extends TileEntityGUI<ReagentFilterContainer, ReagentFilterTileEntity>{

	private static final ResourceLocation GUI_TEXTURES = new ResourceLocation(Crossroads.MODID, "textures/gui/container/reagent_filter_gui.png");

	public ReagentFilterScreen(ReagentFilterContainer cont, Inventory playerInv, Component name){
		super(cont, playerInv, name);
	}

	@Override
	protected void renderBg(PoseStack matrix, float partialTicks, int mouseX, int mouseY){
		super.renderBg(matrix, partialTicks, mouseX, mouseY);

		RenderSystem.color4f(1, 1, 1, 1);
		Minecraft.getInstance().getTextureManager().bind(GUI_TEXTURES);

		blit(matrix, leftPos, topPos, 0, 0, imageWidth, imageHeight);
	}
}