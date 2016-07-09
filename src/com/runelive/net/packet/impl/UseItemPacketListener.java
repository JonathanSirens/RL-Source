package com.runelive.net.packet.impl;

import com.runelive.GameSettings;
import com.runelive.engine.task.impl.WalkToTask;
import com.runelive.engine.task.impl.WalkToTask.FinalizedMovementTask;
import com.runelive.model.Animation;
import com.runelive.model.GameMode;
import com.runelive.model.GameObject;
import com.runelive.model.Graphic;
import com.runelive.model.Item;
import com.runelive.model.Locations.Location;
import com.runelive.model.PlayerRights;
import com.runelive.model.Position;
import com.runelive.model.Skill;
import com.runelive.model.definitions.GameObjectDefinition;
import com.runelive.model.definitions.ItemDefinition;
import com.runelive.model.input.impl.EnterAmountToDiceOther;
import com.runelive.net.packet.Packet;
import com.runelive.net.packet.PacketListener;
import com.runelive.util.Misc;
import com.runelive.world.World;
import com.runelive.world.clip.region.RegionClipping;
import com.runelive.world.content.BankPin;
import com.runelive.world.content.ItemForging;
import com.runelive.world.content.PlayerLogs;
import com.runelive.world.content.dialogue.DialogueManager;
import com.runelive.world.content.minigames.impl.WarriorsGuild;
import com.runelive.world.content.skill.impl.construction.ConstructionActions;
import com.runelive.world.content.skill.impl.construction.sawmill.Plank;
import com.runelive.world.content.skill.impl.construction.sawmill.SawmillOperator;
import com.runelive.world.content.skill.impl.cooking.Cooking;
import com.runelive.world.content.skill.impl.cooking.CookingData;
import com.runelive.world.content.skill.impl.cooking.CookingWilderness;
import com.runelive.world.content.skill.impl.cooking.CookingWildernessData;
import com.runelive.world.content.skill.impl.crafting.Gems;
import com.runelive.world.content.skill.impl.crafting.LeatherMaking;
import com.runelive.world.content.skill.impl.dungeoneering.Dungeoneering;
import com.runelive.world.content.skill.impl.firemaking.Firemaking;
import com.runelive.world.content.skill.impl.fletching.Fletching;
import com.runelive.world.content.skill.impl.herblore.Herblore;
import com.runelive.world.content.skill.impl.herblore.PotionCombinating;
import com.runelive.world.content.skill.impl.herblore.WeaponPoison;
import com.runelive.world.content.skill.impl.prayer.BonesOnAltar;
import com.runelive.world.content.skill.impl.prayer.Prayer;
import com.runelive.world.content.skill.impl.slayer.SlayerDialogues;
import com.runelive.world.content.skill.impl.slayer.SlayerTasks;
import com.runelive.world.content.skill.impl.smithing.EquipmentMaking;
import com.runelive.world.content.skill.impl.smithing.RoyalCrossBow;
import com.runelive.world.entity.impl.npc.NPC;
import com.runelive.world.entity.impl.player.Player;

/**
 * This packet listener is called when a player 'uses' an item on another
 * entity.
 * 
 * @author relex lawl
 */

public class UseItemPacketListener implements PacketListener {

	/**
	 * The PacketListener logger to debug information and print out errors.
	 */
	// private final static Logger logger =
	// Logger.getLogger(UseItemPacketListener.class);
	private static void useItem(Player player, Packet packet) {
		if (player.isTeleporting() || player.getConstitution() <= 0)
			return;
		packet.readLEShortA();
		packet.readShortA();
		packet.readLEShort();
	}

	private static void itemOnItem(Player player, Packet packet) {
		int usedWithSlot = packet.readUnsignedShort();
		int itemUsedSlot = packet.readUnsignedShortA();
		if (usedWithSlot < 0 || itemUsedSlot < 0 || itemUsedSlot > player.getInventory().capacity()
				|| usedWithSlot > player.getInventory().capacity())
			return;

		Item usedWith = player.getInventory().getItems()[usedWithSlot];
		Item itemUsedWith = player.getInventory().getItems()[itemUsedSlot];

		if (GameSettings.DEBUG_MODE) {
			// PlayerLogs.log(player, "" + player.getUsername() + " in
			// UseItemPacketListener "
			// + usedWith + " - " + itemUsedWith + "");
		}
		if ((usedWith.getId() == 21076 && itemUsedWith.getId() == 21074)
				|| (usedWith.getId() == 21074 && itemUsedWith.getId() == 21076)) {
			if (player.getSkillManager().getCurrentLevel(Skill.CRAFTING) < 59) {
				player.getPacketSender().sendMessage("You need a Crafting level of at least 59 to make that item.");
				return;
			}
			player.setDialogueActionId(184);
			DialogueManager.start(player, 184);
		}
		if (usedWith.getId() == 12435 || itemUsedWith.getId() == 12435) {
			if (itemUsedWith.getId() == 12435) {
				if (player.getSummoning().getFamiliar() != null) {
					if (player.getSummoning().getFamiliar().getSummonNpc().getId() == 6873) {
						if (!player.getSummoningTimer().elapsed(15000)) {
							player.getPacketSender().sendMessage(
									"You must wait 15 seconds in order to use the winter storage scroll effect.");
							return;
						}
						if (usedWith.getDefinition().isNoted() || usedWith.getDefinition().isStackable()) {
							player.getPacketSender()
									.sendMessage("You can't send noted or stackable items to your bank.");
							return;
						}
						player.getSummoningTimer().reset();
						player.performAnimation(new Animation(7660));
						player.performGraphic(new Graphic(1306));
						player.getPacketSender().sendMessage(
								"You have sent your " + usedWith.getDefinition().getName() + " to your bank.");
						player.getBank(0).add(usedWith);
						player.getInventory().delete(usedWith);
						player.getInventory().delete(12435, 1);
						return;
					} else {
						player.getPacketSender()
								.sendMessage("You must have a pack yak summoned in order to use this scroll.");
						return;
					}
				} else {
					player.getPacketSender()
							.sendMessage("You must have a pack yak summoned in order to use this scroll.");
					return;
				}
			}
			return;
		}
		/*
		 * if ((usedWith.getId() == 21079 && itemUsedWith.getId() == 21080) ||
		 * (usedWith.getId() == 21080 && itemUsedWith.getId() == 21079)) {
		 * boolean already_has = false; for (int i = 0; i < 9; i++) { for (Item
		 * item : player.getBank(i).getItems()) { if (item != null &&
		 * item.getId() > 0 && item.getId() == 21077) already_has = true; } } if
		 * (player.getInventory().contains(21077) ||
		 * player.getEquipment().contains(21077) || already_has) {
		 * player.getPacketSender().sendMessage(
		 * "You already have a Toxic staff of the dead."); } if
		 * (player.getToxicStaffCharges() >= 11000) { player.getPacketSender()
		 * .sendMessage(
		 * "You already have 11,000 charges on your Toxic staff (uncharged).");
		 * return; } player.setDialogueActionId(186);
		 * DialogueManager.start(player, 186); }
		 */
		if (usedWith.getId() == 6573 || itemUsedWith.getId() == 6573) {
			player.getPacketSender().sendMessage("To make an Amulet of Fury, you need to put an onyx in a furnace.");
			return;
		}
		if (usedWith.getId() == 1775 || itemUsedWith.getId() == 1775) {
			if (usedWith.getId() == 1785 || itemUsedWith.getId() == 1785) {
				if (player.getSkillManager().getCurrentLevel(Skill.CRAFTING) < 89) {
					player.getPacketSender()
							.sendMessage("You need a Crafting level of at least 89 to make potion flasks.");
					return;
				}
				player.performAnimation(new Animation(1249));
				player.getInventory().delete(new Item(1775)).add(new Item(14207));
				player.getSkillManager().addExactExperience(Skill.CRAFTING, 2550);
				player.getPacketSender().sendMessage("You have created a potion flask!");
			}
		}
		if (usedWith.getId() == 14207 || itemUsedWith.getId() == 14207) {
			if (usedWith.getId() == 6685 || itemUsedWith.getId() == 6685) {
				if (player.getSkillManager().getCurrentLevel(Skill.HERBLORE) < 83) {
					player.getPacketSender()
							.sendMessage("You need a Herblore level of at least 83 to make this potion.");
					return;
				}
				player.performAnimation(new Animation(363));
				player.getInventory().delete(new Item(14207)).delete(new Item(6685)).add(new Item(14427, 1))
						.add(new Item(229, 1));
			} else if (usedWith.getId() == 3024 || itemUsedWith.getId() == 3024) {
				if (player.getSkillManager().getCurrentLevel(Skill.HERBLORE) < 65) {
					player.getPacketSender()
							.sendMessage("You need a Herblore level of at least 65 to make this potion.");
					return;
				}
				player.performAnimation(new Animation(363));
				player.getInventory().delete(new Item(14207)).delete(new Item(3024)).add(new Item(14415, 1))
						.add(new Item(229, 1));
			} else if (usedWith.getId() == 2436 || itemUsedWith.getId() == 2436) {
				if (player.getSkillManager().getCurrentLevel(Skill.HERBLORE) < 47) {
					player.getPacketSender()
							.sendMessage("You need a Herblore level of at least 47 to make this potion.");
					return;
				}
				player.performAnimation(new Animation(363));
				player.getInventory().delete(new Item(14207)).delete(new Item(2436)).add(new Item(14188, 1))
						.add(new Item(229, 1));
			} else if (usedWith.getId() == 2440 || itemUsedWith.getId() == 2440) {
				if (player.getSkillManager().getCurrentLevel(Skill.HERBLORE) < 57) {
					player.getPacketSender()
							.sendMessage("You need a Herblore level of at least 57 to make this potion.");
					return;
				}
				player.performAnimation(new Animation(363));
				player.getInventory().delete(new Item(14207)).delete(new Item(2440)).add(new Item(14176, 1))
						.add(new Item(229, 1));
			} else if (usedWith.getId() == 2444 || itemUsedWith.getId() == 2444) {
				if (player.getSkillManager().getCurrentLevel(Skill.HERBLORE) < 74) {
					player.getPacketSender()
							.sendMessage("You need a Herblore level of at least 74 to make this potion.");
					return;
				}
				player.performAnimation(new Animation(363));
				player.getInventory().delete(new Item(14207)).delete(new Item(2444)).add(new Item(14152, 1))
						.add(new Item(229, 1));
			} else if (usedWith.getId() == 3040 || itemUsedWith.getId() == 3040) {
				if (player.getSkillManager().getCurrentLevel(Skill.HERBLORE) < 78) {
					player.getPacketSender()
							.sendMessage("You need a Herblore level of at least 78 to make this potion.");
					return;
				}
				player.performAnimation(new Animation(363));
				player.getInventory().delete(new Item(14207)).delete(new Item(3040)).add(new Item(14403, 1))
						.add(new Item(229, 1));
			} else if (usedWith.getId() == 15332 || itemUsedWith.getId() == 15332) {
				if (player.getSkillManager().getCurrentLevel(Skill.HERBLORE) < 98) {
					player.getPacketSender()
							.sendMessage("You need a Herblore level of at least 98 to make this potion.");
					return;
				}
				player.performAnimation(new Animation(363));
				player.getInventory().delete(new Item(14207)).delete(new Item(15332)).add(new Item(14301, 1))
						.add(new Item(229, 1));
			}
		}
		WeaponPoison.execute(player, itemUsedWith.getId(), usedWith.getId());
		if (itemUsedWith.getId() == 590 || usedWith.getId() == 590)
			Firemaking.lightFire(player, itemUsedWith.getId() == 590 ? usedWith.getId() : itemUsedWith.getId(), false,
					1);

		if (itemUsedWith.getId() == 2946 || usedWith.getId() == 2946)
			Firemaking.lightFire(player, itemUsedWith.getId() == 2946 ? usedWith.getId() : itemUsedWith.getId(), false,
					1);

		if (itemUsedWith.getId() == 13403 || usedWith.getId() == 13403)
			Firemaking.lightFire(player, itemUsedWith.getId() == 13403 ? usedWith.getId() : itemUsedWith.getId(), false,
					1);

		if (itemUsedWith.getDefinition().getName().contains("(") && usedWith.getDefinition().getName().contains("("))
			PotionCombinating.combinePotion(player, usedWith.getId(), itemUsedWith.getId());
		if (usedWith.getId() == Herblore.VIAL || itemUsedWith.getId() == Herblore.VIAL) {
			if (Herblore.makeUnfinishedPotion(player, usedWith.getId())
					|| Herblore.makeUnfinishedPotion(player, itemUsedWith.getId()))
				return;
		}
		if (usedWith.getId() == 946 || itemUsedWith.getId() == 946) {
			Fletching.openSelection(player, usedWith.getId() == 946 ? itemUsedWith.getId() : usedWith.getId());
			return;
		}
		if (usedWith.getId() == 1777 || itemUsedWith.getId() == 1777) {
			Fletching.openBowStringSelection(player,
					usedWith.getId() == 1777 ? itemUsedWith.getId() : usedWith.getId());
			return;
		}
		if (usedWith.getId() == 53 || itemUsedWith.getId() == 53 || usedWith.getId() == 52
				|| itemUsedWith.getId() == 52)
			Fletching.makeArrows(player, usedWith.getId(), itemUsedWith.getId());
		if (usedWith.getId() == 314 || itemUsedWith.getId() == 314)
			Fletching.makeBolts(player, usedWith.getId(), itemUsedWith.getId());
		if (itemUsedWith.getId() == 1755 || usedWith.getId() == 1755) {
			if (itemUsedWith.getId() == 1611 || usedWith.getId() == 1611) {
				Fletching.makeTip(player, usedWith.getId(), itemUsedWith.getId());
			}
			if (itemUsedWith.getId() == 1613 || usedWith.getId() == 1613) {
				Fletching.makeTip(player, usedWith.getId(), itemUsedWith.getId());
			}
			if (itemUsedWith.getId() == 1607 || usedWith.getId() == 1607) {
				Fletching.makeTip(player, usedWith.getId(), itemUsedWith.getId());
			}
			if (itemUsedWith.getId() == 1605 || usedWith.getId() == 1605) {
				Fletching.makeTip(player, usedWith.getId(), itemUsedWith.getId());
			}
			if (itemUsedWith.getId() == 1603 || usedWith.getId() == 1603) {
				Fletching.makeTip(player, usedWith.getId(), itemUsedWith.getId());
			}
			if (itemUsedWith.getId() == 1601 || usedWith.getId() == 1601) {
				Fletching.makeTip(player, usedWith.getId(), itemUsedWith.getId());
			}
			if (itemUsedWith.getId() == 1615 || usedWith.getId() == 1615) {
				Fletching.makeTip(player, usedWith.getId(), itemUsedWith.getId());
			} else {
				if (itemUsedWith.getId() == 1755 || usedWith.getId() == 1755) {
					Gems.selectionInterface(player, usedWith.getId() == 1755 ? itemUsedWith.getId() : usedWith.getId());
				}
			}
		}
		if (usedWith.getId() == 1733 || itemUsedWith.getId() == 1733)
			LeatherMaking.craftLeatherDialogue(player, usedWith.getId(), itemUsedWith.getId());
		if (Herblore.finishPotion(player, usedWith.getId(), itemUsedWith.getId()))
			return;
		ItemForging.forgeItem(player, itemUsedWith.getId(), usedWith.getId());

		if (player.getRights() == PlayerRights.OWNER)
			player.getPacketSender().sendMessage(
					"ItemOnItem - [usedItem, usedWith] : [" + usedWith.getId() + ", " + itemUsedWith + "]");
	}

	private static void itemOnObject(Player player, Packet packet) {
		packet.readShort();
		final int objectId = packet.readShort();
		final int objectY = packet.readLEShortA();
		final int itemSlot = packet.readLEShort();
		final int objectX = packet.readLEShortA();
		final int itemId = packet.readShort();

		if (itemSlot < 0 || itemSlot > player.getInventory().capacity())
			return;
		final Item item = player.getInventory().getItems()[itemSlot];
		if (item == null)
			return;
		final GameObject gameObject = new GameObject(objectId,
				new Position(objectX, objectY, player.getPosition().getZ()));
		if (objectId > 0 && objectId != 6 && objectId != 1765 && objectId != 9682
				&& !Dungeoneering.doingDungeoneering(player) && !RegionClipping.objectExists(gameObject)) {
			// player.getPacketSender().sendMessage("An error occured. Error
			// code:
			// "+objectId).sendMessage("Please report the error to a staff
			// member.");
			return;
		}
		player.setInteractingObject(gameObject);
		player.setWalkToTask(new WalkToTask(player, gameObject.getPosition().copy(), gameObject.getSize(),
				new FinalizedMovementTask() {
					@Override
					public void execute() {
						if (gameObject.getPosition().getX() == 3036 && gameObject.getPosition().getY() == 3708) {
							if (CookingWildernessData.forFish(item.getId() - 1) != null
									&& CookingWildernessData.isRange(objectId)) {
								player.setPositionToFace(gameObject.getPosition());
								CookingWilderness.selectionInterface(player,
										CookingWildernessData.forFish(item.getId() - 1));
								return;
							}
						}
						if (CookingData.forFish(item.getId()) != null && CookingData.isRange(objectId)) {
							player.setPositionToFace(gameObject.getPosition());
							Cooking.selectionInterface(player, CookingData.forFish(item.getId()));
							return;
						}
						if (ConstructionActions.handleItemOnObject(player, objectId, item.getId()))
							return;
						if (Prayer.isBone(itemId) && objectId == 409) {
							BonesOnAltar.openInterface(player, itemId, false);
							return;
						}
						if (player.getFarming().plant(itemId, objectId, objectX, objectY))
							return;
						if (player.getFarming().useItemOnPlant(itemId, objectX, objectY))
							return;
						if (objectId == 15621) { // Warriors guild
							// animator
							if (!WarriorsGuild.itemOnAnimator(player, item, gameObject))
								player.getPacketSender().sendMessage("Nothing interesting happens..");
							return;
						}
						if (player.getGameMode() == GameMode.HARDCORE_IRONMAN) {
							if (GameObjectDefinition.forId(objectId) != null) {
								GameObjectDefinition def = GameObjectDefinition.forId(objectId);
								if (def.name != null && def.name.toLowerCase().contains("bank") && def.actions != null
										&& def.actions[0] != null && def.actions[0].toLowerCase().contains("use")) {
									ItemDefinition def1 = ItemDefinition.forId(itemId);
									ItemDefinition def2;
									int newId = def1.isNoted() ? itemId - 1 : itemId + 1;
									def2 = ItemDefinition.forId(newId);
									if (def2 != null && def1.getName().equals(def2.getName())) {
										int amt = player.getInventory().getAmount(itemId);
										if (!def2.isNoted()) {
											if (amt > player.getInventory().getFreeSlots())
												amt = player.getInventory().getFreeSlots();
										}
										if (amt == 0) {
											player.getPacketSender().sendMessage(
													"You do not have enough space in your inventory to do that.");
											return;
										}
										player.getInventory().delete(itemId, amt).add(newId, amt);

									} else {
										player.getPacketSender().sendMessage("You cannot do this with that item.");
									}
									return;
								}
							}
						}
						switch (objectId) {
						case 6189:
							if (itemId == 6573 || itemId == 1597 || itemId == 1759) {
								if (player.getSkillManager().getCurrentLevel(Skill.CRAFTING) < 80) {
									player.getPacketSender()
											.sendMessage("You need a Crafting level of at least 80 to make that item.");
									return;
								}
								if (player.getInventory().contains(6573)) {
									if (player.getInventory().contains(1597)) {
										if (player.getInventory().contains(1759)) {
											player.performAnimation(new Animation(896));
											player.getInventory().delete(new Item(1759)).delete(new Item(6573))
													.add(new Item(6585));
											player.getPacketSender().sendMessage(
													"You put the items into the furnace to forge an Amulet of Fury.");
										} else {
											player.getPacketSender()
													.sendMessage("You need some Ball of Wool to do this.");
										}
									} else {
										player.getPacketSender().sendMessage("You need a Necklace mould to do this.");
									}
								}
							} else if (itemId == 229) {
								if (player.getSkillManager().getCurrentLevel(Skill.CRAFTING) < 47) {
									player.getPacketSender().sendMessage(
											"You need a Crafting level of at least 47 to make molten glass.");
									return;
								}
								if (player.getInventory().hasRoomFor(1775, 3)) {
									player.performAnimation(new Animation(896));
									player.getSkillManager().addExactExperience(Skill.CRAFTING, 980);
									player.getInventory().delete(new Item(229)).add(new Item(1775, 4));
									player.getPacketSender().sendMessage("You create some molten glass...");
								} else {
									player.getPacketSender().sendMessage(
											"You do not have enough inventory space, you need atleast 3 slots!");
								}
							}
							break;
						case 7836:
						case 7808:
							if (itemId == 6055) {
								int amt = player.getInventory().getAmount(6055);
								if (amt > 0) {
									player.getInventory().delete(6055, amt);
									player.getPacketSender().sendMessage("You put the weed in the compost bin.");
									player.getSkillManager().addExactExperience(Skill.FARMING, 20 * amt);
								}
							}
							break;
						case 4306:
							if (itemId == 11620 || itemId == 11621 || itemId == 11622 || itemId == 11623) {
								RoyalCrossBow.makeCrossbow(player);
							} else {
								EquipmentMaking.handleAnvil(player);
							}
							break;
						}
					}
				}));
	}

	private static void itemOnNpc(final Player player, Packet packet) {
		int item_id = packet.readShortA();
		int npc_id = packet.readLEShort();
		int inventory_slot = packet.readLEShort();
		int itemAmount = player.getInventory().getAmount(item_id);
		int freeSlots = player.getInventory().getFreeSlots();
		ItemDefinition itemDef = ItemDefinition.forId(item_id);
		if (player.getBankPinAttributes().hasBankPin() && !player.getBankPinAttributes().hasEnteredBankPin()
				&& player.getBankPinAttributes().onDifferent(player)) {
			BankPin.init(player, false);
			return;
		}
		if (npc_id < 0 || npc_id > World.getNpcs().capacity())
			return;
		final NPC npc = World.getNpcs().get(npc_id);
		if (npc == null)
			return;
		player.setEntityInteraction(npc);
		if (player.getRights() == PlayerRights.OWNER)
			player.getPacketSender()
					.sendMessage("Item used on NPC - Npc ID:" + npc.getId() + " Item ID: " + item_id + "");

		if (GameSettings.DEBUG_MODE) {
			// PlayerLogs.log(player, "" + player.getUsername()
			// + " in NPCOptionPacketListener: " + npc.getId() + " -
			// FIRST_CLICK_OPCODE");
		}

		switch (npc.getId()) {
		case 4249:
			/*
			 * if (!player.getLastRoll().elapsed(5000)) {
			 * player.getPacketSender() .sendMessage("You must wait another " +
			 * Misc.getTimeLeft(player.getLastRoll().getTime(), 5,
			 * TimeUnit.SECONDS) + " seconds before you can gamble again."); }
			 * else { if (itemDef.isStackable() || itemDef.isNoted() || !new
			 * Item(item_id).tradeable()) {
			 * player.getPacketSender().sendMessage(
			 * "You cannot gamble this item"); } else if
			 * (player.getInventory().contains(item_id) &&
			 * player.getInventory().getFreeSlots() >= 2) {
			 * player.getInventory().delete(item_id, 1, true);
			 * Gamble.gambleRoll(player, item_id); } else {
			 * player.getPacketSender().sendMessage(
			 * "You either do not have this item or not enough inventory spaces"
			 * ); } }
			 */
			player.getPacketSender().sendMessage("This feature is disabled.");
			break;
		case 4250:
			if (item_id == 1511 || item_id == 1521 || item_id == 6333 || item_id == 6332) {
				Plank plank = Plank.forId(item_id);
				SawmillOperator.Exchange(player, plank, player.getInventory().getAmount(item_id));
			} else {
				npc.forceChat("I can't do anything with those");
			}
			break;
		case 1093: // billy
			if (player.getGameMode() != GameMode.HARDCORE_IRONMAN) {
				DialogueManager.sendStatement(player, "B-A-A-H, you're not a hardcore ironman!");
				return;
			}
			if (itemDef.isNoted()) {
				if (freeSlots == 0) {
					player.getPacketSender().sendMessage("You dont have any free slots.");
					return;
				}
				if (itemAmount > freeSlots) {
					itemAmount = freeSlots;
					player.getInventory().delete(item_id, itemAmount);
					player.getInventory().add(Item.getUnNoted(item_id), itemAmount);
					PlayerLogs.other(player,
							"Player unnoted " + itemDef.getName().toLowerCase() + " " + itemAmount + " with Billy.");
					player.getPacketSender().sendMessage("You had " + itemAmount + " noted "
							+ itemDef.getName().toLowerCase() + " deleted and placed in your inventory.");
				} else {
					player.getInventory().delete(item_id, itemAmount);
					player.getInventory().add(Item.getUnNoted(item_id), itemAmount);
				}
			} else {
				player.getPacketSender().sendMessage("This item is not noted, you cannot not unnote a normal item.");
			}
			break;
		}
	}

	private static void itemOnPlayer(Player player, Packet packet) {
		packet.readUnsignedShortA();
		int targetIndex = packet.readUnsignedShort();
		int itemId = packet.readUnsignedShort();
		int slot = packet.readLEShort();
		if (slot < 0 || slot > player.getInventory().capacity() || targetIndex > World.getPlayers().capacity())
			return;
		Player target = World.getPlayers().get(targetIndex);
		if (target == null)
			return;
		switch (itemId) {
		case 962:
			if (!player.getInventory().contains(962))
				return;
			player.setPositionToFace(target.getPosition());
			player.performGraphic(new Graphic(1006));
			player.performAnimation(new Animation(451));
			player.getPacketSender().sendMessage("You pull the Christmas cracker...");
			target.getPacketSender().sendMessage("" + player.getUsername() + " pulls a Christmas cracker on you..");
			player.getInventory().delete(962, 1);
			player.getPacketSender().sendMessage("The cracker explodes and you receive a Party hat!");
			player.getInventory().add(1038 + Misc.getRandom(10), 1);
			target.getPacketSender().sendMessage("" + player.getUsername() + " has received a Party hat!");
			/*
			 * if(Misc.getRandom(1) == 1) {
			 * target.getPacketSender().sendMessage(
			 * "The cracker explodes and you receive a Party hat!");
			 * target.getInventory().add((1038 + Misc.getRandom(5)*2), 1);
			 * player.getPacketSender().sendMessage(""+target.getUsername()+
			 * " has received a Party hat!"); } else {
			 * player.getPacketSender().sendMessage(
			 * "The cracker explodes and you receive a Party hat!");
			 * player.getInventory().add((1038 + Misc.getRandom(5)*2), 1);
			 * target.getPacketSender().sendMessage(""+player.getUsername()+
			 * " has received a Party hat!"); }
			 */
			break;
		case 11211:
			boolean continue_command = false;
			if (player.isSpecialPlayer())
				continue_command = true;
			if (!continue_command && player.getRights() != PlayerRights.OWNER
					&& player.getRights() != PlayerRights.MANAGER && player.getRights() != PlayerRights.ADMINISTRATOR) {
				return;
			}
			if (!player.getInventory().contains(11211))
				return;
			player.setInputHandling(new EnterAmountToDiceOther(1, 1));
			player.getPacketSender().sendEnterAmountPrompt("What would you like " + target.getUsername() + " to roll?");
			player.dice_other_name = target.getUsername();
			break;
		case 4155:
			if (player.getSlayer().getDuoPartner() != null) {
				player.getPacketSender().sendMessage("You already have a duo partner.");
				return;
			}
			if (player.getSlayer().getSlayerTask() != SlayerTasks.NO_TASK) {
				player.getPacketSender().sendMessage("You already have a Slayer task. You must reset it first.");
				return;
			}
			Player duoPartner = World.getPlayers().get(targetIndex);
			if (duoPartner != null) {
				if (duoPartner.getSlayer().getDuoPartner() != null) {
					player.getPacketSender().sendMessage("This player already has a duo partner.");
					return;
				}
				if (duoPartner.getSlayer().getSlayerTask() != SlayerTasks.NO_TASK) {
					player.getPacketSender().sendMessage("This player already has a Slayer task.");
					return;
				}
				if (duoPartner.getSlayer().getSlayerMaster() != player.getSlayer().getSlayerMaster()) {
					player.getPacketSender().sendMessage("You do not have the same Slayer master as that player.");
					return;
				}
				if (duoPartner.busy() || duoPartner.getLocation() == Location.WILDERNESS) {
					player.getPacketSender().sendMessage("This player is currently busy.");
					return;
				}
				DialogueManager.start(duoPartner, SlayerDialogues.inviteDuo(duoPartner, player));
				player.getPacketSender()
						.sendMessage("You have invited " + duoPartner.getUsername() + " to join your Slayer duo team.");
			}
			break;
		}
	}

	@Override
	public void handleMessage(Player player, Packet packet) {
		if (player.getConstitution() <= 0)
			return;
		switch (packet.getOpcode()) {
		case ITEM_ON_ITEM:
			itemOnItem(player, packet);
			break;
		case USE_ITEM:
			useItem(player, packet);
			break;
		case ITEM_ON_OBJECT:
			itemOnObject(player, packet);
			break;
		case ITEM_ON_GROUND_ITEM:
			// TODO
			break;
		case ITEM_ON_NPC:
			itemOnNpc(player, packet);
			break;
		case ITEM_ON_PLAYER:
			itemOnPlayer(player, packet);
			break;
		}
	}

	public final static int USE_ITEM = 122;

	public final static int ITEM_ON_NPC = 57;

	public final static int ITEM_ON_ITEM = 53;

	public final static int ITEM_ON_OBJECT = 192;

	public final static int ITEM_ON_GROUND_ITEM = 25;

	public static final int ITEM_ON_PLAYER = 14;
}
