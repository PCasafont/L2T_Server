/*
 *@author Julian
 *
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

import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.events.instanced.EventsManager;
import l2server.gameserver.events.instanced.types.LuckyChests;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.MagicSkillUse;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.util.Rnd;

/**
 * This class manages all chest.
 */
public class L2ChestInstance extends L2MonsterInstance
{
	private volatile boolean _isInteracted;
	private volatile boolean _specialDrop;

	public L2ChestInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2ChestInstance);
		_isInteracted = false;
		_specialDrop = false;
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		_isInteracted = false;
		_specialDrop = false;
		setMustRewardExpSp(true);
	}

	public synchronized boolean isInteracted()
	{
		return _isInteracted;
	}

	public synchronized void setInteracted()
	{
		_isInteracted = true;
	}

	public synchronized boolean isSpecialDrop()
	{
		return _specialDrop;
	}

	public synchronized void setSpecialDrop()
	{
		_specialDrop = true;
	}

	@Override
	public void doItemDrop(L2NpcTemplate npcTemplate, L2Character lastAttacker)
	{
		int id = getTemplate().NpcId;

		if (!_specialDrop)
		{
			if (id >= 18265 && id <= 18286)
			{
				id += 3536;
			}
			else if (id == 18287 || id == 18288)
			{
				id = 21671;
			}
			else if (id == 18289 || id == 18290)
			{
				id = 21694;
			}
			else if (id == 18291 || id == 18292)
			{
				id = 21717;
			}
			else if (id == 18293 || id == 18294)
			{
				id = 21740;
			}
			else if (id == 18295 || id == 18296)
			{
				id = 21763;
			}
			else if (id == 18297 || id == 18298)
			{
				id = 21786;
			}
		}

		super.doItemDrop(NpcTable.getInstance().getTemplate(id), lastAttacker);
	}

	//cast - trap chest
	public void chestTrap(L2Character player)
	{
		int trapSkillId = 0;
		int rnd = Rnd.get(120);

		if (getTemplate().Level >= 61)
		{
			if (rnd >= 90)
			{
				trapSkillId = 4139;//explosion
			}
			else if (rnd >= 50)
			{
				trapSkillId = 4118;//area paralysys
			}
			else if (rnd >= 20)
			{
				trapSkillId = 1167;//poison cloud
			}
			else
			{
				trapSkillId = 223;//sting
			}
		}
		else if (getTemplate().Level >= 41)
		{
			if (rnd >= 90)
			{
				trapSkillId = 4139;//explosion
			}
			else if (rnd >= 60)
			{
				trapSkillId = 96;//bleed
			}
			else if (rnd >= 20)
			{
				trapSkillId = 1167;//poison cloud
			}
			else
			{
				trapSkillId = 4118;//area paralysys
			}
		}
		else if (getTemplate().Level >= 21)
		{
			if (rnd >= 80)
			{
				trapSkillId = 4139;//explosion
			}
			else if (rnd >= 50)
			{
				trapSkillId = 96;//bleed
			}
			else if (rnd >= 20)
			{
				trapSkillId = 1167;//poison cloud
			}
			else
			{
				trapSkillId = 129;//poison
			}
		}
		else
		{
			if (rnd >= 80)
			{
				trapSkillId = 4139;//explosion
			}
			else if (rnd >= 50)
			{
				trapSkillId = 96;//bleed
			}
			else
			{
				trapSkillId = 129;//poison
			}
		}

		player.sendMessage("There was a trap!");
		handleCast(player, trapSkillId);
	}

	//<--
	//cast casse
	//<--
	private boolean handleCast(L2Character player, int skillId)
	{
		int skillLevel = 1;
		byte lvl = getTemplate().Level;
		if (lvl > 20 && lvl <= 40)
		{
			skillLevel = 3;
		}
		else if (lvl > 40 && lvl <= 60)
		{
			skillLevel = 5;
		}
		else if (lvl > 60)
		{
			skillLevel = 6;
		}

		if (player.isDead() || !player.isVisible() ||
				!player.isInsideRadius(this, getDistanceToWatchObject(player), false, false))
		{
			return false;
		}

		L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);

		if (player.getFirstEffect(skill) == null)
		{
			skill.getEffects(this, player);
			broadcastPacket(new MagicSkillUse(this, player, skill.getId(), skillLevel, skill.getHitTime(), 0, 0));
			return true;
		}
		return false;
	}

	@Override
	public boolean isMovementDisabled()
	{
		if (super.isMovementDisabled())
		{
			return true;
		}
		return !isInteracted();
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (getNpcId() == 44000 && killer instanceof L2PcInstance &&
				((L2PcInstance) killer).getEvent() instanceof LuckyChests)
		{
			int prize = Rnd.get(100);
			L2PcInstance player = (L2PcInstance) killer;
			LuckyChests event = (LuckyChests) player.getEvent();
			MagicSkillUse MSU;
			L2Skill skill;
			if (prize == 0)
			{
				MSU = new MagicSkillUse(player, player, 2025, 1, 1, 0, 0);
				skill = SkillTable.getInstance().getInfo(2025, 1);
				player.sendPacket(MSU);
				player.broadcastPacket(MSU);
				player.useMagic(skill, false, false);
				event.chestPoints(player, 20);
			}
			else if (prize < 5)
			{
				MSU = new MagicSkillUse(player, player, 2024, 1, 1, 0, 0);
				skill = SkillTable.getInstance().getInfo(2024, 1);
				player.sendPacket(MSU);
				player.broadcastPacket(MSU);
				player.useMagic(skill, false, false);
				event.chestPoints(player, 5);
			}
			else if (prize < 25)
			{
				MSU = new MagicSkillUse(player, player, 2023, 1, 1, 0, 0);
				skill = SkillTable.getInstance().getInfo(2023, 1);
				player.sendPacket(MSU);
				player.broadcastPacket(MSU);
				player.useMagic(skill, false, false);
				event.chestPoints(player, 1);
			}
			else if (prize < 35)
			{
				player.stopAllEffects();
				player.reduceCurrentHp(player.getMaxHp() + player.getMaxCp() + 1, this, null);
				player.sendMessage("The chest contained the death!!!");
			}
			else if (prize < 45)
			{
				SkillTable.getInstance().getInfo(1069, 1).getEffects(this, player);
				player.sendMessage("The chest was full of sleeping spores!");
			}
			else if (prize < 55)
			{
				SkillTable.getInstance().getInfo(92, 1).getEffects(this, player);
				player.sendMessage("You have been stunned by the flash that this chest did when opened!");
			}
			else if (prize < 65)
			{
				SkillTable.getInstance().getInfo(495, 10).getEffects(this, player);
				player.sendMessage("The chest shot a lot of knives!");
			}
			else if (prize < 75)
			{
				SkillTable.getInstance().getInfo(736, 1).getEffects(this, player);
				player.sendMessage("The chest was full of poison spores!");
			}
			else
			{
				player.sendMessage("The chest is empty..");
			}
		}
		return super.doDie(killer);
	}

	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		if (getNpcId() == 44000 && !EventsManager.getInstance().isPlayerParticipant(player.getObjectId()))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		super.onAction(player, interact);
	}
}
