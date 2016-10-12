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
abstract public class OlympiadGameNormal extends AbstractOlympiadGame
{
	protected OlympiadParticipant _playerOne;
	protected OlympiadParticipant _playerTwo;

	protected OlympiadGameNormal(int id, OlympiadParticipant[] opponents)
	{
		super(id);

		_playerOne = opponents[0];
		_playerTwo = opponents[1];

		_playerOne.player.setOlympiadGameId(id);
		_playerTwo.player.setOlympiadGameId(id);
	}

	protected static OlympiadParticipant[] createListOfParticipants(List<Integer> list)
	{
		if (list == null || list.isEmpty() || list.size() < 2)
		{
			return null;
		}

		L2PcInstance playerOne = null;
		L2PcInstance playerTwo = null;

		List<Integer> toRemove = new ArrayList<>();
		OlympiadParticipant[] result = null;

		for (int aPlayerId : list)
		{
			if (toRemove.contains(aPlayerId))
			{
				continue;
			}

			playerOne = L2World.getInstance().getPlayer(aPlayerId);

			if (playerOne == null || !playerOne.isOnline())
			{
				toRemove.add(aPlayerId);
				continue;
			}

			for (int bPlayerId : list)
			{
				if (aPlayerId == bPlayerId)
				{
					continue;
				}

				playerTwo = L2World.getInstance().getPlayer(bPlayerId);

				if (playerTwo == null || !playerTwo.isOnline())
				{
					toRemove.add(bPlayerId);
					continue;
				}

				try
				{
					//if (playerOne.getHWID().equals(playerTwo.getHWID()))
					//{
					//	Util.logToFile(playerOne.getName() + " has the same HWID as " + playerTwo.getName(), "OlyFeed.txt", true);
					//	playerTwo = null;
					//	continue;
					//}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				if (playerOne.hasAwakaned())
				{
					if (!playerTwo.hasAwakaned())
					{
						System.out.println(
								playerOne.getName() + " awakened but " + playerTwo.getName() + " did not, skipping...");
						playerTwo = null;
						continue;
					}
				}
				else if (playerTwo.hasAwakaned())
				{
					System.out.println(
							playerOne.getName() + " did not awaken but " + playerTwo.getName() + " did, skipping...");
					playerTwo = null;
					continue;
				}

				break;
			}

			if (playerTwo != null)
			{
				result = new OlympiadParticipant[2];
				result[0] = new OlympiadParticipant(playerOne, 1);
				result[1] = new OlympiadParticipant(playerTwo, 2);
				toRemove.add(playerOne.getObjectId());
				toRemove.add(playerTwo.getObjectId());

				//System.out.println("Matched " + playerOne.getName() + " VS " + playerTwo.getName());
				//Util.logToFile(playerOne.getName() + " (HWID: " + playerOne.getClient().getHardwareId() + ", IP: " + playerOne.getInetAddress() + ") is going to fight against " + playerTwo.getName() + " (HWID: " + playerTwo.getClient().getHardwareId() + ", IP: " + playerTwo.getInetAddress() + ")", "OlympiadLogs", true);
				break;
			}
		}

		for (int invalidatedPlayer : toRemove)
		{
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
	public final boolean containsParticipant(int playerId)
	{
		return _playerOne.objectId == playerId || _playerTwo.objectId == playerId;
	}

	@Override
	public final void sendOlympiadInfo(L2Character player)
	{
		player.sendPacket(new ExOlympiadUserInfo(_playerOne));
		player.sendPacket(new ExOlympiadUserInfo(_playerTwo));
	}

	@Override
	public final void broadcastOlympiadInfo(L2OlympiadStadiumZone stadium)
	{
		broadcastPacket(new ExOlympiadUserInfo(_playerOne), stadium);
		broadcastPacket(new ExOlympiadUserInfo(_playerTwo), stadium);
	}

	@Override
	protected final void broadcastPacketToParticipants(L2GameServerPacket packet)
	{
		_playerOne.updatePlayer();
		if (_playerOne.player != null)
		{
			_playerOne.player.sendPacket(packet);
		}

		_playerTwo.updatePlayer();
		if (_playerTwo.player != null)
		{
			_playerTwo.player.sendPacket(packet);
		}
	}

	@Override
	protected final boolean portPlayersToArena(List<Location> spawns)
	{
		boolean result = true;
		try
		{
			result &= portPlayerToArena(_playerOne, spawns.get(0), _gameId);
			result &= portPlayerToArena(_playerTwo, spawns.get(spawns.size() / 2), _gameId);
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "", e);
			return false;
		}
		return result;
	}

	@Override
	protected boolean needBuffers()
	{
		return true;
	}

	@Override
	protected final void removals()
	{
		if (_aborted)
		{
			return;
		}

		removals(_playerOne.player, true);
		removals(_playerTwo.player, true);
	}

	@Override
	protected final boolean makeCompetitionStart()
	{
		if (!super.makeCompetitionStart())
		{
			return false;
		}

		if (_playerOne.player == null || _playerTwo.player == null)
		{
			return false;
		}

		_playerOne.player.setIsOlympiadStart(true);
		_playerOne.player.updateEffectIcons();
		_playerTwo.player.setIsOlympiadStart(true);
		_playerTwo.player.updateEffectIcons();
		return true;
	}

	@Override
	protected final void cleanEffects()
	{
		if (_playerOne.player != null && !_playerOne.defaulted && !_playerOne.disconnected &&
				_playerOne.player.getOlympiadGameId() == _gameId)
		{
			cleanEffects(_playerOne.player);
		}

		if (_playerTwo.player != null && !_playerTwo.defaulted && !_playerTwo.disconnected &&
				_playerTwo.player.getOlympiadGameId() == _gameId)
		{
			cleanEffects(_playerTwo.player);
		}
	}

	@Override
	protected final void portPlayersBack()
	{
		if (_playerOne.player != null && !_playerOne.defaulted && !_playerOne.disconnected)
		{
			portPlayerBack(_playerOne.player);
		}
		if (_playerTwo.player != null && !_playerTwo.defaulted && !_playerTwo.disconnected)
		{
			portPlayerBack(_playerTwo.player);
		}
	}

	@Override
	protected final void playersStatusBack()
	{
		if (_playerOne.player != null && !_playerOne.defaulted && !_playerOne.disconnected &&
				_playerOne.player.getOlympiadGameId() == _gameId)
		{
			playerStatusBack(_playerOne.player);
		}

		if (_playerTwo.player != null && !_playerTwo.defaulted && !_playerTwo.disconnected &&
				_playerTwo.player.getOlympiadGameId() == _gameId)
		{
			playerStatusBack(_playerTwo.player);
		}
	}

	@Override
	protected final void clearPlayers()
	{
		_playerOne.player = null;
		_playerOne = null;
		_playerTwo.player = null;
		_playerTwo = null;
	}

	@Override
	protected final void handleDisconnect(L2PcInstance player)
	{
		if (player.getObjectId() == _playerOne.objectId)
		{
			_playerOne.disconnected = true;
		}
		else if (player.getObjectId() == _playerTwo.objectId)
		{
			_playerTwo.disconnected = true;
		}
	}

	@Override
	protected final boolean checkBattleStatus()
	{
		if (_aborted)
		{
			return false;
		}

		if (_playerOne.player == null || _playerOne.disconnected)
		{
			return false;
		}

		return !(_playerTwo.player == null || _playerTwo.disconnected);

	}

	@Override
	protected final boolean haveWinner()
	{
		if (!checkBattleStatus())
		{
			return true;
		}

		boolean playerOneLost = true;
		try
		{
			if (_playerOne.player.getOlympiadGameId() == _gameId)
			{
				playerOneLost = _playerOne.player.isDead();
			}
		}
		catch (Exception e)
		{
			playerOneLost = true;
		}

		boolean playerTwoLost = true;
		try
		{
			if (_playerTwo.player.getOlympiadGameId() == _gameId)
			{
				playerTwoLost = _playerTwo.player.isDead();
			}
		}
		catch (Exception e)
		{
			playerTwoLost = true;
		}

		return playerOneLost || playerTwoLost;
	}

	@Override
	protected void validateWinner(L2OlympiadStadiumZone stadium)
	{
		if (_aborted)
		{
			return;
		}

		final boolean pOneCrash = _playerOne.player == null || _playerOne.disconnected;
		final boolean pTwoCrash = _playerTwo.player == null || _playerTwo.disconnected;

		final int playerOnePoints = _playerOne.nobleInfo.getPoints();
		final int playerTwoPoints = _playerTwo.nobleInfo.getPoints();
		int pointDiff = Math.min(playerOnePoints, playerTwoPoints) / getDivider();
		if (pointDiff <= 0)
		{
			pointDiff = 1;
		}
		else if (pointDiff > Config.ALT_OLY_MAX_POINTS)
		{
			pointDiff = Config.ALT_OLY_MAX_POINTS;
		}

		int points;
		SystemMessage sm;

		// Check for if a player defaulted before battle started
		if (_playerOne.defaulted || _playerTwo.defaulted)
		{
			try
			{
				if (_playerOne.defaulted)
				{
					try
					{
						points = Math.min(playerOnePoints / 5, Config.ALT_OLY_MAX_POINTS);
						removePointsFromParticipant(_playerOne, points);

						if (Config.ALT_OLY_LOG_FIGHTS && _playerOne.player != null && _playerOne.player != null)
						{
							logFight(_playerOne.player.getObjectId(), _playerTwo.player.getObjectId(), 0, 0, 0, 0,
									points, getType().toString());
						}
					}
					catch (Exception e)
					{
						Log.log(Level.WARNING, "Exception on validateWinner(): " + e.getMessage(), e);
					}
				}
				if (_playerTwo.defaulted)
				{
					try
					{
						points = Math.min(playerTwoPoints / 5, Config.ALT_OLY_MAX_POINTS);
						removePointsFromParticipant(_playerTwo, points);

						if (Config.ALT_OLY_LOG_FIGHTS && _playerOne.player != null && _playerOne.player != null)
						{
							LogRecord record = new LogRecord(Level.INFO, _playerTwo.name + " default");
							record.setParameters(new Object[]{
									_playerOne.name, _playerTwo.name, 0, 0, 0, 0, points, getType().toString()
							});
							_logResults.log(record);
						}
					}
					catch (Exception e)
					{
						Log.log(Level.WARNING, "Exception on validateWinner(): " + e.getMessage(), e);
					}
				}
				return;
			}
			catch (Exception e)
			{
				Log.log(Level.WARNING, "Exception on validateWinner(): " + e.getMessage(), e);
				return;
			}
		}

		// Create results for players if a player crashed
		if (pOneCrash || pTwoCrash)
		{
			try
			{
				if (pTwoCrash && !pOneCrash)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_WON_THE_GAME);
					sm.addString(_playerOne.name);
					broadcastPacket(sm, stadium);

					_playerOne.nobleInfo.increaseVictories();
					addPointsToParticipant(_playerOne, pointDiff);

					_playerTwo.nobleInfo.increaseDefeats();
					removePointsFromParticipant(_playerTwo, pointDiff);

					int[][] reward = getReward().clone();
					rewardParticipant(_playerOne.player, reward);
					for (int i = 0; i < reward.length; i++)
					{
						reward[i][1] = reward[i][1] * 6 / 10;
					}
					rewardParticipant(_playerTwo.player, reward);

					broadcastPacket(new ExOlympiadResult(
									new Object[]{0, _playerOne.name, 1, pointDiff, _playerOne.player, _playerTwo.player}),
							stadium);

					if (Config.ALT_OLY_LOG_FIGHTS && _playerOne.player != null && _playerOne.player != null)
					{
						LogRecord record = new LogRecord(Level.INFO, _playerTwo.name + " crash");
						record.setParameters(new Object[]{
								_playerOne.name, _playerTwo.name, 0, 0, 0, 0, pointDiff, getType().toString()
						});
						_logResults.log(record);
					}
				}
				else if (pOneCrash && !pTwoCrash)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_WON_THE_GAME);
					sm.addString(_playerTwo.name);
					broadcastPacket(sm, stadium);

					_playerTwo.nobleInfo.increaseVictories();
					addPointsToParticipant(_playerTwo, pointDiff);

					_playerOne.nobleInfo.increaseDefeats();
					removePointsFromParticipant(_playerOne, pointDiff);

					int[][] reward = getReward().clone();
					rewardParticipant(_playerTwo.player, reward);
					for (int i = 0; i < reward.length; i++)
					{
						reward[i][1] = reward[i][1] * 6 / 10;
					}
					rewardParticipant(_playerOne.player, reward);

					broadcastPacket(new ExOlympiadResult(
									new Object[]{1, _playerTwo.name, 1, pointDiff, _playerOne.player, _playerTwo.player}),
							stadium);

					if (Config.ALT_OLY_LOG_FIGHTS && _playerOne.player != null && _playerOne.player != null)
					{
						logFight(_playerOne.player.getObjectId(), _playerTwo.player.getObjectId(), 0, 0, 0, 0,
								pointDiff, getType().toString());
					}
				}
				else if (pOneCrash && pTwoCrash)
				{
					broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_ENDED_IN_A_TIE), stadium);

					_playerOne.nobleInfo.increaseDefeats();
					removePointsFromParticipant(_playerOne, pointDiff);

					_playerTwo.nobleInfo.increaseDefeats();
					removePointsFromParticipant(_playerTwo, pointDiff);

					if (Config.ALT_OLY_LOG_FIGHTS && _playerOne.player != null && _playerOne.player != null)
					{
						logFight(_playerOne.player.getObjectId(), _playerTwo.player.getObjectId(), 0, 0, 0, 0,
								pointDiff, getType().toString());
					}
				}

				_playerOne.nobleInfo.increaseMatches();
				_playerTwo.nobleInfo.increaseMatches();
				if (getType() == CompetitionType.CLASSED)
				{
					_playerOne.nobleInfo.increaseClassedMatches();
					_playerTwo.nobleInfo.increaseClassedMatches();
				}
				else
				{
					_playerOne.nobleInfo.increaseNonClassedMatches();
					_playerTwo.nobleInfo.increaseNonClassedMatches();
				}
			}
			catch (Exception e)
			{
				Log.log(Level.WARNING, "Exception on validateWinner(): " + e.getMessage(), e);
			}

			return;
		}

		try
		{
			// Calculate Fight time
			long _fightTime = System.currentTimeMillis() - _startTime;

			double playerOneHp = 0;
			if (_playerOne.player != null && !_playerOne.player.isDead())
			{
				playerOneHp = _playerOne.player.getCurrentHp() + _playerOne.player.getCurrentCp();
				if (playerOneHp < 0.5)
				{
					playerOneHp = 0;
				}
			}

			double playerTwoHp = 0;
			if (_playerTwo.player != null && !_playerTwo.player.isDead())
			{
				playerTwoHp = _playerTwo.player.getCurrentHp() + _playerTwo.player.getCurrentCp();
				if (playerTwoHp < 0.5)
				{
					playerTwoHp = 0;
				}
			}

			// if players crashed, search if they've relogged
			_playerOne.updatePlayer();
			_playerTwo.updatePlayer();

			if ((_playerOne.player == null || !_playerOne.player.isOnline()) &&
					(_playerTwo.player == null || !_playerTwo.player.isOnline()))
			{
				_playerOne.nobleInfo.increaseDraws();
				_playerTwo.nobleInfo.increaseDraws();
				sm = SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_ENDED_IN_A_TIE);
				broadcastPacket(sm, stadium);
			}
			else if (_playerTwo.player == null || !_playerTwo.player.isOnline() ||
					playerTwoHp == 0 && playerOneHp != 0 ||
					_playerOne.player.getOlyGivenDmg() > _playerTwo.player.getOlyGivenDmg() && playerTwoHp != 0 &&
							playerOneHp != 0)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_WON_THE_GAME);
				sm.addString(_playerOne.name);
				broadcastPacket(sm, stadium);

				_playerOne.nobleInfo.increaseVictories();
				_playerTwo.nobleInfo.increaseDefeats();

				addPointsToParticipant(_playerOne, pointDiff);
				removePointsFromParticipant(_playerTwo, pointDiff);

				// Save Fight Result
				saveResults(_playerOne, _playerTwo, 1, _startTime, _fightTime, getType());
				int[][] reward = getReward().clone();
				rewardParticipant(_playerOne.player, reward);
				for (int i = 0; i < reward.length; i++)
				{
					reward[i][1] = reward[i][1] * 6 / 10;
				}
				rewardParticipant(_playerTwo.player, reward);

				broadcastPacket(new ExOlympiadResult(
						new Object[]{0, _playerOne.name, 1, pointDiff, _playerOne.player, _playerTwo.player}), stadium);

				_playerOne.competitionDone(getType(), true);
				_playerTwo.competitionDone(getType(), false);
			}
			else if (_playerOne.player == null || !_playerOne.player.isOnline() ||
					playerOneHp == 0 && playerTwoHp != 0 ||
					_playerTwo.player.getOlyGivenDmg() > _playerOne.player.getOlyGivenDmg() && playerOneHp != 0 &&
							playerTwoHp != 0)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_WON_THE_GAME);
				sm.addString(_playerTwo.name);
				broadcastPacket(sm, stadium);

				_playerTwo.nobleInfo.increaseVictories();
				_playerOne.nobleInfo.increaseDefeats();

				addPointsToParticipant(_playerTwo, pointDiff);
				removePointsFromParticipant(_playerOne, pointDiff);

				// Save Fight Result
				saveResults(_playerOne, _playerTwo, 2, _startTime, _fightTime, getType());
				int[][] reward = getReward().clone();
				rewardParticipant(_playerTwo.player, reward);
				for (int i = 0; i < reward.length; i++)
				{
					reward[i][1] = reward[i][1] * 6 / 10;
				}
				rewardParticipant(_playerOne.player, reward);

				broadcastPacket(new ExOlympiadResult(
						new Object[]{1, _playerTwo.name, 1, pointDiff, _playerOne.player, _playerTwo.player}), stadium);

				_playerOne.competitionDone(getType(), false);
				_playerTwo.competitionDone(getType(), true);
			}
			else
			{
				// Save Fight Result
				saveResults(_playerOne, _playerTwo, 0, _startTime, _fightTime, getType());

				sm = SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_ENDED_IN_A_TIE);
				broadcastPacket(sm, stadium);

				removePointsFromParticipant(_playerOne,
						Math.min(playerOnePoints / getDivider(), Config.ALT_OLY_MAX_POINTS));
				removePointsFromParticipant(_playerTwo,
						Math.min(playerTwoPoints / getDivider(), Config.ALT_OLY_MAX_POINTS));

				broadcastPacket(
						new ExOlympiadResult(new Object[]{-1, "", 1, pointDiff, _playerOne.player, _playerTwo.player}),
						stadium);

				_playerOne.competitionDone(getType(), false);
				_playerTwo.competitionDone(getType(), false);
			}

			_playerOne.nobleInfo.increaseMatches();
			_playerTwo.nobleInfo.increaseMatches();
			if (getType() == CompetitionType.CLASSED)
			{
				_playerOne.nobleInfo.increaseClassedMatches();
				_playerTwo.nobleInfo.increaseClassedMatches();
			}
			else
			{
				_playerOne.nobleInfo.increaseNonClassedMatches();
				_playerTwo.nobleInfo.increaseNonClassedMatches();
			}

			if (Config.ALT_OLY_LOG_FIGHTS)
			{
				logFight(_playerOne.player.getObjectId(), _playerTwo.player.getObjectId(), playerOneHp, playerTwoHp,
						_playerOne.player.getOlyGivenDmg(), _playerTwo.player.getOlyGivenDmg(), pointDiff,
						getType().toString());
			}
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Exception on validateWinner(): " + e.getMessage(), e);
		}
	}

	/**
	 * @param char1Id
	 * @param char2Id
	 * @param points
	 * @param competitionType
	 */
	private void logFight(int char1Id, int char2Id, double char1Hp, double char2Hp, int char1Dmg, int char2Dmg, int points, String competitionType)
	{
		Connection con = null;
		try
		{
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
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	@Override
	protected final void addDamage(L2PcInstance player, int damage)
	{
	}

	@Override
	public final String[] getPlayerNames()
	{
		return new String[]{_playerOne.name, _playerTwo.name};
	}

	@Override
	public final boolean checkDefaulted()
	{
		SystemMessage reason;
		_playerOne.updatePlayer();
		_playerTwo.updatePlayer();

		reason = checkDefaulted(_playerOne);
		if (reason != null)
		{
			_playerOne.defaulted = true;
			if (_playerTwo.player != null)
			{
				_playerTwo.player.sendPacket(reason);
			}
		}

		reason = checkDefaulted(_playerTwo);
		if (reason != null)
		{
			_playerTwo.defaulted = true;
			if (_playerOne.player != null)
			{
				_playerOne.player.sendPacket(reason);
			}
		}

		return _playerOne.defaulted || _playerTwo.defaulted;
	}

	@Override
	public final void resetDamage()
	{
	}

	protected static void saveResults(OlympiadParticipant one, OlympiadParticipant two, int _winner, long _startTime, long _fightTime, CompetitionType type)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(
					"INSERT INTO olympiad_fights (charOneId, charTwoId, charOneClass, charTwoClass, winner, start, time, classed) values(?,?,?,?,?,?,?,?)");
			statement.setInt(1, one.objectId);
			statement.setInt(2, two.objectId);
			statement.setInt(3, one.baseClass);
			statement.setInt(4, two.baseClass);
			statement.setInt(5, _winner);
			statement.setLong(6, _startTime);
			statement.setLong(7, _fightTime);
			statement.setInt(8, type == CompetitionType.CLASSED ? 1 : 0);
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			if (Log.isLoggable(Level.SEVERE))
			{
				Log.log(Level.SEVERE, "SQL exception while saving olympiad fight.", e);
			}
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
}
