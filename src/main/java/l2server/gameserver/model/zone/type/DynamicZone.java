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

package l2server.gameserver.model.zone.type;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldRegion;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.zone.ZoneType;

import java.util.concurrent.Future;

/**
 * A dynamic zone?
 * Maybe use this for interlude skills like protection field :>
 *
 * @author durgus
 */
public class DynamicZone extends ZoneType {
	private WorldRegion region;
	private Creature owner;
	private Future<?> task;
	private Skill skill;

	protected void setTask(Future<?> task) {
		this.task = task;
	}

	public DynamicZone(WorldRegion region, Creature owner, Skill skill) {
		super(-1);
		this.region = region;
		this.owner = owner;
		this.skill = skill;

		Runnable r = this::remove;
		setTask(ThreadPoolManager.getInstance().scheduleGeneral(r, skill.getBuffDuration()));
	}

	@Override
	protected void onEnter(Creature character) {
		try {
			if (character instanceof Player) {
				character.sendMessage("You have entered a temporary zone!");
			}
			skill.getEffects(owner, character);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onExit(Creature character) {
		if (character instanceof Player) {
			character.sendMessage("You have left a temporary zone!");
		}
		if (character == owner) {
			remove();
			return;
		}
		character.stopSkillEffects(skill.getId());
	}

	protected void remove() {
		if (task == null) {
			return;
		}
		task.cancel(false);
		task = null;

		region.removeZone(this);
		for (Creature member : characterList.values()) {
			try {
				member.stopSkillEffects(skill.getId());
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
		owner.stopSkillEffects(skill.getId());
	}

	@Override
	public void onDieInside(Creature character, Creature killer) {
		if (character == owner) {
			remove();
		} else {
			character.stopSkillEffects(skill.getId());
		}
	}

	@Override
	public void onReviveInside(Creature character) {
		skill.getEffects(owner, character);
	}
}
