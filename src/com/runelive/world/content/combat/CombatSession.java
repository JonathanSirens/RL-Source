package com.runelive.world.content.combat;

import com.runelive.engine.task.Task;
import com.runelive.model.action.distance.CombatFollowMobileAction;
import com.runelive.model.container.impl.Equipment;
import com.runelive.world.content.Sounds;
import com.runelive.world.content.combat.HitQueue.CombatHit;
import com.runelive.world.content.combat.range.CombatRangedAmmo.RangedWeaponData;
import com.runelive.world.content.combat.strategy.impl.DefaultRangedCombatStrategy;
import com.runelive.world.content.combat.weapon.CombatSpecial;
import com.runelive.world.entity.impl.npc.NPC;
import com.runelive.world.entity.impl.player.Player;

/**
 * A {@link Task} implementation that handles every combat 'hook' or 'turn'
 * during a combat session.
 * 
 * @author lare96
 */
public class CombatSession {

	/** The builder assigned to this task. */
	private CombatBuilder builder;

	/**
	 * Create a new {@link CombatSession}.
	 * 
	 * @param builder
	 *            the builder assigned to this task.
	 */
	public CombatSession(CombatBuilder builder) {
		this.builder = builder;
	}

	public void process() {

		if (builder.isCooldown()) {
			builder.cooldown--;
			builder.attackTimer--;

			if (builder.cooldown == 0) {
				builder.reset(true);
			}
			return;
		}

		if (!CombatFactory.checkHook(builder.getCharacter(), builder.getVictim())) {
			return;
		}

		// If the entity is an player we redetermine the combat strategy before
		// attacking.
		if (builder.getCharacter().isPlayer()) {
			builder.determineStrategy();
		}

		// Decrement the attack timer.
		builder.attackTimer--;

		// The attack timer is below 1, we can attack.
		if (builder.attackTimer < 1) {
			// Check if the attacker is close enough to attack.

			if (!CombatFactory.checkAttackDistance(builder)) {
				if (builder.getCharacter().isNpc() && builder.getVictim().isPlayer()) {
					if (builder.getLastAttack().elapsed(4500)) {
						((NPC) builder.getCharacter()).setFindNewTarget(true);
					}
				}
				if (builder.getCharacter().isPlayer()) {
					((Player) builder.getCharacter()).getActionQueue().addAction(new CombatFollowMobileAction(((Player) builder.getCharacter()), builder.getVictim()));
				}
				return;
			}

			if (!builder.getCharacter().getPosition().isWithinDistance(builder.getVictim().getPosition(), 14)) {
				System.out.println(true);
				//builder.getCharacter().getMovementQueue().setFollowCharacter(null);
				return;
			}

			// Check if the attack can be made on this hook
			if (!builder.getStrategy().canAttack(builder.getCharacter(), builder.getVictim())) {
				builder.getCharacter().getCombatBuilder().reset(builder.getCharacter().isNpc() ? true : false);
				return;
			}

			// Do all combat calculations here, we create the combat containers
			// using the attacking entity's combat strategy.

			builder.getStrategy().customContainerAttack(builder.getCharacter(), builder.getVictim());
			CombatContainer container = builder.getContainer();
			if (builder.getCharacter().isPlayer()) {
				Player killer = (Player) builder.getCharacter();
				if(container.getCombatType() == CombatType.MAGIC) {
					int damage_val = container.getTotalDamage();
					int other_health = builder.getVictim().getConstitution();
					if (damage_val > other_health) {
						damage_val = other_health;
					}
					if (killer.getMagicMaxHit() < damage_val) {
						killer.setMagicMaxHit(damage_val);
					}
				}
				if(container.getCombatType() == CombatType.RANGED) {
					int damage_val = container.getTotalDamage();
					int other_health = builder.getVictim().getConstitution();
					if (damage_val > other_health) {
						damage_val = other_health;
					}
					if (killer.getRangeMaxHit() < damage_val) {
						killer.setRangeMaxHit(damage_val);
					}
				}
				if(container.getCombatType() == CombatType.MELEE) {
					int damage_val = container.getTotalDamage();
					int other_health = builder.getVictim().getConstitution();
					if(damage_val > other_health) {
						damage_val = other_health;
					}
					if(killer.getMeleeMaxHit() < damage_val) {
						killer.setMeleeMaxHit(damage_val);
					}
				}
			}
			builder.getCharacter().setEntityInteraction(builder.getVictim());

			if (builder.getCharacter().isPlayer()) {
				Player player = (Player) builder.getCharacter();
				player.getPacketSender().sendInterfaceRemoval();

				if (player.isSpecialActivated() && player.getCastSpell() == null) {
					container = player.getCombatSpecial().container(player, builder.getVictim());
					boolean magicShortbowSpec = player.getCombatSpecial() != null
							&& player.getCombatSpecial() == CombatSpecial.MAGIC_SHORTBOW;
					CombatSpecial.drain(player, player.getCombatSpecial().getDrainAmount());

					Sounds.sendSound(player,
							Sounds.specialSounds(player.getEquipment().get(Equipment.WEAPON_SLOT).getId()));

					if (player.getCombatSpecial().getCombatType() == CombatType.RANGED) {
						DefaultRangedCombatStrategy.decrementAmmo(player, builder.getVictim().getPosition());
						if (CombatFactory.darkBow(player)
								|| player.getRangedWeaponData() == RangedWeaponData.MAGIC_SHORTBOW
										&& magicShortbowSpec) {
							DefaultRangedCombatStrategy.decrementAmmo(player, builder.getVictim().getPosition());
						}
					}
				}
			}

			// If there is no hit type the combat turn is ignored.
			if (container != null && container.getCombatType() != null) {
				// If we have hit splats to deal, we filter them through combat
				// prayer effects now. If not then we still send the hit tasks
				// next to handle any effects.

				// An attack is going to be made for sure, set the last attacker
				// for this victim.

				builder.getVictim().getCombatBuilder().setLastAttacker(builder.getCharacter());
				builder.getVictim().getLastCombat().reset();

				// Start cooldown if we're using magic and not autocasting.
				if (container.getCombatType() == CombatType.MAGIC && builder.getCharacter().isPlayer()) {
					Player player = (Player) builder.getCharacter();

					if (!player.isAutocast()) {
						if (!player.isSpecialActivated())
							player.getCombatBuilder().cooldown = 10;
						player.setCastSpell(null);
						builder.determineStrategy();
					}
				}

				/*
				 * if(container.getHitDelay() == 0) { //An instant attack new
				 * CombatHitTask(builder, container).handleAttack(); } else {
				 * TaskManager.submit(new CombatHitTask(builder, container,
				 * container.getHitDelay(), false)); }
				 */
				builder.getHitQueue().append(new CombatHit(builder, container, container.getHitDelay()));

				builder.setContainer(null); // Fetch a brand new container on
											// next attack
			}

			// Reset the attacking entity.
			builder.attackTimer = builder.getStrategy() != null
					? builder.getStrategy().attackDelay(builder.getCharacter())
					: builder.getCharacter().getAttackSpeed();
			builder.getLastAttack().reset();
			builder.getCharacter().setEntityInteraction(builder.getVictim());

		}
	}
}
