package com.blakebr0.extendedcrafting.block;

import java.util.List;

import com.blakebr0.cucumber.block.BlockBase;
import com.blakebr0.cucumber.iface.IModelHelper;
import com.blakebr0.cucumber.util.Utils;
import com.blakebr0.extendedcrafting.ExtendedCrafting;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;

public class BlockTrimmed extends BlockBase implements IModelHelper {

	public static final PropertyEnum<Type> VARIANT = PropertyEnum.<Type> create("variant", Type.class);

	public BlockTrimmed() {
		super("ec.trimmed", Material.IRON, SoundType.METAL, 5.0F, 10.0F);
		this.setCreativeTab(ExtendedCrafting.tabExtendedCrafting);
		this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, Type.IRON_TRIMMED));
	}

	@Override
	public void init() {

	}

	@Override
	public int damageDropped(IBlockState state) {
		return ((Type) state.getValue(VARIANT)).getMetadata();
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> stacks) {
		for (Type type : Type.values()) {
			stacks.add(new ItemStack(this, 1, type.getMetadata()));
		}
	}

	@Override
	public void initModels() {
		for (Type type : Type.values()) {
			ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), type.getMetadata(), new ModelResourceLocation(getRegistryName().toString() + "_" + type.byMetadata(type.getMetadata()).getName()));
		}
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(VARIANT, Type.byMetadata(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return ((Type) state.getValue(VARIANT)).getMetadata();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { VARIANT });
	}
	
	@Override
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
		int meta = stack.getMetadata();
		if (meta < 6) {
			tooltip.add(Utils.localize("tooltip.ec.trimmed_" + Type.byMetadata(meta).getName()));
		}
	}

	public static enum Type implements IStringSerializable {

		IRON_TRIMMED(0, "iron"),
		GOLD_TRIMMED(1, "gold"),
		DIAMOND_TRIMMED(2, "diamond"),
		EMERALD_TRIMMED(3, "emerald"),
		CRYSTALTINE_TRIMMED(4, "crystaltine"),
		ULTIMATE_TRIMMED(5, "ultimate");

		private static final Type[] META_LOOKUP = new Type[values().length];
		private final int meta;
		private final String name;

		Type(int meta, String name) {
			this.meta = meta;
			this.name = name;
		}

		public int getMetadata() {
			return this.meta;
		}

		@Override
		public String getName() {
			return this.name;
		}

		public static Type byMetadata(int meta) {
			if (meta < 0 || meta >= META_LOOKUP.length) {
				meta = 0;
			}
			return META_LOOKUP[meta];
		}

		static {
			for (Type type : values()) {
				META_LOOKUP[type.getMetadata()] = type;
			}
		}
	}
}