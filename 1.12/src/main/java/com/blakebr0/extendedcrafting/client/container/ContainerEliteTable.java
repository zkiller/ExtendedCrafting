package com.blakebr0.extendedcrafting.client.container;

import javax.annotation.Nullable;

import com.blakebr0.extendedcrafting.crafting.table.TableRecipeManager;
import com.blakebr0.extendedcrafting.crafting.table.elite.EliteCraftResult;
import com.blakebr0.extendedcrafting.crafting.table.elite.EliteCrafting;
import com.blakebr0.extendedcrafting.crafting.table.elite.EliteResultHandler;
import com.blakebr0.extendedcrafting.crafting.table.elite.EliteStackHandler;
import com.blakebr0.extendedcrafting.tile.TileEliteCraftingTable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerEliteTable extends Container {

	public InventoryCrafting matrix;
	public IInventory result;
	private TileEliteCraftingTable tile;
	private IItemHandler handler;

	public ContainerEliteTable(InventoryPlayer player, TileEliteCraftingTable tile, World world) {
		this.tile = tile;
		this.handler = tile.matrix;
		this.matrix = new EliteCrafting(this, tile);
		this.result = new EliteCraftResult(tile);
		this.addSlotToContainer(new EliteResultHandler(player.player, this.matrix, this.result, 0, 178, 68));
		int wy;
		int ex;

		for (wy = 0; wy < 7; ++wy) {
			for (ex = 0; ex < 7; ++ex) {
				this.addSlotToContainer(new SlotItemHandler(handler, ex + wy * 7, 14 + ex * 18, 15 + wy * 18));
			}
		}

		for (wy = 0; wy < 3; ++wy) {
			for (ex = 0; ex < 9; ++ex) {
				this.addSlotToContainer(new Slot(player, ex + wy * 9 + 9, 26 + ex * 18, 155 + wy * 18));
			}
		}

		for (ex = 0; ex < 9; ++ex) {
			this.addSlotToContainer(new Slot(player, ex, 26 + ex * 18, 213));
		}

		this.onCraftMatrixChanged(this.matrix);
		((EliteStackHandler) handler).crafting = matrix;
	}

	public void onCraftMatrixChanged(IInventory matrix) {
		this.result.setInventorySlotContents(0,
				TableRecipeManager.getInstance().findMatchingRecipe(this.matrix, this.tile.getWorld()));
		this.tile.markDirty();
	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);

	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return this.tile.isUseableByPlayer(player);
	}

	@Nullable
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotNumber) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(slotNumber);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (slotNumber == 0) {
				if (!this.mergeItemStack(itemstack1, 50, 86, false)) {
					return ItemStack.EMPTY;
				}

				slot.onSlotChange(itemstack1, itemstack);
			} else if (slotNumber >= 50 && slotNumber < 77) {
				if (!this.mergeItemStack(itemstack1, 77, 86, false)) {
					return ItemStack.EMPTY;
				}
			} else if (slotNumber >= 77 && slotNumber < 86) {
				if (!this.mergeItemStack(itemstack1, 50, 77, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.mergeItemStack(itemstack1, 50, 86, false)) {
				return ItemStack.EMPTY;
			}

			if (itemstack1.getCount() == 0) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}

			if (itemstack1.getCount() == itemstack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(player, itemstack1);
			this.onCraftMatrixChanged(matrix);
		}

		return itemstack;
	}
}