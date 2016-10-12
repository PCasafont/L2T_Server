/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.stats.effects;

import l2server.Config;
import l2server.gameserver.GeoData;
import l2server.gameserver.ai.CtrlEvent;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.L2Effect;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.actor.instance.*;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.VisualEffect;
import l2server.gameserver.templates.skills.L2AbnormalType;
import l2server.gameserver.templates.skills.L2EffectTemplate;
import l2server.gameserver.templates.skills.L2EffectType;

/**
 * @author littlecrow
 *         <p>
 *         Implementation of the Fear Effect
 */
public class EffectFear extends L2Effect
{
	public static final int FEAR_RANGE = 500;

	private int _dX = -1;
	private int _dY = -1;

	public EffectFear(Env env, L2EffectTemplate template)
	{
		super(env, template);
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#getType()
	 */
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.FEAR;
	}

	@Override
	public L2AbnormalType getAbnormalType()
	{
		return L2AbnormalType.FEAR;
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onStart()
	 */
	@Override
	public boolean onStart()
	{
		// Fear skills cannot be used in event
		if (getEffector() instanceof L2PcInstance && ((L2PcInstance) getEffector()).isPlayingEvent())
		{
			return false;
		}

		/* Tenkai custom - Kilian: I don't see the sense of PvE-only fears and never heard of it either, so commented out
		// Fear skills cannot be used l2pcinstance to l2pcinstance. Heroic
		// Dread, Curse: Fear, Fear, Horror, Sword Symphony, Word of Fear, Hell Scream and
		// Mass Curse Fear are the exceptions.
		if (getEffected() instanceof L2PcInstance
				&& getEffector() instanceof L2PcInstance)
		{
			switch (getSkill().getId())
			{
				case 1376:
				case 1169:
				case 65:
				case 1092:
				case 98:
				case 1272:
				case 1381:
				case 763:
					break;
				default:
					return false;
			}
		}
		 */

		if (getEffected() instanceof L2NpcInstance || getEffected() instanceof L2DefenderInstance ||
				getEffected() instanceof L2FortCommanderInstance || getEffected() instanceof L2SiegeFlagInstance ||
				getEffected() instanceof L2SiegeSummonInstance)
		{
			return false;
		}

		if (!getEffected().isAfraid())
		{
			if (getEffected().isCastingNow() && getEffected().canAbortCast())
			{
				getEffected().abortCast();
			}

			if (getEffected().getX() > getEffector().getX())
			{
				_dX = 1;
			}
			if (getEffected().getY() > getEffector().getY())
			{
				_dY = 1;
			}

			getEffected().startFear();
			getEffected().startVisualEffect(VisualEffect.SKULL_FEAR);
			onActionTime();
			return true;
		}
		return false;
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onExit()
	 */
	@Override
	public void onExit()
	{
		getEffected().stopVisualEffect(VisualEffect.SKULL_FEAR);
		getEffected().stopFear(false);
		getEffected().getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, getEffector());
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		int posX = getEffected().getX();
		int posY = getEffected().getY();
		int posZ = getEffected().getZ();

		if (getEffected().getX() > getEffector().getX())
		{
			_dX = 1;
		}
		if (getEffected().getY() > getEffector().getY())
		{
			_dY = 1;
		}

		posX += _dX * FEAR_RANGE;
		posY += _dY * FEAR_RANGE;

		if (Config.GEODATA > 0)
		{
			Location destiny = GeoData.getInstance()
					.moveCheck(getEffected().getX(), getEffected().getY(), getEffected().getZ(), posX, posY, posZ,
							getEffected().getInstanceId());
			posX = destiny.getX();
			posY = destiny.getY();
		}

		if (!(getEffected() instanceof L2PetInstance))
		{
			getEffected().setRunning();
		}

		getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(posX, posY, posZ, 0));
		return true;
	}
}
