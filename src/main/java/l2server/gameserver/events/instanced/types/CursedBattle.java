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
public class CursedBattle extends EventInstance {
	private L2PcInstance cursedPlayer;

	List<L2PcInstance> winners = new ArrayList<>();

	public CursedBattle(int id, EventConfig config) {
		super(id, config);
	}

	@Override
	public boolean startFight() {
		if (!super.startFight()) {
			return false;
		}

		cursedPlayer = selectRandomParticipant();

		int lvl = (int) Math.round(Math.log10(getParticipatedPlayersCount()) / Math.log10(2));
		if (lvl < 1) {
			lvl = 1;
		} else if (lvl > 11) {
			lvl = 11;
		}
		L2Skill curse = SkillTable.getInstance().getInfo(9940, lvl);
		cursedPlayer.addSkill(curse, false);
		cursedPlayer.setCurrentHp(cursedPlayer.getMaxHp());
		cursedPlayer.startVisualEffect(VisualEffect.S_AIR_STUN);
		cursedPlayer.broadcastUserInfo();
		winners.add(0, cursedPlayer);
		return true;
	}

	@Override
	public void calculateRewards() {
		if (cursedPlayer != null) {
			rewardPlayers(winners);
			Announcements.getInstance()
					.announceToAll("The event has ended. The player " + cursedPlayer.getName() + " won being the cursed player at the last moment!");
		} else {
			Announcements.getInstance().announceToAll("The event has ended in a tie");
		}
	}

	@Override
	public void stopFight() {
		if (cursedPlayer != null) {
			cursedPlayer.stopVisualEffect(VisualEffect.S_AIR_STUN);
			cursedPlayer.removeSkill(9940);
			cursedPlayer = null;
		}
		super.stopFight();
	}

	@Override
	public String getRunningInfo(L2PcInstance player) {
		String html = "";
		if (cursedPlayer == null) {
			cursedPlayer = selectRandomParticipant();
		}
		if (cursedPlayer != null) {
			html += "Cursed player: " + cursedPlayer.getName() + ".";
		} else {
			html += "There is no cursed player at the moment.";
		}
		return html;
	}

	@Override
	public boolean onAction(L2PcInstance playerInstance, int targetedPlayerObjectId) {
		return !(!isCursedPlayer(targetedPlayerObjectId) && !isCursedPlayer(playerInstance.getObjectId()));
	}

	@Override
	public void onKill(L2Character killerCharacter, L2PcInstance killedPlayer) {
		if (killedPlayer == null || !isState(EventState.STARTED)) {
			return;
		}

		new EventTeleporter(killedPlayer, teams[0].getCoords(), false, false);

		L2PcInstance killerPlayer = null;

		if (killerCharacter instanceof L2PetInstance || killerCharacter instanceof L2SummonInstance) {
			killerPlayer = ((L2Summon) killerCharacter).getOwner();
		} else if (killerCharacter instanceof L2PcInstance) {
			killerPlayer = (L2PcInstance) killerCharacter;
		}

		if (cursedPlayer == null || !cursedPlayer.isOnline() || !isPlayerParticipant(cursedPlayer.getObjectId())) {
			cursedPlayer = killerPlayer;
			int lvl = (int) Math.round(Math.log10(getParticipatedPlayersCount()) / Math.log10(2));
			if (lvl < 1) {
				lvl = 1;
			} else if (lvl > 11) {
				lvl = 11;
			}
			L2Skill curse = SkillTable.getInstance().getInfo(9940, lvl);
			cursedPlayer.addSkill(curse, false);
			cursedPlayer.setCurrentHp(cursedPlayer.getMaxHp());
			cursedPlayer.startVisualEffect(VisualEffect.S_AIR_STUN);
			cursedPlayer.broadcastUserInfo();
		} else if (killedPlayer.getObjectId() == cursedPlayer.getObjectId()) {
			killedPlayer.removeSkill(9940);
			killedPlayer.stopVisualEffect(VisualEffect.S_AIR_STUN);
			killedPlayer.broadcastUserInfo();
			if (killerCharacter instanceof L2PcInstance && killedPlayer.getObjectId() != killerCharacter.getObjectId()) {
				cursedPlayer = (L2PcInstance) killerCharacter;
				int lvl = (int) Math.round(Math.log10(getParticipatedPlayersCount()) / Math.log10(2));
				if (lvl < 1) {
					lvl = 1;
				} else if (lvl > 11) {
					lvl = 11;
				}
				L2Skill curse = SkillTable.getInstance().getInfo(9940, lvl);
				cursedPlayer.addSkill(curse, false);
				cursedPlayer.setCurrentHp(cursedPlayer.getMaxHp());
				cursedPlayer.startVisualEffect(VisualEffect.S_AIR_STUN);
				cursedPlayer.broadcastUserInfo();
				winners.add(0, cursedPlayer);
				sendToAllParticipants("The participant " + cursedPlayer.getName() + " killed the cursed player " + killedPlayer.getName() +
						". Now he is the cursed player!");

				killerPlayer.addEventPoints(3);
				List<L2PcInstance> assistants = PlayerAssistsManager.getInstance().getAssistants(killerPlayer, killedPlayer, true);
				for (L2PcInstance assistant : assistants) {
					assistant.addEventPoints(1);
				}
			} else {
				cursedPlayer = null;
			}
		}
	}

	public boolean isCursedPlayer(int objectId) {
		return cursedPlayer != null && cursedPlayer.getObjectId() == objectId;
	}
}
