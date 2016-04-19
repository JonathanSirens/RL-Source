package com.runelive.model.input.impl;

import com.runelive.model.input.EnterAmount;
import com.runelive.world.content.skill.impl.crafting.LeatherMaking;
import com.runelive.world.content.skill.impl.crafting.leatherData;
import com.runelive.world.entity.impl.player.Player;

public class EnterAmountOfLeatherToCraft extends EnterAmount {

  @Override
  public void handleAmount(Player player, int amount) {
    for (final leatherData l : leatherData.values()) {
      if (player.getSelectedSkillingItem() == l.getLeather()) {
        LeatherMaking.craftLeather(player, l, amount);
        break;
      }
    }
  }
}
