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

package l2server.gameserver.instancemanager;

import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.LoginServerThread;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.communitybbs.Manager.CustomCommunityBoard;
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.datatables.PlayerClassTable;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.L2GameClient;
import l2server.gameserver.network.L2GameClient.GameClientState;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.network.serverpackets.SetupGauge;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.taskmanager.AttackStanceTaskManager;
import l2server.gameserver.templates.skills.L2SkillTargetType;
import l2server.gameserver.templates.skills.L2SkillType;
import l2server.gameserver.util.Util;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author LasTravel
 */

public class CustomOfflineBuffersManager
{
	private static final String TRUNCATE = "DELETE FROM offline_buffers";
	private static final String STORE_BUFFERS =
			"INSERT INTO offline_buffers (`charId`, `description`, `buffs`, `coinId`) VALUES (?, ?, ?, ?)";
	private static final String RESTORE_BUFFERS = "SELECT * FROM offline_buffers";
	private static final Map<Integer, BufferTable> _customBufferTable = new HashMap<>();

	private class BufferTable
	{
		private int _playerId;
		private int _coinId;
		private String _coinName;
		private String _description;
		private Map<Integer, Long> _buffs;

		private BufferTable(Integer bufferId)
		{
			_playerId = bufferId;
			_coinName = "Adena";
			_buffs = new HashMap<>();
		}

		private void setCointId(int i)
		{
			_coinId = i;
			_coinName = i != 0 ? ItemTable.getInstance().getTemplate(_coinId).getName() : null;
		}

		private int getCointId()
		{
			return _coinId;
		}

		private void addBuff(int skillId, long skillPrice)
		{
			_buffs.put(skillId, skillPrice);
		}

		private Map<Integer, Long> getBuffs()
		{
			return _buffs;
		}

		private String getDescription()
		{
			return _description;
		}

		private void setDescription(String desc)
		{
			_description = desc;
		}

		private int getPlayerId()
		{
			return _playerId;
		}

		private void addBuffs(Map<Integer, Long> playerBuffs)
		{
			_buffs = playerBuffs;
		}

		private String getCoinName()
		{
			return _coinName;
		}

		private L2PcInstance getBuffer(boolean deleteIfIllegal)
		{
			L2PcInstance buffer = L2World.getInstance().getPlayer(_playerId);
			if (buffer != null && deleteIfIllegal && !buffer.getIsOfflineBuffer())
			{
				synchronized (_customBufferTable)
				{
					_customBufferTable.remove(_playerId);
					return null;
				}
			}
			return buffer;
		}
	}

	public String getOfflineBuffersPage(int pageToShow)
	{
		StringBuilder sb = new StringBuilder();
		synchronized (_customBufferTable)
		{
			int maxPlayersPerPage = 20;
			int playersSize = _customBufferTable.size();
			int maxPages = playersSize / maxPlayersPerPage;
			if (playersSize > maxPlayersPerPage * maxPages)
			{
				maxPages++;
			}
			if (pageToShow > maxPages)
			{
				pageToShow = maxPages;
			}
			int pageStart = maxPlayersPerPage * pageToShow;
			int pageEnd = playersSize;
			if (pageEnd - pageStart > maxPlayersPerPage)
			{
				pageEnd = pageStart + maxPlayersPerPage;
			}

			if (maxPages > 1)
			{
				sb.append(CustomCommunityBoard.getInstance()
						.createPages(pageToShow, maxPages, "_bbscustom;worldBuffers;", ""));
			}

			sb.append(
					"<table width=750 bgcolor=999999><tr><td FIXWIDTH=50>Name</td><td FIXWIDTH=60>Class</td><td FIXWIDTH=35 align=center>Mana</td><td FIXWIDTH=80 align=center>Description</td><td FIXWIDTH=30>Buffs Count</td><td FIXWIDTH=35 align=center>Coin</td></tr></table>");

			for (Entry<Integer, BufferTable> i : _customBufferTable.entrySet())
			{
				BufferTable buffTable = i.getValue();
				if (buffTable == null)
				{
					continue;
				}

				L2PcInstance buffer = buffTable.getBuffer(false);
				if (buffer == null || !buffer.getIsOfflineBuffer())
				{
					continue;
				}
				sb.append("<table width=750><tr><td FIXWIDTH=50>" + buffer.getName() + "</td><td FIXWIDTH=60>" +
						PlayerClassTable.getInstance().getClassNameById(buffer.getClassId()) + "(" + buffer.getLevel() +
						")</td><td FIXWIDTH=35 align=center>" +
						Math.round(buffer.getCurrentMp() * 100 / buffer.getMaxMp()) +
						"%</td><td FIXWIDTH=80 align=center>" +
						(buffTable.getDescription() == null ? "" : buffTable.getDescription()) +
						"</td><td FIXWIDTH=30 align=center><button value=\"" + buffTable.getBuffs().size() +
						"\" width=50 height=16 action=\"bypass _bbscustom;action;worldBuff;bufferInfo;" +
						buffTable.getPlayerId() +
						"\" back=\"L2UI_CT1.Button_DF_Calculator_Over\" fore=\"L2UI_CT1.Button_DF_Calculator\"></button></td><td FIXWIDTH=35 align=center>" +
						buffTable.getCoinName() + "</td></tr></table>");
				sb.append("<img src=\"L2UI.Squaregray\" width=740 height=1>");
			}
		}
		return sb.toString();
	}

	public void getSpecificBufferInfo(L2PcInstance player, Integer playerId)
	{
		if (player == null)
		{
			return;
		}

		StringBuilder sb = new StringBuilder();
		synchronized (_customBufferTable)
		{
			BufferTable buffTable = _customBufferTable.get(playerId);
			if (buffTable == null)
			{
				return;
			}

			L2PcInstance buffer = buffTable.getBuffer(true);
			if (buffer == null)
			{
				return;
			}

			sb.append("<html><title>" + buffer.getName() + "</title><body>");
			sb.append("<center>Remaining Mana: " + Math.round(buffer.getCurrentMp()) + "/" +
					Math.round(buffer.getMaxMp()) + "</center><br>");
			sb.append(
					"<table width=300 bgcolor=999999 border=0><tr><td FIXWIDTH=32></td><td FIXWIDTH=130>Buff Name</td><td FIXWIDTH=35>Level</td><td FIXWIDTH=80 align=center>Price</td></tr></table>");

			int loc = 0;
			for (Entry<Integer, Long> i : buffTable.getBuffs().entrySet())
			{
				if (i == null)
				{
					continue;
				}

				int skillId = i.getKey();
				L2Skill buffInfo = buffer.getKnownSkill(skillId);
				if (buffInfo != null)
				{
					sb.append("<table width=300 " + (loc % 2 == 0 ? "bgcolor=131210" : "") +
							" border=0><tr><td FIXWIDTH=32><img src=\"" +
							getCorrectSkillIcon(buffInfo.getName(), buffInfo.getId()) +
							"\" width=32 height=32></td><td FIXWIDTH=130><a action=\"bypass _bbscustom;action;worldBuff;getBuff;" +
							playerId + ";" + skillId + "\">" + buffInfo.getName() +
							"</a></td><td FIXWIDTH=35 align=center>" + buffInfo.getLevelHash() +
							"</td><td FIXWIDTH=80 align=center>" + i.getValue() + "</td></tr></table>");
				}
				loc++;
			}
			sb.append("<br><br></body></html>");
		}
		player.sendPacket(new NpcHtmlMessage(0, sb.toString()));
	}

	public void getBuffFromBuffer(final L2PcInstance player, Integer playerId, Integer skillId)
	{
		if (player == null)
		{
			return;
		}

		if (!player.getClient().getFloodProtectors().getTransaction().tryPerformAction("buy"))
		{
			return;
		}

		if (player.getPrivateStoreType() != 0 || player.isInCrystallize())
		{
			player.sendPacket(
					SystemMessage.getSystemMessage(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));
			return;
		}

		synchronized (_customBufferTable)
		{
			BufferTable buffTable = _customBufferTable.get(playerId);
			if (buffTable == null)
			{
				return;
			}

			L2PcInstance buffer = buffTable.getBuffer(true);
			if (buffer == null)
			{
				return;
			}

			if (buffer.getClient() == null || !buffer.getClient().isDetached() || !buffer.getIsOfflineBuffer())
			{
				_customBufferTable.remove(playerId);
				return;
			}

			//Lil check
			final L2Skill skill = buffer.getKnownSkill(skillId);
			if (skill == null)
			{
				return;
			}

			if (!canGetBuffs(player))
			{
				return;
			}

			if (!buffer.isCastingNow())
			{
				if (buffer.getCurrentMp() >= buffer.getStat().getMpConsume(skill))
				{
					if (skill.getItemConsume() > 0 && skill.getItemConsumeId() > 0)
					{
						if (buffer.getInventory().getInventoryItemCount(skill.getItemConsumeId(), 0) <
								skill.getItemConsume())
						{
							player.sendMessage("World Buffers: " + buffer.getName() +
									" doesn't have the required items to cast: " + skill.getName());
							return;
						}
					}

					if (buffer.isSkillDisabled(skill))
					{
						player.sendMessage("World Buffers: " + skill.getName() + " is currently under reuse!");
						return;
					}

					long price = buffTable.getBuffs().get(skill.getId());
					if (player.destroyItemByItemId("CustomOfflineBuffersManager", buffTable.getCointId(), price, buffer,
							true))
					{
						buffer.addItem("CustomOfflineBuffersManager", buffTable.getCointId(), price, player, false);

						if (player.isInsideRadius(buffer, skill.getCastRange(), true, true))
						{
							buffer.setTarget(player);
						}
						else
						{
							buffer.setTarget(buffer);
							player.sendPacket(new SetupGauge(SetupGauge.GREEN_MINI, skill.getHitTime()));
						}
						buffer.doCast(skill);

						player.sendMessage(
								"World Buffers: " + buffer.getName() + " is casting: " + skill.getName() + " on you!");
						ThreadPoolManager.getInstance().scheduleGeneral(() ->
						{
							if (canGetBuffs(player))
							{
								skill.getEffects(player, player);
							}
						}, Formulas.calcAtkSpd(player, skill, skill.getHitTime()));

						getSpecificBufferInfo(player, buffer.getObjectId());
					}
				}
				else
				{
					player.sendMessage("World Buffers: " + buffer.getName() + " doesn't have enoff mana!");
				}
			}
			else
			{
				player.sendMessage("World Buffers: " + buffer.getName() + " is already casting!");
			}
		}
	}

	private boolean canGetBuffs(L2PcInstance player)
	{
		if (player == null)
		{
			return false;
		}
		if (player.isInCombat() || player.getPvpFlag() > 0 || player.getInstanceId() != 0 || player.isInDuel() ||
				player.isFakeDeath() || player.isOutOfControl() || player.isInOlympiadMode() ||
				AttackStanceTaskManager.getInstance().getAttackStanceTask(player))
		{
			player.sendMessage("World Buffers: You can't get buffs right now!");
			return false;
		}
		return true;
	}

	public void offlineBuffPannel(L2PcInstance player)
	{
		if (!Config.OFFLINE_BUFFERS_ENABLE)
		{
			return;
		}
		BufferTable buffTable = _customBufferTable.get(player.getObjectId());
		List<L2Skill> buffSkills = new ArrayList<>();
		for (L2Skill sk : player.getAllSkills())
		{
			if (sk == null)
			{
				continue;
			}
			if (!sk.isPassive() && sk.getTransformId() == 0 && sk.getSkillType() == L2SkillType.BUFF &&
					(sk.getTargetType() == L2SkillTargetType.TARGET_ONE ||
							sk.getTargetType() == L2SkillTargetType.TARGET_PARTY))
			{
				buffSkills.add(sk);
			}
		}

		//Subclass/Dualclass expoilt for the buffs that are already stored on the table...
		if (buffTable != null)
		{
			if (!buffTable.getBuffs().isEmpty())
			{
				List<Integer> toDelete = new ArrayList<>();
				for (Entry<Integer, Long> buff : buffTable.getBuffs().entrySet())
				{
					if (buff == null)
					{
						continue;
					}
					if (player.getSkillLevelHash(buff.getKey()) == -1)
					{
						toDelete.add(buff.getKey());
					}
				}

				if (!toDelete.isEmpty())
				{
					for (int skillId : toDelete)
					{
						buffTable.getBuffs().remove(skillId);
					}
				}
			}
		}

		StringBuilder sb = new StringBuilder();
		sb.append("<html><title>Available buff set</title><body>");

		sb.append("<center><table width=300 bgcolor=131210><tr><td align=center>Description:</td></tr>");
		if (buffTable != null && buffTable.getDescription() != null)
		{
			sb.append("<tr><td align=center><font color=LEVEL>" + buffTable.getDescription() + "</font></td></tr>");
			sb.append(
					"<tr><td align=center><button action=\"bypass _bbscustom;action;worldBuff;delDesc\" value=Delete Description! width=120 height=20 back=L2UI_ct1.button_df fore=L2UI_ct1.button_df></button></td></tr>");
		}
		else
		{
			sb.append("<tr><td align=center><edit var=\"addDesc\" width=150 type=char length=16></td></tr>");
			sb.append(
					"<tr><td align=center><button action=\"bypass _bbscustom;action;worldBuff;addDesc; $addDesc\" value=\"Add Description!\" width=120 height=20 back=L2UI_ct1.button_df fore=L2UI_ct1.button_df></button></td></tr>");
		}
		sb.append("</table></center><br>");

		sb.append("<center><table width=300 bgcolor=131210><tr><td align=center>Coin to use:</td></tr>");
		if (buffTable != null && buffTable.getCointId() != 0)
		{
			sb.append("<tr><td align=center><font color=LEVEL>" + buffTable.getCoinName() + "</font></td></tr>");
			sb.append(
					"<tr><td align=center><button action=\"bypass _bbscustom;action;worldBuff;delCoin\" value=\"Change this coin!\" width=120 height=20 back=L2UI_ct1.button_df fore=L2UI_ct1.button_df></button></td></tr>");
		}
		else
		{
			sb.append(
					"<tr><td align=center><combobox width=150 height=17 var=\"coinType\" list=Adena;SilverShilen;BlueEva;GoldEinhasad></td></tr>");
			sb.append(
					"<tr><td align=center><button action=\"bypass _bbscustom;action;worldBuff;addCoin; $coinType\" value=\"Use this coin!\" width=120 height=20 back=L2UI_ct1.button_df fore=L2UI_ct1.button_df></button></td></tr>");
		}
		sb.append("</table></center><br>");

		int loc = 0;
		for (L2Skill sk : buffSkills)
		{
			if (sk == null)
			{
				continue;
			}

			sb.append("<table border=0 " + (loc % 2 == 0 ? "bgcolor=131210" : "") + ">");
			sb.append("<tr><td><img src=\"" + getCorrectSkillIcon(sk.getName(), sk.getId()) +
					"\" width=32 height=32></td><td  FIXWIDTH=150>" + sk.getName() + "</td></tr>");

			if (buffTable != null && buffTable.getBuffs().containsKey(sk.getId()))
			{
				sb.append("<tr><td>Price:</td><td>" + buffTable.getBuffs().get(sk.getId()) +
						"</td><td><button action=\"bypass _bbscustom;action;worldBuff;delBuff;" + sk.getId() +
						"\" value=Remove! width=60 height=20 back=L2UI_ct1.button_df fore=L2UI_ct1.button_df></td></tr>");
			}
			else
			{
				sb.append("<tr><td>Price:</td><td><edit var=\"addBuff" + sk.getId() +
						"\" width=100 type=number length=14></td><td><button action=\"bypass _bbscustom;action;worldBuff;addBuff;" +
						sk.getId() + "; $addBuff" + sk.getId() +
						"\" value=Add! width=60 height=20 back=L2UI_ct1.button_df fore=L2UI_ct1.button_df></td></tr>");
			}

			sb.append("</table>");
			sb.append("<img src=l2ui.squaregray width=300 height=1>");

			loc++;
		}

		sb.append("</body></html>");
		player.sendPacket(new NpcHtmlMessage(0, sb.toString()));
	}

	public void addBuffToBuffer(L2PcInstance player, int skillId, Long price)
	{
		if (player == null)
		{
			return;
		}

		synchronized (_customBufferTable)
		{
			BufferTable buffTable = _customBufferTable.get(player.getObjectId());
			if (buffTable == null)
			{
				buffTable = new BufferTable(player.getObjectId());
				_customBufferTable.put(player.getObjectId(), buffTable);
			}

			if (player.getSkillLevelHash(skillId) != -1)
			{
				buffTable.addBuff(skillId, price);
				player.sendMessage("World Buffers: Buff added!");

				offlineBuffPannel(player);
			}
			else
			{
				Log.severe("CustomOfflineBuffersManager: The player: " + player.getName() +
						" is trying to add an invalid skill!");
			}
		}
	}

	public void delBuffToBuffer(L2PcInstance player, int skillId)
	{
		if (player == null)
		{
			return;
		}

		synchronized (_customBufferTable)
		{
			BufferTable buffTable = _customBufferTable.get(player.getObjectId());
			if (buffTable != null)
			{
				buffTable.getBuffs().remove(skillId);

				if (buffTable.getBuffs().size() == 0)
				{
					_customBufferTable.remove(player.getObjectId());
				}

				player.sendMessage("World Buffers: Buff removed!");

				offlineBuffPannel(player);
			}
		}
	}

	public void addDescription(L2PcInstance player, String description)
	{
		if (player == null)
		{
			return;
		}

		synchronized (_customBufferTable)
		{
			BufferTable buffTable = _customBufferTable.get(player.getObjectId());
			if (buffTable == null)
			{
				buffTable = new BufferTable(player.getObjectId());
				_customBufferTable.put(player.getObjectId(), buffTable);
			}
			buffTable.setDescription(description);
			player.sendMessage("World Buffers: Description updated!");

			offlineBuffPannel(player);
		}
	}

	public void changeCurrencyId(L2PcInstance player, String coin)
	{
		if (player == null)
		{
			return;
		}

		synchronized (_customBufferTable)
		{
			BufferTable buffTable = _customBufferTable.get(player.getObjectId());
			if (buffTable == null)
			{
				buffTable = new BufferTable(player.getObjectId());
				_customBufferTable.put(player.getObjectId(), buffTable);
			}

			buffTable.setCointId(coin != null ? TenkaiAuctionManager.getInstance().getCurrencyId(coin) : 0);
			player.sendMessage("World Buffers: Currency updated!");

			offlineBuffPannel(player);
		}
	}

	public void removeDescription(L2PcInstance player, String description)
	{
		if (player == null)
		{
			return;
		}

		synchronized (_customBufferTable)
		{
			BufferTable buffTable = _customBufferTable.get(player.getObjectId());
			if (buffTable != null)
			{
				buffTable.setDescription("");
				player.sendMessage("World Buffers: Description removed!");
			}
		}
	}

	public boolean setUpOfflineBuffer(L2PcInstance player)
	{
		if (player == null)
		{
			return false;
		}

		if (player.getIsOfflineBuffer())
		{
			return true;
		}

		synchronized (_customBufferTable)
		{
			BufferTable buffTable = _customBufferTable.get(player.getObjectId());
			if (buffTable != null)
			{
				if (buffTable.getBuffs().size() > 0 && buffTable.getCointId() != 0)
				{
					if (player.isSitting())
					{
						player.standUp();
					}
					player.setIsOfflineBuffer(true);
					Util.logToFile(
							"The player: " + player.getName() + " set one offline(" + buffTable.getDescription() +
									") buffer with " +
									ItemTable.getInstance().getTemplate(buffTable.getCointId()).getName() +
									" as a price!", "OfflineBuffers", true);
					return true;
				}
			}
		}
		return false;
	}

	private void restoreOfflineBuffers()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement stm = con.prepareStatement(RESTORE_BUFFERS);
			ResultSet rs = stm.executeQuery();
			while (rs.next())
			{
				int charId = rs.getInt("charId");
				String desc = rs.getString("description");
				String buffs = rs.getString("buffs");
				int coinId = rs.getInt("coinId");

				Map<Integer, Long> playerBuffs = new HashMap<>();
				for (String i : buffs.split(";"))
				{
					int buffId = Integer.valueOf(i.split(",")[0]);
					long price = Long.valueOf(i.split(",")[1]);

					playerBuffs.put(buffId, price);
				}

				if (playerBuffs.isEmpty())
				{
					continue;
				}

				L2PcInstance player = null;
				try
				{
					L2GameClient client = new L2GameClient(null);
					client.setDetached(true);
					player = L2PcInstance.load(charId);
					client.setActiveChar(player);
					player.setOnlineStatus(true, false);
					client.setAccountName(player.getAccountNamePlayer());
					client.setState(GameClientState.IN_GAME);
					player.setClient(client);
					player.spawnMe(player.getX(), player.getY(), player.getZ());

					LoginServerThread.getInstance().addGameServerLogin(player.getAccountName(), client);

					player.setOnlineStatus(true, true);
					player.restoreEffects();
					player.broadcastUserInfo();
					player.setIsInvul(true);

					BufferTable table = new BufferTable(player.getObjectId());
					table.setDescription(desc);
					table.addBuffs(playerBuffs);
					table.setCointId(coinId);
					player.setIsOfflineBuffer(true);

					_customBufferTable.put(charId, table);
				}
				catch (Exception e)
				{
					e.printStackTrace();
					if (player != null)
					{
						player.deleteMe();
					}
				}
			}
			rs.close();
			stm.close();

			stm = con.prepareStatement(TRUNCATE);
			stm.execute();
			stm.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		Log.info("CustomOfflineBuffers: restored " + _customBufferTable.size() + " offline buffers!");
	}

	public void storeOfflineBuffers()
	{
		if (!Config.OFFLINE_BUFFERS_RESTORE)
		{
			return;
		}

		if (_customBufferTable.isEmpty())
		{
			return;
		}

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement sq = con.prepareStatement(STORE_BUFFERS);

			for (Entry<Integer, BufferTable> i : _customBufferTable.entrySet())
			{
				BufferTable table = i.getValue();
				if (table.getBuffs().isEmpty())
				{
					continue;
				}

				sq.setInt(1, i.getKey());
				sq.setString(2, table.getDescription());

				//Buff part syntax, buffId,price;
				String buffs = "";
				for (Entry<Integer, Long> b : table.getBuffs().entrySet())
				{
					buffs += b.getKey() + "," + b.getValue() + ";";
				}

				if (!buffs.isEmpty())
				{
					sq.setString(3, buffs);
				}

				sq.setInt(4, table.getCointId());
				sq.executeUpdate();
				sq.clearParameters();
			}
			sq.close();

			Log.info("CustomOfflineBuffers: Buffers stored!");
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

	private String getCorrectSkillIcon(String skillName, int skillId)
	{
		String iconImage = "icon.skill" + skillId;
		if (skillName.startsWith("Dance of") || skillName.startsWith("Song of") || skillId < 1000)
		{
			iconImage = "icon.skill0" + skillId;
		}
		if (skillId == 11566)
		{
			iconImage = "icon.skill11567";
		}
		if (skillId == 11567)
		{
			iconImage = "icon.skill11824";
		}
		return iconImage;
	}

	private CustomOfflineBuffersManager()
	{
		if (Config.OFFLINE_BUFFERS_RESTORE)
		{
			restoreOfflineBuffers();
		}
	}

	public static CustomOfflineBuffersManager getInstance()
	{
		return SingletonHolder._instance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final CustomOfflineBuffersManager _instance = new CustomOfflineBuffersManager();
	}
}
