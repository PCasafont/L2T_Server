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
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.L2Object.InstanceType;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.zone.L2ZoneType;
import l2server.gameserver.network.serverpackets.EtcStatusUpdate;
import l2server.log.Log;
import l2server.util.Rnd;
import l2server.util.StringUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;

/**
 * another type of damage zone with skills
 *
 * @author kerberos
 */
public class L2EffectZone extends L2ZoneType
{
	private int _chance;
	private int _initialDelay;
	private int _reuse;
	private boolean _enabled;
	private boolean _bypassConditions;
	private boolean _isShowDangerIcon;
	private Future<?> _task;
	private HashMap<Integer, Integer> _skills;

	public L2EffectZone(int id)
	{
		super(id);
		_chance = 100;
		_initialDelay = 0;
		_reuse = 30000;
		_enabled = true;
		setTargetType(InstanceType.L2Playable); // default only playabale
		_bypassConditions = false;
		_isShowDangerIcon = true;
	}

	@Override
	public void setParameter(String name, String value)
	{
		switch (name)
		{
			case "chance":
				_chance = Integer.parseInt(value);
				break;
			case "initialDelay":
				_initialDelay = Integer.parseInt(value);
				break;
			case "default_enabled":
				_enabled = Boolean.parseBoolean(value);
				break;
			case "reuse":
				_reuse = Integer.parseInt(value);
				break;
			case "bypassSkillConditions":
				_bypassConditions = Boolean.parseBoolean(value);
				break;
			case "maxDynamicSkillCount":
				_skills = new HashMap<>(Integer.parseInt(value));
				break;
			case "skillIdLvl":
				String[] propertySplit = value.split(";");
				_skills = new HashMap<>(propertySplit.length);
				for (String skill : propertySplit)
				{
					String[] skillSplit = skill.split("-");
					if (skillSplit.length != 2)
					{
						Log.warning(StringUtil
								.concat(getClass().getSimpleName() + ": invalid config property -> skillsIdLvl \"",
										skill, "\""));
					}
					else
					{
						try
						{
							_skills.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
						}
						catch (NumberFormatException nfe)
						{
							if (!skill.isEmpty())
							{
								Log.warning(StringUtil.concat(getClass().getSimpleName() +
												": invalid config property -> skillsIdLvl \"", skillSplit[0], "\"",
										skillSplit[1]));
							}
						}
					}
				}
				break;
			case "showDangerIcon":
				_isShowDangerIcon = Boolean.parseBoolean(value);
				break;
			default:
				super.setParameter(name, value);
				break;
		}
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if (_skills != null)
		{
			if (_task == null)
			{
				synchronized (this)
				{
					if (_task == null)
					{
						_task = ThreadPoolManager.getInstance()
								.scheduleGeneralAtFixedRate(new ApplySkill(), _initialDelay, _reuse);
					}
				}
			}
		}
		if (character instanceof L2PcInstance)
		{
			character.setInsideZone(L2Character.ZONE_ALTERED, true);
			if (_isShowDangerIcon)
			{
				character.setInsideZone(L2Character.ZONE_DANGERAREA, true);
				character.sendPacket(new EtcStatusUpdate((L2PcInstance) character));
			}
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			character.setInsideZone(L2Character.ZONE_ALTERED, false);
			if (_isShowDangerIcon)
			{
				character.setInsideZone(L2Character.ZONE_DANGERAREA, false);
				if (!character.isInsideZone(L2Character.ZONE_DANGERAREA))
				{
					character.sendPacket(new EtcStatusUpdate((L2PcInstance) character));
				}
			}
		}
		if (_characterList.isEmpty() && _task != null)
		{
			_task.cancel(true);
			_task = null;
		}
	}

	private L2Skill getSkill(int skillId, int skillLvl)
	{
		return SkillTable.getInstance().getInfo(skillId, skillLvl);
	}

	public boolean isEnabled()
	{
		return _enabled;
	}

	public int getChance()
	{
		return _chance;
	}

	public void addSkill(int skillId, int skillLvL)
	{
		if (skillLvL < 1) // remove skill
		{
			removeSkill(skillId);
			return;
		}
		if (_skills == null)
		{
			synchronized (this)
			{
				if (_skills == null)
				{
					_skills = new HashMap<>(3);
				}
			}
		}
		_skills.put(skillId, skillLvL);
		//Logozo.info("Zone: "+this+" adding skill: "+skillId+" lvl: "+skillLvL);
	}

	public void removeSkill(int skillId)
	{
		if (_skills != null)
		{
			_skills.remove(skillId);
		}
	}

	public void clearSkills()
	{
		if (_skills != null)
		{
			_skills.clear();
		}
	}

	public void setZoneEnabled(boolean val)
	{
		_enabled = val;
	}

	public int getSkillLevel(int skillId)
	{
		if (_skills == null || !_skills.containsKey(skillId))
		{
			return 0;
		}
		else
		{
			return _skills.get(skillId);
		}
	}

	protected Collection<L2Character> getCharacterList()
	{
		return _characterList.values();
	}

	class ApplySkill implements Runnable
	{
		ApplySkill()
		{
			if (_skills == null)
			{
				throw new IllegalStateException("No skills defined.");
			}
		}

		@Override
		public void run()
		{
			if (isEnabled())
			{
				for (L2Character temp : getCharacterList())
				{
					if (temp != null && !temp.isDead() &&
							(!temp.isGM() || !temp.isInvul())) // Tenkai custom - ignore invul GMs
					{
						if (Rnd.get(100) < getChance())
						{
							synchronized (_skills)
							{
								Map<Integer, Integer> toIterate = new HashMap<>(_skills);
								for (Entry<Integer, Integer> e : toIterate.entrySet())
								{
									L2Skill skill = getSkill(e.getKey(), e.getValue());
									if (_bypassConditions || skill.checkCondition(temp, temp, false))
									{
										if (temp.getFirstEffect(e.getKey()) == null)
										{
											skill.getEffects(temp, temp);
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void onDieInside(L2Character character, L2Character killer)
	{
	}

	@Override
	public void onReviveInside(L2Character character)
	{
	}
}
