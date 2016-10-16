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
import l2server.gameserver.model.actor.instance.L2DecoyInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.itemcontainer.Inventory;
import l2server.gameserver.model.itemcontainer.PcInventory;
import l2server.gameserver.stats.VisualEffect;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.log.Log;

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
public class CharInfo extends L2GameServerPacket
{
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
	private float moveMultiplier, attackSpeedMultiplier;
	//private int territoryId;
	//private boolean isDisguised;

	private int vehicleId, airShipHelm;

	/**
	 */
	public CharInfo(L2PcInstance cha)
	{
		this.activeChar = cha;
		this.objId = cha.getObjectId();
		this.inv = cha.getInventory();
		if (this.activeChar.getVehicle() != null && this.activeChar.getInVehiclePosition() != null)
		{
			this.x = this.activeChar.getInVehiclePosition().getX();
			this.y = this.activeChar.getInVehiclePosition().getY();
			this.z = this.activeChar.getInVehiclePosition().getZ();
			this.vehicleId = this.activeChar.getVehicle().getObjectId();
			if (this.activeChar.isInAirShip() && this.activeChar.getAirShip().isCaptain(this.activeChar))
			{
				this.airShipHelm = this.activeChar.getAirShip().getHelmItemId();
			}
			else
			{
				this.airShipHelm = 0;
			}
		}
		else
		{
			this.x = this.activeChar.getX();
			this.y = this.activeChar.getY();
			this.z = this.activeChar.getZ();
			this.vehicleId = 0;
			this.airShipHelm = 0;
		}
		this.heading = this.activeChar.getHeading();
		this.mAtkSpd = this.activeChar.getMAtkSpd();
		this.pAtkSpd = this.activeChar.getPAtkSpd();
		this.moveMultiplier = this.activeChar.getMovementSpeedMultiplier();
		this.attackSpeedMultiplier = this.activeChar.getAttackSpeedMultiplier();
		this.runSpd = (int) this.activeChar.getTemplate().baseRunSpd;
		this.walkSpd = (int) this.activeChar.getTemplate().baseWalkSpd;
		this.invisibleCharacter = cha.getAppearance().getInvisible() ? cha.getObjectId() : 0;
		//_territoryId = TerritoryWarManager.getInstance().getRegisteredTerritoryId(cha);
		//_isDisguised = TerritoryWarManager.getInstance().isDisguised(cha.getObjectId());
	}

	public CharInfo(L2DecoyInstance decoy)
	{
		this(decoy.getActingPlayer()); // init
		this.vehicleId = 0;
		this.airShipHelm = 0;
		this.objId = decoy.getObjectId();
		this.x = decoy.getX();
		this.y = decoy.getY();
		this.z = decoy.getZ();
		this.heading = decoy.getHeading();
	}

	@Override
	protected final void writeImpl()
	{
		boolean gmSeeInvis = false;
		if (this.invisibleCharacter != 0)
		{
			L2PcInstance tmp = getClient().getActiveChar();
			if (tmp != null && (tmp.isGM() || tmp.isInSameParty(this.activeChar)))
			{
				gmSeeInvis = true;
			}
		}

		if (this.activeChar.getPoly().isMorphed())
		{
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(this.activeChar.getPoly().getPolyId());

			if (template != null)
			{
				writeC(0x0c);
				writeD(this.objId);
				writeD(this.activeChar.getPoly().getPolyId() + 1000000); // npctype id
				writeD(this.activeChar.getReputation() < 0 ? 1 : 0);
				writeD(this.x);
				writeD(this.y);
				writeD(this.z);
				writeD(this.heading);
				writeD(0x00);
				writeD(this.mAtkSpd);
				writeD(this.pAtkSpd);
				writeD(this.runSpd); // TODO: the order of the speeds should be confirmed
				writeD(this.walkSpd);
				writeD(this.runSpd); // swim run speed
				writeD(this.walkSpd); // swim walk speed
				writeD(this.runSpd); // fly run speed
				writeD(this.walkSpd); // fly walk speed
				writeD(this.runSpd); // fly run speed ?
				writeD(this.walkSpd); // fly walk speed ?
				writeF(this.moveMultiplier);
				writeF(this.attackSpeedMultiplier);
				writeF(template.fCollisionRadius);
				writeF(template.fCollisionHeight);
				writeD(this.inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND)); // right hand weapon
				writeD(0);
				writeD(this.inv.getPaperdollItemId(Inventory.PAPERDOLL_LHAND)); // left hand weapon
				writeC(1); // name above char 1=true ... ??
				writeC(this.activeChar.isRunning() ? 1 : 0);
				writeC(this.activeChar.isInCombat() ? 1 : 0);
				writeC(this.activeChar.isAlikeDead() ? 1 : 0);

				if (gmSeeInvis)
				{
					writeC(0);
				}
				else
				{
					writeC(this.invisibleCharacter != 0 ? 1 :
							0); // invisible ?? 0=false  1=true   2=summoned (only works if model has a summon animation)
				}

				writeS(this.activeChar.getAppearance().getVisibleName());

				if (gmSeeInvis || this.activeChar.getAppearance().getInvisible())
				{
					writeS("Invisible");
				}
				else
				{
					writeS(this.activeChar.getAppearance().getVisibleTitle());
				}

				writeD(0);
				writeD(0);
				writeD(0); // hmm karma ??

				writeD(this.activeChar.getClanId()); //clan id
				writeD(this.activeChar.getClanCrestId()); //crest id
				writeD(0); // C2
				writeD(0); // C2
				writeC(0); // C2
				writeC(0x00); // C3  team circle 1-blue, 2-red
				writeF(template.fCollisionRadius);
				writeF(template.fCollisionHeight);
				writeD(0x00); // C4
				writeD(0x00); // C6
				writeD(0x00);
				writeD(0x00);
				writeC(0x01);
				writeC(0x01);
				writeD(0x00);
			}
			else
			{
				Log.warning("Character " + this.activeChar.getName() + " (" + this.activeChar.getObjectId() +
						") morphed in a Npc (" + this.activeChar.getPoly().getPolyId() + ") w/o template.");
			}
		}
		else
		{
			writeD(this.x);
			writeD(this.y);
			writeD(this.z);
			writeD(this.vehicleId);
			writeD(this.objId);
			writeS(this.activeChar.getAppearance().getVisibleName());
			writeH(this.activeChar.getVisibleTemplate().race.ordinal());
			writeC(this.activeChar.getAppearance().getSex() ? 1 : 0);

			writeD(this.activeChar.getVisibleTemplate().startingClassId);

			writeD(this.inv.getPaperdollItemId(Inventory.PAPERDOLL_UNDER));
			writeD(this.inv.getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
			if (this.airShipHelm == 0)
			{
				writeD(this.inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
				writeD(this.inv.getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
			}
			else
			{
				writeD(this.airShipHelm);
				writeD(0);
			}

			writeD(this.inv.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
			writeD(this.inv.getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
			writeD(this.inv.getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
			writeD(this.inv.getPaperdollItemId(Inventory.PAPERDOLL_FEET));

			int cloakId = 0;
			if (this.inv.getPaperdollAppearance(Inventory.PAPERDOLL_CLOAK) != 0)
			{
				cloakId = this.inv.getPaperdollAppearance(Inventory.PAPERDOLL_CLOAK);
			}
			else
			{
				cloakId = this.inv.getPaperdollItemId(Inventory.PAPERDOLL_CLOAK);
			}

			if (this.activeChar.isCloakHidden())
			{
				cloakId = 0;
			}

			switch (cloakId)
			{
				case 34996: // Cloak of Radiant Light
				case 34997: //Cloak of Cold Darkness
				{
					if (!this.activeChar.isClanLeader() || this.activeChar.getClan().getLevel() < 11 ||
							this.activeChar.getClan().getHasCastle() == 0)
					{
						cloakId = this.inv.getPaperdollItemId(Inventory.PAPERDOLL_CLOAK);
					}
				}
			}

			writeD(cloakId);

			writeD(this.inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
			if (this.activeChar.isShowingHat())
			{
				writeD(this.inv.getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
				writeD(this.inv.getPaperdollItemId(Inventory.PAPERDOLL_HAIR2));
			}
			else
			{
				writeD(0x00);
				writeD(0x00);
			}
			// c6 new h's
			writeQ(this.inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_UNDER));
			writeQ(this.inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_HEAD));
			if (this.airShipHelm == 0)
			{
				writeQ(this.inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_RHAND));
			}
			else
			{
				writeQ(0);
			}

			writeC(this.activeChar.getArmorEnchant());

			// T1 new d's
			if (this.airShipHelm == 0)
			{
				writeD(this.inv.getPaperdollAppearance(Inventory.PAPERDOLL_RHAND));
				writeD(this.inv.getPaperdollAppearance(Inventory.PAPERDOLL_LHAND));
			}
			else
			{
				writeD(0);
				writeD(0);
			}
			/*if (this.inv.getPaperdollAppearance(Inventory.PAPERDOLL_CHEST) > 46500 && this.inv.getPaperdollAppearance(Inventory.PAPERDOLL_CHEST) != 46601)
            {
				writeD(this.inv.getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
				writeD(this.inv.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
				writeD(this.inv.getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
				writeD(this.inv.getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
				writeD(this.inv.getPaperdollItemId(Inventory.PAPERDOLL_FEET));
				writeD(this.inv.getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
				writeD(this.inv.getPaperdollItemId(Inventory.PAPERDOLL_HAIR2));
			}
			else*/
			{
				writeD(this.inv.getPaperdollAppearance(Inventory.PAPERDOLL_HEAD));
				writeD(this.inv.getPaperdollAppearance(Inventory.PAPERDOLL_GLOVES));
				writeD(this.inv.getPaperdollAppearance(Inventory.PAPERDOLL_CHEST));
				writeD(this.inv.getPaperdollAppearance(Inventory.PAPERDOLL_LEGS));
				writeD(this.inv.getPaperdollAppearance(Inventory.PAPERDOLL_FEET));
				writeD(this.inv.getPaperdollAppearance(Inventory.PAPERDOLL_HAIR));
				writeD(this.inv.getPaperdollAppearance(Inventory.PAPERDOLL_HAIR2));
			}

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

			if (gmSeeInvis || this.activeChar.getAppearance().getInvisible())
			{
				writeS("Invisible");
			}
			else
			{
				writeS(this.activeChar.getAppearance().getVisibleTitle());
			}

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

			writeC(this.activeChar.isSitting() ? 0 : 1); // standing = 1  sitting = 0
			writeC(this.activeChar.isRunning() ? 1 : 0); // running = 1   walking = 0
			writeC(this.activeChar.isInCombat() ? 1 : 0);

			if (this.activeChar.isInOlympiadMode())
			{
				writeC(0);
			}
			else
			{
				writeC(this.activeChar.isAlikeDead() ? 1 : 0);
			}

			if (gmSeeInvis)
			{
				writeC(0);
			}
			else
			{
				writeC(this.invisibleCharacter != 0 ? 1 : 0); // invisible = 1  visible = 0
			}

			writeC(this.activeChar.getMountType()); // 1-on Strider, 2-on Wyvern, 3-on Great Wolf, 0-no mount
			writeC(this.activeChar.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_CUSTOM_SELL ?
					this.activeChar.getPrivateStoreType() : L2PcInstance.STORE_PRIVATE_SELL);

			writeH(this.activeChar.getCubics().size());
			this.activeChar.getCubics().keySet().forEach(this::writeH);

			writeC(this.activeChar.isInPartyMatchRoom() ? 1 : 0);

			writeC(this.activeChar.isFlyingMounted() ? 2 : 0);

			writeH(this.activeChar.getRecomHave()); //Blue value for name (0 = white, 255 = pure blue)
			writeD(this.activeChar.getMountNpcId() + 1000000);
			writeD(this.activeChar.getCurrentClass().getId());
			writeD(0x00); //?
			writeC(this.activeChar.isMounted() || this.airShipHelm != 0 ? 0 : this.activeChar.getEnchantEffect());

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
			writeH(0x00);

			//writeC(0x00);
			writeH(this.activeChar.getAgathionId());
			writeH(0x00);
			writeC(0x01);

			writeD(0x00); // GoD ???;
			writeD((int) Math.round(this.activeChar.getCurrentHp()));
			writeD(this.activeChar.getMaxHp());
			writeD((int) Math.round(this.activeChar.getCurrentMp()));
			writeD(this.activeChar.getMaxMp());

			writeC(this.activeChar.isShowingHat() ? 1 : 0); // Show/hide hat

			Set<Integer> abnormals = this.activeChar.getAbnormalEffect();
			if (this.activeChar.getAppearance().getInvisible())
			{
				abnormals.add(VisualEffect.STEALTH.getId());
			}
			writeD(abnormals.size());
			abnormals.forEach(this::writeH);

			//writeC(this.inv.getMaxTalismanCount());
			writeC(this.activeChar.hasCoCAura() ? 100 : 0x00);
			writeC(this.inv.getCloakStatus());
			boolean showWings = true;
			if (getWriteClient() != null && getWriteClient().getActiveChar() != null)
			{
				final L2PcInstance player = getWriteClient().getActiveChar();
				showWings = !player.isNickNameWingsDisabled() && !player.isPlayingEvent();
			}

			writeC(showWings ? this.activeChar.getSpentAbilityPoints() : 0x00);
		}
	}
}
