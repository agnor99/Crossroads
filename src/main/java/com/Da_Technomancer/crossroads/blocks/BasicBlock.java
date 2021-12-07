package com.Da_Technomancer.crossroads.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class BasicBlock extends Block{

	public BasicBlock(String name, BlockBehaviour.Properties prop){
		super(prop);
		setRegistryName(name);
		CRBlocks.toRegister.add(this);
		CRBlocks.blockAddQue(this);
	}
}
