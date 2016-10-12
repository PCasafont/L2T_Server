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

package l2server.gameserver.stats.skills;

import l2server.Config;
import l2server.gameserver.GeoData;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.model.*;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.*;
import l2server.gameserver.model.base.Experience;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.FlyToLocation;
import l2server.gameserver.network.serverpackets.FlyToLocation.FlyType;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.network.serverpackets.ValidateLocation;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.gameserver.templates.skills.L2AbnormalType;
import l2server.gameserver.templates.skills.L2SkillTargetType;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;

public class L2SkillSummon extends L2Skill
{
	private int _npcId;
	private float _expPenalty;
	private final boolean _isCubic;

	// cubic AI
	// Activation time for a cubic
	private final int _activationtime;
	// Activation chance for a cubic.
	private final int _activationchance;
	// Maximum casts made by the cubic until it goes idle.
	private final int _maxcount;

	// What is the total lifetime of summons (in millisecs)
	private final int _summonTotalLifeTime;
	// How much lifetime is lost per second of idleness (non-fighting)
	private final int _summonTimeLostIdle;
	// How much time is lost per second of activity (fighting)
	private final int _summonTimeLostActive;

	// item consume time in milliseconds
	private final int _itemConsumeTime;
	// item consume count over time
	private final int _itemConsumeOT;
	// item consume id over time
	private final int _itemConsumeIdOT;
	// how many times to consume an item
	private final int _itemConsumeSteps;

	// id of the debuff skill got during the summon's life
	private final int _summonPrice;
	// summon points that it consumes
	private final int _summonPoints;

	private final int _summonAmount;

	public L2SkillSummon(StatsSet set)
	{
		super(set);

		_npcId = set.getInteger("npcId", 0); // default for undescribed skills
		_expPenalty = set.getFloat("expPenalty", 0.0f);
		_isCubic = set.getBool("isCubic", false);

		_activationtime = set.getInteger("activationtime", 8);
		_activationchance = set.getInteger("activationchance", 30);
		_maxcount = set.getInteger("maxcount", -1);

		_summonTotalLifeTime = set.getInteger("summonTotalLifeTime", -1); // infinite default
		_summonTimeLostIdle = set.getInteger("summonTimeLostIdle", 0);
		_summonTimeLostActive = set.getInteger("summonTimeLostActive", 0);

		_itemConsumeOT = set.getInteger("itemConsumeCountOT", 0);
		_itemConsumeIdOT = set.getInteger("itemConsumeIdOT", 0);
		_itemConsumeTime = set.getInteger("itemConsumeTime", 0);
		_itemConsumeSteps = set.getInteger("itemConsumeSteps", 0);

		_summonPrice = set.getInteger("summonPrice", 0);
		_summonPoints = set.getInteger("summonPoints", 0);

		_summonAmount = set.getInteger("summonAmount", 1);
	}

	@Override
	public boolean checkCondition(L2Character activeChar, L2Object target, boolean itemOrWeapon)
	{
		if (activeChar instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) activeChar;

			if (isCubic())
			{
				if (getTargetType() != L2SkillTargetType.TARGET_SELF)
				{
					return true; //Player is always able to cast mass cubic skill
				}

				//Since GoD can resummon a cubic instantly
				/*if (player.isGM())
                {
					for (L2CubicInstance cubic : player.getCubics().values())
					{
						if (cubic == null)
						{
							continue;
						}

						if (cubic.getId() == _npcId)
						{
							cubic.stopAction();

							player.delCubic(cubic.getId());

							player.broadcastUserInfo();
						}
					}
				}*/

				int cubicMastery = player.getCubicMastery();
				int count = player.getCubics().size();
				if (count > cubicMastery)
				{
					activeChar.sendMessage("You already have " + count + " cubic(s).");
					return false;
				}
			}
			else if (!(this instanceof L2SkillTrap))// Normal summon
			{
				if (player.inObserverMode())
				{
					return false;
				}

				//Since GoD can resummon a summon if it's die instantly
                /*for (L2SummonInstance summon : player.getSummons())
				{
					if (summon == null)
					{
						continue;
					}

					if (summon.isDead())
					{
						summon.deleteMe(player);
					}
				}*/

				if (_summonPoints > 0 && player.getSpentSummonPoints() + _summonPoints > player.getMaxSummonPoints())
				{
					activeChar.sendMessage("You don't have enough summon points.");
					return false;
				}
				else if (player.getSpentSummonPoints() == 0 && !player.getSummons().isEmpty())
				{
					String currentSummonName = player.getSummon(0).getTemplate().getName();
					if (!currentSummonName.equalsIgnoreCase("Tree of life") &&
							!currentSummonName.equalsIgnoreCase("Unison of Lights"))
					{
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ALREADY_HAVE_A_PET));
						return false;
					}
					else
					{
						player.getSummon(0).unSummon(player);
					}
				}
			}
		}
		return super.checkCondition(activeChar, null, false);
	}

	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		if (caster.isAlikeDead() || !(caster instanceof L2PcInstance))
		{
			return;
		}

		L2PcInstance activeChar = (L2PcInstance) caster;

		if (_npcId == 0)
		{
			activeChar.sendMessage("Summon skill " + getId() + " not described yet");
			return;
		}

		if (_isCubic)
		{
			// Gnacik :
			// If skill is enchanted calculate cubic skill level based on enchant
			// 8 at 101 (+1 Power)
			// 12 at 130 (+30 Power)
			// Because 12 is max 5115-5117 skills
			// TODO: make better method of calculation, dunno how its calculated on offi
			int cubicSkillLevel = getLevel() + getEnchantLevel();

			if (targets.length > 1 || targets.length == 1 && targets[0] != activeChar) // Not self cubic skill
			{
				for (L2Object obj : targets)
				{
					if (!(obj instanceof L2PcInstance))
					{
						continue;
					}
					L2PcInstance player = (L2PcInstance) obj;
					int cubicMastery = player.getCubicMastery();
					if (cubicMastery == 0 && !player.getCubics().isEmpty())
					{
						// Player can have only 1 cubic - we shuld replace old cubic with new one
						for (L2CubicInstance c : player.getCubics().values())
						{
							c.stopAction();
							c = null;
						}
						player.getCubics().clear();
					}
					// TODO: Should remove first cubic summoned and replace with new cubic
					if (player.getCubics().containsKey(_npcId))
					{
						L2CubicInstance cubic = player.getCubic(_npcId);
						cubic.stopAction();
						cubic.cancelDisappear();
						player.delCubic(_npcId);
					}
					if (player.getCubics().size() > cubicMastery)
					{
						continue;
					}
					if (player == activeChar)
					{
						player.addCubic(_npcId, cubicSkillLevel, getPower(), _activationtime, _activationchance,
								_maxcount, _summonTotalLifeTime, false);
					}
					else
					// given by other player
					{
						player.addCubic(_npcId, cubicSkillLevel, getPower(), _activationtime, _activationchance,
								_maxcount, _summonTotalLifeTime, true);
					}
					if (hasEffects())
					{
						getEffects(player, player, new Env((byte) 0, L2ItemInstance.CHARGED_NONE));
					}
					player.broadcastUserInfo();
				}
				return;
			}
			else
			// Normal cubic skill
			{
				int cubicMastery = activeChar.getCubicMastery();
				if (activeChar.getCubics().containsKey(_npcId))
				{
					L2CubicInstance cubic = activeChar.getCubic(_npcId);
					cubic.stopAction();
					cubic.cancelDisappear();
					activeChar.delCubic(_npcId);
				}
				if (activeChar.getCubics().size() > cubicMastery)
				{
					if (Config.DEBUG)
					{
						Log.fine("player can't summon any more cubics. ignore summon skill");
					}
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CUBIC_SUMMONING_FAILED));
					return;
				}
				activeChar.addCubic(_npcId, cubicSkillLevel, getPower(), _activationtime, _activationchance, _maxcount,
						_summonTotalLifeTime, false);

				if (hasEffects())
				{
					getEffects(activeChar, activeChar, new Env((byte) 0, L2ItemInstance.CHARGED_NONE));
				}

				activeChar.broadcastUserInfo();
				return;
			}
		}

		if (_summonPoints > 0 && activeChar.getSpentSummonPoints() + _summonPoints > activeChar.getMaxSummonPoints())
		{
			return;
		}

		if (activeChar.getSpentSummonPoints() == 0 && !activeChar.getSummons().isEmpty())
		{
			if (Config.DEBUG)
			{
				Log.fine("player has a pet already. ignore summon skill");
			}
			return;
		}

		if (getId() == 10532) // Clone Attack
		{
			int x = 0, y = 0, z = 0;

			L2Object target = activeChar.getTarget();
			if (target == null)
			{
				target = activeChar;
			}
			int px = target.getX();
			int py = target.getY();
			double ph = Util.convertHeadingToDegree(((L2Character) target).getHeading());

			ph += 180;

			if (ph > 360)
			{
				ph -= 360;
			}

			ph = Math.PI * ph / 180;

			x = (int) (px + 25 * Math.cos(ph));
			y = (int) (py + 25 * Math.sin(ph));
			z = target.getZ();

			Location loc = new Location(x, y, z);

			if (Config.GEODATA > 0)
			{
				loc = GeoData.getInstance().moveCheck(activeChar.getX(), activeChar.getY(), activeChar.getZ(), x, y, z,
						activeChar.getInstanceId());
			}

			activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			activeChar
					.broadcastPacket(new FlyToLocation(activeChar, loc.getX(), loc.getY(), loc.getZ(), FlyType.DUMMY));
			activeChar.abortAttack();
			activeChar.abortCast();

			activeChar.setXYZ(loc.getX(), loc.getY(), loc.getZ());
			activeChar.broadcastPacket(new ValidateLocation(activeChar));

			for (L2Abnormal e : activeChar.getAllEffects())
			{
				if (e.getType() != L2AbnormalType.HIDE)
				{
					continue;
				}

				e.exit();
			}
		}

		for (int i = 0; i < _summonAmount; i++)
		{
			L2SummonInstance summon;
			L2NpcTemplate summonTemplate = NpcTable.getInstance().getTemplate(_npcId);
			if (summonTemplate == null)
			{
				Log.warning("Summon attempt for nonexisting NPC ID:" + _npcId + ", skill ID:" + getId());
				return; // npcID doesn't exist
			}
			if (summonTemplate.Type.equalsIgnoreCase("L2SiegeSummon"))
			{
				summon = new L2SiegeSummonInstance(IdFactory.getInstance().getNextId(), summonTemplate, activeChar,
						this);
			}
			else if (summonTemplate.Type.equalsIgnoreCase("L2MerchantSummon"))
			{
				summon = new L2MerchantSummonInstance(IdFactory.getInstance().getNextId(), summonTemplate, activeChar,
						this);
			}
			else if (summonTemplate.Name.equalsIgnoreCase("Incarnation"))
			{
				summon = new L2CloneInstance(IdFactory.getInstance().getNextId(), summonTemplate, activeChar, this);
			}
			else
			{
				summon = new L2SummonInstance(IdFactory.getInstance().getNextId(), summonTemplate, activeChar, this);
			}

			summon.setName(summonTemplate.Name);
			summon.setTitle(activeChar.getName());
			summon.setExpPenalty(_expPenalty);
			if (summon.getLevel() > Config.MAX_LEVEL)
			{
				summon.getStat().setExp(Experience.getAbsoluteExp(Config.MAX_LEVEL));
				Log.warning("Summon (" + summon.getName() + ") NpcID: " + summon.getNpcId() + " has a level above " +
						Config.MAX_LEVEL + ". Please rectify.");
			}
			else
			{
				summon.getStat().setExp(Experience.getAbsoluteExp(summon.getLevel()));
			}

			if (!(summon instanceof L2MerchantSummonInstance) && !(summon instanceof L2CloneInstance))
			{
				activeChar.addSummon(summon);
			}

			double angle = Rnd.get() * Math.PI * 2;
			summon.setXYZ(activeChar.getX() + (int) (Math.cos(angle) * 60),
					activeChar.getY() + (int) (Math.sin(angle) * 60), activeChar.getZ());
			while (!GeoData.getInstance().canSeeTarget(activeChar, summon))
			{
				angle = Rnd.get() * Math.PI * 2;
				summon.setXYZ(activeChar.getX() + (int) (Math.cos(angle) * 60),
						activeChar.getY() + (int) (Math.sin(angle) * 60), activeChar.getZ());
			}
			summon.setHeading(activeChar.getHeading());
			summon.setCurrentHp(summon.getMaxHp());
			summon.setCurrentMp(summon.getMaxMp());
			summon.setRunning();

			//L2World.getInstance().storeObject(summon);
			summon.spawnMe();

			if (summon instanceof L2CloneInstance)
			{
				summon.setTarget(summon.getOwner().getTarget());

				if (summon.getTarget() != null && summon.getTarget().isAutoAttackable(summon.getOwner()))
				{
					summon.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, summon.getTarget());
				}
			}

			if (activeChar.isPlayingEvent())
			{
				summon.setInsideZone(L2Character.ZONE_PVP, true);
				summon.setInsideZone(L2Character.ZONE_PVP, true);
			}

			//for (L2Abnormal eff : activeChar.getAllEffects())
			//	summon.addEffect(eff);
		}

		// self Effect
		if (hasSelfEffects())
		{
			final L2Abnormal effect = caster.getFirstEffect(getId());
			if (effect != null && effect.isSelfEffect())
			{
				//Replace old effect with new one.
				effect.exit();
			}
			getEffectsSelf(caster);
		}
	}

	public final boolean isCubic()
	{
		return _isCubic;
	}

	public final int getTotalLifeTime()
	{
		return _summonTotalLifeTime;
	}

	public final int getTimeLostIdle()
	{
		return _summonTimeLostIdle;
	}

	public final int getTimeLostActive()
	{
		return _summonTimeLostActive;
	}

	/**
	 * @return Returns the itemConsume count over time.
	 */
	public final int getItemConsumeOT()
	{
		return _itemConsumeOT;
	}

	/**
	 * @return Returns the itemConsumeId over time.
	 */
	public final int getItemConsumeIdOT()
	{
		return _itemConsumeIdOT;
	}

	public final int getItemConsumeSteps()
	{
		return _itemConsumeSteps;
	}

	/**
	 * @return Returns the itemConsume time in milliseconds.
	 */
	public final int getItemConsumeTime()
	{
		return _itemConsumeTime;
	}

	public final int getSummonPrice()
	{
		return _summonPrice;
	}

	public final int getSummonPoints()
	{
		return _summonPoints;
	}

	public final int getNpcId()
	{
		return _npcId;
	}
}
