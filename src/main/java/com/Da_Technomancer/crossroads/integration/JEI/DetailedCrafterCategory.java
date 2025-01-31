package com.Da_Technomancer.crossroads.integration.JEI;


import com.Da_Technomancer.crossroads.Crossroads;
import com.Da_Technomancer.crossroads.blocks.CRBlocks;
import com.Da_Technomancer.crossroads.crafting.recipes.DetailedCrafterRec;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class DetailedCrafterCategory implements IRecipeCategory<DetailedCrafterRec>{

	public static final RecipeType<DetailedCrafterRec> TYPE = RecipeType.create(Crossroads.MODID, "detailed_crafter", DetailedCrafterRec.class);
	private final IDrawable back;
	private final IDrawable icon;
	private final IDrawable gear;
	private final IDrawable flask;
	private final IDrawable leaf;
	private final ICraftingGridHelper gridHelper;

	protected DetailedCrafterCategory(IGuiHelper guiHelper){
		ResourceLocation location = new ResourceLocation(Crossroads.MODID, "textures/gui/container/detailed_crafter.png");
		back = guiHelper.createDrawable(location, 29, 16, 129, 62);
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(CRBlocks.detailedCrafter, 1));
		gear = guiHelper.createDrawable(location, 176, 0, 16, 16);
		flask = guiHelper.createDrawable(location, 176, 16, 16, 16);
		leaf = guiHelper.createDrawable(location, 176, 32, 16, 16);
		gridHelper = guiHelper.createCraftingGridHelper(1);
	}

	@Override
	public ResourceLocation getUid(){
		return TYPE.getUid();
	}

	@Override
	public Class<? extends DetailedCrafterRec> getRecipeClass(){
		return TYPE.getRecipeClass();
	}

	@Override
	public RecipeType<DetailedCrafterRec> getRecipeType(){
		return TYPE;
	}

	@Override
	public Component getTitle(){
		return CRBlocks.detailedCrafter.getName();
	}

	@Override
	public IDrawable getBackground(){
		return back;
	}

	@Override
	public void draw(DetailedCrafterRec recipe, IRecipeSlotsView view, PoseStack matrix, double mouseX, double mouseY){
		//Minecraft.getInstance().fontRenderer.drawString("Shapeless", 60, 5, 0x404040);
		switch(recipe.getPath()){
			case TECHNOMANCY:
				gear.draw(matrix, 95, 44);
				break;
			case ALCHEMY:
				flask.draw(matrix, 79, 44);
				break;
			case WITCHCRAFT:
				leaf.draw(matrix, 111, 44);
				break;
		}
	}

	@Override
	public IDrawable getIcon(){
		return icon;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, DetailedCrafterRec recipe, IFocusGroup focuses){
		//Based on (read: shamelessly copied from) the JEI implementation of vanilla crafting recipes
		List<List<ItemStack>> inputs = recipe.getIngredients().stream()
				.map(ingredient -> List.of(ingredient.getItems()))
				.toList();
		ItemStack resultItem = recipe.getResultItem();

		int width = recipe.getWidth();
		int height = recipe.getHeight();
		gridHelper.setOutputs(builder, VanillaTypes.ITEM, List.of(resultItem));
		gridHelper.setInputs(builder, VanillaTypes.ITEM, inputs, width, height);
	}
}
