package com.Da_Technomancer.crossroads.gui.screen;

import com.Da_Technomancer.crossroads.API.templates.MachineGUI;
import com.Da_Technomancer.crossroads.Crossroads;
import com.Da_Technomancer.crossroads.gui.container.RadiatorContainer;
import com.Da_Technomancer.crossroads.tileentities.fluid.RadiatorTileEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class RadiatorScreen extends MachineGUI<RadiatorContainer, RadiatorTileEntity>{

	private static final ResourceLocation TEXTURE = new ResourceLocation(Crossroads.MODID, "textures/gui/container/radiator_gui.png");

	public RadiatorScreen(RadiatorContainer cont, Inventory playerInv, Component name){
		super(cont, playerInv, name);
	}

	@Override
	public void init(){
		super.init();
		initFluidManager(0, 10, 70);
		initFluidManager(1, 70, 70);
	}


	@Override
	protected void renderBg(PoseStack matrix, float partialTicks, int mouseX, int mouseY){
		RenderSystem.setShaderTexture(0, TEXTURE);

		blit(matrix, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		super.renderBg(matrix, partialTicks, mouseX, mouseY);
	}
}