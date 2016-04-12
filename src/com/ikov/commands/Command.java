package com.ikov.commands;

import com.ikov.model.PlayerRights;
import com.ikov.world.entity.impl.player.Player;

/**
 * Represents a single command.
 * @author Blake
 *
 */
public abstract class Command {
	
	/**
	 * The command name
	 */
	private String name;
	
	/**
	 * Gets the commands name.
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * The rights required to execute the command.
	 */
	private PlayerRights rights;
	
	public PlayerRights getRights() {
		return rights;
	}
	
	/**
	 * Creates a new command.
	 * @param name the command's name.
	 */
	public Command(String name) {
		this.name = name;
	}
	
	/**
	 * Creates a new command.
	 * @param name the command's name.
	 * @param rights the rights required to execute this command.
	 */
	public Command(String name, PlayerRights rights) {
		this.name = name;
		this.rights = rights;
	}

	/**
	 * Executes the command.
	 * @param player the executor of this command
	 * @param input the command input
	 * @return {@code true} if sucessful 
	 * @throws Exception
	 */
	public abstract boolean execute(Player player, String input) throws Exception;

}