package com.chaos.world.content.skill.impl.firemaking;

import com.chaos.engine.task.Task;
import com.chaos.engine.task.TaskManager;
import com.chaos.model.Animation;
import com.chaos.model.GameObject;
import com.chaos.model.Position;
import com.chaos.model.Skill;
import com.chaos.model.movement.WalkingQueue;
import com.chaos.util.Misc;
import com.chaos.world.content.Achievements;
import com.chaos.world.content.Achievements.AchievementData;
import com.chaos.world.content.CustomObjects;
import com.chaos.world.content.Sounds;
import com.chaos.world.content.Sounds.Sound;
import com.chaos.world.content.skill.impl.dungeoneering.Dungeoneering;
import com.chaos.world.entity.impl.player.Player;

/**
 * The Firemaking skill
 * 
 * @author Gabriel Hannason
 */

public class Firemaking {

	public static void lightFire(final Player player, int log, final boolean addingToFire, final int amount) {
		if (!player.getClickDelay().elapsed(2000) || player.getWalkingQueue().isLockMovement())
			return;
		if (player.getPosition().getX() == 2352 && player.getPosition().getY() == 3703) {
			player.getPacketSender().sendMessage("You can not light a fire on this spot try going south a few paces.");
			return;
		}
		if (!player.getLocation().isFiremakingAllowed()) {
			player.getPacketSender().sendMessage("You can not light a fire in this area.");
			return;
		}
		boolean objectExists2 = CustomObjects.objectExists(player.getPosition().copy());
		boolean objectExists = CustomObjects
				.objectExists(new Position(player.getPosition().getLocalX() + 1, (player.getPosition().getLocalY())));
		if (!Dungeoneering.doingDungeoneering(player)) {
			if (objectExists || objectExists2 && !addingToFire || player.getPosition().getZ() > 0
					|| !player.getWalkingQueue().canWalk(1, 0) && !player.getWalkingQueue().canWalk(-1, 0)
							&& !player.getWalkingQueue().canWalk(0, 1) && !player.getWalkingQueue().canWalk(0, -1)) {
				player.getPacketSender().sendMessage("You can not light a fire here.");
				return;
			}
		}
		final Logdata.logData logData = Logdata.getLogData(player, log);
		if (logData == null)
			return;
		player.getWalkingQueue().clear();
		if (objectExists || objectExists2 && addingToFire)
			WalkingQueue.stepAway(player);
		player.getPacketSender().sendInterfaceRemoval();
		player.setEntityInteraction(null);
		player.getSkillManager().stopSkilling();
		int cycle = 2 + Misc.getRandom(3);
		if (player.getSkillManager().getMaxLevel(Skill.FIREMAKING) < logData.getLevel()) {
			player.getPacketSender()
					.sendMessage("You need a Firemaking level of atleast " + logData.getLevel() + " to light this.");
			return;
		}
		if (!addingToFire) {
			player.getPacketSender().sendMessage("You attempt to light a fire..");
			player.performAnimation(new Animation(733));
			player.getWalkingQueue().setLockMovement(true);
		}
		player.setCurrentTask(new Task(addingToFire ? 2 : cycle, player, addingToFire ? true : false) {
			int added = 0;

			@Override
			public void execute() {
				player.getPacketSender().sendInterfaceRemoval();
				if (addingToFire && player.getInteractingObject() == null) { // fire
																				// has
																				// died
					player.getSkillManager().stopSkilling();
					player.getPacketSender().sendMessage("The fire has died out.");
					return;
				}
				player.getInventory().delete(logData.getLogId(), 1);
				if (addingToFire) {
					player.performAnimation(new Animation(827));
					player.getPacketSender().sendMessage("You add some logs to the fire..");
				} else {
					if (!player.moving) {
						player.getWalkingQueue().setLockMovement(false);
						player.performAnimation(new Animation(65535));

						WalkingQueue.stepAway(player);
					}
					CustomObjects
							.globalFiremakingTask(
									new GameObject(2732, new Position(player.getPosition().getX(),
											player.getPosition().getY(), player.getPosition().getZ())),
									player, logData.getBurnTime());
					player.getPacketSender().sendMessage("The fire catches and the logs begin to burn.");
					stop();
				}
				if (logData == Logdata.logData.OAK) {
					Achievements.finishAchievement(player, AchievementData.BURN_AN_OAK_LOG);
				} else if (logData == Logdata.logData.MAGIC) {
					Achievements.doProgress(player, AchievementData.BURN_100_MAGIC_LOGS);
					Achievements.doProgress(player, AchievementData.BURN_2500_MAGIC_LOGS);
				}
				Sounds.sendSound(player, Sound.LIGHT_FIRE);
				if (player.getInventory().contains(2946) && player.getDonatorRights().isDonator()) {
					player.getSkillManager().addSkillExperience(Skill.FIREMAKING, logData.getXp() * 2);
				} else {
					player.getSkillManager().addSkillExperience(Skill.FIREMAKING, logData.getXp());
				}
				added++;
				if (added >= amount || !player.getInventory().contains(logData.getLogId())) {
					stop();
					if (added < amount && addingToFire && Logdata.getLogData(player, -1) != null
							&& Logdata.getLogData(player, -1).getLogId() != log) {
						player.getClickDelay().reset(0);
						Firemaking.lightFire(player, -1, true, (amount - added));
					}
					return;
				}
			}

			@Override
			public void stop() {
				setEventRunning(false);
				player.performAnimation(new Animation(65535));
				player.getWalkingQueue().setLockMovement(false);
			}
		});
		TaskManager.submit(player.getCurrentTask());
		player.getClickDelay().reset(System.currentTimeMillis() + 500);
	}

}
