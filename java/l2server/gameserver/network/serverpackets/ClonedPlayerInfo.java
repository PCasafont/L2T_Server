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
import l2server.gameserver.events.instanced.EventInstance.EventType;
import l2server.gameserver.instancemanager.CursedWeaponsManager;
import l2server.gameserver.model.L2Transformation;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.itemcontainer.Inventory;
import l2server.gameserver.model.itemcontainer.PcInventory;
import l2server.gameserver.stats.VisualEffect;

import java.util.Set;

/**
 * 0000: 03 32 15 00 00 44 fe 00 00 80 f1 ff ff 00 00 00	.2...D..........<p>
 * 0010: 00 6b b4 c0 4a 45 00 6c 00 6c 00 61 00 6d 00 69	.k..JE.l.l.a.m.i<p>
 * 0020: 00 00 00 01 00 00 00 01 00 00 00 12 00 00 00 00	................<p>
 * 0030: 00 00 00 2a 00 00 00 42 00 00 00 71 02 00 00 31	...*...B...q...1<p>
 * 0040: 00 00 00 18 00 00 00 1f 00 00 00 25 00 00 00 00	...........%....<p>
 * 0050: 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 f9	................<p>
 * 0060: 00 00 00 b3 01 00 00 00 00 00 00 00 00 00 00 7d	...............}<p>
 * 0070: 00 00 00 5a 00 00 00 32 00 00 00 32 00 00 00 00	...Z...2...2....<p>
 * 0080: 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 67	...............g<p>
 * 0090: 66 66 66 66 66 f2 3f 5f 63 97 a8 de 1a f9 3f 00	fffff.?_c.....?.<p>
 * 00a0: 00 00 00 00 00 1e 40 00 00 00 00 00 00 37 40 01	.............7..<p>
 * 00b0: 00 00 00 01 00 00 00 01 00 00 00 00 00 c1 0c 00	................<p>
 * 00c0: 00 00 00 00 00 00 00 00 00 01 01 00 00 00 00 00	................<p>
 * 00d0: 00 00<p>
 * <p>
 * dddddSdddddddddddddddddddddddddddffffdddSdddccccccc (h)<p>
 * dddddSdddddddddddddddddddddddddddffffdddSdddddccccccch
 * dddddSddddddddddddddddddddddddddddffffdddSdddddccccccch (h) c (dchd) ddc dcc c cddd d
 * dddddSdddddddddddddddhhhhhhhhhhhhhhhhhhhhhhhhddddddddddddddffffdddSdddddccccccch [h] c (ddhd) ddc c ddc cddd d d dd d d d
 *
 * @version $Revision: 1.7.2.6.2.11 $ $Date: 2005/04/11 10:05:54 $
 */
public class ClonedPlayerInfo extends L2GameServerPacket
{
	private L2Npc npc;
	private L2PcInstance activeChar;
	private PcInventory inv;
	private int objId;
	private int x, y, z, heading;
	private int mAtkSpd, pAtkSpd;

	/**
	 * Run speed, swimming run speed and flying run speed
	 */
	private int runSpd;
	/**
	 * Walking speed, swimming walking speed and flying walking speed
	 */
	private int walkSpd;

	/**
	 */
	public ClonedPlayerInfo(L2Npc npc, L2PcInstance cha)
	{
		this.npc = npc;
		this.activeChar = cha;
		this.objId = this.npc.getObjectId();
		this.inv = cha.getInventory();
		this.x = this.npc.getX();
		this.y = this.npc.getY();
		this.z = this.npc.getZ();
		this.heading = this.npc.getHeading();
		this.mAtkSpd = this.activeChar.getMAtkSpd();
		this.pAtkSpd = this.activeChar.getPAtkSpd();
		this.runSpd = (int) this.activeChar.getTemplate().baseRunSpd;
		this.walkSpd = (int) this.activeChar.getTemplate().baseWalkSpd;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(this.x);
		writeD(this.y);
		writeD(this.z);
		writeD(0);
		writeD(this.objId);
		writeS(this.activeChar.getAppearance().getVisibleName());
		writeH(this.activeChar.getVisibleTemplate().race.ordinal());
		writeC(this.activeChar.getAppearance().getSex() ? 1 : 0);

		writeD(this.activeChar.getVisibleTemplate().startingClassId);

		writeD(this.inv.getPaperdollItemId(Inventory.PAPERDOLL_UNDER));
		writeD(this.inv.getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
		writeD(this.inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
		writeD(this.inv.getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
		writeD(this.inv.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
		writeD(this.inv.getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
		writeD(this.inv.getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
		writeD(this.inv.getPaperdollItemId(Inventory.PAPERDOLL_FEET));
		writeD(this.inv.getPaperdollItemId(Inventory.PAPERDOLL_CLOAK));
		writeD(this.inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
		writeD(this.inv.getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
		writeD(this.inv.getPaperdollItemId(Inventory.PAPERDOLL_HAIR2));

		// c6 new h's
		writeQ(this.inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_UNDER));
		writeQ(this.inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_HEAD));
		writeQ(this.inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_RHAND));

		writeC(this.activeChar.getArmorEnchant());

		// T1 new d's
		writeD(0);
		writeD(0);
		writeD(0x00);
		writeD(this.inv.getPaperdollAppearance(Inventory.PAPERDOLL_GLOVES));
		writeD(this.inv.getPaperdollAppearance(Inventory.PAPERDOLL_CHEST));
		writeD(this.inv.getPaperdollAppearance(Inventory.PAPERDOLL_LEGS));
		writeD(this.inv.getPaperdollAppearance(Inventory.PAPERDOLL_FEET));
		writeD(0x00);
		writeD(0x00);
		// end of t1 new d's

		writeC(this.activeChar.getPvpFlag());
		writeD(this.activeChar.getReputation());

		writeD(this.mAtkSpd);
		writeD(this.pAtkSpd);

		writeH(this.runSpd); // TODO: the order of the speeds should be confirmed
		writeH(this.walkSpd);
		writeH(this.runSpd); // swim run speed
		writeH(this.walkSpd); // swim walk speed
		writeH(this.runSpd); // fly run speed
		writeH(this.walkSpd); // fly walk speed
		writeH(this.runSpd); // fly run speed ?
		writeH(this.walkSpd); // fly walk speed ?
		writeF(this.activeChar.getMovementSpeedMultiplier()); // this.activeChar.getProperMultiplier()
		writeF(this.activeChar.getAttackSpeedMultiplier()); // this.activeChar.getAttackSpeedMultiplier()
		L2Transformation transform = this.activeChar.getTransformation();

		if (this.activeChar.getMountType() != 0)
		{
			writeF(NpcTable.getInstance().getTemplate(this.activeChar.getMountNpcId()).fCollisionRadius);
			writeF(NpcTable.getInstance().getTemplate(this.activeChar.getMountNpcId()).fCollisionHeight);
		}
		else if (transform != null)
		{
			writeF(transform.getCollisionRadius());
			writeF(transform.getCollisionHeight());
		}
		else
		{
			writeF(this.activeChar.getCollisionRadius());
			writeF(this.activeChar.getCollisionHeight());
		}

		writeD(this.activeChar.getAppearance().getHairStyle());
		writeD(this.activeChar.getAppearance().getHairColor());
		writeD(this.activeChar.getAppearance().getFace());

		writeS(this.activeChar.getAppearance().getVisibleTitle());

		if (!this.activeChar.isCursedWeaponEquipped() && !(this.activeChar.isPlayingEvent() &&
				(this.activeChar.getEvent().getType() == EventType.DeathMatch ||
						this.activeChar.getEvent().getType() == EventType.Survival ||
						this.activeChar.getEvent().getType() == EventType.KingOfTheHill)))
		{
			writeD(this.activeChar.getClanId());
			writeD(this.activeChar.getClanCrestId());
			writeD(this.activeChar.getAllyId());
			writeD(this.activeChar.getAllyCrestId());
		}
		else
		{
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
		}

		writeC(1); // standing = 1  sitting = 0
		writeC(this.npc.isRunning() ? 1 : 0); // running = 1   walking = 0
		writeC(this.npc.isInCombat() ? 1 : 0);

		if (this.activeChar.isInOlympiadMode())
		{
			writeC(0);
		}
		else
		{
			writeC(this.activeChar.isAlikeDead() ? 1 : 0);
		}

		writeC(0);

		writeC(this.activeChar.getMountType()); // 1-on Strider, 2-on Wyvern, 3-on Great Wolf, 0-no mount
		writeC(this.activeChar.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_CUSTOM_SELL ?
				this.activeChar.getPrivateStoreType() : L2PcInstance.STORE_PRIVATE_SELL);

		writeH(this.activeChar.getCubics().size());
		for (int id : this.activeChar.getCubics().keySet())
		{
			writeH(id);
		}

		writeC(0);

		writeC(0);

		writeH(this.activeChar.getRecomHave()); //Blue value for name (0 = white, 255 = pure blue)
		writeD(this.activeChar.getMountNpcId() + 1000000);
		writeD(this.activeChar.getCurrentClass().getId());
		writeD(0x00); //?
		writeC(0x00);

		writeC(this.activeChar.getTeam());

		writeD(this.activeChar.getClanCrestLargeId());
		writeC(this.activeChar.isNoble() ? 1 : 0); // Symbol on char menu ctrl+I
		writeC(this.activeChar.hasHeroAura() ? 1 : 0); // Hero Aura

		writeC(this.activeChar.isFishing() ? 1 : 0); //0x01: Fishing Mode (Cant be undone by setting back to 0)
		writeD(this.activeChar.getFishx());
		writeD(this.activeChar.getFishy());
		writeD(this.activeChar.getFishz());

		writeD(this.activeChar.getAppearance().getNameColor());

		writeD(this.heading);

		writeC(this.activeChar.getPledgeClass());
		writeH(this.activeChar.getPledgeType());

		writeD(this.activeChar.getAppearance().getTitleColor());

		if (this.activeChar.isCursedWeaponEquipped())
		{
			writeC(CursedWeaponsManager.getInstance().getLevel(this.activeChar.getCursedWeaponEquippedId()));
		}
		else
		{
			writeC(0x00);
		}

		if (this.activeChar.getClanId() > 0)
		{
			writeD(this.activeChar.getClan().getReputationScore());
		}
		else
		{
			writeD(0x00);
		}

		// T1
		writeH(this.activeChar.getTransformationId());
		writeH(this.activeChar.getAgathionId());

		//writeC(0x00);
		writeD(0x00);
		writeC(0x01);

		writeD(0x00); // GoD ???;
		writeD((int) Math.round(this.npc.getCurrentHp()));
		writeD(this.npc.getMaxHp());
		writeD((int) Math.round(this.npc.getCurrentMp()));
		writeD(this.npc.getMaxMp());

		writeC(this.activeChar.isShowingHat() ? 1 : 0); // Show/hide hat

		Set<Integer> abnormals = this.npc.getAbnormalEffect();
		if (this.activeChar.getAppearance().getInvisible())
		{
			abnormals.add(VisualEffect.STEALTH.getId());
		}
		writeD(abnormals.size());
		for (int abnormalId : abnormals)
		{
			writeH(abnormalId);
		}

		//writeC(this.inv.getMaxTalismanCount());
		writeC(0x00);
		writeC(this.inv.getCloakStatus());
		boolean showWings = true;
		if (getWriteClient() != null && getWriteClient().getActiveChar() != null)
		{
			showWings = !getWriteClient().getActiveChar().isNickNameWingsDisabled() &&
					getWriteClient().getActiveChar().isPlayingEvent();
		}

		writeC(showWings ? this.activeChar.getSpentAbilityPoints() : 0x00);
	}

	@Override
	protected final Class<?> getOpCodeClass()
	{
		return CharInfo.class;
	}
}
