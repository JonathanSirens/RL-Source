package com.runelive.world.entity.impl.npc;

import com.runelive.engine.task.TaskManager;
import com.runelive.engine.task.impl.NPCDeathTask;
import com.runelive.model.Direction;
import com.runelive.model.Locations;
import com.runelive.model.Locations.Location;
import com.runelive.model.Position;
import com.runelive.model.definitions.NpcDefinition;
import com.runelive.util.Filter;
import com.runelive.util.RandomGenerator;
import com.runelive.world.World;
import com.runelive.world.clip.region.Region;
import com.runelive.world.content.Area;
import com.runelive.world.content.combat.CombatFactory;
import com.runelive.world.content.combat.CombatType;
import com.runelive.world.content.combat.effect.CombatPoisonEffect.PoisonType;
import com.runelive.world.content.combat.effect.CombatVenomEffect.VenomType;
import com.runelive.world.content.combat.strategy.CombatStrategies;
import com.runelive.world.content.combat.strategy.CombatStrategy;
import com.runelive.world.content.combat.strategy.impl.KalphiteQueen;
import com.runelive.world.content.combat.strategy.impl.Nex;
import com.runelive.world.content.skill.impl.hunter.Hunter;
import com.runelive.world.content.skill.impl.hunter.PuroPuro;
import com.runelive.world.content.skill.impl.runecrafting.DesoSpan;
import com.runelive.world.entity.impl.Character;
import com.runelive.world.entity.impl.player.Player;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Random;

/**
 * Represents a non-playable character, which players can interact with.
 *
 * @author Gabriel Hannason
 */

public class NPC extends Character {

    public NPC(int id, Position position) {
        super(position);
        NpcDefinition definition = NpcDefinition.forId(id);
        if (definition == null)
            throw new NullPointerException("NPC " + id + " is not defined!");
        this.defaultPosition = position;
        this.id = id;
        this.definition = definition;
        this.makeArea = Area.create(position, definition.getSize());
        this.defaultConstitution = definition.getHitpoints() < 100 ? 100 : definition.getHitpoints();
        this.constitution = defaultConstitution;
        this.projectileClipping = NPC.isProjectileNpc(id);
        this.walkingDistance = NPC.getWalkingDistance(id);
        this.walkingRandom = NPC.getWalkingRandom(id);
        setLocation(Location.getLocation(this));
    }

    public void sequence() {

        /** COMBAT **/
        getCombatBuilder().process();

        if (getCombatBuilder().isAttacking()) {
            follow(getCombatBuilder().getVictim());
        } else {
            walking();
        }

        /**
         * HP restoration
         */
        if (constitution < defaultConstitution) {
            if (!isDying) {
                if (getLastCombat().elapsed((id == 13447 || id == 3200 ? 50000 : 5000))
                        && !getCombatBuilder().isAttacking() && getLocation() != Location.PEST_CONTROL_GAME
                        && getLocation() != Location.DUNGEONEERING) {
                    setConstitution(constitution + (int) (defaultConstitution * 0.1));
                    if (constitution > defaultConstitution)
                        setConstitution(defaultConstitution);
                }
            }
        }
    }

    @Override
    public void appendDeath() {
        if (!isDying && !summoningNpc) {
            TaskManager.submit(new NPCDeathTask(this));
            isDying = true;
        }
    }

    @Override
    public int getConstitution() {
        return constitution;
    }

    @Override
    public NPC setConstitution(int constitution) {
        this.constitution = constitution;
        if (this.constitution <= 0)
            appendDeath();
        return this;
    }

    @Override
    public void heal(int heal) {
        if ((this.constitution + heal) > getDefaultConstitution()) {
            setConstitution(getDefaultConstitution());
            return;
        }
        setConstitution(this.constitution + heal);
    }

    @Override
    public int getBaseAttack(CombatType type) {
        return getDefinition().getAttackBonus();
    }

    @Override
    public int getAttackSpeed() {
        return this.getDefinition().getAttackSpeed();
    }

    @Override
    public int getBaseDefence(CombatType type) {

        if (type == CombatType.MAGIC)
            return getDefinition().getDefenceMage();
        else if (type == CombatType.RANGED)
            return getDefinition().getDefenceRange();

        return getDefinition().getDefenceMelee();
    }

    @Override
    public boolean isNpc() {
        return true;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof NPC && ((NPC) other).getIndex() == getIndex();
    }

    @Override
    public int getSize() {
        return getDefinition().getSize();
    }

    @Override
    public void poisonVictim(Character victim, CombatType type) {
        if (getDefinition().isPoisonous()) {
            CombatFactory.poisonEntity(victim,
                    type == CombatType.RANGED || type == CombatType.MAGIC ? PoisonType.MILD : PoisonType.EXTRA);
        }

    }

    @Override
    public void venomVictim(Character victim, CombatType type) {
        if (getDefinition().isVenomous()) {
            CombatFactory.venomEntity(victim, VenomType.SUPER);
        }

    }

    /**
     * Prepares the dynamic json loader for loading world npcs.
     *
     * @return the dynamic json loader.
     * @throws Exception if any errors occur while preparing for load.
     */
    public static void init() {
        try {
            DataInputStream stream = new DataInputStream(new FileInputStream(new File("./data/def/npcSpawns.dat")));
            while (stream.available() > 0) {
                int id = stream.readShort();
                int size = stream.readByte();
                for (int i = 0; i < size; i++) {
                    Position position = new Position(stream.readShort(), stream.readShort(), stream.readShort());
                    Direction direction = Direction.values()[stream.readByte()];
                    boolean canWalk = stream.readByte() == 0;
                    int walking = stream.readByte();
                    stream.readByte(); //This is for world reading.  We currently do not need this
                    NPC npc = new NPC(id, position);
                    npc.setDirection(direction);
                    npc.walkingDistance = walking;
                    if (!canWalk) {
                        npc.setWalking(false);
                    }
                    World.register(npc);
                    if (id > 5070 && id < 5081) {
                        Hunter.HUNTER_NPC_LIST.add(npc);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Nex.spawn();
        PuroPuro.spawn();
        DesoSpan.spawn();
        KalphiteQueen.spawn(1158, new Position(3485, 9509));
    }

    @Override
    public CombatStrategy determineStrategy() {
        return CombatStrategies.getStrategy(id);
    }

    public boolean switchesVictim() {
        if (getLocation() == Location.DUNGEONEERING) {
            return true;
        }
        return id == 6263 || id == 6265 || id == 6203 || id == 6208 || id == 6206 || id == 6247 || id == 6250
                || id == 3200 || id == 4540 || id == 1158 || id == 1160 || id == 8133 || id == 13447 || id == 13451
                || id == 13452 || id == 13453 || id == 13454 || id == 2896 || id == 2882 || id == 2881 || id == 6260
                || id == 5866;
    }

    public int getAggressiveDistanceLimit() {
        switch (id) {
            case 135:
            case 1472:
            case 132:
            case 133:
            case 1851:
            case 1854:
            case 1857:
                return 5;
        }
        return 10;
    }

    public int getAggressionDistance() {
        int distance = walkingDistance;
        if (distance == 0) {
            distance = 7;
        }
        /*
		 * switch(id) { }
		 */
        if (Nex.nexMob(id)) {
            distance = 60;
        } else if (id == 2896) {
            distance = 50;
        }
        return distance;
    }

	/*
	 * Fields
	 */
    /**
     * INSTANCES
     **/
    private final Position defaultPosition;
    private Player spawnedFor;
    private NpcDefinition definition;
    private Position lastWalkPosition = null;
    public Area makeArea;
    public Position walkingTo;

    /**
     * INTS
     **/
    private final int id;
    private int constitution = 100;
    private int defaultConstitution;
    private int transformationId = -1;
    private int containsCount;
    private final int walkingRandom;
    public int walkingDistance;
    protected int maximumDistance = 8;

    /**
     * BOOLEANS
     **/
    private boolean[] attackWeakened = new boolean[3], strengthWeakened = new boolean[3];
    private boolean summoningNpc, summoningCombat;
    private boolean isDying;
    private boolean visible = true;
    private boolean healed, chargingAttack;
    private boolean findNewTarget;
    public boolean projectileClipping;
    private boolean randomWalks;
    private boolean walkEnabled = true;

    public void setWalking(boolean canWalk) {
        this.walkEnabled = canWalk;
    }

	/*
	 * Getters and setters
	 */

    public int getId() {
        return id;
    }

    public Position getDefaultPosition() {
        return defaultPosition;
    }

    public int getDefaultConstitution() {
        return defaultConstitution;
    }

    public int getTransformationId() {
        return transformationId;
    }

    public void setTransformationId(int transformationId) {
        this.transformationId = transformationId;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setDying(boolean isDying) {
        this.isDying = isDying;
    }

    public void setDefaultConstitution(int defaultConstitution) {
        this.defaultConstitution = defaultConstitution;
    }

    /**
     * @return the statsWeakened
     */
    public boolean[] getDefenceWeakened() {
        return attackWeakened;
    }

    public void setSummoningNpc(boolean summoningNpc) {
        this.summoningNpc = summoningNpc;
    }

    public boolean isSummoningNpc() {
        return summoningNpc;
    }

    public boolean isDying() {
        return isDying;
    }

    /**
     * @return the statsBadlyWeakened
     */
    public boolean[] getStrengthWeakened() {
        return strengthWeakened;
    }

    public NpcDefinition getDefinition() {
        return definition;
    }

    public Player getSpawnedFor() {
        return spawnedFor;
    }

    public NPC setSpawnedFor(Player spawnedFor) {
        this.spawnedFor = spawnedFor;
        return this;
    }

    public boolean hasHealed() {
        return healed;
    }

    public void setHealed(boolean healed) {
        this.healed = healed;
    }

    public boolean isChargingAttack() {
        return chargingAttack;
    }

    public NPC setChargingAttack(boolean chargingAttack) {
        this.chargingAttack = chargingAttack;
        return this;
    }

    public boolean findNewTarget() {
        return findNewTarget;
    }

    public void setFindNewTarget(boolean findNewTarget) {
        this.findNewTarget = findNewTarget;
    }

    public boolean summoningCombat() {
        return summoningCombat;
    }

    public void setSummoningCombat(boolean summoningCombat) {
        this.summoningCombat = summoningCombat;
    }

    public void disableRandomWalking() {
        randomWalks = false;
    }

    public boolean canWalk() {
        switch (id) {
            case 1532:
            case 1533:
            case 329:
            case 322:
            case 321:
            case 2067:
            case 324:
            case 334:
            case 1176:
            case 1174:
            case 29: // musician
            case 1056: // mime
            case 571: // Baker
            case 539: // Silk stall
            case 573: // Fur stall
            case 569: // Silver stall
            case 572: // spice stall
            case 540: // Gem stall
                return false;
        }

        return this.walkEnabled;
    }

    public static int getWalkingDistance(int id) {
        switch (id) {
            case 6053:
            case 6054:
            case 6055:
            case 6056:
            case 6057:
            case 6058:
            case 6059:
            case 6060:
            case 6061:
            case 6062:
            case 6063:
            case 6064:
            case 7845:
            case 7846:
            case 7866:
            case 7867:
            case 7902:
            case 7903:
            case 7904:
            case 7905:
            case 7906:
                return 200;
            case 7312:
            case 5117:
            case 5114:
            case 5115:
            case 5116:
            case 5081:
            case 6915:
            case 6913:
            case 6942:
            case 7015:
            case 5079:
            case 6908:
            case 5080:
            case 7012:
            case 7010:
            case 5073:
            case 5075:
            case 5076:
            case 5074:
            case 5072:
            case 7031: {
                return 6;
            }
            case 5082:
            case 5083:
            case 5084:
            case 5085: {
                return 12;
            }
        }
        return 3;
    }

    private static int getWalkingRandom(int id) {
        switch (id) {
            case 659:
                return 15;
            case 2263:
            case 2264:
            case 2265:
                return 7;
            case 6053:
            case 6054:
            case 6055:
            case 6056:
            case 6057:
            case 6058:
            case 6059:
            case 6060:
            case 6061:
            case 6062:
            case 6063:
            case 6064:
            case 7845:
            case 7846:
            case 7866:
            case 7867:
            case 7902:
            case 7903:
            case 7904:
            case 7905:
            case 7906:
                return 1;
        }
        return 3;
    }

    public static boolean randomWalks(int id) {
        switch (id) {
            case 333:
            case 599:
            case 6390:
            case 550:
            case 549:
            case 541:
            case 5989:
            case 2538:
            case 519:
            case 494:
            case 1282:
            case 2619:
            case 3788:
            case 44:
            case 45:
            case 324:
            case 243:
            case 241:
            case 2244:
            case 326:
            case 334:
            case 322:
            case 2589:
            case 2579:
            case 2578:
            case 3782:
            case 3790:
            case 3789:
            case 6142:
            case 6143:
            case 6144:
            case 6145:
            case 904:
            case 905:
            case 520:
            case 2290:
            case 2618:
            case 546:
            case 902:
            case 590:
            case 7044:
            case 8948:
            case 309:
            case 312:
            case 313:
            case 316:
            case 2262:
            case 8091:
            case 943:
            case 2067:
                return false;
        }
        return true;
    }

    public static boolean isProjectileNpc(int id) {
        switch (id) {
            case 5082:
            case 5083:
            case 5084:
            case 5085:
            case 6053:
            case 6054:
            case 6055:
            case 6056:
            case 6057:
            case 6058:
            case 6059:
            case 6060:
            case 6061:
            case 6062:
            case 6063:
            case 6064:
            case 7845:
            case 7846:
            case 7866:
            case 7867:
            case 7902:
            case 7903:
            case 7904:
            case 7905:
            case 7906:
                return true;
        }
        return false;
    }

    public void follow(Character a) {
        if (!canWalk()) {
            return;
        }
        if (a == null) {
            return;
        }
        if ((this.isFrozen() || constitution <= 0)) {
            return;
        }
        int distance = this.distance(a);
        if (!isSummoningNpc()) {
            if (determineStrategy().getCombatType() != CombatType.MELEE) {
                if (distance <= (determineStrategy().getCombatType() == CombatType.RANGED ? 8 : 10) && (Region.canMagicAttack(this, a) || determineStrategy().getCombatType() == CombatType.MIXED)) {
                    return;
                }
            }
        }
        Area area = Area.create(getPosition(), NpcDefinition.forId(id).getSize() - 1);
        Position targetPos = a.getWalkingQueue().getNextPosition();
        Direction direction;
        if (!isSummoningNpc() && makeArea.distance(targetPos) > maximumDistance) {
            if (getCombatBuilder().getLastAttacker() == null) {
                return;
            }
            if (getCombatBuilder().getLastAttacker().getCombatBuilder().isAttacking() == false) {
                return;
            }
            direction = NPC.moveAwayDirection(targetPos, area);
        } else {
            direction = this.getNextFollowPoint(targetPos, area);
        }
        if(direction == Direction.NONE) {
            return;
        }
        if (projectileClipping ? World.projectileDirectionBlocked(direction, getPosition().getZ(), getPosition().getX(), getPosition().getY(), this.getSize()) : World.directionBlocked(direction, getPosition().getZ(), getPosition().getX(), getPosition().getY(), this.getSize())) {
            return;
        }
        walkingQueue.addStepInternal(getPosition().getX() + direction.getX(), getPosition().getY() + direction.getY());
    }

    private Direction getNextFollowPoint(Position position, Area destination) {
        int x = position.getX();
        int y = position.getY();
        if (destination.contains(x, y)) {
            Direction direction = destination.moveAwayFrom(position, containsCount % 4);
            if (direction == Direction.NONE || destination.getX() + direction.getX() == getLastPosition().getX() && destination.getY() + direction.getY() == getLastPosition().getY()) {
                containsCount += new Random().nextInt(3) + 1;
            } else {
                containsCount = 0;
            }
            return direction;
        }
        containsCount = 0;
        int distanceTo = destination.distance(x, y);
        if (distanceTo == 1) {
            return Direction.NONE;
        }
        int diffX = -destination.xDifference(x);
        int diffY = -destination.yDifference(y);
        Direction dirX = Direction.direction(diffX, 0);
        Direction dirY = Direction.direction(0, diffY);
        if (dirX == Direction.NONE && dirY == Direction.NONE) {
            return Direction.NONE;
        }
        int z = position.getZ() % 4;
        if (World.directionBlocked(dirX, z, destination.getX(), destination.getY(), destination.getSize())) {
            dirX = Direction.NONE;
        }
        if (World.directionBlocked(dirY, z, destination.getX(), destination.getY(), destination.getSize())) {
            dirY = Direction.NONE;
        }
		/*if (dirX == Direction.NONE) {
			return dirY;
		}
		if (dirY == Direction.NONE) {
			return dirX;
		}*/
        Direction direction = Direction.direction(dirX.getX(), dirY.getY());
        if (World.directionBlocked(direction, z, destination.getX(), destination.getY(), destination.getSize())) {
            if (Math.abs(diffX) >= Math.abs(diffY)) {
                return dirX;
            } else {
                return dirY;
            }
        }
        return direction;
    }

    public static Direction moveAwayDirection(Position position, Area destination) {
        int x = position.getX();
        int y = position.getY();
        int diffX = destination.xDifference(x);
        int diffY = destination.yDifference(y);
        Direction dirX = Direction.direction(diffX, 0);
        Direction dirY = Direction.direction(0, diffY);
        if (dirX == Direction.NONE && dirY == Direction.NONE) {
            return Direction.NONE;
        }
        int z = position.getZ() % 4;
        if (World.directionBlocked(dirX, z, destination.getX(), destination.getY(), destination.getSize())) {
            dirX = Direction.NONE;
        }
        if (World.directionBlocked(dirY, z, destination.getX(), destination.getY(), destination.getSize())) {
            dirY = Direction.NONE;
        }
        if (dirX == Direction.NONE) {
            return dirY;
        }
        if (dirY == Direction.NONE) {
            return dirX;
        }
        Direction direction = Direction.direction(dirX.getX(), dirY.getY());
        if (World.directionBlocked(direction, z, destination.getX(), destination.getY(), destination.getSize())) {
            if (Math.abs(diffX) >= Math.abs(diffY)) {
                return dirX;
            } else {
                return dirY;
            }
        }
        return direction;
    }

    public void walking() {
        if (constitution <= 0 || !this.canWalk()) {
            return;
        }
        if (walkingDistance == 3 && !randomWalks) {
            return;
        }
        if (makeArea == null) {
            return;
        }
        if ((((lastWalkPosition != null && getPosition().equals(lastWalkPosition)) || (walkingTo != null && getPosition().equals(walkingTo))) && RandomGenerator.nextInt(walkingRandom) == 0) || RandomGenerator.nextInt(walkingRandom * 6) == 0) {
            int randX = RandomGenerator.random(walkingDistance);
            int randY = RandomGenerator.random(walkingDistance);
            if (RandomGenerator.nextInt(2) == 0) {
                randX = -randX;
            }
            if (RandomGenerator.nextInt(2) == 0) {
                randY = -randY;
            }
            int newX = getPosition().getX() + randX;
            int newY = getPosition().getY() + randY;
            int minimumX = makeArea.getX() - walkingDistance;
            int minimumY = makeArea.getY() - walkingDistance;
            int maximumX = makeArea.getX() + walkingDistance;
            int maximumY = makeArea.getY() + walkingDistance;
            if (newX < minimumX) {
                newX = minimumX;
            } else if (newX > maximumX) {
                newX = maximumX;
            }
            if (newY < minimumY) {
                newY = minimumY;
            } else if (newY > maximumY) {
                newY = maximumY;
            }
            walkingTo = new Position(newX, newY, getPosition().getZ());
        }
        lastWalkPosition = getPosition();
        Direction direction;
        if (walkingTo == null) {
            direction = Direction.NONE;
        } else {
            direction = Direction.direction(walkingTo.getX() - getPosition().getX(), walkingTo.getY() - getPosition().getY());
        }
        if (projectileClipping ? World.projectileDirectionBlocked(direction, getPosition().getZ(), getPosition().getX(), getPosition().getY(), this.getSize()) : World.directionBlocked(direction, getPosition().getZ(), getPosition().getX(), getPosition().getY(), this.getSize())) {
            return;
        }
        walkingQueue.addStepInternal(getPosition().getX() + direction.getX(), getPosition().getY() + direction.getY());
    }
}
