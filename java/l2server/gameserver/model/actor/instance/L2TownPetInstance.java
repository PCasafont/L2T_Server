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
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.MyTargetSelected;
import l2server.gameserver.network.serverpackets.ValidateLocation;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.util.Rnd;

/**
 * @author Kerberos
 */

public class L2TownPetInstance extends L2Npc
{
	int randomX, randomY, spawnX, spawnY;

	public L2TownPetInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2TownPetInstance);

		if (Config.ALLOW_PET_WALKERS)
		{
			ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new RandomWalkTask(), 2000, 4000);
		}
	}

	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		if (!canTarget(player))
		{
			return;
		}

		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);

			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			// The color to display in the select window is White
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);

			// Send a Server->Client packet ValidateLocation to correct the L2ArtefactInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else if (interact)
		{
			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if (!canInteract(player))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		spawnX = getX();
		spawnY = getY();
	}

	public class RandomWalkTask implements Runnable
	{
		@Override
		public void run()
		{
			if (!isInActiveRegion())
			{
				return; // but rather the AI should be turned off completely..
			}
			randomX = spawnX + Rnd.get(2 * 50) - 50;
			randomY = spawnY + Rnd.get(2 * 50) - 50;
			setRunning();
			if (randomX != getX() && randomY != getY())
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,
						new L2CharPosition(randomX, randomY, getZ(), 0));
			}
		}
	}
}
