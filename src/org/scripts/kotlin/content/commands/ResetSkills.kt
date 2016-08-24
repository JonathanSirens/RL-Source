package org.scripts.kotlin.content.commands

import com.chaos.model.Flag
import com.chaos.model.StaffRights
import com.chaos.model.Skill
import com.chaos.model.player.command.Command
import com.chaos.world.content.skill.SkillManager
import com.chaos.world.entity.impl.player.Player

/**
 * "The digital revolution is far more significant than the invention of writing or even of printing." - Douglas
 * Engelbart
 * Created on 8/6/2016.

 * @author Seba
 */
class ResetSkills(staffRights: StaffRights) : Command(staffRights) {

    override fun execute(player: Player, args: Array<String>?, privilege: StaffRights) {
        for (skill in Skill.values()) {
            val level = if (skill == Skill.CONSTITUTION) 100 else if (skill == Skill.PRAYER) 10 else 1
            player.skillManager.setCurrentLevel(skill, level).setMaxLevel(skill, level).setExperience(skill,
                    SkillManager.getExperienceForLevel(if (skill == Skill.CONSTITUTION) 10 else 1))
        }
        player.packetSender.sendMessage("Your skill levels have now been reset.")
        player.updateFlag.flag(Flag.APPEARANCE)
    }
}
