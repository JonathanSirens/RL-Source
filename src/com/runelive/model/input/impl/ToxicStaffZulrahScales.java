package com.runelive.model.input.impl;

import com.runelive.model.Item;
import com.runelive.model.input.EnterAmount;
import com.runelive.world.entity.impl.player.Player;

public class ToxicStaffZulrahScales extends EnterAmount {

  @Override
  public void handleAmount(Player player, int amount) {
    player.getPacketSender().sendInterfaceRemoval();
    if (player.getInventory().contains(21079)) {
      player.getInventory().delete(new Item(21079, 1));
      player.getInventory().add(new Item(21077, 1));
    }
    int amount_of_scales = amount;
    if (amount_of_scales + player.getToxicStaffCharges() > 11000) {
      amount_of_scales = 11000 - player.getToxicStaffCharges();
    }
    player.getInventory().delete(new Item(21080, amount_of_scales));
    player.addToxicStaffCharges(amount_of_scales);
    player.getPacketSender().sendMessage(
        "You have added " + amount_of_scales + " scales to your Toxic staff of the dead!");
  }
}
