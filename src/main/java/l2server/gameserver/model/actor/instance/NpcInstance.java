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

import l2server.gameserver.model.Abnormal;
import l2server.gameserver.model.L2Effect;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.status.FolkStatus;
import l2server.gameserver.stats.effects.EffectBuff;
import l2server.gameserver.stats.effects.EffectDebuff;
import l2server.gameserver.templates.chars.NpcTemplate;

public class NpcInstance extends Npc {
	public NpcInstance(int objectId, NpcTemplate template) {
		super(objectId, template);
		setInstanceType(InstanceType.L2NpcInstance);
		setIsInvul(false);
	}
	
	@Override
	public FolkStatus getStatus() {
		return (FolkStatus) super.getStatus();
	}
	
	@Override
	public void initCharStatus() {
		setStatus(new FolkStatus(this));
	}
	
	@Override
	public void addEffect(Abnormal newEffect) {
		if (newEffect == null) {
			return;
		}
		
		boolean found = false;
		for (L2Effect effect : newEffect.getEffects()) {
			if (!(effect instanceof EffectDebuff || effect instanceof EffectBuff)) {
				found = true;
			}
		}
		
		if (!found) {
			super.addEffect(newEffect);
		} else {
			newEffect.stopEffectTask();
		}
	}
}
