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
import l2server.gameserver.model.actor.instance.DecoyInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.itemcontainer.Inventory;
import l2server.gameserver.model.itemcontainer.PcInventory;
import l2server.gameserver.stats.VisualEffect;
import l2server.gameserver.templates.chars.NpcTemplate;

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
 * 0090: 66 66 66 66 66 f2 3f 5f 63 97 a8 de 1a f9 3f 00	fffff.?c.....?.<p>
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
public class CharInfo extends L2GameServerPacket {
	private Player activeChar;
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

	public CharInfo(Player cha) {
		activeChar = cha;
		objId = cha.getObjectId();
		inv = cha.getInventory();
		if (activeChar.getVehicle() != null && activeChar.getInVehiclePosition() != null) {
			x = activeChar.getInVehiclePosition().getX();
			y = activeChar.getInVehiclePosition().getY();
			z = activeChar.getInVehiclePosition().getZ();
			vehicleId = activeChar.getVehicle().getObjectId();
			if (activeChar.isInAirShip() && activeChar.getAirShip().isCaptain(activeChar)) {
				airShipHelm = activeChar.getAirShip().getHelmItemId();
			} else {
				airShipHelm = 0;
			}
		} else {
			x = activeChar.getX();
			y = activeChar.getY();
			z = activeChar.getZ();
			vehicleId = 0;
			airShipHelm = 0;
		}
		heading = activeChar.getHeading();
		mAtkSpd = activeChar.getMAtkSpd();
		pAtkSpd = activeChar.getPAtkSpd();
		moveMultiplier = activeChar.getMovementSpeedMultiplier();
		attackSpeedMultiplier = activeChar.getAttackSpeedMultiplier();
		runSpd = (int) activeChar.getTemplate().baseRunSpd;
		walkSpd = (int) activeChar.getTemplate().baseWalkSpd;
		invisibleCharacter = cha.getAppearance().getInvisible() ? cha.getObjectId() : 0;
		//territoryId = TerritoryWarManager.getInstance().getRegisteredTerritoryId(cha);
		//isDisguised = TerritoryWarManager.getInstance().isDisguised(cha.getObjectId());
	}

	public CharInfo(DecoyInstance decoy) {
		this(decoy.getActingPlayer()); // init
		vehicleId = 0;
		airShipHelm = 0;
		objId = decoy.getObjectId();
		x = decoy.getX();
		y = decoy.getY();
		z = decoy.getZ();
		heading = decoy.getHeading();
	}

	@Override
	protected final void writeImpl() {
		boolean gmSeeInvis = false;
		if (invisibleCharacter != 0) {
			Player tmp = getClient().getActiveChar();
			if (tmp != null && (tmp.isGM() || tmp.isInSameParty(activeChar))) {
				gmSeeInvis = true;
			}
		}

		if (activeChar.getPoly().isMorphed()) {
			NpcTemplate template = NpcTable.getInstance().getTemplate(activeChar.getPoly().getPolyId());

			if (template != null) {
				writeC(0x0c);
				writeD(objId);
				writeD(activeChar.getPoly().getPolyId() + 1000000); // npctype id
				writeD(activeChar.getReputation() < 0 ? 1 : 0);
				writeD(x);
				writeD(y);
				writeD(z);
				writeD(heading);
				writeD(0x00);
				writeD(mAtkSpd);
				writeD(pAtkSpd);
				writeD(runSpd); // TODO: the order of the speeds should be confirmed
				writeD(walkSpd);
				writeD(runSpd); // swim run speed
				writeD(walkSpd); // swim walk speed
				writeD(runSpd); // fly run speed
				writeD(walkSpd); // fly walk speed
				writeD(runSpd); // fly run speed ?
				writeD(walkSpd); // fly walk speed ?
				writeF(moveMultiplier);
				writeF(attackSpeedMultiplier);
				writeF(template.fCollisionRadius);
				writeF(template.fCollisionHeight);
				writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND)); // right hand weapon
				writeD(0);
				writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_LHAND)); // left hand weapon
				writeC(1); // name above char 1=true ... ??
				writeC(activeChar.isRunning() ? 1 : 0);
				writeC(activeChar.isInCombat() ? 1 : 0);
				writeC(activeChar.isAlikeDead() ? 1 : 0);

				if (gmSeeInvis) {
					writeC(0);
				} else {
					writeC(invisibleCharacter != 0 ? 1 : 0); // invisible ?? 0=false  1=true   2=summoned (only works if model has a summon animation)
				}

				writeS(activeChar.getAppearance().getVisibleName());

				if (gmSeeInvis || activeChar.getAppearance().getInvisible()) {
					writeS("Invisible");
				} else {
					writeS(activeChar.getAppearance().getVisibleTitle());
				}

				writeD(0);
				writeD(0);
				writeD(0); // hmm karma ??

				writeD(activeChar.getClanId()); //clan id
				writeD(activeChar.getClanCrestId()); //crest id
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
			} else {
				log.warn("Character " + activeChar.getName() + " (" + activeChar.getObjectId() + ") morphed in a Npc (" +
						activeChar.getPoly().getPolyId() + ") w/o template.");
			}
		} else {
			writeD(x);
			writeD(y);
			writeD(z);
			writeD(vehicleId);
			writeD(objId);
			writeS(activeChar.getAppearance().getVisibleName());
			writeH(activeChar.getVisibleTemplate().race.ordinal());
			writeC(activeChar.getAppearance().getSex() ? 1 : 0);

			writeD(activeChar.getVisibleTemplate().startingClassId);

			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_UNDER));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
			if (airShipHelm == 0) {
				writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
				writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
			} else {
				writeD(airShipHelm);
				writeD(0);
			}

			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_FEET));

			int cloakId = 0;
			if (inv.getPaperdollAppearance(Inventory.PAPERDOLL_CLOAK) != 0) {
				cloakId = inv.getPaperdollAppearance(Inventory.PAPERDOLL_CLOAK);
			} else {
				cloakId = inv.getPaperdollItemId(Inventory.PAPERDOLL_CLOAK);
			}

			if (activeChar.isCloakHidden()) {
				cloakId = 0;
			}

			switch (cloakId) {
				case 34996: // Cloak of Radiant Light
				case 34997: //Cloak of Cold Darkness
				{
					if (!activeChar.isClanLeader() || activeChar.getClan().getLevel() < 11 || activeChar.getClan().getHasCastle() == 0) {
						cloakId = inv.getPaperdollItemId(Inventory.PAPERDOLL_CLOAK);
					}
				}
			}

			writeD(cloakId);

			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
			if (activeChar.isShowingHat()) {
				writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
				writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_HAIR2));
			} else {
				writeD(0x00);
				writeD(0x00);
			}
			// c6 new h's
			writeQ(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_UNDER));
			writeQ(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_HEAD));
			if (airShipHelm == 0) {
				writeQ(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_RHAND));
			} else {
				writeQ(0);
			}

			writeC(activeChar.getArmorEnchant());

			// T1 new d's
			if (airShipHelm == 0) {
				writeD(inv.getPaperdollAppearance(Inventory.PAPERDOLL_RHAND));
				writeD(inv.getPaperdollAppearance(Inventory.PAPERDOLL_LHAND));
			} else {
				writeD(0);
				writeD(0);
			}
			/*if (inv.getPaperdollAppearance(Inventory.PAPERDOLL_CHEST) > 46500 && inv.getPaperdollAppearance(Inventory.PAPERDOLL_CHEST) != 46601)
            {
				writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
				writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
				writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
				writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
				writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_FEET));
				writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
				writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_HAIR2));
			}
			else*/
			{
				writeD(inv.getPaperdollAppearance(Inventory.PAPERDOLL_HEAD));
				writeD(inv.getPaperdollAppearance(Inventory.PAPERDOLL_GLOVES));
				writeD(inv.getPaperdollAppearance(Inventory.PAPERDOLL_CHEST));
				writeD(inv.getPaperdollAppearance(Inventory.PAPERDOLL_LEGS));
				writeD(inv.getPaperdollAppearance(Inventory.PAPERDOLL_FEET));
				writeD(inv.getPaperdollAppearance(Inventory.PAPERDOLL_HAIR));
				writeD(inv.getPaperdollAppearance(Inventory.PAPERDOLL_HAIR2));
			}

			// end of t1 new d's
			writeC(activeChar.getPvpFlag());
			writeD(activeChar.getReputation());

			writeD(mAtkSpd);
			writeD(pAtkSpd);

			writeH(runSpd); // TODO: the order of the speeds should be confirmed
			writeH(walkSpd);
			writeH(runSpd); // swim run speed
			writeH(walkSpd); // swim walk speed
			writeH(runSpd); // fly run speed
			writeH(walkSpd); // fly walk speed
			writeH(runSpd); // fly run speed ?
			writeH(walkSpd); // fly walk speed ?
			writeF(activeChar.getMovementSpeedMultiplier()); // activeChar.getProperMultiplier()
			writeF(activeChar.getAttackSpeedMultiplier()); // activeChar.getAttackSpeedMultiplier()
			L2Transformation transform = activeChar.getTransformation();

			if (activeChar.getMountType() != 0) {
				writeF(NpcTable.getInstance().getTemplate(activeChar.getMountNpcId()).fCollisionRadius);
				writeF(NpcTable.getInstance().getTemplate(activeChar.getMountNpcId()).fCollisionHeight);
			} else if (transform != null) {
				writeF(transform.getCollisionRadius());
				writeF(transform.getCollisionHeight());
			} else {
				writeF(activeChar.getCollisionRadius());
				writeF(activeChar.getCollisionHeight());
			}

			writeD(activeChar.getAppearance().getHairStyle());
			writeD(activeChar.getAppearance().getHairColor());
			writeD(activeChar.getAppearance().getFace());

			if (gmSeeInvis || activeChar.getAppearance().getInvisible()) {
				writeS("Invisible");
			} else {
				writeS(activeChar.getAppearance().getVisibleTitle());
			}

			if (!activeChar.isCursedWeaponEquipped() && !(activeChar.isPlayingEvent() &&
					(activeChar.getEvent().getType() == EventType.DeathMatch || activeChar.getEvent().getType() == EventType.Survival ||
							activeChar.getEvent().getType() == EventType.KingOfTheHill))) {
				writeD(activeChar.getClanId());
				writeD(activeChar.getClanCrestId());
				writeD(activeChar.getAllyId());
				writeD(activeChar.getAllyCrestId());
			} else {
				writeD(0);
				writeD(0);
				writeD(0);
				writeD(0);
			}

			writeC(activeChar.isSitting() ? 0 : 1); // standing = 1  sitting = 0
			writeC(activeChar.isRunning() ? 1 : 0); // running = 1   walking = 0
			writeC(activeChar.isInCombat() ? 1 : 0);

			if (activeChar.isInOlympiadMode()) {
				writeC(0);
			} else {
				writeC(activeChar.isAlikeDead() ? 1 : 0);
			}

			if (gmSeeInvis) {
				writeC(0);
			} else {
				writeC(invisibleCharacter != 0 ? 1 : 0); // invisible = 1  visible = 0
			}

			writeC(activeChar.getMountType()); // 1-on Strider, 2-on Wyvern, 3-on Great Wolf, 0-no mount
			writeC(activeChar.getPrivateStoreType() != Player.STORE_PRIVATE_CUSTOM_SELL ? activeChar.getPrivateStoreType() :
					Player.STORE_PRIVATE_SELL);

			writeH(activeChar.getCubics().size());
			activeChar.getCubics().keySet().forEach(this::writeH);

			writeC(activeChar.isInPartyMatchRoom() ? 1 : 0);

			writeC(activeChar.isFlyingMounted() ? 2 : 0);

			writeH(activeChar.getRecomHave()); //Blue value for name (0 = white, 255 = pure blue)
			writeD(activeChar.getMountNpcId() + 1000000);
			writeD(activeChar.getCurrentClass().getId());
			writeD(0x00); //?
			writeC(activeChar.isMounted() || airShipHelm != 0 ? 0 : activeChar.getEnchantEffect());

			writeC(activeChar.getTeam());

			writeD(activeChar.getClanCrestLargeId());
			writeC(activeChar.isNoble() ? 1 : 0); // Symbol on char menu ctrl+I
			writeC(activeChar.hasHeroAura() ? 1 : 0); // Hero Aura

			writeC(activeChar.isFishing() ? 1 : 0); //0x01: Fishing Mode (Cant be undone by setting back to 0)
			writeD(activeChar.getFishx());
			writeD(activeChar.getFishy());
			writeD(activeChar.getFishz());

			writeD(activeChar.getAppearance().getNameColor());

			writeD(heading);

			writeC(activeChar.getPledgeClass());
			writeH(activeChar.getPledgeType());

			writeD(activeChar.getAppearance().getTitleColor());

			if (activeChar.isCursedWeaponEquipped()) {
				writeC(CursedWeaponsManager.getInstance().getLevel(activeChar.getCursedWeaponEquippedId()));
			} else {
				writeC(0x00);
			}

			if (activeChar.getClanId() > 0) {
				writeD(activeChar.getClan().getReputationScore());
			} else {
				writeD(0x00);
			}

			// T1
			writeH(activeChar.getTransformationId());
			writeH(0x00);

			//writeC(0x00);
			writeH(activeChar.getAgathionId());
			writeH(0x00);
			writeC(0x01);

			writeD(0x00); // GoD ???;
			writeD((int) Math.round(activeChar.getCurrentHp()));
			writeD(activeChar.getMaxHp());
			writeD((int) Math.round(activeChar.getCurrentMp()));
			writeD(activeChar.getMaxMp());

			writeC(activeChar.isShowingHat() ? 1 : 0); // Show/hide hat

			Set<Integer> abnormals = activeChar.getAbnormalEffect();
			if (activeChar.getAppearance().getInvisible()) {
				abnormals.add(VisualEffect.STEALTH.getId());
			}
			writeD(abnormals.size());
			abnormals.forEach(this::writeH);

			//writeC(inv.getMaxTalismanCount());
			writeC(activeChar.hasCoCAura() ? 100 : 0x00);
			writeC(inv.getCloakStatus());
			boolean showWings = true;
			if (getWriteClient() != null && getWriteClient().getActiveChar() != null) {
				final Player player = getWriteClient().getActiveChar();
				showWings = !player.isNickNameWingsDisabled() && !player.isPlayingEvent();
			}

			writeC(showWings ? activeChar.getSpentAbilityPoints() : 0x00);
		}
	}
}
