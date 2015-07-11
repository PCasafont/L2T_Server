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
package handlers.usercommandhandlers;

import java.util.logging.Level;

import l2tserver.Config;
import l2tserver.gameserver.ThreadPoolManager;
import l2tserver.gameserver.TimeController;
import l2tserver.gameserver.ai.CtrlIntention;
import l2tserver.gameserver.datatables.MapRegionTable;
import l2tserver.gameserver.datatables.SkillTable;
import l2tserver.gameserver.handler.IUserCommandHandler;
import l2tserver.gameserver.instancemanager.GrandBossManager;
import l2tserver.gameserver.model.L2Skill;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.network.serverpackets.ActionFailed;
import l2tserver.gameserver.network.serverpackets.MagicSkillUse;
import l2tserver.gameserver.network.serverpackets.SetupGauge;
import l2tserver.gameserver.util.Broadcast;


/**
 *
 *
 */
public class Escape implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		52
	};
	
	/**
	 * 
	 * @see l2tserver.gameserver.handler.IUserCommandHandler#useUserCommand(int, l2tserver.gameserver.model.actor.instance.L2PcInstance)
	 */
	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		// Thanks nbd, such leetness
		if (activeChar.getEvent() != null && !activeChar.getEvent().onEscapeUse(activeChar.getObjectId()))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (!activeChar.canEscape() || activeChar.isCombatFlagEquipped())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		int unstuckTimer = (activeChar.getAccessLevel().isGm() ? 1000 : Config.UNSTUCK_INTERVAL * 1000);
		
		// Check to see if player is in jail
		if (activeChar.isInJail())
		{
			activeChar.sendMessage("You can not escape from jail.");
			return false;
		}
		
		// Check to see if player is immobilized (Soul)
		if (activeChar.isImmobilized())
		{
			activeChar.sendMessage("You cannot escape if you are immobilized.");
			return false;
		}
		
		if (GrandBossManager.getInstance().getZone(activeChar) != null && !activeChar.isGM())
		{
			activeChar.sendMessage("You may not use an escape command in a Boss Zone.");
			return false;
		}
		
		if (activeChar.isCastingNow() || activeChar.isMovementDisabled() || activeChar.isMuted()
				|| activeChar.isAlikeDead() || activeChar.isInOlympiadMode() || activeChar.inObserverMode())
			return false;
		activeChar.forceIsCasting(TimeController.getGameTicks() + unstuckTimer / TimeController.MILLIS_IN_TICK);
		
		
		L2Skill escape = SkillTable.getInstance().getInfo(2099, 1); // 5 minutes escape
		L2Skill GM_escape = SkillTable.getInstance().getInfo(2100, 1); // 1 second escape
		if (activeChar.getAccessLevel().isGm())
		{
			if (GM_escape != null)
			{
				activeChar.doCast(GM_escape);
				return true;
			}
			activeChar.sendMessage("You use Escape: 1 second.");
		}
		else if (Config.UNSTUCK_INTERVAL == 300 && escape  != null)
		{
			activeChar.doCast(escape);
			return true;
		}
		else
		{
			if (Config.UNSTUCK_INTERVAL > 100)
			{
				activeChar.sendMessage("You use Escape: " + unstuckTimer / 60000 + " minutes.");
			}
			else
				activeChar.sendMessage("You use Escape: " + unstuckTimer / 1000 + " seconds.");
		}
		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		//SoE Animation section
		activeChar.setTarget(activeChar);
		activeChar.disableAllSkills();
		
		MagicSkillUse msk = new MagicSkillUse(activeChar, 1050, 1, unstuckTimer, 0);
		Broadcast.toSelfAndKnownPlayersInRadius(activeChar, msk, 810000/*900*/);
		SetupGauge sg = new SetupGauge(0, unstuckTimer);
		activeChar.sendPacket(sg);
		//End SoE Animation section
		
		EscapeFinalizer ef = new EscapeFinalizer(activeChar);
		// continue execution later
		activeChar.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(ef, unstuckTimer));
		
		return true;
	}
	
	static class EscapeFinalizer implements Runnable
	{
		private L2PcInstance _activeChar;
		
		EscapeFinalizer(L2PcInstance activeChar)
		{
			_activeChar = activeChar;
		}
		
		public void run()
		{
			if (_activeChar.isDead())
				return;
			
			_activeChar.enableAllSkills();
			_activeChar.setIsCastingNow(false);
			_activeChar.setInstanceId(0);
			
			try
			{
				_activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "", e);
			}
		}
	}
	
	/**
	 * 
	 * @see l2tserver.gameserver.handler.IUserCommandHandler#getUserCommandList()
	 */
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}