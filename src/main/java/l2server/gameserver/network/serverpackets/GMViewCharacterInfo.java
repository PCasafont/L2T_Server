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
import l2server.gameserver.model.actor.instance.Player;
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
public class GMViewCharacterInfo extends L2GameServerPacket {
	private Player activeChar;
	
	/**
	 */
	public GMViewCharacterInfo(Player character) {
		activeChar = character;
	}
	
	@Override
	protected final void writeImpl() {
		float moveMultiplier = activeChar.getMovementSpeedMultiplier();
		int runSpd = (int) activeChar.getTemplate().baseRunSpd;
		int walkSpd = (int) activeChar.getTemplate().baseWalkSpd;
		
		writeD(activeChar.getX());
		writeD(activeChar.getY());
		writeD(activeChar.getZ());
		writeD(activeChar.getHeading());
		writeD(activeChar.getObjectId());
		writeS(activeChar.getName());
		writeD(activeChar.getRace().ordinal());
		writeD(activeChar.getAppearance().getSex() ? 1 : 0);
		writeD(activeChar.getCurrentClass().getId());
		writeD(activeChar.getLevel());
		writeQ(activeChar.getExp());
		writeF(Experience.getExpPercent(activeChar.getLevel(), activeChar.getExp())); // High Five exp %
		writeD(activeChar.getSTR());
		writeD(activeChar.getDEX());
		writeD(activeChar.getCON());
		writeD(activeChar.getINT());
		writeD(activeChar.getWIT());
		writeD(activeChar.getMEN());
		writeD(activeChar.getLUC());
		writeD(activeChar.getCHA());
		writeD(activeChar.getMaxVisibleHp());
		writeD((int) activeChar.getCurrentHp());
		writeD(activeChar.getMaxMp());
		writeD((int) activeChar.getCurrentMp());
		writeQ(activeChar.getSp());
		writeD(activeChar.getCurrentLoad());
		writeD(activeChar.getMaxLoad());
		writeD(activeChar.getPkKills());
		
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HAIR));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_REAR));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LEAR));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_NECK));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RFINGER));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LFINGER));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HEAD));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_GLOVES));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_CHEST));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LEGS));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_FEET));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_CLOAK));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HAIR));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HAIR2));
		// T1 new D's
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RBRACELET));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LBRACELET));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_DECO1));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_DECO2));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_DECO3));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_DECO4));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_DECO5));
		writeD(activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_DECO6));
		writeD(0); // T3 Unknown
		// end of T1 new D's
		
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_REAR));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEAR));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_NECK));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RFINGER));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LFINGER));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_FEET));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_CLOAK));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIR2));
		// T1 new D's
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RBRACELET));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LBRACELET));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_DECO1));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_DECO2));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_DECO3));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_DECO4));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_DECO5));
		writeD(activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_DECO6));
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
		writeQ(activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_RHAND));
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
		writeQ(activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_RHAND));
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
		
		writeD(activeChar.getPAtk(null));
		writeD(activeChar.getPAtkSpd());
		writeD(activeChar.getPDef(null));
		writeD(activeChar.getEvasionRate(null));
		writeD(activeChar.getAccuracy());
		writeD(activeChar.getCriticalHit(null, null));
		writeD(activeChar.getMAtk(null, null));
		
		writeD(activeChar.getMAtkSpd());
		writeD(activeChar.getPAtkSpd());
		
		writeD(activeChar.getMDef(null, null));
		
		writeD(activeChar.getPvpFlag()); // 0-non-pvp  1-pvp = violett name
		writeD(activeChar.getReputation());
		
		writeD(runSpd);
		writeD(walkSpd);
		writeD(runSpd); // swimspeed
		writeD(walkSpd); // swimspeed
		writeD(runSpd);
		writeD(walkSpd);
		writeD(runSpd);
		writeD(walkSpd);
		writeF(moveMultiplier);
		writeF(activeChar.getAttackSpeedMultiplier()); //2.9);//
		writeF(activeChar.getCollisionRadius()); // scale
		writeF(activeChar.getCollisionHeight()); // y offset ??!? fem dwarf 4033
		writeD(activeChar.getAppearance().getHairStyle());
		writeD(activeChar.getAppearance().getHairColor());
		writeD(activeChar.getAppearance().getFace());
		writeD(activeChar.isGM() ? 0x01 : 0x00); // builder level
		
		writeS(activeChar.getTitle());
		writeD(activeChar.getClanId()); // pledge id
		writeD(activeChar.getClanCrestId()); // pledge crest id
		writeD(activeChar.getAllyId()); // ally id
		writeC(activeChar.getMountType()); // mount type
		writeC(activeChar.getPrivateStoreType() != Player.STORE_PRIVATE_CUSTOM_SELL ? activeChar.getPrivateStoreType() :
				Player.STORE_PRIVATE_SELL);
		writeC(activeChar.hasDwarvenCraft() ? 1 : 0);
		writeD(activeChar.getPkKills());
		writeD(activeChar.getPvpKills());
		
		writeH(activeChar.getRecomLeft());
		writeH(activeChar.getRecomHave()); //Blue value for name (0 = white, 255 = pure blue)
		writeD(activeChar.getCurrentClass().getId());
		writeD(0x00); // special effects? circles around player...
		writeD(activeChar.getMaxCp());
		writeD((int) activeChar.getCurrentCp());
		
		writeC(activeChar.isRunning() ? 0x01 : 0x00); //changes the Speed display on Status Window
		
		writeC(321);
		
		writeD(activeChar.getPledgeClass()); //changes the text above CP on Status Window
		
		writeC(activeChar.isNoble() ? 0x01 : 0x00);
		writeC(activeChar.isHero() ? 0x01 : 0x00);
		
		writeD(activeChar.getAppearance().getNameColor());
		writeD(activeChar.getAppearance().getTitleColor());
		
		byte attackAttribute = activeChar.getAttackElement();
		writeH(attackAttribute);
		writeH(activeChar.getAttackElementValue(attackAttribute));
		writeH(activeChar.getDefenseElementValue(Elementals.FIRE));
		writeH(activeChar.getDefenseElementValue(Elementals.WATER));
		writeH(activeChar.getDefenseElementValue(Elementals.WIND));
		writeH(activeChar.getDefenseElementValue(Elementals.EARTH));
		writeH(activeChar.getDefenseElementValue(Elementals.HOLY));
		writeH(activeChar.getDefenseElementValue(Elementals.DARK));
		
		writeD(activeChar.getAgathionId());
		
		writeD(activeChar.getFame());
		writeD(activeChar.getVitalityPoints());
	}
}
