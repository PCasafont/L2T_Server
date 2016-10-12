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
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.instancemanager.CastleManager;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.model.itemcontainer.Inventory;
import l2server.gameserver.model.olympiad.OlympiadManager;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.taskmanager.AttackStanceTaskManager;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.gameserver.templates.item.L2Item;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class L2NpcBufferInstance extends L2NpcInstance
{
	private static final int[] buffs =
			{14779, 14780, 14781, 14782, 14783, 14784, 14785, 14786, 14787, 14788, 14789, 14790};

	public L2NpcBufferInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);

		setInstanceType(InstanceType.L2NpcBufferInstance);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (player == null)
		{
			return;
		}

		if (player.getEvent() != null)
		{
			player.sendMessage("I can not help you if you are registered for an event");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (AttackStanceTaskManager.getInstance().getAttackStanceTask(player))
		{
			player.sendMessage("I can not help you while you are fighting");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (command.startsWith("BuffMe"))
		{
			if (player.isInCombat() || player.isDead() || player.isInOlympiadMode() || player.getPvpFlag() > 0 ||
					OlympiadManager.getInstance().isRegisteredInComp(player) || player.isPlayingEvent())
			{
				player.sendMessage("You can't use this option now!");
				return;
			}

			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			int skillId = Integer.valueOf(st.nextToken());

			if (skillId < 4)
			{
				int[] buffs = {14779, 14780, 14781, 14782, 14783, 14784, 14788, 14789, 14790, 14993, 14994, 14995};
				for (int id : buffs)
				{
					giveBuff(player, id);
				}

				if (player.getLevel() < Config.MAX_LEVEL)
				{
					Castle castle = CastleManager.getInstance().findNearestCastle(player);
					if (castle != null && castle.getTendency() == Castle.TENDENCY_LIGHT)
					{
						giveBuff(player, 19036);
					}
				}

				if (skillId == 1) //Mage
				{
					giveBuff(player, 14787);
				}
				else if (skillId == 2) //Knight
				{
					giveBuff(player, 14785);
				}
				else if (skillId == 3) //Warrior
				{
					giveBuff(player, 14786);
				}
			}
			else
			{
				giveBuff(player, skillId);
			}

			showChatWindow(player, 9);
		}
		else if (command.startsWith("Buff"))
		{
			StringTokenizer st = new StringTokenizer(command.substring(5), " ");
			int buffId = Integer.parseInt(st.nextToken());
			int chatPage = Integer.parseInt(st.nextToken());
			int buffLevel = SkillTable.getInstance().getMaxLevel(buffId);

			L2Skill skill = SkillTable.getInstance().getInfo(buffId, buffLevel);
			if (skill != null)
			{
				skill.getEffects(player, player);
				player.setCurrentMp(player.getMaxMp());
			}

			showChatWindow(player, chatPage);
		}
		else if (command.startsWith("Heal"))
		{
			if ((player.isInCombat() || player.getPvpFlag() > 0) && !player.isInsideZone(L2Character.ZONE_PEACE))
			{
				player.sendMessage("You cannot be healed while engaged in combat.");
				return;
			}

			player.setCurrentHp(player.getMaxHp());
			player.setCurrentMp(player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());

			for (L2SummonInstance summon : player.getSummons())
			{
				summon.setCurrentHp(summon.getMaxHp());
				summon.setCurrentMp(summon.getMaxMp());
				summon.setCurrentCp(summon.getMaxCp());
			}

			showChatWindow(player);
		}
		else if (command.startsWith("RemoveBuffs"))
		{
			player.stopAllEffectsExceptThoseThatLastThroughDeath();
			showChatWindow(player, 0);
		}
		else if (command.startsWith("Pet") && player.getPet() == null && player.getSummons().isEmpty())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
		}
		else if (command.startsWith("PetBuff"))
		{
			StringTokenizer st = new StringTokenizer(command.substring(8), " ");
			int buffId = Integer.parseInt(st.nextToken());
			int chatPage = Integer.parseInt(st.nextToken());
			int buffLevel = SkillTable.getInstance().getMaxLevel(buffId);

			L2Skill skill = SkillTable.getInstance().getInfo(buffId, buffLevel);
			if (skill != null)
			{
				if (player.getPet() != null)
				{
					skill.getEffects(player, player.getPet());
					player.setCurrentMp(player.getMaxMp());
				}
				for (L2SummonInstance summon : player.getSummons())
				{
					skill.getEffects(player, summon);
					player.setCurrentMp(player.getMaxMp());
				}
			}

			showChatWindow(player, chatPage);
		}
		else if (command.startsWith("PetHeal"))
		{
			if (player.getPet() != null)
			{
				player.getPet().setCurrentHp(player.getPet().getMaxHp());
				player.getPet().setCurrentMp(player.getPet().getMaxMp());
				player.getPet().setCurrentCp(player.getPet().getMaxCp());
			}
			for (L2SummonInstance summon : player.getSummons())
			{
				summon.setCurrentHp(summon.getMaxHp());
				summon.setCurrentMp(summon.getMaxMp());
				summon.setCurrentCp(summon.getMaxCp());
			}
			showChatWindow(player, 10);
		}
		else if (command.startsWith("PetRemoveBuffs"))
		{
			player.getPet().stopAllEffects();
			showChatWindow(player, 0);
		}
		else if (command.startsWith("Chat"))
		{
			showChatWindow(player, Integer.valueOf(command.substring(5)));
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	private static void giveBuff(L2PcInstance player, int skillId)
	{
		if (player == null)
		{
			return;
		}

		boolean buffSelf = true;
		boolean buffSummon = player.getTarget() != player;

		if (buffSummon)
		{
			if (player.getPet() != null)
			{
				SkillTable.getInstance().getInfo(skillId, 1).getEffects(player.getPet(), player.getPet());
				player.getPet().setCurrentHpMp(player.getPet().getMaxHp(), player.getPet().getMaxMp());
				if (player.getTarget() == player.getPet())
				{
					buffSelf = false;
				}
			}

			if (player.getSummons() != null)
			{
				for (L2SummonInstance summon : player.getSummons())
				{
					if (summon == null)
					{
						continue;
					}

					SkillTable.getInstance().getInfo(skillId, 1).getEffects(summon, summon);
					summon.setCurrentHpMp(summon.getMaxHp(), summon.getMaxMp());
					if (player.getTarget() == summon)
					{
						buffSelf = false;
					}
				}
			}
		}

		if (buffSelf)
		{
			SkillTable.getInstance().getInfo(skillId, 1).getEffects(player, player);
			player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
		}
	}

	public static void buff(L2Playable character)
	{
		int type = 2;
		if (character instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) character;
			if (!player.isMageClass())
			{
				L2ItemInstance shield = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
				if (shield != null && shield.getItem().getType1() == L2Item.TYPE1_SHIELD_ARMOR)
				{
					type = 0;
				}
				else
				{
					type = 1;
				}
			}
		}
		else
		{
			type = 1;
		}

		for (int buff : buffs)
		{
			if (buff == 14785 && type != 0 || buff == 14786 && type != 1 || buff == 14787 && type != 2)
			{
				continue;
			}

			SkillTable.getInstance().getInfo(buff, 1).getEffects(character, character);
			character.setCurrentMp(character.getMaxMp());
		}
	}

	@Override
	public void showChatWindow(L2PcInstance player, int val)
	{
		if (val >= 10 && player.getPet() == null && player.getSummons().isEmpty())
		{
			val = 0;
		}
		// Send a Server->Client NpcHtmlMessage containing the text of the L2NpcInstance to the L2PcInstance
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		String filename = getHtmlPath(getNpcId(), val);
		html.setFile(player.getHtmlPrefix(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);

		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	public static void giveBasicBuffs(L2PcInstance player)
	{
		int type = 2; //Mage

		L2ItemInstance shield = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if (!player.isMageClass() || player.getMaxSummonPoints() > 1)
		{
			if (shield != null && shield.getItem().getType1() == L2Item.TYPE1_SHIELD_ARMOR)
			{
				type = 0; //Knight
			}
			else
			{
				type = 1; //Warrior
			}
		}
		else
		{
			if (shield != null && !shield.getName().contains("Sigil"))
			{
				type = 1;
			}
		}

		List<Integer> _buffIds = new ArrayList<>();
		for (int a = 14779; a <= 14790; a++)
		{
			if (a == 14785 && type != 0)
			{
				continue;
			}

			if (a == 14786 && type != 1)
			{
				continue;
			}

			if (a == 14787 && type != 2)
			{
				continue;
			}

			_buffIds.add(a);
		}

		for (int a = 14993; a <= 14995; a++)
		{
			_buffIds.add(a);
		}

		if (player.getLevel() < Config.MAX_LEVEL)
		{
			Castle castle = CastleManager.getInstance().getCastle(player.getX(), player.getY(), player.getZ());
			if (castle != null && castle.getTendency() == Castle.TENDENCY_LIGHT)
			{
				_buffIds.add(19036);
			}
		}

		for (int a : _buffIds)
		{
			SkillTable.getInstance().getInfo(a, 1).getEffects(player, player);
			if (!player.getSummons().isEmpty())
			{
				for (L2Summon summon : player.getSummons())
				{
					if (summon == null)
					{
						continue;
					}

					SkillTable.getInstance().getInfo(a, 1).getEffects(summon, summon);
				}
			}

			if (player.getPet() != null)
			{
				SkillTable.getInstance().getInfo(a, 1).getEffects(player.getPet(), player.getPet());
			}
		}

		player.heal();
	}
}
