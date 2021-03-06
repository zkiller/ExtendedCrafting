package com.blakebr0.extendedcrafting.client.container;

import com.blakebr0.extendedcrafting.crafting.CombinationRecipe;
import com.blakebr0.extendedcrafting.tile.TileCraftingCore;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class ContainerCraftingCore extends Container {
	
	public CombinationRecipe recipe;
	private TileCraftingCore tile;

    public ContainerCraftingCore(InventoryPlayer player, TileCraftingCore tile){
    	this.tile = tile;
    	this.recipe = tile.getActiveRecipe();
	
        for(int i = 0; i < 3; ++i){
            for(int j = 0; j < 9; ++j){
                this.addSlotToContainer(new Slot(player, j + i * 9 + 9, 8 + j * 18, 102 + i * 18));
            }
        }

        for(int i = 0; i < 9; ++i){
            this.addSlotToContainer(new Slot(player, i, 8 + i * 18, 160));
        }
    }
    
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotNumber) {
        ItemStack itemstack = null;
        Slot slot = this.inventorySlots.get(slotNumber);

        if(slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if(slotNumber >= 0 && slotNumber < 27) {
                if (!this.mergeItemStack(itemstack1, 27, 36, false)) {
                    return null;
                }
            } else if(slotNumber >= 27 && slotNumber < 36) {
                if(!this.mergeItemStack(itemstack1, 0, 27, false)) {
                    return null;
                }
            } else if(!this.mergeItemStack(itemstack1, 0, 27, false)) {
                return null;
            }

            if (itemstack1.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }

            if (itemstack1.stackSize == itemstack.stackSize) {
                return null;
            }

            slot.onPickupFromSlot(player, itemstack1);
        }

        return itemstack;
    }
	
	@Override
    public boolean canInteractWith(EntityPlayer player){
        return this.tile.isUseableByPlayer(player);
    }

}
