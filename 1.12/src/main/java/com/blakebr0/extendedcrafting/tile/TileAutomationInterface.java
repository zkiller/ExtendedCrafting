package com.blakebr0.extendedcrafting.tile;

import java.util.Locale;

import javax.annotation.Nonnull;

import com.blakebr0.cucumber.helper.StackHelper;
import com.blakebr0.cucumber.lib.CustomEnergyStorage;
import com.blakebr0.cucumber.util.Utils;
import com.blakebr0.extendedcrafting.config.ModConfig;
import com.blakebr0.extendedcrafting.lib.FakeRecipeHandler;
import com.blakebr0.extendedcrafting.lib.IExtendedTable;
import com.blakebr0.extendedcrafting.util.VanillaPacketDispatcher;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

public class TileAutomationInterface extends TileEntity implements ITickable, ISidedInventory {
	
	private final ItemStackHandler inventory = new StackHandler(2);
	private final ItemStackHandler recipe = new FakeRecipeHandler();
	private final CustomEnergyStorage energy = new CustomEnergyStorage(ModConfig.confInterfaceRFCapacity);
	private int oldEnergy;
	private ItemStack result = ItemStack.EMPTY;
	private boolean hasRecipe = false;
	public int autoInsert = -1;
	public int autoExtract = -1;
	private int ticks = 0;
		
	public IItemHandlerModifiable getInventory() {
		return inventory;
	}
	
	public ItemStackHandler getRecipe() {
		return recipe;
	}
	
	public ItemStack getResult() {
		return result;
	}
	
	public CustomEnergyStorage getEnergy() {
		return energy;
	}
	
	@Override
	public void update() {
		boolean mark = false;
		ticks++;
		if (!this.getWorld().isRemote) {
			ItemStack input = this.getInventory().getStackInSlot(0);
			ItemStack output = this.getInventory().getStackInSlot(1);
			if (this.hasTable()) {
				if (!input.isEmpty()) {
					if (this.hasRecipe() && this.getEnergy().getEnergyStored() >= ModConfig.confInterfaceRFRate) {
						this.handleInput(input);
					}
				}
				
				if (this.hasRecipe() && this.getEnergy().getEnergyStored() >= ModConfig.confInterfaceRFRate && ticks % 10 == 0) {
					this.handleOutput(output);
				}
			}
			
			if (this.getInserterFace() != null && this.getEnergy().getEnergyStored() >= ModConfig.confInterfaceRFRate && ticks % 4 == 0) {
				TileEntity tile = this.getWorld().getTileEntity(this.getPos().offset(this.getInserterFace()));
				if (tile != null && tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN)) {
					IItemHandler handler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN);
					for (int i = 0; i < handler.getSlots(); i++) {
						ItemStack stack = handler.getStackInSlot(i);
						if (!stack.isEmpty() && ((FakeRecipeHandler) this.getRecipe()).getStacks().stream().anyMatch(s -> s.isItemEqual(stack))) {
							if (input.isEmpty() || (input.isItemEqual(stack) && input.getCount() < input.getMaxStackSize())) {
								ItemStack toInsert = stack.copy(); toInsert.setCount(1);
								this.getInventory().insertItem(0, toInsert, false);
								handler.extractItem(i, 1, false);
								this.getEnergy().extractEnergy(ModConfig.confInterfaceRFRate, false);
								break;
							}
						}
					}
				}
			}
			
			if (this.getExtractorFace() != null && this.getEnergy().getEnergyStored() >= ModConfig.confInterfaceRFRate && ticks % 4 == 0) {
				TileEntity tile = this.getWorld().getTileEntity(this.getPos().offset(this.getExtractorFace()));
				if (tile != null && tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP)) {
					IItemHandler handler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
					for (int i = 0; i < handler.getSlots(); i++) {
						ItemStack stack = handler.getStackInSlot(i);
						if (!output.isEmpty()) {
							if (stack.isEmpty() || (stack.isItemEqual(output) && stack.getCount() < stack.getMaxStackSize())) {
								ItemStack toInsert = output.copy(); toInsert.setCount(1);
								handler.insertItem(i, toInsert, false);
								output.shrink(1);
								this.getEnergy().extractEnergy(ModConfig.confInterfaceRFRate, false);
								break;
							}
						}
					}
				}
			}
		}
		
		if (this.oldEnergy != this.energy.getEnergyStored()) {
			this.oldEnergy = this.energy.getEnergyStored();
			if (!mark) {
				mark = true;
			}
		}
		
		if (ticks > 100) {
			ticks = 0;
		}
		
		if (mark) {
			this.markDirty();
		}
	}
	
	private void handleInput(ItemStack input) {
		IExtendedTable table = this.getTable();
		IItemHandlerModifiable matrix = table.getMatrix();
		ItemStackHandler recipe = this.getRecipe();
		
		int slotToPut = -1;
		ItemStack stackToPut = ItemStack.EMPTY;
		for (int i = 0; i < matrix.getSlots(); i++) {
			ItemStack slot = matrix.getStackInSlot(i);
			ItemStack recipeStack = recipe.getStackInSlot(i);
			if ((slot.isEmpty() || slot.isItemEqual(input)) && input.isItemEqual(recipeStack)) {
				if (slot.isEmpty() || slot.getCount() < slot.getMaxStackSize()) {
					if (slot.isEmpty()) {
						slotToPut = i;
						break;
					} else if (stackToPut.isEmpty() || (!stackToPut.isEmpty() && slot.getCount() < stackToPut.getCount())) {
						slotToPut = i;
						stackToPut = slot.copy();
					}
				}
			}
		}
		
		if (slotToPut > -1) {
			ItemStack toInsert = input.copy(); toInsert.setCount(1);
			matrix.insertItem(slotToPut, toInsert, false);
			input.shrink(1);
			this.getEnergy().extractEnergy(ModConfig.confInterfaceRFRate, false);
		}
	}
	
	public void handleOutput(ItemStack output) {
		ItemStack result = this.getTable().getResult();
		IItemHandlerModifiable matrix = this.getTable().getMatrix();
		if (!result.isEmpty() && (output.isEmpty() || (output.isItemEqual(result) && output.getCount() < result.getMaxStackSize()))) {				
			if (this.getEnergy().getEnergyStored() >= ModConfig.confInterfaceRFRate) {
				ItemStack toInsert = result.copy(); toInsert.setCount(1);
				result.shrink(1);
				for (int i = 0; i < matrix.getSlots(); i++) {
					ItemStack slotStack = matrix.getStackInSlot(i);
					if (!slotStack.isEmpty()) {
						if (slotStack.getItem().hasContainerItem(slotStack) && slotStack.getCount() == 1) {
							matrix.setStackInSlot(i, slotStack.getItem().getContainerItem(slotStack));
						} else {
							matrix.setStackInSlot(i, StackHelper.decrease(slotStack.copy(), 1, false));
						}
					}
				}
				this.getInventory().insertItem(1, toInsert, false);
				this.getEnergy().extractEnergy(ModConfig.confInterfaceRFRate, false);
			}
		}
	}
	
	public IExtendedTable getTable() {
		TileEntity tile = this.getWorld().getTileEntity(this.getPos().down());
		return tile != null && tile instanceof IExtendedTable ? (IExtendedTable) tile : null;
	}
	
	public boolean hasTable() {
		return getTable() != null;
	}
	
	public boolean hasRecipe() {
		return this.hasRecipe;
	}
	
	public void setHasRecipe(boolean hasRecipe) {
		this.hasRecipe = hasRecipe;
	}
	
	public void saveRecipe() {
		ItemStackHandler recipe = this.getRecipe();
		IItemHandlerModifiable matrix = this.getTable().getMatrix();
		recipe.setSize(matrix.getSlots());
		for (int i = 0; i < matrix.getSlots(); i++) {
			recipe.setStackInSlot(i, matrix.getStackInSlot(i).copy());
		}
		ItemStack result = this.getTable().getResult();
		if (result != null) {
			this.result = result;
		}
		this.setHasRecipe(true);
		this.markDirty();
	}
	
	public void clearRecipe() {
		ItemStackHandler recipe = this.getRecipe();
		recipe.setSize(1);
		this.result = ItemStack.EMPTY;
		this.setHasRecipe(false);
		this.markDirty();
	}
	
	public EnumFacing getInserterFace() {
		return this.autoInsert > -1 && this.autoInsert < EnumFacing.values().length ? EnumFacing.values()[this.autoInsert] : null;
	}
	
	public EnumFacing getExtractorFace() {
		return this.autoExtract > -1 && this.autoExtract < EnumFacing.values().length ? EnumFacing.values()[this.autoExtract] : null;
	}
	
	public String getInserterFaceName() {
		return this.getInserterFace() != null ? this.getInserterFace().getName().toUpperCase(Locale.ROOT) : Utils.localize("ec.interface.none");
	}
	
	public String getExtractorFaceName() {
		return this.getExtractorFace() != null ? this.getExtractorFace().getName().toUpperCase(Locale.ROOT) : Utils.localize("ec.interface.none");
	}
	
	public void switchInserter() {
		if (this.autoInsert >= EnumFacing.values().length - 1) {
			this.autoInsert = -1;
		} else {
			this.autoInsert++;
			if (this.autoInsert == EnumFacing.DOWN.getIndex()) {
				this.autoInsert++;
			}
		}
		this.markDirty();
	}
	
	public void switchExtractor() {
		if (this.autoExtract >= EnumFacing.values().length - 1) {
			this.autoExtract = -1;
		} else {
			this.autoExtract++;
			if (this.autoExtract == EnumFacing.DOWN.getIndex()) {
				this.autoExtract++;
			}
		}
		this.markDirty();
	}
		
	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		tag = super.writeToNBT(tag);
		tag.merge(inventory.serializeNBT());
		tag.merge(recipe.serializeNBT());
		tag.setInteger("Energy", this.energy.getEnergyStored());
		tag.setTag("Result", this.result.serializeNBT());
		tag.setBoolean("HasRecipe", this.hasRecipe);
		tag.setInteger("AutoInsert", this.autoInsert);
		tag.setInteger("AutoExtract", this.autoExtract);
		return tag;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		inventory.deserializeNBT(tag);
		recipe.deserializeNBT(tag);
		energy.setEnergy(tag.getInteger("Energy"));
		this.result = new ItemStack(tag.getCompoundTag("Result"));
		this.hasRecipe = tag.getBoolean("HasRecipe");
		this.autoInsert = tag.getInteger("AutoInsert");
		this.autoExtract = tag.getInteger("AutoExtract");
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, -1, this.getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager manager, SPacketUpdateTileEntity packet) {
		this.readFromNBT(packet.getNbtCompound());
	}

	@Override
	public final NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound());
	}

	@Override
	public void markDirty() {
		super.markDirty();
		VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
	}

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nonnull EnumFacing side) {
		return this.getCapability(capability, side) != null;
	}

	@Nonnull
	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nonnull EnumFacing side) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return (T) new SidedInvWrapper(this, side);
		} else if (capability == CapabilityEnergy.ENERGY) {
			return CapabilityEnergy.ENERGY.cast(energy);
		}
		return super.getCapability(capability, side);
	}
	
	public boolean isUseableByPlayer(EntityPlayer player) {
		return this.getWorld().getTileEntity(this.getPos()) == this && player.getDistanceSq(this.getPos().add(0.5, 0.5, 0.5)) <= 64;
	}
	
	class StackHandler extends ItemStackHandler {
				
		StackHandler(int size) {
			super(size);
		}

		@Override
		public void onContentsChanged(int slot) {
			TileAutomationInterface.this.markDirty();
		}
	}

	@Override
	public int getSizeInventory() {
		return inventory.getSlots();
	}

	@Override
	public boolean isEmpty() {
		for (int i = 0; i < inventory.getSlots(); i++) {
			if (!inventory.getStackInSlot(i).isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		return inventory.getStackInSlot(index);
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		return index >= 0 && index < inventory.getSlots() && !inventory.getStackInSlot(index).isEmpty() && count > 0 ? inventory.getStackInSlot(index).splitStack(count) : ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		if (index >= 0 && index < inventory.getSlots()) {
			inventory.setStackInSlot(index, ItemStack.EMPTY);
			return ItemStack.EMPTY;
		} else {
			return ItemStack.EMPTY;
		}
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		ItemStack itemstack = (ItemStack) this.inventory.getStackInSlot(index);
		boolean flag = !stack.isEmpty() && stack.isItemEqual(itemstack) && ItemStack.areItemStackTagsEqual(stack, itemstack);
		this.inventory.setStackInSlot(index, stack);

		if (stack.getCount() > this.getInventoryStackLimit()) {
			stack.setCount(this.getInventoryStackLimit());
		}

		if (index == 0 && !flag) {
			this.markDirty();
		}		
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		return this.isUseableByPlayer(player);
	}

	@Override
	public void openInventory(EntityPlayer player) {
		
	}

	@Override
	public void closeInventory(EntityPlayer player) {
		
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return true;
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {
		
	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {
		
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		return new int[] { 0, 1 };
	}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
		return index == 0;
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		return index == 1;
	}
}
