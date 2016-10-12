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
	private L2Npc _npc;
	private L2PcInstance _activeChar;
	private PcInventory _inv;
	private int _objId;
	private int _x, _y, _z, _heading;
	private int _mAtkSpd, _pAtkSpd;

	/**
	 * Run speed, swimming run speed and flying run speed
	 */
	private int _runSpd;
	/**
	 * Walking speed, swimming walking speed and flying walking speed
	 */
	private int _walkSpd;

	/**
	 */
	public ClonedPlayerInfo(L2Npc npc, L2PcInstance cha)
	{
		_npc = npc;
		_activeChar = cha;
		_objId = _npc.getObjectId();
		_inv = cha.getInventory();
		_x = _npc.getX();
		_y = _npc.getY();
		_z = _npc.getZ();
		_heading = _npc.getHeading();
		_mAtkSpd = _activeChar.getMAtkSpd();
		_pAtkSpd = _activeChar.getPAtkSpd();
		_runSpd = (int) _activeChar.getTemplate().baseRunSpd;
		_walkSpd = (int) _activeChar.getTemplate().baseWalkSpd;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(0);
		writeD(_objId);
		writeS(_activeChar.getAppearance().getVisibleName());
		writeH(_activeChar.getVisibleTemplate().race.ordinal());
		writeC(_activeChar.getAppearance().getSex() ? 1 : 0);

		writeD(_activeChar.getVisibleTemplate().startingClassId);

		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_UNDER));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_FEET));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_CLOAK));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_HAIR2));

		// c6 new h's
		writeQ(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_UNDER));
		writeQ(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_HEAD));
		writeQ(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_RHAND));

		writeC(_activeChar.getArmorEnchant());

		// T1 new d's
		writeD(0);
		writeD(0);
		writeD(0x00);
		writeD(_inv.getPaperdollAppearance(Inventory.PAPERDOLL_GLOVES));
		writeD(_inv.getPaperdollAppearance(Inventory.PAPERDOLL_CHEST));
		writeD(_inv.getPaperdollAppearance(Inventory.PAPERDOLL_LEGS));
		writeD(_inv.getPaperdollAppearance(Inventory.PAPERDOLL_FEET));
		writeD(0x00);
		writeD(0x00);
		// end of t1 new d's

		writeC(_activeChar.getPvpFlag());
		writeD(_activeChar.getReputation());

		writeD(_mAtkSpd);
		writeD(_pAtkSpd);

		writeH(_runSpd); // TODO: the order of the speeds should be confirmed
		writeH(_walkSpd);
		writeH(_runSpd); // swim run speed
		writeH(_walkSpd); // swim walk speed
		writeH(_runSpd); // fly run speed
		writeH(_walkSpd); // fly walk speed
		writeH(_runSpd); // fly run speed ?
		writeH(_walkSpd); // fly walk speed ?
		writeF(_activeChar.getMovementSpeedMultiplier()); // _activeChar.getProperMultiplier()
		writeF(_activeChar.getAttackSpeedMultiplier()); // _activeChar.getAttackSpeedMultiplier()
		L2Transformation transform = _activeChar.getTransformation();

		if (_activeChar.getMountType() != 0)
		{
			writeF(NpcTable.getInstance().getTemplate(_activeChar.getMountNpcId()).fCollisionRadius);
			writeF(NpcTable.getInstance().getTemplate(_activeChar.getMountNpcId()).fCollisionHeight);
		}
		else if (transform != null)
		{
			writeF(transform.getCollisionRadius());
			writeF(transform.getCollisionHeight());
		}
		else
		{
			writeF(_activeChar.getCollisionRadius());
			writeF(_activeChar.getCollisionHeight());
		}

		writeD(_activeChar.getAppearance().getHairStyle());
		writeD(_activeChar.getAppearance().getHairColor());
		writeD(_activeChar.getAppearance().getFace());

		writeS(_activeChar.getAppearance().getVisibleTitle());

		if (!_activeChar.isCursedWeaponEquipped() && !(_activeChar.isPlayingEvent() &&
				(_activeChar.getEvent().getType() == EventType.DeathMatch ||
						_activeChar.getEvent().getType() == EventType.Survival ||
						_activeChar.getEvent().getType() == EventType.KingOfTheHill)))
		{
			writeD(_activeChar.getClanId());
			writeD(_activeChar.getClanCrestId());
			writeD(_activeChar.getAllyId());
			writeD(_activeChar.getAllyCrestId());
		}
		else
		{
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
		}

		writeC(1); // standing = 1  sitting = 0
		writeC(_npc.isRunning() ? 1 : 0); // running = 1   walking = 0
		writeC(_npc.isInCombat() ? 1 : 0);

		if (_activeChar.isInOlympiadMode())
		{
			writeC(0);
		}
		else
		{
			writeC(_activeChar.isAlikeDead() ? 1 : 0);
		}

		writeC(0);

		writeC(_activeChar.getMountType()); // 1-on Strider, 2-on Wyvern, 3-on Great Wolf, 0-no mount
		writeC(_activeChar.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_CUSTOM_SELL ?
				_activeChar.getPrivateStoreType() : L2PcInstance.STORE_PRIVATE_SELL);

		writeH(_activeChar.getCubics().size());
		for (int id : _activeChar.getCubics().keySet())
		{
			writeH(id);
		}

		writeC(0);

		writeC(0);

		writeH(_activeChar.getRecomHave()); //Blue value for name (0 = white, 255 = pure blue)
		writeD(_activeChar.getMountNpcId() + 1000000);
		writeD(_activeChar.getCurrentClass().getId());
		writeD(0x00); //?
		writeC(0x00);

		writeC(_activeChar.getTeam());

		writeD(_activeChar.getClanCrestLargeId());
		writeC(_activeChar.isNoble() ? 1 : 0); // Symbol on char menu ctrl+I
		writeC(_activeChar.hasHeroAura() ? 1 : 0); // Hero Aura

		writeC(_activeChar.isFishing() ? 1 : 0); //0x01: Fishing Mode (Cant be undone by setting back to 0)
		writeD(_activeChar.getFishx());
		writeD(_activeChar.getFishy());
		writeD(_activeChar.getFishz());

		writeD(_activeChar.getAppearance().getNameColor());

		writeD(_heading);

		writeC(_activeChar.getPledgeClass());
		writeH(_activeChar.getPledgeType());

		writeD(_activeChar.getAppearance().getTitleColor());

		if (_activeChar.isCursedWeaponEquipped())
		{
			writeC(CursedWeaponsManager.getInstance().getLevel(_activeChar.getCursedWeaponEquippedId()));
		}
		else
		{
			writeC(0x00);
		}

		if (_activeChar.getClanId() > 0)
		{
			writeD(_activeChar.getClan().getReputationScore());
		}
		else
		{
			writeD(0x00);
		}

		// T1
		writeH(_activeChar.getTransformationId());
		writeH(_activeChar.getAgathionId());

		//writeC(0x00);
		writeD(0x00);
		writeC(0x01);

		writeD(0x00); // GoD ???;
		writeD((int) Math.round(_npc.getCurrentHp()));
		writeD(_npc.getMaxHp());
		writeD((int) Math.round(_npc.getCurrentMp()));
		writeD(_npc.getMaxMp());

		writeC(_activeChar.isShowingHat() ? 1 : 0); // Show/hide hat

		Set<Integer> abnormals = _npc.getAbnormalEffect();
		if (_activeChar.getAppearance().getInvisible())
		{
			abnormals.add(VisualEffect.STEALTH.getId());
		}
		writeD(abnormals.size());
		for (int abnormalId : abnormals)
		{
			writeH(abnormalId);
		}

		//writeC(_inv.getMaxTalismanCount());
		writeC(0x00);
		writeC(_inv.getCloakStatus());
		boolean showWings = true;
		if (getWriteClient() != null && getWriteClient().getActiveChar() != null)
		{
			showWings = !getWriteClient().getActiveChar().isNickNameWingsDisabled() &&
					getWriteClient().getActiveChar().isPlayingEvent();
		}

		writeC(showWings ? _activeChar.getSpentAbilityPoints() : 0x00);
	}

	@Override
	protected final Class<?> getOpCodeClass()
	{
		return CharInfo.class;
	}
}
