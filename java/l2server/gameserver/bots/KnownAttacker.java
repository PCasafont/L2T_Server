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
package l2server.gameserver.bots;

import java.util.ArrayList;
import java.util.List;

import l2server.gameserver.model.actor.L2Character;

/**
 *
 * @author LittleHakor
 */
public class KnownAttacker
{
	private final L2Character _character;
	
	// A record of the time at which this attacker last attacked us.
	private long _lastAttackTime;
	
	// The last time at which this character could not be reached.
	private long _lastUnreachabilityTime;
	
	private final List<RecentDamage> _damages = new ArrayList<RecentDamage>(3);
	
	public KnownAttacker(final L2Character character)
	{
		_character = character;
		
		_lastAttackTime = System.currentTimeMillis();
	}
	
	public final void onForget()
	{
		_damages.clear();
	}
	
	public final L2Character getCharacter()
	{
		return _character;
	}
	
	private final RecentDamage initAndGetRecentDamages(final DamageType damageType)
	{
		RecentDamage result = null;
		
		synchronized (_damages)
		{
			result = new RecentDamage(damageType);
			
			_damages.add(result);
		}
		
		return result;
	}
	
	private final RecentDamage getDamageByType(final DamageType damageType, final boolean allowInit)
	{
		RecentDamage result = null;
		if (_damages.size() == 0)
			result = allowInit ? initAndGetRecentDamages(damageType) : null;
		else
		{
			for (RecentDamage recentDamage : _damages)
			{
				if (recentDamage.getType() != damageType)
					continue;
				
				result = recentDamage;
				break;
			}
			
			if (result == null && allowInit)
				result = initAndGetRecentDamages(damageType);
		}
		
		return result;
	}
	
	public final void increaseRecentDamages(final DamageType damageType, final int damages)
	{
		final RecentDamage recentDamages = getDamageByType(damageType, true);
		
		recentDamages.incrementAmount(damages);
		
		_lastAttackTime = System.currentTimeMillis();
		
		//_character.broadcastPacket(new CreatureSay(_character.getObjectId(), Say2.ALL, _character.getName(), "Dealt " + damages + " " + damageType + ". Total = " + getTotalDamagesByType(damageType) + "."));
	}
	
	public final int getTotalDamagesByType(final DamageType damageType)
	{
		final RecentDamage recentDamages = getDamageByType(damageType, false);
		
		if (recentDamages == null)
			return 0;
		
		return recentDamages.getAmount();
	}
	
	public final int getTotalDamages()
	{
		if (_damages.size() == 0)
			return 0;
		
		int result = 0;
		for (RecentDamage recentDamage : _damages)
		{
			result += recentDamage.getAmount();
		}
		
		return result;
	}

	public final void setLastAttackTime(final long time)
	{
		_lastAttackTime = time;
	}
	
	public final long getLastAttackTime()
	{
		return _lastAttackTime;
	}
	
	public final void setLastUnreachabilityTime(final long time)
	{
		_lastUnreachabilityTime = time;
	}
	
	public final long getLastUnreachabilityTime()
	{
		return _lastUnreachabilityTime;
	}
}
