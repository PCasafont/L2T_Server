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

package l2server.gameserver.model.actor.instance;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.InstanceType;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.CreatureZone;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.templates.chars.NpcTemplate;

import java.util.Collection;
import java.util.concurrent.ScheduledFuture;

/**
 * @author Drunkard Zabb0x
 * Lets drink2code!
 */
public class XmassTreeInstance extends Npc {
	public static final int SPECIAL_TREE_ID = 13007;
	private ScheduledFuture<?> aiTask;
	
	class XmassAI implements Runnable {
		private XmassTreeInstance caster;
		private Skill skill;
		
		protected XmassAI(XmassTreeInstance caster, Skill skill) {
			this.caster = caster;
			this.skill = skill;
		}
		
		@Override
		public void run() {
			if (skill == null || caster.isInsideZone(CreatureZone.ZONE_PEACE)) {
				caster.aiTask.cancel(false);
				caster.aiTask = null;
				return;
			}
			Collection<Player> plrs = getKnownList().getKnownPlayersInRadius(200);
			for (Player player : plrs) {
				if (player.getFirstEffect(skill.getId()) == null) {
					skill.getEffects(player, player);
				}
			}
		}
	}
	
	public XmassTreeInstance(int objectId, NpcTemplate template) {
		super(objectId, template);
		setInstanceType(InstanceType.L2XmassTreeInstance);
		if (template.NpcId == SPECIAL_TREE_ID) {
			aiTask = ThreadPoolManager.getInstance()
					.scheduleGeneralAtFixedRate(new XmassAI(this, SkillTable.getInstance().getInfo(2139, 1)), 3000, 3000);
		}
	}
	
	@Override
	public void deleteMe() {
		if (aiTask != null) {
			aiTask.cancel(true);
		}
		
		super.deleteMe();
	}
	
	@Override
	public int getDistanceToWatchObject(WorldObject object) {
		return 900;
	}
	
	/* (non-Javadoc)
	 * @see l2server.gameserver.model.WorldObject#isAttackable()
	 */
	@Override
	public boolean isAutoAttackable(Creature attacker) {
		return false;
	}
	
	/**
	 * @see Npc#onAction(Player, boolean)
	 */
	@Override
	public void onAction(Player player, boolean interact) {
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}
