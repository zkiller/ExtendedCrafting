package com.blakebr0.extendedcrafting.render;

import com.blakebr0.extendedcrafting.tile.TileCraftingCore;
import com.blakebr0.extendedcrafting.tile.TilePedestal;

import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ModRenders {
	
	public static void init(){
		ClientRegistry.bindTileEntitySpecialRenderer(TilePedestal.class, new RenderPedestal());
		ClientRegistry.bindTileEntitySpecialRenderer(TileCraftingCore.class, new RenderCraftingCore());
	}
}
