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
package l2tserver.gameserver.stats.skills;

import java.util.logging.Level;

import l2tserver.gameserver.datatables.NpcTable;
import l2tserver.gameserver.model.L2Abnormal;
import l2tserver.gameserver.model.L2Object;
import l2tserver.gameserver.model.L2Skill;
import l2tserver.gameserver.model.L2Spawn;
import l2tserver.gameserver.model.actor.L2Character;
import l2tserver.gameserver.model.actor.L2Npc;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.stats.Formulas;
import l2tserver.gameserver.templates.StatsSet;
import l2tserver.gameserver.templates.chars.L2NpcTemplate;
import l2tserver.log.Log;
import l2tserver.util.Point3D;
import l2tserver.util.Rnd;

public class L2SkillSpawn extends L2Skill
{
	private final int _npcId;
	private final int _despawnDelay;
	private final boolean _summonSpawn;
	private final boolean _randomOffset;
	private int _count;
	
	public L2SkillSpawn(StatsSet set)
	{
		super(set);
		_npcId = set.getInteger("npcId", 0);
		_despawnDelay = set.getInteger("despawnDelay", 0);
		_summonSpawn = set.getBool("isSummonSpawn", false);
		_randomOffset = set.getBool("randomOffset", true);
		_count = set.getInteger("count", 1);
	}
	
	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		if (caster.isAlikeDead())
			return;
		
		if (_npcId == 0)
		{
			Log.warning("NPC ID not defined for skill ID:"+this.getId());
			return;
		}
		
		final L2NpcTemplate template = NpcTable.getInstance().getTemplate(_npcId);
		if (template == null)
		{
			Log.warning("Spawn of the nonexisting NPC ID:"+_npcId+", skill ID:"+this.getId());
			return;
		}
		
		try
		{
			int x = 0;
			int y = 0;
			int z = 0;
			
			boolean skillMastery = Formulas.calcSkillMastery(caster, this);
			int first = 0;
			if (skillMastery && getId() == 10532)
				first =- _count;
			
			for (int i = first; i < _count; i++)
			{
				final L2Spawn spawn = new L2Spawn(template);
				
				spawn.setInstanceId(caster.getInstanceId());
				spawn.setHeading(-1);
				
				if (caster instanceof L2PcInstance && getTargetType() == L2Skill.SkillTargetType.TARGET_GROUND)
				{
					Point3D wordPosition = ((L2PcInstance)caster).getSkillCastPosition();
					
					if (wordPosition != null)
					{
						x = wordPosition.getX();
						y = wordPosition.getY();
						z = wordPosition.getZ();
					}
				}
				else
				{	
					if (_randomOffset)
					{
						x = caster.getX() + (Rnd.nextBoolean() ? Rnd.get(20, 50) : Rnd.get(-50, -20));
						y = caster.getY() + (Rnd.nextBoolean() ? Rnd.get(20, 50) : Rnd.get(-50, -20));
					}
					else
					{
						x = caster.getX();
						y = caster.getY();
					}
					z = caster.getZ();
				}
				
				spawn.setX(x);
				spawn.setY(y);
				spawn.setZ(z + 20);
				
				spawn.stopRespawn();
				
				L2Npc npc = spawn.getNpc();
				
				if (caster instanceof L2PcInstance)
				{	
					npc.setOwner((L2PcInstance)caster);
					npc.setInstanceId(caster.getInstanceId());
				}
				spawn.doSpawn(_summonSpawn);
				
				if (_despawnDelay > 0)
					npc.scheduleDespawn(_despawnDelay);
			}
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Exception while spawning NPC ID: " + _npcId + ", skill ID: " + this.getId() + ", exception: " + e.getMessage(), e);
		}
		 
		// self Effect
		if (hasSelfEffects())
		{
			final L2Abnormal effect = caster.getFirstEffect(getId());
			if (effect != null && effect.isSelfEffect())
			{
				//Replace old effect with new one.
				effect.exit();
			}
			getEffectsSelf(caster);
		}
	}
}
