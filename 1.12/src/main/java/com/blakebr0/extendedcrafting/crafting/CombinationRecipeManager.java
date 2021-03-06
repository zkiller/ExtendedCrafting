package com.blakebr0.extendedcrafting.crafting;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;

public class CombinationRecipeManager {

	private static final CombinationRecipeManager INSTANCE = new CombinationRecipeManager();

	private ArrayList<CombinationRecipe> recipes = new ArrayList<CombinationRecipe>();

	public static final CombinationRecipeManager getInstance() {
		return INSTANCE;
	}
	
	public void addRecipe(ItemStack output, int cost, ItemStack input, Object... pedestals) {
		recipes.add(new CombinationRecipe(output, cost, input, pedestals));
	}

	public void addRecipe(ItemStack output, int cost, int perTick, ItemStack input, Object... pedestals) {
		recipes.add(new CombinationRecipe(output, cost, perTick, input, pedestals));
	}

	public ArrayList<CombinationRecipe> getRecipes() {
		return recipes;
	}
	
	public void removeRecipe(ItemStack stack) {
		for (CombinationRecipe recipe : getRecipes()) {
			if (recipe.getOutput().isItemEqual(stack)) {
				this.recipes.remove(recipe);
				break;
			}
		}
	}
}
