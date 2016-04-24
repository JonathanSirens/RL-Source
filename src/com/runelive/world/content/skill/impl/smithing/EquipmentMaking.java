package com.runelive.world.content.skill.impl.smithing;

import com.runelive.engine.task.Task;
import com.runelive.engine.task.TaskManager;
import com.runelive.model.Animation;
import com.runelive.model.Graphic;
import com.runelive.model.Item;
import com.runelive.model.Skill;
import com.runelive.model.definitions.ItemDefinition;
import com.runelive.world.content.Sounds;
import com.runelive.world.content.Sounds.Sound;
import com.runelive.world.entity.impl.player.Player;

public class EquipmentMaking {

  public static void handleAnvil(Player player) {
    String bar = searchForBars(player);
    if (bar == null) {
      player.getPacketSender().sendMessage("You do not have any bars in your inventory to smith.");
      return;
    } else {
      switch (bar.toLowerCase()) {
        case "bronze bar":
          player.setSelectedSkillingItem(2349);
          SmithingData.showBronzeInterface(player);
          break;
        case "iron bar":
          player.setSelectedSkillingItem(2351);
          SmithingData.makeIronInterface(player);
          break;
        case "steel bar":
          player.setSelectedSkillingItem(2353);
          SmithingData.makeSteelInterface(player);
          break;
        case "mithril bar":
          player.setSelectedSkillingItem(2359);
          SmithingData.makeMithInterface(player);
          break;
        case "adamant bar":
          player.setSelectedSkillingItem(2361);
          SmithingData.makeAddyInterface(player);
          break;
        case "rune bar":
          player.setSelectedSkillingItem(2363);
          SmithingData.makeRuneInterface(player);
          break;
      }
    }
  }

  public static String searchForBars(Player player) {
    for (int bar : SmithingData.SMELT_BARS) {
      if (player.getInventory().contains(bar)) {
        return ItemDefinition.forId(bar).getName();
      }
    }
    return null;
  }

  public static void smithItem(final Player player, final Item bar, final Item itemToSmith,
      final int x) {
    if (bar.getId() < 0)
      return;
    player.getSkillManager().stopSkilling();
    if (!player.getInventory().contains(2347)) {
      player.getPacketSender().sendMessage("You need a Hammer to smith items.");
      player.getPacketSender().sendInterfaceRemoval();
      return;
    }
    if (player.getInventory().getAmount(bar.getId()) < bar.getAmount() || x <= 0) {
      player.getPacketSender().sendMessage("You do not have enough bars to smith this item.");
      return;
    }
    if (SmithingData.getData(itemToSmith, "reqLvl") > player.getSkillManager()
        .getCurrentLevel(Skill.SMITHING)) {
      player.getPacketSender().sendMessage("You need a Smithing level of at least "
          + SmithingData.getData(itemToSmith, "reqLvl") + " to make this item.");
      return;
    }
    player.getPacketSender().sendInterfaceRemoval();
    player.setCurrentTask(new Task(3, player, true) {
      int amountMade = 0;

      @Override
      public void execute() {
        if (player.getInventory().getAmount(bar.getId()) < bar.getAmount()
            || !player.getInventory().contains(2347) || amountMade >= x) {
          this.stop();
          return;
        }
        if (player.getInteractingObject() != null)
          player.getInteractingObject().performGraphic(new Graphic(2123));
        player.performAnimation(new Animation(898));
        amountMade++;
        Sounds.sendSound(player, Sound.SMITH_ITEM);
        player.getInventory().delete(bar);
        player.getInventory().add(itemToSmith);
        player.getInventory().refreshItems();
        player.getSkillManager().addExperience(Skill.SMITHING,
            (int) (SmithingData.getData(itemToSmith, "xp")));
      }
    });
    TaskManager.submit(player.getCurrentTask());
  }
}
