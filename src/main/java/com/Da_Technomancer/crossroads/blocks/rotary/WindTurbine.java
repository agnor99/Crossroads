package com.Da_Technomancer.crossroads.blocks.rotary;

import com.Da_Technomancer.crossroads.API.CRProperties;
import com.Da_Technomancer.crossroads.API.rotary.RotaryUtil;
import com.Da_Technomancer.crossroads.blocks.CRBlocks;
import com.Da_Technomancer.crossroads.tileentities.rotary.WindTurbineTileEntity;
import com.Da_Technomancer.essentials.ESConfig;
import com.Da_Technomancer.essentials.blocks.redstone.IReadable;
import com.Da_Technomancer.essentials.blocks.redstone.RedstoneUtil;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class WindTurbine extends BaseEntityBlock implements IReadable{

	public WindTurbine(){
		super(Properties.of(Material.WOOD).strength(2));
		String name = "wind_turbine";
		setRegistryName(name);
		CRBlocks.toRegister.add(this);
		CRBlocks.blockAddQue(this);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
		return new WindTurbineTileEntity(true);
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit){
		ItemStack heldItem = playerIn.getItemInHand(hand);
		if(ESConfig.isWrench(heldItem)){
			if(!worldIn.isClientSide){
				worldIn.setBlockAndUpdate(pos, state.setValue(CRProperties.HORIZ_FACING, state.getValue(CRProperties.HORIZ_FACING).getClockWise()));
				RotaryUtil.increaseMasterKey(true);
			}
			return InteractionResult.SUCCESS;
		}else if(Tags.Items.DYES.contains(heldItem.getItem())){
			BlockEntity te = worldIn.getBlockEntity(pos);
			if(te instanceof WindTurbineTileEntity){
				if(!worldIn.isClientSide){
					((WindTurbineTileEntity) te).dyeBlade(heldItem);
				}
				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.PASS;
	}

	@Override
	public RenderShape getRenderShape(BlockState state){
		return RenderShape.MODEL;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
		return defaultBlockState().setValue(CRProperties.HORIZ_FACING, context.getHorizontalDirection());
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder){
		builder.add(CRProperties.HORIZ_FACING);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag advanced){
		tooltip.add(new TranslatableComponent("tt.crossroads.wind_turbine.desc"));
		tooltip.add(new TranslatableComponent("tt.crossroads.wind_turbine.power", WindTurbineTileEntity.LOW_POWER, WindTurbineTileEntity.HIGH_POWER, (WindTurbineTileEntity.LOW_POWER + WindTurbineTileEntity.HIGH_POWER) / 2D));
		tooltip.add(new TranslatableComponent("tt.crossroads.wind_turbine.limits", WindTurbineTileEntity.MAX_SPEED));
		tooltip.add(new TranslatableComponent("tt.crossroads.wind_turbine.env"));
		tooltip.add(new TranslatableComponent("tt.crossroads.boilerplate.inertia", WindTurbineTileEntity.INERTIA));
	}

	@Override
	public float read(Level world, BlockPos pos, BlockState state){
		BlockEntity te = world.getBlockEntity(pos);
		if(te instanceof WindTurbineTileEntity){
			return ((WindTurbineTileEntity) te).getRedstoneOutput();
		}else{
			return 0;
		}
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state){
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos){
		return RedstoneUtil.clampToVanilla(read(world, pos, state));
	}
}
