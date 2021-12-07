package com.Da_Technomancer.crossroads.gui.container;

import com.Da_Technomancer.crossroads.API.templates.MachineContainer;
import com.Da_Technomancer.crossroads.Crossroads;
import com.Da_Technomancer.crossroads.tileentities.fluid.FatFeederTileEntity;
import com.Da_Technomancer.essentials.gui.container.FluidSlotManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.commons.lang3.tuple.Pair;

@ObjectHolder(Crossroads.MODID)
public class FatFeederContainer extends MachineContainer<FatFeederTileEntity>{

	@ObjectHolder("fat_feeder")
	private static MenuType<FatFeederContainer> type = null;

	public FatFeederContainer(int id, Inventory playerInv, FriendlyByteBuf buf){
		super(type, id, playerInv, buf);
	}

	@Override
	protected void addSlots(){
		Pair<Slot, Slot> flSlots = FluidSlotManager.createFluidSlots(new FluidSlotManager.FakeInventory(this), 0, 100,19, 100, 54, te, new int[] {0});
		addFluidManagerSlots(flSlots);
	}

	@Override
	protected int slotCount(){
		return 2;
	}
}
