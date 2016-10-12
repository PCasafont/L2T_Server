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

import l2server.Config;
import l2server.gameserver.datatables.SubPledgeSkillTree;
import l2server.gameserver.datatables.SubPledgeSkillTree.SubUnitSkill;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2SquadTrainer;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.ExAcquireSkillList;
import l2server.gameserver.network.serverpackets.ExAcquireSkillList.SkillType;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.gameserver.templates.skills.L2AbnormalType;
import l2server.util.Rnd;

/**
 * @author Kerberos | ZaKaX
 */
public class L2CastleMagicianInstance extends L2NpcInstance implements L2SquadTrainer
{
	protected static final int COND_ALL_FALSE = 0;
	protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	protected static final int COND_OWNER = 2;

	/**
	 * @param template
	 */
	public L2CastleMagicianInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2CastleMagicianInstance);
	}

	@Override
	public void showChatWindow(L2PcInstance player, int val)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "castlemagician/magician-no.htm";

		int condition = validateCondition(player);
		if (condition > COND_ALL_FALSE)
		{
			if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
			{
				filename = "castlemagician/magician-busy.htm"; // Busy because of siege
			}
			else if (condition == COND_OWNER) // Clan owns castle
			{
				if (val == 0)
				{
					filename = "castlemagician/magician.htm";
				}
				else
				{
					filename = "castlemagician/magician-" + val + ".htm";
				}
			}
		}

		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player.getHtmlPrefix(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("Chat"))
		{
			int val = 0;
			try
			{
				val = Integer.parseInt(command.substring(5));
			}
			catch (IndexOutOfBoundsException | NumberFormatException ignored)
			{
			}
			showChatWindow(player, val);
		}
		else if (command.startsWith("ExchangeKE"))
		{
			String filename = null;
			int item = 9914;
			int i0 = Rnd.get(100);
			if (i0 < 5)
			{
				int i1 = Rnd.get(100);

				if (i1 < 5)
				{
					item = 9931; // Red Talisman of Meditation
				}
				else if (i1 <= 50)
				{
					item = 9932; // Blue Talisman - Divine Protection
				}
				else if (i1 <= 75)
				{
					item = 10416; // Blue Talisman - Explosion
				}
			}
			else if (i0 <= 15)
			{
				switch (Rnd.get(5))
				{
					case 1: // Red Talisman of Minimum Clarity
						item = 9917;
						break;
					case 2: // Red Talisman of Maxmimum Clarity
						item = 9918;
						break;
					case 3: // Red Talisman of Mental Regeneration
						item = 9928;
						break;
					case 4: // Blue Talisman of Protection
						item = 9929;
						break;
					default: // Blue Talisman of Invisibility
						item = 9920;
				}
			}
			else if (i0 <= 30)
			{
				switch (Rnd.get(8))
				{
					case 1: // Blue Talisman of Defense
						item = 9916;
						break;
					case 2: // Blue Talisman of Defense
						item = 9916;
						break;
					case 3: // Blue Talisman of Healing
						item = 9924;
						break;
					case 4: // Red Talisman of Recovery
						item = 9925;
						break;
					case 5: // Blue Talisman of Defense
						item = 9926;
						break;
					case 6: // Blue Talisman of Magic Defense
						item = 9927;
						break;
					case 7: // Red Talisman - Life Force
						item = 10518;
						break;
					default: // Blue Talisman - Greater Healing
						item = 10424;
				}
			}
			else
			{
				switch (Rnd.get(46))
				{
					case 0: // Blue Talisman of Power
						item = 9914;
						break;
					case 1: // Blue Talisman of Wild Magic
						item = 9915;
						break;
					case 2: // Blue Talisman of Invisibility
						item = 9920;
						break;
					case 3: // Blue Talisman of Invisibility
						item = 9920;
						break;
					case 4: // Blue Talisman - Shield Protection
						item = 9921;
						break;
					case 5: // Black Talisman - Mending
						item = 9922;
						break;
					case 6: // Yellow Talisman of Power
						item = 9933;
						break;
					case 7: // Yellow Talisman of Violent Haste
						item = 9934;
						break;
					case 8: // Yellow Talisman of Arcane Defense
						item = 9935;
						break;
					case 9: // Yellow Talisman of Arcane Power
						item = 9936;
						break;
					case 10: // Yellow Talisman of Arcane Haste
						item = 9937;
						break;
					case 11: // Yellow Talisman of Accuracy
						item = 9938;
						break;
					case 12: // Yellow Talisman of Defense
						item = 9939;
						break;
					case 13: // Yellow Talisman of Alacrity
						item = 9940;
						break;
					case 14: // Yellow Talisman of Speed
						item = 9941;
						break;
					case 15: // Yellow Talisman of Critical Reduction
						item = 9942;
						break;
					case 16: // Yellow Talisman of Critical Damage
						item = 9943;
						break;
					case 17: // Yellow Talisman of Critical Dodging
						item = 9944;
						break;
					case 18: // Yellow Talisman of Evasion
						item = 9945;
						break;
					case 19: // Yellow Talisman of Healing
						item = 9946;
						break;
					case 20: // Yellow Talisman of CP Regeneration
						item = 9947;
						break;
					case 21: // Yellow Talisman of Physical Regeneration
						item = 9948;
						break;
					case 22: // Yellow Talisman of Mental Regeneration
						item = 9949;
						break;
					case 23: // Grey Talisman of Weight Training
						item = 9950;
						break;
					case 24: // White Talisman of Protection
						item = 9965;
						break;
					case 25: // Orange Talisman - Hot Springs CP Potion
						item = 9952;
						break;
					case 26: // Orange Talisman - Elixir of Life
						item = 9953;
						break;
					case 27: // Orange Talisman - Elixir of Mental Strength
						item = 9954;
						break;
					case 28: // Black Talisman - Vocalization
						item = 9955;
						break;
					case 29: // Black Talisman - Arcane Freedom
						item = 9956;
						break;
					case 30: // Black Talisman - Physical Freedom
						item = 9957;
						break;
					case 31: // Black Talisman - Rescue
						item = 9958;
						break;
					case 32: // Black Talisman - Free Speech
						item = 9959;
						break;
					case 33: // White Talisman of Bravery
						item = 9960;
						break;
					case 34: // White Talisman of Motion
						item = 9961;
						break;
					case 35: // White Talisman of Grounding
						item = 9962;
						break;
					case 36: // White Talisman of Attention
						item = 9963;
						break;
					case 37: // White Talisman of Bandages
						item = 9964;
						break;
					case 38: // White Talisman - Storm
						item = 10418;
						break;
					case 39: // White Talisman - Water
						item = 10420;
						break;
					case 40: // White Talisman -  Earth
						item = 10519;
						break;
					case 41: // White Talisman - Light
						item = 10422;
						break;
					case 42: // Blue Talisman - Self-Destruction
						item = 10423;
						break;
					case 43: // White Talisman - Darkness
						item = 10419;
						break;
					default: // White Talisman - Fire
						item = 10421;
				}
			}

			if (player.exchangeItemsById("ExchangeKE", this, 9912, 10, item, 1, true))
			{
				filename = "castlemagician/magician-KE-Exchange.htm";
			}
			else
			{
				filename = "castlemagician/magician-no-KE.htm";
			}

			showChatWindowByFileName(player, filename);
		}
		else if (command.equals("gotoleader"))
		{
			if (player.getClan() != null)
			{
				L2PcInstance clanLeader = player.getClan().getLeader().getPlayerInstance();
				if (clanLeader == null)
				{
					return;
				}

				if (clanLeader.getFirstEffect(L2AbnormalType.CLAN_GATE) != null)
				{
					if (!validateGateCondition(clanLeader, player))
					{
						return;
					}

					player.teleToLocation(clanLeader.getX(), clanLeader.getY(), clanLeader.getZ(), false);
					return;
				}
				String filename = "castlemagician/magician-nogate.htm";
				showChatWindowByFileName(player, filename);
			}
		}
		else if (command.equals("subskills"))
		{
			if (player.getClan() != null)
			{
				if (player.isClanLeader())
				{
					ExAcquireSkillList skilllist = new ExAcquireSkillList(SkillType.SubUnit);
					SubUnitSkill[] array = SubPledgeSkillTree.getInstance().getAvailableSkills(player.getClan());
					if (array.length == 0)
					{
						player.sendPacket(SystemMessageId.NO_MORE_SKILLS_TO_LEARN);
						return;
					}
					for (SubUnitSkill sus : array)
					{
						skilllist.addSkill(sus.getSkill().getId(), sus.getSkill().getLevel(), sus.getSkill().getLevel(),
								sus.getReputation(), 0);
					}
					player.sendPacket(skilllist);
				}
				else
				{
					String filename = "castlemagician/magician-nosquad.htm";
					showChatWindowByFileName(player, filename);
				}
			}
		}
		else if (command.equals("give_cloak"))
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());

			int tendency = getCastle().getTendency();

			if (!player.isClanLeader() && tendency != 0)
			{
				int cloakId = tendency == 1 ? 34925 : 34926;

				if (player.getInventory().getItemByItemId(cloakId) == null)
				{
					L2ItemInstance cloak = player.getInventory().addItem("Members Cloak", cloakId, 1, player, this);

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
					return COND_OWNER;
				}
			}
		}
		return COND_ALL_FALSE;
	}

	private static boolean validateGateCondition(L2PcInstance clanLeader, L2PcInstance player)
	{
		if (clanLeader.isAlikeDead())
		{
			// Need retail message if there's one.
			player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
			return false;
		}

		if (clanLeader.isInStoreMode())
		{
			// Need retail message if there's one.
			player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
			return false;
		}

		if (clanLeader.isRooted() || clanLeader.isInCombat())
		{
			// Need retail message if there's one.
			player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
			return false;
		}

		if (clanLeader.isInOlympiadMode())
		{
			// Need retail message if there's one.
			player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
			return false;
		}

		if (clanLeader.inObserverMode())
		{
			// Need retail message if there's one.
			player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
			return false;
		}

		if (clanLeader.isInsideZone(L2Character.ZONE_NOSUMMONFRIEND))
		{
			// Need retail message if there's one.
			player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
			return false;
		}

		if (clanLeader.getInstanceId() > 0)
		{
			if (!Config.ALLOW_SUMMON_TO_INSTANCE ||
					InstanceManager.getInstance().getInstance(player.getInstanceId()).isSummonAllowed())
			{
				// Need retail message if there's one.
				player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
				return false;
			}
		}

		if (clanLeader.getIsInsideGMEvent() || player.getIsInsideGMEvent())
		{
			return false;
		}

		if (player.getEvent() != null && !player.getEvent().onEscapeUse(player.getObjectId()))
		{
			player.sendMessage("You are on Tenkai Event, teleporting disabled.");
			return false;
		}

		if (clanLeader.getEvent() != null && !clanLeader.getEvent().onEscapeUse(clanLeader.getObjectId()))
		{
			// Need retail message if there's one.
			player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
			return false;
		}

		return true;
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.model.actor.L2SquadTrainer#showSubUnitSkillList(l2server.gameserver.model.actor.instance.L2PcInstance)
	 */
	@Override
	public void showSubUnitSkillList(L2PcInstance player)
	{
		onBypassFeedback(player, "subskills");
	}
}
