package com.ikov.commands.ranks;

import com.ikov.GameSettings;
import com.ikov.model.Locations.Location;
import com.ikov.model.Position;
import com.ikov.util.Misc;
import com.ikov.world.World;
import com.ikov.world.content.PlayerLogs;
import com.ikov.world.content.PlayerPunishment;
import com.ikov.world.content.transportation.TeleportHandler;
import com.ikov.world.content.transportation.TeleportType;
import com.ikov.world.entity.impl.player.Player;
import com.ikov.world.entity.impl.player.PlayerSaving;
import com.ikov.util.ForumDatabase;
import com.ikov.model.PlayerRights;

public class WikiManagers {
	
	/**
	* @Author Jonathan Sirens
	* Initiates Command
	**/
	
	public static void initiate_command(final Player player, String[] command, String wholeCommand) {
		String other_player_name = "invalid_name_process";
		switch(command[0].toLowerCase()) {
			case "forumpromote":
				if(!GameSettings.FORUM_DATABASE_CONNECTIONS) {
					player.getPacketSender().sendMessage("Forum database connections are currently turned off.");
					return;
				}
				if(player.getForumDelay().elapsed(20000)) {
					player.getPacketSender().sendMessage("Please wait another 20 seconds before connecting to the forum...");
					return;
				}
				other_player_name = wholeCommand.substring(13);
				try {
					ForumDatabase.connect();
					int current_rank_id = ForumDatabase.getCurrentMemberID(other_player_name);
					if(current_rank_id != ForumDatabase.regular_donator 
					&& current_rank_id != ForumDatabase.super_donator
					&& current_rank_id != ForumDatabase.extreme_donator
					&& current_rank_id != ForumDatabase.legendary_donator
					&& current_rank_id != ForumDatabase.uber_donator
					&& current_rank_id != ForumDatabase.validating
					&& current_rank_id != ForumDatabase.members) {
						player.getPacketSender().sendMessage("This player has a rank on the forum that is not supported.");
						return;
					} else if(current_rank_id == ForumDatabase.banned) {
						player.getPacketSender().sendMessage("This forum account is banned.");
						return;
					}
					if(ForumDatabase.check_has_username(other_player_name)) {
						player.getForumDelay().reset();
						ForumDatabase.promote_wiki_editor(other_player_name);
						player.getPacketSender().sendMessage("You have given "+other_player_name+" the rank Wiki Editor on the forum.");
					} else {
						player.getPacketSender().sendMessage("This player does not have a forum account under this name.");
					}
					ForumDatabase.destroy_connection();
				} catch (Exception e) {
					System.out.println(e);
				}
			break;
			case "forumdemote":
				if(!GameSettings.FORUM_DATABASE_CONNECTIONS) {
					player.getPacketSender().sendMessage("Forum database connections are currently turned off.");
					return;
				}
				if(player.getForumDelay().elapsed(20000)) {
					player.getPacketSender().sendMessage("Please wait another 20 seconds before connecting to the forum...");
					return;
				}
				other_player_name = wholeCommand.substring(12);
				try {
					ForumDatabase.connect();
					int current_rank_id = ForumDatabase.getCurrentMemberID(other_player_name);
					if(current_rank_id != ForumDatabase.wiki_editor) {
						player.getPacketSender().sendMessage("This player does not have Wiki Editor on the forum.");
						return;
					} else if(current_rank_id == ForumDatabase.banned) {
						player.getPacketSender().sendMessage("This forum account is banned.");
						return;
					}
					if(ForumDatabase.check_has_username(other_player_name)) {
						player.getForumDelay().reset();
						ForumDatabase.demote_wiki_editor(other_player_name);
						player.getPacketSender().sendMessage("You have demoted "+other_player_name+" from the rank Wiki Editor on the forum.");
					} else {
						player.getPacketSender().sendMessage("This player does not have a forum account under this name.");
					}
					ForumDatabase.destroy_connection();
				} catch (Exception e) {
					System.out.println(e);
				}
			break;
			case "promote":
				Player target = World.getPlayerByName(wholeCommand.substring(8));
				if (target == null) {
					player.getPacketSender().sendMessage("Player must be online to give them wiki editor.");
				} else {
					target.setRights(PlayerRights.WIKI_EDITOR);
					target.getPacketSender().sendMessage("You have been given Wiki Editor!");
					player.getPacketSender().sendMessage("You have given "+wholeCommand.substring(8)+" the rank Wiki Editor!");
					target.getPacketSender().sendRights();
				}
			break;
			case "demote":
				Player target = World.getPlayerByName(wholeCommand.substring(7));
				if (target == null) {
					player.getPacketSender().sendMessage("Player must be online to demote them!");
				} else {
					target.setRights(PlayerRights.PLAYER);
					target.getPacketSender().sendMessage("Your Wiki Editor rank has been taken.");
					player.getPacketSender().sendMessage("You have demoted "+wholeCommand.substring(7)+" from the rank Wiki Editor.");
					target.getPacketSender().sendRights();
				}
			break;
		}
	}
	
}