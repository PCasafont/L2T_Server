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

package handlers.targethandlers;

import l2server.gameserver.GeoEngine;
import l2server.gameserver.handler.ISkillTargetTypeHandler;
import l2server.gameserver.handler.SkillTargetTypeHandler;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.*;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.templates.skills.SkillTargetType;
import l2server.gameserver.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author nBd
 */
public class TargetAura implements ISkillTargetTypeHandler {
	@SuppressWarnings("unused")
	@Override
	public WorldObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
		List<Creature> targetList = new ArrayList<Creature>();

		int radius = skill.getSkillRadius();
		boolean srcInArena = activeChar.isInsideZone(CreatureZone.ZONE_PVP) && !activeChar.isInsideZone(CreatureZone.ZONE_SIEGE);

		Player src = activeChar.getActingPlayer();

		// Go through the Creature knownList
		Collection<WorldObject> objs = activeChar.getKnownList().getKnownObjects().values();
		//synchronized (activeChar.getKnownList().getKnownObjects())
		{
			for (WorldObject obj : objs) {
				if (obj instanceof Attackable || obj instanceof Playable) {
					// Don't add this target if this is a Pc->Pc pvp
					// casting and pvp condition not met
					if (obj == activeChar || obj == src || ((Creature) obj).isDead()) {
						continue;
					}

					if (src != null) {
						if (!GeoEngine.getInstance().canSeeTarget(activeChar, obj)) {
							continue;
						}

						// check if both attacker and target are
						// L2PcInstances and if they are in same party
						if (obj instanceof Player) {
							Player trg = (Player) obj;

							if (!src.checkPvpSkill(obj, skill)) {
								continue;
							}

							if (src.getParty() != null && ((Player) obj).getParty() != null &&
									src.getParty().getPartyLeaderOID() == ((Player) obj).getParty().getPartyLeaderOID()) {
								continue;
							}

							if (!srcInArena && !(((Creature) obj).isInsideZone(CreatureZone.ZONE_PVP) &&
									!((Creature) obj).isInsideZone(CreatureZone.ZONE_SIEGE))) {
								if (src.getAllyId() == ((Player) obj).getAllyId() && src.getAllyId() != 0) {
									continue;
								}

								if (src.getClanId() != 0 && src.getClanId() == ((Player) obj).getClanId()) {
									continue;
								}
							}
						}
						if (obj instanceof Summon) {
							Player trg = ((Summon) obj).getOwner();

							if (trg == src) {
								continue;
							}

							if (!src.checkPvpSkill(trg, skill)) {
								continue;
							}

							if (src.getParty() != null && trg.getParty() != null &&
									src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID()) {
								continue;
							}

							if (!srcInArena && !(((Creature) obj).isInsideZone(CreatureZone.ZONE_PVP) &&
									!((Creature) obj).isInsideZone(CreatureZone.ZONE_SIEGE))) {
								if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0) {
									continue;
								}

								if (src.getClanId() != 0 && src.getClanId() == trg.getClanId()) {
									continue;
								}
							}
						}
					} else
					// Skill user is not L2PlayableInstance
					{
						if (!(obj instanceof Playable) // Target is not L2PlayableInstance
								&& !activeChar.isConfused()) // and caster not confused (?)
						{
							continue;
						}
					}
					if (!Util.checkIfInRange(radius, activeChar, obj, true)) {
						continue;
					}

					if (onlyFirst == false) {
						targetList.add((Creature) obj);
					} else {
						return new Creature[]{(Creature) obj};
					}
				}
			}
		}
		return targetList.toArray(new Creature[targetList.size()]);
	}

	@Override
	public Enum<SkillTargetType> getTargetType() {
		// TODO Auto-generated method stub
		return SkillTargetType.TARGET_AURA;
	}

	public static void main(String[] args) {
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetAura());
	}
}
