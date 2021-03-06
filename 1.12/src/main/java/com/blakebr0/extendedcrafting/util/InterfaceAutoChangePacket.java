package com.blakebr0.extendedcrafting.util;

import com.blakebr0.extendedcrafting.tile.TileAutomationInterface;
import com.blakebr0.extendedcrafting.tile.TileCompressor;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.items.ItemStackHandler;

public class InterfaceAutoChangePacket implements IMessage {

	private long pos;
	private int mode;
	
	public InterfaceAutoChangePacket() {
		
	}

	public InterfaceAutoChangePacket(long pos, int mode) {
		this.pos = pos;
		this.mode = mode;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.pos = buf.readLong();
		this.mode = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong(this.pos);
		buf.writeInt(this.mode);
	}

	public static class Handler implements IMessageHandler<InterfaceAutoChangePacket, IMessage> {

		@Override
		public IMessage onMessage(InterfaceAutoChangePacket message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}

		private void handle(InterfaceAutoChangePacket message, MessageContext ctx) {
			TileEntity tile = ctx.getServerHandler().player.world.getTileEntity(BlockPos.fromLong(message.pos));
			if (tile instanceof TileAutomationInterface) {
				TileAutomationInterface machine = (TileAutomationInterface) tile;
				if (machine.hasTable()) {
					if (message.mode == 0) {
						machine.switchInserter();
					} else if (message.mode == 1) {
						machine.switchExtractor();
					}
				}
			}
		}
	}
}
