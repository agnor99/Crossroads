package com.Da_Technomancer.crossroads.tileentities.technomancy;

import com.Da_Technomancer.crossroads.API.CircuitUtil;
import com.Da_Technomancer.crossroads.Crossroads;
import com.Da_Technomancer.crossroads.gui.container.SequenceBoxContainer;
import com.Da_Technomancer.essentials.blocks.redstone.IRedstoneHandler;
import com.Da_Technomancer.essentials.blocks.redstone.RedstoneUtil;
import com.Da_Technomancer.essentials.packets.INBTReceiver;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;

@ObjectHolder(Crossroads.MODID)
public class SequenceBoxTileEntity extends TileEntity implements INBTReceiver, INamedContainerProvider{

	private final ArrayList<Float> sequenceVal = new ArrayList<>();
	private final ArrayList<String> sequenceStr = new ArrayList<>();
	private int index = 0;
	private boolean hadRedstoneSignal = false;//Whether this block is currently receiving a redstone signal- used for differenting pulses

	@ObjectHolder("sequence_box")
	private static TileEntityType<SequenceBoxTileEntity> type = null;

	public SequenceBoxTileEntity(){
		super(type);
		sanitizeState();
	}

	private void sanitizeState(){
		//These 2 while loops should never actually trigger (in theory)
		while(sequenceStr.size() > sequenceVal.size()){
			sequenceStr.remove(sequenceStr.size() - 1);
		}
		while(sequenceStr.size() < sequenceVal.size()){
			sequenceVal.remove(sequenceVal.size() - 1);
		}

		if(sequenceVal.size() == 0){
			sequenceVal.add(0F);
			sequenceStr.add("0");
		}

		if(index >= sequenceVal.size()){
			index = 0;
		}
	}

	public void worldUpdate(Block blockIn){
		CircuitUtil.updateFromWorld(circHandler, blockIn);
		boolean hasRedstone = world.isBlockPowered(pos);
		if(hasRedstone != hadRedstoneSignal){
			hadRedstoneSignal = hasRedstone;
			markDirty();
			if(hasRedstone){
				//advance the sequence
				index++;
				index %= sequenceVal.size();
				circHandler.notifyOutputChange();
			}
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT nbt){
		super.write(nbt);
		nbt.putBoolean("redstone", hadRedstoneSignal);
		nbt.putInt("index", index);
		for(int i = 0; i < sequenceVal.size(); i++){
			nbt.putFloat(i + "_val", sequenceVal.get(i));
			nbt.putString(i + "_str", sequenceStr.get(i));
		}
		return nbt;
	}

	@Override
	public void read(BlockState state, CompoundNBT nbt){
		super.read(state, nbt);
		hadRedstoneSignal = nbt.getBoolean("redstone");
		index = nbt.getInt("index");
		sequenceVal.clear();
		sequenceStr.clear();
		int i = 0;
		while(nbt.contains(i + "_val")){
			sequenceVal.add(nbt.getFloat(i + "_val"));
			sequenceStr.add(nbt.getString(i + "_str"));
			i++;
		}
		sanitizeState();
	}

	@Override
	public CompoundNBT getUpdateTag(){
		CompoundNBT nbt =  super.getUpdateTag();
		nbt.putInt("index", index);
		for(int i = 0; i < sequenceVal.size(); i++){
			nbt.putFloat(i + "_val", sequenceVal.get(i));
			nbt.putString(i + "_str", sequenceStr.get(i));
		}
		return nbt;
	}

	@Override
	public void remove(){
		super.remove();
		circOpt.invalidate();
	}

	public final CircuitUtil.OutputCircHandler circHandler = new CircuitUtil.OutputCircHandler();
	private final LazyOptional<IRedstoneHandler> circOpt = CircuitUtil.makeBaseCircuitOptional(this, circHandler, () -> sequenceVal.get(index));

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side){
		if(cap == RedstoneUtil.REDSTONE_CAPABILITY){
			return (LazyOptional<T>) circOpt;
		}
		return super.getCapability(cap, side);
	}

	//UI stuff below

	public void encodeBuf(PacketBuffer buf){
		buf.writeBlockPos(pos);
		buf.writeVarInt(index);
		buf.writeVarInt(sequenceStr.size());
		for(String input : sequenceStr){
			buf.writeString(input);
		}
	}

	@Override
	public ITextComponent getDisplayName(){
		return new TranslationTextComponent("container.sequence_box");
	}

	@Nullable
	@Override
	public Container createMenu(int id, PlayerInventory playerInv, PlayerEntity player){
		PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
		encodeBuf(buf);
		return new SequenceBoxContainer(id, playerInv, buf);
	}

	@Override
	public void receiveNBT(CompoundNBT nbt, @Nullable ServerPlayerEntity sender){
		//TODO
	}
}
