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

import l2server.Config;
import l2server.gameserver.GeoData;
import l2server.gameserver.GmListTable;
import l2server.gameserver.TimeController;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.ai.L2SummonAI;
import l2server.gameserver.datatables.PetDataTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.events.instanced.types.SimonSays;
import l2server.gameserver.instancemanager.AirShipManager;
import l2server.gameserver.instancemanager.CastleManager;
import l2server.gameserver.model.*;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.*;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.Quest.QuestEventType;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.network.serverpackets.SocialAction;
import l2server.gameserver.pathfinding.AbstractNodeLoc;
import l2server.gameserver.pathfinding.PathFinding;
import l2server.log.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

/**
 * This class ...
 *
 * @version $Revision: 1.11.2.7.2.9 $ $Date: 2005/04/06 16:13:48 $
 */
public final class RequestActionUse extends L2GameClientPacket
{

	private int _actionId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;

	@Override
	protected void readImpl()
	{
		_actionId = readD();
		_ctrlPressed = readD() == 1;
		_shiftPressed = readC() == 1;
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
		{
			return;
		}

		if (Config.DEBUG)
		{
			Log.finest(activeChar.getName() + " request Action use: id " + _actionId + " 2:" + _ctrlPressed + " 3:" +
					_shiftPressed);
		}

		// dont do anything if player is dead
		if (activeChar.isAlikeDead() || activeChar.isDead())
		{
			getClient().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		// don't do anything if player is confused
		if (activeChar.isOutOfControl())
		{
			getClient().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		// don't allow to do some action if player is transformed
		if (activeChar.isTransformed())
		{
			// Allow naviarope to use summon actions
			int[] allowedActions = activeChar.isTransformed() && activeChar.getTransformationId() != 509 ?
					ExBasicActionList._actionsOnTransform : ExBasicActionList._defaultActionList;
			if (!(Arrays.binarySearch(allowedActions, _actionId) >= 0))
			{
				getClient().sendPacket(ActionFailed.STATIC_PACKET);
				Log.warning("Player " + activeChar + " used action which he does not have! id = " + _actionId +
						" transform: " + activeChar.getTransformation());
				return;
			}
		}

		L2PetInstance pet = activeChar.getPet();
		List<L2Summon> summons = new ArrayList<>(activeChar.getSummons());
		summons.add(pet);
		L2Object target = activeChar.getTarget();

		if (Config.DEBUG)
		{
			Log.info("Requested Action ID: " + String.valueOf(_actionId));
		}

		switch (_actionId)
		{
			case 0: // Sit/Stand
				if (activeChar.getMountType() != 0)
				{
					break;
				}

				if (target != null && !activeChar.isSitting() && target instanceof L2StaticObjectInstance &&
						((L2StaticObjectInstance) target).getType() == 1 &&
						CastleManager.getInstance().getCastle(target) != null &&
						activeChar.isInsideRadius(target, L2StaticObjectInstance.INTERACTION_DISTANCE, false, false))
				{
					ChairSit cs = new ChairSit(activeChar, ((L2StaticObjectInstance) target).getStaticObjectId());
					activeChar.sendPacket(cs);
					activeChar.sitDown();
					activeChar.broadcastPacket(cs);
					break;
				}

				if (activeChar.isSitting())
				{
					activeChar.standUp();
				}
				else
				{
					activeChar.sitDown();
				}

				if (Config.DEBUG)
				{
					Log.fine("new wait type: " + (activeChar.isSitting() ? "SITTING" : "STANDING"));
				}

				break;
			case 1: // Walk/Run
				if (activeChar.isRunning())
				{
					activeChar.setWalking();
				}
				else
				{
					activeChar.setRunning();
				}

				if (Config.DEBUG)
				{
					Log.fine("new move type: " + (activeChar.isRunning() ? "RUNNING" : "WALKIN"));
				}
				break;
			case 10: // Private Store - Sell
				activeChar.tryOpenPrivateSellStore(false);
				break;
			case 28: // Private Store - Buy
				activeChar.tryOpenPrivateBuyStore();
				break;
			case 15:
			case 21: // Change Movement Mode (pet follow/stop)
				for (L2Summon summon : summons)
				{
					if (summon != null && !activeChar.isBetrayed())
					{
						if (summon != summons.get(0))
						{
							((L2SummonAI) summon.getAI()).setStartFollowController(
									!((L2SummonAI) summons.get(0).getAI()).getStartFollowController());
						}
						((L2SummonAI) summon.getAI()).notifyFollowStatusChange();
					}
				}
				break;
			case 16:
			case 22: // Attack (pet attack)
			case 1099:
				if (target == null)
				{
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}

				List<AbstractNodeLoc> paths = PathFinding.getInstance()
						.findPath(activeChar.getX(), activeChar.getY(), activeChar.getZ(), target.getX(), target.getY(),
								target.getZ(), activeChar.getInstanceId(), true);
				if ((paths == null || paths.size() < 2) && !GeoData.getInstance().canSeeTarget(activeChar, target))
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_SEE_TARGET));
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}

				if (activeChar.isInOlympiadMode() && !activeChar.isOlympiadStart())
				{
					// if L2PcInstance is in Olympia and the match isn't already start, send a Server->Client packet ActionFailed
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}

				if (target.getActingPlayer() != null && activeChar.getSiegeState() > 0 &&
						activeChar.isInsideZone(L2Character.ZONE_SIEGE) &&
						target.getActingPlayer().getSiegeState() == activeChar.getSiegeState() &&
						target.getActingPlayer() != activeChar &&
						target.getActingPlayer().getSiegeSide() == activeChar.getSiegeSide() &&
						!Config.isServer(Config.TENKAI))
				{
					sendPacket(SystemMessage.getSystemMessage(
							SystemMessageId.FORCED_ATTACK_IS_IMPOSSIBLE_AGAINST_SIEGE_SIDE_TEMPORARY_ALLIED_MEMBERS));
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}

				for (L2Summon summon : summons)
				{
					if (summon != null && summon != target && activeChar != target && !summon.isBetrayed())
					{
						if (summon instanceof L2MobSummonInstance && !(target instanceof L2MobSummonInstance))
						{
							activeChar.sendMessage("Your Coke Mob is only able to attack other trained monsters");
							continue;
						}

						if (summon.isAttackingDisabled())
						{
							if (summon.getAttackEndTime() > TimeController.getGameTicks() &&
									summon.getTarget() == target)
							{
								summon.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
							}
							else
							{
								continue;
							}
						}

						if (summon instanceof L2PetInstance && summon.getLevel() - activeChar.getLevel() > 20)
						{
							activeChar.sendPacket(
									SystemMessage.getSystemMessage(SystemMessageId.PET_TOO_HIGH_TO_CONTROL));
							continue;
						}

						if (!activeChar.getAccessLevel().allowPeaceAttack() &&
								activeChar.isInsidePeaceZone(summon, target))
						{
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IN_PEACEZONE));
							continue;
						}

						if (summon.getNpcId() == 12564 || summon.getNpcId() == 12621)
						{
							// sin eater and wyvern can't attack with attack button
							activeChar.sendPacket(ActionFailed.STATIC_PACKET);
							continue;
						}

						if (summon.isLockedTarget())
						{
							summon.getOwner()
									.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_CHANGE_TARGET));
							continue;
						}

						if (target.isAutoAttackable(activeChar) || _ctrlPressed)
						{
							if (target instanceof L2DoorInstance)
							{
								if (((L2DoorInstance) target).isAttackable(activeChar) &&
										summon.getTemplate().getName().equalsIgnoreCase("Swoop Cannon"))
								{
									summon.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
								}
							}
							// siege golem AI doesn't support attacking other than doors at the moment
							else if (!summon.getTemplate().getName().equalsIgnoreCase("Siege Golem"))
							{
								summon.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
							}
						}
						else
						{
							summon.setFollowStatus(false);
							summon.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, target);
						}
					}
				}
				break;
			case 17:
			case 23: // Stop (pet - cancel action)
			case 1101:
				for (L2Summon summon : summons)
				{
					if (summon != null && !summon.isMovementDisabled() && !summon.isBetrayed())
					{
						summon.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
						if (_actionId == 1101)
						{
							((L2SummonAI) summon.getAI()).setStartFollowController(true);
							((L2SummonAI) summon.getAI()).notifyFollowStatusChange();
						}
					}
				}
				break;
			case 19: // Unsummon Pet
				if (pet != null)
				{
					//returns pet to control item
					if (pet.isDead())
					{
						activeChar.sendPacket(
								SystemMessage.getSystemMessage(SystemMessageId.DEAD_PET_CANNOT_BE_RETURNED));
					}
					else if (pet.isAttackingNow() || pet.isInCombat() || pet.isMovementDisabled() || pet.isBetrayed())
					{
						activeChar.sendPacket(
								SystemMessage.getSystemMessage(SystemMessageId.PET_CANNOT_SENT_BACK_DURING_BATTLE));
					}
					else
					{
						if (!pet.isHungry())
						{
							pet.unSummon(activeChar);
						}
						else if (pet.getPetData().getFood().length > 0)
						{
							activeChar.sendPacket(
									SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_RESTORE_HUNGRY_PETS));
						}
						else
						{
							activeChar.sendPacket(
									SystemMessage.getSystemMessage(SystemMessageId.THE_HELPER_PET_CANNOT_BE_RETURNED));
						}
					}
				}
				break;
			case 38: // Mount/Dismount
				activeChar.mountPlayer(pet);
				break;
			case 32: // Wild Hog Cannon - Switch Mode
				// useSkill(4230);
				break;
			case 36: // Soulless - Toxic Smoke
				useSkill(4259);
				break;
			case 37: // Dwarven Manufacture
				if (activeChar.isAlikeDead())
				{
					getClient().sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				if (activeChar.getPrivateStoreType() != 0)
				{
					activeChar.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
					activeChar.broadcastUserInfo();
				}
				if (activeChar.isSitting())
				{
					activeChar.standUp();
				}

				if (activeChar.getCreateList() == null)
				{
					activeChar.setCreateList(new L2ManufactureList());
				}

				activeChar.sendPacket(new RecipeShopManageList(activeChar, true));
				break;
			case 39: // Soulless - Parasite Burst
				useSkill(4138);
				break;
			case 41: // Wild Hog Cannon - Attack
				if (target != null && (target instanceof L2DoorInstance || target instanceof L2SiegeFlagInstance))
				{
					useSkill(4230);
				}
				else
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
				}
				break;
			case 42: // Kai the Cat - Self Damage Shield
				useSkill(4378, activeChar);
				break;
			case 43: // Unicorn Merrow - Hydro Screw
				useSkill(4137);
				break;
			case 44: // Big Boom - Boom Attack
				useSkill(4139);
				break;
			case 45: // Unicorn Boxer - Master Recharge
				useSkill(4025, activeChar);
				break;
			case 46: // Mew the Cat - Mega Storm Strike
				useSkill(4261);
				break;
			case 47: // Silhouette - Steal Blood
				useSkill(4260);
				break;
			case 48: // Mechanic Golem - Mech. Cannon
				useSkill(4068);
				break;
			case 51: // General Manufacture
				// Player shouldn't be able to set stores if he/she is alike dead (dead or fake death)
				if (activeChar.isAlikeDead())
				{
					getClient().sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				if (activeChar.getPrivateStoreType() != 0)
				{
					activeChar.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
					activeChar.broadcastUserInfo();
				}
				if (activeChar.isSitting())
				{
					activeChar.standUp();
				}

				if (activeChar.getCreateList() == null)
				{
					activeChar.setCreateList(new L2ManufactureList());
				}

				activeChar.sendPacket(new RecipeShopManageList(activeChar, false));
				break;
			case 52: // Unsummon
			case 1102:
				for (L2Summon summon : summons)
				{
					if (summon == null)
					{
						continue;
					}

					if (summon.isBetrayed())
					{
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_REFUSING_ORDER));
					}
					else if (summon.isAttackingNow() || summon.isInCombat())
					{
						activeChar.sendPacket(
								SystemMessage.getSystemMessage(SystemMessageId.PET_CANNOT_SENT_BACK_DURING_BATTLE));
					}
					else
					{
						summon.unSummon(activeChar);
					}
				}
				break;
			case 53: // Move to target
			case 1100:
				for (L2Summon summon : summons)
				{
					if (target != null && summon != null && summon != target && !summon.isMovementDisabled() &&
							!summon.isBetrayed() && !(summon instanceof L2MobSummonInstance))
					{
						summon.setFollowStatus(false);
						summon.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,
								new L2CharPosition(target.getX(), target.getY(), target.getZ(), 0));
					}
				}
				break;
			case 54: // Move to target hatch/strider
				if (target != null && pet != null && pet != target && !pet.isMovementDisabled() && !pet.isBetrayed())
				{
					pet.setFollowStatus(false);
					pet.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,
							new L2CharPosition(target.getX(), target.getY(), target.getZ(), 0));
				}
				break;
			case 61: // Private Store Package Sell
				activeChar.tryOpenPrivateSellStore(true);
				break;
			case 65: // Bot Report
				if (target == null || target == activeChar || !(target instanceof L2PcInstance) ||
						((L2PcInstance) target).getClient().getConnection() == null)
				{
					return;
				}

				if (!activeChar.getFloodProtectors().getReportBot().tryPerformAction("ReportBot"))
				{
					activeChar.sendMessage("You can use this action only one time every 5 minutes!");
					return;
				}

				String ip1 = activeChar.getClient().getConnection().getInetAddress().getHostAddress();
				String ip2 = ((L2PcInstance) target).getClient().getConnection().getInetAddress().getHostAddress();

				String msgContent = activeChar.getName() + " has reported a possible bot user. Nick: " +
						activeChar.getTarget().getName() + ". Loc: " + activeChar.getTarget().getX() + ", " +
						activeChar.getTarget().getY() + ", " + activeChar.getTarget().getZ() + ".";
				GmListTable
						.broadcastToGMs(new CreatureSay(activeChar.getObjectId(), 17, "Bot Report System", msgContent));

				SimpleDateFormat formatter;
				formatter = new SimpleDateFormat("dd/MM/yyyy H:mm:ss");
				String date = formatter.format(new Date());

				FileWriter save = null;
				try
				{
					File file = new File("log/BotReport.csv");

					boolean writeHead = false;
					if (!file.exists())
					{
						writeHead = true;
					}

					save = new FileWriter(file, true);

					if (writeHead)
					{
						String header = "Date, Botter, Reporter, Botter IP, Reporter IP, X, Y, Z\r\n";
						save.write(header);
					}

					String out =
							date + ", " + activeChar.getTarget().getName() + ", " + activeChar.getName() + ", " + ip2 +
									", " + ip1 + ", " + activeChar.getTarget().getPosition().getX() + ", " +
									activeChar.getTarget().getPosition().getY() + ", " +
									activeChar.getTarget().getPosition().getZ() + "\r\n";
					save.write(out);
				}
				catch (IOException e)
				{
					Log.log(Level.WARNING, "Bot Report System: BotReport.csv error: ", e);
				}
				finally
				{
					try
					{
						save.close();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				activeChar.sendMessage("Thank you for reporting.");
				break;
			case 67: // Steer
				if (activeChar.isInAirShip())
				{
					if (activeChar.getAirShip().setCaptain(activeChar))
					{
						activeChar.broadcastUserInfo();
					}
				}
				break;
			case 68: // Cancel Control
				if (activeChar.isInAirShip() && activeChar.getAirShip().isCaptain(activeChar))
				{
					if (activeChar.getAirShip().setCaptain(null))
					{
						activeChar.broadcastUserInfo();
					}
				}
				break;
			case 69: // Destination Map
				AirShipManager.getInstance().sendAirShipTeleportList(activeChar);
				break;
			case 70: // Exit Airship
				if (activeChar.isInAirShip())
				{
					if (activeChar.getAirShip().isCaptain(activeChar))
					{
						if (activeChar.getAirShip().setCaptain(null))
						{
							activeChar.broadcastUserInfo();
						}
					}
					else if (activeChar.getAirShip().isInDock())
					{
						activeChar.getAirShip().oustPlayer(activeChar);
					}
				}
				break;
			case 71:
			case 72:
			case 73:
				useCoupleSocial(_actionId - 55);
				break;
			case 1000: // Siege Golem - Siege Hammer
				if (target instanceof L2DoorInstance)
				{
					useSkill(4079);
				}
				break;
			case 1001: // TODO Sin Eater - Ultimate Bombastic Buster
				break;
			case 1003: // Wind Hatchling/Strider - Wild Stun
				useSkill(4710);
				break;
			case 1004: // Wind Hatchling/Strider - Wild Defense
				useSkill(4711, activeChar);
				break;
			case 1005: // Star Hatchling/Strider - Bright Burst
				useSkill(4712);
				break;
			case 1006: // Star Hatchling/Strider - Bright Heal
				useSkill(4713, activeChar);
				break;
			case 1007: // Cat Queen - Blessing of Queen
				useSkill(4699, activeChar);
				break;
			case 1008: // Cat Queen - Gift of Queen
				useSkill(4700, activeChar);
				break;
			case 1009: // Cat Queen - Cure of Queen
				useSkill(4701);
				break;
			case 1010: // Unicorn Seraphim - Blessing of Seraphim
				useSkill(4702, activeChar);
				break;
			case 1011: // Unicorn Seraphim - Gift of Seraphim
				useSkill(4703, activeChar);
				break;
			case 1012: // Unicorn Seraphim - Cure of Seraphim
				useSkill(4704);
				break;
			case 1013: // Nightshade - Curse of Shade
				useSkill(4705);
				break;
			case 1014: // Nightshade - Mass Curse of Shade
				useSkill(4706);
				break;
			case 1015: // Nightshade - Shade Sacrifice
				useSkill(4707);
				break;
			case 1016: // Cursed Man - Cursed Blow
				useSkill(4709);
				break;
			case 1017: // Cursed Man - Cursed Strike/Stun
				useSkill(4708);
				break;
			case 1031: // Feline King - Slash
				useSkill(5135);
				break;
			case 1032: // Feline King - Spinning Slash
				useSkill(5136);
				break;
			case 1033: // Feline King - Grip of the Cat
				useSkill(5137);
				break;
			case 1034: // Magnus the Unicorn - Whiplash
				useSkill(5138);
				break;
			case 1035: // Magnus the Unicorn - Tridal Wave
				useSkill(5139);
				break;
			case 1036: // Spectral Lord - Corpse Kaboom
				useSkill(5142);
				break;
			case 1037: // Spectral Lord - Dicing Death
				useSkill(5141);
				break;
			case 1038: // Spectral Lord - Force Curse
				useSkill(5140);
				break;
			case 1039: // Swoop Cannon - Cannon Fodder
				if (!(target instanceof L2DoorInstance))
				{
					useSkill(5110);
				}
				break;
			case 1040: // Swoop Cannon - Big Bang
				if (!(target instanceof L2DoorInstance))
				{
					useSkill(5111);
				}
				break;
			case 1041: // Great Wolf - Bite Attack
				useSkill(5442);
				break;
			case 1042: // Great Wolf - Maul
				useSkill(5444);
				break;
			case 1043: // Great Wolf - Cry of the Wolf
				useSkill(5443);
				break;
			case 1044: // Great Wolf - Awakening
				useSkill(5445);
				break;
			case 1045: // Great Wolf - Howl
				useSkill(5584);
				break;
			case 1046: // Strider - Roar
				useSkill(5585);
				break;
			case 1047: // Divine Beast - Bite
				useSkill(5580);
				break;
			case 1048: // Divine Beast - Stun Attack
				useSkill(5581);
				break;
			case 1049: // Divine Beast - Fire Breath
				useSkill(5582);
				break;
			case 1050: // Divine Beast - Roar
				useSkill(5583);
				break;
			case 1051: //Feline Queen - Bless The Body
				useSkill(5638);
				break;
			case 1052: //Feline Queen - Bless The Soul
				useSkill(5639);
				break;
			case 1053: //Feline Queen - Haste
				useSkill(5640);
				break;
			case 1054: //Unicorn Seraphim - Acumen
				useSkill(5643);
				break;
			case 1055: //Unicorn Seraphim - Clarity
				useSkill(5647);
				break;
			case 1056: //Unicorn Seraphim - Empower
				useSkill(5648);
				break;
			case 1057: //Unicorn Seraphim - Wild Magic
				useSkill(5646);
				break;
			case 1058: //Nightshade - Death Whisper
				useSkill(5652);
				break;
			case 1059: //Nightshade - Focus
				useSkill(5653);
				break;
			case 1060: //Nightshade - Guidance
				useSkill(5654);
				break;
			case 1061: // Wild Beast Fighter, White Weasel - Death blow
				useSkill(5745);
				break;
			case 1062: // Wild Beast Fighter - Double attack
				useSkill(5746);
				break;
			case 1063: // Wild Beast Fighter - Spin attack
				useSkill(5747);
				break;
			case 1064: // Wild Beast Fighter - Meteor Shower
				useSkill(5748);
				break;
			case 1065: // Fox Shaman, Wild Beast Fighter, White Weasel, Fairy Princess - Awakening
				useSkill(5753);
				break;
			case 1066: // Fox Shaman, Spirit Shaman - Thunder Bolt
				useSkill(5749);
				break;
			case 1067: // Fox Shaman, Spirit Shaman - Flash
				useSkill(5750);
				break;
			case 1068: // Fox Shaman, Spirit Shaman - Lightning Wave
				useSkill(5751);
				break;
			case 1069: // Fox Shaman, Fairy Princess - Flare
				useSkill(5752);
				break;
			case 1070: // White Weasel, Fairy Princess, Improved Baby Buffalo, Improved Baby Kookaburra, Improved Baby Cougar, Spirit Shaman, Toy Knight, Turtle Ascetic - Buff control
				useSkill(5771);
				break;
			case 1071: // Tigress - Power Strike
				useSkill(5761);
				break;
			case 1072: // Toy Knight - Piercing attack
				useSkill(6046);
				break;
			case 1073: // Toy Knight - Whirlwind
				useSkill(6047);
				break;
			case 1074: // Toy Knight - Lance Smash
				useSkill(6048);
				break;
			case 1075: // Toy Knight - Battle Cry
				useSkill(6049);
				break;
			case 1076: // Turtle Ascetic - Power Smash
				useSkill(6050);
				break;
			case 1077: // Turtle Ascetic - Energy Burst
				useSkill(6051);
				break;
			case 1078: // Turtle Ascetic - Shockwave
				useSkill(6052);
				break;
			case 1079: // Turtle Ascetic - Howl
				useSkill(6053);
				break;
			case 1080: // Phoenix Rush
				useSkill(6041);
				break;
			case 1081: // Phoenix Cleanse
				useSkill(6042);
				break;
			case 1082: // Phoenix Flame Feather
				useSkill(6043);
				break;
			case 1083: // Phoenix Flame Beak
				useSkill(6044);
				break;
			case 1084: // Switch State
				//useSkill(6054);
				if (pet != null && pet instanceof L2BabyPetInstance)
				{
					((L2BabyPetInstance) pet).switchMode();
				}
				break;
			case 1086: // Panther Cancel
				useSkill(6094);
				break;
			case 1087: // Panther Dark Claw
				useSkill(6095);
				break;
			case 1088: // Panther Fatal Claw
				useSkill(6096);
				break;
			case 1089: // Deinonychus - Tail Strike
				useSkill(6199);
				break;
			case 1090: // Guardian's Strider - Strider Bite
				useSkill(6205);
				break;
			case 1091: // Guardian's Strider - Strider Fear
				useSkill(6206);
				break;
			case 1092: // Guardian's Strider - Strider Dash
				useSkill(6207);
				break;
			case 1093: // Maguen - Maguen Strike
				useSkill(6618);
				break;
			case 1094: // Maguen - Maguen Wind Walk
				useSkill(6681);
				break;
			case 1095: // Elite Maguen - Maguen Power Strike
				useSkill(6619);
				break;
			case 1096: // Elite Maguen - Elite Maguen Wind Walk
				useSkill(6682);
				break;
			case 1097: // Maguen - Maguen Return
				useSkill(6683);
				break;
			case 1098: // Elite Maguen - Maguen Party Return
				useSkill(6684);
				break;
			case 1103: // TODO: Passive mode
			case 1104: // TODO: Defend mode
				getClient().getActiveChar()
						.setIsSummonsInDefendingMode(!getClient().getActiveChar().getIsSummonsInDefendingMode());
				break;
			case 1106: // Bear Claw
				useSkill(11278);
				break;
			case 1107: // Bear Tumbling
				useSkill(11279);
				break;
			case 1108: // Cougar Bite
				useSkill(11280);
				break;
			case 1109: // Cougar Pounce
				useSkill(11281);
				break;
			case 1110: // Reaper Touch
				useSkill(11282);
				break;
			case 1111: // Reaper Power
				useSkill(11283);
				break;
			case 1113: // Lion's Roar
				useSkill(10051);
				break;
			case 1114: // Lion's Claw
				useSkill(10052);
				break;
			case 1115: // Lion's Dash
				useSkill(10053);
				break;
			case 1116: // Lion's Red Flame
				useSkill(10054);
				break;
			case 1117: // Thunder Flight
				useSkill(10794);
				break;
			case 1118: // Thunder Purity
				useSkill(10795);
				break;
			case 1120: // Thunder Feather
				useSkill(10797);
				break;
			case 1121: // Thunder Sharp Claw
				useSkill(10798);
				break;
			case 1122: // Blessing of Life
				//useSkill(11806); //LasTravel this skill now it's self casted by the Tree of life
				break;
			case 1123: // Siege Golem - Siege Punch
				useSkill(14767);
				break;
			case 1124: //Feline Aggression
				useSkill(11323);
				break;
			case 1125: //Feline Stun
				useSkill(11324);
				break;
			case 1126: //Feline Bite
				useSkill(11325);
				break;
			case 1127: //Feline Pounce
				useSkill(11326);
				break;
			case 1128: //Feline Touch
				useSkill(11327);
				break;
			case 1129: //Feline power
				useSkill(11328);
				break;
			case 1130: //Unicorn's Aggression
				useSkill(11332);
				break;
			case 1131: //Unicorn's Stun
				useSkill(11333);
				break;
			case 1132: //Unicorn's Bite
				useSkill(11334);
				break;
			case 1133: //Unicorn's Pounce
				useSkill(11335);
				break;
			case 1134: //Unicorn's Touch
				useSkill(11336);
				break;
			case 1135: //Unicorn's Power
				useSkill(11337);
				break;
			case 1136: //Phantom Aggression
				useSkill(11341);
				break;
			case 1137: //Phantom Stun
				useSkill(11342);
				break;
			case 1138: //Phantom Bite
				useSkill(11343);
				break;
			case 1139: //Phantom Pounce
				useSkill(11344);
				break;
			case 1140: //Phantom Touch
				useSkill(11345);
				break;
			case 1141: //Phantom Power
				useSkill(11346);
				break;
			case 1142: //Panther Roar
				useSkill(10087);
				break;
			case 1143: //Panther Rush
				useSkill(10088);
				break;
			case 1144:
				useSkill(11375); //Commando Jumping Attack
				break;
			case 1145:
				useSkill(11376); //Commando Double Smash
				break;
			case 1146:
				useSkill(11378); //Elemental Slam
				break;
			case 1147:
				useSkill(11377); //Witch Cat Power
				break;
			case 1148:
				useSkill(11379); //Lancer Rush
				break;
			case 1149:
				useSkill(11380); //Power Stump
				break;
			case 1150:
				useSkill(11382); //Multiple Icicles
				break;
			case 1151:
				useSkill(11381); //Cherub Power
				break;
			case 1152:
				useSkill(11383); //Phantom Sword Attack
				break;
			case 1153:
				useSkill(11384); //Phantom Blow
				break;
			case 1154:
				useSkill(11385); //Phantom Spike
				break;
			case 1155:
				useSkill(11386); //Phantom Crash
				break;
			case 5000: // Baby Rudolph - Reindeer Scratch
				useSkill(23155);
				break;
			case 5001: // Deseloph, Hyum, Rekang, Lilias, Lapham, Mafum - Rosy Seduction
				useSkill(23167);
				break;
			case 5002: // Deseloph, Hyum, Rekang, Lilias, Lapham, Mafum - Critical Seduction
				useSkill(23168);
				break;
			case 5003: // Hyum, Lapham, Hyum, Lapham - Thunder Bolt
				useSkill(5749);
				break;
			case 5004: // Hyum, Lapham, Hyum, Lapham - Flash
				useSkill(5750);
				break;
			case 5005: // Hyum, Lapham, Hyum, Lapham - Lightning Wave
				useSkill(5751);
				break;
			case 5006: // Deseloph, Hyum, Rekang, Lilias, Lapham, Mafum, Deseloph, Hyum, Rekang, Lilias, Lapham, Mafum - Buff Control
				useSkill(5771);
				break;
			case 5007: // Deseloph, Lilias, Deseloph, Lilias - Piercing Attack
				useSkill(6046);
				break;
			case 5008: // Deseloph, Lilias, Deseloph, Lilias - Spin Attack
				useSkill(6047);
				break;
			case 5009: // Deseloph, Lilias, Deseloph, Lilias - Smash
				useSkill(6048);
				break;
			case 5010: // Deseloph, Lilias, Deseloph, Lilias - Ignite
				useSkill(6049);
				break;
			case 5011: // Rekang, Mafum, Rekang, Mafum - Power Smash
				useSkill(6050);
				break;
			case 5012: // Rekang, Mafum, Rekang, Mafum - Energy Burst
				useSkill(6051);
				break;
			case 5013: // Rekang, Mafum, Rekang, Mafum - Shockwave
				useSkill(6052);
				break;
			case 5014: // Rekang, Mafum, Rekang, Mafum - Ignite
				useSkill(6053);
				break;
			case 5015: // Deseloph, Hyum, Rekang, Lilias, Lapham, Mafum, Deseloph, Hyum, Rekang, Lilias, Lapham, Mafum - Switch Stance
				useSkill(6054);
				break;
			// Social Packets
			case 12: // Greeting
				tryBroadcastSocial(2);
				break;
			case 13: // Victory
				tryBroadcastSocial(3);
				break;
			case 14: // Advance
				tryBroadcastSocial(4);
				break;
			case 24: // Yes
				tryBroadcastSocial(6);
				break;
			case 25: // No
				tryBroadcastSocial(5);
				break;
			case 26: // Bow
				tryBroadcastSocial(7);
				break;
			case 29: // Unaware
				tryBroadcastSocial(8);
				break;
			case 30: // Social Waiting
				tryBroadcastSocial(9);
				break;
			case 31: // Laugh
				tryBroadcastSocial(10);
				break;
			case 33: // Applaud
				tryBroadcastSocial(11);
				break;
			case 34: // Dance
				tryBroadcastSocial(12);
				break;
			case 35: // Sorrow
				tryBroadcastSocial(13);
				break;
			case 62: // Charm
				tryBroadcastSocial(14);
				break;
			case 66: // Shyness
				tryBroadcastSocial(15);
				break;
			case 76: // Friend Invite
				if (!(target instanceof L2PcInstance))
				{
					return;
				}
				SystemMessage sm;
				// can't use friend invite for locating invisible characters
				if (!target.getActingPlayer().isOnline() || target.getActingPlayer().getAppearance().getInvisible())
				{
					//Target is not found in the game.
					activeChar.sendPacket(SystemMessageId.THE_USER_YOU_REQUESTED_IS_NOT_IN_GAME);
					return;
				}
				else if (target == activeChar)
				{
					//You cannot add yourself to your own friend list.
					activeChar.sendPacket(SystemMessageId.YOU_CANNOT_ADD_YOURSELF_TO_OWN_FRIEND_LIST);
					return;
				}
				else if (BlockList.isBlocked(activeChar, target.getActingPlayer()))
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.BLOCKED_C1);
					sm.addCharName(target.getActingPlayer());
					activeChar.sendPacket(sm);
					return;
				}
				else if (BlockList.isBlocked(target.getActingPlayer(), activeChar))
				{
					activeChar.sendMessage("You are in target's block list.");
					return;
				}
				else if (activeChar.isInOlympiadMode() || target.getActingPlayer().isInOlympiadMode())
				{
					//activeChar.sendPacket(SystemMessageId.A_USER_CURRENTLY_PARTICIPATING_IN_THE_OLYMPIAD_CANNOT_SEND_PARTY_AND_FRIEND_INVITATIONS);
					return;
				}

				if (activeChar.getFriendList().contains(target.getObjectId()))
				{
					// Player already is in your friendlist
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_IN_FRIENDS_LIST);
					sm.addString(target.getName());
					activeChar.sendPacket(sm);
					return;
				}

				if (!target.getActingPlayer().isProcessingRequest())
				{
					// requets to become friend
					activeChar.onTransactionRequest(target.getActingPlayer());
					sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_REQUESTED_C1_TO_BE_FRIEND);
					sm.addString(target.getName());
					FriendAddRequest ajf = new FriendAddRequest(activeChar.getName());
					target.sendPacket(ajf);
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER);
					sm.addString(target.getName());
				}
				activeChar.sendPacket(sm);

				break;
			case 78:
			case 79:
			case 80:
			case 81:
				useTacticalSign(_actionId - 77, false);
				break;
			case 82:
			case 83:
			case 84:
			case 85:
				useTacticalSign(_actionId - 81, true);
				break;
			case 87: // Propose
				tryBroadcastSocial(28);
				break;
			case 88: // Provoke
				tryBroadcastSocial(29);
				break;
			case 89: // Show off
				tryBroadcastSocial(30);
				break;
			case 90:
				activeChar.sendPacket(new ExInstanceList(activeChar));
				break;
			case 11334:
			case 11335:
				break;
			default:
				Log.warning(activeChar.getName() + ": unhandled action type " + _actionId);
		}
	}

	/*
	 * Cast a skill for active pet/servitor.
	 * Target is specified as a parameter but can be
	 * overwrited or ignored depending on skill type.
	 */
	private void useSkill(int skillId, L2Object target)
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		if (activeChar.getPrivateStoreType() != 0)
		{
			activeChar.sendMessage("Cannot use skills while trading");
			return;
		}

		List<L2Summon> summons = new ArrayList<>(activeChar.getSummons());
		summons.add(activeChar.getPet());
		for (L2Summon summon : summons)
		{
			if (summon == null || summon.isBetrayed())
			{
				continue;
			}

			int lvl;
			if (summon instanceof L2PetInstance)
			{
				if (summon.getLevel() - activeChar.getLevel() > 20)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_TOO_HIGH_TO_CONTROL));
					continue;
				}
				lvl = PetDataTable.getInstance().getPetData(summon.getNpcId())
						.getAvailableLevel(skillId, summon.getLevel());
			}
			else
			{
				lvl = summon.getSkillLevelHash(skillId);
			}

			if (lvl <= 0)
			{
				continue;
			}

			L2Skill skill = SkillTable.getInstance().getInfo(skillId, lvl);
			if (skill == null || skill.isOffensive() && activeChar == target)
			{
				continue;
			}

			summon.setTarget(target);
			//summon.setLastActionId(_actionId);
			summon.useMagic(skill, _ctrlPressed, _shiftPressed);
		}
	}

	/*
	 * Cast a skill for active pet/servitor.
	 * Target is retrieved from owner' target,
	 * then validated by overloaded method useSkill(int, L2Character).
	 */
	private void useSkill(int skillId)
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		useSkill(skillId, activeChar.getTarget());
	}

	/*
	 * Check if player can broadcast SocialAction packet
	 */
	private void tryBroadcastSocial(int id)
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		if (Config.DEBUG)
		{
			Log.fine("Social Action:" + id);
		}

		if (activeChar.isFishing())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_DO_WHILE_FISHING_3));
			return;
		}

		if (activeChar.canMakeSocialAction())
		{
			activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), id));
			if (activeChar.isPlayingEvent() && activeChar.getEvent() instanceof SimonSays)
			{
				((SimonSays) activeChar.getEvent()).onSocialAction(activeChar, id);
			}
			if (activeChar.getTarget() != null && activeChar.getTarget() instanceof L2Npc &&
					((L2Npc) activeChar.getTarget()).getTemplate().getEventQuests(QuestEventType.ON_SOCIAL_ACTION) !=
							null)
			{
				for (Quest quest : ((L2Npc) activeChar.getTarget()).getTemplate()
						.getEventQuests(QuestEventType.ON_SOCIAL_ACTION))
				{
					quest.notifySocialAction((L2Npc) activeChar.getTarget(), activeChar, id);
				}
			}
		}
	}

	private void useCoupleSocial(int id)
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		L2Object target = activeChar.getTarget();
		if (!(target instanceof L2PcInstance))
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		L2PcInstance player = (L2PcInstance) target;
		if (activeChar.isFishing() || player.isFishing())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_DO_WHILE_FISHING_3));
			return;
		}

		double distance = activeChar.getPlanDistanceSq(player);
		if (distance > 2000 || distance < 70)
		{
			activeChar.sendPacket(SystemMessageId.TARGET_DO_NOT_MEET_LOC_REQUIREMENTS);
			return;
		}

		if (activeChar.canMakeSocialAction() && player.canMakeSocialAction())
		{
			activeChar.setMultiSocialAction(id, player.getObjectId());
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_REQUESTED_COUPLE_ACTION_C1);
			sm.addPcName(player);
			activeChar.sendPacket(sm);
			player.sendPacket(new ExAskCoupleAction(activeChar.getObjectId(), id));
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.COUPLE_ACTION_CANCELED);
		}
	}

	private void useTacticalSign(int id, boolean target)
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		L2Party party = activeChar.getParty();
		if (party == null)
		{
			return;
		}

		if (target)
		{
			int objId = party.getTaggedChar(id);
			if (objId == 0)
			{
				return;
			}

			L2Object targetChar = activeChar.getKnownList().getKnownObjects().get(objId);
			if (targetChar == null)
			{
				if (activeChar.getObjectId() == objId)
				{
					targetChar = activeChar;
				}
				else
				{
					return;
				}
			}

			if (targetChar instanceof L2PcInstance)
			{
				L2PcInstance targetPlayer = (L2PcInstance) targetChar;
				if (targetPlayer.getAppearance().getInvisible())
				{
					return;
				}
			}

			targetChar.onAction(activeChar);
			return;
		}

		L2Object targetChar = activeChar.getTarget();
		if (targetChar == null)
		{
			return;
		}

		if (targetChar instanceof L2PcInstance)
		{
			L2PcInstance targetPlayer = (L2PcInstance) targetChar;
			if (targetPlayer.getAppearance().getInvisible())
			{
				return;
			}
		}

		int tag = party.tagCharacter(targetChar.getObjectId(), id);

		party.broadcastToPartyMembers(new ExTacticalSign(targetChar.getObjectId(), tag));
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return _actionId != 10 && _actionId != 28;
	}
}
