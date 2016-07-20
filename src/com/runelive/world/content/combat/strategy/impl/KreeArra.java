package com.runelive.world.content.combat.strategy.impl;

import com.runelive.engine.task.Task;
import com.runelive.engine.task.TaskManager;
import com.runelive.model.Animation;
import com.runelive.model.Graphic;
import com.runelive.model.Locations.Location;
import com.runelive.model.Position;
import com.runelive.model.Projectile;
import com.runelive.model.movement.MovementQueue;
import com.runelive.util.Misc;
import com.runelive.world.content.combat.CombatContainer;
import com.runelive.world.content.combat.CombatType;
import com.runelive.world.content.combat.HitQueue.CombatHit;
import com.runelive.world.content.combat.strategy.CombatStrategy;
import com.runelive.world.entity.impl.Character;
import com.runelive.world.entity.impl.npc.NPC;
import com.runelive.world.entity.impl.player.Player;

public class KreeArra implements CombatStrategy {

	public static String[] randomMessage = {
			"Scree!", "Squawk!"
	};

	public static String getRandomMessage() {
		return randomMessage[(int) (Math.random() * randomMessage.length)];
	}

	@Override
	public boolean canAttack(Character entity, Character victim) {
		return victim.isPlayer()
				&& ((Player) victim).getMinigameAttributes().getGodwarsDungeonAttributes().hasEnteredRoom();
	}

	@Override
	public CombatContainer attack(Character entity, Character victim) {
		return null;
	}

	@Override
	public boolean customContainerAttack(Character entity, Character victim) {
		int speechChance = Misc.inclusiveRandom(1, 4);
		NPC kreearra = (NPC) entity;

		if (victim.getConstitution() <= 0) {
			return true;
		}
		if (kreearra.isChargingAttack()) {
			return true;
		}

		final CombatType style = Misc.getRandom(1) == 0 ? CombatType.MAGIC : CombatType.RANGED;
		kreearra.performAnimation(new Animation(kreearra.getDefinition().getAttackAnimation()));
		kreearra.setChargingAttack(true);
		Player target = (Player) victim;
		TaskManager.submit(new Task(1, kreearra, false) {
			int tick = 0;

			@Override
			public void execute() {
				if (tick == 1) {
					for (final Player near : Misc.getCombinedPlayerList(target)) {
						if (near == null || near.getLocation() != Location.GODWARS_DUNGEON || near.isTeleporting())
							continue;
						if (near.getPosition().distanceToPoint(kreearra.getPosition().getX(),
								kreearra.getPosition().getY()) > 20)
							continue;
						new Projectile(kreearra, victim, style == CombatType.MAGIC ? 1198 : 1197, 44, 3, 43, 43, 0)
								.sendProjectile();
						if(speechChance == 1) {
							kreearra.forceChat(getRandomMessage());
						}
						if (style == CombatType.RANGED) { // Moving players
															// randomly
							int xToMove = Misc.getRandom(1);
							int yToMove = Misc.getRandom(1);
							int xCoord = target.getPosition().getX();
							int yCoord = target.getPosition().getY();
							if (xCoord >= 2841 || xCoord <= 2823 || yCoord <= 5295 || yCoord >= 5307) {
								if(speechChance == 1) {
									kreearra.forceChat("Storms align to me!");
								}
								return;
							} else if (Misc.getRandom(3) <= 1 && MovementQueue.canWalk(target.getPosition(),
									new Position(xCoord + -xToMove, yCoord + -yToMove, 2), 1)) {
								target.getMovementQueue().reset();
								if (!target.isTeleporting())
									target.moveTo(new Position(xCoord + -xToMove, yCoord + -yToMove, 2));
								target.performGraphic(new Graphic(128));
								if(speechChance == 1) {
									kreearra.forceChat(getRandomMessage());
								}
							}
						}
					}
				} else if (tick == 2) {
					for (final Player near : Misc.getCombinedPlayerList(target)) {
						if (near == null || near.getLocation() != Location.GODWARS_DUNGEON || near.isTeleporting())
							continue;
						if (near.getPosition().distanceToPoint(kreearra.getPosition().getX(),
								kreearra.getPosition().getY()) > 20)
							continue;
						new CombatHit(kreearra.getCombatBuilder(),
								new CombatContainer(kreearra, victim, 1, style, true)).handleAttack();
					}
					kreearra.setChargingAttack(false);
					stop();
				}
				tick++;
			}
		});
		return true;
	}

	@Override
	public int attackDelay(Character entity) {
		return entity.getAttackSpeed();
	}

	@Override
	public int attackDistance(Character entity) {
		return 20;
	}

	@Override
	public CombatType getCombatType() {
		return CombatType.MIXED;
	}
}
