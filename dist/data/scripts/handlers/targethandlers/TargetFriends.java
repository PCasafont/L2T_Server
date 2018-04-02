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
import l2server.gameserver.events.instanced.EventTeam;
import l2server.gameserver.handler.ISkillTargetTypeHandler;
import l2server.gameserver.handler.SkillTargetTypeHandler;
import l2server.gameserver.model.L2ClanMember;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.Summon;
import l2server.gameserver.model.actor.instance.MonsterInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.instance.SummonInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.skills.SkillTargetDirection;
import l2server.gameserver.templates.skills.SkillTargetType;
import l2server.gameserver.templates.skills.SkillType;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Used by all skills that affects friendly players.
 *
 * @author Rewritten by ZaKaX.
 * @author Unhardcodded by nBd.
 */
public class TargetFriends implements ISkillTargetTypeHandler {
	@Override
	public WorldObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
		if (skill.isUseableWithoutTarget()) {
			final ArrayList<Creature> result = new ArrayList<Creature>();

			if (activeChar instanceof Playable) {
				final Player aPlayer = activeChar.getActingPlayer();

				// Friendly targets for players...
				if (skill.getTargetDirection() != SkillTargetDirection.PARTY_ALL_NOTME) {
					result.add(aPlayer);
				}

				final Summon aPet = aPlayer.getPet();
				if (aPet != null && !aPet.isDead()) {
					result.add(aPet);
				}

				for (SummonInstance summon : aPlayer.getSummons()) {
					if (summon.isDead()) {
						continue;
					}

					result.add(summon);
				}

				if (aPlayer.isInOlympiadMode()) {
					return result.toArray(new Creature[result.size()]);
				}

				Collection<Creature> candidates;
				if (skill.getSkillRadius() > 0) {
					candidates = aPlayer.getKnownList().getKnownCharactersInRadius(skill.getSkillRadius());
				} else {
					candidates = new ArrayList<Creature>();
					if (aPlayer.isInParty()) {
						candidates.addAll(aPlayer.getParty().getPartyMembers());
					}
					if (aPlayer.getClan() != null) {
						for (L2ClanMember member : aPlayer.getClan().getMembers()) {
							candidates.add(member.getPlayerInstance());
						}
					}
				}

				for (Creature obj : candidates) {
					if (obj == null) {
						continue;
					}

					final Player kTarget = obj.getActingPlayer();
					if (kTarget == null || aPlayer == kTarget) {
						continue;
					} else if (!aPlayer.isAbleToCastOnTarget(kTarget, skill, true)) {
						continue;
					}

					if (aPlayer.isInOlympiadMode()) {
						continue;
					}

					if (aPlayer.isPlayingEvent()) {
						EventTeam playerTeam = aPlayer.getEvent().getParticipantTeam(aPlayer.getObjectId());
						EventTeam targetTeam = aPlayer.getEvent().getParticipantTeam(aPlayer.getObjectId());
						if (playerTeam != targetTeam) {
							continue;
						}
					}

					if (skill.getTargetDirection() == SkillTargetDirection.PARTY_ALL) {
						if (!aPlayer.isInSameParty(kTarget)) {
							continue;
						}

						// We need the check for cases where player actually isn't in a party.
						//if (result.size() >= 9)
						//	break;
					} else if (skill.getTargetDirection() == SkillTargetDirection.PARTY_ALL_NOTME) {
						if (aPlayer == kTarget || !aPlayer.isInSameParty(kTarget)) {
							continue;
						}
					} else if (skill.getTargetDirection() == SkillTargetDirection.PARTY_AND_CLAN) {
						if (!aPlayer.isInSameParty(kTarget) && !aPlayer.isInSameClan(kTarget)) {
							continue;
						}
					} else if (skill.getTargetDirection() == SkillTargetDirection.CLAN) {
						if (!aPlayer.isInSameClan(kTarget)) {
							continue;
						}
					} else if (skill.getTargetDirection() == SkillTargetDirection.DEAD_PARTY_MEMBER) {
						if (!aPlayer.isInSameParty(kTarget)) {
							continue;
						}
					} else if (skill.getTargetDirection() == SkillTargetDirection.DEAD_CLAN_MEMBER) {
						if (!aPlayer.isInSameClan(kTarget)) {
							continue;
						}
					} else if (skill.getTargetDirection() == SkillTargetDirection.DEAD_PARTY_AND_CLAN_MEMBER) {
						if (!aPlayer.isInSameClan(kTarget)) {
							continue;
						}
					} else if (skill.getTargetDirection() == SkillTargetDirection.ALLIANCE) {
						if (!aPlayer.isInSameAlly(kTarget)) {
							continue;
						}
					}
					if (!GeoEngine.getInstance().canSeeTarget(aPlayer, kTarget)) {
						continue;
					}

					result.add(kTarget);
					final Summon kPet = kTarget.getPet();
					if (kPet != null && !kPet.isDead()) {
						result.add(kPet);
					}

					for (SummonInstance summon : kTarget.getSummons()) {
						if (summon.isDead()) {
							continue;
						}

						result.add(summon);
					}
				}
			} else if (activeChar instanceof MonsterInstance) {
				final MonsterInstance aMonster = (MonsterInstance) activeChar;

				for (Creature obj : aMonster.getKnownList().getKnownCharactersInRadius(skill.getSkillRadius())) {
					if (!(obj instanceof MonsterInstance)) {
						continue;
					}

					final MonsterInstance kMonster = (MonsterInstance) obj;

					if (aMonster == kMonster) {
						continue;
					} else if (aMonster.getFactionId() == null || kMonster.getFactionId() == null) {
						continue;
					} else if (!aMonster.getFactionId().equals(kMonster.getFactionId())) {
						continue;
					}

					result.add(kMonster);
				}
			}

			return result.toArray(new Creature[result.size()]);
		} else {
			if (target != null && (GeoEngine.getInstance().canSeeTarget(activeChar, target) || skill.getSkillType() == SkillType.SUMMON_FRIEND)) {
				if (activeChar instanceof MonsterInstance) {
					final MonsterInstance aMonster = (MonsterInstance) activeChar;

					if (target instanceof MonsterInstance) {
						final MonsterInstance mTarget = (MonsterInstance) target;

						if (aMonster.getFactionId() != null && mTarget.getFactionId() != null &&
								aMonster.getFactionId().equals(mTarget.getFactionId())) {
							return new Creature[]{mTarget};
						}
					}
				} else if (activeChar instanceof Playable) {
					final Player aPlayer = activeChar.getActingPlayer();
					if (target instanceof Playable) {
						final Player tPlayer = target.getActingPlayer();

						if (skill.getTargetDirection() == SkillTargetDirection.PARTY_ONE) {
							if (aPlayer == tPlayer || aPlayer.isInSameParty(tPlayer)) {
								return new Creature[]{target};
							}
						} else if (skill.getTargetDirection() == SkillTargetDirection.PARTY_AND_CLAN) {
							if (aPlayer == tPlayer || aPlayer.isInSameParty(tPlayer) || aPlayer.isInSameClan(tPlayer)) {
								return new Creature[]{target};
							}
						} else if (skill.getTargetDirection() == SkillTargetDirection.PARTY_ONE_NOTME) {
							if (aPlayer != tPlayer && aPlayer.isInSameParty(tPlayer)) {
								return new Creature[]{target};
							}
						}
					}
				}
			}

			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			return null;
		}
	}

	@Override
	public Enum<SkillTargetType> getTargetType() {
		return SkillTargetType.TARGET_FRIENDS;
	}

	public static void main(String[] args) {
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetFriends());
	}
}
