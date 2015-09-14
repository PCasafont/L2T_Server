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
package ai.individual.Apherus;

import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.DoorTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.datatables.MapRegionTable.TeleportWhereType;
import l2server.gameserver.instancemanager.BossManager;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2RaidBossInstance;
import l2server.gameserver.model.zone.L2ZoneType;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.util.Rnd;
import ai.group_template.L2AttackableAIScript;

/**
 * @author LasTravel
 * 
 * Apherus Raid Boss AI
 */

public class Apherus extends L2AttackableAIScript
{
	private static final String		_qn						= "Apherus";
	private static final int		_apherus				= 25775;
	private static final int[]		_apherusDoorsNpcs 		= {33133, 33134, 33135, 33136};
	private static final int[]		_apherusDoors 			= {26210041, 26210042, 26210043, 26210044};
	private static final int		_apherusKey				= 17373;
	private static final int[]		_apreusDoorGuyards		= {25776, 25777, 25778};
	private static final int		_apherusZoneId			= 60060;
	private static boolean 			_doorIsOpen				= false;
	private static L2Npc			_apherusRaid			= null;
	private static final L2Skill 	_gardenApherusRecovery	= SkillTable.getInstance().getInfo(14088, 1);
	private static final L2Skill 	_apherusInvincibility	= SkillTable.getInstance().getInfo(14201, 1);

	public Apherus(int id, String name, String descr)
	{
		super(id, name, descr);
		
		addAttackId(_apherus);
		addKillId(_apherus);
		addEnterZoneId(_apherusZoneId);
		addExitZoneId(_apherusZoneId);
		addSpawnId(_apherus);
		
		for (int a : _apherusDoorsNpcs)
		{
			addTalkId(a);
			addStartNpc(a);
		}
		
		L2RaidBossInstance boss = BossManager.getInstance().getBoss(_apherus);
		if (boss != null)
			notifySpawn(boss);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		_apherusRaid = npc;
		_apherusInvincibility.getEffects(npc, npc);
		
		//Be sure the doors are closed
		_doorIsOpen = false;
		for (int door : _apherusDoors)
		{
			DoorTable.getInstance().getDoor(door).closeMe();
		}
		return super.onSpawn(npc);
	}
	
	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if (character.isRaid())
			character.stopSkillEffects(_gardenApherusRecovery.getId());
		else if(character instanceof L2Playable)
		{
			if (!_doorIsOpen)
			{
				if (!character.isGM())
					character.teleToLocation(TeleportWhereType.Town);
			}
		}
		return super.onEnterZone(character, zone);
	}
	
	@Override
	public String onExitZone(L2Character character, L2ZoneType zone)
	{
		if (character.isRaid())
			_gardenApherusRecovery.getEffects(character, character);
		
		return super.onExitZone(character, zone);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		if (!_doorIsOpen && BossManager.getInstance().getBoss(_apherus) != null)
		{
			if (!player.destroyItemByItemId(_qn, _apherusKey, 1, player, true))
				return "apherusDoor-no.html";
			
			if (Rnd.get(100) > 80)
			{
				_doorIsOpen = true;
				for (int door : _apherusDoors)
				{
					DoorTable.getInstance().getDoor(door).openMe();
				}
				
				npc.broadcastPacket(new ExShowScreenMessage(1811740, 3000));
				
				_apherusRaid.stopSkillEffects(_apherusInvincibility.getId());
			}
			else
			{
				npc.broadcastPacket(new ExShowScreenMessage("$s1. The key does not match, so we're in trouble".replace("$s1", player.getName()), 3000));
				for (int a = 0; a < 4; a++)
				{
					L2MonsterInstance protector = (L2MonsterInstance) addSpawn(_apreusDoorGuyards[Rnd.get(_apreusDoorGuyards.length)], player.getX(), player.getY(), player.getZ(), 0, false, 600000, false);
					protector.setIsRunning(true);
					protector.setTarget(player);
					protector.addDamageHate(player, 500, 99999);
					protector.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
				}
			}
		}
		else
		{
			return "apherusDoor-no.html";
		}
		return super.onTalk(npc, player);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		_doorIsOpen = false;
		for (int door : _apherusDoors)
		{
			DoorTable.getInstance().getDoor(door).closeMe();
		}
		return super.onKill(npc, killer, isPet);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		if (!_doorIsOpen)	//cheaterzzzzzz
		{
			attacker.teleToLocation(TeleportWhereType.Town);
			npc.teleToLocation(npc.getSpawn().getX(), npc.getSpawn().getY(), npc.getSpawn().getZ());
		}
		return super.onAttack(npc, attacker, damage, isPet, skill);
	}
	
	@Override
	public int getOnKillDelay(int npcId)
	{
		return 0;
	}
	
	public static void main(String[] args)
	{
		new Apherus(-1, _qn, "ai/individual");
	}
}
