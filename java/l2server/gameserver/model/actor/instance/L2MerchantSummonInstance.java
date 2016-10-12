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
import l2server.gameserver.TradeController;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.ai.L2CharacterAI;
import l2server.gameserver.model.L2Party;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.L2TradeList;
import l2server.gameserver.model.L2WorldRegion;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.log.Log;

import java.util.StringTokenizer;

/**
 * @author Kerberos
 */
public class L2MerchantSummonInstance extends L2SummonInstance
{
	public L2MerchantSummonInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, L2Skill skill)
	{
		super(objectId, template, owner, skill);
		setInstanceType(InstanceType.L2MerchantSummonInstance);
	}

	@Override
	public boolean hasAI()
	{
		return false;
	}

	@Override
	public L2CharacterAI getAI()
	{
		return null;
	}

	@Override
	public void deleteMe(L2PcInstance owner)
	{

	}

	@Override
	public void unSummon(L2PcInstance owner)
	{
		if (isVisible())
		{
			stopAllEffects();
			L2WorldRegion oldRegion = getWorldRegion();
			decayMe();
			if (oldRegion != null)
			{
				oldRegion.removeFromZones(this);
			}
			getKnownList().removeAllKnownObjects();
			setTarget(null);
		}
	}

	@Override
	public void setFollowStatus(boolean state)
	{

	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}

	@Override
	public boolean isInvul()
	{
		return true;
	}

	@Override
	public L2Party getParty()
	{
		return null;
	}

	@Override
	public boolean isInParty()
	{
		return false;
	}

	@Override
	public boolean useMagic(L2Skill skill, boolean forceUse, boolean dontMove)
	{
		return false;
	}

	@Override
	public void doCast(L2Skill skill)
	{

	}

	@Override
	public boolean isInCombat()
	{
		return false;
	}

	@Override
	public final void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss)
	{

	}

	@Override
	public void reduceCurrentHp(double i, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{

	}

	@Override
	public void updateAndBroadcastStatus(int val)
	{

	}

	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		if (player.isOutOfControl())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);

			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			final MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);

			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else if (interact)
		{
			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if (!isInsideRadius(player, 150, false, false))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			else
			{
				showMessageWindow(player);
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	public void onBypassFeedback(L2PcInstance player, String command)
	{
		final StringTokenizer st = new StringTokenizer(command, " ");
		final String actualCommand = st.nextToken(); // Get actual command

		if (actualCommand.equalsIgnoreCase("Buy"))
		{
			if (st.countTokens() < 1)
			{
				return;
			}

			final int val = Integer.parseInt(st.nextToken());
			showBuyWindow(player, val);
		}
		else if (actualCommand.equalsIgnoreCase("Sell"))
		{
			showSellWindow(player);
		}
	}

	protected final void showBuyWindow(L2PcInstance player, int val)
	{
		double taxRate = 50;

		player.tempInventoryDisable();

		if (Config.DEBUG)
		{
			Log.fine("Showing buylist");
		}

		L2TradeList list = TradeController.getInstance().getBuyList(val);

		if (list != null && list.getNpcId() == getNpcId())
		{
			player.sendPacket(new ExBuyList(list, player.getAdena(), taxRate));
			player.sendPacket(new ExSellList(player, list, taxRate, false));
		}
		else
		{
			Log.warning("possible client hacker: " + player.getName() + " attempting to buy from GM shop! < Ban him!");
			Log.warning("buylist id:" + val);
		}

		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	protected final void showSellWindow(L2PcInstance player)
	{
		if (Config.DEBUG)
		{
			Log.fine("Showing selllist");
		}

		player.sendPacket(new SellList(player));

		if (Config.DEBUG)
		{
			Log.fine("Showing sell window");
		}

		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	private void showMessageWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		final String filename = "merchant/" + getNpcId() + ".htm";
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player.getHtmlPrefix(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
}
