package com.runelive.model.input.impl;

import com.runelive.model.Item;
import com.runelive.model.container.impl.PriceChecker;
import com.runelive.model.input.EnterAmount;
import com.runelive.world.entity.impl.player.Player;

public class EnterAmountToRemoveFromPriceCheck extends EnterAmount {

	public EnterAmountToRemoveFromPriceCheck(int item, int slot) {
		super(item, slot);
	}

	@Override
	public void handleAmount(Player player, long value) {
		int amount = (int) value;
		if (value > Integer.MAX_VALUE) {
			amount = Integer.MAX_VALUE;
		}
		if (!player.getPriceChecker().isOpen() || player.getInterfaceId() != PriceChecker.INTERFACE_ID)
			return;
		if (!player.getPriceChecker().contains(getItem()))
			return;
		int invAmount = player.getPriceChecker().getAmount(getItem());
		if (amount > invAmount)
			amount = invAmount;
		if (amount <= 0)
			return;
		player.getPriceChecker().switchItem(player.getInventory(), new Item(getItem(), amount),
				player.getPriceChecker().getSlot(getItem()), false, true);
	}
}
