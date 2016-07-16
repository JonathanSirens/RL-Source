package com.runelive.world.content.minigames.impl.zulrah;

import com.runelive.engine.task.Task;
import com.runelive.engine.task.TaskManager;
import com.runelive.model.Animation;
import com.runelive.model.Graphic;
import com.runelive.model.Locations;
import com.runelive.model.Locations.Location;
import com.runelive.model.Position;
import com.runelive.model.RegionInstance;
import com.runelive.model.RegionInstance.RegionInstanceType;
import com.runelive.world.World;
import com.runelive.world.content.dialogue.DialogueManager;
import com.runelive.world.entity.impl.npc.NPC;
import com.runelive.world.entity.impl.npc.NPCMovementCoordinator.Coordinator;
import com.runelive.world.entity.impl.player.Player;

public class Zulrah {

	/**
	 * Initializes our instance for Zulrah
	 * @param player
     */
	public Zulrah(Player player) {
		this.player = player;
	}

	/**
	 * Defines the {@link Player} that is currently doing Zulrah.
	 */
	private final Player player;

	/**
	 * Weakness is magic, uses range to attack with
	 */
	public static final int GREEN_ZULRAH = 2043;

	/**
	 * Weakness is range, uses range and mage to attack with
	 */
	public static final int BLUE_ZULRAH = 2042;

	/**
	 * Weakness is magic, uses melee to attack with
	 */
	public static final int RED_ZULRAH = 2044;

	/**
	 * Weakness is magic, uses mage and range to attack with
	 */
	public static final int JAD_ZULRAH = 2045;

	/**
	 * Zulrah's animation to go down to the next faze
	 */
	public static final Animation GO_DOWN = new Animation(5072);

	/**
	 * Zulrah's animation to go up to the next faze
	 */
	public static final Animation GO_UP = new Animation(5073);

	/**
	 * Zulrah's attack animation to shoot at a player
	 */
	public static final Animation SHOOT = new Animation(5069);

	/**
	 * Zulrah's attack animation to lung at a player
	 */
	public static final Animation LUNG = new Animation(5807);

	/**
	 * Zulrah's target animation to target a player
	 */
	public static final Animation TARGET = new Animation(5806);

	/**
	 * Zulrah's fire ball magic attack graphic
	 */
	public static final Graphic FIRE_BALL = new Graphic(1046);

	/**
	 * Zulrah's range bolt range attack graphic
	 */
	public static final Graphic RANGE_BOLT = new Graphic(1044);

	/**
	 * Zulrah's toxic cloud graphic
	 */
	public static final Graphic TOXIC_CLOUD = new Graphic(1045);

	/**
	 * Snakeling spawn graphic
	 */
	public static final Graphic SNAKELING_SPAWN = new Graphic(1047);

	/**
	 * Zulrah's toxic venom cloud positions that are possible
	 */
	public static final Position[] TOXIC_CLOUD_POSITIONS = {
		new Position(2263, 3076),
		new Position(2263, 3073),
		new Position(2263, 3070),
		new Position(2266, 3069),
		new Position(2269, 3069),
		new Position(2272, 3070),
		new Position(2273, 3073),
		new Position(2263, 3076)
	};

	/**
	 * The rotation of Zulrah.
	 */
	private int zulrahRotation = 0;

	/**
	 * Gets the current Zulrah rotation.
	 * This goes in order 1-4
	 */
	public int getZulrahRotation() {
		return this.zulrahRotation;
	}

	/**
	 * Adds onto the next Zulrah rotation.
	 * This goes in order 1-4
	 */
	public void nextZulrahRotation() {
		if(this.zulrahRotation == 4) {
			this.zulrahRotation = 1;
		} else {
			this.zulrahRotation += 1;
		}
	}

	/**
	 * Sets the Zulrah rotation.
	 * This overrides whatever current value.
	 */
	public void setZulrahRotation(int rotation) {
		this.zulrahRotation = rotation;
	}

	public int zulrahStep = 0;

	/**
	 * Gets the current Zulrah step.
	 * This goes in order 1-(amount)
	 */
	public int getZulrahStep() {
		return this.zulrahStep;
	}

	/**
	 * Adds onto the next Zulrah step.
	 * This goes in order 1-(amount)
	 */
	public void nextZulrahStep() {
		if(this.zulrahRotation != 3) {
			if(this.zulrahStep == 11) {
				this.zulrahStep = 0;
			}
		} else {
			if(this.zulrahStep == 10) {
				this.zulrahStep = 0;
			}
		}
		this.zulrahStep += 1;
	}

	/**
	 * Sets the Zulrah step.
	 * This overrides whatever current value.
	 */
	public void setZulrahStep(int step) {
		this.zulrahStep = step;
	}

	/**
	 * Gets the current Zulrah Npc ID that you need to kill.
	 * This is based on your current Zulrah rotation & Zulrah step.
	 * The max amount possible with this is 11, the minimum is 10.
	 */
	public int getZulrahID() {
		int id = -1;
		System.out.println("Zulrah rotation: "+getZulrahRotation());
		System.out.println("Zulrah step: "+getZulrahStep());
		switch(getZulrahRotation()) {
			case 1:
				switch(getZulrahStep()) {
					case 1:
						setZulrahPosition(new Position(2269, 3077, getPrivateFloor()));
						return Zulrah.GREEN_ZULRAH;
					case 2:
						setZulrahPosition(new Position(2269, 3075, getPrivateFloor()));
						return Zulrah.RED_ZULRAH;
					case 3:
						setZulrahPosition(new Position(2269, 3073, getPrivateFloor()));
						return Zulrah.BLUE_ZULRAH;
					case 4:
						setZulrahPosition(new Position(2267, 3065, getPrivateFloor()));
						return Zulrah.GREEN_ZULRAH;
					case 5:
						setZulrahPosition(new Position(2266, 3073, getPrivateFloor()));
						return Zulrah.RED_ZULRAH;
					case 6:
						setZulrahPosition(new Position(2259, 3072, getPrivateFloor()));
						return Zulrah.BLUE_ZULRAH;
					case 7:
						setZulrahPosition(new Position(2269, 3065, getPrivateFloor()));
						return Zulrah.GREEN_ZULRAH;
					case 8:
						setZulrahPosition(new Position(2271, 3065, getPrivateFloor()));
						return Zulrah.BLUE_ZULRAH;
					case 9:
						setZulrahPosition(new Position(2260, 3074, getPrivateFloor()));
						return Zulrah.JAD_ZULRAH;
					case 10:
						setZulrahPosition(new Position(2267, 3074, getPrivateFloor()));
						return Zulrah.RED_ZULRAH;
					case 11:
						setZulrahPosition(new Position(2266, 3077, getPrivateFloor()));
						return Zulrah.GREEN_ZULRAH;
				}
				break;
			case 2:
				switch(getZulrahStep()) {
					case 1:
						setZulrahPosition(new Position(2269, 3076, getPrivateFloor()));
						return Zulrah.GREEN_ZULRAH;
					case 2:
						setZulrahPosition(new Position(2269, 3075, getPrivateFloor()));
						return Zulrah.RED_ZULRAH;
					case 3:
						setZulrahPosition(new Position(2269, 3073, getPrivateFloor()));
						return Zulrah.BLUE_ZULRAH;
					case 4:
						setZulrahPosition(new Position(2260, 3075, getPrivateFloor()));
						return Zulrah.GREEN_ZULRAH;
					case 5:
						setZulrahPosition(new Position(2267, 3065, getPrivateFloor()));
						return Zulrah.BLUE_ZULRAH;
					case 6:
						setZulrahPosition(new Position(2266, 3073, getPrivateFloor()));
						return Zulrah.RED_ZULRAH;
					case 7:
						setZulrahPosition(new Position(2277, 3072, getPrivateFloor()));
						return Zulrah.GREEN_ZULRAH;
					case 8:
						setZulrahPosition(new Position(2269, 3065, getPrivateFloor()));
						return Zulrah.BLUE_ZULRAH;
					case 9:
						setZulrahPosition(new Position(2260, 3074, getPrivateFloor()));
						return Zulrah.JAD_ZULRAH;
					case 10:
						setZulrahPosition(new Position(2267, 3074, getPrivateFloor()));
						return Zulrah.RED_ZULRAH;
					case 11:
						setZulrahPosition(new Position(2267, 3076, getPrivateFloor()));
						return Zulrah.GREEN_ZULRAH;
				}
				break;
			case 3:
				switch(getZulrahStep()) {
					case 1:
						setZulrahPosition(new Position(2269, 3076, getPrivateFloor()));
						return Zulrah.GREEN_ZULRAH;
					case 2:
						setZulrahPosition(new Position(2277, 3074, getPrivateFloor()));
						return Zulrah.GREEN_ZULRAH;
					case 3:
						setZulrahPosition(new Position(2269, 3074, getPrivateFloor()));
						return Zulrah.RED_ZULRAH;
					case 4:
						setZulrahPosition(new Position(2259, 3072, getPrivateFloor()));
						return Zulrah.BLUE_ZULRAH;
					case 5:
						setZulrahPosition(new Position(2269, 3065, getPrivateFloor()));
						return Zulrah.GREEN_ZULRAH;
					case 6:
						setZulrahPosition(new Position(2278, 3072, getPrivateFloor()));
						return Zulrah.BLUE_ZULRAH;
					case 7:
						setZulrahPosition(new Position(2269, 3072, getPrivateFloor()));
						return Zulrah.GREEN_ZULRAH;
					case 8:
						setZulrahPosition(new Position(2260, 3074, getPrivateFloor()));
						return Zulrah.GREEN_ZULRAH;
					case 9:
						setZulrahPosition(new Position(2267, 3073, getPrivateFloor()));
						return Zulrah.BLUE_ZULRAH;
					case 10:
						setZulrahPosition(new Position(2280, 3073, getPrivateFloor()));
						return Zulrah.JAD_ZULRAH;
				}
				break;
			case 4:
				switch(getZulrahStep()) {
					case 1:
						setZulrahPosition(new Position(2269, 3076, getPrivateFloor()));
						return Zulrah.GREEN_ZULRAH;
					case 2:
						setZulrahPosition(new Position(2277, 3074, getPrivateFloor()));
						return Zulrah.BLUE_ZULRAH;
					case 3:
						setZulrahPosition(new Position(2269, 3065, getPrivateFloor()));
						return Zulrah.GREEN_ZULRAH;
					case 4:
						setZulrahPosition(new Position(2259, 3072, getPrivateFloor()));
						return Zulrah.BLUE_ZULRAH;
					case 5:
						setZulrahPosition(new Position(2269, 3074, getPrivateFloor()));
						return Zulrah.RED_ZULRAH;
					case 6:
						setZulrahPosition(new Position(2278, 3072, getPrivateFloor()));
						return Zulrah.GREEN_ZULRAH;
					case 7:
						setZulrahPosition(new Position(2270, 3065, getPrivateFloor()));
						return Zulrah.GREEN_ZULRAH;
					case 8:
						setZulrahPosition(new Position(2258, 3074, getPrivateFloor()));
						return Zulrah.BLUE_ZULRAH;
					case 9:
						setZulrahPosition(new Position(2268, 3072, getPrivateFloor()));
						return Zulrah.GREEN_ZULRAH;
					case 10:
						setZulrahPosition(new Position(2267, 3073, getPrivateFloor()));
						return Zulrah.BLUE_ZULRAH;
					case 11:
						setZulrahPosition(new Position(2280, 3073, getPrivateFloor()));
						return Zulrah.JAD_ZULRAH;
				}
				break;
		}
		return id;
	}

	private Position zulrahPosition = new Position(0,0);

	/**
	 * Grabs the current Zulrah position
	 */
	public Position getZulrahPosition() {
		return this.zulrahPosition;
	}

	/**
	 * Sets the current zulrah position
	 */
	private void setZulrahPosition(Position newZulrahPosition) {
		this.zulrahPosition = newZulrahPosition;
	}

	/**
	 * This gets your private floor
	 */
	private int getPrivateFloor() {
		return player.getIndex() * 4;
	}
	
	/**
	 * Enter the Zulrah Island
	 */
	public void enterIsland() {
		player.moveTo(new Position(2268, 3069, getPrivateFloor()));
		player.setLocation(Location.ZULRAH_PIT);
		player.setRegionInstance(new RegionInstance(player, RegionInstanceType.ZULRAH_PIT));
		spawnZulrah();
	}
	
	/**
	 * Leave the Zulrah Island
	 */
	public void leaveIsland(boolean resetStats) {
		Locations.Location.ZULRAH_PIT.leave(player);
		if (resetStats)
			player.restart();
	}

	/**
	 * Spawn the first faze of Zulrah
	 */
	private void spawnZulrah() {
		//This adds onto the current zulrah rotation # that you have saved
		nextZulrahRotation();
		//This adds onto the current zulrah kill step
		nextZulrahStep();
		//Grabs the current zulrah ID and sets the current position
		int zulrahId = getZulrahID();
		new ZulrahBoss(player, zulrahId, getZulrahPosition());
		/*TaskManager.submit(new Task(1, player, false) {
			int tick = 0;
			@Override
			public void execute() {
				if (player.getRegionInstance() == null || !player.isRegistered() || player.getLocation() != Location.ZULRAH_PIT) {
					stop();
					return;
				}
				if(tick == 1) {
					DialogueManager.sendStatement(player, "Prepare for battle!");
				}
				if(tick == 3) {
					//This adds onto the current zulrah rotation # that you have saved
					nextZulrahRotation();
					//This adds onto the current zulrah kill step
					nextZulrahStep();
					//Grabs the current zulrah ID and sets the current position
					int zulrahId = getZulrahID();
					//Declares the npc zulrah
					NPC n = new NPC(zulrahId, getZulrahPosition()).setSpawnedFor(player);

					//Adds the npc zulrah to the official game
					World.register(n);
					player.getRegionInstance().getNpcsList().add(n);
					//The npc zulrah attacks the player
					n.getCombatBuilder().attack(player);
					stop();
				}
				tick++;
			}
		});*/
	}
	
	/**
	 * Spawn the next faze of Zulrah @nextZulrahStep
	 */
	public void next(final NPC zulrah) {
		nextZulrahStep();
		TaskManager.submit(new Task(1, player, false) {
			int tick = 0;
			int health = zulrah.getConstitution();
			int id = getZulrahID();
			NPC nextZulrah = new NPC(id, getZulrahPosition());
			@Override
			public void execute() {
				if(tick == 0) {
					zulrah.setChargingAttack(true);
					zulrah.performAnimation(GO_DOWN);
				}
				if(tick == 2) {
					if(zulrah.isRegistered()) {
						World.deregister(zulrah);
						player.getRegionInstance().getNpcsList().remove(zulrah);
					}
				}
				if(tick == 3) {
					nextZulrah = new NPC(id, getZulrahPosition()).setSpawnedFor(player);
					nextZulrah.getMovementCoordinator().setCoordinator(new Coordinator(false, -1));
					World.register(nextZulrah);
					player.getRegionInstance().getNpcsList().add(nextZulrah);
					nextZulrah.setConstitution(health);
					nextZulrah.performAnimation(GO_UP);
				}
				if(tick == 4) {
					nextZulrah.setChargingAttack(false);
					nextZulrah.getCombatBuilder().attack(player);
					stop();
				}
				tick++;
			}
		});
	}

	/**
	 * Kill the Zulrah
	 */
	public void handleZulrahDeath(NPC n) {
		
	}

}

final class ZulrahBoss extends NPC {

	/**
	 * Creates our new instance for our boss.
	 * @param id
	 * @param position
	 * @param newInstance
	 */
	public ZulrahBoss(Player player, int id, Position position) {
		super(id, position);
		this.player = player;
		this.setSpawnedFor(player);
		this.getMovementCoordinator().setCoordinator(getCoordinator());
		player.getRegionInstance().getNpcsList().add(this);
		World.register(this);
	}

	/**
	 * We will store the player that this boss instance is spawned for.
	 */
	private final Player player;

	/**
	 * We will use this to setup our timed events
	 */
	private int ticks = 0;

	/**
	 * Returns our default coordinator to make sure that Zulrah cannot walk.
	 * @return
	 */
	private Coordinator getCoordinator() {
		return new Coordinator(false, -1);
	}

	/**
	 * This is our process for the Boss.  TIP: This runs on a 600ms delay
	 */
	@Override
	public void sequence() {
		//TODO: Jonny setup your spawn method in here based on you you see fit.
		switch(ticks++) {
			case 2:
				this.getCombatBuilder().attack(player);
				break;
		}
		super.sequence();
		//TODO: Jonny here you can process your next() method.
	}
}
