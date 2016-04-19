package com.runelive.world.content.combat.strategy.impl;

import com.runelive.model.Animation;
import com.runelive.model.Locations.Location;
import com.runelive.model.definitions.WeaponAnimations;
import com.runelive.model.definitions.WeaponInterfaces.WeaponInterface;
import com.runelive.world.content.combat.CombatContainer;
import com.runelive.world.content.combat.CombatType;
import com.runelive.world.content.combat.strategy.CombatStrategy;
import com.runelive.world.clip.region.RegionClipping;
import com.runelive.world.content.minigames.impl.Dueling;
import com.runelive.world.content.minigames.impl.Dueling.DuelRule;
import com.runelive.world.entity.impl.Character;
import com.runelive.world.entity.impl.npc.NPC;
import com.runelive.world.entity.impl.player.Player;

/**
 * The default combat strategy assigned to an {@link Character} during a melee based combat session.
 * This is the combat strategy used by all {@link Npc}s by default.
 * 
 * @author lare96
 */
public class DefaultMeleeCombatStrategy implements CombatStrategy {

  @Override
  public boolean canAttack(Character entity, Character victim) {


    if (entity.isPlayer()) {
      Player player = (Player) entity;
      if (Dueling.checkRule(player, DuelRule.NO_MELEE)) {
        player.getPacketSender().sendMessage("Melee-attacks have been turned off in this duel!");
        player.getCombatBuilder().reset(true);
        return false;
      }
    }
	if(victim.isPlayer()) {
		Player target = (Player) victim;
		if(target.getLocation() != Location.DUEL_ARENA) {
			if (RegionClipping.getClipping(target.getPosition().getX(), target.getPosition().getY(), target.getPosition().getY()) != 0) {
				return false;
			}
		}
	}
    if (entity.isPlayer()) {
      Player player = (Player) entity;
      if (victim.isNpc()) {
        if (player.getLocation() == Location.ZULRAH_PIT) {
          if (player.getZulrahRotating() || player.getZulrahRotatingProcess()) {
            return false;
          }
        }
      }
    }

    return true;
  }

  @Override
  public CombatContainer attack(Character entity, Character victim) {

    // Start the performAnimation for this attack.
    startAnimation(entity);

    // Create the combat container for this hook.
    return new CombatContainer(entity, victim, 1, CombatType.MELEE, true);
  }

  @Override
  public int attackDelay(Character entity) {

    // The attack speed for the weapon being used.
    return entity.getAttackSpeed();
  }

  @Override
  public int attackDistance(Character entity) {

    // The default distance for all npcs using melee is 1.
    if (entity.isNpc()) {
      return ((NPC) entity).getDefinition().getSize();
    }

    // The default distance for all players is 1, or 2 if they are using a
    // halberd.
    Player player = (Player) entity;
    if (player.getWeapon() == WeaponInterface.HALBERD) {
      return 2;
    }
    return 1;
  }

  /**
   * Starts the performAnimation for the argued entity in the current combat hook.
   * 
   * @param entity the entity to start the performAnimation for.
   */
  private void startAnimation(Character entity) {
    if (entity.isNpc()) {
      NPC npc = (NPC) entity;
      npc.performAnimation(new Animation(npc.getDefinition().getAttackAnimation()));
    } else if (entity.isPlayer()) {
      Player player = (Player) entity;
      if (!player.isSpecialActivated()) {
        player.performAnimation(new Animation(WeaponAnimations.getAttackAnimation(player)));
      } else {
        player.performAnimation(new Animation(player.getFightType().getAnimation()));
      }
    }
  }

  @Override
  public boolean customContainerAttack(Character entity, Character victim) {
    return false;
  }

  @Override
  public CombatType getCombatType() {
    return CombatType.MELEE;
  }
}
