package com.Da_Technomancer.crossroads.API.technomancy;

import com.Da_Technomancer.crossroads.API.MiscUtil;
import com.Da_Technomancer.crossroads.API.beams.EnumBeamAlignments;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

public class GatewayAddress{

	protected static final EnumBeamAlignments[] LEGAL_VALS = new EnumBeamAlignments[8];

	static{
		LEGAL_VALS[0] = EnumBeamAlignments.LIGHT;
		LEGAL_VALS[1] = EnumBeamAlignments.ENCHANTMENT;
		LEGAL_VALS[2] = EnumBeamAlignments.CHARGE;
		LEGAL_VALS[3] = EnumBeamAlignments.TIME;
		LEGAL_VALS[4] = EnumBeamAlignments.RIFT;
		LEGAL_VALS[5] = EnumBeamAlignments.EQUILIBRIUM;
		LEGAL_VALS[6] = EnumBeamAlignments.EXPANSION;
		LEGAL_VALS[7] = EnumBeamAlignments.FUSION;
	}

	private final EnumBeamAlignments[] address = new EnumBeamAlignments[4];

	/**
	 * Instantiates a new instance with a set address
	 * @param addressIn A size 4 non-null array with the address, will be copied to prevent mutability
	 */
	public GatewayAddress(EnumBeamAlignments[] addressIn){
		System.arraycopy(addressIn, 0, address, 0, 4);
	}

	public boolean fullAddress(){
		for(EnumBeamAlignments entry : address){
			if(entry == null){
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns one configured alignment
	 * @param index The index to return, [0-3]
	 * @return The specified configured alignment
	 */
	public EnumBeamAlignments getEntry(int index){
		return address[index];
	}

	public int serialize(){
		int serial = 0;
		for(int i = 0; i < 4; i++){
			serial |= address[i] == null ? 0 : (address[i].ordinal() + 1) << 4*i;
		}
		return serial;
	}

	public static GatewayAddress deserialize(int serial){
		EnumBeamAlignments[] vals = EnumBeamAlignments.values();
		final int mask = 0xF;
		EnumBeamAlignments[] entries = new EnumBeamAlignments[4];
		for(int i = 0; i < 4; i++){
			int subSerial = (serial >>> i*4) & mask;
			entries[i] = subSerial == 0 ? null : vals[subSerial - 1];
		}
		return new GatewayAddress(entries);
	}

	@Override
	public boolean equals(Object o){
		if(this == o){
			return true;
		}
		if(o == null || getClass() != o.getClass()){
			return false;
		}
		GatewayAddress that = (GatewayAddress) o;
		return Arrays.equals(address, that.address);
	}

	@Override
	public int hashCode(){
		return serialize();
	}

	public static EnumBeamAlignments getLegalEntry(int index){
		//((a % b) + b) % b is used instead of a % b in order to handle negative indices
		return LEGAL_VALS[((index % LEGAL_VALS.length) + LEGAL_VALS.length) % LEGAL_VALS.length];
	}

	public static int getEntryID(EnumBeamAlignments align){
		for(int i = 0; i < LEGAL_VALS.length; i++){
			if(LEGAL_VALS[i] == align){
				return i;
			}
		}
		return -1;
	}

	public static class Location{

		public final BlockPos pos;
		public final ResourceLocation dim;
		private ResourceKey<Level> cache;//Used to retrieve the associated world data more quickly

		public Location(BlockPos pos, Level world){
			this.pos = pos.immutable();
			cache = world.dimension();
			this.dim = cache.location();
		}

		public Location(long posSerial, String dimSerial){
			this.pos = BlockPos.of(posSerial);
			this.dim = new ResourceLocation(dimSerial);
		}

		@Nullable
		public Level evalDim(MinecraftServer server){
			try{
				cache = MiscUtil.getWorldKey(dim, cache);
				return MiscUtil.getWorld(cache, server);
			}catch(Exception e){
				return null;
			}
		}

		@Nullable
		public IGateway evalTE(MinecraftServer server){
			Level w = evalDim(server);
			if(w == null){
				return null;
			}
			//Load the chunk
			ChunkPos chunkPos = new ChunkPos(pos);
			((ServerChunkCache) (w.getChunkSource())).addRegionTicket(TicketType.PORTAL, chunkPos, 3, pos);
			BlockEntity te = w.getBlockEntity(pos);
			if(te instanceof IGateway){
				return (IGateway) te;
			}
			return null;
		}

		@Override
		public boolean equals(Object o){
			if(this == o){
				return true;
			}
			if(o == null || getClass() != o.getClass()){
				return false;
			}
			Location location = (Location) o;
			return dim == location.dim && pos.equals(location.pos);
		}

		@Override
		public int hashCode(){
			return Objects.hash(pos, dim);
		}
	}
}
