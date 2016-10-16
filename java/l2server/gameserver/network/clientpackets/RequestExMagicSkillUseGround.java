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

import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.ValidateLocation;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Point3D;

/**
 * Fromat:(ch) dddddc
 *
 * @author -Wooden-
 */
public final class RequestExMagicSkillUseGround extends L2GameClientPacket
{

	private int x;
	private int y;
	private int z;
	private int skillId;
	private boolean ctrlPressed;
	private boolean shiftPressed;

	@Override
	protected void readImpl()
	{
		x = readD();
		y = readD();
		z = readD();
		skillId = readD();
		ctrlPressed = readD() != 0;
		shiftPressed = readC() != 0;
	}

	/**
	 */
	@Override
	protected void runImpl()
	{
		// Get the current L2PcInstance of the player
		L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
		{
			return;
		}

		// Get the level of the used skill
		int level = activeChar.getSkillLevelHash(skillId);
		if (level <= 0)
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		// Get the L2Skill template corresponding to the skillID received from the client
		L2Skill skill = SkillTable.getInstance().getInfo(skillId, level);

		// Check the validity of the skill
		if (skill != null)
		{
			activeChar.setSkillCastPosition(new Point3D(x, y, z));

			// normally magicskilluse packet turns char client side but for these skills, it doesn't (even with correct target)
			activeChar.setHeading(Util.calculateHeadingFrom(activeChar.getX(), activeChar.getY(), x, y));
			activeChar.broadcastPacket(new ValidateLocation(activeChar));

			activeChar.useMagic(skill, ctrlPressed, shiftPressed);
		}
		else
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			Log.warning("No skill found with id " + skillId + " and level " + level + " !!");
		}
	}
}
