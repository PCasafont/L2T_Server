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
import l2server.L2DatabaseFactory;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.zone.type.L2OlympiadStadiumZone;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExOlympiadResult;
import l2server.gameserver.network.serverpackets.ExOlympiadUserInfo;
import l2server.gameserver.network.serverpackets.L2GameServerPacket;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * @author GodKratos, Pere, DS
 */
abstract public class OlympiadGameNormal extends AbstractOlympiadGame {
	protected OlympiadParticipant playerOne;
	protected OlympiadParticipant playerTwo;

	protected OlympiadGameNormal(int id, OlympiadParticipant[] opponents) {
		super(id);

		playerOne = opponents[0];
		playerTwo = opponents[1];

		playerOne.player.setOlympiadGameId(id);
		playerTwo.player.setOlympiadGameId(id);
	}

	protected static OlympiadParticipant[] createListOfParticipants(List<Integer> list) {
		if (list == null || list.isEmpty() || list.size() < 2) {
			return null;
		}

		L2PcInstance playerOne = null;
		L2PcInstance playerTwo = null;

		List<Integer> toRemove = new ArrayList<>();
		OlympiadParticipant[] result = null;

		for (int aPlayerId : list) {
			if (toRemove.contains(aPlayerId)) {
				continue;
			}

			playerOne = L2World.getInstance().getPlayer(aPlayerId);

			if (playerOne == null || !playerOne.isOnline()) {
				toRemove.add(aPlayerId);
				continue;
			}

			for (int bPlayerId : list) {
				if (aPlayerId == bPlayerId) {
					continue;
				}

				playerTwo = L2World.getInstance().getPlayer(bPlayerId);

				if (playerTwo == null || !playerTwo.isOnline()) {
					toRemove.add(bPlayerId);
					continue;
				}

				try {
					//if (playerOne.getHWID().equals(playerTwo.getHWID()))
					//{
					//	Util.logToFile(playerOne.getName() + " has the same HWID as " + playerTwo.getName(), "OlyFeed.txt", true);
					//	playerTwo = null;
					//	continue;
					//}
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (playerOne.hasAwakaned()) {
					if (!playerTwo.hasAwakaned()) {
						System.out.println(playerOne.getName() + " awakened but " + playerTwo.getName() + " did not, skipping...");
						playerTwo = null;
						continue;
					}
				} else if (playerTwo.hasAwakaned()) {
					System.out.println(playerOne.getName() + " did not awaken but " + playerTwo.getName() + " did, skipping...");
					playerTwo = null;
					continue;
				}

				break;
			}

			if (playerTwo != null) {
				result = new OlympiadParticipant[2];
				result[0] = new OlympiadParticipant(playerOne, 1);
				result[1] = new OlympiadParticipant(playerTwo, 2);
				toRemove.add(playerOne.getObjectId());
				toRemove.add(playerTwo.getObjectId());

				//System.out.println("Matched " + playerOne.getName() + " VS " + playerTwo.getName());
				//Util.logToFile(playerOne.getName() + " (HWID: " + playerOne.getClient().getHardwareId() + ", IP: " + playerOne.getInetAddress() + ") is going to Fight against " + playerTwo.getName() + " (HWID: " + playerTwo.getClient().getHardwareId() + ", IP: " + playerTwo.getInetAddress() + ")", "OlympiadLogs", true);
				break;
			}
		}

		for (int invalidatedPlayer : toRemove) {
			list.remove((Integer) invalidatedPlayer);
		}

		/*
		int playerOneObjectId = 0;
		int playerTwoObjectId = 0;
		L2PcInstance playerOne = null;
		L2PcInstance playerTwo = null;

		while (list.size() > 1)
		{
			int best = 0;
			for (Integer objId : list)
			{
				int strength = 0;
				L2PcInstance targetPlayer = L2World.getInstance().getPlayer(objId);
				if (targetPlayer != null)
					strength = targetPlayer.getStrenghtPoints(true);
				if (strength > best)
				{
					playerOneObjectId = objId;
					best = strength;
				}
			}
			list.remove((Integer)playerOneObjectId);
			playerOne = L2World.getInstance().getPlayer(playerOneObjectId);
			if (playerOne == null || !playerOne.isOnline())
				continue;

			best = 0;
			for (Integer objId : list)
			{
				int strength = 0;
				L2PcInstance targetPlayer = L2World.getInstance().getPlayer(objId);
				if (targetPlayer != null)
					strength = targetPlayer.getStrenghtPoints(true);
				if (strength > best)
				{
					playerTwoObjectId = objId;
					best = strength;
				}
			}
			list.remove((Integer)playerTwoObjectId);
			playerTwo = L2World.getInstance().getPlayer(playerTwoObjectId);
			if (playerTwo == null || !playerTwo.isOnline())
			{
				list.add(playerOneObjectId);
				continue;
			}

			OlympiadParticipant[] result = new OlympiadParticipant[2];
			result[0] = new OlympiadParticipant(playerOne, 1);
			result[1] = new OlympiadParticipant(playerTwo, 2);

			return result;
		}*/

		return result;
	}

	@Override
	public final boolean containsParticipant(int playerId) {
		return playerOne.objectId == playerId || playerTwo.objectId == playerId;
	}

	@Override
	public final void sendOlympiadInfo(L2Character player) {
		player.sendPacket(new ExOlympiadUserInfo(playerOne));
		player.sendPacket(new ExOlympiadUserInfo(playerTwo));
	}

	@Override
	public final void broadcastOlympiadInfo(L2OlympiadStadiumZone stadium) {
		broadcastPacket(new ExOlympiadUserInfo(playerOne), stadium);
		broadcastPacket(new ExOlympiadUserInfo(playerTwo), stadium);
	}

	@Override
	protected final void broadcastPacketToParticipants(L2GameServerPacket packet) {
		playerOne.updatePlayer();
		if (playerOne.player != null) {
			playerOne.player.sendPacket(packet);
		}

		playerTwo.updatePlayer();
		if (playerTwo.player != null) {
			playerTwo.player.sendPacket(packet);
		}
	}

	@Override
	protected final boolean portPlayersToArena(List<Location> spawns) {
		boolean result = true;
		try {
			result &= portPlayerToArena(playerOne, spawns.get(0), gameId);
			result &= portPlayerToArena(playerTwo, spawns.get(spawns.size() / 2), gameId);
		} catch (Exception e) {
			Log.log(Level.WARNING, "", e);
			return false;
		}
		return result;
	}

	@Override
	protected boolean needBuffers() {
		return true;
	}

	@Override
	protected final void removals() {
		if (aborted) {
			return;
		}

		removals(playerOne.player, true);
		removals(playerTwo.player, true);
	}

	@Override
	protected final boolean makeCompetitionStart() {
		if (!super.makeCompetitionStart()) {
			return false;
		}

		if (playerOne.player == null || playerTwo.player == null) {
			return false;
		}

		playerOne.player.setIsOlympiadStart(true);
		playerOne.player.updateEffectIcons();
		playerTwo.player.setIsOlympiadStart(true);
		playerTwo.player.updateEffectIcons();
		return true;
	}

	@Override
	protected final void cleanEffects() {
		if (playerOne.player != null && !playerOne.defaulted && !playerOne.disconnected && playerOne.player.getOlympiadGameId() == gameId) {
			cleanEffects(playerOne.player);
		}

		if (playerTwo.player != null && !playerTwo.defaulted && !playerTwo.disconnected && playerTwo.player.getOlympiadGameId() == gameId) {
			cleanEffects(playerTwo.player);
		}
	}

	@Override
	protected final void portPlayersBack() {
		if (playerOne.player != null && !playerOne.defaulted && !playerOne.disconnected) {
			portPlayerBack(playerOne.player);
		}
		if (playerTwo.player != null && !playerTwo.defaulted && !playerTwo.disconnected) {
			portPlayerBack(playerTwo.player);
		}
	}

	@Override
	protected final void playersStatusBack() {
		if (playerOne.player != null && !playerOne.defaulted && !playerOne.disconnected && playerOne.player.getOlympiadGameId() == gameId) {
			playerStatusBack(playerOne.player);
		}

		if (playerTwo.player != null && !playerTwo.defaulted && !playerTwo.disconnected && playerTwo.player.getOlympiadGameId() == gameId) {
			playerStatusBack(playerTwo.player);
		}
	}

	@Override
	protected final void clearPlayers() {
		playerOne.player = null;
		playerOne = null;
		playerTwo.player = null;
		playerTwo = null;
	}

	@Override
	protected final void handleDisconnect(L2PcInstance player) {
		if (player.getObjectId() == playerOne.objectId) {
			playerOne.disconnected = true;
		} else if (player.getObjectId() == playerTwo.objectId) {
			playerTwo.disconnected = true;
		}
	}

	@Override
	protected final boolean checkBattleStatus() {
		if (aborted) {
			return false;
		}

		if (playerOne.player == null || playerOne.disconnected) {
			return false;
		}

		return !(playerTwo.player == null || playerTwo.disconnected);
	}

	@Override
	protected final boolean haveWinner() {
		if (!checkBattleStatus()) {
			return true;
		}

		boolean playerOneLost = true;
		try {
			if (playerOne.player.getOlympiadGameId() == gameId) {
				playerOneLost = playerOne.player.isDead();
			}
		} catch (Exception e) {
			playerOneLost = true;
		}

		boolean playerTwoLost = true;
		try {
			if (playerTwo.player.getOlympiadGameId() == gameId) {
				playerTwoLost = playerTwo.player.isDead();
			}
		} catch (Exception e) {
			playerTwoLost = true;
		}

		return playerOneLost || playerTwoLost;
	}

	@Override
	protected void validateWinner(L2OlympiadStadiumZone stadium) {
		if (aborted) {
			return;
		}

		final boolean pOneCrash = playerOne.player == null || playerOne.disconnected;
		final boolean pTwoCrash = playerTwo.player == null || playerTwo.disconnected;

		final int playerOnePoints = playerOne.nobleInfo.getPoints();
		final int playerTwoPoints = playerTwo.nobleInfo.getPoints();
		int pointDiff = Math.min(playerOnePoints, playerTwoPoints) / getDivider();
		if (pointDiff <= 0) {
			pointDiff = 1;
		} else if (pointDiff > Config.ALT_OLY_MAX_POINTS) {
			pointDiff = Config.ALT_OLY_MAX_POINTS;
		}

		int points;
		SystemMessage sm;

		// Check for if a player defaulted before battle started
		if (playerOne.defaulted || playerTwo.defaulted) {
			try {
				if (playerOne.defaulted) {
					try {
						points = Math.min(playerOnePoints / 5, Config.ALT_OLY_MAX_POINTS);
						removePointsFromParticipant(playerOne, points);

						if (Config.ALT_OLY_LOG_FIGHTS && playerOne.player != null && playerOne.player != null) {
							logFight(playerOne.player.getObjectId(), playerTwo.player.getObjectId(), 0, 0, 0, 0, points, getType().toString());
						}
					} catch (Exception e) {
						Log.log(Level.WARNING, "Exception on validateWinner(): " + e.getMessage(), e);
					}
				}
				if (playerTwo.defaulted) {
					try {
						points = Math.min(playerTwoPoints / 5, Config.ALT_OLY_MAX_POINTS);
						removePointsFromParticipant(playerTwo, points);

						if (Config.ALT_OLY_LOG_FIGHTS && playerOne.player != null && playerOne.player != null) {
							LogRecord record = new LogRecord(Level.INFO, playerTwo.name + " default");
							record.setParameters(new Object[]{playerOne.name, playerTwo.name, 0, 0, 0, 0, points, getType().toString()});
							logResults.log(record);
						}
					} catch (Exception e) {
						Log.log(Level.WARNING, "Exception on validateWinner(): " + e.getMessage(), e);
					}
				}
				return;
			} catch (Exception e) {
				Log.log(Level.WARNING, "Exception on validateWinner(): " + e.getMessage(), e);
				return;
			}
		}

		// Create results for players if a player crashed
		if (pOneCrash || pTwoCrash) {
			try {
				if (pTwoCrash && !pOneCrash) {
					sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_WON_THE_GAME);
					sm.addString(playerOne.name);
					broadcastPacket(sm, stadium);

					playerOne.nobleInfo.increaseVictories();
					addPointsToParticipant(playerOne, pointDiff);

					playerTwo.nobleInfo.increaseDefeats();
					removePointsFromParticipant(playerTwo, pointDiff);

					int[][] reward = getReward().clone();
					rewardParticipant(playerOne.player, reward);
					for (int i = 0; i < reward.length; i++) {
						reward[i][1] = reward[i][1] * 6 / 10;
					}
					rewardParticipant(playerTwo.player, reward);

					broadcastPacket(new ExOlympiadResult(new Object[]{0, playerOne.name, 1, pointDiff, playerOne.player, playerTwo.player}), stadium);

					if (Config.ALT_OLY_LOG_FIGHTS && playerOne.player != null && playerOne.player != null) {
						LogRecord record = new LogRecord(Level.INFO, playerTwo.name + " crash");
						record.setParameters(new Object[]{playerOne.name, playerTwo.name, 0, 0, 0, 0, pointDiff, getType().toString()});
						logResults.log(record);
					}
				} else if (pOneCrash && !pTwoCrash) {
					sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_WON_THE_GAME);
					sm.addString(playerTwo.name);
					broadcastPacket(sm, stadium);

					playerTwo.nobleInfo.increaseVictories();
					addPointsToParticipant(playerTwo, pointDiff);

					playerOne.nobleInfo.increaseDefeats();
					removePointsFromParticipant(playerOne, pointDiff);

					int[][] reward = getReward().clone();
					rewardParticipant(playerTwo.player, reward);
					for (int i = 0; i < reward.length; i++) {
						reward[i][1] = reward[i][1] * 6 / 10;
					}
					rewardParticipant(playerOne.player, reward);

					broadcastPacket(new ExOlympiadResult(new Object[]{1, playerTwo.name, 1, pointDiff, playerOne.player, playerTwo.player}), stadium);

					if (Config.ALT_OLY_LOG_FIGHTS && playerOne.player != null && playerOne.player != null) {
						logFight(playerOne.player.getObjectId(), playerTwo.player.getObjectId(), 0, 0, 0, 0, pointDiff, getType().toString());
					}
				} else if (pOneCrash && pTwoCrash) {
					broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_ENDED_IN_A_TIE), stadium);

					playerOne.nobleInfo.increaseDefeats();
					removePointsFromParticipant(playerOne, pointDiff);

					playerTwo.nobleInfo.increaseDefeats();
					removePointsFromParticipant(playerTwo, pointDiff);

					if (Config.ALT_OLY_LOG_FIGHTS && playerOne.player != null && playerOne.player != null) {
						logFight(playerOne.player.getObjectId(), playerTwo.player.getObjectId(), 0, 0, 0, 0, pointDiff, getType().toString());
					}
				}

				playerOne.nobleInfo.increaseMatches();
				playerTwo.nobleInfo.increaseMatches();
				if (getType() == CompetitionType.CLASSED) {
					playerOne.nobleInfo.increaseClassedMatches();
					playerTwo.nobleInfo.increaseClassedMatches();
				} else {
					playerOne.nobleInfo.increaseNonClassedMatches();
					playerTwo.nobleInfo.increaseNonClassedMatches();
				}
			} catch (Exception e) {
				Log.log(Level.WARNING, "Exception on validateWinner(): " + e.getMessage(), e);
			}

			return;
		}

		try {
			// Calculate Fight time
			long fightTime = System.currentTimeMillis() - startTime;

			double playerOneHp = 0;
			if (playerOne.player != null && !playerOne.player.isDead()) {
				playerOneHp = playerOne.player.getCurrentHp() + playerOne.player.getCurrentCp();
				if (playerOneHp < 0.5) {
					playerOneHp = 0;
				}
			}

			double playerTwoHp = 0;
			if (playerTwo.player != null && !playerTwo.player.isDead()) {
				playerTwoHp = playerTwo.player.getCurrentHp() + playerTwo.player.getCurrentCp();
				if (playerTwoHp < 0.5) {
					playerTwoHp = 0;
				}
			}

			// if players crashed, search if they've relogged
			playerOne.updatePlayer();
			playerTwo.updatePlayer();

			if ((playerOne.player == null || !playerOne.player.isOnline()) && (playerTwo.player == null || !playerTwo.player.isOnline())) {
				playerOne.nobleInfo.increaseDraws();
				playerTwo.nobleInfo.increaseDraws();
				sm = SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_ENDED_IN_A_TIE);
				broadcastPacket(sm, stadium);
			} else if (playerTwo.player == null || !playerTwo.player.isOnline() || playerTwoHp == 0 && playerOneHp != 0 ||
					playerOne.player.getOlyGivenDmg() > playerTwo.player.getOlyGivenDmg() && playerTwoHp != 0 && playerOneHp != 0) {
				sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_WON_THE_GAME);
				sm.addString(playerOne.name);
				broadcastPacket(sm, stadium);

				playerOne.nobleInfo.increaseVictories();
				playerTwo.nobleInfo.increaseDefeats();

				addPointsToParticipant(playerOne, pointDiff);
				removePointsFromParticipant(playerTwo, pointDiff);

				// Save Fight Result
				saveResults(playerOne, playerTwo, 1, startTime, fightTime, getType());
				int[][] reward = getReward().clone();
				rewardParticipant(playerOne.player, reward);
				for (int i = 0; i < reward.length; i++) {
					reward[i][1] = reward[i][1] * 6 / 10;
				}
				rewardParticipant(playerTwo.player, reward);

				broadcastPacket(new ExOlympiadResult(new Object[]{0, playerOne.name, 1, pointDiff, playerOne.player, playerTwo.player}), stadium);

				playerOne.competitionDone(getType(), true);
				playerTwo.competitionDone(getType(), false);
			} else if (playerOne.player == null || !playerOne.player.isOnline() || playerOneHp == 0 && playerTwoHp != 0 ||
					playerTwo.player.getOlyGivenDmg() > playerOne.player.getOlyGivenDmg() && playerOneHp != 0 && playerTwoHp != 0) {
				sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_WON_THE_GAME);
				sm.addString(playerTwo.name);
				broadcastPacket(sm, stadium);

				playerTwo.nobleInfo.increaseVictories();
				playerOne.nobleInfo.increaseDefeats();

				addPointsToParticipant(playerTwo, pointDiff);
				removePointsFromParticipant(playerOne, pointDiff);

				// Save Fight Result
				saveResults(playerOne, playerTwo, 2, startTime, fightTime, getType());
				int[][] reward = getReward().clone();
				rewardParticipant(playerTwo.player, reward);
				for (int i = 0; i < reward.length; i++) {
					reward[i][1] = reward[i][1] * 6 / 10;
				}
				rewardParticipant(playerOne.player, reward);

				broadcastPacket(new ExOlympiadResult(new Object[]{1, playerTwo.name, 1, pointDiff, playerOne.player, playerTwo.player}), stadium);

				playerOne.competitionDone(getType(), false);
				playerTwo.competitionDone(getType(), true);
			} else {
				// Save Fight Result
				saveResults(playerOne, playerTwo, 0, startTime, fightTime, getType());

				sm = SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_ENDED_IN_A_TIE);
				broadcastPacket(sm, stadium);

				removePointsFromParticipant(playerOne, Math.min(playerOnePoints / getDivider(), Config.ALT_OLY_MAX_POINTS));
				removePointsFromParticipant(playerTwo, Math.min(playerTwoPoints / getDivider(), Config.ALT_OLY_MAX_POINTS));

				broadcastPacket(new ExOlympiadResult(new Object[]{-1, "", 1, pointDiff, playerOne.player, playerTwo.player}), stadium);

				playerOne.competitionDone(getType(), false);
				playerTwo.competitionDone(getType(), false);
			}

			playerOne.nobleInfo.increaseMatches();
			playerTwo.nobleInfo.increaseMatches();
			if (getType() == CompetitionType.CLASSED) {
				playerOne.nobleInfo.increaseClassedMatches();
				playerTwo.nobleInfo.increaseClassedMatches();
			} else {
				playerOne.nobleInfo.increaseNonClassedMatches();
				playerTwo.nobleInfo.increaseNonClassedMatches();
			}

			if (Config.ALT_OLY_LOG_FIGHTS) {
				logFight(playerOne.player.getObjectId(),
						playerTwo.player.getObjectId(),
						playerOneHp,
						playerTwoHp,
						playerOne.player.getOlyGivenDmg(),
						playerTwo.player.getOlyGivenDmg(),
						pointDiff,
						getType().toString());
			}
		} catch (Exception e) {
			Log.log(Level.WARNING, "Exception on validateWinner(): " + e.getMessage(), e);
		}
	}

	/**
	 * @param char1Id
	 * @param char2Id
	 * @param points
	 * @param competitionType
	 */
	private void logFight(int char1Id, int char2Id, double char1Hp, double char2Hp, int char1Dmg, int char2Dmg, int points, String competitionType) {
		Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(
					"INSERT INTO log_olys(player1_id, player2_id, player1_hp, player2_hp, player1_damage, player2_damage, points, competition_type, time) VALUES(?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, char1Id);
			statement.setInt(2, char2Id);
			statement.setDouble(3, char1Hp);
			statement.setDouble(4, char2Hp);
			statement.setInt(5, char1Dmg);
			statement.setInt(6, char2Dmg);
			statement.setInt(7, points);
			statement.setString(8, competitionType);
			statement.setLong(9, System.currentTimeMillis());
			statement.execute();
			statement.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			L2DatabaseFactory.close(con);
		}
	}

	@Override
	protected final void addDamage(L2PcInstance player, int damage) {
	}

	@Override
	public final String[] getPlayerNames() {
		return new String[]{playerOne.name, playerTwo.name};
	}

	@Override
	public final boolean checkDefaulted() {
		SystemMessage reason;
		playerOne.updatePlayer();
		playerTwo.updatePlayer();

		reason = checkDefaulted(playerOne);
		if (reason != null) {
			playerOne.defaulted = true;
			if (playerTwo.player != null) {
				playerTwo.player.sendPacket(reason);
			}
		}

		reason = checkDefaulted(playerTwo);
		if (reason != null) {
			playerTwo.defaulted = true;
			if (playerOne.player != null) {
				playerOne.player.sendPacket(reason);
			}
		}

		return playerOne.defaulted || playerTwo.defaulted;
	}

	@Override
	public final void resetDamage() {
	}

	protected static void saveResults(OlympiadParticipant one,
	                                  OlympiadParticipant two,
	                                  int winner,
	                                  long startTime,
	                                  long fightTime,
	                                  CompetitionType type) {
		Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(
					"INSERT INTO olympiad_fights (charOneId, charTwoId, charOneClass, charTwoClass, winner, start, time, classed) VALUES(?,?,?,?,?,?,?,?)");
			statement.setInt(1, one.objectId);
			statement.setInt(2, two.objectId);
			statement.setInt(3, one.baseClass);
			statement.setInt(4, two.baseClass);
			statement.setInt(5, winner);
			statement.setLong(6, startTime);
			statement.setLong(7, fightTime);
			statement.setInt(8, type == CompetitionType.CLASSED ? 1 : 0);
			statement.execute();
			statement.close();
		} catch (SQLException e) {
			if (Log.isLoggable(Level.SEVERE)) {
				Log.log(Level.SEVERE, "SQL exception while saving olympiad Fight.", e);
			}
		} finally {
			L2DatabaseFactory.close(con);
		}
	}
}
