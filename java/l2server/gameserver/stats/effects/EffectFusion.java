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

import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.L2Effect;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.L2AbnormalType;
import l2server.gameserver.templates.skills.L2EffectTemplate;

/**
 * @author Kerberos
 */
public class EffectFusion extends L2Effect
{
	public int _effect;
	public int _maxEffect;

	public EffectFusion(Env env, L2EffectTemplate template)
	{
		super(env, template);
		_effect = getSkill().getLevel();
		_maxEffect = SkillTable.getInstance().getMaxLevel(getSkill().getId());
	}

	@Override
	public L2AbnormalType getAbnormalType()
	{
		return L2AbnormalType.BUFF;
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		return true;
	}

	public void increaseEffect()
	{
		if (_effect < _maxEffect)
		{
			_effect++;
			updateBuff();
		}
	}

	public void decreaseForce()
	{
		_effect--;
		if (_effect < 1)
		{
			exit();
		}
		else
		{
			updateBuff();
		}
	}

	private void updateBuff()
	{
		exit();
		SkillTable.getInstance().getInfo(getSkill().getId(), _effect).getEffects(getEffector(), getEffected());
	}
}
