package com.Da_Technomancer.crossroads.gui.container;

import com.Da_Technomancer.crossroads.API.templates.MachineContainer;
import com.Da_Technomancer.crossroads.Crossroads;
import com.Da_Technomancer.crossroads.tileentities.witchcraft.FormulationVatTileEntity;
import com.Da_Technomancer.essentials.gui.container.FluidSlotManager;
import com.Da_Technomancer.essentials.gui.container.IntDeferredRef;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.commons.lang3.tuple.Pair;

@ObjectHolder(Crossroads.MODID)
public class FormulationVatContainer extends MachineContainer<FormulationVatTileEntity>{

	@ObjectHolder("formulation_vat")
	private static MenuType<FormulationVatContainer> type = null;

	public final IntDeferredRef craftProgress;

	public FormulationVatContainer(int id, Inventory playerInv, FriendlyByteBuf buf){
		super(type, id, playerInv, buf);
		craftProgress = new IntDeferredRef(te::getProgess, te.getLevel().isClientSide);
		addDataSlot(craftProgress);
	}

	@Override
	protected void addSlots(){
		addSlot(new StrictSlot(te, 0, 44, 50));//Input
		Pair<Slot, Slot> fSlots = FluidSlotManager.createFluidSlots(new FluidSlotManager.FakeInventory(this), 0, 116, 15, 116, 50, te, new int[] {0, 1});
		addFluidManagerSlots(fSlots);
	}

	@Override
	protected int slotCount(){
		return 3;
	}
}
