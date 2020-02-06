package handlers.effecthandlers;

import org.l2j.gameserver.model.StatsSet;
import org.l2j.gameserver.model.actor.Creature;
import org.l2j.gameserver.model.effects.AbstractEffect;
import org.l2j.gameserver.model.items.instance.Item;
import org.l2j.gameserver.model.skills.Skill;
import org.l2j.gameserver.network.SystemMessageId;

/**
 * Mana Damage Over Time effect implementation.
 */
public final class ManaDamOverTime extends AbstractEffect {

	public final double power;
	
	public ManaDamOverTime(StatsSet params) {
		power = params.getDouble("power", 0);
		setTicks(params.getInt("ticks"));
	}
	
	@Override
	public boolean onActionTime(Creature effector, Creature effected, Skill skill, Item item) {
		if (effected.isDead()) {
			return false;
		}
		
		final double manaDam = power * getTicksMultiplier();
		if (manaDam > effected.getCurrentMp() && skill.isToggle()) {
			effected.sendPacket(SystemMessageId.YOUR_SKILL_WAS_DEACTIVATED_DUE_TO_LACK_OF_MP);
			return false;
		}
		
		effected.reduceCurrentMp(manaDam);
		return skill.isToggle();
	}
}
