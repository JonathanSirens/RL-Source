package com.chaos.world.content.combat.strategy.impl;

import com.chaos.engine.task.Task;
import com.chaos.engine.task.TaskManager;
import com.chaos.model.Animation;
import com.chaos.model.Projectile;
import com.chaos.world.content.combat.CombatContainer;
import com.chaos.world.content.combat.CombatType;
import com.chaos.world.content.combat.strategy.CombatStrategy;
import com.chaos.world.entity.impl.Character;
import com.chaos.world.entity.impl.npc.NPC;

public class DagannothPrime implements CombatStrategy {

    @Override
    public boolean canAttack(Character entity, Character victim) {
        return true;
    }

    @Override
    public CombatContainer attack(Character entity, Character victim) {
        return null;
    }

    @Override
    public boolean customContainerAttack(Character entity, Character victim) {
        NPC prime = (NPC) entity;
        if (prime.getConstitution() <= 0 || victim.getConstitution() <= 0) {
            return true;
        }
        prime.performAnimation(new Animation(prime.getDefinition().getAttackAnimation()));
        TaskManager.submit(new Task(1, prime, false) {

            @Override
            protected void execute() {
                new Projectile(prime, victim, 500, 44, 3, 43, 31, 0).sendProjectile();
                prime.getCombatBuilder()
                        .setContainer(new CombatContainer(prime, victim, 1, 2, CombatType.MAGIC, true));
                stop();
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
        return 15;
    }

    @Override
    public CombatType getCombatType() {
        return CombatType.RANGED;
    }
}