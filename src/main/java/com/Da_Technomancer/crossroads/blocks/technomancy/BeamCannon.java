package com.Da_Technomancer.crossroads.blocks.technomancy;

import com.Da_Technomancer.crossroads.API.MiscUtil;
import com.Da_Technomancer.crossroads.blocks.CRBlocks;
import com.Da_Technomancer.crossroads.tileentities.technomancy.BeamCannonTileEntity;
import com.Da_Technomancer.essentials.blocks.ESProperties;
import com.Da_Technomancer.essentials.tileentities.ITickableTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;

public class BeamCannon extends AbstractCannon{

	private static final VoxelShape[] SHAPES = new VoxelShape[6];
	static{
		SHAPES[0] = Shapes.or(box(0, 7, 0, 16, 16, 16));
		SHAPES[1] = Shapes.or(box(0, 0, 0, 16, 9, 16));
		SHAPES[2] = Shapes.or(box(0, 0, 7, 16, 16, 16));
		SHAPES[3] = Shapes.or(box(0, 0, 0, 16, 16, 9));
		SHAPES[4] = Shapes.or(box(7, 0, 0, 16, 16, 16));
		SHAPES[5] = Shapes.or(box(0, 0, 0, 9, 16, 16));
	}

	public BeamCannon(){
		super("beam_cannon", CRBlocks.getMetalProperty());
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context){
		return SHAPES[state.getValue(ESProperties.FACING).get3DDataValue()];
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
		return new BeamCannonTileEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> type){
		return ITickableTileEntity.createTicker(type, BeamCannonTileEntity.TYPE);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn){
		tooltip.add(new TranslatableComponent("tt.crossroads.beam_cannon.desc"));
		tooltip.add(new TranslatableComponent("tt.crossroads.beam_cannon.angle"));
		tooltip.add(new TranslatableComponent("tt.crossroads.beam_cannon.lockable"));
		tooltip.add(new TranslatableComponent("tt.crossroads.boilerplate.inertia", BeamCannonTileEntity.INERTIA));
		tooltip.add(new TranslatableComponent("tt.crossroads.beam_cannon.quip").setStyle(MiscUtil.TT_QUIP));
	}
}
