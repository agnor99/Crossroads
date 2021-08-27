package com.Da_Technomancer.crossroads.items.witchcraft;

import com.Da_Technomancer.crossroads.API.witchcraft.ICultivatable;
import com.Da_Technomancer.crossroads.items.CRItems;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class VillagerBrain extends Item implements ICultivatable{

	private static final long LIFETIME = 30 * 60 * 20;//30 minutes
	private static final String TRADES = "cr_trades";
	private static final String CURRENT_TRADE = "cr_current_trade";

	public VillagerBrain(){
		super(new Properties().stacksTo(1));//Not added to any creative tab
		String name = "villager_brain";
		setRegistryName(name);
		CRItems.toRegister.add(this);
	}

	@Override
	public long getLifetime(){
		return LIFETIME;
	}

	@Override
	public double getFreezeTemperature(){
		return 0;
	}

	public MerchantOffers getOffers(ItemStack stack){
		return new MerchantOffers(stack.getOrCreateTagElement(TRADES));
	}

	public void setOffers(ItemStack stack, MerchantOffers offers){
		stack.getOrCreateTag().put(TRADES, offers.createTag());
	}

	public MerchantOffer getCurrentOffer(ItemStack stack){
		CompoundNBT nbt = stack.getOrCreateTag();
		int tradeIndex = nbt.getInt(CURRENT_TRADE);
		MerchantOffers offers = getOffers(stack);
		if(offers.size() == 0){
			return null;
		}
		if(tradeIndex >= offers.size()){
			tradeIndex %= offers.size();
			nbt.putInt(CURRENT_TRADE, tradeIndex);
		}

		MerchantOffer currentOffer = offers.get(tradeIndex);
		//If this has been frozen, make the trade worse
		if(wasFrozen(stack)){
			currentOffer.setSpecialPriceDiff(4);
		}
		return currentOffer;
	}

	public void incrementCurrentOffer(ItemStack stack){
		CompoundNBT nbt = stack.getOrCreateTag();
		MerchantOffers offers = getOffers(stack);
		if(offers.size() != 0){
			int tradeIndex = nbt.getInt(CURRENT_TRADE);
			tradeIndex = (tradeIndex + 1) % offers.size();
			nbt.putInt(CURRENT_TRADE, tradeIndex);
		}
	}

	@Override
	public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand){
		ItemStack held = player.getItemInHand(hand);
		incrementCurrentOffer(held);
		return ActionResult.success(held);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag){
		MerchantOffer offer = getCurrentOffer(stack);
		if(offer == null){
			//No trades
			tooltip.add(new TranslationTextComponent("tt.crossroads.villager_brain.trade.none"));
		}else if(offer.getCostB().isEmpty()){
			//Single input trade
			tooltip.add(new TranslationTextComponent("tt.crossroads.villager_brain.trade.single", getDisplayParameter(offer.getCostA()), offer.getCostA().getCount(), getDisplayParameter(offer.getResult()), offer.getResult().getCount()));
		}else{
			//Dual input trade
			tooltip.add(new TranslationTextComponent("tt.crossroads.villager_brain.trade.dual", getDisplayParameter(offer.getCostA()), offer.getCostA().getCount(), getDisplayParameter(offer.getCostB()), offer.getCostB().getCount(), getDisplayParameter(offer.getResult()), offer.getResult().getCount()));
		}
		ICultivatable.addTooltip(stack, world, tooltip);
		tooltip.add(new TranslationTextComponent("tt.crossroads.village_brain.desc"));
	}

	private static Object getDisplayParameter(ItemStack stack){
		int totalEnchants = 0;
		ITextComponent firstEnchantName = null;

		if(stack.isEnchanted()){
			//Doesn't work on enchanted books
			ListNBT enchantList = stack.getEnchantmentTags();
			totalEnchants = enchantList.size();
			CompoundNBT compoundnbt = enchantList.getCompound(0);
			Enchantment firstEnchant = Registry.ENCHANTMENT.get(ResourceLocation.tryParse(compoundnbt.getString("id")));
			if(firstEnchant != null){
				firstEnchantName = firstEnchant.getFullname(compoundnbt.getInt("lvl"));
			}
		}
		if(stack.getItem() instanceof EnchantedBookItem){
			ListNBT enchantList = EnchantedBookItem.getEnchantments(stack);
			totalEnchants = enchantList.size();
			CompoundNBT compoundnbt = enchantList.getCompound(0);
			Enchantment firstEnchant = Registry.ENCHANTMENT.get(ResourceLocation.tryParse(compoundnbt.getString("id")));
			if(firstEnchant != null){
				firstEnchantName = firstEnchant.getFullname(compoundnbt.getInt("lvl"));
			}
		}

		if(firstEnchantName != null){
			if(totalEnchants > 1){
				return new TranslationTextComponent("tt.crossroads.villager_brain.item.enchant.multi", stack.getHoverName(), firstEnchantName, totalEnchants, totalEnchants - 1);
			}else{
				return new TranslationTextComponent("tt.crossroads.villager_brain.item.enchant", stack.getHoverName(), firstEnchantName);
			}
		}
		return stack.getHoverName();
	}

	@Nullable
	@Override
	public CultivationTrade getCultivationTrade(ItemStack self, World world){
		//Performs villager trades
		if(isSpoiled(self, world)){
			return null;
		}

		MerchantOffer offer = getCurrentOffer(self);
		if(offer == null){
			return null;
		}
		return new CultivationTrade(offer.getCostA(), offer.getCostB(), offer.getResult());
	}
}
