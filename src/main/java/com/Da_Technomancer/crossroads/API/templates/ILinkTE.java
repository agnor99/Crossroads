package com.Da_Technomancer.crossroads.API.templates;

import com.Da_Technomancer.crossroads.API.packets.ILongReceiver;
import com.Da_Technomancer.crossroads.API.packets.ModPackets;
import com.Da_Technomancer.crossroads.API.packets.SendLongToClient;
import com.Da_Technomancer.crossroads.items.CrossroadsItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.ArrayList;

/**
 * A helper class to be placed on TileEntities to enable basic linking behaviour
 */
public interface ILinkTE extends ILongReceiver{

	public static final String POS_NBT = "c_link";
	public static final String DIM_NBT = "c_link_dim";
	public static final byte LINK_PACKET_ID = 8;
	public static final byte CLEAR_PACKET_ID = 9;

	public static boolean isLinkTool(ItemStack stack){
		return stack.getItem() == CrossroadsItems.linkingTool;
	}

	/**
	 * @return This TE
	 */
	public TileEntity getTE();

	public default int getRange(){
		return 16;
	}

	/**
	 * @return Whether this device can ever be the start of a link
	 */
	public boolean canBeginLinking();

	/**
	 *
	 * @param otherTE The ILinkTE to attempt linking to
	 * @return Whether this TE is allowed to link to the otherTE. The source TE controls whether the link is allowed, the target is not checked. Do not check range
	 */
	public boolean canLink(ILinkTE otherTE);

	/**
	 * A mutable list of linked relative block positions.
	 * @return The list of links
	 */
	public ArrayList<BlockPos> getLinks();

	/**
	 * @return The maximum number of linked devices. The source controls this
	 */
	public default int getMaxLinks(){
		return 3;
	}

	/**
	 *
	 * @param endpoint The endpoint that this is being linked to
	 * @param player The calling player, for sending chat messages
	 * @return Whether the operation succeeded
	 */
	public default boolean link(ILinkTE endpoint, PlayerEntity player){
		ArrayList<BlockPos> links = getLinks();
		BlockPos linkPos = endpoint.getTE().getPos().subtract(getTE().getPos());
		if(links.contains(linkPos)){
			player.sendMessage(new StringTextComponent("Device already linked; Canceling linking"));
		}else if(links.size() < getMaxLinks()){
			links.add(linkPos);
			BlockPos tePos = getTE().getPos();
			ModPackets.network.sendToAllAround(new SendLongToClient(LINK_PACKET_ID, linkPos.toLong(), tePos), new NetworkRegistry.TargetPoint(getTE().getWorld().provider.getDimension(), tePos.getX(), tePos.getY(), tePos.getZ(), 512));
			getTE().markDirty();
			player.sendMessage(new StringTextComponent("Linked device at " + getTE().getPos() + " to send to " + endpoint.getTE().getPos()));
			return true;
		}else{
			player.sendMessage(new StringTextComponent("All " + getMaxLinks() + " links already occupied; Canceling linking"));
		}
		return false;
	}

	/**
	 * Must be called from the block (server side only) to perform linking
	 * @param wrench The held wrench itemstack
	 * @param player The current player   
	 * @return The possibly modified wrench
	 */
	public default ItemStack wrench(ItemStack wrench, PlayerEntity player){
		if(player.isSneaking()){
			player.sendMessage(new StringTextComponent("Clearing links"));
			clearLinks();
		}else if(wrench.hasTagCompound() && wrench.getTagCompound().hasKey(POS_NBT) && wrench.getTagCompound().getInteger(DIM_NBT) == player.world.provider.getDimension()){
			BlockPos prev = BlockPos.fromLong(wrench.getTagCompound().getLong(POS_NBT));

			TileEntity te = player.world.getTileEntity(prev);
			if(te instanceof ILinkTE && ((ILinkTE) te).canLink(this) && te != this){
				if(prev.distanceSq(getTE().getPos()) <= ((ILinkTE) te).getRange() * ((ILinkTE) te).getRange()){
					((ILinkTE) te).link(this, player);
				}else{
					player.sendMessage(new StringTextComponent("Out of range; Canceling linking"));
				}
			}else{
				player.sendMessage(new StringTextComponent("Invalid pair; Canceling linking"));
			}
		}else if(canBeginLinking()){
			if(!wrench.hasTagCompound()){
				wrench.setTagCompound(new CompoundNBT());
			}

			wrench.getTagCompound().setLong(POS_NBT, getTE().getPos().toLong());
			wrench.getTagCompound().setInteger(DIM_NBT, getTE().getWorld().provider.getDimension());
			player.sendMessage(new StringTextComponent("Beginning linking"));
			return wrench;
		}

		if(wrench.hasTagCompound()){
			wrench.getTagCompound().removeTag(POS_NBT);
			wrench.getTagCompound().removeTag(DIM_NBT);
		}
		return wrench;
	}

	public default void clearLinks(){
		getLinks().clear();
		BlockPos tePos = getTE().getPos();
		ModPackets.network.sendToAllAround(new SendLongToClient(CLEAR_PACKET_ID, 0, tePos), new NetworkRegistry.TargetPoint(getTE().getWorld().provider.getDimension(), tePos.getX(), tePos.getY(), tePos.getZ(), 512));
	}
}