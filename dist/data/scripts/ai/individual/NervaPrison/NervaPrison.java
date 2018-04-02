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

package ai.individual.NervaPrison;

import ai.group_template.L2AttackableAIScript;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.instancemanager.ZoneManager;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.instance.DoorInstance;
import l2server.gameserver.model.actor.instance.MonsterInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.zone.ZoneType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author LasTravel
 * <p>
 * Prison AI
 * <p>
 * Source:
 * - http://l2wiki.com/Raiders_Crossroads
 * - https://4gameforum.com/showthread.php?t=23180
 */

public class NervaPrison extends L2AttackableAIScript {
	private static final String qn = "NervaPrison";
	private static final int doorNpc = 19459;
	private static final int kaysen = 19458;
	private static final int nervaKey = 36665;
	private static final int kaiser = 23329;
	private static final Map<ZoneType, List<DoorInstance>> prisons = new HashMap<ZoneType, List<DoorInstance>>();

	public NervaPrison(int id, String name, String descr) {
		super(id, name, descr);

		addTalkId(doorNpc);
		addStartNpc(doorNpc);
		addFirstTalkId(kaysen);
		addKillId(kaiser);
		addSpawnId(kaysen);

		for (int i = 60052; i <= 60059; i++) {
			List<DoorInstance> doors = new ArrayList<DoorInstance>(2);
			ZoneType zone = ZoneManager.getInstance().getZoneById(i);
			for (Creature door : zone.getCharactersInside().values()) {
				if (door instanceof DoorInstance) {
					doors.add((DoorInstance) door);
				}
			}

			prisons.put(zone, doors);
		}
	}

	@Override
	public String onTalk(Npc npc, Player player) {
		if (npc.getNpcId() == doorNpc) {
			if (!player.destroyItemByItemId(qn, nervaKey, 1, player, true)) {
				return "19459-1.html";
			}

			for (Entry<ZoneType, List<DoorInstance>> currentZone : prisons.entrySet()) {
				if (currentZone.getKey().isInsideZone(npc)) {
					for (DoorInstance door : currentZone.getValue()) {
						if (door.getOpen()) {
							return super.onFirstTalk(npc, player); //Cheating
						}
					}

					for (DoorInstance door : currentZone.getValue()) {
						door.openMe();
					}
				}
			}
			npc.deleteMe();
		}
		return super.onTalk(npc, player);
	}

	@Override
	public String onFirstTalk(Npc npc, Player player) {
		if (!npc.isDead() && npc.isInsideRadius(player, 50, false, false)) {
			for (Entry<ZoneType, List<DoorInstance>> currentZone : prisons.entrySet()) {
				if (currentZone.getKey().isInsideZone(npc)) {
					for (DoorInstance door : currentZone.getValue()) {
						if (!door.getOpen()) {
							return super.onFirstTalk(npc, player); //Cheating
						}
					}

					for (DoorInstance door : currentZone.getValue()) {
						door.closeMe();
					}
				}
			}

			npc.deleteMe();

			MonsterInstance kaiser =
					(MonsterInstance) addSpawn(NervaPrison.kaiser, npc.getX(), npc.getY(), npc.getZ(), 0, false, 180000, false); //3min
			kaiser.setTarget(player);
			kaiser.addDamageHate(player, 500, 99999);
			kaiser.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
		} else {
			return "19458.html";
		}
		return super.onFirstTalk(npc, player);
	}

	@Override
	public String onKill(Npc npc, Player killer, boolean isPet) {
		for (Entry<ZoneType, List<DoorInstance>> currentZone : prisons.entrySet()) {
			if (currentZone.getKey().isInsideZone(npc)) {
				for (DoorInstance door : currentZone.getValue()) {
					door.openMe();
				}
			}
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public final String onSpawn(Npc npc) {
		for (Entry<ZoneType, List<DoorInstance>> currentZone : prisons.entrySet()) {
			if (currentZone.getKey().isInsideZone(npc)) {
				for (DoorInstance door : currentZone.getValue()) {
					door.closeMe();
				}

				//Kick players inside...
				for (Creature chara : currentZone.getKey().getCharactersInside().values()) {
					if (chara == null) {
						continue;
					}

					if (chara instanceof Playable) {
						chara.teleToLocation(npc.getX(), npc.getY() + 500, npc.getZ());
					}
				}
			}
		}
		return super.onSpawn(npc);
	}

	public static void main(String[] args) {
		new NervaPrison(-1, qn, "ai/individual");
	}
}
