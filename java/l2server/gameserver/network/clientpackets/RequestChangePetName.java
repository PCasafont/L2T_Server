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

import l2server.gameserver.datatables.PetNameTable;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2PetInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 *
 * @version $Revision: 1.3.4.4 $ $Date: 2005/04/06 16:13:48 $
 */
public final class RequestChangePetName extends L2GameClientPacket
{
	//

	private String _name;

	@Override
	protected void readImpl()
	{
		_name = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		final L2PetInstance pet = activeChar.getPet();
		if (pet == null)
		{
			return;
		}

		if (pet.getName() != null)
		{
			activeChar
					.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NAMING_YOU_CANNOT_SET_NAME_OF_THE_PET));
			return;
		}
		else if (PetNameTable.getInstance().doesPetNameExist(_name, pet.getTemplate().NpcId))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NAMING_ALREADY_IN_USE_BY_ANOTHER_PET));
			return;
		}
		else if (_name.length() < 3 || _name.length() > 16)
		{
			// SystemMessage sm = SystemMessage.getSystemMessage(SystemMessage.NAMING_PETNAME_UP_TO_8CHARS);
			activeChar.sendMessage("Your pet's name can be up to 16 characters.");
			return;
		}
		else if (!PetNameTable.getInstance().isValidPetName(_name))
		{
			activeChar
					.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NAMING_PETNAME_CONTAINS_INVALID_CHARS));
			return;
		}

		pet.setName(_name);
		pet.updateAndBroadcastStatus(1);
	}
}
