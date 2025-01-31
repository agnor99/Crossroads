package com.Da_Technomancer.crossroads.API.technomancy;

import com.Da_Technomancer.crossroads.API.beams.EnumBeamAlignments;
import com.Da_Technomancer.crossroads.Crossroads;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GatewaySavedData extends SavedData{

	/**
	 * Determines a reserved address based on world seed that may not be assigned to new gateways
	 * This address is being reserved for a feature to be added in the future TODO
	 * @param w The world, to get the seed from
	 * @return An address that can not be assigned to gateways
	 */
	public static GatewayAddress getReservedAddress(@Nonnull ServerLevel w){
		EnumBeamAlignments[] address = new EnumBeamAlignments[4];
		address[0] = EnumBeamAlignments.RIFT;//Fix the first alignment as rift
		long seed = w.getSeed();
		Random rand = new Random(seed);
		//The other 3 alignments are chosen randomly based on seed
		for(int i = 1; i < address.length; i++){
			address[i] = GatewayAddress.getLegalEntry(rand.nextInt(Integer.MAX_VALUE));
		}
		return new GatewayAddress(address);
	}

	/**
	 * Generates a unique new address and registers it
	 * @param w The world for the address to point to
	 * @param pos The position for the address to point to
	 * @return The newly generated address, or null if generating an address was impossible
	 */
	@Nullable
	public static GatewayAddress requestAddress(@Nonnull ServerLevel w, @Nonnull BlockPos pos){
		GatewaySavedData data = get(w);
		if(data.addressBook.size() + 1 >= (int) Math.pow(GatewayAddress.LEGAL_VALS.length, 4)){
			Crossroads.logger.warn("Ran out of Technomancy Gateway Addresses! No new gateways can be built");
			Crossroads.logger.warn("Let the mod author know that someone managed to hit the limit :)");
			return null;//Every single possible address has been assigned. Impressive?
		}

		//Generate a unique new address
		EnumBeamAlignments[] address = new EnumBeamAlignments[4];
		GatewayAddress reserved = getReservedAddress(w);
		GatewayAddress gateAdd;
		Random rand = new Random(pos.asLong());//We use the position as a seed, so that if a controller is broken and replaced/reformed at the same spot, it will have the same address unless it is already taken

		// set up the initial gateway address. Will be used if there are no collisions
		int[] initial = new int[4];
		for(int i = 0; i < 4; i++){
			initial[i] = rand.nextInt(8);
		}

		// number of attempts made to pick a new address
		int attempts = 0;

		int quadraticOffset;
		int[] offsetComponents = new int[4];

		// Loop until the address is not already taken. If it is, use an offset of progressively larger
		// squares and try again.
		do{
			// Gateway address space is 2^12; split the offset up into groups of 3 bits
			// representing offset for each part of the address
			offsetComponents[0] = (attempts & 0xe00) >> 9;
			offsetComponents[1] = (attempts & 0x1c0) >> 6;
			offsetComponents[2] = (attempts & 0x38) >> 3;
			offsetComponents[3] = (attempts & 0x7);
			for(int i = 0; i < 4; i++){
				address[i] = GatewayAddress.getLegalEntry(initial[i] + offsetComponents[i]);
			}
			gateAdd = new GatewayAddress(address);
			attempts += 1;
		}while(data.addressBook.containsKey(gateAdd) || gateAdd.equals(reserved));//Generate a new address every time the generated address is already in use

		//Register this new address in the addressBook
		data.addressBook.put(gateAdd, new GatewayAddress.Location(pos, w));
		data.setDirty();

		return gateAdd;
	}

	/**
	 * Unregisters an address
	 * @param w Any server world
	 * @param address The address to unregister
	 */
	public static void releaseAddress(@Nonnull ServerLevel w, @Nullable GatewayAddress address){
		if(address != null){
			GatewaySavedData data = get(w);
			data.addressBook.remove(address);
			data.setDirty();
		}
	}

	/**
	 * Finds the destination an address points to
	 * @param w Any non-null server world
	 * @param address The address to lookup
	 * @return The mapped destination. Null if address was null or the address was not registered
	 */
	@Nullable
	public static GatewayAddress.Location lookupAddress(@Nonnull ServerLevel w, @Nullable GatewayAddress address){
		GatewaySavedData data = get(w);
		return address == null ? null : data.addressBook.get(address);
	}

	private static GatewaySavedData get(ServerLevel world){
		//We want all dimensions to share the same saved data,
		//So we always reference the overworld instance
		DimensionDataStorage storage;
		if(world.dimension().location().equals(DimensionType.OVERWORLD_EFFECTS)){
			storage = world.getDataStorage();
		}else{
			storage = world.getServer().overworld().getDataStorage();//MCP note: getOverworld
		}
		return storage.computeIfAbsent(GatewaySavedData::load, GatewaySavedData::new, ID);
	}

	public static final String ID = Crossroads.MODID + "_gateways";

	private final Map<GatewayAddress, GatewayAddress.Location> addressBook = new HashMap<>();

	private GatewaySavedData(){
		super();
	}

	public static GatewaySavedData load(CompoundTag nbt){
		GatewaySavedData data = new GatewaySavedData();
		data.addressBook.clear();
		int i = 0;
		while(nbt.contains("key_" + i)){
			data.addressBook.put(GatewayAddress.deserialize(nbt.getInt("key_" + i)), new GatewayAddress.Location(nbt.getLong("pos_" + i), nbt.getString("dim_" + i)));
			i++;
		}
		nbt.getInt("atmos_charge");
		return data;
	}

	@Override
	public CompoundTag save(CompoundTag nbt){
		int i = 0;
		for(Map.Entry<GatewayAddress, GatewayAddress.Location> entry : addressBook.entrySet()){
			nbt.putInt("key_" + i, entry.getKey().serialize());
			nbt.putLong("pos_" + i, entry.getValue().pos.asLong());
			nbt.putString("dim_" + i, entry.getValue().dim.toString());
			i++;
		}

		return nbt;
	}
}
