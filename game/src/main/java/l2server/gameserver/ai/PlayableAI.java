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

package l2server.gameserver.ai;

import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.network.SystemMessageId;

/**
 * This class manages AI of Playable.<BR><BR>
 * <p>
 * PlayableAI :<BR><BR>
 * <li>SummonAI</li>
 * <li>PlayerAI</li>
 * <BR> <BR>
 *
 * @author JIV
 */
public abstract class PlayableAI extends CreatureAI {
	public PlayableAI(Creature creature) {
		super(creature);
	}

	/**
	 * @see CreatureAI#onIntentionAttack(Creature)
	 */
	@Override
	protected void onIntentionAttack(Creature target) {
		if (target instanceof Playable) {
			if (target.getActingPlayer().getProtectionBlessing() && actor.getActingPlayer().getLevel() - target.getActingPlayer().getLevel() >= 10 &&
					actor.getActingPlayer().getReputation() < 0 && !target.isInsideZone(Creature.ZONE_PVP)) {
				// If attacker have karma and have level >= 10 than his target and target have
				// Newbie Protection Buff,
				actor.getActingPlayer().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				clientActionFailed();
				return;
			}

			if (actor.getActingPlayer().getProtectionBlessing() && target.getActingPlayer().getLevel() - actor.getActingPlayer().getLevel() >= 10 &&
					target.getActingPlayer().getReputation() < 0 && !target.isInsideZone(Creature.ZONE_PVP)) {
				// If target have karma and have level >= 10 than his target and actor have
				// Newbie Protection Buff,
				actor.getActingPlayer().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				clientActionFailed();
				return;
			}

			if (target.getActingPlayer().isCursedWeaponEquipped() && actor.getActingPlayer().getLevel() <= 20) {
				actor.getActingPlayer().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				clientActionFailed();
				return;
			}

			if (actor.getActingPlayer().isCursedWeaponEquipped() && target.getActingPlayer().getLevel() <= 20) {
				actor.getActingPlayer().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				clientActionFailed();
				return;
			}
		}

		super.onIntentionAttack(target);
	}

	/**
	 * @see CreatureAI#onIntentionCast(Skill, WorldObject)
	 */
	@Override
	protected void onIntentionCast(Skill skill, WorldObject target) {
		if (target instanceof Playable && skill.isOffensive()) {
			if (target.getActingPlayer().getProtectionBlessing() && actor.getActingPlayer().getLevel() - target.getActingPlayer().getLevel() >= 10 &&
					actor.getActingPlayer().getReputation() < 0 && !((Playable) target).isInsideZone(Creature.ZONE_PVP)) {
				// If attacker have karma and have level >= 10 than his target and target have
				// Newbie Protection Buff,
				actor.getActingPlayer().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				clientActionFailed();
				actor.setIsCastingNow(false);
				actor.setIsCastingNow2(false);
				return;
			}

			if (actor.getActingPlayer().getProtectionBlessing() && target.getActingPlayer().getLevel() - actor.getActingPlayer().getLevel() >= 10 &&
					target.getActingPlayer().getReputation() < 0 && !((Playable) target).isInsideZone(Creature.ZONE_PVP)) {
				// If target have karma and have level >= 10 than his target and actor have
				// Newbie Protection Buff,
				actor.getActingPlayer().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				clientActionFailed();
				actor.setIsCastingNow(false);
				actor.setIsCastingNow2(false);
				return;
			}

			if (target.getActingPlayer().isCursedWeaponEquipped() && actor.getActingPlayer().getLevel() <= 20) {
				actor.getActingPlayer().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				clientActionFailed();
				actor.setIsCastingNow(false);
				actor.setIsCastingNow2(false);
				return;
			}

			if (actor.getActingPlayer().isCursedWeaponEquipped() && target.getActingPlayer().getLevel() <= 20) {
				actor.getActingPlayer().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				clientActionFailed();
				actor.setIsCastingNow(false);
				actor.setIsCastingNow2(false);
				return;
			}
		}

		super.onIntentionCast(skill, target);
	}
}
