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

import l2server.gameserver.model.Elementals;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.base.Experience;
import l2server.gameserver.model.itemcontainer.Inventory;

/**
 * TODO Add support for Eval. Score
 * <p>
 * dddddSdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddffffddddSddd   rev420
 * dddddSdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddffffddddSdddcccddhh  rev478
 * dddddSdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddffffddddSdddcccddhhddd rev551
 *
 * @version $Revision: 1.2.2.2.2.8 $ $Date: 2005/03/27 15:29:39 $
 */
public class GMViewCharacterInfo extends L2GameServerPacket
{
	private L2PcInstance activeChar;

	/**
	 */
	public GMViewCharacterInfo(L2PcInstance character)
	{
		this.activeChar = character;
	}

	@Override
	protected final void writeImpl()
	{
		float moveMultiplier = this.activeChar.getMovementSpeedMultiplier();
		int runSpd = (int) this.activeChar.getTemplate().baseRunSpd;
		int walkSpd = (int) this.activeChar.getTemplate().baseWalkSpd;

		writeD(this.activeChar.getX());
		writeD(this.activeChar.getY());
		writeD(this.activeChar.getZ());
		writeD(this.activeChar.getHeading());
		writeD(this.activeChar.getObjectId());
		writeS(this.activeChar.getName());
		writeD(this.activeChar.getRace().ordinal());
		writeD(this.activeChar.getAppearance().getSex() ? 1 : 0);
		writeD(this.activeChar.getCurrentClass().getId());
		writeD(this.activeChar.getLevel());
		writeQ(this.activeChar.getExp());
		writeF(Experience.getExpPercent(this.activeChar.getLevel(), this.activeChar.getExp())); // High Five exp %
		writeD(this.activeChar.getSTR());
		writeD(this.activeChar.getDEX());
		writeD(this.activeChar.getCON());
		writeD(this.activeChar.getINT());
		writeD(this.activeChar.getWIT());
		writeD(this.activeChar.getMEN());
		writeD(this.activeChar.getLUC());
		writeD(this.activeChar.getCHA());
		writeD(this.activeChar.getMaxVisibleHp());
		writeD((int) this.activeChar.getCurrentHp());
		writeD(this.activeChar.getMaxMp());
		writeD((int) this.activeChar.getCurrentMp());
		writeQ(this.activeChar.getSp());
		writeD(this.activeChar.getCurrentLoad());
		writeD(this.activeChar.getMaxLoad());
		writeD(this.activeChar.getPkKills());

		writeD(this.activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HAIR));
		writeD(this.activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_REAR));
		writeD(this.activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LEAR));
		writeD(this.activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_NECK));
		writeD(this.activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RFINGER));
		writeD(this.activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LFINGER));
		writeD(this.activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HEAD));
		writeD(this.activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
		writeD(this.activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND));
		writeD(this.activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_GLOVES));
		writeD(this.activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_CHEST));
		writeD(this.activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LEGS));
		writeD(this.activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_FEET));
		writeD(this.activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_CLOAK));
		writeD(this.activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
		writeD(this.activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HAIR));
		writeD(this.activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HAIR2));
		// T1 new D's
		writeD(this.activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RBRACELET));
		writeD(this.activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LBRACELET));
		writeD(this.activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_DECO1));
		writeD(this.activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_DECO2));
		writeD(this.activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_DECO3));
		writeD(this.activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_DECO4));
		writeD(this.activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_DECO5));
		writeD(this.activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_DECO6));
		writeD(0); // T3 Unknown
		// end of T1 new D's

		writeD(this.activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
		writeD(this.activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_REAR));
		writeD(this.activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEAR));
		writeD(this.activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_NECK));
		writeD(this.activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RFINGER));
		writeD(this.activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LFINGER));
		writeD(this.activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
		writeD(this.activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
		writeD(this.activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
		writeD(this.activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
		writeD(this.activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
		writeD(this.activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
		writeD(this.activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_FEET));
		writeD(this.activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_CLOAK));
		writeD(this.activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
		writeD(this.activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
		writeD(this.activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIR2));
		// T1 new D's
		writeD(this.activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RBRACELET));
		writeD(this.activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LBRACELET));
		writeD(this.activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_DECO1));
		writeD(this.activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_DECO2));
		writeD(this.activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_DECO3));
		writeD(this.activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_DECO4));
		writeD(this.activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_DECO5));
		writeD(this.activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_DECO6));
		writeD(0); // T3 Unknown
		writeD(0); // T3 Unknown
		writeD(0); // T3 Unknown
		// end of T1 new D's

		// c6 new h's
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeQ(this.activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_RHAND));
		writeH(0x00);

		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeQ(this.activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_RHAND));
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		// end of c6 new h's

		// start of T1 new h's
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		// end of T1 new h's
		writeH(0x00);
		writeH(0x00);

		writeD(this.activeChar.getPAtk(null));
		writeD(this.activeChar.getPAtkSpd());
		writeD(this.activeChar.getPDef(null));
		writeD(this.activeChar.getEvasionRate(null));
		writeD(this.activeChar.getAccuracy());
		writeD(this.activeChar.getCriticalHit(null, null));
		writeD(this.activeChar.getMAtk(null, null));

		writeD(this.activeChar.getMAtkSpd());
		writeD(this.activeChar.getPAtkSpd());

		writeD(this.activeChar.getMDef(null, null));

		writeD(this.activeChar.getPvpFlag()); // 0-non-pvp  1-pvp = violett name
		writeD(this.activeChar.getReputation());

		writeD(runSpd);
		writeD(walkSpd);
		writeD(runSpd); // swimspeed
		writeD(walkSpd); // swimspeed
		writeD(runSpd);
		writeD(walkSpd);
		writeD(runSpd);
		writeD(walkSpd);
		writeF(moveMultiplier);
		writeF(this.activeChar.getAttackSpeedMultiplier()); //2.9);//
		writeF(this.activeChar.getCollisionRadius()); // scale
		writeF(this.activeChar.getCollisionHeight()); // y offset ??!? fem dwarf 4033
		writeD(this.activeChar.getAppearance().getHairStyle());
		writeD(this.activeChar.getAppearance().getHairColor());
		writeD(this.activeChar.getAppearance().getFace());
		writeD(this.activeChar.isGM() ? 0x01 : 0x00); // builder level

		writeS(this.activeChar.getTitle());
		writeD(this.activeChar.getClanId()); // pledge id
		writeD(this.activeChar.getClanCrestId()); // pledge crest id
		writeD(this.activeChar.getAllyId()); // ally id
		writeC(this.activeChar.getMountType()); // mount type
		writeC(this.activeChar.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_CUSTOM_SELL ?
				this.activeChar.getPrivateStoreType() : L2PcInstance.STORE_PRIVATE_SELL);
		writeC(this.activeChar.hasDwarvenCraft() ? 1 : 0);
		writeD(this.activeChar.getPkKills());
		writeD(this.activeChar.getPvpKills());

		writeH(this.activeChar.getRecomLeft());
		writeH(this.activeChar.getRecomHave()); //Blue value for name (0 = white, 255 = pure blue)
		writeD(this.activeChar.getCurrentClass().getId());
		writeD(0x00); // special effects? circles around player...
		writeD(this.activeChar.getMaxCp());
		writeD((int) this.activeChar.getCurrentCp());

		writeC(this.activeChar.isRunning() ? 0x01 : 0x00); //changes the Speed display on Status Window

		writeC(321);

		writeD(this.activeChar.getPledgeClass()); //changes the text above CP on Status Window

		writeC(this.activeChar.isNoble() ? 0x01 : 0x00);
		writeC(this.activeChar.isHero() ? 0x01 : 0x00);

		writeD(this.activeChar.getAppearance().getNameColor());
		writeD(this.activeChar.getAppearance().getTitleColor());

		byte attackAttribute = this.activeChar.getAttackElement();
		writeH(attackAttribute);
		writeH(this.activeChar.getAttackElementValue(attackAttribute));
		writeH(this.activeChar.getDefenseElementValue(Elementals.FIRE));
		writeH(this.activeChar.getDefenseElementValue(Elementals.WATER));
		writeH(this.activeChar.getDefenseElementValue(Elementals.WIND));
		writeH(this.activeChar.getDefenseElementValue(Elementals.EARTH));
		writeH(this.activeChar.getDefenseElementValue(Elementals.HOLY));
		writeH(this.activeChar.getDefenseElementValue(Elementals.DARK));

		writeD(this.activeChar.getAgathionId());

		writeD(this.activeChar.getFame());
		writeD(this.activeChar.getVitalityPoints());
	}
}
