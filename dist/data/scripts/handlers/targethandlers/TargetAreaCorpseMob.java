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
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.skills.SkillTargetType;
import l2server.gameserver.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author nBd
 */
public class TargetAreaCorpseMob implements ISkillTargetTypeHandler {
	@Override
	public WorldObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
		List<Creature> targetList = new ArrayList<Creature>();

		if (!(target instanceof Attackable) || !target.isDead()) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return null;
		}

		if (onlyFirst == false) {
			targetList.add(target);
		} else {
			return new Creature[]{target};
		}

		boolean srcInArena = activeChar.isInsideZone(CreatureZone.ZONE_PVP) && !activeChar.isInsideZone(CreatureZone.ZONE_SIEGE);

		Player src = null;
		if (activeChar instanceof Player) {
			src = (Player) activeChar;
		}

		Player trg = null;

		int radius = skill.getSkillRadius();

		Collection<WorldObject> objs = activeChar.getKnownList().getKnownObjects().values();
		//synchronized (activeChar.getKnownList().getKnownObjects())
		{
			for (WorldObject obj : objs) {
				if (!(obj instanceof Attackable || obj instanceof Playable) || ((Creature) obj).isDead() || (Creature) obj == activeChar) {
					continue;
				}

				if (!Util.checkIfInRange(radius, target, obj, true)) {
					continue;
				}

				if (!GeoEngine.getInstance().canSeeTarget(activeChar, obj)) {
					continue;
				}

				if (obj instanceof Player && src != null) {
					trg = (Player) obj;

					if (src.getParty() != null && trg.getParty() != null &&
							src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID()) {
						continue;
					}

					if (trg.isInsideZone(CreatureZone.ZONE_PEACE)) {
						continue;
					}

					if (!srcInArena && !(trg.isInsideZone(CreatureZone.ZONE_PVP) && !trg.isInsideZone(CreatureZone.ZONE_SIEGE))) {
						if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0) {
							continue;
						}

						if (src.getClan() != null && trg.getClan() != null) {
							if (src.getClan().getClanId() == trg.getClan().getClanId()) {
								continue;
							}
						}

						if (!src.checkPvpSkill(obj, skill)) {
							continue;
						}
					}
				}
				if (obj instanceof Summon && src != null) {
					trg = ((Summon) obj).getOwner();

					if (src.getParty() != null && trg.getParty() != null &&
							src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID()) {
						continue;
					}

					if (!srcInArena && !(trg.isInsideZone(CreatureZone.ZONE_PVP) && !trg.isInsideZone(CreatureZone.ZONE_SIEGE))) {
						if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0) {
							continue;
						}

						if (src.getClan() != null && trg.getClan() != null) {
							if (src.getClan().getClanId() == trg.getClan().getClanId()) {
								continue;
							}
						}

						if (!src.checkPvpSkill(trg, skill)) {
							continue;
						}
					}

					if (((Summon) obj).isInsideZone(CreatureZone.ZONE_PEACE)) {
						continue;
					}
				}
				targetList.add((Creature) obj);
			}
		}
		if (targetList.size() == 0) {
			return null;
		}

		return targetList.toArray(new Creature[targetList.size()]);
	}

	@Override
	public Enum<SkillTargetType> getTargetType() {
		// TODO Auto-generated method stub
		return SkillTargetType.TARGET_AREA_CORPSE_MOB;
	}

	public static void main(String[] args) {
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetAreaCorpseMob());
	}
}
