package com.Da_Technomancer.crossroads.integration.JEI;

import com.Da_Technomancer.crossroads.API.MiscUtil;
import com.Da_Technomancer.crossroads.Crossroads;
import com.Da_Technomancer.crossroads.blocks.CRBlocks;
import com.Da_Technomancer.crossroads.crafting.recipes.CopshowiumRec;
import com.Da_Technomancer.crossroads.fluids.CRFluids;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class CopshowiumCategory implements IRecipeCategory<CopshowiumRec>{

	public static final ResourceLocation ID = new ResourceLocation(Crossroads.MODID, "copshowium");
	private final IDrawable back;
	private final IDrawable icon;
	private final IDrawableAnimated arrow;
	private final IDrawableStatic arrowStatic;
	private final IDrawable fluidOverlay;

	protected CopshowiumCategory(IGuiHelper guiHelper){
		back = guiHelper.createBlankDrawable(180, 100);
		icon = guiHelper.createDrawableIngredient(new ItemStack(CRBlocks.copshowiumCreationChamber, 1));
		arrowStatic = guiHelper.createDrawable(new ResourceLocation("textures/gui/container/furnace.png"), 79, 35, 24, 17);
		arrow = guiHelper.createAnimatedDrawable(guiHelper.createDrawable(new ResourceLocation("textures/gui/container/furnace.png"), 176, 14, 24, 17), 40, IDrawableAnimated.StartDirection.LEFT, false);
		fluidOverlay = JEICrossroadsPlugin.createFluidOverlay(guiHelper);
	}

	@Override
	public ResourceLocation getUid(){
		return ID;
	}

	@Override
	public Class<? extends CopshowiumRec> getRecipeClass(){
		return CopshowiumRec.class;
	}

	@Override
	public Component getTitle(){
		return CRBlocks.copshowiumCreationChamber.getName();
	}

	@Override
	public IDrawable getBackground(){
		return back;
	}

	@Override
	public void draw(CopshowiumRec rec, PoseStack matrix, double mouseX, double mouseY){
		if(rec.isFlux()){
			Minecraft.getInstance().font.draw(matrix, MiscUtil.localize("crossroads.jei.copshowium.flux"), 10, 10, 4210752);
		}
//		GlStateManager.enableAlpha();
//		GlStateManager.enableBlend();
		arrowStatic.draw(matrix, 75, 56);
		arrow.draw(matrix, 75, 56);
//		GlStateManager.disableBlend();
//		GlStateManager.disableAlpha();
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, CopshowiumRec recipe, IIngredients ingredients){
		IGuiFluidStackGroup fluidGroup = recipeLayout.getFluidStacks();

		fluidGroup.init(0, true, 50, 30, 16, 64, 4000, true, fluidOverlay);
//		fluidGroup.set(0, recipe.getInput());
		fluidGroup.init(1, false, 110, 30, 16, 64, 4000, true, fluidOverlay);
//		fluidGroup.set(1, ingredients.getOutputs(VanillaTypes.FLUID).get(0));

		fluidGroup.set(ingredients);
	}

	@Override
	public IDrawable getIcon(){
		return icon;
	}

	@Override
	public void setIngredients(CopshowiumRec recipe, IIngredients ingredients){
		int displaySize = 1000;
		ingredients.setInputLists(VanillaTypes.FLUID, ImmutableList.of(recipe.getInput().getMatchedFluidStacks(displaySize)));
		ingredients.setOutput(VanillaTypes.FLUID, new FluidStack(CRFluids.moltenCopshowium.still, (int) (displaySize * recipe.getMult())));
	}
}
