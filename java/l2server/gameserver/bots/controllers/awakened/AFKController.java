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
package l2server.gameserver.bots.controllers.awakened;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import l2server.gameserver.bots.controllers.BotController;
import l2server.gameserver.instancemanager.ZoneManager;
import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.zone.type.L2TownZone;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;

public class AFKController extends BotController
{
	private static final boolean DEBUG = false;
	private static final int CHANCE_TO_QUIT_CHECK_PLAYER_SHOPS = 80;
	@SuppressWarnings("unused") // TODO
	private static final int CHANCE_TO_QUIT_CHECK_TOWN_SHOPS = 50;
	@SuppressWarnings("unused") // TODO
	private static final int DELAY_FIRST_ACTION = 1;
	private static final int MAX_AFK_TIME = 60;
	private static final int DELAY_MOVE_BETWEEN_PLAYER_SHOPS = 2;
	@SuppressWarnings("unused") // TODO
	private static final int DELAY_MOVE_BETWEEN_TOWN_SHOPS = 10;
	
	private List<Integer> _checkedObjects = new ArrayList<Integer>();
	private List<AfkModes> _afkModesOrder = new ArrayList<AfkModes>();
	
	private AfkModes _currentMode;
	private Location _nextLocation;
	private long _lastMovementAction;
	private boolean _started = true;
	
	private static enum AfkModes
	{
		/**
		 * TOTALLY_AFK
		 * On this mode the bot will be totally afk(genius)
		 */
		TOTALLY_AFK,
		
		/**
		 * CHECKING_SHOPS
		 * On this mode the bot will check randomly all the player shops
		 */
		CHECKING_SHOPS,
		
		/**
		 * SIMPLE_ROUTINE
		 * On this mode the bot will move randomly into some town npcs
		 */
		//SIMPLE_ROUTINE,
	}
	
	public AFKController(final L2PcInstance player)
	{
		super(player);
		
		_afkModesOrder.addAll(EnumSet.allOf(AfkModes.class));
		Collections.shuffle(_afkModesOrder);
		
		_currentMode = _afkModesOrder.get(0);
		
		//GmListTable.broadcastMessageToGMs(player.getName() + " running in mode: " + _currentMode.toString());
		
		if (DEBUG)
			Log.info("Starting " + _player.getName() + " at mode " + _currentMode.toString());
	}
	
	@Override
	public void startController()
	{
		super.startController();
		
		_lastMovementAction = System.currentTimeMillis() + 20000 + Rnd.get(20000);
	}
	
	@Override
	protected L2CharPosition getBestPositionToMoveNext(final boolean prefersForward, boolean allowOppositeRetry, Location loc)
	{
		//This function is only used when the character is not moving sooo...
		if (_lastMovementAction > System.currentTimeMillis())
		{	
			if (_nextLocation != null)
			{
				if (!Util.checkIfInRange(100, _player.getX(), _player.getY(), _player.getZ(), _nextLocation.getX(), _nextLocation.getY(), _nextLocation.getZ(), false))
					return super.getBestPositionToMoveNext(prefersForward, allowOppositeRetry, _nextLocation);
			}
			return null;
		}

		if (_started && _currentMode == AfkModes.TOTALLY_AFK)
		{
			_started = false;
			_lastMovementAction = System.currentTimeMillis() + Rnd.get(MAX_AFK_TIME / 6, MAX_AFK_TIME) * 60000;
			return new L2CharPosition(_player.getX() + Rnd.get(-100, 100), _player.getY() + Rnd.get(-100, 100), _player.getZ() + 10, 0);
		}
		
		int x = 0;
		int y = 0;
		int z = 0;
		int h = 0;
		
		boolean changeMode = false;
		switch(_currentMode)
		{
			case CHECKING_SHOPS:
			{
				//Our shop right now is already spawned at any town, lets move randomly between the player shops
				L2TownZone townZone = ZoneManager.getInstance().getZone(_player.getX(), _player.getY(), _player.getZ(), L2TownZone.class);
				if (townZone != null)
				{
					List<L2PcInstance> townPlayers = townZone.getPlayersInside();
					if (!townPlayers.isEmpty())
					{
						double maxRange = 300000000;
						L2PcInstance temp = null;
						
						//Get the close shop
						for (L2PcInstance shop : townPlayers)
						{
							if (shop == null || !shop.isInStoreMode() || !_checkedObjects.contains(shop.getObjectId()))
								continue;
							
							double dist = _player.getDistanceSq(shop);
							if (dist < maxRange)
							{
								maxRange = dist;
								temp = shop;
							}
						}
						
						//Move to the most close shop
						if (temp != null)
						{
							_checkedObjects.add(temp.getObjectId());
							
							//Get the shop position...
							x = temp.getX() + Rnd.get(-50, 50);
							y = temp.getY() + Rnd.get(-50, 50);
							z = temp.getZ() + 10;
							
							if (DEBUG)
								Log.info("Moving " + _player.getName() + " to " + temp.getName() + " shop!");
							
							_nextLocation = new Location(x, y, z, h);
							_lastMovementAction = System.currentTimeMillis() + Rnd.get(DELAY_MOVE_BETWEEN_PLAYER_SHOPS / 2, DELAY_MOVE_BETWEEN_PLAYER_SHOPS) * 60000;
						}
						else
							changeMode = true;
					}
					else
						changeMode = true;
					
					if (!changeMode && Rnd.get(100) < CHANCE_TO_QUIT_CHECK_PLAYER_SHOPS)
						changeMode = true;
				}
				else
					changeMode = true;
			}
			break;
			
			/*case SIMPLE_ROUTINE:
			{
				L2TownZone zone = TownManager.getTown(_player.getX(), _player.getY(), _player.getZ());
				if (zone != null)
				{
					List<L2Npc> townNpcs = new ArrayList<L2Npc>();
					for (L2Npc npc : zone.getNpcsInside())
					{
						if (npc == null)
							continue;
						
						if (npc instanceof L2MerchantInstance && !(npc instanceof L2ClanHallManagerInstance))
						{
							if (!_checkedObjects.contains(npc))
							{
								townNpcs.add(npc);
							}
						}
					}
					
					//Select one random npc
					if (!townNpcs.isEmpty())
					{
						L2Npc npc = townNpcs.get(Rnd.get(townNpcs.size()));
						if (npc != null)
						{
							x = npc.getX() + Rnd.get(-50, 50);
							y = npc.getY() + Rnd.get(-50, 50);
							z = npc.getZ() + 10;
							
							if (DEBUG)
								Log.info("Moving " + _player.getName() + " to " + npc.getName() + " npc!");
							
							_checkedObjects.add(npc.getObjectId());
							
							_nextLocation = new Location(x, y, z, h);
							_lastMovementAction = System.currentTimeMillis() + Rnd.get(DELAY_MOVE_BETWEEN_TOWN_SHOPS / 2, DELAY_MOVE_BETWEEN_TOWN_SHOPS) * 60000;
							
							if (Rnd.get(100) < CHANCE_TO_QUIT_CHECK_TOWN_SHOPS)
								changeMode = true;
						}
						else
							changeMode = true;
					}
					else
						changeMode = true;
				}
				else
					changeMode = true;
			}
			break;
			*/
			case TOTALLY_AFK:
				changeMode = true;
				break;
		}
		
		if (changeMode && _lastMovementAction < System.currentTimeMillis())
		{
			_nextLocation = null;//new Location(_player.getX() + Rnd.get(-100, 100), _player.getY() + Rnd.get(-100, 100), _player.getZ() + 10, 0);
			
			int nextPos = 0;
			for (AfkModes mode : _afkModesOrder)
			{
				if (mode == _currentMode)
					break;
				nextPos++;
			}
			
			nextPos += 1;
			
			if (nextPos < _afkModesOrder.size())
			{
				_currentMode = _afkModesOrder.get(nextPos);
				
				//GmListTable.broadcastMessageToGMs(_player.getName() + " running in mode: " + _currentMode.toString());
				
				if (_currentMode == AfkModes.TOTALLY_AFK)
					_lastMovementAction = System.currentTimeMillis() + Rnd.get(MAX_AFK_TIME / 6, MAX_AFK_TIME) * 60000;
			}
			else
			{
				_player.getBotController().onExitWorld();
				return null;
			}
			
			if (DEBUG)
				Log.info("Changing " + _player.getName() + " mode to " + _currentMode.toString());
		}
		
		return null;
		//return super.getBestPositionToMoveNext(prefersForward, allowOppositeRetry, _nextLocation);
	}
}