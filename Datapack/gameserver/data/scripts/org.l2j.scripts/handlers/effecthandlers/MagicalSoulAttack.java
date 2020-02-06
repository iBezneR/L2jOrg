package handlers.effecthandlers;

import org.l2j.gameserver.enums.ShotType;
import org.l2j.gameserver.model.StatsSet;
import org.l2j.gameserver.model.actor.Creature;
import org.l2j.gameserver.model.effects.AbstractEffect;
import org.l2j.gameserver.model.effects.EffectType;
import org.l2j.gameserver.model.items.instance.Item;
import org.l2j.gameserver.model.skills.Skill;
import org.l2j.gameserver.model.stats.Formulas;
import org.l2j.gameserver.network.SystemMessageId;

import static org.l2j.gameserver.network.serverpackets.SystemMessage.getSystemMessage;
import static org.l2j.gameserver.util.GameUtils.isPlayer;

/**
 * Magical Soul Attack effect implementation.
 * @author Adry_85
 */
public final class MagicalSoulAttack extends AbstractEffect {
	public final double power;
	
	public MagicalSoulAttack(StatsSet params)
	{
		power = params.getDouble("power", 0);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.MAGICAL_ATTACK;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, Item item) {
		if (effector.isAlikeDead()) {
			return;
		}
		
		if (isPlayer(effected) && effected.getActingPlayer().isFakeDeath()) {
			effected.stopFakeDeath(true);
		}
		
		final int chargedSouls = Math.min(skill.getMaxSoulConsumeCount(), effector.getActingPlayer().getCharges());
		if (!effector.getActingPlayer().decreaseCharges(chargedSouls)) {
			effector.sendPacket(getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(skill));
			return;
		}
		
		final boolean sps = skill.useSpiritShot() && effector.isChargedShot(ShotType.SPIRITSHOTS);
		final boolean bss = skill.useSpiritShot() && effector.isChargedShot(ShotType.BLESSED_SPIRITSHOTS);
		final boolean mcrit = Formulas.calcCrit(skill.getMagicCriticalRate(), effector, effected, skill);
		final double mAtk = effector.getMAtk() * (chargedSouls > 0 ? (1.3 + (chargedSouls * 0.05)) : 1);
		final double damage = Formulas.calcMagicDam(effector, effected, skill, mAtk, power, effected.getMDef(), sps, bss, mcrit);
		
		effector.doAttack(damage, effected, skill, false, false, mcrit, false);
	}
}