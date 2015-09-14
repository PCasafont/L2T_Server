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
package l2server.gameserver.network;

import java.nio.ByteBuffer;

import l2server.Config;
import l2server.gameserver.network.L2GameClient.GameClientState;
import l2server.gameserver.network.clientpackets.*;
import l2server.log.Log;
import l2server.network.IClientFactory;
import l2server.network.IMMOExecutor;
import l2server.network.IPacketHandler;
import l2server.network.MMOConnection;
import l2server.network.ReceivablePacket;
import l2server.util.Util;

/**
 * Stateful Packet Handler<BR>
 * The Stateful approach prevents the server from handling inconsistent packets, examples:<BR>
 * <li>Clients sends a MoveToLocation packet without having a character attached. (Potential errors handling the packet).</li>
 * <li>Clients sends a RequestAuthLogin being already authed. (Potential exploit).</li>
 * <BR><BR>
 * Note: If for a given exception a packet needs to be handled on more then one state, then it should be added to all these states.
 * @author  KenM
 */
public final class L2GamePacketHandler implements IPacketHandler<L2GameClient>, IClientFactory<L2GameClient>, IMMOExecutor<L2GameClient>
{
	
	// implementation
	public ReceivablePacket<L2GameClient> handlePacket(ByteBuffer buf, L2GameClient client)
	{
		if (client.dropPacket())
			return null;
		
		int opcode = buf.get() & 0xFF;
		int id3;
		
		ReceivablePacket<L2GameClient> msg = null;
		GameClientState state = client.getState();
		
		switch (state)
		{
			case CONNECTED:
				switch (opcode)
				{
					case 0x0e:
						msg = new ProtocolVersion();
						break;
					case 0x2b:
						msg = new AuthLogin();
						break;
					default:
						printDebug(opcode, buf, state, client);
						break;
				}
				break;
			case AUTHED:
				switch (opcode)
				{
					case 0x00:
						msg = new Logout();
						break;
					case 0x0c:
						msg = new CharacterCreate();
						break;
					case 0x0d:
						msg = new CharacterDelete();
						break;
					case 0x12:
						msg = new CharacterSelect();
						break;
					case 0x13:
						msg = new NewCharacter();
						break;
					case 0x7b:
						msg = new CharacterRestore();
						break;
					case 0xd0:
						int id2 = -1;
						if (buf.remaining() >= 2)
						{
							id2 = buf.getShort() & 0xffff;
						}
						else
						{
							if (Config.PACKET_HANDLER_DEBUG)
								Log.warning("Client: "+client.toString()+" sent a 0xd0 without the second opcode.");
							break;
						}
						
						switch (id2)
						{
							case 0x33:
								msg = new RequestGotoLobby();
								break;
							case 0x36:
								//msg = new RequestGotoLobby();
								break;
								/*
								 * case 0x3d:
								 *
								 *  msg = new RequestAllFortressInfo();
								 *  break;
								 */
							case 0x5a:
								msg = new RequestExCubeGameChangeTeam();
								break;
							case 0xa2:
							case 0xa3:
								//unk
								break;
							case 0xa6:
								msg = new RequestEx2ndPasswordCheck();
								break;
							case 0xa7:
								msg = new RequestEx2ndPasswordVerify();
								break;
							case 0xa8:
								msg = new RequestEx2ndPasswordReq();
								break;
							case 0xa9:
								msg = new RequestCharacterNameCreatable();
								break;
							case 0xc8:
								//Log.info("NCGuardSendDataToServer");
								break;
							case 0x103:
								// Ignore
								break;
							default:
								printDebugDoubleOpcode(opcode, id2, buf, state, client);
						}
						break;
					default:
						printDebug(opcode, buf, state, client);
						break;
				}
				break;
			case IN_GAME:
				switch(opcode)
				{
					case 0x00:
						msg = new Logout();
						break;
					case 0x01:
						msg = new AttackRequest();
						break;
					case 0x03:
						msg = new RequestStartPledgeWar();
						break;
					case 0x04:
						msg = new RequestReplyStartPledgeWar();
						break;
					case 0x05:
						msg = new RequestStopPledgeWar();
						break;
					case 0x06: // RequestSCCheck
						msg = new RequestReplyStopPledgeWar();
						break;
					case 0x07:
						msg = new RequestSurrenderPledgeWar();
						break;
					case 0x08:
						msg = new RequestReplySurrenderPledgeWar();
						break;
					case 0x09:
						msg = new RequestSetPledgeCrest();
						break;
					case 0x0b:
						msg = new RequestGiveNickName();
						break;
					case 0x0f:
						msg = new MoveBackwardToLocation();
						break;
					case 0x10:
						// Say
						break;
					case 0x11:
						msg = new EnterWorld();
						break;
					case 0x12:
						// CharacterSelect, in case of player spam clicks on loginscreen
						break;
					case 0x14:
						msg = new RequestItemList();
						break;
					case 0x15:
						// RequestEquipItem
						break;
					case 0x16:
						msg = new RequestUnEquipItem();
						break;
					case 0x17:
						msg = new RequestDropItem();
						break;
					case 0x19:
						msg = new UseItem();
						break;
					case 0x1a:
						msg = new TradeRequest();
						break;
					case 0x1b:
						msg = new AddTradeItem();
						break;
					case 0x1c:
						msg = new TradeDone();
						break;
					case 0x1f:
						msg = new Action();
						break;
					case 0x22:
						msg = new RequestLinkHtml();
						break;
					case 0x23:
						msg = new RequestBypassToServer();
						break;
					case 0x24:
						msg = new RequestBBSwrite();
						break;
					case 0x25:
						// RequestCreatePledge
						break;
					case 0x26:
						msg = new RequestJoinPledge();
						break;
					case 0x27:
						msg = new RequestAnswerJoinPledge();
						break;
					case 0x28:
						msg = new RequestWithdrawalPledge();
						break;
					case 0x29:
						msg = new RequestOustPledgeMember();
						break;
					case 0x2c:
						msg = new RequestGetItemFromPet();
						break;
					case 0x2e:
						msg = new RequestAllyInfo();
						break;
					case 0x2f:
						msg = new RequestCrystallizeItem();
						break;
					case 0x30:
						msg = new RequestPrivateStoreManageSell();
						break;
					case 0x31:
						msg = new SetPrivateStoreListSell();
						break;
					case 0x32:
						msg = new AttackRequest();
						break;
					case 0x33:
						// RequestTeleportPacket
						break;
					case 0x34:
						//msg = new RequestSocialAction();
						client.handleCheat("Used obsolete RequestSocialAction packet");
						break;
					case 0x35:
						//msg = new ChangeMoveType2();
						client.handleCheat("Used obsolete ChangeMoveType packet");
						break;
					case 0x36:
						//msg = new ChangeWaitType2();
						client.handleCheat("Used obsolete ChangeWaitType packet");
						break;
					case 0x37:
						msg = new RequestSellItem();
						break;
					case 0x38:
						// RequestMagicSkillList
						break;
					case 0x39:
						msg = new RequestMagicSkillUse();
						break;
					case 0x3a: // SendApperingPacket
						msg = new Appearing();
						break;
					case 0x3b:
						if (Config.ALLOW_WAREHOUSE)
							msg = new SendWareHouseDepositList();
						break;
					case 0x3c:
						msg = new SendWareHouseWithDrawList();
						break;
					case 0x3d:
						msg = new RequestShortCutReg();
						break;
					case 0x3f:
						msg = new RequestShortCutDel();
						break;
					case 0x40:
						msg = new RequestBuyItem();
						break;
					case 0x41:
						// RequestDismissPledge
						break;
					case 0x42:
						msg = new RequestJoinParty();
						break;
					case 0x43:
						msg = new RequestAnswerJoinParty();
						break;
					case 0x44:
						msg = new RequestWithDrawalParty();
						break;
					case 0x45:
						msg = new RequestOustPartyMember();
						break;
					case 0x46:
						// RequestDismissParty
						break;
					case 0x47:
						msg = new CannotMoveAnymore();
						break;
					case 0x48:
						msg = new RequestTargetCanceld();
						break;
					case 0x49:
						msg = new Say2();
						break;
					case 0x4a:
						int id_2 = -1;
						if (buf.remaining() >= 2)
						{
							id_2 = buf.getShort() & 0xffff;
						}
						else
						{
							if (Config.PACKET_HANDLER_DEBUG)
								Log.warning("Client: "+client.toString()+" sent a 0x4a without the second opcode.");
							break;
						}
						switch (id_2)
						{
							case 0x00:
								// SuperCmdCharacterInfo
								break;
							case 0x01:
								// SuperCmdSummonCmd
								break;
							case 0x02:
								// SuperCmdServerStatus
								break;
							case 0x03:
								// SendL2ParamSetting
								break;
							case 0x97:
								msg = new RequestFriendDetailInfo();
								break;
							case 0x98:
								msg = new RequestUpdateFriendMemo();
								break;
							case 0x99:
								msg = new RequestUpdateBlockMemo();
								break;
							case 0x9a:
								//msg = new RequestInzonePartyInfoHistory(); // TODO: Remove comment tags when this will be created
								break;
							case 0xbc:
								//msg = new ConfirmMenteeAdd(); // TODO: Remove comment tags when this will be created
								break;
							case 0xbd:
								//msg = new RequestMentorCancel(); // TODO: Remove comment tags when this will be created
								break;
							case 0xbe:
								//msg = new RequestMentorList(); // TODO: Remove comment tags when this will be created
								break;
							case 0xbf:
								//msg = new RequestMenteeAdd(); // TODO: Remove comment tags when this will be created
								break;
							default:
								this.printDebugDoubleOpcode(opcode, id_2, buf, state, client);
								break;
						}
						break;
					case 0x4d:
						msg = new RequestPledgeMemberList();
						break;
					case 0x4f:
						//RequestMagicList
						break;
					case 0x50:
						msg = new RequestSkillList();
						break;
					case 0x52:
						msg = new MoveWithDelta();
						break;
					case 0x53:
						msg = new RequestGetOnVehicle();
						break;
					case 0x54:
						msg = new RequestGetOffVehicle();
						break;
					case 0x55:
						msg = new AnswerTradeRequest();
						break;
					case 0x56:
						msg = new RequestActionUse();
						break;
					case 0x57:
						msg = new RequestRestart();
						break;
					case 0x58:
						msg = new RequestSiegeInfo();
						break;
					case 0x59:
						msg = new ValidatePosition();
						break;
					case 0x5a:
						// RequestSEKCustom
						break;
					case 0x5b:
						msg = new StartRotating();
						break;
					case 0x5c:
						msg = new FinishRotating();
						break;
					case 0x5e:
						msg = new RequestShowBoard();
						break;
					case 0x5f:
						msg = new RequestEnchantItem();
						break;
					case 0x60:
						msg = new RequestDestroyItem();
						break;
					case 0x62:
						msg = new RequestQuestList();
						break;
					case 0x63: // RequestDestroyQuest
						msg = new RequestQuestAbort();
						break;
					case 0x65:
						msg = new RequestPledgeInfo();
						break;
					case 0x66:
						msg = new RequestPledgeExtendedInfo();
						break;
					case 0x67:
						msg = new RequestPledgeCrest();
						break;
					case 0x6a:
						//msg = new RequestFriendInfo();
						break;
					case 0x6b:
						msg = new RequestSendFriendMsg();
						break;
					case 0x6c:
						msg = new RequestShowMiniMap();
						break;
					case 0x6d:
						// RequestSendMsnChatLog
						break;
					case 0x6e: //RequestReload
						msg = new RequestRecordInfo();
						break;
					case 0x6f:
						msg = new RequestHennaEquip();
						break;
					case 0x70:
						msg = new RequestHennaRemoveList();
						break;
					case 0x71:
						msg = new RequestHennaItemRemoveInfo();
						break;
					case 0x72:
						msg = new RequestHennaRemove();
						break;
					case 0x73:
						msg = new RequestAcquireSkillInfo();
						break;
					case 0x74:
						msg = new SendBypassBuildCmd();
						break;
					case 0x75:
						msg = new RequestMoveToLocationInVehicle();
						break;
					case 0x76:
						msg = new CannotMoveAnymoreInVehicle();
						break;
					case 0x77:
						msg = new RequestFriendInvite();
						break;
					case 0x78: // RequestFriendAddReply
						msg = new RequestAnswerFriendInvite();
						break;
					case 0x79:
						msg = new RequestFriendList();
						break;
					case 0x7a:
						msg = new RequestFriendDel();
						break;
					case 0x7c:
						msg = new RequestAcquireSkill();
						break;
					case 0x7d:
						msg = new RequestRestartPoint();
						break;
					case 0x7e:
						msg = new RequestGMCommand();
						break;
					case 0x7f:
						msg = new RequestPartyMatchConfig();
						break;
					case 0x80:
						msg = new RequestPartyMatchList();
						break;
					case 0x81:
						msg = new RequestPartyMatchDetail();
						break;
					case 0x83:
						msg = new RequestPrivateStoreBuy();
						break;
					case 0x85:
						msg = new RequestTutorialLinkHtml();
						break;
					case 0x86:
						msg = new RequestTutorialPassCmdToServer();
						break;
					case 0x87:
						msg = new RequestTutorialQuestionMark();
						break;
					case 0x88:
						msg = new RequestTutorialClientEvent();
						break;
					case 0x89:
						msg = new RequestPetition();
						break;
					case 0x8a:
						msg = new RequestPetitionCancel();
						break;
					case 0x8b:
						msg = new RequestGmList();
						break;
					case 0x8c:
						msg = new RequestJoinAlly();
						break;
					case 0x8d:
						msg = new RequestAnswerJoinAlly();
						break;
					case 0x8e: // RequestWithdrawAlly
						msg = new AllyLeave();
						break;
					case 0x8f: // RequestOustAlly
						msg = new AllyDismiss();
						break;
					case 0x90:
						msg = new RequestDismissAlly();
						break;
					case 0x91:
						msg = new RequestSetAllyCrest();
						break;
					case 0x92:
						msg = new RequestAllyCrest();
						break;
					case 0x93:
						msg = new RequestChangePetName();
						break;
					case 0x94:
						msg = new RequestPetUseItem();
						break;
					case 0x95:
						msg = new RequestGiveItemToPet();
						break;
					case 0x96:
						msg = new RequestPrivateStoreQuitSell();
						break;
					case 0x97:
						msg = new SetPrivateStoreMsgSell();
						break;
					case 0x98:
						msg = new RequestPetGetItem();
						break;
					case 0x99:
						msg = new RequestPrivateStoreManageBuy();
						break;
					case 0x9a:
						msg = new SetPrivateStoreListBuy ();
						break;
					case 0x9c:
						msg = new RequestPrivateStoreQuitBuy();
						break;
					case 0x9d:
						msg = new SetPrivateStoreMsgBuy();
						break;
					case 0x9f: // SendPrivateStoreBuyList
						msg = new RequestPrivateStoreSell();
						break;
					case 0xa0:
						//SendTimeCheckPacket
						break;
					case 0xa6:
						// RequestSkillCoolTime
						break;
					case 0xa7:
						//msg = new RequestPackageSendableItemList();
						break;
					case 0xa8:
						// msg = new RequestPackageSend();
						break;
					case 0xa9:
						msg = new RequestBlock();
						break;
					case 0xaa:
						msg = new RequestSiegeInfo();
						break;
					case 0xab:
						msg = new RequestSiegeAttackerList();
						break;
					case 0xac:
						msg = new RequestSiegeDefenderList();
						break;
					case 0xad:
						msg = new RequestJoinSiege();
						break;
					case 0xae:
						msg = new RequestConfirmSiegeWaitingList();
						break;
					case 0xaf:
						// RequestSetCastleSiegeTime
						break;
					case 0xb0:
						msg = new MultiSellChoose();
						break;
					case 0xb1:
						// NetPing
						break;
					case 0xb2:
						// RequestRemainTime
						break;
					case 0xb3:
						msg = new BypassUserCmd();
						break;
					case 0xb4:
						msg = new SnoopQuit();
						break;
					case 0xb5:
						msg = new RequestRecipeBookOpen();
						break;
					case 0xb6: // RequestRecipeItemDelete
						msg = new RequestRecipeBookDestroy();
						break;
					case 0xb7:
						msg = new RequestRecipeItemMakeInfo();
						break;
					case 0xb8:
						msg = new RequestRecipeItemMakeSelf();
						break;
					case 0xb9:
						// RequestRecipeShopManageList
						break;
					case 0xba:
						msg = new RequestRecipeShopMessageSet();
						break;
					case 0xbb:
						msg = new RequestRecipeShopListSet();
						break;
					case 0xbc:
						//msg = new RequestRecipeShopManageQuit();
						break;
					case 0xbd:
						// RequestRecipeShopManageCancel
						break;
					case 0xbe:
						msg = new RequestRecipeShopMakeInfo();
						break;
					case 0xbf:
						msg = new RequestRecipeShopMakeItem();
						break;
					case 0xc0: // RequestRecipeShopSellList
						msg = new RequestRecipeShopManagePrev();
						break;
					case 0xc1: // RequestObserverEndPacket
						msg = new ObserverReturn();
						break;
					case 0xc2:
						// Unused (RequestEvaluate/VoteSociality)
						break;
					case 0xc3: // RequestHennaItemList
						msg = new RequestHennaDrawList();
						break;
					case 0xc4:
						msg = new RequestHennaItemDrawInfo();
						break;
					case 0xc5:
						msg = new RequestBuySeed();
						break;
					case 0xc6: // ConfirmDlg
						msg = new DlgAnswer();
						break;
					case 0xc7: // RequestPreviewItem
						msg = new RequestPreviewItem();
						break;
					case 0xc8:
						msg = new RequestSSQStatus();
						break;
					case 0xc9:
						// PetitionVote
						break;
					case 0xcb:
						msg = new GameGuardReply();
						break;
					case 0xcc:
						msg = new RequestPledgePower();
						break;
					case 0xcd:
						msg = new RequestMakeMacro();
						break;
					case 0xce:
						msg = new RequestDeleteMacro();
						break;
					case 0xcf: // RequestProcureCrop
						//msg = new RequestBuyProcure();
						break;
						
					case 0xd0:
						int id2 = -1;
						if (buf.remaining() >= 2)
						{
							id2 = buf.getShort() & 0xffff;
						}
						else
						{
							if (Config.PACKET_HANDLER_DEBUG)
								Log.warning("Client: "+client.toString()+" sent a 0xd0 without the second opcode.");
							break;
						}
						
						switch (id2)
						{
							case 0x01:
								msg = new RequestManorList();
								break;
							case 0x02:
								msg = new RequestProcureCropList();
								break;
							case 0x03:
								msg = new RequestSetSeed();
								break;
							case 0x04:
								msg = new RequestSetCrop();
								break;
							case 0x05:
								msg = new RequestWriteHeroWords();
								break;
							case 0x06:
								msg = new RequestExAskJoinMPCC();
								break;
							case 0x07:
								msg = new RequestExAcceptJoinMPCC();
								break;
							case 0x08:
								msg = new RequestExOustFromMPCC();
								break;
							case 0x09:
								msg = new RequestOustFromPartyRoom();
								break;
							case 0x0a:
								msg = new RequestDismissPartyRoom();
								break;
							case 0x0b:
								msg = new RequestWithdrawPartyRoom();
								break;
							case 0x0c: // RequestHandOverPartyMaster
								msg = new RequestChangePartyLeader();
								break;
							case 0x0d:
								msg = new RequestAutoSoulShot();
								break;
							case 0x0e:
								msg = new RequestExEnchantSkillInfo();
								break;
							case 0x0f:
								msg = new RequestExEnchantSkill();
								break;
							case 0x10:
								msg = new RequestExPledgeCrestLarge();
								break;
							case 0x11:
								msg = new RequestExSetPledgeCrestLarge();
								break;
							case 0x12:
								msg = new RequestPledgeSetAcademyMaster();
								break;
							case 0x13:
								msg = new RequestPledgePowerGradeList();
								break;
							case 0x14:
								msg = new RequestPledgeMemberPowerInfo();
								break;
							case 0x15:
								msg = new RequestPledgeSetMemberPowerGrade();
								break;
							case 0x16:
								msg = new RequestPledgeMemberInfo();
								break;
							case 0x17:
								msg = new RequestPledgeWarList();
								break;
							case 0x18:
								msg = new RequestExFishRanking();
								break;
							case 0x19:
								msg = new RequestPCCafeCouponUse();
								break;
							case 0x1a:
								//unk
								break;
							case 0x1b:
								msg = new RequestDuelStart();
								break;
							case 0x1c:
								msg = new RequestDuelAnswerStart();
								break;
							case 0x1d:
								// RequestExSetTutorial
								break;
							case 0x1e:
								msg = new RequestExRqItemLink();
								break;
							case 0x1f:
								// CanNotMoveAnymoreAirShip
								break;
							case 0x20:
								msg = new MoveToLocationInAirShip();
								break;
							case 0x21:
								msg = new RequestKeyMapping();
								break;
							case 0x22:
								msg = new RequestSaveKeyMapping();
								break;
							case 0x23:
								msg = new RequestExRemoveItemAttribute();
								break;
							case 0x24:
								msg = new RequestSaveInventoryOrder();
								break;
							case 0x25:
								msg = new RequestExitPartyMatchingWaitingRoom();
								break;
							case 0x26:
								msg = new RequestConfirmTargetItem();
								break;
							case 0x27:
								msg = new RequestConfirmRefinerItem();
								break;
							case 0x28:
								msg = new RequestConfirmGemStone();
								break;
							case 0x29:
								msg = new RequestOlympiadObserverEnd();
								break;
							case 0x2a:
								msg = new RequestCursedWeaponList();
								break;
							case 0x2b:
								msg = new RequestCursedWeaponLocation();
								break;
							case 0x2c:
								msg = new RequestPledgeReorganizeMember();
								break;
							case 0x2d:
								msg = new RequestExMPCCShowPartyMembersInfo();
								break;
							case 0x2e:
								msg = new RequestOlympiadMatchList();
								break;
							case 0x2f:
								msg = new RequestAskJoinPartyRoom();
								break;
							case 0x30:
								msg = new AnswerJoinPartyRoom();
								break;
							case 0x31:
								msg = new RequestListPartyMatchingWaitingRoom();
								break;
							/*case 0x32:
								msg = new RequestExEnchantSkillSafe();
								break;
							case 0x33:
								msg = new RequestExEnchantSkillUntrain();
								break;
							case 0x34:
								msg = new RequestExEnchantSkillRouteChange();
								break;*/
							case 0x32:
								msg = new RequestExEnchantItemAttribute();
								break;
							case 0x33:
								msg = new ExGetOnAirShip();
								break;
							case 0x35:
								msg = new MoveToLocationAirShip();
								break;
							case 0x36:
								msg = new RequestBidItemAuction();
								break;
							case 0x37:
								msg = new RequestInfoItemAuction();
								break;
							case 0x38:
								msg = new RequestExChangeName();
								break;
							case 0x39:
								msg = new RequestAllCastleInfo();
								break;
							case 0x3a:
								msg = new RequestAllFortressInfo();
								break;
							case 0x3b:
								msg = new RequestAllAgitInfo();
								break;
							case 0x3c:
								msg = new RequestFortressSiegeInfo();
								break;
							case 0x3d:
								msg = new RequestGetBossRecord();
								break;
							case 0x3e:
								msg = new RequestRefine();
								break;
							case 0x3f:
								msg = new RequestConfirmCancelItem();
								break;
							case 0x40:
								msg = new RequestRefineCancel();
								break;
							case 0x41:
								msg = new RequestExMagicSkillUseGround();
								break;
							case 0x42:
								msg = new RequestDuelSurrender();
								break;
							case 0x43:
								msg = new RequestExEnchantSkillInfoDetail();
								break;
							case 0x45:
								msg = new RequestFortressMapInfo();
								break;
							case 0x46:
								// RequestPVPMatchRecord
								break;
							case 0x47:
								msg = new SetPrivateStoreWholeMsg();
								break;
							case 0x48:
								msg = new RequestDispel();
								break;
							case 0x49:
								msg = new RequestExTryToPutEnchantTargetItem();
								break;
							case 0x4a:
								msg = new RequestExTryToPutEnchantSupportItem();
								break;
							case 0x4b:
								msg = new RequestExCancelEnchantItem();
								break;
							case 0x4c:
								msg = new RequestChangeNicknameColor();
								break;
							case 0x4d:
								msg = new RequestResetNickname();
								break;
							case 0x4e:
								id3 = 0;
								if (buf.remaining() >= 4)
								{
									id3 = buf.getInt();
								}
								else
								{
									Log.warning("Client: "+client.toString()+" sent a 0xd0:0x51 without the third opcode.");
									break;
								}
								switch (id3)
								{
									case 0x00:
										msg = new RequestBookMarkSlotInfo();
										break;
									case 0x01:
										msg = new RequestSaveBookMarkSlot();
										break;
									case 0x02:
										msg = new RequestModifyBookMarkSlot();
										break;
									case 0x03:
										msg = new RequestDeleteBookMarkSlot();
										break;
									case 0x04:
										msg = new RequestTeleportBookMark();
										break;
									case 0x05:
										// RequestChangeBookMarkSlot
										break;
									default:
										this.printDebugDoubleOpcode(opcode, id3, buf, state, client);
										break;
								}
								break;
							case 0x4f:
								msg = new RequestWithDrawPremiumItem();
								break;
							case 0x50:
								// RequestJump
								break;
							case 0x51:
								// RequestStartShowCrataeCubeRank
								break;
							case 0x52:
								// RequestStopShowCrataeCubeRank
								break;
							case 0x53:
								// NotifyStartMiniGame
								break;
							case 0x54:
								// RequestJoinDominionWar
								msg = new RequestJoinDominionWar();
								break;
							case 0x55:
								msg = new RequestDominionInfo();
								break;
							case 0x56:
								// RequestExCleftEnter
								break;
							case 0x57:
								msg = new RequestExCubeGameChangeTeam();
								break;
							case 0x58:
								msg = new EndScenePlayer();
								break;
							case 0x59:
								msg = new RequestExCubeGameReadyAnswer();
								break;
							case 0x5a:
								msg = new RequestListMpccWaiting();
								break;
							case 0x5c:
								//msg = new RequestJoinMpccRoom();
								break;
							case 0x5e:
								// Unknown
								break;
							case 0x60:
								msg = new RequestSeedPhase();
								break;
							case 0x62:
								msg = new RequestPostItemList();
								break;
							case 0x63:
								msg = new RequestSendPost();
								break;
							case 0x64:
								msg = new RequestReceivedPostList();
								break;
							case 0x65:
								msg = new RequestDeleteReceivedPost();
								break;
							case 0x66:
								msg = new RequestReceivedPost();
								break;
							case 0x67:
								msg = new RequestPostAttachment();
								break;
							case 0x68:
								msg = new RequestRejectPostAttachment();
								break;
							case 0x69:
								msg = new RequestSentPostList();
								break;
							case 0x6a:
								msg = new RequestDeleteSentPost();
								break;
							case 0x6b:
								msg = new RequestSentPost();
								break;
							case 0x6c:
								msg = new RequestCancelPost();
								break;
							case 0x6d:
								// RequestShowNewUserPetition
								break;
							case 0x6e:
								// RequestShowStepThree
								break;
							case 0x6f:
								// RequestShowStepTwo
								break;
							case 0x70:
								// ExRaidReserveResult
								break;
							case 0x72:
								msg = new RequestRefundItem();
								break;
							case 0x73:
								msg = new RequestBuySellUIClose();
								break;
							case 0x74:
								// RequestEventMatchObserverEnd
								break;
							case 0x75:
								msg = new RequestPartyLootModification();
								break;
							case 0x76:
								msg = new AnswerPartyLootModification();
								break;
							case 0x77:
								msg = new AnswerCoupleAction();
								break;
							case 0x78:
								msg = new BrEventRankerList();
								break;
							case 0x79:
								// AskMembership
								break;
							case 0x7a:
								// RequestAddExpandQuestAlarm
								break;
							case 0x7b:
								// RequestVoteNew
								msg = new RequestVoteNew();
								break;
							case 0x7c:
								msg = new ExGetOnShuttle();
								break;
							case 0x7d:
								msg = new ExGetOffShuttle();
								break;
							case 0x7e:
								msg = new MoveToLocationInShuttle();
								break;
							case 0x7f:
								// Unknown
								break;
							case 0x81:
								msg = new RequestExAddContactToContactList();
								break;
							case 0x82:
								msg = new RequestExDeleteContactFromContactList();
								break;
							case 0x83:
								msg = new RequestExShowContactList();
								break;
							case 0x84:
								msg = new RequestExFriendListExtended();
								break;
							case 0x85:
								msg = new RequestExMoveToArena();
								break;
							case 0x86:
								// RequestBRGamePoint
								break;
							case 0x87:
								// Request BRProductList
								break;
							case 0x88:
								// RequestBRProductInfo
								break;
							case 0x89:
								// RequestBRBuyProduct
								break;
							case 0x8a:
								// RequestBRRecentProductList
								break;
							case 0x8b:
								// BrMinigameLoadScores
								break;
							case 0x8c:
								// BrMinigameInsertScore
								break;
							case 0x8e:
								msg = new RequestCrystallizeEstimate();
								break;
							case 0x8f:
								msg = new RequestCrystallizeItemCancel(); 
								break;
							case 0x90:
								//Log.info("RequestExEscapeScene");
								//msg = new RequestExEscapeScene();
								// RequestEx2ndPasswordCheck 
								break;
							case 0x91:
								msg = new RequestFlyMove();
								break;
							case 0x92:
								msg = new RequestSurrenderPledgeWarEx();
								break;
							case 0x93:
								//Log.info("RequestDynamicQuest");
								//msg = new RequestDynamicQuest();
								break;
							case 0x94:
								msg = new RequestFriendDetailInfo();
								break;
							case 0x95:
								msg = new RequestUpdateFriendMemo();
								break;
							case 0x96:
								msg = new RequestUpdateBlockMemo();
								break;
							case 0x97:
								//msg = new RequestInzonePartyInfoHistory(); // getting when check char detailed info...
								break;
							case 0x98:
								msg = new RequestCommissionRegistrableItemList();
								break;
							case 0x99:
								msg = new RequestCommissionInfo();
								break;
							case 0x9a:
								//msg = new RequestCommissionRegister();
								break;
							case 0x9b:
								msg= new RequestCommissionCancel();
								break;
							case 0x9c:
								//msg= new RequestCommissionDelete();
								break;
							case 0x9d:
								//msg= new RequestCommissionList();
								break;
							case 0x9e:
								//msg= new RequestCommissionBuyInfo();
								break;
							case 0x9f:
								//msg= new RequestCommissionBuyItem();
								break;
							case 0xa0:
								//msg= new RequestCommissionRegisteredItem();
								break;
							case 0xa1:
								msg = new RequestCallToChangeClass();
								break;
							case 0xa2:
								msg = new RequestChangeToAwakenedClass();
								break;
							/*case 0xa3:
								msg = new RequestWorldStatistics();
								break;
							case 0xa4:
								msg = new RequestUserStatistics();
								break;
							case 0xa5:
								msg= new RequestRegistPartySubstitute();
								break;
							case 0xa6:
								msg= new RequestDeletePartySubstitute();
								break;
							case 0xa7:
								msg= new RequestRegistWaitingSubstitute();
								break;
							case 0xa8:
								msg= new RequestAcceptWaitingSubstitute();
								break;
							case 0xa9:
								//Log.info("Request24HzSessionID");
								//msg = new Request24HzSessionID(); // TODO: Remove comment tags when this will be created
								break;*/
							case 0xa6:
								// RequestEx2ndPasswordCheck, in case of player spam clicks on loginscreen
								break;
							case 0xa7:
								// RequestEx2ndPasswordVerify, in case of player spam clicks on loginscreen
								break;
							case 0xa8:
								// RequestEx2ndPasswordReq, in case of player spam clicks on loginscreen
								break;
							case 0xa9: 
								//Log.info("RequestGoodsInventoryInfo");
								//msg= new RequestGoodsInventoryInfo(); // TODO: Remove comment tags when this will be created
								break;
							case 0xaa: 
								//Log.info("RequestUseGoodsInventoryItem");
								//msg= new RequestUseGoodsInventoryItem(); // TODO: Remove comment tags when this will be created
								break;
							case 0xac:
								//Log.info("RequestFirstPlayStart");
								// RequestFirstPlayStart (ignore for now, since it looks useless for us)
								break;
							case 0xad:
								msg = new RequestFlyMoveStart();
								break;
							/*case 0xb4:
								msg = new SendChangeAttributeTargetItem();
								break;
							case 0xb5:
								msg = new RequestChangeAttributeItem();
								break;
							case 0xb6:
								msg = new RequestChangeAttributeCancel();
								break;
							case 0xb7:
								//Log.info("RequestExchangeSubstitute");
								//msg = new RequestExchangeSubstitute();
								break;
							case 0xb8:
								//Log.info("RequestBR_PresentBuyProduct");
								//msg = new RequestBR_PresentBuyProduct();
								break;*/
							case 0xb4:
								msg = new ConfirmMenteeAdd();
								break;
							case 0xb5:
								msg = new RequestMentorCancel();
								break;
							case 0xb6:
								msg = new RequestMentorList();
								break;
							case 0xb7:
								msg = new RequestMenteeAdd();
								break;
							case 0xb8:
								msg = new RequestMenteeSearch();
								break;
							case 0xb9:
								//Log.info("RequestExTryToPut_Shape_Shifting_EnchantSupportItem");
								//msg = new RequestExTryToPut_Shape_Shifting_EnchantSupportItem();
								break;
							case 0xba:
								msg = new RequestInstanceList();
								break;
							case 0xbb:
								//Log.info("RequestShape_Shifting_Item");
								//msg = new RequestShape_Shifting_Item();
								break;
							case 0xbc:
								Log.info("Leave CoC registration");
								break;
							case 0xbd:
								Log.info("Leave CoC match");
								break;
							case 0xbe:
								// Something with Ceremony of Chaos with Teleport to Arena (when Observer mode is on)
								//msg = new RequestInstanceList();
								break;
							case 0xc0:
								// Something with Ceremony of Chaos with Return (when Observer mode is on)
								// msg = new RequestReturnFromObserver();
								break;
							case 0xc1:
								Log.info("Click on CoC invitation");
								break;
							case 0xc3:
								// Something with items shape when putting it there...
								break;
							case 0xc4:
								msg = new RequestPutTargetForItemAppearance();
								break;
							case 0xc5:
								msg = new RequestPutTemplateForItemAppearance();
								break;
							case 0xc6:
								msg = new RequestCancelItemAppearance();
								break;
							case 0xc7:
								Log.info("RequestShapeShiftingItem");
								break;
							case 0xc8:
							//	Log.info("NCGuardSendDataToServer");
								break;
							case 0xca:
								msg = new RequestShowBeautyList();
								break;
							case 0xcb:
								msg = new RequestRegistBeauty();
								break;
							/*case 0xce:
								Log.info("RequestUnionJoin");
								break;
							case 0xcf:
								Log.info("RequestUnionChange");
								break;
							case 0xd0:
								Log.info("RequestUnionWithdraw");
								break;
							case 0xd1:
								Log.info("RequestUnionRequest");
								break;
							case 0xd2:
								Log.info("RequestUnionAdjust");
								break;
							case 0xd3:
								Log.info("RequestUnionSummon");
								break;
							case 0xd4:
								Log.info("RequestUnionStart");
								break;
							case 0xd5:
								Log.info("RequestEventKalieToken (Click on Kaliel's Token obtain)");
								break;
							case 0xd6:
								Log.info("RequestShowPledgeUnionInfo");
								break;
							case 0xd7:
								Log.info("RequestEventCampaignPacket");
								break;
							case 0xd6:
								msg = new RequestBeautyList();
								break;
							case 0xd8:
								Log.info("RequestShowResetShopList");
								break;
							case 0xd9:
								Log.info("NetPing");
								break;
							case 0xda:
								Log.info("RequestBR_AddBasketProductInfo");
								break;
							case 0xdb:
								Log.info("RequestResetBeauty");
								break;*/
							case 0xd1:
								msg = new RequestBeautyList();
								break;
							case 0xd3:
								msg = new RequestPledgeRecruitInfo();
								break;
							case 0xd4:
								msg = new RequestPledgeRecruitBoardSearch();
								break;
							case 0xd5:
								msg = new RequestPledgeRecruitBoardAccess();
								break;
							case 0xd6:
								msg = new RequestPledgeRecruitBoardDetail();
								break;
							case 0xd7:
								msg = new RequestPledgeRecruitApply();
								break;
							case 0xd8:
								msg = new RequestPledgeApplication();
								break;
							case 0xd9:
								msg = new RequestPledgeApplicantList();
								break;
							case 0xda:
								msg = new RequestPledgeApplicant();
								break;
							case 0xdb:
								msg = new RequestPledgeApplicationAccept();
								break;
							case 0xdc:
								msg = new RequestPledgeWaitingListSearch();
								break;
							case 0xdd:
								msg = new RequestPledgeWaitingListApply();
								break;
							case 0xde:
								msg = new RequestPledgeRecruitApplyInfo();
								break;
							case 0xdf:
								Log.info("RequestPledgeJoinSys");
								break;
							case 0xe0:
								Log.info("ResponsePetitionAlarm");
								break;
							case 0xe1:
								//Log.info("NotifyExitBeautyshop");
								break;
							case 0xe2:
								Log.info("RequestRegisterXMasWishCard");
								break;
							case 0xe3:
								Log.info("RequestExAddEnchantScrollItem");
								break;
							case 0xe4:
								msg = new RequestExRemoveEnchantSupportItem();
								break;
							case 0xe5:
								Log.info("RequestCardReward");
								break;
							case 0xe6:
								msg = new RequestDivideAdena();
								break;
							case 0xe7:
								msg = new RequestDivideAdenaCancel();
								break;
							case 0xe8:
								msg = new RequestDivideAdenaConfirm();
								break;
							case 0xe9:
								msg = new RequestAPSkillAcquire();
								break;
							case 0xea:
								msg = new RequestAPSkillList();
								break;
							case 0xeb:
								msg = new RequestAPReset();
								break;
							case 0xec:
								msg = new RequestAPConvert();
								break;
							case 0xed:
								//Log.info("Unk FE:ED (" + client.toString() + ")");
								break;
							case 0xee:
								msg = new RequestAPShowListWnd();
								break;
							case 0xef:
								msg = new RequestAPCloseListWnd();
								break;
							case 0xf3:
								Log.info("RequestExitTrainingCamp");
								break;
							case 0xf4:
								msg = new RequestCompoundOne();
								break;
							case 0xf5:
								msg = new RequestCompoundOneRemove();
								break;
							case 0xf6:
								msg = new RequestCompoundTwo();
								break;
							case 0xf7:
								msg = new RequestCompoundTwoRemove();
								break;
							case 0xf8:
								msg = new RequestCompoundCancel();
								break;
							case 0xf9:
								msg = new RequestCompoundStart();
								break;
							case 0xfd:
								// Ignore
								break;
							case 0xfe:
								// Ignore
								break;
							case 0xff:
								// Ignore
								break;
							case 0x102:
								// Ignore
								break;
							case 0x103:
								// Ignore
								break;
							default:
								printDebugDoubleOpcode(opcode, id2, buf, state, client);
								break;
						}
						break;
						/*case 0xee:
						msg = new RequestChangePartyLeader(data, _client);
						break;*/
					default:
						printDebug(opcode, buf, state, client);
						break;
				}
				break;
		}
		return msg;
	}
	
	private void printDebug(int opcode, ByteBuffer buf, GameClientState state, L2GameClient client)
	{
		client.onUnknownPacket();
		if (!Config.PACKET_HANDLER_DEBUG)
			return;
		
		int size = buf.remaining();
		Log.warning("Unknown Packet: 0x"+Integer.toHexString(opcode)+" on State: "+state.name()+" Client: "+client.toString());
		byte[] array = new byte[size];
		buf.get(array);
		Log.warning(Util.printData(array, size));
	}
	
	private void printDebugDoubleOpcode(int opcode, int id2, ByteBuffer buf, GameClientState state, L2GameClient client)
	{
		/*client.onUnknownPacket();
		if (!Config.PACKET_HANDLER_DEBUG)
			return;*/
		
		int size = buf.remaining();
		Log.warning("Unknown Packet: 0x"+Integer.toHexString(opcode)+":0x" + Integer.toHexString(id2)+" on State: "+state.name()+" Client: "+client.toString());
		byte[] array = new byte[size];
		buf.get(array);
		Log.warning(Util.printData(array, size));
	}
	
	// impl
	public L2GameClient create(MMOConnection<L2GameClient> con)
	{
		return new L2GameClient(con);
	}
	
	public void execute(ReceivablePacket<L2GameClient> rp)
	{
		rp.getClient().execute(rp);
	}
}
