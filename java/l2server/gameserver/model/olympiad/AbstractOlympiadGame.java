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

package l2server.gameserver.model.olympiad;

import l2server.Config;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.HeroSkillTable;
import l2server.gameserver.events.instanced.EventsManager;
import l2server.gameserver.instancemanager.AntiFeedManager;
import l2server.gameserver.instancemanager.CastleManager;
import l2server.gameserver.instancemanager.FortManager;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Party;
import l2server.gameserver.model.L2Party.messageType;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2MobSummonInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2PetInstance;
import l2server.gameserver.model.actor.instance.L2SummonInstance;
import l2server.gameserver.model.zone.type.L2OlympiadStadiumZone;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.log.Log;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author godson, GodKratos, Pere, DS
 */
public abstract class AbstractOlympiadGame
{
	protected static final Logger _logResults = Logger.getLogger("olympiad");

	protected static final String POINTS = "olympiad_points";
	protected static final String COMP_DONE = "competitions_done";
	protected static final String COMP_WON = "competitions_won";
	protected static final String COMP_LOST = "competitions_lost";
	protected static final String COMP_DRAWN = "competitions_drawn";

	protected static final String COMP_CLASSED = "competitions_classed";
	protected static final String COMP_NONCLASSED = "competitions_nonclassed";
	protected static final String COMP_TEAMS = "competitions_teams";

	protected long _startTime = 0;
	protected boolean _aborted = false;
	protected final int _gameId;

	protected AbstractOlympiadGame(int id)
	{
		_gameId = id;
	}

	public final boolean isAborted()
	{
		return _aborted;
	}

	public final int getGameId()
	{
		return _gameId;
	}

	protected boolean makeCompetitionStart()
	{
		_startTime = System.currentTimeMillis();
		return !_aborted;
	}

	protected final void addPointsToParticipant(OlympiadParticipant par, int points)
	{
		par.nobleInfo.setPoints(par.nobleInfo.getPoints() + points);
		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_GAINED_S2_OLYMPIAD_POINTS);
		sm.addString(par.name);
		sm.addNumber(points);
		broadcastPacketToParticipants(sm);
	}

	protected final void removePointsFromParticipant(OlympiadParticipant par, int points)
	{
		par.nobleInfo.setPoints(par.nobleInfo.getPoints() - points);
		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_LOST_S2_OLYMPIAD_POINTS);
		sm.addString(par.name);
		sm.addNumber(points);
		broadcastPacketToParticipants(sm);
	}

	/**
	 * Function return null if player passed all checks
	 * or SystemMessage with reason for broadcast to opponent(s).
	 *
	 * @return
	 */
	protected final SystemMessage checkDefaulted(OlympiadParticipant par)
	{
		L2PcInstance player = par.player;
		if (player == null || !player.isOnline() || player.getClient() == null || player.getClient().isDetached())
		{
			int playerPoints = par.nobleInfo.getPoints();
			int points = Math.min(playerPoints / 2, Config.ALT_OLY_MAX_POINTS);
			removePointsFromParticipant(par, points);
			return SystemMessage.getSystemMessage(
					SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_ENDS_THE_GAME);
		}

		// safety precautions
		if (player.inObserverMode() || EventsManager.getInstance().isPlayerParticipant(player.getObjectId()))
		{
			return SystemMessage.getSystemMessage(
					SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME);
		}

		SystemMessage sm;
		if (player.isDead())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_CANNOT_PARTICIPATE_OLYMPIAD_WHILE_DEAD);
			sm.addPcName(player);
			player.sendPacket(sm);
			return SystemMessage.getSystemMessage(
					SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME);
		}
		if (player.isSubClassActive())
		{
			sm = SystemMessage
					.getSystemMessage(SystemMessageId.C1_CANNOT_PARTICIPATE_IN_OLYMPIAD_WHILE_CHANGED_TO_SUB_CLASS);
			sm.addPcName(player);
			player.sendPacket(sm);
			return SystemMessage.getSystemMessage(
					SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME);
		}
		if (player.isCursedWeaponEquipped())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_CANNOT_JOIN_OLYMPIAD_POSSESSING_S2);
			sm.addPcName(player);
			sm.addItemName(player.getCursedWeaponEquippedId());
			player.sendPacket(sm);
			return SystemMessage.getSystemMessage(
					SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME);
		}
		if (!player.isInventoryUnder90(true))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_CAN_PROCEED_WHEN_WHEIGHT_BELOW_80_AND_QUANTITY_90);
			player.sendPacket(sm);
			return SystemMessage.getSystemMessage(
					SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME);
		}

		return null;
	}

	protected static boolean portPlayerToArena(OlympiadParticipant par, Location loc, int id)
	{
		final L2PcInstance player = par.player;
		if (player == null || !player.isOnline())
		{
			return false;
		}

		try
		{
			player.setLastCords(player.getX(), player.getY(), player.getZ());
			if (player.isSitting())
			{
				player.standUp();
			}
			player.setTarget(null);

			player.getSummons().stream().filter(summon -> summon instanceof L2MobSummonInstance)
					.forEach(summon -> summon.unSummon(player));

			player.setOlympiadGameId(id);
			player.setInstanceId(id + Olympiad.BASE_INSTANCE_ID);
			player.setIsInOlympiadMode(true);
			player.setIsOlympiadStart(false);
			player.setOlympiadSide(par.side);
			player.setOlyGivenDmg(0);
			player.olyBuff = 5;
			player.teleToLocation(loc, false);
			player.sendPacket(new ExOlympiadMode(2));
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, e.getMessage(), e);
			return false;
		}
		return true;
	}

	protected static void removals(L2PcInstance player, boolean removeParty)
	{
		try
		{
			if (player == null)
			{
				return;
			}

			// Remove Buffs
			player.stopAllEffectsExceptThoseThatLastThroughDeath();

			if (player.getFirstEffect(9982) != null)
			{
				player.getFirstEffect(9982).exit();
			}

			// Remove Clan Skills
			if (player.getClan() != null)
			{
				player.getClan().removeSkillEffects(player);
				if (player.getClan().getHasCastle() > 0)
				{
					CastleManager.getInstance().getCastleByOwner(player.getClan()).removeResidentialSkills(player);
				}
				if (player.getClan().getHasFort() > 0)
				{
					FortManager.getInstance().getFortByOwner(player.getClan()).removeResidentialSkills(player);
				}
			}
			// Abort casting if player casting
			player.abortAttack();
			player.abortCast();

			// Force the character to be visible
			player.getAppearance().setVisible();

			// Remove Hero Skills
			if (player.isHero())
			{
				for (L2Skill skill : HeroSkillTable.getHeroSkills())
				{
					player.removeSkill(skill, false);
				}
			}

			// Heal Player fully
			player.setCurrentCp(player.getMaxCp());
			player.setCurrentHp(player.getMaxHp());
			player.setCurrentMp(player.getMaxMp());

			// Remove Summon's Buffs
			final L2PetInstance pet = player.getPet();
			if (pet != null)
			{
				pet.stopAllEffectsExceptThoseThatLastThroughDeath();
				pet.abortAttack();
				pet.abortCast();
				pet.unSummon(player);
			}
			for (L2SummonInstance summon : player.getSummons())
			{
				summon.stopAllEffectsExceptThoseThatLastThroughDeath();
				summon.abortAttack();
				summon.abortCast();
			}

			// stop any cubic that has been given by other player.
			player.stopCubicsByOthers();

			// Remove player from his party
			if (removeParty)
			{
				final L2Party party = player.getParty();
				if (party != null)
				{
					party.removePartyMember(player, messageType.Expelled);
				}
			}
			// Remove Agathion
			if (player.getAgathionId() > 0)
			{
				player.setAgathionId(0);
				player.broadcastUserInfo();
			}

			player.checkItemRestriction();

			// Remove shot automation
			player.disableAutoShotsAll();

			// Discharge any active shots
			if (player.getActiveWeaponInstance() != null)
			{
				player.getActiveWeaponInstance().setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
				player.getActiveWeaponInstance().setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
			}

			// enable skills with cool time <= 15 minutes
			for (L2Skill skill : player.getAllSkills())
			{
				if (skill.getReuseDelay() <= 3600000)
				{
					player.enableSkill(skill);
				}
			}

			player.sendSkillList();
			player.sendPacket(new SkillCoolTime(player));
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, e.getMessage(), e);
		}
	}

	protected static void cleanEffects(L2PcInstance player)
	{
		try
		{
			// prevent players kill each other
			player.setIsOlympiadStart(false);
			player.setTarget(null);
			player.abortAttack();
			player.abortCast();
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

			if (player.isDead())
			{
				player.setIsDead(false);
			}

			player.stopAllEffectsExceptThoseThatLastThroughDeath();
			if (player.getFirstEffect(9982) != null)
			{
				player.getFirstEffect(9982).exit();
			}
			player.clearSouls();
			player.clearCharges();
			if (player.getAgathionId() > 0)
			{
				player.setAgathionId(0);
			}
			final L2PetInstance pet = player.getPet();
			if (pet != null && !pet.isDead())
			{
				pet.setTarget(null);
				pet.abortAttack();
				pet.abortCast();
				pet.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				pet.stopAllEffectsExceptThoseThatLastThroughDeath();
			}
			for (L2SummonInstance summon : player.getSummons())
			{
				if (summon.isDead())
				{
					continue;
				}

				summon.setTarget(null);
				summon.abortAttack();
				summon.abortCast();
				summon.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				summon.stopAllEffectsExceptThoseThatLastThroughDeath();
			}

			player.setCurrentCp(player.getMaxCp());
			player.setCurrentHp(player.getMaxHp());
			player.setCurrentMp(player.getMaxMp());
			player.getStatus().startHpMpRegeneration();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, e.getMessage(), e);
		}
	}

	protected static void playerStatusBack(L2PcInstance player)
	{
		try
		{
			if (player.isTransformed())
			{
				player.unTransform(true);
			}

			player.setIsInOlympiadMode(false);
			player.setIsOlympiadStart(false);
			player.setOlympiadSide(-1);
			player.setOlympiadGameId(-1);
			player.sendPacket(new ExOlympiadMode(0));

			// Add Clan Skills
			if (player.getClan() != null)
			{
				player.getClan().addSkillEffects(player);
				if (player.getClan().getHasCastle() > 0)
				{
					CastleManager.getInstance().getCastleByOwner(player.getClan()).giveResidentialSkills(player);
				}
				if (player.getClan().getHasFort() > 0)
				{
					FortManager.getInstance().getFortByOwner(player.getClan()).giveResidentialSkills(player);
				}
			}

			// Add Hero Skills
			if (player.isHero())
			{
				for (L2Skill skill : HeroSkillTable.getHeroSkills())
				{
					player.addSkill(skill, false);
				}
			}
			player.sendSkillList();

			// heal again after adding clan skills
			player.setCurrentCp(player.getMaxCp());
			player.setCurrentHp(player.getMaxHp());
			player.setCurrentMp(player.getMaxMp());
			player.getStatus().startHpMpRegeneration();

			if (Config.L2JMOD_DUALBOX_CHECK_MAX_OLYMPIAD_PARTICIPANTS_PER_IP > 0)
			{
				AntiFeedManager.getInstance().removePlayer(AntiFeedManager.OLYMPIAD_ID, player);
			}
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "portPlayersToArena()", e);
		}
	}

	protected static void portPlayerBack(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}

		if (player.getLastX() == 0 && player.getLastY() == 0)
		{
			return;
		}

		player.setInstanceId(0);
		player.teleToLocation(player.getLastX(), player.getLastY(), player.getLastZ());
		player.setLastCords(0, 0, 0);
	}

	public static void rewardParticipant(L2PcInstance player, int[][] reward)
	{
		if (player == null || !player.isOnline() || reward == null)
		{
			return;
		}

		try
		{
			SystemMessage sm;
			L2ItemInstance item;
			final InventoryUpdate iu = new InventoryUpdate();
			for (int[] it : reward)
			{
				if (it == null || it.length != 2)
				{
					continue;
				}

				item = player.getInventory().addItem("Olympiad", it[0], it[1], player, null);
				if (item == null)
				{
					continue;
				}

				iu.addModifiedItem(item);
				sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
				sm.addItemName(it[0]);
				sm.addNumber(it[1]);
				player.sendPacket(sm);
			}
			player.sendPacket(iu);
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, e.getMessage(), e);
		}
	}

	protected final void broadcastPacket(L2GameServerPacket packet, L2OlympiadStadiumZone stadium)
	{
		broadcastPacketToParticipants(packet);
		stadium.broadcastPacketToObservers(packet, _gameId);
	}

	public abstract CompetitionType getType();

	public abstract String[] getPlayerNames();

	public abstract boolean containsParticipant(int playerId);

	public abstract void sendOlympiadInfo(L2Character player);

	public abstract void broadcastOlympiadInfo(L2OlympiadStadiumZone stadium);

	protected abstract void broadcastPacketToParticipants(L2GameServerPacket packet);

	protected abstract boolean needBuffers();

	protected abstract boolean checkDefaulted();

	protected abstract void removals();

	protected abstract boolean portPlayersToArena(List<Location> spawns);

	protected abstract void cleanEffects();

	protected abstract void portPlayersBack();

	protected abstract void playersStatusBack();

	protected abstract void clearPlayers();

	protected abstract void handleDisconnect(L2PcInstance player);

	protected abstract void resetDamage();

	protected abstract void addDamage(L2PcInstance player, int damage);

	protected abstract boolean checkBattleStatus();

	protected abstract boolean haveWinner();

	protected abstract void validateWinner(L2OlympiadStadiumZone stadium);

	protected abstract int getDivider();

	protected abstract int[][] getReward();
}
