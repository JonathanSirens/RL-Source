package com.ikov.model.input.impl;

import com.ikov.model.input.EnterAmount;
import com.ikov.model.input.Input;
import com.ikov.world.content.pos.PlayerOwnedShops;
import com.ikov.world.entity.impl.player.Player;

public class EnterAmountOfPosOfferPrice extends EnterAmount {

	private int item_amount;
	private int slot;

	public EnterAmountOfPosOfferPrice(int slot) {
		this.slot = slot;
	}

	public EnterAmountOfPosOfferPrice(int slot, int item_amount) {
		this.slot = slot;
		this.item_amount = item_amount;
	}

	@Override
	public void handleAmount(Player player, int price) {
		if (price <= 0 || price >= Integer.MAX_VALUE) {
			player.getPacketSender().sendMessage("Please enter a valid price.");
			return;
		}
		player.getPlayerOwnedShop().sellItem(player, getSlot(), getItemAmount(), price);
	}

	public int getItemAmount() {
		return item_amount;
	}

	public int getSlot() {
		return slot;
	}

}