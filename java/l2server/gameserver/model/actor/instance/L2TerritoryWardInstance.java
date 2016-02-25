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

import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.ClanTable;
import l2server.gameserver.instancemanager.TerritoryWarManager;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.MyTargetSelected;
import l2server.gameserver.network.serverpackets.StatusUpdate;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.network.serverpackets.ValidateLocation;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.log.Log;

public final class L2TerritoryWardInstance extends L2Attackable
{
	public L2TerritoryWardInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		
		disableCoreAI(true);
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if (isInvul())
			return false;
		if (getCastle() == null || !getCastle().getZone().isActive())
			return false;
		
		final L2PcInstance actingPlayer = attacker.getActingPlayer();
		if (actingPlayer == null)
			return false;
		if (actingPlayer.getSiegeSide() == 0)
		{
			actingPlayer.sendMessage("SiegeSiege == 0");
			return false;
		}
		if (TerritoryWarManager.getInstance().isAllyField(actingPlayer, getCastle().getCastleId()))
			return false;
		
		return true;
	}
	
	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		
		if (getCastle() == null)
			Log.warning("L2TerritoryWardInstance(" + getName() + ") spawned outside Castle Zone!");
	}
	
	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		if (skill != null || !TerritoryWarManager.getInstance().isTWInProgress()) //wards can't be damaged by skills
			return;
		
		final L2PcInstance actingPlayer = attacker.getActingPlayer();
		if (actingPlayer == null)
			return;
		if (actingPlayer.isCombatFlagEquipped())
			return;
		if (actingPlayer.getSiegeSide() == 0)
			return;
		if (getCastle() == null) // ?
			return;
		if (TerritoryWarManager.getInstance().isAllyField(actingPlayer, getCastle().getCastleId()))
			return;
		
		super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);
	}
	
	@Override
	public void reduceCurrentHpByDOT(double i, L2Character attacker, L2Skill skill)
	{
		// wards can't be damaged by DOTs
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		// Kill the L2NpcInstance (the corpse disappeared after 7 seconds)
		if (!super.doDie(killer) || getCastle() == null || !TerritoryWarManager.getInstance().isTWInProgress())
			return false;
		
		if (killer instanceof L2PcInstance)
		{
			if (((L2PcInstance) killer).getSiegeSide() > 0 && !((L2PcInstance) killer).isCombatFlagEquipped())
				((L2PcInstance) killer).addItem("Pickup", getNpcId() - 23012, 1, null, false);
			else
				TerritoryWarManager.getInstance().getTerritoryWard(getNpcId() - 36491).spawnMe();
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_S1_WARD_HAS_BEEN_DESTROYED_C2_HAS_THE_WARD);
			sm.addString(getName().replaceAll(" Ward", ""));
			sm.addPcName((L2PcInstance) killer);
			TerritoryWarManager.getInstance().announceToParticipants(sm, 0, 0);
		}
		else
			TerritoryWarManager.getInstance().getTerritoryWard(getNpcId() - 36491).spawnMe();
		decayMe();
		return true;
	}
	
	@Override
	public void onForcedAttack(L2PcInstance player)
	{
		onAction(player);
	}
	
	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		if (player == null || !canTarget(player))
			return;
		
		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
			player.sendPacket(my);
			
			// Send a Server->Client packet StatusUpdate of the L2NpcInstance to the L2PcInstance to update its HP bar
			StatusUpdate su = new StatusUpdate(this);
			su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
			su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
			player.sendPacket(su);
			
			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else if (interact)
		{
			if (isAutoAttackable(player) && Math.abs(player.getZ() - getZ()) < 100)
			{
				// Tenkai custom - only castle owners who are not in same ally like ward owner can pick
				if (player.getClan() == null || player.getClan().getHasCastle() == 0 || player.getAllyId() != 0 && player.getAllyId() == ClanTable.getInstance().getClan(this.getCastle().getOwnerId()).getAllyId())
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			}
			else
			{
				// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}
}
