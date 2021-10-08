package com.Da_Technomancer.crossroads.gui.screen;

import com.Da_Technomancer.crossroads.API.templates.MachineGUI;
import com.Da_Technomancer.crossroads.Crossroads;
import com.Da_Technomancer.crossroads.gui.container.SteamBoilerContainer;
import com.Da_Technomancer.crossroads.tileentities.fluid.SteamBoilerTileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public class SteamBoilerScreen extends MachineGUI<SteamBoilerContainer, SteamBoilerTileEntity>{

	private static final ResourceLocation TEXTURE = new ResourceLocation(Crossroads.MODID, "textures/gui/container/steam_boiler_gui.png");

	public SteamBoilerScreen(SteamBoilerContainer cont, Inventory playerInv, Component name){
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
		Minecraft.getInstance().getTextureManager().bind(TEXTURE);

		blit(matrix, leftPos, topPos, 0, 0, imageWidth, imageHeight);


		super.renderBg(matrix, partialTicks, mouseX, mouseY);
	}
}