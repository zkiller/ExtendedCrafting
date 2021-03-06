package com.blakebr0.extendedcrafting.crafting;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class CompressorRecipeManager {

	private static final CompressorRecipeManager INSTANCE = new CompressorRecipeManager();

	private ArrayList<CompressorRecipe> recipes = new ArrayList<CompressorRecipe>();

	public static final CompressorRecipeManager getInstance() {
		return INSTANCE;
	}
	
	public void addRecipe(ItemStack output, Object input, int inputCount, ItemStack catalyst, boolean consumeCatalyst, int powerCost) {
		recipes.add(new CompressorRecipe(output, input, inputCount, catalyst, consumeCatalyst, powerCost));
	}

	public void addRecipe(ItemStack output, Object input, int inputCount, ItemStack catalyst, boolean consumeCatalyst, int powerCost, int powerRate) {
		recipes.add(new CompressorRecipe(output, input, inputCount, catalyst, consumeCatalyst, powerCost, powerRate));
	}

	public ArrayList<CompressorRecipe> getRecipes() {
		return recipes;
	}
	
	public ArrayList<CompressorRecipe> getValidRecipes() {
		ArrayList recipes = new ArrayList<>();
		for (CompressorRecipe recipe : getRecipes()) {
			if (recipe.getInput() instanceof NonNullList<?> && ((NonNullList<?>) recipe.getInput()).isEmpty()) {
				continue;
			} else {
				recipes.add(recipe);
			}
		}
		return recipes;
	}
	
	public void removeRecipe(ItemStack stack) {
		for (CompressorRecipe recipe : getRecipes()) {
			if (recipe.getOutput().isItemEqual(stack)) {
				this.recipes.remove(recipe);
				break;
			}
		}
	}
}
