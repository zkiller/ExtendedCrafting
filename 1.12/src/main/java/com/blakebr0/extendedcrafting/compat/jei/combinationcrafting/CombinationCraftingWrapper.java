package com.blakebr0.extendedcrafting.compat.jei.combinationcrafting;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.blakebr0.cucumber.util.Utils;
import com.blakebr0.extendedcrafting.crafting.CombinationRecipe;

import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IStackHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class CombinationCraftingWrapper implements IRecipeWrapper {

	private IJeiHelpers helpers;
	private final CombinationRecipe recipe;

	public CombinationCraftingWrapper(IJeiHelpers helpers, CombinationRecipe recipe) {
		this.helpers = helpers;
		this.recipe = recipe;
	}

	public List<ItemStack> getInput() {
		return this.helpers.getStackHelper().toItemStackList(this.recipe.getInput());
	}

	@Override
	public List<String> getTooltipStrings(int mouseX, int mouseY) {
		if (mouseX > 1 && mouseX < 14 && mouseY > 9 && mouseY < 86) {
			return Arrays.<String> asList(Utils.format(this.recipe.getCost()) + " RF", Utils.format(this.recipe.getPerTick()) + " RF/t");
		}
		return Collections.emptyList();
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		IStackHelper helper = this.helpers.getStackHelper();
		ItemStack output = this.recipe.getOutput();

		List<List<ItemStack>> inputs = helper.expandRecipeItemStackInputs(this.recipe.getPedestalItems());
		ingredients.setInputLists(ItemStack.class, inputs);
		ingredients.setOutput(ItemStack.class, output);
	}
}