package com.ikov.model.input.impl;

import com.ikov.model.input.EnterAmount;
import com.ikov.world.World;
import com.ikov.world.entity.impl.player.Player;

public class EnterAmountToDiceOther extends EnterAmount {


  public EnterAmountToDiceOther(int item, int slot) {
    super(item, slot);
  }

  @Override
  public void handleAmount(Player player, int amount) {
    if (amount > 100) {
      player.getPacketSender().sendMessage("You can't roll over 100.");
      return;
    }
    if (amount < 0) {
      player.getPacketSender().sendMessage("You can't roll under 0.");
      return;
    }
    Player other = World.getPlayerByName(player.dice_other_name);
    other.dice_other_amount = amount;
    other.dice_other = true;
    player.getPacketSender()
        .sendMessage("The player <col=ff0000><shad=0>" + player.dice_other_name
            + "</col></shad> will roll a <col=ff0000><shad=0>" + amount
            + "</col></shad> on their next roll.");
  }
}
