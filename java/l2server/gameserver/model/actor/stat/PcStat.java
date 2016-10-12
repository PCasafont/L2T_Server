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

package l2server.gameserver.model.actor.stat;

import l2server.Config;
import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.datatables.PlayerClassTable;
import l2server.gameserver.datatables.PlayerStatDataTable;
import l2server.gameserver.datatables.SkillTreeTable;
import l2server.gameserver.events.instanced.EventInstance.EventType;
import l2server.gameserver.instancemanager.MailManager;
import l2server.gameserver.instancemanager.MentorManager;
import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2PetInstance;
import l2server.gameserver.model.base.Experience;
import l2server.gameserver.model.base.PlayerClass;
import l2server.gameserver.model.entity.Message;
import l2server.gameserver.model.entity.RecoBonus;
import l2server.gameserver.model.quest.QuestState;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.stats.Stats;

public class PcStat extends PlayableStat
{
	//

	// =========================================================
	// Data Field

	private int _oldMaxHp; // stats watch
	private int _oldMaxMp; // stats watch
	private int _oldMaxCp; // stats watch
	private float _vitalityPoints = -1;

	public static final int MAX_VITALITY_POINTS = 140000;
	public static final int MIN_VITALITY_POINTS = 0;

	// =========================================================
	// Constructor
	public PcStat(L2PcInstance activeChar)
	{
		super(activeChar);
	}

	// =========================================================
	// Method - Public
	@Override
	public boolean addExp(long value)
	{
		L2PcInstance activeChar = getActiveChar();

		// Allowed to gain exp?
		if (!getActiveChar().getAccessLevel().canGainExp() && getActiveChar().isInParty() ||
				getActiveChar().isNoExp() && value > 0)
		{
			return false;
		}

		if (!super.addExp(value))
		{
			return false;
		}

		// EXP status update currently not used in retail
		activeChar.sendPacket(new UserInfo(activeChar));
		return true;
	}

	/**
	 * Add Experience and SP rewards to the L2PcInstance, remove its Karma (if necessary) and Launch increase level task.<BR><BR>
	 * <p>
	 * <B><U> Actions </U> :</B><BR><BR>
	 * <li>Remove Karma when the player kills L2MonsterInstance</li>
	 * <li>Send a Server->Client packet StatusUpdate to the L2PcInstance</li>
	 * <li>Send a Server->Client System Message to the L2PcInstance </li>
	 * <li>If the L2PcInstance increases it's level, send a Server->Client packet SocialAction (broadcast) </li>
	 * <li>If the L2PcInstance increases it's level, manage the increase level task (Max MP, Max MP, Recommandation, Expertise and beginner skills...) </li>
	 * <li>If the L2PcInstance increases it's level, send a Server->Client packet UserInfo to the L2PcInstance </li><BR><BR>
	 *
	 * @param addToExp The Experience value to add
	 * @param addToSp  The SP value to add
	 */
	//@Override
	public boolean addExpAndSp(long addToExp, long addToSp, double bonusMultiplier)
	{
		if (getActiveChar().getTemporaryLevel() != 0)
		{
			return false;
		}

		float ratioTakenByPlayer = 0;
		// Allowed to gain exp/sp?
		L2PcInstance activeChar = getActiveChar();
		if (!activeChar.getAccessLevel().canGainExp())
		{
			return false;
		}

		// if this player has a pet that takes from the owner's Exp, give the pet Exp now

		if (activeChar.getPet() != null)
		{
			L2PetInstance pet = activeChar.getPet();
			ratioTakenByPlayer = pet.getPetLevelData().getOwnerExpTaken() / 100f;

			// only give exp/sp to the pet by taking from the owner if the pet has a non-zero, positive ratio
			// allow possible customizations that would have the pet earning more than 100% of the owner's exp/sp
			if (ratioTakenByPlayer > 1)
			{
				ratioTakenByPlayer = 1;
			}
			if (!pet.isDead())
			{
				pet.addExpAndSp((long) (addToExp * (1 - ratioTakenByPlayer)),
						(int) (addToSp * (1 - ratioTakenByPlayer)));
			}
			// now adjust the max ratio to avoid the owner earning negative exp/sp
			addToExp = (long) (addToExp * ratioTakenByPlayer);
			addToSp = (long) (addToSp * ratioTakenByPlayer);
		}

		if (!super.addExpAndSp(addToExp, addToSp))
		{
			return false;
		}

		// Send a Server->Client System Message to the L2PcInstance
		if (bonusMultiplier > 1)
		{
			SystemMessage sm =
					SystemMessage.getSystemMessage(SystemMessageId.YOU_EARNED_S1_EXP_BONUS_S2_AND_S3_SP_BONUS_S4);
			int exp = (int) Math.round(addToExp / bonusMultiplier);
			int sp = (int) Math.round(addToSp / bonusMultiplier);
			sm.addNumber(exp);
			sm.addItemNumber(addToExp - exp);
			sm.addNumber(sp);
			sm.addItemNumber(addToSp - sp);
			activeChar.sendPacket(sm);
		}
		else if (addToExp == 0 && addToSp != 0)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_SP);
			sm.addItemNumber(addToSp);
			activeChar.sendPacket(sm);
		}
		else if (addToSp == 0 && addToExp != 0)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_EXPERIENCE);
			sm.addItemNumber(addToExp);
			activeChar.sendPacket(sm);
		}
		else
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_EARNED_S1_EXP_AND_S2_SP);
			sm.addItemNumber(addToExp);
			sm.addItemNumber(addToSp);
			activeChar.sendPacket(sm);
		}
		return true;
	}

	public boolean addExpAndSp(long addToExp, long addToSp, boolean useBonuses)
	{
		if (getActiveChar().getTemporaryLevel() != 0)
		{
			return false;
		}

		double bonusMultiplier = 1.0;
		if (useBonuses)
		{
			if (Config.ENABLE_VITALITY && _vitalityPoints > 0)
			{
				addToExp *= Config.VITALITY_MULTIPLIER;
				addToSp *= Config.VITALITY_MULTIPLIER;
			}

			// Apply recommendation bonus
			bonusMultiplier = RecoBonus.getRecoMultiplier(getActiveChar());
			addToExp *= bonusMultiplier;
			addToSp *= bonusMultiplier;
		}

		return addExpAndSp(addToExp, addToSp, bonusMultiplier);
	}

	@Override
	public boolean removeExpAndSp(long addToExp, long addToSp)
	{
		return removeExpAndSp(addToExp, addToSp, true);
	}

	public boolean removeExpAndSp(long addToExp, long addToSp, boolean sendMessage)
	{
		if (getActiveChar().getTemporaryLevel() != 0)
		{
			return false;
		}

		int level = getLevel();
		if (!super.removeExpAndSp(addToExp, addToSp))
		{
			return false;
		}

		if (sendMessage)
		{
			// Send a Server->Client System Message to the L2PcInstance
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EXP_DECREASED_BY_S1);
			sm.addItemNumber(addToExp);
			getActiveChar().sendPacket(sm);
			sm = SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1);
			sm.addItemNumber(addToSp);
			getActiveChar().sendPacket(sm);
			if (getLevel() < level)
			{
				getActiveChar().broadcastStatusUpdate();
			}
		}
		return true;
	}

	@Override
	public final boolean addLevel(byte value)
	{
		if (getActiveChar().getTemporaryLevel() != 0)
		{
			return false;
		}

		if (getLevel() + value > Config.MAX_LEVEL)
		{
			return false;
		}

		boolean levelIncreased = super.addLevel(value);

		if (levelIncreased)
		{
			if (!Config.DISABLE_TUTORIAL)
			{
				QuestState qs = getActiveChar().getQuestState("Q255_Tutorial");
				if (qs != null)
				{
					qs.getQuest().notifyEvent("CE40", null, getActiveChar());
				}
			}
			getActiveChar().setCurrentCp(getMaxCp());
			getActiveChar().broadcastPacket(new SocialAction(getActiveChar().getObjectId(), SocialAction.LEVEL_UP));
			getActiveChar().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_INCREASED_YOUR_LEVEL));
			MentorManager mm = MentorManager.getInstance();
			if (getActiveChar().getBaseClass() == getActiveChar().getActiveClass() &&
					mm.getItemsCount(getLevel()) != 0 && getActiveChar().isMentee())
			{
				Message msg = new Message(getActiveChar().getMentorId(), mm.getTitle(),
						mm.getMessage(getActiveChar().getName(), String.valueOf(getLevel())),
						Message.SendBySystem.MENTORING);
				msg.createAttachments()
						.addItem("Send Coins to Mentor", 33804, mm.getItemsCount(getLevel()), null, null);
				MailManager.getInstance().sendMessage(msg);
			}

			if (getActiveChar().getBaseClass() == getActiveChar().getActiveClass() && getActiveChar().isMentee() &&
					getLevel() >= 86)
			{
				int mentorId = getActiveChar().getMentorId();
				getActiveChar().removeMentor();
				for (L2Abnormal e : getActiveChar().getAllEffects())
				{
					if (e.getSkill().getId() >= 9227 && e.getSkill().getId() <= 9233)
					{
						e.exit();
					}
				}
				getActiveChar().removeSkill(9379);
				L2PcInstance mentor = L2World.getInstance().getPlayer(mentorId);
				if (mentor != null && mentor.isOnline())
				{
					mentor.giveMentorBuff();
				}
			}

			if (getActiveChar().getFriendList().size() > 0)
			{
				for (int i : getActiveChar().getFriendList())
				{
					L2PcInstance friend;
					if (L2World.getInstance().getPlayer(i) != null)
					{
						friend = L2World.getInstance().getPlayer(i);
						friend.sendPacket(new FriendPacket(true, getActiveChar().getObjectId(), friend));
						friend.sendPacket(new FriendList(friend));
					}
				}
			}

			getActiveChar().sendPacket(new ExMentorList(getActiveChar()));
			if (getActiveChar().isMentee())
			{
				if (L2World.getInstance().getPlayer(getActiveChar().getMentorId()) != null)
				{
					L2PcInstance player = L2World.getInstance().getPlayer(getActiveChar().getMentorId());
					player.sendPacket(new ExMentorList(player));
				}
			}
			else if (getActiveChar().isMentor())
			{
				for (int objId : getActiveChar().getMenteeList())
				{
					if (L2World.getInstance().getPlayer(objId) != null)
					{
						L2PcInstance player = L2World.getInstance().getPlayer(objId);
						player.sendPacket(new ExMentorList(player));
					}
				}
			}
		}

		getActiveChar().rewardSkills(); // Give Expertise skill of this level
		if (SkillTreeTable.getInstance().hasNewSkillsToLearn(getActiveChar(), getActiveChar().getCurrentClass()))
		{
			getActiveChar().sendPacket(new ExNewSkillToLearnByLevelUp());
		}

		if (getActiveChar().getClan() != null)
		{
			getActiveChar().getClan().updateClanMember(getActiveChar());
			getActiveChar().getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(getActiveChar()));
		}
		if (getActiveChar().isInParty())
		{
			getActiveChar().getParty().recalculatePartyLevel(); // Recalculate the party level
		}

		if (getActiveChar().isTransformed() || getActiveChar().isInStance())
		{
			getActiveChar().getTransformation().onLevelUp();
		}

		StatusUpdate su = new StatusUpdate(getActiveChar());
		su.addAttribute(StatusUpdate.LEVEL, getLevel());
		su.addAttribute(StatusUpdate.MAX_CP, getMaxCp());
		su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
		su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
		getActiveChar().sendPacket(su);

		// Update the overloaded status of the L2PcInstance
		getActiveChar().refreshOverloaded();
		// Update the expertise status of the L2PcInstance
		getActiveChar().refreshExpertisePenalty();
		// Send a Server->Client packet UserInfo to the L2PcInstance
		getActiveChar().sendPacket(new UserInfo(getActiveChar()));
		getActiveChar().sendPacket(new ExVoteSystemInfo(getActiveChar()));
		if (getLevel() >= 85 && getActiveChar().getClassId() < 139)
		{
			PlayerClass cl = PlayerClassTable.getInstance().getClassById(getActiveChar().getClassId());
			if (cl.getAwakeningClassId() != -1)
			{
				getActiveChar().sendPacket(new ExCallToChangeClass(cl.getId(), false));
			}
		}

		return levelIncreased;
	}

	@Override
	public boolean addSp(long value)
	{
		if (!super.addSp(value))
		{
			return false;
		}

		StatusUpdate su = new StatusUpdate(getActiveChar());
		su.addAttribute(StatusUpdate.SP, (int) getSp());
		getActiveChar().sendPacket(su);

		return true;
	}

	@Override
	public final long getExpForLevel(int level)
	{
		return Experience.getAbsoluteExp(level);
	}

	@Override
	public final L2PcInstance getActiveChar()
	{
		return (L2PcInstance) super.getActiveChar();
	}

	@Override
	public final long getExp()
	{
		if (getActiveChar().isSubClassActive())
		{
			return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).getExp();
		}

		return super.getExp();
	}

	public final long getBaseClassExp()
	{
		return super.getExp();
	}

	@Override
	public final void setExp(long value)
	{
		if (getActiveChar().isSubClassActive())
		{
			getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).setExp(value);
		}
		else
		{
			super.setExp(value);
		}
	}

	@Override
	public final byte getLevel()
	{
		if (getActiveChar().isSubClassActive())
		{
			return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).getLevel();
		}

		return super.getLevel();
	}

	public final byte getBaseClassLevel()
	{
		return super.getLevel();
	}

	@Override
	public final void setLevel(byte value)
	{
		if (getActiveChar().getTemporaryLevel() != 0)
		{
			return;
		}

		if (value > Config.MAX_LEVEL)
		{
			value = Config.MAX_LEVEL;
		}

		if (getActiveChar().isSubClassActive())
		{
			getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).setLevel(value);
		}
		else
		{
			super.setLevel(value);
		}
	}

	@Override
	public final int getMaxCp()
	{
		if (getActiveChar() == null || getActiveChar().getCurrentClass() == null)
		{
			return 1;
		}

		if (getActiveChar().isPlayingEvent() && getActiveChar().getEvent().isType(EventType.StalkedSalkers))
		{
			return 0;
		}

		// Get the Max CP (base+modifier) of the L2PcInstance
		int val = (int) calcStat(Stats.MAX_CP,
				PlayerStatDataTable.getInstance().getMaxCp(getActiveChar().getClassId(), getActiveChar().getLevel()),
				null, null);

		if (val != _oldMaxCp)
		{
			_oldMaxCp = val;

			// Launch a regen task if the new Max CP is higher than the old one
			if (getActiveChar().getStatus().getCurrentCp() != val)
			{
				getActiveChar().getStatus()
						.setCurrentCp(getActiveChar().getStatus().getCurrentCp()); // trigger start of regeneration
			}
		}
		return val;
	}

	@Override
	public final int getMaxHp()
	{
		if (getActiveChar() == null || getActiveChar().getCurrentClass() == null ||
				getActiveChar().isPlayingEvent() && getActiveChar().getEvent().isType(EventType.StalkedSalkers))
		{
			return 1;
		}

		// Get the Max HP (base+modifier) of the L2PcInstance
		int maxHp = (int) calcStat(Stats.MAX_HP,
				PlayerStatDataTable.getInstance().getMaxHp(getActiveChar().getClassId(), getActiveChar().getLevel()),
				null, null);
		int val = (int) calcStat(Stats.LIMIT_HP, maxHp, null, null);

		if (val != _oldMaxHp)
		{
			_oldMaxHp = val;

			// Launch a regen task if the new Max HP is higher than the old one
			if (getActiveChar().getStatus().getCurrentHp() != val)
			{
				getActiveChar().getStatus()
						.setCurrentHp(getActiveChar().getStatus().getCurrentHp()); // trigger start of regeneration
			}
		}

		return val;
	}

	@Override
	public int getMaxVisibleHp()
	{
		getMaxHp();
		if (getActiveChar() == null || getActiveChar().getCurrentClass() == null)
		{
			return 1;
		}

		return (int) calcStat(Stats.MAX_HP,
				PlayerStatDataTable.getInstance().getMaxHp(getActiveChar().getClassId(), getActiveChar().getLevel()),
				null, null);
	}

	@Override
	public final int getMaxMp()
	{
		if (getActiveChar() == null || getActiveChar().getCurrentClass() == null)
		{
			return 1;
		}

		// Get the Max MP (base+modifier) of the L2PcInstance
		int val = (int) calcStat(Stats.MAX_MP,
				PlayerStatDataTable.getInstance().getMaxMp(getActiveChar().getClassId(), getActiveChar().getLevel()),
				null, null);

		if (val != _oldMaxMp)
		{
			_oldMaxMp = val;

			// Launch a regen task if the new Max MP is higher than the old one
			if (getActiveChar().getStatus().getCurrentMp() != val)
			{
				getActiveChar().getStatus()
						.setCurrentMp(getActiveChar().getStatus().getCurrentMp()); // trigger start of regeneration
			}
		}

		return val;
	}

	@Override
	public final long getSp()
	{
		if (getActiveChar().isSubClassActive())
		{
			return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).getSp();
		}

		return super.getSp();
	}

	public final long getBaseClassSp()
	{
		return super.getSp();
	}

	@Override
	public final void setSp(long value)
	{
		if (getActiveChar().isSubClassActive())
		{
			getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).setSp(value);
		}
		else
		{
			super.setSp(value);
		}
	}

	@Override
	public int getRunSpeed()
	{
		if (getActiveChar() == null)
		{
			return 1;
		}

		int val;

		L2PcInstance player = getActiveChar();
		if (player.isMounted())
		{
			float baseRunSpd = NpcTable.getInstance().getTemplate(getActiveChar().getMountNpcId()).baseRunSpd;
			val = (int) Math.round(calcStat(Stats.RUN_SPEED, baseRunSpd, null, null));
		}
		else
		{
			val = super.getRunSpeed();
		}

		val += Config.RUN_SPD_BOOST;

		// Apply max run speed cap.
		if (!Config.isServer(Config.TENKAI) && val > Config.MAX_RUN_SPEED && !getActiveChar().isGM())
		{
			return Config.MAX_RUN_SPEED;
		}

		if (val < 2)
		{
			val = 2;
		}

		return val;
	}

	@Override
	public int getPAtkSpd()
	{
		int val = super.getPAtkSpd();

		if (!Config.isServer(Config.TENKAI) && val > Config.MAX_PATK_SPEED && !getActiveChar().isGM())
		{
			return Config.MAX_PATK_SPEED;
		}

		return val;
	}

	@Override
	public int getEvasionRate(L2Character target)
	{

		//if (val > Config.MAX_EVASION && !getActiveChar().isGM())
		//	return Config.MAX_EVASION;

		return super.getEvasionRate(target);
	}

	@Override
	public int getMEvasionRate(L2Character target)
	{

		//if (val > Config.MAX_EVASION && !getActiveChar().isGM())
		//	return Config.MAX_EVASION;

		return super.getMEvasionRate(target);
	}

	@Override
	public int getMAtkSpd()
	{
		int val = super.getMAtkSpd();

		if (!Config.isServer(Config.TENKAI) && val > Config.MAX_MATK_SPEED && !getActiveChar().isGM())
		{
			return Config.MAX_MATK_SPEED;
		}

		return val;
	}

	@Override
	public float getMovementSpeedMultiplier()
	{
		if (getActiveChar() == null)
		{
			return 1;
		}

		if (getActiveChar().isMounted())
		{
			return getRunSpeed() * 1f / NpcTable.getInstance().getTemplate(getActiveChar().getMountNpcId()).baseRunSpd;
		}

		return super.getMovementSpeedMultiplier();
	}

	@Override
	public int getWalkSpeed()
	{
		if (getActiveChar() == null)
		{
			return 1;
		}

		return getRunSpeed() * 70 / 100;
	}

	/*
	 * Return current vitality points in integer format
	 */
	public int getVitalityPoints()
	{
		return (int) _vitalityPoints;
	}

	/*
	 * Set current vitality points to this value
	 *
	 * if quiet = true - does not send system messages
	 */
	public void setVitalityPoints(int points, boolean quiet, boolean allowGM)
	{
		points = Math.min(Math.max(points, MIN_VITALITY_POINTS), MAX_VITALITY_POINTS);
		if (points == _vitalityPoints)
		{
			return;
		}

		if (getActiveChar().isGM() && !allowGM)
		{
			return;
		}

		_vitalityPoints = points;
	}

	public synchronized void updateVitalityPoints(float points, boolean useRates, boolean quiet)
	{
		if (points == 0 || !Config.ENABLE_VITALITY)
		{
			return;
		}

		if (useRates)
		{
			byte level = getLevel();
			if (level < 10)
			{
				return;
			}

			if (points < 0) // vitality consumed
			{
				int stat = (int) calcStat(Stats.VITALITY_CONSUME_RATE, 1, getActiveChar(), null);
				if (stat == 0) // is vitality consumption stopped ?
				{
					return;
				}
				if (stat < 0) // is vitality gained ?
				{
					points = -points;
				}
			}

			if (points < 0)
			{
				// vitality decreased
				points *= Config.RATE_VITALITY_LOST;
			}
		}

		if (points < 0)
		{
			points = Math.max(_vitalityPoints + points, MIN_VITALITY_POINTS);
		}

		if (points == _vitalityPoints)
		{
			return;
		}

		_vitalityPoints = points;
	}

	@Override
	public int getDefenseElementValue(byte defenseAttribute)
	{
		return super.getDefenseElementValue(defenseAttribute) + getActiveChar().getHennaStatElem(defenseAttribute);
	}
}
