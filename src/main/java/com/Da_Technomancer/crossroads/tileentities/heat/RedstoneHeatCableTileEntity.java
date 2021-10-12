package com.Da_Technomancer.crossroads.tileentities.heat;

import com.Da_Technomancer.crossroads.API.Capabilities;
import com.Da_Technomancer.crossroads.API.heat.HeatInsulators;
import com.Da_Technomancer.crossroads.CRConfig;
import com.Da_Technomancer.crossroads.Crossroads;
import com.Da_Technomancer.essentials.blocks.ESProperties;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;

@ObjectHolder(Crossroads.MODID)
public class RedstoneHeatCableTileEntity extends HeatCableTileEntity{

	@ObjectHolder("redstone_heat_cable")
	private static BlockEntityType<RedstoneHeatCableTileEntity> type = null;

	public RedstoneHeatCableTileEntity(BlockPos pos, BlockState state){
		this(HeatInsulators.WOOL);
	}

	public RedstoneHeatCableTileEntity(HeatInsulators insulator){
		super(type, pos, state);
		this.insulator = insulator;
	}

	private boolean isUnlocked(){
		return getBlockState().getValue(ESProperties.REDSTONE_BOOL);
	}

	@Override
	public void tick(){
		if(isUnlocked()){
			super.tick();
		}else{
			//Energy loss
			double prevTemp = temp;
			temp = runLoss();

			if(temp != prevTemp){
				setChanged();
			}

			if(temp > insulator.getLimit()){
				if(CRConfig.heatEffects.get()){
					insulator.getEffect().doEffect(level, worldPosition);
				}else{
					level.setBlock(worldPosition, Blocks.FIRE.defaultBlockState(), 3);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing){
		if(capability == Capabilities.HEAT_CAPABILITY){
			if((facing == null || !locked(facing.get3DDataValue())) && isUnlocked()){
				return (LazyOptional<T>) heatOpt;
			}else{
				return LazyOptional.empty();
			}
		}
		return super.getCapability(capability, facing);
	}

	public float getTemp(){
		return (float) temp;
	}
}
