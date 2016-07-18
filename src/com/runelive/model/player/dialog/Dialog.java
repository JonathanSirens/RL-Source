package com.runelive.model.player.dialog;

import com.runelive.model.options.Option;
import com.runelive.util.FontUtils;
import com.runelive.world.entity.impl.player.Player;

public abstract class Dialog {

	public static DialogMessage createNpc(String string) {
		return create(DialogType.NPC, FontUtils.wrapText(FontUtils.FontSize.FANCY, string, 350));
	}

	public static DialogMessage createPlayer(String string) {
		return create(DialogType.PLAYER, FontUtils.wrapText(FontUtils.FontSize.FANCY, string, 350));
	}

	public static DialogMessage create(DialogType type, String ...lines) {
		return new DialogMessage(type, lines);
	}

	public static DialogMessage createNpc(String ...lines) {
		return create(DialogType.NPC, lines);
	}

	public static DialogMessage createPlayer(String ...lines) {
		return create(DialogType.PLAYER, lines);
	}

	public static DialogMessage createStatement(String ...lines) {
		return create(DialogType.STATEMENT, lines);
	}

	public static DialogMessage createOption(Option option) {
		return new DialogMessage(option);
	}

	public static DialogMessage createEmpty() {
		return create(DialogType.EMPTY);
	}

	public Dialog(Player player) {
		this.player = player;
		this.state = 0;
	}

	/**
	 * Returns the message as a string array for the given id.
	 * @return
	 */

	public abstract DialogMessage getMessage();

	/**
	 * Sets the id at which the dialog ends.
	 * @param id
	 */
	public void setEndState(int id) {
		this.endState = id;
	}

	/**
	 * Sets the state of the dialog.
	 * @param id
	 */
	public void setState(int id) {
		this.state = id;
	}

	/**
	 * Returns the next id in the dialog.
	 * @return
	 */
	public boolean finished() {
		return this.state == this.endState;
	}

	/**
	 * Returns the Player of the dialog.
	 * @return
	 */
	public Player getPlayer() {
		return this.player;
	}

	/**
	 * Increments the dialog state.
	 */
	public void incrementState() {
		this.state++;
	}

	/**
	 * Returns the state of the dialog.
	 * @return
	 */
	public int getState() {
		return this.state;
	}

	private Player player;
	private int state;
	private int endState;

}
