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

package l2server.gameserver.network.clientpackets;

import l2server.Config;
import l2server.gameserver.datatables.ComboSkillTable;
import l2server.gameserver.datatables.ComboSkillTable.Combo;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.templates.skills.L2SkillType;
import l2server.gameserver.util.GMAudit;
import l2server.log.Log;

import java.util.Map.Entry;

/**
 * This class ...
 *
 * @version $Revision: 1.7.2.1.2.3 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestMagicSkillUse extends L2GameClientPacket
{

	private int _magicId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;

	@Override
	protected void readImpl()
	{
		_magicId = readD(); // Identifier of the used skill
		_ctrlPressed = readD() != 0; // True if it's a ForceAttack : Ctrl pressed
		_shiftPressed = readC() != 0; // True if Shift pressed
	}

	@Override
	protected void runImpl()
	{
		// Get the current L2PcInstance of the player
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		if (activeChar.getCaptcha() != null && !activeChar.onActionCaptcha(true))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (activeChar.hasIdentityCrisis())
		{
			activeChar.sendMessage("You cannot use any skill while having identity crisis.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (activeChar.isEventDisarmed())
		{
			activeChar.sendMessage("You cannot use any skill while playing this event.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		// Get the level of the used skill
		int level = activeChar.getSkillLevelHash(_magicId);

		// Check combo
		if (activeChar.getTarget() instanceof L2Character)
		{
			for (L2Abnormal ab : ((L2Character) activeChar.getTarget()).getAllEffects())
			{
				if (ab.getComboId() != 0)
				{
					Combo combo = ComboSkillTable.getInstance().getCombo(ab.getComboId());
					if (combo.skills.containsKey(_magicId))
					{
						_magicId = combo.skills.get(_magicId);
						level = 1;
						break;
					}

					for (Entry<Integer, Integer> comboSkill : combo.skills.entrySet())
					{
						if (comboSkill.getValue() == _magicId && activeChar.getSkillLevelHash(comboSkill.getKey()) > 0)
						{
							level = 1;
							break;
						}
					}
				}
			}
		}

		if (level <= 0)
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		// Get the L2Skill template corresponding to the skillID received from the client
		L2Skill skill = SkillTable.getInstance().getInfo(_magicId, level);

		// Check the validity of the skill
		if (skill != null)
		{
			if (skill.getSkillType() != L2SkillType.STRSIEGEASSAULT && activeChar.isMounted())
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			if (skill.getSkillType() != L2SkillType.TRANSFORMDISPEL &&
					(activeChar.isTransformed() || activeChar.isInStance()) &&
					(!activeChar.containsAllowedTransformSkill(skill.getId()) ||
							activeChar.getLastSkillCast() != null &&
									activeChar.getLastSkillCast().getSkillType() == L2SkillType.TRANSFORMDISPEL))
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			// Log.fine("	skill:"+skill.getName() + " level:"+skill.getLevel() + " passive:"+skill.isPassive());
			// Log.fine("	range:"+skill.getCastRange()+" targettype:"+skill.getTargetType()+" optype:"+skill.getOperateType()+" power:"+skill.getPower());
			// Log.fine("	reusedelay:"+skill.getReuseDelay()+" hittime:"+skill.getHitTime());
			// Log.fine("	currentState:"+activeChar.getCurrentState());	//for debug

			// If Alternate rule Karma punishment is set to true, forbid skill Return to player with Karma
			if (skill.getSkillType() == L2SkillType.RECALL && !Config.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT &&
					activeChar.getReputation() < 0)
			{
				return;
			}

			// players mounted on pets cannot use any toggle skills
			if (skill.isToggle() && activeChar.isMounted())
			{
				return;
			}

			if (activeChar.isGM())
			{
				GMAudit.auditGMAction(activeChar.getName(), "Use skill: " + skill.getName(),
						activeChar.getTarget() != null ? activeChar.getTarget().getName() : "No Target");
			}

			if (skill.isStanceSwitch())
			{
				int offset = activeChar.getElementalStance();
				if (offset > 4)
				{
					offset = 5;
				}

				if (skill.getId() == 11177)
				{
					offset = offset < 5 ? 0 : 12;
				}

				L2Skill magic = SkillTable.getInstance().getInfo(skill.getId() + offset, skill.getLevelHash());
				activeChar.useMagic(magic, _ctrlPressed, _shiftPressed);
				return;
			}

			if (activeChar.getQueuedSkill() != null && activeChar.getQueuedSkill().getSkillId() == 30001 &&
					skill.getId() != activeChar.getQueuedSkill().getSkillId())
			{
				activeChar.setQueuedSkill(null, _ctrlPressed, _shiftPressed);
			}

			// activeChar.stopMove();
			activeChar.useMagic(skill, _ctrlPressed, _shiftPressed);
		}
		else
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			Log.warning("No skill found with id " + _magicId + " and level " + level + " !!");
		}
	}
}
