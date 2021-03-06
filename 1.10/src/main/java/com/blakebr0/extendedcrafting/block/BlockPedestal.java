package com.blakebr0.extendedcrafting.block;

import com.blakebr0.extendedcrafting.tile.TilePedestal;
import com.blakebr0.extendedcrafting.util.StackHelper;
import com.blakebr0.extendedcrafting.util.VanillaPacketDispatcher;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockPedestal extends BlockBase implements ITileEntityProvider {

	public static final AxisAlignedBB AABB = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 1.0D, 0.9375D);
	
	public BlockPedestal(){
		super("pedestal", Material.IRON, SoundType.METAL, 5.0F, 10.0F);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack held, EnumFacing side, float hitX, float hitY, float hitZ) {
		if(!world.isRemote){
			TilePedestal tile = (TilePedestal)world.getTileEntity(pos);
			if(tile != null){
				if(StackHelper.isNull(tile.getInventory().getStackInSlot(0))){
					if(!StackHelper.isNull(held)){
						tile.getInventory().setStackInSlot(0, StackHelper.withSize(held.copy(), 1, false));
						player.setHeldItem(hand, StackHelper.decrease(held, 1, false));
						world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1.0F, 1.0F);
					}
				} else {
					EntityItem item = new EntityItem(world, player.posX, player.posY, player.posZ, tile.getInventory().getStackInSlot(0));
					item.setNoPickupDelay();
					world.spawnEntityInWorld(item);
					tile.getInventory().setStackInSlot(0, StackHelper.getNull());
				}
			}
		}
		return true;
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state){
		TilePedestal tile = (TilePedestal)world.getTileEntity(pos);
        if(tile instanceof TilePedestal){
        	ItemStack stack = tile.getInventory().getStackInSlot(0);
        	if(!StackHelper.isNull(stack)){
        		EntityItem item = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), stack);
        		world.spawnEntityInWorld(item);
        	}
        }
		super.breakBlock(world, pos, state);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta){
		return new TilePedestal();
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos){
		return AABB;
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World worldIn, BlockPos pos) {
		return FULL_BLOCK_AABB;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
}
