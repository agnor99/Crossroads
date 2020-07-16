package com.Da_Technomancer.crossroads.items;

import com.Da_Technomancer.crossroads.API.MiscUtil;
import com.Da_Technomancer.crossroads.CRConfig;
import com.Da_Technomancer.crossroads.Crossroads;
import com.Da_Technomancer.crossroads.entity.EntityBullet;
import com.Da_Technomancer.crossroads.tileentities.rotary.WindingTableTileEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class SpringGun extends ShootableItem implements WindingTableTileEntity.IWindableItem{

	private static final ITag<Item> AMMO_TAG = ItemTags.makeWrapperTag(Crossroads.MODID + ":spring_gun_ammo");
	private static final Predicate<ItemStack> AMMO_PREDICATE = s -> AMMO_TAG.contains(s.getItem());

	private static final double MIN_SPEED = 1;

	protected SpringGun(){
		super(new Properties().group(CRItems.TAB_CROSSROADS).maxStackSize(1));
		String name = "spring_gun";
		setRegistryName(name);
		CRItems.toRegister.add(this);
	}

	private float calcDamage(double wind){
		//Values based on regression between (1, 5) and (10, 50)
		return Math.max(Math.round(20.8D * Math.sqrt(wind) - 15.8D), 0);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand){
		ItemStack held = playerIn.getHeldItem(hand);
		double wind = getWindLevel(held);
		ItemStack ammo = playerIn.findAmmo(held);
		if(wind > MIN_SPEED && !ammo.isEmpty()){
			if(!worldIn.isRemote){
				//Shoot
				ItemStack bulletItem = ammo.copy();
				bulletItem.setCount(1);
				EntityBullet bullet = new EntityBullet(worldIn, playerIn, calcDamage(wind), bulletItem);
				bullet.shoot(playerIn.getPosX(), playerIn.getPosY() + 1.2, playerIn.getPosZ(), (float) wind * 2.5F, 0.2F);
				worldIn.addEntity(bullet);

				//Consume ammo
				if(!playerIn.abilities.isCreativeMode){
					ammo.shrink(1);
				}

				worldIn.playSound(playerIn, playerIn.getPosX(), playerIn.getPosY(), playerIn.getPosZ(), SoundEvents.ITEM_CROSSBOW_SHOOT, SoundCategory.PLAYERS, 1F, 1F);//Play sound for other players
			}

			playerIn.playSound(SoundEvents.ITEM_CROSSBOW_SHOOT, SoundCategory.PLAYERS, 1F, 1F);//Play sound on the client
			//Return the discharged weapon
			setWindLevel(held, 0);
			return ActionResult.resultSuccess(held);
		}

		return ActionResult.resultFail(held);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		double wind = getWindLevel(stack);
		double maxWind = getMaxWind();
		tooltip.add(new TranslationTextComponent("tt.crossroads.boilerplate.spring_speed", CRConfig.formatVal(wind), CRConfig.formatVal(maxWind)));
		tooltip.add(new TranslationTextComponent("tt.crossroads.spring_gun.winding", CRConfig.formatVal(calcDamage(wind)), CRConfig.formatVal(calcDamage(maxWind))));
		tooltip.add(new TranslationTextComponent("tt.crossroads.spring_gun.desc"));
		tooltip.add(new TranslationTextComponent("tt.crossroads.spring_gun.quip").func_230530_a_(MiscUtil.TT_QUIP));
	}

	@Override
	public double getMaxWind(){
		return CRConfig.springGunCap.get();
	}

	@Override
	public Predicate<ItemStack> getInventoryAmmoPredicate(){
		return AMMO_PREDICATE;
	}

	@Override
	public int func_230305_d_(){
		return 15;//Don't actually know what this does- looks mob AI related?
	}

	@Override
	public UseAction getUseAction(ItemStack stack){
		return UseAction.CROSSBOW;//Doesn't really have any effect as arm posing is hard coded for crossbows
	}

	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items){
		if(isInGroup(group)){
			items.add(new ItemStack(this, 1));
			ItemStack stack = new ItemStack(this, 1);
			setWindLevel(stack, getMaxWind());
			items.add(stack);
		}
	}
}
