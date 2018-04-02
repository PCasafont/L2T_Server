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

package ai.individual.GenesisStatues;

import ai.group_template.L2AttackableAIScript;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.MonsterInstance;
import l2server.gameserver.model.actor.instance.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LasTravel
 * <p>
 * Genesis Statues AI
 */

public class GenesisStatues extends L2AttackableAIScript {
	private static final int[] statues = {33138, 33139, 33140};
	private static final int[] keepers = {23038, 23039, 23040};
	private static final Skill blessingOfGarden = SkillTable.getInstance().getInfo(14200, 1);
	private static Map<Integer, Long> spawns = new HashMap<Integer, Long>(3);
	
	public GenesisStatues(int id, String name, String descr) {
		super(id, name, descr);
		
		for (int statue : statues) {
			addTalkId(statue);
			addStartNpc(statue);
			
			spawns.put(statue, 0L);
		}
		
		for (int keeper : keepers) {
			addKillId(keeper);
		}
	}
	
	@Override
	public String onTalk(Npc npc, Player player) {
		long currentTime = System.currentTimeMillis();
		if (!spawns.containsKey(npc.getNpcId()) || spawns.get(npc.getNpcId()) + 3600000 > currentTime) {
			//final SimpleDateFormat dateFormatter = new SimpleDateFormat("[EEEE d MMMMMMM] @ k:m:s: ");
			
			//player.sendMessage("Magic will happen again on the " + dateFormatter.format(spawns.get(npc.getNpcId()) + 3600000) + ".");
			return npc.getNpcId() + "-no.html";
		} else {
			spawns.put(npc.getNpcId(), currentTime);
			
			MonsterInstance angelStatue =
					(MonsterInstance) addSpawn(npc.getNpcId() - 10100, player.getX(), player.getY(), player.getZ(), 0, false, 0, false);
			angelStatue.setTarget(player);
			angelStatue.addDamageHate(player, 500, 99999);
			angelStatue.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
		}
		return super.onTalk(npc, player);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isPet) {
		blessingOfGarden.getEffects(killer, killer);
		
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(String[] args) {
		new GenesisStatues(-1, "GenesisStatues", "ai/individual");
	}
}
