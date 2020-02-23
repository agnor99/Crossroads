package com.Da_Technomancer.crossroads.tileentities.heat;

import com.Da_Technomancer.crossroads.API.CRProperties;
import com.Da_Technomancer.crossroads.API.Capabilities;
import com.Da_Technomancer.crossroads.API.heat.HeatInsulators;
import com.Da_Technomancer.crossroads.API.heat.HeatUtil;
import com.Da_Technomancer.crossroads.API.heat.IHeatHandler;
import com.Da_Technomancer.crossroads.API.templates.ModuleTE;
import com.Da_Technomancer.crossroads.CRConfig;
import com.Da_Technomancer.crossroads.Crossroads;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;
import java.util.ArrayList;

@ObjectHolder(Crossroads.MODID)
public class HeatCableTileEntity extends ModuleTE{

	@ObjectHolder("heat_cable")
	private static TileEntityType<HeatCableTileEntity> type = null;

	@SuppressWarnings("unchecked")//Darn Java, not being able to verify arrays of parameterized types. Bah Humbug!
	protected final LazyOptional<IHeatHandler>[] neighCache = new LazyOptional[] {LazyOptional.empty(), LazyOptional.empty(), LazyOptional.empty(), LazyOptional.empty(), LazyOptional.empty(), LazyOptional.empty()};
	protected HeatInsulators insulator;

	public HeatCableTileEntity(){
		this(HeatInsulators.WOOL);
	}

	public HeatCableTileEntity(HeatInsulators insulator){
		super(type);
		this.insulator = insulator;
	}

	protected HeatCableTileEntity(TileEntityType<? extends HeatCableTileEntity> type){
		super(type);
	}

	@Override
	public void updateContainingBlockInfo(){
		super.updateContainingBlockInfo();
		//When adjusting a side to lock, we need to invalidate the optional in case a side was disconnected
		System.out.println("SIDE: " + world.isRemote);
		heatOpt.invalidate();
		heatOpt = LazyOptional.of(this::createHeatHandler);
	}

	@Override
	protected boolean useHeat(){
		return true;
	}

	@Override
	protected HeatHandler createHeatHandler(){
		return new CableHeatHandler();
	}

	protected boolean locked(int side){
		return !getBlockState().get(CRProperties.CONDUIT_SIDES_BASE[side]).isConnection();
	}

	private void updateConnect(int side, boolean newVal){
		BlockState state = getBlockState();
		if(state.get(CRProperties.HAS_MATCH_SIDES[side]) != newVal){
			state = state.with(CRProperties.HAS_MATCH_SIDES[side], newVal);
			world.setBlockState(pos, state, 2);
			updateContainingBlockInfo();
		}
	}

	@Override
	public void tick(){
		super.tick();

		if(world.isRemote){
			return;
		}

		double prevTemp = temp;

		//Heat transfer
		ArrayList<IHeatHandler> heatHandlers = new ArrayList<>(6);
		for(Direction side : Direction.values()){
			if(locked(side.getIndex())){
				continue;
			}
			LazyOptional<IHeatHandler> otherOpt = neighCache[side.getIndex()];
			if(!neighCache[side.getIndex()].isPresent()){
				TileEntity te = world.getTileEntity(pos.offset(side));
				if(te != null){
					otherOpt = te.getCapability(Capabilities.HEAT_CAPABILITY, side.getOpposite());
					neighCache[side.getIndex()] = otherOpt;
				}
			}

			if(otherOpt.isPresent()){
				IHeatHandler handler = otherOpt.orElseThrow(NullPointerException::new);
				temp += handler.getTemp();
				handler.addHeat(-handler.getTemp());
				heatHandlers.add(handler);
				updateConnect(side.getIndex(), true);
			}else{
				updateConnect(side.getIndex(), false);
			}
		}

		temp /= heatHandlers.size() + 1;

		for(IHeatHandler handler : heatHandlers){
			handler.addHeat(temp);
		}

		//Energy loss
		double biomeTemp = HeatUtil.convertBiomeTemp(world, pos);
		temp += Math.min(insulator.getRate(), Math.abs(temp - biomeTemp)) * Math.signum(biomeTemp - temp);

		if(temp != prevTemp){
			markDirty();
		}

		if(temp > insulator.getLimit()){
			if(CRConfig.heatEffects.get()){
				insulator.getEffect().doEffect(world, pos);
			}else{
				world.setBlockState(pos, Blocks.FIRE.getDefaultState(), 3);
			}
		}
	}

	@Override
	public void read(CompoundNBT nbt){
		super.read(nbt);
		insulator = nbt.contains("insul") ? HeatInsulators.valueOf(nbt.getString("insul")) : HeatInsulators.WOOL;
	}

	@Override
	public CompoundNBT write(CompoundNBT nbt){
		super.write(nbt);
		nbt.putString("insul", insulator.name());
		return nbt;
	}

	@Override
	public CompoundNBT getUpdateTag(){
		return write(super.getUpdateTag());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing){
		if(capability == Capabilities.HEAT_CAPABILITY && (facing == null || !locked(facing.getIndex()))){
			return (LazyOptional<T>) heatOpt;
		}
		return super.getCapability(capability, facing);
	}

	private class CableHeatHandler extends HeatHandler{

		@Override
		public void init(){
			if(!initHeat){
				if(insulator == HeatInsulators.ICE){
					temp = -10;
				}else{
					temp = HeatUtil.convertBiomeTemp(world, pos);
				}
				initHeat = true;
				markDirty();
			}
		}
	}
}
