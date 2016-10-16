package l2server.gameserver.events.instanced.types;

import l2server.gameserver.Announcements;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.events.instanced.EventConfig;
import l2server.gameserver.events.instanced.EventInstance;
import l2server.gameserver.events.instanced.EventTeleporter;
import l2server.gameserver.instancemanager.PlayerAssistsManager;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2PetInstance;
import l2server.gameserver.model.actor.instance.L2SummonInstance;
import l2server.gameserver.stats.VisualEffect;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pere
 */
public class CursedBattle extends EventInstance
{
	private L2PcInstance cursedPlayer;

	List<L2PcInstance> winners = new ArrayList<>();

	public CursedBattle(int id, EventConfig config)
	{
		super(id, config);
	}

	@Override
	public boolean startFight()
	{
		if (!super.startFight())
		{
			return false;
		}

		this.cursedPlayer = selectRandomParticipant();

		int lvl = (int) Math.round(Math.log10(getParticipatedPlayersCount()) / Math.log10(2));
		if (lvl < 1)
		{
			lvl = 1;
		}
		else if (lvl > 11)
		{
			lvl = 11;
		}
		L2Skill curse = SkillTable.getInstance().getInfo(9940, lvl);
		this.cursedPlayer.addSkill(curse, false);
		this.cursedPlayer.setCurrentHp(this.cursedPlayer.getMaxHp());
		this.cursedPlayer.startVisualEffect(VisualEffect.S_AIR_STUN);
		this.cursedPlayer.broadcastUserInfo();
		this.winners.add(0, this.cursedPlayer);
		return true;
	}

	@Override
	public void calculateRewards()
	{
		if (this.cursedPlayer != null)
		{
			rewardPlayers(this.winners);
			Announcements.getInstance().announceToAll("The event has ended. The player " + this.cursedPlayer.getName() +
					" won being the cursed player at the last moment!");
		}
		else
		{
			Announcements.getInstance().announceToAll("The event has ended in a tie");
		}
	}

	@Override
	public void stopFight()
	{
		if (this.cursedPlayer != null)
		{
			this.cursedPlayer.stopVisualEffect(VisualEffect.S_AIR_STUN);
			this.cursedPlayer.removeSkill(9940);
			this.cursedPlayer = null;
		}
		super.stopFight();
	}

	@Override
	public String getRunningInfo(L2PcInstance player)
	{
		String html = "";
		if (this.cursedPlayer == null)
		{
			this.cursedPlayer = selectRandomParticipant();
		}
		if (this.cursedPlayer != null)
		{
			html += "Cursed player: " + this.cursedPlayer.getName() + ".";
		}
		else
		{
			html += "There is no cursed player at the moment.";
		}
		return html;
	}

	@Override
	public boolean onAction(L2PcInstance playerInstance, int targetedPlayerObjectId)
	{
		return !(!isCursedPlayer(targetedPlayerObjectId) && !isCursedPlayer(playerInstance.getObjectId()));
	}

	@Override
	public void onKill(L2Character killerCharacter, L2PcInstance killedPlayer)
	{
		if (killedPlayer == null || !isState(EventState.STARTED))
		{
			return;
		}

		new EventTeleporter(killedPlayer, this.teams[0].getCoords(), false, false);

		L2PcInstance killerPlayer = null;

		if (killerCharacter instanceof L2PetInstance || killerCharacter instanceof L2SummonInstance)
		{
			killerPlayer = ((L2Summon) killerCharacter).getOwner();
		}
		else if (killerCharacter instanceof L2PcInstance)
		{
			killerPlayer = (L2PcInstance) killerCharacter;
		}

		if (this.cursedPlayer == null || !this.cursedPlayer.isOnline() || !isPlayerParticipant(this.cursedPlayer.getObjectId()))
		{
			this.cursedPlayer = killerPlayer;
			int lvl = (int) Math.round(Math.log10(getParticipatedPlayersCount()) / Math.log10(2));
			if (lvl < 1)
			{
				lvl = 1;
			}
			else if (lvl > 11)
			{
				lvl = 11;
			}
			L2Skill curse = SkillTable.getInstance().getInfo(9940, lvl);
			this.cursedPlayer.addSkill(curse, false);
			this.cursedPlayer.setCurrentHp(this.cursedPlayer.getMaxHp());
			this.cursedPlayer.startVisualEffect(VisualEffect.S_AIR_STUN);
			this.cursedPlayer.broadcastUserInfo();
		}
		else if (killedPlayer.getObjectId() == this.cursedPlayer.getObjectId())
		{
			killedPlayer.removeSkill(9940);
			killedPlayer.stopVisualEffect(VisualEffect.S_AIR_STUN);
			killedPlayer.broadcastUserInfo();
			if (killerCharacter instanceof L2PcInstance && killedPlayer.getObjectId() != killerCharacter.getObjectId())
			{
				this.cursedPlayer = (L2PcInstance) killerCharacter;
				int lvl = (int) Math.round(Math.log10(getParticipatedPlayersCount()) / Math.log10(2));
				if (lvl < 1)
				{
					lvl = 1;
				}
				else if (lvl > 11)
				{
					lvl = 11;
				}
				L2Skill curse = SkillTable.getInstance().getInfo(9940, lvl);
				this.cursedPlayer.addSkill(curse, false);
				this.cursedPlayer.setCurrentHp(this.cursedPlayer.getMaxHp());
				this.cursedPlayer.startVisualEffect(VisualEffect.S_AIR_STUN);
				this.cursedPlayer.broadcastUserInfo();
				this.winners.add(0, this.cursedPlayer);
				sendToAllParticipants("The participant " + this.cursedPlayer.getName() + " killed the cursed player " +
						killedPlayer.getName() + ". Now he is the cursed player!");

				killerPlayer.addEventPoints(3);
				List<L2PcInstance> assistants =
						PlayerAssistsManager.getInstance().getAssistants(killerPlayer, killedPlayer, true);
				for (L2PcInstance assistant : assistants)
				{
					assistant.addEventPoints(1);
				}
			}
			else
			{
				this.cursedPlayer = null;
			}
		}
	}

	public boolean isCursedPlayer(int objectId)
	{
		return this.cursedPlayer != null && this.cursedPlayer.getObjectId() == objectId;
	}
}
