package com.runelive.world.content;

import com.runelive.model.Animation;
import com.runelive.model.Flag;
import com.runelive.model.Skill;
import com.runelive.world.content.Emotes.Skillcape_Data;
import com.runelive.world.entity.impl.player.Player;

/**
 * Handles a player's run energy
 * 
 * @author Gabriel Hannason Thanks to Russian for formula!
 */
public class EnergyHandler {

	public static void processPlayerEnergy(Player p) {
		if (p.isRunning() && p.getMovementQueue().isMoving()) {
			int energy = p.getRunEnergy();
			if (energy > 0) {
				if (!Skillcape_Data.AGILITY.isWearingCape(p) || !Skillcape_Data.MASTER_AGILITY.isWearingCape(p)) {
					energy = (energy - 1);
					p.setRunEnergy(energy);
					p.getPacketSender().sendRunEnergy(energy);
				}
			} else {
				p.setRunning(false);
				p.getPacketSender().sendRunStatus();
				p.getPacketSender().sendRunEnergy(0);
			}
		}
		if (p.getRunEnergy() < 100) {
			if (System.currentTimeMillis() >= (restoreEnergyFormula(p) + p.getLastRunRecovery().getTime())) {
				int energy = p.getRunEnergy() + 1;
				p.setRunEnergy(energy);
				p.getPacketSender().sendRunEnergy(energy);
				p.getLastRunRecovery().reset();
			}
		}
	}

	public static void rest(Player player) {
		if (player.busy() || player.getCombatBuilder().isBeingAttacked() || player.getCombatBuilder().isAttacking()) {
			player.getPacketSender().sendMessage("You cannot do this right now.");
			return;
		}
		player.getMovementQueue().reset();
		player.setResting(true);

		player.performAnimation(new Animation(11786));
		player.getCharacterAnimations().setStandingAnimation(2034);
		player.getUpdateFlag().flag(Flag.APPEARANCE);
		player.getPacketSender().sendMessage("You begin to rest...");
	}

	public static double restoreEnergyFormula(Player p) {
		return 2260 - (p.getSkillManager().getCurrentLevel(Skill.AGILITY) * (p.isResting() ? 13000 : 10));
	}
}
