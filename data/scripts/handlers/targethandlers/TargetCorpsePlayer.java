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

import l2server.gameserver.handler.ISkillTargetTypeHandler;
import l2server.gameserver.handler.SkillTargetTypeHandler;
import l2server.gameserver.instancemanager.CastleManager;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2PetInstance;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.skills.L2SkillTargetType;
import l2server.gameserver.templates.skills.L2SkillType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author nBd
 */
public class TargetCorpsePlayer implements ISkillTargetTypeHandler
{
	/**
	 */
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new ArrayList<L2Character>();

		if (target != null && target.isDead())
		{
			L2PcInstance player = null;

			if (activeChar instanceof L2PcInstance)
			{
				player = (L2PcInstance) activeChar;
			}

			L2PcInstance targetPlayer = null;

			if (target instanceof L2PcInstance)
			{
				targetPlayer = (L2PcInstance) target;
			}

			L2PetInstance targetPet = null;

			if (target instanceof L2PetInstance)
			{
				targetPet = (L2PetInstance) target;
			}

			if (player != null && (targetPlayer != null || targetPet != null))
			{
				boolean condGood = true;

				if (skill.getSkillType() == L2SkillType.RESURRECT)
				{
					// check target is not in a active siege zone
					//check target is not in a active siege zone
					Castle castle = null;

					if (targetPlayer != null)
					{
						castle = CastleManager.getInstance()
								.getCastle(targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ());
					}
					else
					{
						castle = CastleManager.getInstance()
								.getCastle(targetPet.getOwner().getX(), targetPet.getOwner().getY(),
										targetPet.getOwner().getZ());
					}

					if (castle != null)
					{
						if (castle.getSiege().getIsInProgress())
						{
							if (targetPlayer != null)
							{
								boolean isAttacker = castle.getSiege().checkIsAttacker(player.getClan());
								boolean isDefender = castle.getSiege().checkIsDefender(player.getClan());
								boolean none = !isAttacker && !isDefender;
								if (none && targetPlayer.isInsideZone(L2Character.ZONE_SIEGE))
								{
									condGood = false;
								}
								else if (isAttacker)
								{
									condGood = false;
								}
								else if (isDefender && castle.getSiege().getControlTowerCount() == 0)
								{
									condGood = false;
								}
							}

							if (!condGood)
							{
								activeChar.sendPacket(SystemMessage
										.getSystemMessage(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE));
							}
						}
					}

					if (targetPlayer != null)
					{
						if (targetPlayer.isReviveRequested())
						{
							if (targetPlayer.isRevivingPet())
							{
								player.sendPacket(SystemMessage.getSystemMessage(
										SystemMessageId.MASTER_CANNOT_RES)); // While a pet is attempting to resurrect, it cannot help in resurrecting its master.
							}
							else
							{
								player.sendPacket(SystemMessage.getSystemMessage(
										SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED)); // Resurrection is already been proposed.
							}

							condGood = false;
						}
					}
					else if (targetPet != null)
					{
						if (targetPet.getOwner() != player)
						{
							condGood = false;
							player.sendMessage("You are not the owner of this pet");
						}
					}
				}

				if (condGood)
				{
					if (onlyFirst == false)
					{
						targetList.add(target);
						return targetList.toArray(new L2Object[targetList.size()]);
					}
					else
					{
						return new L2Character[]{target};
					}
				}
			}
		}
		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
		return null;
	}

	/**
	 */
	@Override
	public Enum<L2SkillTargetType> getTargetType()
	{
		// TODO Auto-generated method stub
		return L2SkillTargetType.TARGET_CORPSE_PLAYER;
	}

	public static void main(String[] args)
	{
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetCorpsePlayer());
	}
}
