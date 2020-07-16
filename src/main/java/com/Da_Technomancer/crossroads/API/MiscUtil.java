package com.Da_Technomancer.crossroads.API;

import com.Da_Technomancer.essentials.ESConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.FoodStats;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

public final class MiscUtil{

	/**
	 * A common style applied to "quip" lines in tooltips
	 */
	public static final Style TT_QUIP = ESConfig.TT_QUIP;

	/**
	 * Rounds to a set number of decimal places
	 * @param numIn The value to round
	 * @param decPlac The number of decimal places to round to
	 * @return The rounded value
	 */
	public static double preciseRound(double numIn, int decPlac){
		return Math.round(numIn * Math.pow(10, decPlac)) / Math.pow(10D, decPlac);
	}

	/**
	 * The same as Math.round except if the decimal
	 * is exactly .5 then it rounds down.
	 *
	 * This is for systems that require rounding and
	 * NEED the distribution of output to not be higher than
	 * the input to prevent dupe bugs.
	 * @param in The value to round
	 * @return The rounded value
	 */
	public static int safeRound(double in){
		if(in % 1 <= .5D){
			return (int) Math.floor(in);
		}else{
			return (int) Math.ceil(in);
		}
	}

	/**
	 * A server-side friendly version of Entity.class' raytrace (currently called Entity#func_213324_a(double, float, boolean))
	 */
	public static BlockRayTraceResult rayTrace(Entity ent, double blockReachDistance){
		Vector3d vec3d = ent.getPositionVec().add(0, ent.getEyeHeight(), 0);
		Vector3d vec3d2 = vec3d.add(ent.getLook(1F).scale(blockReachDistance));
		return ent.world.rayTraceBlocks(new RayTraceContext(vec3d, vec3d2, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, ent));
	}

	/**
	 * Localizes the input. Do not trust the result if called on the physical server
	 * @param input The string to localize
	 * @return The localized string
	 */
	public static String localize(String input){
		return new TranslationTextComponent(input).getUnformattedComponentText();
	}

	/**
	 * Localizes and formats the input. Do not trust the result if called on the physical server
	 * @param input The string to localize
	 * @param formatArgs Arguments to pass the formatter
	 * @return The localized and formatted string
	 */
	public static String localize(String input, Object... formatArgs){
		return new TranslationTextComponent(input, formatArgs).getString();
	}

	public static String getLocalizedFluidName(String localizationKey){
		return localizationKey == null || localizationKey.isEmpty() || localizationKey.equals("block.minecraft.air") ? localize("tt.crossroads.boilerplate.empty") : localize(localizationKey);
	}

	/**
	 * Calculates a set of integer quantities to withdraw from a fixed integer source, where the total size of the withdrawn set is toWithdraw
	 * Does not mutate the passed arguments
	 * Attempts to distribute proportionally to source, with an over-emphasis on small amounts for the remainder
	 * Will not withdraw any variety in greater quantity than in source
	 * @param src The source quantities to calculate withdrawal from. Only positive and zero values are allowed
	 * @param toWithdraw The quantity to withdraw. Must be positive or zero
	 * @return The distribution withdrawn. Each index corresponds to the same src index
	 */
	public static int[] withdrawExact(int[] src, int toWithdraw){
		if(toWithdraw <= 0 || src.length == 0){
			//Withdraw nothing
			return new int[src.length];
		}
		int srcQty = 0;
		for(int val : src){
			srcQty += val;
		}
		if(toWithdraw < srcQty){
			int[] withdrawn = new int[src.length];
			double basePortion = (double) toWithdraw / (double) srcQty;//Multiplier for src applied get withdrawn. Any remaining space will be filled by a different distribution
			int totalWithdrawn = 0;
			for(int i = 0; i < withdrawn.length; i++){
				int toMove = (int) (basePortion * src[i]);//Intentional truncation- rounding down
				totalWithdrawn += toMove;
				withdrawn[i] += toMove;
			}

			if(totalWithdrawn < toWithdraw){
				//For the remaining space to fill, perform a round robin distribution
				//We start the distribution with the smallest (least magnitude pointed to) index, and end with the largest.
				//We do this to reduce dependence on the index order, and because it has been found that ensuring small pools are drawn from is more conveniently for play

				//Sort the source pools from least to greatest
				//Selection sort, using an array of values where each value is an index in src
				int[] sorted = new int[src.length];
				for(int i = 0; i < sorted.length - 1; i++){
					int smallestVal = Integer.MAX_VALUE;
					int smallestInd = 0;
					for(int j = i; j < sorted.length; j++){
						if(i == 0){
							//For the first pass, we populate sorted with 0,1,2,3,etc to represent the original, unsorted array
							sorted[j] = j;
						}
						//src[sorted[j]] < smallestVal is the normal sorting ordering
						//sorted[j] < sorted[smallestInd] is a secondary sorting ordering that is only considered if (src[sorted[j]] == smallestVal)
						//This secondary sorting ordering was added to maintain certain useful specialized behaviour from the previous algorithm (red-green-blue-black sorting). It may be removed in a later update
						//It does not interfere with sorting from lowest to highest quantity, and only acts as a tie-breaker
						if(src[sorted[j]] < smallestVal || sorted[j] < sorted[smallestInd] && src[sorted[j]] == smallestVal){
							smallestInd = sorted[j];
							smallestVal = src[sorted[j]];
						}
					}
					if(smallestInd != i){
						int toSwap = sorted[i];
						sorted[i] = smallestInd;
						sorted[smallestInd] = toSwap;
					}
				}

				//Perform round robin distribution
				int indexInSorted = 0;//The index in sorted (which contains more indices...) we are drawing from
				while(totalWithdrawn < toWithdraw){
					int indexInSrc = sorted[indexInSorted];
					if(src[indexInSrc] > withdrawn[indexInSrc]){//Make sure there is sufficient to withdraw from this pool, otherwise proceed to the next pool
						//Withdraw one from the current pool
						withdrawn[indexInSrc]++;
						totalWithdrawn++;
					}
					//Move to the next pool in sorted
					indexInSorted++;
					indexInSorted %= sorted.length;
				}
			}

			return withdrawn;
		}else{
			//Less total in src than to withdraw. Return same as in src
			return Arrays.copyOf(src, src.length);
		}
	}

	/**
	 * Server-side safe way of setting hunger and saturation of a player
	 * @param player The player to set the food of
	 * @param hunger New hunger value, [0, 20]
	 * @param saturation New saturation value, [0, 20]
	 */
	public static void setPlayerFood(PlayerEntity player, int hunger, float saturation){
		// The way saturation is coded is weird, and the best way to do this is through nbt.
		CompoundNBT nbt = new CompoundNBT();
		FoodStats stats = player.getFoodStats();
		stats.write(nbt);
		nbt.putInt("foodLevel", Math.min(hunger, 20));
		nbt.putFloat("foodSaturationLevel", Math.min(20F, saturation));
		stats.read(nbt);
	}

	/**
	 * Adds a message to a player's chat
	 * Works on both sides
	 * @param player The player to add a message to
	 * @param message The message to send
	 */
	public static void chatMessage(Entity player, ITextComponent message){
		player.sendMessage(message, player.getUniqueID());
	}

	/**
	 * Gets the name of a dimension, for logging purposes
	 * @param world The world to get the dimension of
	 * @return The name of the dimension, for logging purposes, unlocalized
	 */
	public static String getDimensionName(@Nonnull World world){
		return world.func_234923_W_().func_240901_a_().toString();//MCP: getRegistryKey<World>; get registry name
	}

	/**
	 * Gets the registry key for a world with the given registry ID
	 * @param registryID The world registry keyname
	 * @param cache An optional cache parameter- will return this value if it matches the passed ID
	 * @return The registry key in the World Key registry associated with a given registry keyname
	 */
	public static RegistryKey<World> getWorldKey(ResourceLocation registryID, @Nullable RegistryKey<World> cache){
		if(cache != null && cache.func_240901_a_().equals(registryID)){
			return cache;
		}

		return RegistryKey.func_240903_a_(Registry.WORLD_KEY, registryID);
	}

	/**
	 * Gets the world associated with a given registry key, or null if it doesn't exist
	 * Server side only
	 * @param registryKey The registry key to search for
	 * @param server The server instance
	 * @return The world instance for the passed registry key
	 */
	@Nullable
	public static ServerWorld getWorld(RegistryKey<World> registryKey, MinecraftServer server){
		return server.getWorld(registryKey);
	}
}
