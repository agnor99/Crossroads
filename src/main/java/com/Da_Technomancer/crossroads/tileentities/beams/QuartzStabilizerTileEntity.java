package com.Da_Technomancer.crossroads.tileentities.beams;

import com.Da_Technomancer.crossroads.API.IInfoTE;
import com.Da_Technomancer.crossroads.API.beams.BeamUnit;
import com.Da_Technomancer.crossroads.API.beams.BeamUnitStorage;
import com.Da_Technomancer.crossroads.API.templates.BeamRenderTE;
import com.Da_Technomancer.crossroads.blocks.CrossroadsBlocks;
import com.Da_Technomancer.essentials.blocks.EssentialsProperties;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class QuartzStabilizerTileEntity extends BeamRenderTE implements IInfoTE{

	private int setting = 0;
	private static final int CAPACITY = 1024;
	private static final int[] RATES = new int[] {1, 2, 4, 8, 16, 32, 64};
	private BeamUnitStorage storage = new BeamUnitStorage();

	private Direction dir = null;

	private Direction getDir(){
		if(dir == null){
			BlockState state = world.getBlockState(pos);
			if(state.getBlock() != CrossroadsBlocks.quartzStabilizer){
				return Direction.NORTH;
			}
			dir = state.get(EssentialsProperties.FACING);
		}
		return dir;
	}

	@Override
	public void resetBeamer(){
		super.resetBeamer();
		dir = null;
	}

	public int adjustSetting(){
		setting += 1;
		setting %= RATES.length;
		markDirty();
		return RATES[setting];
	}

	@Override
	public CompoundNBT writeToNBT(CompoundNBT nbt){
		super.writeToNBT(nbt);
		nbt.setInteger("setting", setting);
		storage.writeToNBT("stab_mag", nbt);
		return nbt;
	}

	@Override
	public void readFromNBT(CompoundNBT nbt){
		super.readFromNBT(nbt);
		setting = nbt.getInteger("setting");
		storage = BeamUnitStorage.readFromNBT("stab_mag", nbt);
	}

	@Override
	protected void doEmit(BeamUnit toEmit){
		if(toEmit != null){
			storage.addBeam(toEmit);

			//Prevent overfilling by removing excess beams
			if(storage.getPower() > CAPACITY){
				storage.subtractBeam(storage.getOutput().mult(1D - (double) CAPACITY / (double) storage.getPower(), false));
			}
		}

		Direction dir = getDir();

		if(!storage.isEmpty()){
			//As it would turn out, the problem of meeting a quota for the sum of values drawn from a limited source while also approximately maintaining the source ratio is quite messy when all values must be integers
			//This is about as clean an implementation as is possible
			int toFill = RATES[setting];
			BeamUnit toDraw;

			if(toFill < storage.getPower()){
				int[] output = storage.getOutput().mult(((double) toFill) / ((double) storage.getPower()), true).getValues();//Use the floor formula as a starting point
				int[] stored = storage.getOutput().getValues();
				int available = 0;

				for(int i = 0; i < 4; i++){
					stored[i] -= output[i];
					available += stored[i];
					toFill -= output[i];
				}

				toFill = Math.min(toFill, available);

				int source = 0;

				//Round-robin distribution of drawing additional power from storage to meet the quota
				//Ignoring the source element ratio, as toFill << RATES[storage] in most cases, making the effect on ratio minor
				for(int i = 0; i < toFill; i++){
					while(stored[source] == 0){
						source++;
					}
					output[source]++;
					stored[source]--;
					source++;
				}
				toDraw = new BeamUnit(output);
			}else{
				toDraw = storage.getOutput();
			}

			storage.subtractBeam(toDraw);

			if(beamer[dir.getIndex()].emit(toDraw, world)){
				refreshBeam(dir.getIndex());
			}
		}else if(beamer[dir.getIndex()].emit(null, world)){
			refreshBeam(dir.getIndex());
		}
	}

	@Override
	protected boolean[] inputSides(){
		boolean[] out = {true, true, true, true, true, true};
		out[getDir().getIndex()] = false;
		return out;
	}

	@Override
	protected boolean[] outputSides(){
		boolean[] out = new boolean[6];
		out[getDir().getIndex()] = true;
		return out;
	}

	@Override
	public void addInfo(ArrayList<String> chat, PlayerEntity player, @Nullable Direction side, BlockRayTraceResult hit){
		chat.add("Current maximum output: " + RATES[setting]);
		chat.add("Stored: " + (storage.getPower() == 0 ? "Empty" : storage.getOutput().toString()));
	}
}