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

package l2server.gameserver.ai.aplayer;

import l2server.gameserver.GeoData;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.ai.L2PlayerAI;
import l2server.gameserver.datatables.EnchantItemTable;
import l2server.gameserver.datatables.MapRegionTable.TeleportWhereType;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.handler.IItemHandler;
import l2server.gameserver.handler.ItemHandler;
import l2server.gameserver.model.*;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Character.AIAccessor;
import l2server.gameserver.model.actor.instance.L2ApInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.templates.item.L2ArmorType;
import l2server.gameserver.templates.item.L2Item;
import l2server.gameserver.templates.item.L2WeaponType;
import l2server.gameserver.templates.skills.L2SkillType;
import l2server.log.Log;
import l2server.util.Point3D;
import l2server.util.Rnd;

import java.util.concurrent.ScheduledFuture;

/**
 * @author Pere
 *         This is the abstract superclass of APlayer AI
 */
public abstract class L2APlayerAI extends L2PlayerAI implements Runnable
{
	protected L2ApInstance _player;
	protected long _timer;

	//protected TIntIntHashMap _hate = new TIntIntHashMap();

	private ScheduledFuture<?> _task;

	public L2APlayerAI(AIAccessor accessor)
	{
		super(accessor);
		_player = (L2ApInstance) _accessor.getActor();
		_task = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 5000, 1000);

		// The players always run
		_player.setRunning();

		//_player.setTitle("L2 Tenkai");

		checkGear();

		for (L2ItemInstance item : _player.getInventory().getItems())
		{
			if (item == null || !item.isEquipped())
			{
				continue;
			}

			boolean hasElement = false;
			if (item.getElementals() != null)
			{
				for (Elementals elem : item.getElementals())
				{
					if (elem != null && elem.getValue() > 0)
					{
						hasElement = true;
					}
				}
			}

			if (hasElement)
			{
				continue;
			}

			if (!(item.getItem().getItemType() == L2WeaponType.FISHINGROD || item.isShadowItem() ||
					item.isCommonItem() || item.isPvp() || item.isHeroItem() || item.isTimeLimitedItem() ||
					item.getItemId() >= 7816 && item.getItemId() <= 7831 ||
					item.getItem().getItemType() == L2WeaponType.NONE ||
					item.getItem().getItemGradePlain() != L2Item.CRYSTAL_S &&
							item.getItem().getItemGradePlain() != L2Item.CRYSTAL_R ||
					item.getItem().getBodyPart() == L2Item.SLOT_BACK ||
					item.getItem().getBodyPart() == L2Item.SLOT_R_BRACELET ||
					item.getItem().getBodyPart() == L2Item.SLOT_UNDERWEAR ||
					item.getItem().getBodyPart() == L2Item.SLOT_BELT ||
					item.getItem().getBodyPart() == L2Item.SLOT_NECK ||
					(item.getItem().getBodyPart() & L2Item.SLOT_R_EAR) != 0 ||
					(item.getItem().getBodyPart() & L2Item.SLOT_R_FINGER) != 0 ||
					item.getItem().getElementals() != null || item.getItemType() == L2ArmorType.SHIELD ||
					item.getItemType() == L2ArmorType.SIGIL))
			{
				if (item.isWeapon())
				{
					item.setElementAttr((byte) Rnd.get(6), 300);
				}
				else
				{
					for (int elem = 0; elem < 6; elem += 2)
					{
						item.setElementAttr((byte) (elem + Rnd.get(2)), 120);
					}
				}
			}
		}
	}

	protected abstract int[] getRandomGear();

	private void checkGear()
	{
		if (_player.getActiveWeaponItem() != null && _player.getActiveWeaponItem().getCrystalType() > L2Item.CRYSTAL_D)
		{
			return;
		}

		for (int itemId : getRandomGear())
		{
			L2ItemInstance item = _player.getInventory().addItem("", itemId, 1, _player, _player);
			if (item != null && EnchantItemTable.isEnchantable(item))
			{
				item.setEnchantLevel(10 + Rnd.get(5));
			}
		}

		for (L2ItemInstance item : _player.getInventory().getItems())
		{
			if (item == null)
			{
				continue;
			}

			if (item.isEquipable() && !item.isEquipped())
			{
				if (item.getItem().getCrystalType() > L2Item.CRYSTAL_D)
				{
					_player.useEquippableItem(item, false);
				}
				else
				{
					_player.destroyItem("Destroy", item, _player, false);
				}
			}
		}
	}

	protected L2Character decideTarget()
	{
		L2Character target = _player.getTarget() instanceof L2Character ? (L2Character) _player.getTarget() : null;

		if (_player.getParty() != null)
		{
			target = _player.getParty().getTarget();
			//if (!_player.isInsideRadius(target, 3000, false, false))
			//	_player.teleToLocation(target.getX(), target.getY(), target.getZ());
			for (L2PcInstance member : _player.getParty().getPartyMembers())
			{
				if (member.isDead() && member.getClassId() == 146)
				{
					interactWith(member);
				}
			}
		}

		//if (target == null || target.isDead())
		//	L2World.getInstance().getPlayer("lolol");

		if (target == null || target.getDistanceSq(_player) > 2000 * 2000 || !target.isVisible())
		{
			for (L2Character cha : _player.getKnownList().getKnownCharacters())
			{
				if (!cha.isDead() && cha.isVisible() && _player.isEnemy(cha))
				{
					target = cha;
					break;
				}
			}
		}

		if (target != null && !_player.isEnemy(target))
		{
			target = null;
		}

		_player.setTarget(target);

		return target;
	}

	protected void travelTo(L2Character target)
	{
		Point3D direction = new Point3D(target.getX() - _player.getX(), target.getY() - _player.getY(),
				target.getZ() - _player.getZ());
		double length = Math.sqrt(
				(long) direction.getX() * (long) direction.getX() + (long) direction.getY() * (long) direction.getY());
		double angle = Math.acos(direction.getX() / length);
		if (direction.getY() < 0)
		{
			angle = Math.PI * 2 - angle;
		}

		int newX = _player.getX() + (int) (1000 * Math.cos(angle));
		int newY = _player.getY() + (int) (1000 * Math.sin(angle));
		int newZ = GeoData.getInstance().getHeight(newX, newY, _player.getZ());
		//int newX = target.getX();
		//int newY = target.getY();
		//int newZ = target.getZ();

		double offset = 0.1;
		while (!GeoData.getInstance()
				.canMoveFromToTarget(_player.getX(), _player.getY(), _player.getZ(), newX, newY, newZ, 0) &&
				offset < Math.PI)
		{
			newX = _player.getX() + (int) (1000 * Math.cos(angle + offset));
			newY = _player.getY() + (int) (1000 * Math.sin(angle + offset));
			newZ = GeoData.getInstance().getHeight(newX, newY, _player.getZ() + 50);
			// This makes offset alternate direction and increment a bit at each loop
			offset += offset * -2.01;
		}

		//Log.info(player.getX() + " " + player.getY() + " " + player.getZ() + " " + newX + " " + newY + " " + newZ + " " + offset);

		//setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(-14190, 123542, newZ, 0));
		setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(newX, newY, newZ, 0));
	}

	protected boolean interactWith(L2Character target)
	{
		if (_player.isAlly(target))
		{
			if (target.isDead())
			{
				// TODO: Resurrect (in healers override it)

				for (L2PcInstance member : _player.getParty().getPartyMembers())
				{
					if (member.getClassId() == 146 && !member.isDead() || member.isCastingNow())
					{
						return false;
					}
				}
				if (!_player.isCastingNow())
				{
					for (L2Skill skill : _player.getAllSkills())
					{
						if (skill.getSkillType() != L2SkillType.RESURRECT)
						{
							continue;
						}

						if (_player.useMagic(skill, true, false))
						{
							return true;
						}
					}

					L2ItemInstance scroll = _player.getInventory().addItem("", 737, 1, _player, _player);
					IItemHandler handler = ItemHandler.getInstance().getItemHandler(scroll.getEtcItem());
					if (handler == null)
					{
						Log.warning("No item handler registered for Herb - item ID " + scroll.getItemId() + ".");
					}
					else
					{
						_player.setTarget(target);
						handler.useItem(_player, scroll, false);
					}
				}
			}
			else
			{
				setIntention(CtrlIntention.AI_INTENTION_FOLLOW, target);
			}
			return true;
		}

		return false;
	}

	protected void think()
	{
		// If this object no longer belongs to the player, cancel the task
		if (this != _accessor.getActor().getAI())
		{
			_task.cancel(false);
			return;
		}

		// If the player left, cancel the task
		if (!_player.isOnline())
		{
			_task.cancel(false);
			return;
		}

		if (_player.getParty() != null && _player.getParty().getLeader() == _player)
		{
			_player.getParty().think();
		}

		// If dead, make a little timer
		if (_player.isDead())
		{
			_player.setReputation(0);

			// If there's some nearby ally, wait
			for (L2PcInstance player : _player.getKnownList().getKnownPlayers().values())
			{
				if (!_player.isInsideZone(L2Character.ZONE_TOWN) && _player.isAlly(player) && !player.isDead() &&
						_player.isInsideRadius(player, 2000, true, false))
				{
					return;
				}
			}

			if (_timer == 0)
			{
				_timer = System.currentTimeMillis() + 5000L;
			}
			else if (_timer < System.currentTimeMillis())
			{
				_player.doRevive();
				_player.teleToLocation(TeleportWhereType.Town);
				_timer = 0;
			}
			return;
		}

		// Check if the player was disarmed and try to equip the weapon again
		if (_player.getActiveWeaponInstance() == null && !_player.isDisarmed())
		{
			for (L2ItemInstance item : _player.getInventory().getItems())
			{
				if (item.isWeapon() && item.getItem().getCrystalType() > L2Item.CRYSTAL_D)
				{
					_player.useEquippableItem(item, false);
					break;
				}
			}

			if (_player.getActiveWeaponInstance() == null)
			{
				checkGear();
			}
		}

		// Check shots
		L2ItemInstance item = _player.getInventory().getItemByItemId(17754);
		if (item == null || item.getCount() < 1000)
		{
			_player.getInventory().addItem("", 17754, 1000, _player, _player);
		}
		item = _player.getInventory().getItemByItemId(19442);
		if (item == null || item.getCount() < 1000)
		{
			_player.getInventory().addItem("", 19442, 1000, _player, _player);
		}
		_player.checkAutoShots();

		// Check spirit ores
		item = _player.getInventory().getItemByItemId(3031);
		if (item == null || item.getCount() < 100)
		{
			_player.getInventory().addItem("", 3031, 100, _player, _player);
		}

		SkillTable.getInstance().getInfo(14779, 1).getEffects(getActor(), getActor());
		SkillTable.getInstance().getInfo(14780, 1).getEffects(getActor(), getActor());
		SkillTable.getInstance().getInfo(14781, 1).getEffects(getActor(), getActor());
		SkillTable.getInstance().getInfo(14782, 1).getEffects(getActor(), getActor());
		SkillTable.getInstance().getInfo(14783, 1).getEffects(getActor(), getActor());
		SkillTable.getInstance().getInfo(14784, 1).getEffects(getActor(), getActor());
		switch (((L2PcInstance) getActor()).getClassId())
		{
			case 139:
				SkillTable.getInstance().getInfo(14785, 1).getEffects(getActor(), getActor());
				break;
			case 140:
			case 141:
			case 142:
			case 144:
				SkillTable.getInstance().getInfo(14785, 1).getEffects(getActor(), getActor());
				break;
			case 143:
			case 145:
			case 146:
				SkillTable.getInstance().getInfo(14785, 1).getEffects(getActor(), getActor());
				break;
		}
		SkillTable.getInstance().getInfo(14788, 1).getEffects(getActor(), getActor());
		SkillTable.getInstance().getInfo(14789, 1).getEffects(getActor(), getActor());
		SkillTable.getInstance().getInfo(14790, 1).getEffects(getActor(), getActor());
		getActor().setCurrentHpMp(getActor().getMaxHp(), getActor().getMaxMp());

		// Artificially using the NPC heal whenever possible
		if (getIntention() == CtrlIntention.AI_INTENTION_IDLE && !_player.isInCombat() &&
				(_player.getPvpFlag() == 0 || _player.isInsideZone(L2Character.ZONE_PEACE)))
		{
			_player.setCurrentHp(_player.getMaxHp());
			_player.setCurrentMp(_player.getMaxMp());
			_player.setCurrentCp(_player.getMaxCp());
			_player.broadcastUserInfo();
		}

		if (!_player.isNoblesseBlessed())
		{
			_player.setTarget(_player);
			L2Skill skill = _player.getKnownSkill(1323);
			if (skill != null)
			{
				_player.useMagic(skill, true, false);
			}
		}

		boolean forceEnabled = false;
		for (L2Abnormal e : _player.getAllEffects())
		{
			if (e.getSkill().getName().endsWith(_player.getName() + " Force"))
			{
				forceEnabled = true;
				break;
			}
		}

		if (!forceEnabled)
		{
			for (L2Skill force : _player.getAllSkills())
			{
				if (force.getName().endsWith(" Force"))
				{
					_player.useMagic(force, true, false);
				}
			}
		}

		/*if ((player.getTarget() == null
				|| (player.getTarget() instanceof L2Character
				&& ((L2Character)player.getTarget()).isDead()))
				&& player.getCurrentHp() < player.getMaxHp() * 0.6)
		{
			// Unstuck
			IUserCommandHandler handler = UserCommandHandler.getInstance().getUserCommandHandler(52);
			handler.useUserCommand(52, player);
			return;
		}*/

		// Decide a target to follow or attack
		L2Character target = decideTarget();

		// If there's no target, go to the PvP zone
		if (target == null)
		{
			setIntention(CtrlIntention.AI_INTENTION_IDLE);
			if (_player.isInParty())
			{
				for (L2PcInstance member : _player.getParty().getPartyMembers())
				{
					if (member != _player && (_player.isInsideRadius(member, 30, false, false) ||
							!_player.isInsideRadius(member, 200, false, false) &&
									_player.isInsideRadius(member, 3000, false, false)))
					{
						setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,
								new L2CharPosition(member.getX() + Rnd.get(100) - 50, member.getY() + Rnd.get(100) - 50,
										member.getZ(), 0));
						return;
					}
				}
			}
			else
			{
				L2PcInstance mostPvP = L2World.getInstance().getMostPvP(_player.isInParty(), true);

				if (mostPvP != null && _player.getPvpFlag() == 0)
				{
					_player.teleToLocation(mostPvP.getX(), mostPvP.getY(), mostPvP.getZ());
				}
				return;
			}
		}

		/*if (_player.getDistanceSq(target) > 2000 * 2000)
			travelTo(target);
		else*/
		interactWith(target);
	}

	@Override
	protected void onEvtAttacked(L2Character attacker)
	{
		if (attacker instanceof L2Attackable && !attacker.isCoreAIDisabled())
		{
			clientStartAutoAttack();
		}
	}

	@Override
	public void run()
	{
		think();
	}

	// -----------------------------------------------------

	/**
	 * Finds the first occurrence of the abnormalType specified, if it's present.
	 *
	 * @param player       The L2PcInstance to check
	 * @param abnormalType The abnormalType to check
	 * @return Returns true if exists at last one occurrence of given abnormalType
	 */
	protected boolean hasAbnormalType(L2PcInstance player, String abnormalType)
	{
		if (player != null && !abnormalType.equals("none"))
		{
			for (L2Abnormal e : player.getAllEffects())
			{
				for (String stackType : e.getStackType())
				{
					if (stackType.equals(abnormalType))
					{
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Finds the first occurrence of Effect if it's present, searching by it's Skill ID.
	 *
	 * @param player  The L2PcInstance to check
	 * @param skillId The ID of the skill to check
	 * @return Returns true if exists at last one occurrence of given Effect
	 */
	protected boolean hasSkillEffects(L2PcInstance player, int skillId)
	{
		if (player != null)
		{
			for (L2Abnormal e : player.getAllEffects())
			{
				if (e.getSkill().getId() == skillId)
				{
					return true;
				}
			}
		}

		return false;
	}
}
