package com.strattus.world;

import java.util.Iterator;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.strattus.GameSettings;
import com.strattus.model.PlayerRights;
import com.strattus.util.Misc;
import com.strattus.world.content.ShootingStar;
import com.strattus.world.content.minigames.impl.FightPit;
import com.strattus.world.content.minigames.impl.PestControl;
import com.strattus.world.entity.Entity;
import com.strattus.world.entity.EntityHandler;
import com.strattus.world.entity.impl.CharacterList;
import com.strattus.world.entity.impl.npc.NPC;
import com.strattus.world.entity.impl.player.Player;
import com.strattus.world.entity.impl.player.PlayerHandler;
import com.strattus.world.entity.updating.NpcUpdateSequence;
import com.strattus.world.entity.updating.PlayerUpdateSequence;
import com.strattus.world.entity.updating.UpdateSequence;

/**
 * @author Gabriel Hannason
 * Thanks to lare96 for help with parallel updating system
 */
public class World {

	/** All of the registered players. */
	private static CharacterList<Player> players = new CharacterList<>(1000);

	/** All of the registered NPCs. */
	private static CharacterList<NPC> npcs = new CharacterList<>(2027);

	/** Used to block the game thread until updating has completed. */
	private static Phaser synchronizer = new Phaser(1);

	/** A thread pool that will update players in parallel. */
	private static ExecutorService updateExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new ThreadFactoryBuilder().setNameFormat("UpdateThread").setPriority(Thread.MAX_PRIORITY).build());

	/** The queue of {@link Player}s waiting to be logged in. **/
    private static Queue<Player> logins = new ConcurrentLinkedQueue<>();

    /**The queue of {@link Player}s waiting to be logged out. **/
    private static Queue<Player> logouts = new ConcurrentLinkedQueue<>();
    
    /**The queue of {@link Player}s waiting to be given their vote reward. **/
    private static Queue<Player> voteRewards = new ConcurrentLinkedQueue<>();
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
    
    public static void register(Entity entity) {
		EntityHandler.register(entity);
	}

	public static void deregister(Entity entity) {
		EntityHandler.deregister(entity);
	}

	public static Player getPlayerByName(String username) {
		Optional<Player> op = players.search(p -> p != null && p.getUsername().equals(Misc.formatText(username)));
		return op.isPresent() ? op.get() : null;
	}

	public static Player getPlayerByLong(long encodedName) {
		Optional<Player> op = players.search(p -> p != null && p.getLongUsername().equals(encodedName));
		return op.isPresent() ? op.get() : null;
	}

	public static void sendMessage(String message) {
		players.forEach(p -> p.getPacketSender().sendMessage(message));
	}
	
	public static void sendStaffMessage(String message) {
		players.stream().filter(p -> p != null && (p.getRights() == PlayerRights.OWNER || p.getRights() == PlayerRights.DEVELOPER || p.getRights() == PlayerRights.ADMINISTRATOR || p.getRights() == PlayerRights.MODERATOR || p.getRights() == PlayerRights.SUPPORT)).forEach(p -> p.getPacketSender().sendMessage(message));
	}
	
	public static void updateServerTime() {
		players.forEach(p -> p.getPacketSender().sendString(39161, "@or2@Server time: @or2@[ @yel@"+Misc.getCurrentServerTime()+"@or2@ ]"));
	}

	public static void updatePlayersOnline() {
		players.forEach(p -> p.getPacketSender().sendString(39160, "@or2@Players online:   @or2@[ @yel@"+(int)(players.size())+"@or2@ ]"));
		players.forEach(p -> p.getPacketSender().sendString(57003, "Players:  @gre@"+(int)(World.getPlayers().size())+""));
	}

	public static void savePlayers() {
		players.forEach(p -> p.save());
	}

	public static CharacterList<Player> getPlayers() {
		return players;
	}

	public static CharacterList<NPC> getNpcs() {
		return npcs;
	}
	
	public static void sequence() {
		
		 // Handle queued logins.
        for (int amount = 0; amount < GameSettings.LOGIN_THRESHOLD; amount++) {
            Player player = logins.poll();
            if (player == null)
                break;
            PlayerHandler.handleLogin(player);
        }

        // Handle queued logouts.
        int amount = 0;
        Iterator<Player> $it = logouts.iterator();
        while ($it.hasNext()) {
            Player player = $it.next();
            if (player == null || amount >= GameSettings.LOGOUT_THRESHOLD)
                break;
            if (PlayerHandler.handleLogout(player)) {
                $it.remove();
                amount++;
            }
        }
        
        FightPit.sequence();
		PestControl.sequence();
		ShootingStar.sequence();
		
		// First we construct the update sequences.
		UpdateSequence<Player> playerUpdate = new PlayerUpdateSequence(synchronizer, updateExecutor);
		UpdateSequence<NPC> npcUpdate = new NpcUpdateSequence();
		// Then we execute pre-updating code.
		players.forEach(playerUpdate::executePreUpdate);
		npcs.forEach(npcUpdate::executePreUpdate);
		// Then we execute parallelized updating code.
		synchronizer.bulkRegister(players.size());
		players.forEach(playerUpdate::executeUpdate);
		synchronizer.arriveAndAwaitAdvance();
		// Then we execute post-updating code.
		players.forEach(playerUpdate::executePostUpdate);
		npcs.forEach(npcUpdate::executePostUpdate);
	}
	
	public static Queue<Player> getLoginQueue() {
		return logins;
	}
	
	public static Queue<Player> getLogoutQueue() {
		return logouts;
	}
	
	public static Queue<Player> getVoteRewardingQueue() {
		return voteRewards;
	}
}
