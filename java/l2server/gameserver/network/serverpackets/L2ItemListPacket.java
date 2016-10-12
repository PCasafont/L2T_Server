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

import l2server.gameserver.templates.item.L2Item;

/**
 * @author Pere
 */
public abstract class L2ItemListPacket extends L2GameServerPacket
{
	public interface ItemInstanceInfo
	{
		int getObjectId();

		L2Item getItem();

		int getLocationSlot();

		long getCount();

		boolean isEquipped();

		int getEnchantLevel();

		int getMana();

		int getRemainingTime();

		boolean isSoulEnhanced();

		int[] getEnsoulEffectIds();

		int[] getEnsoulSpecialEffectIds();

		boolean isAugmented();

		long getAugmentationBonus();

		boolean isElementEnchanted();

		byte getAttackElementType();

		int getAttackElementPower();

		int getElementDefAttr(byte i);

		int getAppearance();
	}

	protected void writeItem(ItemInstanceInfo item)
	{
		byte mask = 0x00;
		if (item.isAugmented())
		{
			mask |= 0x01;
		}
		if (item.isElementEnchanted())
		{
			mask |= 0x02;
		}
		if (item.getAppearance() != 0)
		{
			mask |= 0x08;
		}
		if (item.isSoulEnhanced())
		{
			mask |= 0x10;
		}

		writeC(mask); // mask

		writeD(item.getObjectId());
		writeD(item.getItem().getItemId());
		writeC(item.getLocationSlot());
		writeQ(item.getCount());
		writeH(item.getItem().getType2()); // item type2
		writeH(item.isEquipped() ? 0x01 : 0x00);
		writeQ(item.getItem().getBodyPart());
		writeH(item.getEnchantLevel()); // enchant level
		writeD(item.getMana());
		writeD(item.getRemainingTime());

		writeC(0x01); // ???

		if (item.isAugmented())
		{
			writeQ(item.getAugmentationBonus());
		}

		if (item.isElementEnchanted())
		{
			writeH(item.getAttackElementType());
			writeH(item.getAttackElementPower());
			for (byte i = 0; i < 6; i++)
			{
				writeH(item.getElementDefAttr(i));
			}
		}

		// Enchant Effects
		//writeD(0x00);
		//writeD(0x00);
		//writeD(0x00);

		if (item.getAppearance() != 0)
		{
			writeD(item.getAppearance());
		}

		// Big crap to avoid having 2 different arrays
		if (item.isSoulEnhanced())
		{
			int[] ensoulEffects = item.getEnsoulEffectIds();
			int[] ensoulSpecialEffects = item.getEnsoulSpecialEffectIds();
			writeC(ensoulEffects.length);
			for (int effect : ensoulEffects)
			{
				writeD(effect);
			}
			writeC(ensoulSpecialEffects.length);
			for (int effect : ensoulSpecialEffects)
			{
				writeD(effect);
			}
		}
	}
}
