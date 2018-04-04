package l2server.gameserver.model.actor.status;

import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.SiegeFlagInstance;

public class SiegeFlagStatus extends NpcStatus {
	public SiegeFlagStatus(SiegeFlagInstance activeChar) {
		super(activeChar);
	}

	@Override
	public void reduceHp(double value, Creature attacker) {
		reduceHp(value, attacker, true, false, false);
	}

	@Override
	public void reduceHp(double value, Creature attacker, boolean awake, boolean isDOT, boolean isHpConsumption) {
		if (getActiveChar().isAdvancedHeadquarter()) {
			value /= 2.;
		}

		super.reduceHp(value, attacker, awake, isDOT, isHpConsumption);
	}

	@Override
	public SiegeFlagInstance getActiveChar() {
		return (SiegeFlagInstance) super.getActiveChar();
	}
}
