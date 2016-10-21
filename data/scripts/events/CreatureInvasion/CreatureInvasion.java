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

package events.CreatureInvasion;

import l2server.Config;
import l2server.gameserver.Announcements;
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.instancemanager.GrandBossManager;
import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestTimer;
import l2server.gameserver.network.serverpackets.NpcSay;
import l2server.gameserver.templates.item.L2Weapon;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LasTravel
 */
public class CreatureInvasion extends Quest
{
	private static final boolean _debug = false;
	private static final String _qn = "CreatureInvasion";

	private static final int _invasionDuration = 15; //Minutes
	private static final int[] _weakCreatures = {13031, 13120};
	private static final int[] _hardCreatures = {13123, 13034};
	private static final int[] _strangeCreatures = {13035, 13124};
	private static final int[] _bowSkillIds = {3260, 3262};
	private static final int _bowId = 9141;
	private static final int _bossId = 26123;
	private static final int[] _allCreatureIds = {
			_weakCreatures[0],
			_weakCreatures[1],
			_hardCreatures[0],
			_hardCreatures[1],
			_strangeCreatures[0],
			_strangeCreatures[1]
	};
	private static Map<Integer, AttackInfo> _attackInfo = new HashMap<Integer, AttackInfo>();
	private static Map<String, String> _rewardedIps = new HashMap<String, String>();
	private static Map<String, List<DropChances>> _dropInfo = new HashMap<String, List<DropChances>>();
	private static ArrayList<L2Npc> _allCreatures = new ArrayList<L2Npc>();
	private static boolean _isEventStarted;
	private static BossAttackInfo _bossAttackInfo;

	public CreatureInvasion(int id, String name, String descr)
	{
		super(id, name, descr);

		addAttackId(_bossId);
		addKillId(_bossId);

		for (int i : _allCreatureIds)
		{
			addAttackId(i);
			addKillId(i);
		}

		loadCreatureDrops();
	}

	private void loadCreatureDrops()
	{
		_dropInfo.clear();
		File file = new File(
				Config.DATAPACK_ROOT + "/" + Config.DATA_FOLDER + "scripts/events/CreatureInvasion/creatureDrops.xml");
		if (!file.exists())
		{
			return;
		}

		XmlDocument doc = new XmlDocument(file);
		for (XmlNode n : doc.getChildren())
		{
			if (n.getName().equalsIgnoreCase("list"))
			{
				for (XmlNode d : n.getChildren())
				{
					if (d.getName().equalsIgnoreCase("drop"))
					{
						String category = d.getString("category");
						List<DropChances> dropChances = new ArrayList<DropChances>();

						for (XmlNode b : d.getChildren())
						{
							int itemId = b.getInt("itemId");
							long min = b.getLong("min");
							long max = b.getLong("max");
							int chance = b.getInt("chance");
							dropChances.add(new DropChances(itemId, min, max, chance));
						}
						_dropInfo.put(category, dropChances);
					}
				}
			}
		}
		Log.info(getName() + ": Loaded " + _dropInfo.size() + " drop categories!");
	}

	private class DropChances
	{
		private int _itemId;
		private long _minAmount;
		private long _maxAmount;
		private int _chance;

		private DropChances(int itemId, long minAmount, long maxAmount, int chance)
		{
			_itemId = itemId;
			_minAmount = minAmount;
			_maxAmount = maxAmount;
			_chance = chance;
		}

		private int getItemId()
		{
			return _itemId;
		}

		private long getMinAmount()
		{
			return _minAmount;
		}

		private long getMaxAmount()
		{
			return _maxAmount;
		}

		private int getChance()
		{
			return _chance;
		}
	}

	private class BossAttackInfo
	{
		private L2Npc _boss;
		private Map<Integer, Long> _registredDamages;

		private BossAttackInfo(L2Npc boss)
		{
			_boss = boss;
			_registredDamages = new HashMap<Integer, Long>();
		}

		private void deleteBoss()
		{
			if (_boss != null)
			{
				_boss.deleteMe();
			}
		}

		private void addDamage(int playerId, long damage)
		{
			synchronized (_registredDamages)
			{
				if (!_registredDamages.containsKey(playerId))
				{
					_registredDamages.put(playerId, damage);
				}
				else
				{
					_registredDamages.put(playerId, _registredDamages.get(playerId) + damage);
				}
			}
		}

		private void giveRewards()
		{
			synchronized (_registredDamages)
			{
				for (L2PcInstance player : L2World.getInstance().getAllPlayersArray())
				{
					if (player == null || player.getInstanceId() != 0 || player.isInStoreMode() ||
							!player.isInsideRadius(_boss, 3000, false, false) ||
							_registredDamages.get(player.getObjectId()) == null ||
							_registredDamages.get(player.getObjectId()) < 1000)
					{
						continue;
					}

					if (_rewardedIps.containsKey(player.getExternalIP()) &&
							_rewardedIps.get(player.getExternalIP()).equalsIgnoreCase(player.getInternalIP()))
					{
						continue;
					}

					_rewardedIps.put(player.getExternalIP(), player.getInternalIP());

					for (DropChances i : _dropInfo.get("boss"))
					{
						if (Rnd.get(100) < i.getChance())
						{
							long amount = Rnd.get(i.getMinAmount(), i.getMaxAmount());
							player.addItem(getName(), i.getItemId(), amount, player, true);
							_boss.broadcastChat(player.getName() + " received " + amount + " " +
									ItemTable.getInstance().getTemplate(i.getItemId()).getName() + "!", 0);
						}
					}
				}
				Log.info(getName() + ": Rewarded: " + _rewardedIps.size() + " players!");
			}
		}
	}

	private class AttackInfo
	{
		private Long _attackedTime;
		private int _playerId;
		private String _externalIP;
		private String _internalIP;

		private AttackInfo(int playerId, String externalIP, String internalIP)
		{
			_playerId = playerId;
			_externalIP = externalIP;
			_internalIP = internalIP;
			setAttackedTime();
		}

		private long getAttackedTime()
		{
			return _attackedTime;
		}

		private void setAttackedTime()
		{
			_attackedTime = System.currentTimeMillis();
		}

		private int getPlayerId()
		{
			return _playerId;
		}

		private String getExternalIP()
		{
			return _externalIP;
		}

		private String getInternalIP()
		{
			return _internalIP;
		}

		private void updateInfo(int playerId, String externalIP, String internalIP)
		{
			_playerId = playerId;
			_externalIP = externalIP;
			_internalIP = internalIP;
			setAttackedTime();
		}
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (_debug)
		{
			Log.warning(getName() + ": onAdvEvent: " + event);
		}

		if (event.equalsIgnoreCase("start_invasion"))
		{
			if (!_isEventStarted)
			{
				_isEventStarted = true;

				Announcements.getInstance().announceToAll("The Creature Invasion has started!");
				for (L2PcInstance pl : L2World.getInstance().getAllPlayersArray())
				{
					if (pl == null || pl.getInstanceId() != 0 || pl.getEvent() != null || pl.getIsInsideGMEvent() ||
							pl.inObserverMode() || pl.isInOlympiadMode() || pl.isInStoreMode() ||
							GrandBossManager.getInstance().getZone(pl) != null)
					{
						continue;
					}

					for (int i = 0; i <= Rnd.get(1, 2); i++)
					{
						spawnCreature(pl.getX(), pl.getY(), pl.getZ());
					}
				}
				startQuestTimer("spawn_boss", _invasionDuration * 60000, null, null);
			}
			else
			{
				notifyEvent("end_event", null, null);
			}
		}
		else if (event.equalsIgnoreCase("spawn_boss"))
		{
			L2Npc boss = addSpawn(_bossId, 82689, 148600, -3473, 65137, false, 0);
			_bossAttackInfo = new BossAttackInfo(boss);

			Announcements.getInstance().announceToAll("The Golden Pig has appeared on Giran!");

			startQuestTimer("end_event", _invasionDuration * 60000, null, null);
		}
		else if (event.equalsIgnoreCase("end_event"))
		{
			if (_isEventStarted)
			{
				QuestTimer timer = getQuestTimer("spawn_boss", null, null);
				if (timer != null)
				{
					timer.cancel();
				}

				if (_bossAttackInfo != null)
				{
					_bossAttackInfo.deleteBoss();
				}

				synchronized (_allCreatures)
				{
					for (L2Npc creature : _allCreatures)
					{
						if (creature == null)
						{
							continue;
						}
						creature.deleteMe();
					}
				}

				_isEventStarted = false;
				_bossAttackInfo = null;
				_attackInfo.clear();
				_allCreatures.clear();
				_rewardedIps.clear();

				Announcements.getInstance().announceToAll("The Creature Invasion has been ended!");
			}
		}
		return "";
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet, L2Skill skill)
	{
		if (_debug)
		{
			Log.warning(getName() + ": onAttack: " + npc.getName());
		}

		if (player.getSummons() != null)
		{
			for (L2Summon s : player.getSummons())
			{
				s.unSummon(player);
			}
		}

		if (player.getPet() != null)
		{
			player.getPet().unSummon(player);
		}

		if (!_isEventStarted)
		{
			npc.deleteMe();
		}

		if (!isValidAttack(player, skill, npc))
		{
			if (!player.isGM() && player.isInsideZone(L2Character.ZONE_PEACE))
			{
				player.doDie(null);
			}

			return "";
		}

		if (Util.contains(_allCreatureIds, npc.getNpcId()))
		{
			synchronized (_attackInfo)
			{
				AttackInfo attackInfo = _attackInfo.get(npc.getObjectId());

				int sameIPs = 0;
				int underAttack = 0;

				for (Map.Entry<Integer, AttackInfo> _info : _attackInfo.entrySet())
				{
					if (_info == null)
					{
						continue;
					}

					AttackInfo i = _info.getValue();
					if (i == null)
					{
						continue;
					}

					if (System.currentTimeMillis() < i.getAttackedTime() + 7000)
					{
						if (i.getPlayerId() == player.getObjectId())
						{
							underAttack++;
						}

						if (i.getExternalIP().equalsIgnoreCase(player.getExternalIP()) &&
								i.getInternalIP().equalsIgnoreCase(player.getInternalIP()))
						{
							sameIPs++;
						}

						if (underAttack > 1 || sameIPs > 1)
						{
							player.doDie(npc);

							if (underAttack > 1)
							{
								player.sendPacket(new NpcSay(npc.getObjectId(), 2, npc.getTemplate().TemplateId,
										player.getName() + " you cant attack more than one mob at same time!"));
							}

							if (sameIPs > 1)
							{
								player.sendPacket(new NpcSay(npc.getObjectId(), 2, npc.getTemplate().TemplateId,
										player.getName() + " dualbox is not allowed here!"));
							}
							return "";
						}
					}
				}

				if (attackInfo == null)
				{
					attackInfo = new AttackInfo(player.getObjectId(), player.getExternalIP(), player.getInternalIP());
					_attackInfo.put(npc.getObjectId(), attackInfo);
				}
				else
				{
					//Already exists information for this NPC
					//Check if the attacker is the same as the stored
					if (attackInfo.getPlayerId() != player.getObjectId())
					{
						//The attacker is not same
						//If the last attacked stored info +10 seconds is bigger than the current time, this mob is currently attacked by someone
						if (attackInfo.getAttackedTime() + 7000 > System.currentTimeMillis())
						{
							player.doDie(null);
							player.sendPacket(new NpcSay(npc.getObjectId(), 2, npc.getTemplate().TemplateId,
									player.getName() + " don't attack mobs from other players!"));
							return "";
						}
						else
						{
							//Add new information, none is currently attacking this NPC
							attackInfo.updateInfo(player.getObjectId(), player.getExternalIP(), player.getInternalIP());
						}
					}
					else
					{
						//player id is the same, update the attack time
						attackInfo.setAttackedTime();
					}
				}
			}
		}
		else if (npc.getNpcId() == _bossId)
		{
			_bossAttackInfo.addDamage(player.getObjectId(), damage);
		}
		return super.onAttack(npc, player, damage, isPet);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (_debug)
		{
			Log.warning(getName() + ": onKill: " + npc.getName());
		}

		if (!_isEventStarted)
		{
			Log.warning(killer.getName() + ": is killing creatures out of the event...!");
			return "";
		}

		if (Util.contains(_allCreatureIds, npc.getNpcId()))
		{
			synchronized (_attackInfo)
			{
				AttackInfo info = _attackInfo.get(npc.getObjectId()); //Get the attack info
				if (info != null)
				{
					_attackInfo.remove(npc.getObjectId()); //Delete the stored info for this npc
				}

				spawnCreature(killer.getX(), killer.getY(), killer.getZ());

				//TODO Reward the player based on his playerLevel
				if (isValidAttack(killer, killer.getLastSkillCast(), npc))
				{
					String dropType = "newPlayer";
					if (killer.getOnlineTime() > 10 * 3600)
					{
						dropType = "oldPlayer";
					}

					int a = 0;
					for (DropChances i : _dropInfo.get(dropType))
					{
						int dropChance = i.getChance();
						long maxAmount = i.getMaxAmount();

						if (Util.contains(_strangeCreatures, npc.getNpcId()))
						{
							dropChance *= 1.2;
							maxAmount *= 1.2;
						}

						if (Rnd.get(100) < dropChance)
						{
							if (a == 3)
							{
								break;
							}
							killer.addItem(getName(), i.getItemId(), Rnd.get(i.getMinAmount(), maxAmount), killer,
									true);
						}
					}
				}
			}
		}
		else if (npc.getNpcId() == _bossId)
		{
			_bossAttackInfo.giveRewards();

			QuestTimer q = getQuestTimer("end_event", null, null);
			if (q != null)
			{
				q.cancel();
			}

			notifyEvent("end_event", null, null);
		}
		return super.onKill(npc, killer, isPet);
	}

	private void spawnCreature(int x, int y, int z)
	{
		if (_bossAttackInfo != null)
		{
			return;
		}

		L2Npc creature = addSpawn(getCreatureId(), x, y, z + 5, 0, true, 0);
		synchronized (_allCreatures)
		{
			_allCreatures.add(creature);
		}
	}

	private boolean isValidAttack(L2PcInstance player, L2Skill skill, L2Npc npc)
	{
		if (player == null)
		{
			return false;
		}

		L2Weapon playerWeapon = player.getActiveWeaponItem();
		if (playerWeapon == null || playerWeapon.getItemId() != _bowId)
		{
			player.sendPacket(new NpcSay(npc.getObjectId(), 2, npc.getTemplate().TemplateId,
					player.getName() + " You should use the Redemption Bow!"));
			return false;
		}

		if (skill == null || !Util.contains(_bowSkillIds, skill.getId()))
		{
			if (skill != null && skill.hasEffects())
			{
				L2Abnormal abn = npc.getFirstEffect(skill.getId());
				if (abn != null)
				{
					abn.exit();
				}
			}

			player.sendPacket(new NpcSay(npc.getObjectId(), 2, npc.getTemplate().TemplateId,
					player.getName() + " You should use the Redemption Bow Skills!"));
			return false;
		}
		return true;
	}

	private int getCreatureId()
	{
		int rnd = Rnd.get(1, 100);
		if (rnd > 10)
		{
			return _weakCreatures[Rnd.get(_weakCreatures.length)];
		}
		else if (rnd > 3)
		{
			return _hardCreatures[Rnd.get(_hardCreatures.length)];
		}
		else
		{
			return _strangeCreatures[Rnd.get(_strangeCreatures.length)];
		}
	}

	@Override
	public int getOnKillDelay(int npcId)
	{
		return 0;
	}

	public static void main(String[] args)
	{
		new CreatureInvasion(-1, _qn, "events");
	}
}
