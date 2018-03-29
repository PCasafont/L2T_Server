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

package l2server.gameserver.model.zone.type;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.L2WorldRegion;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.zone.L2ZoneType;

import java.util.concurrent.Future;

/**
 * A dynamic zone?
 * Maybe use this for interlude skills like protection field :>
 *
 * @author durgus
 */
public class L2DynamicZone extends L2ZoneType
{
	private L2WorldRegion region;
	private L2Character owner;
	private Future<?> task;
	private L2Skill skill;

	protected void setTask(Future<?> task)
	{
		this.task = task;
	}

	public L2DynamicZone(L2WorldRegion region, L2Character owner, L2Skill skill)
	{
		super(-1);
		this.region = region;
		this.owner = owner;
		this.skill = skill;

		Runnable r = this::remove;
		setTask(ThreadPoolManager.getInstance().scheduleGeneral(r, skill.getBuffDuration()));
	}

	@Override
	protected void onEnter(L2Character character)
	{
		try
		{
			if (character instanceof L2PcInstance)
			{
				character.sendMessage("You have entered a temporary zone!");
			}
			skill.getEffects(owner, character);
		}
		catch (NullPointerException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			character.sendMessage("You have left a temporary zone!");
		}
		if (character == owner)
		{
			remove();
			return;
		}
		character.stopSkillEffects(skill.getId());
	}

	protected void remove()
	{
		if (task == null)
		{
			return;
		}
		task.cancel(false);
		task = null;

		region.removeZone(this);
		for (L2Character member : characterList.values())
		{
			try
			{
				member.stopSkillEffects(skill.getId());
			}
			catch (NullPointerException e)
			{
				e.printStackTrace();
			}
		}
		owner.stopSkillEffects(skill.getId());
	}

	@Override
	public void onDieInside(L2Character character, L2Character killer)
	{
		if (character == owner)
		{
			remove();
		}
		else
		{
			character.stopSkillEffects(skill.getId());
		}
	}

	@Override
	public void onReviveInside(L2Character character)
	{
		skill.getEffects(owner, character);
	}
}
