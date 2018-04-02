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
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.Summon;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.skills.SkillTargetType;
import l2server.gameserver.util.Util;
import l2server.util.Point3D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author nBd
 */
public class TargetArea implements ISkillTargetTypeHandler {
	@Override
	public WorldObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
		List<Creature> targetList = new ArrayList<Creature>();
		Point3D targetPoint = null; // FIXME activeChar.getLastRepeatingSkillTargetPoint();

		if (targetPoint == null && (!(target instanceof Attackable || target instanceof Playable) ||
				// Target is not Attackable or L2PlayableInstance
				skill.getCastRange() >= 0 &&
						(target == null || target == activeChar || target.isAlikeDead()))) // target is null or self or dead/faking
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return null;
		}

		Creature cha;
		if (skill.getCastRange() >= 0 && targetPoint == null) {
			cha = target;

			if (!onlyFirst) {
				targetList.add(cha); // Add target to target list
			} else {
				return new Creature[]{cha};
			}
		} else {
			cha = activeChar;
		}
		boolean effectOriginIsL2PlayableInstance = cha instanceof Playable;

		boolean srcIsSummon = activeChar instanceof Summon;

		Player src = activeChar.getActingPlayer();

		int radius = skill.getSkillRadius();

		boolean srcInArena = activeChar.isInsideZone(Creature.ZONE_PVP) && !activeChar.isInsideZone(Creature.ZONE_SIEGE);

		Collection<WorldObject> objs = activeChar.getKnownList().getKnownObjects().values();
		//synchronized (activeChar.getKnownList().getKnownObjects())
		{
			for (WorldObject obj : objs) {
				if (!(obj instanceof Attackable || obj instanceof Playable)) {
					continue;
				}

				if (obj == cha) {
					continue;
				}

				target = (Creature) obj;

				if (!GeoEngine.getInstance().canSeeTarget(activeChar, target)) {
					continue;
				}

				if (!target.isDead() && target != activeChar) {
					/* FIXME
                    if(skill.isRepeating() && targetPoint != null)
					{
						if(radius < Util.calculateDistance(targetPoint.getX(), targetPoint.getY(), targetPoint.getZ(), target.getX(), target.getY(), target.getZ(), true))
							continue;
					}
					else*/
					if (!Util.checkIfInRange(radius, obj, cha, true)) {
						continue;
					}

					if (src != null) // caster is l2playableinstance and exists
					{
						if (obj instanceof Player) {
							Player trg = (Player) obj;

							if (trg == src) {
								continue;
							}

							if (src.getParty() != null && trg.getParty() != null &&
									src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID()) {
								continue;
							}

							if (trg.isInsideZone(Creature.ZONE_PEACE) && trg.getReputation() < 0 && trg.getPvpFlag() == 0) {
								continue;
							}

							if (!srcInArena && !(trg.isInsideZone(Creature.ZONE_PVP) && !trg.isInsideZone(Creature.ZONE_SIEGE))) {
								if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0) {
									continue;
								}

								if (src.getClan() != null && trg.getClan() != null) {
									if (src.getClan().getClanId() == trg.getClan().getClanId()) {
										continue;
									}
								}

								if (!src.checkPvpSkill(obj, skill, srcIsSummon)) {
									continue;
								}
							}
						}
						if (obj instanceof Summon) {
							Player trg = ((Summon) obj).getOwner();

							if (trg == src) {
								continue;
							}

							if (src.getParty() != null && trg.getParty() != null &&
									src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID()) {
								continue;
							}

							if (trg.isInsideZone(Creature.ZONE_PEACE) && trg.getReputation() < 0 && trg.getPvpFlag() == 0)

							{
								if (!srcInArena && !(trg.isInsideZone(Creature.ZONE_PVP) && !trg.isInsideZone(Creature.ZONE_SIEGE))) {
									if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0) {
										continue;
									}

									if (src.getClan() != null && trg.getClan() != null) {
										if (src.getClan().getClanId() == trg.getClan().getClanId()) {
											continue;
										}
									}

									if (!src.checkPvpSkill(trg, skill, srcIsSummon)) {
										continue;
									}
								}
							}

							if (((Summon) obj).isInsideZone(Creature.ZONE_PEACE)) {
								continue;
							}
						}
					} else
					// Skill user is not L2PlayableInstance
					{
						if (effectOriginIsL2PlayableInstance && // If effect starts at L2PlayableInstance and
								!(obj instanceof Playable)) // Object is not L2PlayableInstance
						{
							continue;
						}
					}
					targetList.add((Creature) obj);
				}
			}
		}

		if (targetList.size() == 0) {
			return null;
		}

		return targetList.toArray(new Creature[targetList.size()]);
	}

	@Override
	public Enum<SkillTargetType> getTargetType() {
		return SkillTargetType.TARGET_AREA;
	}

	public static void main(String[] args) {
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetArea());
	}
}
