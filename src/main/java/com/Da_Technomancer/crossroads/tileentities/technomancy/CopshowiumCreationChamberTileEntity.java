package com.Da_Technomancer.crossroads.tileentities.technomancy;

import com.Da_Technomancer.crossroads.API.Capabilities;
import com.Da_Technomancer.crossroads.API.EnergyConverters;
import com.Da_Technomancer.crossroads.API.beams.BeamUnit;
import com.Da_Technomancer.crossroads.API.beams.EnumBeamAlignments;
import com.Da_Technomancer.crossroads.API.beams.IBeamHandler;
import com.Da_Technomancer.crossroads.API.technomancy.FluxUtil;
import com.Da_Technomancer.crossroads.API.templates.InventoryTE;
import com.Da_Technomancer.crossroads.CRConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class CopshowiumCreationChamberTileEntity extends InventoryTE{

	/**
	 * The number of mB of molten copshowium produced from 1mb of molten copper OR 1mb of distilled water.
	 * Based on balance and convenience.
	 */
	private static final double COPSHOWIUM_PER_COPPER = 2D;
	public static final int CAPACITY = 1_440;
	public static final int FLUX_INGOT = 4;


	public CopshowiumCreationChamberTileEntity(){
		super(0);
		fluidProps[0] = new TankProperty(CAPACITY, true, true, (Fluid f) -> f != null && (f.getName().equals(CRConfig.cccExpenLiquid.get()) || f.getName().equals(CRConfig.cccEntropLiquid.get())));//Input
		fluidProps[1] = new TankProperty(CAPACITY, false, true);//Copshowium
	}

	@Override
	public void addInfo(ArrayList<String> chat, PlayerEntity player, @Nullable Direction side, BlockRayTraceResult hit){
		chat.add("Temporal Entropy: " + EntropySavedData.getEntropy(world) + "%");
		super.addInfo(chat, player, side, hitX, hitY, hitZ);
	}

	@Override
	protected int fluidTanks(){
		return 2;
	}

	private final FluidHandler inputHandler = new FluidHandler(0);
	private final FluidHandler outputHandler = new FluidHandler(1);
	private final FluidHandler internalHandler = new FluidHandler(-1);
	private final IBeamHandler magicHandler = new BeamHandler();

	@SuppressWarnings("unchecked")
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing){
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY){
			return facing == null ? (T) internalHandler : facing == Direction.UP ? (T) inputHandler : facing == Direction.DOWN ? (T) outputHandler : null;
		}

		if(capability == Capabilities.BEAM_CAPABILITY && (facing == null || facing.getAxis() != Direction.Axis.Y)){
			return (T) magicHandler;
		}

		return super.getCapability(capability, facing);
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, Direction direction){
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack){
		return false;
	}

	@Override
	public String getName(){
		return "container.copshowium_maker";
	}

	private class BeamHandler implements IBeamHandler{

		@Override
		public void setMagic(BeamUnit mag){
			EnumBeamAlignments align = EnumBeamAlignments.getAlignment(mag);
			if(align == EnumBeamAlignments.TIME && fluids[0] != null){
				if(EntropySavedData.getSeverity(world).getRank() >= EntropySavedData.Severity.DESTRUCTIVE.getRank()){
					FluxUtil.overloadFlux(world, pos);
					return;
				}


				if(fluids[0].getFluid().getName().equals(CRConfig.cccExpenLiquid.get())){
					fluids[1] = new FluidStack(BlockMoltenCopshowium.getMoltenCopshowium(), (int) (((double) fluids[0].amount) * COPSHOWIUM_PER_COPPER) + (fluids[1] == null ? 0 : fluids[1].amount));
					fluids[0] = null;
					markDirty();
					if(fluids[1].amount > CAPACITY){
						world.setBlockState(pos, BlockMoltenCopshowium.getMoltenCopshowium().getBlock().getDefaultState());
					}
				}else if(fluids[0].getFluid().getName().equals(CRConfig.cccEntropLiquid.get()) && EntropySavedData.getSeverity(world).getRank() >= EntropySavedData.Severity.UNSTABLE.getRank()){
					int created = (int) (((double) fluids[0].amount) * COPSHOWIUM_PER_COPPER);
					fluids[1] = new FluidStack(BlockMoltenCopshowium.getMoltenCopshowium(), created + (fluids[1] == null ? 0 : fluids[1].amount));
					fluids[0] = null;
					EntropySavedData.addEntropy(world, FLUX_INGOT * created / EnergyConverters.INGOT_MB);
					markDirty();
					if(fluids[1].amount > CAPACITY){
						world.setBlockState(pos, BlockMoltenCopshowium.getMoltenCopshowium().getBlock().getDefaultState());
					}
				}
			}else if(align == EnumBeamAlignments.VOID){
				//A void beam destroys all stored liquid
				fluids[0] = null;
				fluids[1] = null;
				markDirty();
			}
		}
	}
}