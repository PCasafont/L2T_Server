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
package custom.AFKS;

import java.util.Collection;
import java.util.List;

import l2server.gameserver.GeoData;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.bots.BotMode;
import l2server.gameserver.bots.BotsManager;
import l2server.gameserver.instancemanager.ZoneManager;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.zone.type.L2TownZone;
import l2server.util.Point3D;
import l2server.util.Rnd;

/**
 * @author LasTravel
 */

public class AFKS extends Quest
{
	@SuppressWarnings("unused") // TODO
	private static final boolean _debug = true;
	private static final String _qn = "AFKS";

	private static final int DELAY_FIRST_START = 1;
	private static final int DELAY_UPDATE_TASK = 30; // In seconds plz
	@SuppressWarnings("unused")
	private static final int MAX_FAKERS_AT_EACH_TOWN = 30;
	
	private static int botUsageName = 1;
	
	public AFKS(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AFKSTASK(), DELAY_FIRST_START * 1000L, DELAY_UPDATE_TASK * 1000L);
	}
	
	private class AFKSTASK implements Runnable
	{
		public void run()
		{
			Collection<L2TownZone> allTowns = ZoneManager.getInstance().getAllZones(L2TownZone.class);
			if (!allTowns.isEmpty())
			{
				for (L2TownZone zone : allTowns)
				{
					if (zone == null || zone.getName().equalsIgnoreCase("gainak"))
						continue;
					
					List<Location> spawns = zone.getSpawns();
					List<L2PcInstance> playersInTown = zone.getPlayersInside();
					if (spawns.isEmpty() && playersInTown.isEmpty())
						continue;

					int townBots = 0;
					int townPlayers = 0;
					for (L2PcInstance player : playersInTown)
					{
						if (player == null)
							continue;
						
						if (player.isBot())
						{
							//System.out.println("BOT " + zone.getName() + ": " + player.getName());
							townBots++;
						}
						else
						{
							//System.out.println("PLAYER " + zone.getName() + ": " + player.getName());
							townPlayers++;
						}
					}

					//if (townPlayers > 0)
					//	System.out.println(zone.getName() + ": " + townPlayers + ", " + townBots);
					float multiplier = 0.2f;
					//if (zone.getId() == 11022)
					//	multiplier = 1.0f;
					
					int maxBotCount = Math.round(townPlayers * multiplier);
					if (townBots < maxBotCount)
					{
						//int rndBots = Rnd.get(MAX_FAKERS_AT_EACH_TOWN / 2, MAX_FAKERS_AT_EACH_TOWN);
						int rndBots = maxBotCount - townBots;
						
						BotsManager.getInstance().prepareBots(rndBots, 99, String.valueOf(botUsageName), BotMode.AFKER);
						
						List<L2PcInstance> botsToUse = BotsManager.getInstance().getBots(String.valueOf(botUsageName));
						if (botsToUse.isEmpty())
							return;
						
						botUsageName++;
						
						for (int i = 0; i < rndBots; i++)
						{
							Location loc = null;
							if (playersInTown.size() > 3)
							{
								L2PcInstance player1 = playersInTown.get(Rnd.get(playersInTown.size()));
								int loops = 0;
								while (loops < playersInTown.size() && player1.isBot())
								{
									player1 = playersInTown.get(Rnd.get(playersInTown.size()));
									loops++;
								}
									
								L2PcInstance player2 = playersInTown.get(Rnd.get(playersInTown.size()));
								loops = 0;
								while (loops < playersInTown.size() && !GeoData.getInstance().canSeeTarget(player1, player2))
								{
									player2 = playersInTown.get(Rnd.get(playersInTown.size()));
									loops++;
								}
								
								if (GeoData.getInstance().canSeeTarget(player1, player2))
								{
									int dx = Math.round((player2.getX() - player1.getX())
											* (0.3f + (float)Rnd.get() * 0.4f));
									int dy = Math.round((player2.getY() - player1.getY())
											* (0.3f + (float)Rnd.get() * 0.4f));
									int x = player1.getX() + dx + Rnd.get(-200, 200);
									int y = player1.getY() + dy + Rnd.get(-200, 200);
									int z = Math.max(player1.getZ(), player2.getZ());
									//System.out.println("Player 1 (" + player1.getName() + "): " + player1.getX() + ", " + player1.getY());
									//System.out.println("Player 2 (" + player2.getName() + "): " + player2.getX() + ", " + player2.getY());
									//System.out.println("Result: " + x + ", " + y);
									if (GeoData.getInstance().canSeeTarget(player1, new Point3D(x, y, z)))
									{
										//System.out.println("Success!");
										z = GeoData.getInstance().getHeight(x, y, z);
										loc = new Location(x, y, z, Rnd.get(65535));
									}
								}
							}
							
							if (loc == null)
							{
								loc = spawns.get(Rnd.get(spawns.size()));
								if (loc != null)
									loc = new Location(loc.getX() + Rnd.get(-200, 200), loc.getY() + Rnd.get(-200, 200), loc.getZ(), Rnd.get(65535));
							}
							
							if (loc != null)
							{
								final L2PcInstance rndBot = botsToUse.get(Rnd.get(botsToUse.size()));
								if (rndBot != null)
								{
									botsToUse.remove(rndBot);
									
									final Location finalLoc = loc;
									ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
									{
										public void run()
										{
											rndBot.teleToLocation(finalLoc.getX(), finalLoc.getY(), finalLoc.getZ());
											rndBot.getBotController().startController();
										}
									}, Rnd.get(DELAY_UPDATE_TASK) * 1000);
								}
							}
						}
					}
				}
			}
		}
	}
	
	public static void main(String[] args)
	{
		new AFKS(-1, _qn, "custom");
	}
}
