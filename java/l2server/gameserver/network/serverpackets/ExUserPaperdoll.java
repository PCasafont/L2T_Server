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

package l2server.gameserver.network.serverpackets;

import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.itemcontainer.Inventory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author Pere
 */
public final class ExUserPaperdoll extends L2GameServerPacket
{
	private int _objectId;
	private byte[] _data;

	public ExUserPaperdoll(L2PcInstance character)
	{
		_objectId = character.getObjectId();
		int airShipHelm = 0;
		if (character.isInAirShip() && character.getAirShip().isCaptain(character))
		{
			airShipHelm = character.getAirShip().getHelmItemId();
		}

		ByteBuffer buffer = ByteBuffer.allocate(1000).order(ByteOrder.LITTLE_ENDIAN);

		// Write data to the buffer
		buffer.putShort((short) 0x21);

		// Mask
		buffer.put((byte) 0xff);
		buffer.put((byte) 0xff);
		buffer.put((byte) 0xff);
		buffer.put((byte) 0xff);
		buffer.put((byte) 0xff);

		// Underwear
		buffer.putShort((short) 22);
		buffer.putInt(character.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_UNDER));
		buffer.putInt(character.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_UNDER));
		buffer.putLong(character.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_UNDER));
		buffer.putInt(character.getInventory().getPaperdollAppearance(Inventory.PAPERDOLL_UNDER));

		// Right earring
		buffer.putShort((short) 22);
		buffer.putInt(character.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_REAR));
		buffer.putInt(character.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_REAR));
		buffer.putLong(character.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_REAR));
		buffer.putInt(character.getInventory().getPaperdollAppearance(Inventory.PAPERDOLL_REAR));

		// Left earring
		buffer.putShort((short) 22);
		buffer.putInt(character.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LEAR));
		buffer.putInt(character.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEAR));
		buffer.putLong(character.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_LEAR));
		buffer.putInt(character.getInventory().getPaperdollAppearance(Inventory.PAPERDOLL_LEAR));

		// Necklace
		buffer.putShort((short) 22);
		buffer.putInt(character.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_NECK));
		buffer.putInt(character.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_NECK));
		buffer.putLong(character.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_NECK));
		buffer.putInt(character.getInventory().getPaperdollAppearance(Inventory.PAPERDOLL_NECK));

		// Right ring
		buffer.putShort((short) 22);
		buffer.putInt(character.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RFINGER));
		buffer.putInt(character.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RFINGER));
		buffer.putLong(character.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_RFINGER));
		buffer.putInt(character.getInventory().getPaperdollAppearance(Inventory.PAPERDOLL_RFINGER));

		// Left ring
		buffer.putShort((short) 22);
		buffer.putInt(character.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LFINGER));
		buffer.putInt(character.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LFINGER));
		buffer.putLong(character.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_LFINGER));
		buffer.putInt(character.getInventory().getPaperdollAppearance(Inventory.PAPERDOLL_LFINGER));

		// Helmet
		buffer.putShort((short) 22);
		buffer.putInt(character.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HEAD));
		buffer.putInt(character.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
		buffer.putLong(character.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_HEAD));
		buffer.putInt(character.getInventory().getPaperdollAppearance(Inventory.PAPERDOLL_HEAD));

		if (airShipHelm == 0)
		{
			// Right hand
			buffer.putShort((short) 22);
			buffer.putInt(character.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
			buffer.putInt(character.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
			buffer.putLong(character.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_RHAND));
			buffer.putInt(character.getInventory().getPaperdollAppearance(Inventory.PAPERDOLL_RHAND));

			// Left hand
			buffer.putShort((short) 22);
			buffer.putInt(character.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND));
			buffer.putInt(character.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
			buffer.putLong(character.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_LHAND));
			buffer.putInt(character.getInventory().getPaperdollAppearance(Inventory.PAPERDOLL_LHAND));
		}
		else
		{
			// Right hand
			buffer.putShort((short) 22);
			buffer.putInt(0);
			buffer.putInt(0);
			buffer.putInt(0);
			buffer.putInt(0);

			// Left hand
			buffer.putShort((short) 22);
			buffer.putInt(0);
			buffer.putInt(0);
			buffer.putInt(0);
			buffer.putInt(0);
		}

		// Gloves
		buffer.putShort((short) 22);
		buffer.putInt(character.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_GLOVES));
		buffer.putInt(character.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
		buffer.putLong(character.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_GLOVES));
		buffer.putInt(character.getInventory().getPaperdollAppearance(Inventory.PAPERDOLL_GLOVES));

		// Chest
		buffer.putShort((short) 22);
		buffer.putInt(character.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_CHEST));
		buffer.putInt(character.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
		buffer.putLong(character.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_CHEST));
		buffer.putInt(character.getInventory().getPaperdollAppearance(Inventory.PAPERDOLL_CHEST));

		// Legs
		buffer.putShort((short) 22);
		buffer.putInt(character.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LEGS));
		buffer.putInt(character.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
		buffer.putLong(character.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_LEGS));
		buffer.putInt(character.getInventory().getPaperdollAppearance(Inventory.PAPERDOLL_LEGS));

		// Feet
		buffer.putShort((short) 22);
		buffer.putInt(character.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_FEET));
		buffer.putInt(character.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_FEET));
		buffer.putLong(character.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_FEET));
		buffer.putInt(character.getInventory().getPaperdollAppearance(Inventory.PAPERDOLL_FEET));

		// Cloak
		buffer.putShort((short) 22);
		buffer.putInt(character.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_CLOAK));
		int cloakApp = character.getInventory().getPaperdollAppearance(Inventory.PAPERDOLL_CLOAK);
		if (character.isCloakHidden())
		{
			buffer.putInt(0);
		}
		else if (cloakApp != 0)
		{
			buffer.putInt(cloakApp);
		}
		else
		{
			buffer.putInt(character.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_CLOAK));
		}
		buffer.putLong(character.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_CLOAK));
		buffer.putInt(character.getInventory().getPaperdollAppearance(Inventory.PAPERDOLL_CLOAK));

		// Right hand
		buffer.putShort((short) 22);
		buffer.putInt(character.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
		buffer.putInt(character.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
		buffer.putLong(character.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_RHAND));
		buffer.putInt(character.getInventory().getPaperdollAppearance(Inventory.PAPERDOLL_RHAND));

		// Hair
		buffer.putShort((short) 22);
		buffer.putInt(character.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HAIR));
		buffer.putInt(character.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
		buffer.putLong(character.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_HAIR));
		buffer.putInt(character.getInventory().getPaperdollAppearance(Inventory.PAPERDOLL_HAIR));

		// Hair 2
		buffer.putShort((short) 22);
		buffer.putInt(character.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HAIR2));
		buffer.putInt(character.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIR2));
		buffer.putLong(character.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_HAIR2));
		buffer.putInt(character.getInventory().getPaperdollAppearance(Inventory.PAPERDOLL_HAIR2));

		// Right bracelet
		buffer.putShort((short) 22);
		buffer.putInt(character.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RBRACELET));
		buffer.putInt(character.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RBRACELET));
		buffer.putLong(character.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_RBRACELET));
		buffer.putInt(character.getInventory().getPaperdollAppearance(Inventory.PAPERDOLL_RBRACELET));

		// Left bracelet
		buffer.putShort((short) 22);
		buffer.putInt(character.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LBRACELET));
		buffer.putInt(character.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LBRACELET));
		buffer.putLong(character.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_LBRACELET));
		buffer.putInt(character.getInventory().getPaperdollAppearance(Inventory.PAPERDOLL_LBRACELET));

		// Deco 1
		buffer.putShort((short) 22);
		buffer.putInt(character.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_DECO1));
		buffer.putInt(character.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_DECO1));
		buffer.putLong(character.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO1));
		buffer.putInt(character.getInventory().getPaperdollAppearance(Inventory.PAPERDOLL_DECO1));

		// Deco 2
		buffer.putShort((short) 22);
		buffer.putInt(character.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_DECO2));
		buffer.putInt(character.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_DECO2));
		buffer.putLong(character.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO2));
		buffer.putInt(character.getInventory().getPaperdollAppearance(Inventory.PAPERDOLL_DECO2));

		// Deco 3
		buffer.putShort((short) 22);
		buffer.putInt(character.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_DECO3));
		buffer.putInt(character.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_DECO3));
		buffer.putLong(character.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO3));
		buffer.putInt(character.getInventory().getPaperdollAppearance(Inventory.PAPERDOLL_DECO3));

		// Deco 4
		buffer.putShort((short) 22);
		buffer.putInt(character.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_DECO4));
		buffer.putInt(character.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_DECO4));
		buffer.putLong(character.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO4));
		buffer.putInt(character.getInventory().getPaperdollAppearance(Inventory.PAPERDOLL_DECO4));

		// Deco 5
		buffer.putShort((short) 22);
		buffer.putInt(character.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_DECO5));
		buffer.putInt(character.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_DECO5));
		buffer.putLong(character.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO5));
		buffer.putInt(character.getInventory().getPaperdollAppearance(Inventory.PAPERDOLL_DECO5));

		// Deco 6
		buffer.putShort((short) 22);
		buffer.putInt(character.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_DECO6));
		buffer.putInt(character.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_DECO6));
		buffer.putLong(character.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO6));
		buffer.putInt(character.getInventory().getPaperdollAppearance(Inventory.PAPERDOLL_DECO6));

		// Belt
		buffer.putShort((short) 22);
		buffer.putInt(character.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_BELT));
		buffer.putInt(character.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_BELT));
		buffer.putLong(character.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_BELT));
		buffer.putInt(character.getInventory().getPaperdollAppearance(Inventory.PAPERDOLL_BELT));

		// ???
		buffer.putShort((short) 22);
		buffer.putInt(character.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_BROOCH));
		buffer.putInt(character.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_BROOCH));
		buffer.putLong(character.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_BROOCH));
		buffer.putInt(character.getInventory().getPaperdollAppearance(Inventory.PAPERDOLL_BROOCH));

		// ???
		buffer.putShort((short) 22);
		buffer.putInt(character.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_JEWELRY1));
		buffer.putInt(character.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_JEWELRY1));
		buffer.putLong(character.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_JEWELRY1));
		buffer.putInt(character.getInventory().getPaperdollAppearance(Inventory.PAPERDOLL_JEWELRY1));

		// ???
		buffer.putShort((short) 22);
		buffer.putInt(character.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_JEWELRY2));
		buffer.putInt(character.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_JEWELRY2));
		buffer.putLong(character.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_JEWELRY2));
		buffer.putInt(character.getInventory().getPaperdollAppearance(Inventory.PAPERDOLL_JEWELRY2));

		// ???
		buffer.putShort((short) 22);
		buffer.putInt(character.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_JEWELRY3));
		buffer.putInt(character.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_JEWELRY3));
		buffer.putLong(character.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_JEWELRY3));
		buffer.putInt(character.getInventory().getPaperdollAppearance(Inventory.PAPERDOLL_JEWELRY3));

		// ???
		buffer.putShort((short) 22);
		buffer.putInt(character.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_JEWELRY4));
		buffer.putInt(character.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_JEWELRY4));
		buffer.putLong(character.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_JEWELRY4));
		buffer.putInt(character.getInventory().getPaperdollAppearance(Inventory.PAPERDOLL_JEWELRY4));

		// ???
		buffer.putShort((short) 22);
		buffer.putInt(character.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_JEWELRY5));
		buffer.putInt(character.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_JEWELRY5));
		buffer.putLong(character.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_JEWELRY5));
		buffer.putInt(character.getInventory().getPaperdollAppearance(Inventory.PAPERDOLL_JEWELRY5));

		// ???
		buffer.putShort((short) 22);
		buffer.putInt(character.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_JEWELRY6));
		buffer.putInt(character.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_JEWELRY6));
		buffer.putLong(character.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_JEWELRY6));
		buffer.putInt(character.getInventory().getPaperdollAppearance(Inventory.PAPERDOLL_JEWELRY6));

		int size = buffer.position();
		buffer.position(0);
		_data = new byte[size];
		buffer.get(_data, 0, size);
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_objectId);
		writeB(_data);
	}
}
