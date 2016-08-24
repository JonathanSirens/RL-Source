package com.chaos.model.options.threeoption;

import com.chaos.model.options.Option;
import com.chaos.world.entity.impl.player.Player;

public abstract class ThreeOption extends Option {
	private final String firstOption;
	private final String secondOption;
	private final String thirdOption;

	protected ThreeOption(String firstOption, String secondOption, String thirdOption) {
		this.firstOption = firstOption;
		this.secondOption = secondOption;
		this.thirdOption = thirdOption;
	}

	@Override
	protected final void display(Player player) {
		player.getDialog().sendOption3(firstOption, secondOption, thirdOption);
	}
}