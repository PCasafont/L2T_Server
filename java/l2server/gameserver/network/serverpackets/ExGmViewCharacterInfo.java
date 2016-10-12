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

import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.instancemanager.RaidBossPointsManager;
import l2server.gameserver.model.Elementals;
import l2server.gameserver.model.L2Transformation;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.base.Experience;
import l2server.gameserver.templates.chars.L2NpcTemplate;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author Pere
 */
public final class ExGmViewCharacterInfo extends L2GameServerPacket
{
	private int _objectId;
	private byte[] _data;

	public ExGmViewCharacterInfo(L2PcInstance player)
	{
		_objectId = player.getObjectId();

		ByteBuffer buffer = ByteBuffer.allocate(500).order(ByteOrder.LITTLE_ENDIAN);

		// Write data to the buffer
		buffer.putShort((short) 0x17);

		// Mask
		buffer.put((byte) 0xff);
		buffer.put((byte) 0xff);
		buffer.put((byte) 0xfe);

		int relation = 0x00;
		if (player.getClan() != null)
		{
			relation |= 0x20;
			if (player.isClanLeader())
			{
				relation |= 0x40;
			}
		}
		if (player.getSiegeState() == 1)
		{
			relation |= 0x1000;
		}
		buffer.putInt(relation);

		// Basic info
		buffer.putShort((short) (player.getName().length() * 2 + 16));
		buffer.putShort((short) player.getName().length());
		for (char c : player.getName().toCharArray())
		{
			buffer.putShort((short) c);
		}
		buffer.put((byte) (player.isGM() ? 1 : 0));
		buffer.put((byte) player.getVisibleTemplate().race.ordinal());
		buffer.put((byte) (player.getAppearance().getSex() ? 1 : 0));
		buffer.putInt(player.getVisibleTemplate().startingClassId);
		buffer.putInt(player.getCurrentClass() != null ? player.getCurrentClass().getId() : 0);
		buffer.put((byte) player.getLevel());

		// Base stats
		buffer.putShort((short) 18);
		buffer.putShort((short) player.getSTR());
		buffer.putShort((short) player.getDEX());
		buffer.putShort((short) player.getCON());
		buffer.putShort((short) player.getINT());
		buffer.putShort((short) player.getWIT());
		buffer.putShort((short) player.getMEN());
		buffer.putShort((short) player.getLUC()); // LUC
		buffer.putShort((short) player.getCHA()); // CHA

		// Max stats
		buffer.putShort((short) 14);
		buffer.putInt(player.getMaxVisibleHp());
		buffer.putInt(player.getMaxMp());
		buffer.putInt(player.getMaxCp());

		// Current stats
		buffer.putShort((short) 38);
		buffer.putInt((int) player.getCurrentHp());
		buffer.putInt((int) player.getCurrentMp());
		buffer.putInt((int) player.getCurrentCp());
		buffer.putLong(player.getSp());
		buffer.putLong(player.getExp());
		buffer.putDouble(Experience.getExpPercent(player.getLevel(), player.getExp()));

		// Enchant effect
		buffer.putShort((short) 4);
		int airShipHelm = 0;
		if (player.isInAirShip() && player.getAirShip().isCaptain(player))
		{
			airShipHelm = player.getAirShip().getHelmItemId();
		}
		buffer.put((byte) (player.isMounted() || airShipHelm != 0 ? 0 : player.getEnchantEffect()));
		buffer.put((byte) player.getArmorEnchant());

		// Appearance
		buffer.putShort((short) 15);
		buffer.putInt(player.getAppearance().getHairStyle());
		buffer.putInt(player.getAppearance().getHairColor());
		buffer.putInt(player.getAppearance().getFace());
		buffer.put((byte) (player.isShowingHat() ? 1 : 0));

		// Unknown
		buffer.putShort((short) 6);
		buffer.put((byte) player.getMountType());
		buffer.put((byte) (player.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_CUSTOM_SELL ?
				player.getPrivateStoreType() : L2PcInstance.STORE_PRIVATE_SELL));
		buffer.put((byte) (player.canCrystallize() ? 1 : 0));
		buffer.put((byte) player.getSpentAbilityPoints());

		// Stats
		buffer.putShort((short) 56);
		buffer.putShort((short) 40);
		buffer.putInt(player.getPAtk(null));
		buffer.putInt(player.getPAtkSpd());
		buffer.putInt(player.getPDef(null));
		buffer.putInt(player.getEvasionRate(null));
		buffer.putInt(player.getAccuracy());
		buffer.putInt(player.getCriticalHit(null, null));
		buffer.putInt(player.getMAtk(null, null));
		buffer.putInt(player.getMAtkSpd());
		buffer.putInt(player.getPAtkSpd());
		buffer.putInt(player.getMEvasionRate(null));
		buffer.putInt(player.getMDef(null, null));
		buffer.putInt(player.getMAccuracy());
		buffer.putInt(player.getMCriticalHit(null, null));

		// Element resistances
		buffer.putShort((short) 14);
		buffer.putShort((short) player.getDefenseElementValue(Elementals.FIRE));
		buffer.putShort((short) player.getDefenseElementValue(Elementals.WATER));
		buffer.putShort((short) player.getDefenseElementValue(Elementals.WIND));
		buffer.putShort((short) player.getDefenseElementValue(Elementals.EARTH));
		buffer.putShort((short) player.getDefenseElementValue(Elementals.HOLY));
		buffer.putShort((short) player.getDefenseElementValue(Elementals.DARK));

		// Position
		buffer.putShort((short) 18);
		buffer.putInt(player.getX());
		buffer.putInt(player.getY());
		buffer.putInt(player.getZ());
		buffer.putInt(player.getHeading());

		// Speeds
		float moveMultiplier = player.getMovementSpeedMultiplier();
		short runSpd = (short) player.getTemplate().baseRunSpd;
		short walkSpd = (short) player.getTemplate().baseWalkSpd;
		buffer.putShort((short) 18);
		buffer.putShort(runSpd);
		buffer.putShort(walkSpd);
		buffer.putShort(runSpd); // swim run speed
		buffer.putShort(walkSpd); // swim walk speed
		buffer.putShort((short) 0);
		buffer.putShort((short) 0);
		buffer.putShort(player.isFlying() ? runSpd : (short) 0); // fly speed
		buffer.putShort(player.isFlying() ? walkSpd : (short) 0); // fly speed

		// Multipliers
		buffer.putShort((short) 18);
		buffer.putDouble(moveMultiplier);
		buffer.putDouble(player.getAttackSpeedMultiplier());

		// Collisions
		buffer.putShort((short) 18);
		int mountNpcId = player.getMountNpcId();
		L2Transformation trans = player.getTransformation();
		if (trans != null)
		{
			buffer.putDouble(trans.getCollisionRadius());
			buffer.putDouble(trans.getCollisionHeight());
		}
		else if (player.getMountType() != 0 && mountNpcId != 0)
		{
			L2NpcTemplate mountTemplate = NpcTable.getInstance().getTemplate(mountNpcId);
			buffer.putDouble(mountTemplate.fCollisionRadius);
			buffer.putDouble(mountTemplate.fCollisionHeight);
		}
		else
		{
			buffer.putDouble(player.getCollisionRadius());
			buffer.putDouble(player.getCollisionHeight());
		}

		// Atk Element
		buffer.putShort((short) 5);
		byte attackAttribute = player.getAttackElement();
		buffer.put(attackAttribute);
		buffer.putShort((short) player.getAttackElementValue(attackAttribute));

		// Clan
		String title = player.getTitle() != null ? player.getTitle() : "";

		if (player.getAppearance().getInvisible() && player.isGM())
		{
			title = "Invisible";
		}

		buffer.putShort((short) (title.length() * 2 + 32));
		buffer.putShort((short) title.length());
		for (char c : title.toCharArray())
		{
			buffer.putShort((short) c);
		}
		buffer.putShort((short) player.getPledgeType());
		buffer.putInt(player.getClanId());
		buffer.putInt(player.getClanCrestLargeId());
		buffer.putInt(player.getClanCrestId());
		buffer.putInt(player.getClanPrivileges());
		buffer.putShort((short) (player.isClanLeader() ? 1 : 0));
		buffer.putShort((short) 0);
		buffer.put((byte) 0);
		buffer.putInt(player.getAllyCrestId());
		buffer.put((byte) 0);

		// Social
		buffer.putShort((short) 22);
		buffer.put(player.getPvpFlag());
		buffer.putInt(player.getReputation());
		buffer.put((byte) (player.isNoble() ? 1 : 0));
		buffer.put((byte) (player.hasHeroAura() ? 1 : 0));
		buffer.put((byte) player.getPledgeClass());
		buffer.putInt(player.getPkKills());
		buffer.putInt(player.getPvpKills());
		buffer.putShort((short) player.getRecomLeft());
		buffer.putShort((short) player.getRecomHave());

		// Unknown
		buffer.putShort((short) 15);
		buffer.putInt(player.getVitalityPoints());
		buffer.put((byte) 0);
		buffer.putInt(player.getFame());
		buffer.putInt(RaidBossPointsManager.getInstance().getPointsByOwnerId(player.getObjectId())); // Raid points

		// Unknown
		buffer.putShort((short) 9);
		buffer.put((byte) player.getInventory().getMaxTalismanCount());
		buffer.put((byte) player.getInventory().getMaxJewelryCount());
		buffer.put((byte) player.getTeam());
		buffer.put(
				(byte) 0); //Player floor effects: 0 nothing, 1 red circle (intense), 2 white circle, 3 red circle (pale)
		buffer.put((byte) 0);
		buffer.put((byte) 0);
		buffer.put((byte) 0);

		// Movement flags
		buffer.putShort((short) 4);
		buffer.put((byte) (player.isFlying() ? 0x01 : 0x00)); // flying?
		buffer.put((byte) (player.isRunning() ? 0x01 : 0x00));

		// Colors
		buffer.putShort((short) 10);
		buffer.putInt(player.getAppearance().getNameColor());
		buffer.putInt(player.getAppearance().getTitleColor());

		// Unknown
		buffer.putShort((short) 9);
		buffer.putInt(player.getMountNpcId() > 0 ? player.getMountNpcId() + 1000000 : 0);
		buffer.putShort((short) player.getInventoryLimit());
		buffer.put((byte) 0);

		// Unknown
		buffer.putShort((short) 9);
		buffer.putInt(1);
		buffer.putShort((short) 0);
		buffer.put((byte) 0);

		int size = buffer.position();
		buffer.position(0);
		_data = new byte[size];
		buffer.get(_data, 0, size);
	}

	@Override
	protected final void writeImpl()
	{
		//writeH(0x155);

		writeD(_objectId);
		writeD(0);
		writeB(_data);
	}
}
