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

package l2server.gameserver.model.actor.instance;

import gnu.trove.TIntArrayList;
import l2server.Config;
import l2server.gameserver.datatables.ClanTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.datatables.TeleportLocationTable;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.L2TeleportLocation;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.model.itemcontainer.PcInventory;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.gameserver.templates.skills.L2SkillType;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.StringUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Castle Chamberlains implementation used for: - tax rate control - regional
 * manor system control - castle treasure control - ...
 */
public class L2CastleChamberlainInstance extends L2MerchantInstance
{
	protected static final int COND_ALL_FALSE = 0;
	protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	protected static final int COND_OWNER = 2;
	private int _preDay;
	private int _preHour;

	public L2CastleChamberlainInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2CastleChamberlainInstance);
	}

	private void sendHtmlMessage(L2PcInstance player, NpcHtmlMessage html)
	{
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcId%", String.valueOf(getNpcId()));
		player.sendPacket(html);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		// BypassValidation Exploit plug.
		if (player.getLastFolkNPC().getObjectId() != getObjectId())
		{
			return;
		}

		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		int condition = validateCondition(player);
		if (condition <= COND_ALL_FALSE)
		{
		}
		else if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
		{
		}
		else if (condition == COND_OWNER)
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			String actualCommand = st.nextToken(); // Get actual command

			String val = "";
			if (st.countTokens() >= 1)
			{
				val = st.nextToken();
			}

			if (actualCommand.equalsIgnoreCase("banish_foreigner"))
			{
				if (!validatePrivileges(player, L2Clan.CP_CS_DISMISS))
				{
					return;
				}
				if (siegeBlocksFunction(player))
				{
					return;
				}
				getCastle().banishForeigners(); // Move non-clan members off castle area
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(player.getHtmlPrefix(), "chamberlain/chamberlain-banishafter.htm");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
			}
			else if (actualCommand.equalsIgnoreCase("banish_foreigner_show"))
			{
				if (!validatePrivileges(player, L2Clan.CP_CS_DISMISS))
				{
					return;
				}
				if (siegeBlocksFunction(player))
				{
					return;
				}
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(player.getHtmlPrefix(), "chamberlain/chamberlain-banishfore.htm");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
			}
			else if (actualCommand.equalsIgnoreCase("list_siege_clans"))
			{
				if ((player.getClanPrivileges() & L2Clan.CP_CS_MANAGE_SIEGE) == L2Clan.CP_CS_MANAGE_SIEGE)
				{
					getCastle().getSiege().listRegisterClan(player); // List current register clan
				}
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile(player.getHtmlPrefix(), "chamberlain/chamberlain-noprivs.htm");
					player.sendPacket(html);
				}
			}
			else if (actualCommand.equalsIgnoreCase("receive_report"))
			{
				if (player.isClanLeader())
				{
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile(player.getHtmlPrefix(), "chamberlain/chamberlain-report.htm");
					html.replace("%objectId%", String.valueOf(getObjectId()));
					L2Clan clan = ClanTable.getInstance().getClan(getCastle().getOwnerId());
					html.replace("%clanname%", clan.getName());
					html.replace("%clanleadername%", clan.getLeaderName());
					html.replace("%castlename%", getCastle().getName());
					player.sendPacket(html);
				}
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile(player.getHtmlPrefix(), "chamberlain/chamberlain-noprivs.htm");
					player.sendPacket(html);
				}
			}
			else if (actualCommand.equalsIgnoreCase("items"))
			{
				if ((player.getClanPrivileges() & L2Clan.CP_CS_USE_FUNCTIONS) == L2Clan.CP_CS_USE_FUNCTIONS)
				{
					if (val.isEmpty())
					{
						return;
					}
					player.tempInventoryDisable();

					if (Config.DEBUG)
					{
						Log.fine("Showing chamberlain buylist");
					}

					showBuyWindow(player, Integer.parseInt(val + "1"));
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile(player.getHtmlPrefix(), "chamberlain/chamberlain-noprivs.htm");
					player.sendPacket(html);
				}
			}
			else if (actualCommand.equalsIgnoreCase("manage_siege_defender"))
			{
				if ((player.getClanPrivileges() & L2Clan.CP_CS_MANAGE_SIEGE) == L2Clan.CP_CS_MANAGE_SIEGE)
				{
					getCastle().getSiege().listRegisterClan(player);
				}
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile(player.getHtmlPrefix(), "chamberlain/chamberlain-noprivs.htm");
					player.sendPacket(html);
				}
			}
			else if (actualCommand.equalsIgnoreCase("manage_vault"))
			{
				if ((player.getClanPrivileges() & L2Clan.CP_CS_TAXES) == L2Clan.CP_CS_TAXES)
				{
					String filename = "chamberlain/chamberlain-vault.htm";
					long amount = 0;
					if (val.equalsIgnoreCase("deposit"))
					{
						try
						{
							String adenaAmount = "";
							if (st.hasMoreTokens())
							{
								adenaAmount = st.nextToken();
							}
							if (Util.isDigit(adenaAmount))
							{
								amount = Long.parseLong(adenaAmount);
							}
						}
						catch (NoSuchElementException e)
						{
							e.printStackTrace();
						}
						if (amount > 0 && getCastle().getTreasury() + amount < PcInventory.MAX_ADENA)
						{
							if (player.reduceAdena("Castle", amount, this, true))
							{
								getCastle().addToTreasuryNoTax(amount);
							}
							else
							{
								sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
							}
						}
					}
					else if (val.equalsIgnoreCase("withdraw"))
					{
						try
						{
							if (st.hasMoreTokens())
							{
								String var = st.nextToken();
								if (Util.isDigit(var))
								{
									amount = Long.parseLong(var);
								}
							}
						}
						catch (NoSuchElementException e)
						{
							e.printStackTrace();
						}
						if (amount > 0)
						{
							if (getCastle().getTreasury() < amount)
							{
								filename = "chamberlain/chamberlain-vault-no.htm";
							}
							else
							{
								if (getCastle().addToTreasuryNoTax(-1 * amount))
								{
									player.addAdena("Castle", amount, this, true);
								}
							}
						}
					}
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile(player.getHtmlPrefix(), filename);
					html.replace("%objectId%", String.valueOf(getObjectId()));
					html.replace("%npcname%", getName());
					html.replace("%tax_income%", Util.formatAdena(getCastle().getTreasury()));
					html.replace("%withdraw_amount%", Util.formatAdena(amount));
					player.sendPacket(html);
				}
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile(player.getHtmlPrefix(), "chamberlain/chamberlain-noprivs.htm");
					player.sendPacket(html);
				}
			}
			else if (actualCommand.equalsIgnoreCase("operate_door")) // door
			// control
			{
				if ((player.getClanPrivileges() & L2Clan.CP_CS_OPEN_DOOR) == L2Clan.CP_CS_OPEN_DOOR)
				{
					if (!val.isEmpty())
					{
						boolean open = Integer.parseInt(val) == 1;
						while (st.hasMoreTokens())
						{
							getCastle().openCloseDoor(player, Integer.parseInt(st.nextToken()), open);
						}

						NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
						String file = "chamberlain/doors-close.htm";
						if (open)
						{
							file = "chamberlain/doors-open.htm";
						}
						html.setFile(player.getHtmlPrefix(), file);
						html.replace("%objectId%", String.valueOf(getObjectId()));
						player.sendPacket(html);
						return;
					}

					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile(player.getHtmlPrefix(), "chamberlain/" + getCastle().getName() + "-d.htm");
					html.replace("%objectId%", String.valueOf(getObjectId()));
					html.replace("%npcname%", getName());
					player.sendPacket(html);
				}
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile(player.getHtmlPrefix(), "chamberlain/chamberlain-noprivs.htm");
					player.sendPacket(html);
				}
			}
			else if (actualCommand.equalsIgnoreCase("tax_set")) // tax rates
			// control
			{
				if ((player.getClanPrivileges() & L2Clan.CP_CS_TAXES) == L2Clan.CP_CS_TAXES)
				{
					if (!val.isEmpty())
					{
						getCastle().setTaxPercent(player, Integer.parseInt(val));
					}

					final String msg = StringUtil.concat("<html><body>", getName(), ":<br>" + "Current tax rate: ",
							String.valueOf(getCastle().getTaxPercent()),
							"%<br>" + "<table>" + "<tr>" + "<td>Change tax rate to:</td>" +
									"<td><edit var=\"value\" width=40><br>" +
									"<button value=\"Adjust\" action=\"bypass -h npc_%objectId%_tax_set $value\" width=80 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>" +
									"</tr>" + "</table>" + "</center>" + "</body></html>");
					sendHtmlMessage(player, msg);
				}
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile(player.getHtmlPrefix(), "chamberlain/chamberlain-tax.htm");
					html.replace("%objectId%", String.valueOf(getObjectId()));
					html.replace("%tax%", String.valueOf(getCastle().getTaxPercent()));
					player.sendPacket(html);
				}
			}
			else if (actualCommand.equalsIgnoreCase("manage_functions"))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(player.getHtmlPrefix(), "chamberlain/chamberlain-manage.htm");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
			}
			else if (actualCommand.equalsIgnoreCase("products"))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(player.getHtmlPrefix(), "chamberlain/chamberlain-products.htm");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%npcId%", String.valueOf(getNpcId()));
				player.sendPacket(html);
			}
			else if (actualCommand.equalsIgnoreCase("functions"))
			{
				if (val.equalsIgnoreCase("tele"))
				{
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					if (getCastle().getFunction(Castle.FUNC_TELEPORT) == null)
					{
						html.setFile(player.getHtmlPrefix(), "chamberlain/chamberlain-nac.htm");
					}
					else
					{
						html.setFile(player.getHtmlPrefix(), "chamberlain/" + getCastle().getName() + "-t" +
								getCastle().getFunction(Castle.FUNC_TELEPORT).getLvl() + ".htm");
					}
					sendHtmlMessage(player, html);
				}
				else if (val.equalsIgnoreCase("support"))
				{
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					if (getCastle().getFunction(Castle.FUNC_SUPPORT) == null)
					{
						html.setFile(player.getHtmlPrefix(), "chamberlain/chamberlain-nac.htm");
					}
					else
					{
						html.setFile(player.getHtmlPrefix(),
								"chamberlain/support" + getCastle().getFunction(Castle.FUNC_SUPPORT).getLvl() + ".htm");
						html.replace("%mp%", String.valueOf((int) getCurrentMp()));
					}
					sendHtmlMessage(player, html);
				}
				else if (val.equalsIgnoreCase("back"))
				{
					showChatWindow(player);
				}
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(player.getHtmlPrefix(), "chamberlain/chamberlain-functions.htm");
					if (getCastle().getFunction(Castle.FUNC_RESTORE_EXP) != null)
					{
						html.replace("%xp_regen%",
								String.valueOf(getCastle().getFunction(Castle.FUNC_RESTORE_EXP).getLvl()));
					}
					else
					{
						html.replace("%xp_regen%", "0");
					}
					if (getCastle().getFunction(Castle.FUNC_RESTORE_HP) != null)
					{
						html.replace("%hp_regen%",
								String.valueOf(getCastle().getFunction(Castle.FUNC_RESTORE_HP).getLvl()));
					}
					else
					{
						html.replace("%hp_regen%", "0");
					}
					if (getCastle().getFunction(Castle.FUNC_RESTORE_MP) != null)
					{
						html.replace("%mp_regen%",
								String.valueOf(getCastle().getFunction(Castle.FUNC_RESTORE_MP).getLvl()));
					}
					else
					{
						html.replace("%mp_regen%", "0");
					}
					sendHtmlMessage(player, html);
				}
			}
			else if (actualCommand.equalsIgnoreCase("manage"))
			{
				if ((player.getClanPrivileges() & L2Clan.CP_CS_SET_FUNCTIONS) == L2Clan.CP_CS_SET_FUNCTIONS)
				{
					if (val.equalsIgnoreCase("recovery"))
					{
						if (st.countTokens() >= 1)
						{
							if (getCastle().getOwnerId() == 0)
							{
								player.sendMessage("This castle have no owner, you cannot change configuration");
								return;
							}
							val = st.nextToken();
							if (val.equalsIgnoreCase("hp_cancel"))
							{
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getHtmlPrefix(), "chamberlain/functions-cancel.htm");
								html.replace("%apply%", "recovery hp 0");
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("mp_cancel"))
							{
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getHtmlPrefix(), "chamberlain/functions-cancel.htm");
								html.replace("%apply%", "recovery mp 0");
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("exp_cancel"))
							{
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getHtmlPrefix(), "chamberlain/functions-cancel.htm");
								html.replace("%apply%", "recovery exp 0");
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("edit_hp"))
							{
								val = st.nextToken();
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getHtmlPrefix(), "chamberlain/functions-apply.htm");
								html.replace("%name%", "Fireplace (HP Recovery Device)");
								int percent = Integer.parseInt(val);
								int cost;
								switch (percent)
								{
									case 80:
										cost = Config.CS_HPREG1_FEE;
										break;
									case 120:
										cost = Config.CS_HPREG2_FEE;
										break;
									case 180:
										cost = Config.CS_HPREG3_FEE;
										break;
									case 240:
										cost = Config.CS_HPREG4_FEE;
										break;
									default: // 300
										cost = Config.CS_HPREG5_FEE;
										break;
								}

								html.replace("%cost%", String.valueOf(cost) + "</font>Adena /" +
										String.valueOf(Config.CS_HPREG_FEE_RATIO / 1000 / 60 / 60 / 24) +
										" Day</font>)");
								html.replace("%use%",
										"Provides additional HP recovery for clan members in the castle.<font color=\"00FFFF\">" +
												String.valueOf(percent) + "%</font>");
								html.replace("%apply%", "recovery hp " + String.valueOf(percent));
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("edit_mp"))
							{
								val = st.nextToken();
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getHtmlPrefix(), "chamberlain/functions-apply.htm");
								html.replace("%name%", "Carpet (MP Recovery)");
								int percent = Integer.parseInt(val);
								int cost;
								switch (percent)
								{
									case 5:
										cost = Config.CS_MPREG1_FEE;
										break;
									case 15:
										cost = Config.CS_MPREG2_FEE;
										break;
									case 30:
										cost = Config.CS_MPREG3_FEE;
										break;
									default: // 40
										cost = Config.CS_MPREG4_FEE;
										break;
								}
								html.replace("%cost%", String.valueOf(cost) + "</font>Adena /" +
										String.valueOf(Config.CS_MPREG_FEE_RATIO / 1000 / 60 / 60 / 24) +
										" Day</font>)");
								html.replace("%use%",
										"Provides additional MP recovery for clan members in the castle.<font color=\"00FFFF\">" +
												String.valueOf(percent) + "%</font>");
								html.replace("%apply%", "recovery mp " + String.valueOf(percent));
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("edit_exp"))
							{
								val = st.nextToken();
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getHtmlPrefix(), "chamberlain/functions-apply.htm");
								html.replace("%name%", "Chandelier (EXP Recovery Device)");
								int percent = Integer.parseInt(val);
								int cost;
								switch (percent)
								{
									case 15:
										cost = Config.CS_EXPREG1_FEE;
										break;
									case 25:
										cost = Config.CS_EXPREG2_FEE;
										break;
									case 35:
										cost = Config.CS_EXPREG3_FEE;
										break;
									default: // 50
										cost = Config.CS_EXPREG4_FEE;
										break;
								}
								html.replace("%cost%", String.valueOf(cost) + "</font>Adena /" +
										String.valueOf(Config.CS_EXPREG_FEE_RATIO / 1000 / 60 / 60 / 24) +
										" Day</font>)");
								html.replace("%use%",
										"Restores the Exp of any clan member who is resurrected in the castle.<font color=\"00FFFF\">" +
												String.valueOf(percent) + "%</font>");
								html.replace("%apply%", "recovery exp " + String.valueOf(percent));
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("hp"))
							{
								if (st.countTokens() >= 1)
								{
									int fee;
									if (Config.DEBUG)
									{
										Log.warning("Hp editing invoked");
									}
									val = st.nextToken();
									NpcHtmlMessage html = new NpcHtmlMessage(1);
									html.setFile(player.getHtmlPrefix(), "chamberlain/functions-apply_confirmed.htm");
									if (getCastle().getFunction(Castle.FUNC_RESTORE_HP) != null)
									{
										if (getCastle().getFunction(Castle.FUNC_RESTORE_HP).getLvl() ==
												Integer.parseInt(val))
										{
											html.setFile(player.getHtmlPrefix(), "chamberlain/functions-used.htm");
											html.replace("%val%", String.valueOf(val) + "%");
											sendHtmlMessage(player, html);
											return;
										}
									}
									int percent = Integer.parseInt(val);
									switch (percent)
									{
										case 0:
											fee = 0;
											html.setFile(player.getHtmlPrefix(),
													"chamberlain/functions-cancel_confirmed.htm");
											break;
										case 80:
											fee = Config.CS_HPREG1_FEE;
											break;
										case 120:
											fee = Config.CS_HPREG2_FEE;
											break;
										case 180:
											fee = Config.CS_HPREG3_FEE;
											break;
										case 240:
											fee = Config.CS_HPREG4_FEE;
											break;
										default: // 300
											fee = Config.CS_HPREG5_FEE;
											break;
									}
									if (!getCastle().updateFunctions(player, Castle.FUNC_RESTORE_HP, percent, fee,
											Config.CS_HPREG_FEE_RATIO,
											getCastle().getFunction(Castle.FUNC_RESTORE_HP) == null))
									{
										html.setFile(player.getHtmlPrefix(), "chamberlain/low_adena.htm");
										sendHtmlMessage(player, html);
									}
									sendHtmlMessage(player, html);
								}
								return;
							}
							else if (val.equalsIgnoreCase("mp"))
							{
								if (st.countTokens() >= 1)
								{
									int fee;
									if (Config.DEBUG)
									{
										Log.warning("Mp editing invoked");
									}
									val = st.nextToken();
									NpcHtmlMessage html = new NpcHtmlMessage(1);
									html.setFile(player.getHtmlPrefix(), "chamberlain/functions-apply_confirmed.htm");
									if (getCastle().getFunction(Castle.FUNC_RESTORE_MP) != null)
									{
										if (getCastle().getFunction(Castle.FUNC_RESTORE_MP).getLvl() ==
												Integer.parseInt(val))
										{
											html.setFile(player.getHtmlPrefix(), "chamberlain/functions-used.htm");
											html.replace("%val%", String.valueOf(val) + "%");
											sendHtmlMessage(player, html);
											return;
										}
									}
									int percent = Integer.parseInt(val);
									switch (percent)
									{
										case 0:
											fee = 0;
											html.setFile(player.getHtmlPrefix(),
													"chamberlain/functions-cancel_confirmed.htm");
											break;
										case 5:
											fee = Config.CS_MPREG1_FEE;
											break;
										case 15:
											fee = Config.CS_MPREG2_FEE;
											break;
										case 30:
											fee = Config.CS_MPREG3_FEE;
											break;
										default: // 40
											fee = Config.CS_MPREG4_FEE;
											break;
									}
									if (!getCastle().updateFunctions(player, Castle.FUNC_RESTORE_MP, percent, fee,
											Config.CS_MPREG_FEE_RATIO,
											getCastle().getFunction(Castle.FUNC_RESTORE_MP) == null))
									{
										html.setFile(player.getHtmlPrefix(), "chamberlain/low_adena.htm");
										sendHtmlMessage(player, html);
									}
									sendHtmlMessage(player, html);
								}
								return;
							}
							else if (val.equalsIgnoreCase("exp"))
							{
								if (st.countTokens() >= 1)
								{
									int fee;
									if (Config.DEBUG)
									{
										Log.warning("Exp editing invoked");
									}
									val = st.nextToken();
									NpcHtmlMessage html = new NpcHtmlMessage(1);
									html.setFile(player.getHtmlPrefix(), "chamberlain/functions-apply_confirmed.htm");
									if (getCastle().getFunction(Castle.FUNC_RESTORE_EXP) != null)
									{
										if (getCastle().getFunction(Castle.FUNC_RESTORE_EXP).getLvl() ==
												Integer.parseInt(val))
										{
											html.setFile(player.getHtmlPrefix(), "chamberlain/functions-used.htm");
											html.replace("%val%", String.valueOf(val) + "%");
											sendHtmlMessage(player, html);
											return;
										}
									}
									int percent = Integer.parseInt(val);
									switch (percent)
									{
										case 0:
											fee = 0;
											html.setFile(player.getHtmlPrefix(),
													"chamberlain/functions-cancel_confirmed.htm");
											break;
										case 15:
											fee = Config.CS_EXPREG1_FEE;
											break;
										case 25:
											fee = Config.CS_EXPREG2_FEE;
											break;
										case 35:
											fee = Config.CS_EXPREG3_FEE;
											break;
										default: // 50
											fee = Config.CS_EXPREG4_FEE;
											break;
									}
									if (!getCastle().updateFunctions(player, Castle.FUNC_RESTORE_EXP, percent, fee,
											Config.CS_EXPREG_FEE_RATIO,
											getCastle().getFunction(Castle.FUNC_RESTORE_EXP) == null))
									{
										html.setFile(player.getHtmlPrefix(), "chamberlain/low_adena.htm");
										sendHtmlMessage(player, html);
									}
									sendHtmlMessage(player, html);
								}
								return;
							}
						}
						NpcHtmlMessage html = new NpcHtmlMessage(1);
						html.setFile(player.getHtmlPrefix(), "chamberlain/edit_recovery.htm");
						String hp =
								"[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 80\">80%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 120\">120%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 180\">180%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 240\">240%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 300\">300%</a>]";
						String exp =
								"[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 25\">25%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 35\">35%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 50\">50%</a>]";
						String mp =
								"[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 30\">30%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 40\">40%</a>]";
						if (getCastle().getFunction(Castle.FUNC_RESTORE_HP) != null)
						{
							html.replace("%hp_recovery%",
									String.valueOf(getCastle().getFunction(Castle.FUNC_RESTORE_HP).getLvl()) +
											"%</font> (<font color=\"FFAABB\">" +
											String.valueOf(getCastle().getFunction(Castle.FUNC_RESTORE_HP).getLease()) +
											"</font>Adena /" +
											String.valueOf(Config.CS_HPREG_FEE_RATIO / 1000 / 60 / 60 / 24) + " Day)");
							html.replace("%hp_period%", "Withdraw the fee for the next time at " +
									format.format(getCastle().getFunction(Castle.FUNC_RESTORE_HP).getEndTime()));
							html.replace("%change_hp%",
									"[<a action=\"bypass -h npc_%objectId%_manage recovery hp_cancel\">Deactivate</a>]" +
											hp);
						}
						else
						{
							html.replace("%hp_recovery%", "none");
							html.replace("%hp_period%", "none");
							html.replace("%change_hp%", hp);
						}
						if (getCastle().getFunction(Castle.FUNC_RESTORE_EXP) != null)
						{
							html.replace("%exp_recovery%",
									String.valueOf(getCastle().getFunction(Castle.FUNC_RESTORE_EXP).getLvl()) +
											"%</font> (<font color=\"FFAABB\">" + String.valueOf(
											getCastle().getFunction(Castle.FUNC_RESTORE_EXP).getLease()) +
											"</font>Adena /" +
											String.valueOf(Config.CS_EXPREG_FEE_RATIO / 1000 / 60 / 60 / 24) + " Day)");
							html.replace("%exp_period%", "Withdraw the fee for the next time at " +
									format.format(getCastle().getFunction(Castle.FUNC_RESTORE_EXP).getEndTime()));
							html.replace("%change_exp%",
									"[<a action=\"bypass -h npc_%objectId%_manage recovery exp_cancel\">Deactivate</a>]" +
											exp);
						}
						else
						{
							html.replace("%exp_recovery%", "none");
							html.replace("%exp_period%", "none");
							html.replace("%change_exp%", exp);
						}
						if (getCastle().getFunction(Castle.FUNC_RESTORE_MP) != null)
						{
							html.replace("%mp_recovery%",
									String.valueOf(getCastle().getFunction(Castle.FUNC_RESTORE_MP).getLvl()) +
											"%</font> (<font color=\"FFAABB\">" +
											String.valueOf(getCastle().getFunction(Castle.FUNC_RESTORE_MP).getLease()) +
											"</font>Adena /" +
											String.valueOf(Config.CS_MPREG_FEE_RATIO / 1000 / 60 / 60 / 24) + " Day)");
							html.replace("%mp_period%", "Withdraw the fee for the next time at " +
									format.format(getCastle().getFunction(Castle.FUNC_RESTORE_MP).getEndTime()));
							html.replace("%change_mp%",
									"[<a action=\"bypass -h npc_%objectId%_manage recovery mp_cancel\">Deactivate</a>]" +
											mp);
						}
						else
						{
							html.replace("%mp_recovery%", "none");
							html.replace("%mp_period%", "none");
							html.replace("%change_mp%", mp);
						}
						sendHtmlMessage(player, html);
					}
					else if (val.equalsIgnoreCase("other"))
					{
						if (st.countTokens() >= 1)
						{
							if (getCastle().getOwnerId() == 0)
							{
								player.sendMessage("This castle have no owner, you cannot change configuration");
								return;
							}
							val = st.nextToken();
							if (val.equalsIgnoreCase("tele_cancel"))
							{
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getHtmlPrefix(), "chamberlain/functions-cancel.htm");
								html.replace("%apply%", "other tele 0");
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("support_cancel"))
							{
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getHtmlPrefix(), "chamberlain/functions-cancel.htm");
								html.replace("%apply%", "other support 0");
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("edit_support"))
							{
								val = st.nextToken();
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getHtmlPrefix(), "chamberlain/functions-apply.htm");
								html.replace("%name%", "Insignia (Supplementary Magic)");
								int stage = Integer.parseInt(val);
								int cost;
								switch (stage)
								{
									case 1:
										cost = Config.CS_SUPPORT1_FEE;
										break;
									case 2:
										cost = Config.CS_SUPPORT2_FEE;
										break;
									case 3:
										cost = Config.CS_SUPPORT3_FEE;
										break;
									default:
										cost = Config.CS_SUPPORT4_FEE;
										break;
								}
								html.replace("%cost%", String.valueOf(cost) + "</font>Adena /" +
										String.valueOf(Config.CS_SUPPORT_FEE_RATIO / 1000 / 60 / 60 / 24) +
										" Day</font>)");
								html.replace("%use%", "Enables the use of supplementary magic.");
								html.replace("%apply%", "other support " + String.valueOf(stage));
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("edit_tele"))
							{
								val = st.nextToken();
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile(player.getHtmlPrefix(), "chamberlain/functions-apply.htm");
								html.replace("%name%", "Mirror (Teleportation Device)");
								int stage = Integer.parseInt(val);
								int cost;
								switch (stage)
								{
									case 1:
										cost = Config.CS_TELE1_FEE;
										break;
									default:
										cost = Config.CS_TELE2_FEE;
										break;
								}
								html.replace("%cost%", String.valueOf(cost) + "</font>Adena /" +
										String.valueOf(Config.CS_TELE_FEE_RATIO / 1000 / 60 / 60 / 24) +
										" Day</font>)");
								html.replace("%use%",
										"Teleports clan members in a castle to the target <font color=\"00FFFF\">Stage " +
												String.valueOf(stage) + "</font> staging area");
								html.replace("%apply%", "other tele " + String.valueOf(stage));
								sendHtmlMessage(player, html);
								return;
							}
							else if (val.equalsIgnoreCase("tele"))
							{
								if (st.countTokens() >= 1)
								{
									int fee;
									if (Config.DEBUG)
									{
										Log.warning("Tele editing invoked");
									}
									val = st.nextToken();
									NpcHtmlMessage html = new NpcHtmlMessage(1);
									html.setFile(player.getHtmlPrefix(), "chamberlain/functions-apply_confirmed.htm");
									if (getCastle().getFunction(Castle.FUNC_TELEPORT) != null)
									{
										if (getCastle().getFunction(Castle.FUNC_TELEPORT).getLvl() ==
												Integer.parseInt(val))
										{
											html.setFile(player.getHtmlPrefix(), "chamberlain/functions-used.htm");
											html.replace("%val%", "Stage " + String.valueOf(val));
											sendHtmlMessage(player, html);
											return;
										}
									}
									int lvl = Integer.parseInt(val);
									switch (lvl)
									{
										case 0:
											fee = 0;
											html.setFile(player.getHtmlPrefix(),
													"chamberlain/functions-cancel_confirmed.htm");
											break;
										case 1:
											fee = Config.CS_TELE1_FEE;
											break;
										default:
											fee = Config.CS_TELE2_FEE;
											break;
									}
									if (!getCastle().updateFunctions(player, Castle.FUNC_TELEPORT, lvl, fee,
											Config.CS_TELE_FEE_RATIO,
											getCastle().getFunction(Castle.FUNC_TELEPORT) == null))
									{
										html.setFile(player.getHtmlPrefix(), "chamberlain/low_adena.htm");
										sendHtmlMessage(player, html);
									}
									sendHtmlMessage(player, html);
								}
								return;
							}
							else if (val.equalsIgnoreCase("support"))
							{
								if (st.countTokens() >= 1)
								{
									int fee;
									if (Config.DEBUG)
									{
										Log.warning("Support editing invoked");
									}
									val = st.nextToken();
									NpcHtmlMessage html = new NpcHtmlMessage(1);
									html.setFile(player.getHtmlPrefix(), "chamberlain/functions-apply_confirmed.htm");
									if (getCastle().getFunction(Castle.FUNC_SUPPORT) != null)
									{
										if (getCastle().getFunction(Castle.FUNC_SUPPORT).getLvl() ==
												Integer.parseInt(val))
										{
											html.setFile(player.getHtmlPrefix(), "chamberlain/functions-used.htm");
											html.replace("%val%", "Stage " + String.valueOf(val));
											sendHtmlMessage(player, html);
											return;
										}
									}
									int lvl = Integer.parseInt(val);
									switch (lvl)
									{
										case 0:
											fee = 0;
											html.setFile(player.getHtmlPrefix(),
													"chamberlain/functions-cancel_confirmed.htm");
											break;
										case 1:
											fee = Config.CS_SUPPORT1_FEE;
											break;
										case 2:
											fee = Config.CS_SUPPORT2_FEE;
											break;
										case 3:
											fee = Config.CS_SUPPORT3_FEE;
											break;
										default:
											fee = Config.CS_SUPPORT4_FEE;
											break;
									}
									if (!getCastle().updateFunctions(player, Castle.FUNC_SUPPORT, lvl, fee,
											Config.CS_SUPPORT_FEE_RATIO,
											getCastle().getFunction(Castle.FUNC_SUPPORT) == null))
									{
										html.setFile(player.getHtmlPrefix(), "chamberlain/low_adena.htm");
										sendHtmlMessage(player, html);
									}
									else
									{
										sendHtmlMessage(player, html);
									}
								}
								return;
							}
						}
						NpcHtmlMessage html = new NpcHtmlMessage(1);
						html.setFile(player.getHtmlPrefix(), "chamberlain/edit_other.htm");
						String tele =
								"[<a action=\"bypass -h npc_%objectId%_manage other edit_tele 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_tele 2\">Level 2</a>]";
						String support =
								"[<a action=\"bypass -h npc_%objectId%_manage other edit_support 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 2\">Level 2</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 3\">Level 3</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 4\">Level 4</a>]";
						if (getCastle().getFunction(Castle.FUNC_TELEPORT) != null)
						{
							html.replace("%tele%",
									"Stage " + String.valueOf(getCastle().getFunction(Castle.FUNC_TELEPORT).getLvl()) +
											"</font> (<font color=\"FFAABB\">" +
											String.valueOf(getCastle().getFunction(Castle.FUNC_TELEPORT).getLease()) +
											"</font>Adena /" +
											String.valueOf(Config.CS_TELE_FEE_RATIO / 1000 / 60 / 60 / 24) + " Day)");
							html.replace("%tele_period%", "Withdraw the fee for the next time at " +
									format.format(getCastle().getFunction(Castle.FUNC_TELEPORT).getEndTime()));
							html.replace("%change_tele%",
									"[<a action=\"bypass -h npc_%objectId%_manage other tele_cancel\">Deactivate</a>]" +
											tele);
						}
						else
						{
							html.replace("%tele%", "none");
							html.replace("%tele_period%", "none");
							html.replace("%change_tele%", tele);
						}
						if (getCastle().getFunction(Castle.FUNC_SUPPORT) != null)
						{
							html.replace("%support%",
									"Stage " + String.valueOf(getCastle().getFunction(Castle.FUNC_SUPPORT).getLvl()) +
											"</font> (<font color=\"FFAABB\">" +
											String.valueOf(getCastle().getFunction(Castle.FUNC_SUPPORT).getLease()) +
											"</font>Adena /" +
											String.valueOf(Config.CS_SUPPORT_FEE_RATIO / 1000 / 60 / 60 / 24) +
											" Day)");
							html.replace("%support_period%", "Withdraw the fee for the next time at " +
									format.format(getCastle().getFunction(Castle.FUNC_SUPPORT).getEndTime()));
							html.replace("%change_support%",
									"[<a action=\"bypass -h npc_%objectId%_manage other support_cancel\">Deactivate</a>]" +
											support);
						}
						else
						{
							html.replace("%support%", "none");
							html.replace("%support_period%", "none");
							html.replace("%change_support%", support);
						}
						sendHtmlMessage(player, html);
					}
					else if (val.equalsIgnoreCase("back"))
					{
						showChatWindow(player);
					}
					else
					{
						NpcHtmlMessage html = new NpcHtmlMessage(1);
						html.setFile(player.getHtmlPrefix(), "chamberlain/manage.htm");
						sendHtmlMessage(player, html);
					}
				}
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(player.getHtmlPrefix(), "chamberlain/chamberlain-noprivs.htm");
					sendHtmlMessage(player, html);
				}
			}
			else if (actualCommand.equalsIgnoreCase("support"))
			{
				setTarget(player);
				L2Skill skill;
				if (val.isEmpty())
				{
					return;
				}

				try
				{
					int skill_id = Integer.parseInt(val);
					try
					{
						if (getCastle().getFunction(Castle.FUNC_SUPPORT) == null)
						{
							return;
						}
						if (getCastle().getFunction(Castle.FUNC_SUPPORT).getLvl() == 0)
						{
							return;
						}
						NpcHtmlMessage html = new NpcHtmlMessage(1);
						int skill_lvl = 0;
						if (st.countTokens() >= 1)
						{
							skill_lvl = Integer.parseInt(st.nextToken());
						}
						skill = SkillTable.getInstance().getInfo(skill_id, skill_lvl);
						if (skill.getSkillType() == L2SkillType.SUMMON)
						{
							player.doSimultaneousCast(skill);
						}
						else
						{
							if (!(skill.getMpConsume() > getCurrentMp()))
							{
								this.doCast(skill);
							}
							else
							{
								html.setFile(player.getHtmlPrefix(), "chamberlain/support-no_mana.htm");
								html.replace("%mp%", String.valueOf((int) getCurrentMp()));
								sendHtmlMessage(player, html);
								return;
							}
						}
						html.setFile(player.getHtmlPrefix(), "chamberlain/support-done.htm");
						html.replace("%mp%", String.valueOf((int) getCurrentMp()));
						sendHtmlMessage(player, html);
					}
					catch (Exception e)
					{
						player.sendMessage("Invalid skill level, contact your admin!");
					}
				}
				catch (Exception e)
				{
					player.sendMessage("Invalid skill level, contact your admin!");
				}
			}
			else if (actualCommand.equalsIgnoreCase("support_back"))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				if (getCastle().getFunction(Castle.FUNC_SUPPORT).getLvl() == 0)
				{
					return;
				}
				html.setFile(player.getHtmlPrefix(),
						"chamberlain/support" + getCastle().getFunction(Castle.FUNC_SUPPORT).getLvl() + ".htm");
				html.replace("%mp%", String.valueOf((int) getStatus().getCurrentMp()));
				sendHtmlMessage(player, html);
			}
			else if (actualCommand.equalsIgnoreCase("goto"))
			{
				int whereTo = Integer.parseInt(val);
				doTeleport(player, whereTo);
			}
			else if (actualCommand.equalsIgnoreCase("siege_change")) // siege day set
			{
				if (Config.CL_SET_SIEGE_TIME_LIST.isEmpty())
				{
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(player.getHtmlPrefix(), "chamberlain/chamberlain-noadmin.htm");
					sendHtmlMessage(player, html);
				}
				else if (player.isClanLeader())
				{
					if (getCastle().getSiege().getTimeRegistrationOverDate().getTimeInMillis() <
							Calendar.getInstance().getTimeInMillis())
					{
						NpcHtmlMessage html = new NpcHtmlMessage(1);
						html.setFile(player.getHtmlPrefix(), "chamberlain/siegetime1.htm");
						sendHtmlMessage(player, html);
					}
					else if (getCastle().getSiege().getIsTimeRegistrationOver())
					{
						NpcHtmlMessage html = new NpcHtmlMessage(1);
						html.setFile(player.getHtmlPrefix(), "chamberlain/siegetime2.htm");
						sendHtmlMessage(player, html);
					}
					else
					{
						NpcHtmlMessage html = new NpcHtmlMessage(1);
						html.setFile(player.getHtmlPrefix(), "chamberlain/siegetime3.htm");
						html.replace("%time%", String.valueOf(getCastle().getSiegeDate().getTime()));
						sendHtmlMessage(player, html);
					}
				}
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(player.getHtmlPrefix(), "chamberlain/chamberlain-noprivs.htm");
					sendHtmlMessage(player, html);
				}
			}
			else if (actualCommand.equalsIgnoreCase("siege_time_set")) // set preDay
			{
				boolean isAfternoon = Config.SIEGE_HOUR_LIST_MORNING.isEmpty();
				switch (Integer.parseInt(val))
				{
					case 0:
					case 4:
						break;
					case 1:
						_preDay = Integer.parseInt(st.nextToken());
						break;
					case 2:
						isAfternoon = Boolean.parseBoolean(st.nextToken());
						break;
					case 3:
						_preHour = Integer.parseInt(st.nextToken());
						break;
					default:
						break;
				}
				NpcHtmlMessage html = getNextSiegeTimePage(player.getHtmlPrefix(), Integer.parseInt(val), isAfternoon);

				if (html == null)
				{
					if (Config.CL_SET_SIEGE_TIME_LIST.contains("day"))
					{
						getCastle().getSiegeDate().set(Calendar.DAY_OF_WEEK, _preDay);
					}
					else
					{
						getCastle().getSiegeDate().set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
					}
					if (Config.CL_SET_SIEGE_TIME_LIST.contains("hour"))
					{
						getCastle().getSiegeDate().set(Calendar.HOUR_OF_DAY, _preHour);
					}
					if (Config.CL_SET_SIEGE_TIME_LIST.contains("minute"))
					{
						getCastle().getSiegeDate().set(Calendar.MINUTE, Integer.parseInt(st.nextToken()));
					}
					// now store the changed time and finished next Siege Time registration
					getCastle().getSiege().endTimeRegistration(false);

					html = new NpcHtmlMessage(1);
					html.setFile(player.getHtmlPrefix(), "chamberlain/siegetime8.htm");
					html.replace("%time%", String.valueOf(getCastle().getSiegeDate().getTime()));
				}
				sendHtmlMessage(player, html);
			}
			else if (actualCommand.equals("give_crown"))
			{
				if (siegeBlocksFunction(player))
				{
					return;
				}

				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());

				if (player.isClanLeader())
				{
					if (player.getInventory().getItemByItemId(6841) == null)
					{
						L2ItemInstance crown = player.getInventory().addItem("Castle Crown", 6841, 1, player, this);

						SystemMessage ms = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
						ms.addItemName(crown);
						player.sendPacket(ms);

						html.setFile(player.getHtmlPrefix(), "chamberlain/chamberlain-gavecrown.htm");
						html.replace("%CharName%", String.valueOf(player.getName()));
						html.replace("%FeudName%", String.valueOf(getCastle().getName()));
					}
					else
					{
						html.setFile(player.getHtmlPrefix(), "chamberlain/chamberlain-hascrown.htm");
					}
				}
				else
				{
					html.setFile(player.getHtmlPrefix(), "chamberlain/chamberlain-noprivs.htm");
				}

				player.sendPacket(html);
			}
			else if (actualCommand.equals("give_cloak"))
			{
				if (siegeBlocksFunction(player))
				{
					return;
				}

				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());

				int tendency = getCastle().getTendency();

				if (player.isClanLeader() && tendency != 0)
				{
					int cloakId = tendency == 1 ? 34996 : 34997;

					if (player.getInventory().getItemByItemId(cloakId) == null)
					{
						L2ItemInstance cloak = player.getInventory().addItem("Lord Cloak", cloakId, 1, player, this);

						SystemMessage ms = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
						ms.addItemName(cloak);
						player.sendPacket(ms);
						return;
					}
					else
					{
						html.setFile(player.getHtmlPrefix(), "chamberlain/chamberlain-hascloak.htm");
					}
				}
				else
				{
					html.setFile(player.getHtmlPrefix(), "chamberlain/chamberlain-noprivs.htm");
				}

				player.sendPacket(html);
			}
			else
			{
				super.onBypassFeedback(player, command);
			}
		}
	}

	private NpcHtmlMessage getNextSiegeTimePage(String htmlPrefix, int now, boolean isAfternoon)
	{
		NpcHtmlMessage ret = new NpcHtmlMessage(1);
		if (now == 0 && Config.CL_SET_SIEGE_TIME_LIST.contains("day"))
		{
			ret.setFile(htmlPrefix, "chamberlain/siegetime4.htm");
			return ret;
		}
		if (now < 3 && Config.CL_SET_SIEGE_TIME_LIST.contains("hour"))
		{
			switch (now)
			{
				case 0:
				case 1:
					if (!Config.SIEGE_HOUR_LIST_MORNING.isEmpty() && !Config.SIEGE_HOUR_LIST_AFTERNOON.isEmpty())
					{
						ret.setFile(htmlPrefix, "chamberlain/siegetime5.htm");
						return ret;
					}
				case 2:
					ret.setFile(htmlPrefix, "chamberlain/siegetime6.htm");
					TIntArrayList list;
					int inc = 0;
					String ampm = "";

					if (!isAfternoon)
					{
						if (Config.SIEGE_HOUR_LIST_AFTERNOON.isEmpty())
						{
							ampm = "AM";
						}
						list = Config.SIEGE_HOUR_LIST_MORNING;
					}
					else
					{
						if (Config.SIEGE_HOUR_LIST_MORNING.isEmpty())
						{
							ampm = "PM";
						}
						inc = 12;
						list = Config.SIEGE_HOUR_LIST_AFTERNOON;
					}

					final StringBuilder tList = new StringBuilder(list.size() * 50);
					for (int hour : list.toNativeArray())
					{
						if (hour == 0)
						{
							StringUtil.append(tList, "<a action=\"bypass -h npc_%objectId%_siege_time_set 3 ",
									String.valueOf(hour + inc), "\">", String.valueOf(hour + 12), ":00 ", ampm,
									"</a><br>");
						}
						else
						{
							StringUtil.append(tList, "<a action=\"bypass -h npc_%objectId%_siege_time_set 3 ",
									String.valueOf(hour + inc), "\">", String.valueOf(hour), ":00 ", ampm, "</a><br>");
						}
					}
					ret.replace("%links%", tList.toString());
			}
			return ret;
		}
		if (now < 4 && Config.CL_SET_SIEGE_TIME_LIST.contains("minute"))
		{
			ret.setFile(htmlPrefix, "chamberlain/siegetime7.htm");
			return ret;
		}

		return null;
	}

	private void sendHtmlMessage(L2PcInstance player, String htmlMessage)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setHtml(htmlMessage);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}

	@Override
	public void showChatWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "chamberlain/chamberlain-no.htm";

		int condition = validateCondition(player);
		if (condition > COND_ALL_FALSE)
		{
			if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
			{
				filename = "chamberlain/chamberlain-busy.htm"; // Busy because of siege
			}
			else if (condition == COND_OWNER) // Clan owns castle
			{
				filename = "chamberlain/chamberlain.htm"; // Owner message window
			}
		}

		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player.getHtmlPrefix(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}

	private void doTeleport(L2PcInstance player, int val)
	{
		if (Config.DEBUG)
		{
			Log.warning("doTeleport(L2PcInstance player, int val) is called");
		}
		L2TeleportLocation list = TeleportLocationTable.getInstance().getTemplate(val);
		if (list != null)
		{
			if (player.destroyItemByItemId("Teleport", list.getItemId(), list.getPrice(), this, true))
			{
				if (Config.DEBUG)
				{
					Log.warning("Teleporting player " + player.getName() + " for Castle to new location: " +
							list.getLocX() + ":" + list.getLocY() + ":" + list.getLocZ());
				}
				player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ());
			}
		}
		else
		{
			Log.warning("No teleport destination with id:" + val);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	protected int validateCondition(L2PcInstance player)
	{
		if (player.isGM())
		{
			return COND_OWNER;
		}

		if (getCastle() != null && getCastle().getCastleId() > 0)
		{
			if (player.getClan() != null)
			{
				if (getCastle().getZone().isActive())
				{
					return COND_BUSY_BECAUSE_OF_SIEGE; // Busy because of siege
				}
				else if (getCastle().getOwnerId() == player.getClanId()) // Clan owns castle
				{
					return COND_OWNER; // Owner
				}
			}
		}
		return COND_ALL_FALSE;
	}

	private boolean validatePrivileges(L2PcInstance player, int privilege)
	{
		if ((player.getClanPrivileges() & privilege) != privilege)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile(player.getHtmlPrefix(), "chamberlain/chamberlain-noprivs.htm");
			player.sendPacket(html);
			return false;
		}
		return true;
	}

	private boolean siegeBlocksFunction(L2PcInstance player)
	{
		if (getCastle().getSiege().getIsInProgress())
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile(player.getHtmlPrefix(), "chamberlain/chamberlain-busy.htm");
			html.replace("%npcname%", String.valueOf(getName()));
			player.sendPacket(html);
			return true;
		}
		return false;
	}
}
