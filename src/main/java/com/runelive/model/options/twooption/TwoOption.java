package com.runelive.model.options.twooption;

import com.runelive.model.options.Option;
import com.runelive.world.entity.impl.player.Player;

public abstract class TwoOption extends Option {
	private final String firstOption;
	private final String secondOption;

	protected TwoOption(String s1, String s2) {
		this.firstOption = s1;
		this.secondOption = s2;
	}

	@Override
	protected final void display(Player player) {
		player.getDialog().sendOption2(firstOption, secondOption);
	}
}
