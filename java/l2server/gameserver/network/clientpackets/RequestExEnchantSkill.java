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

package l2server.gameserver.network.clientpackets;

import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.datatables.EnchantCostsTable;
import l2server.gameserver.datatables.EnchantCostsTable.EnchantSkillDetail;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.L2EnchantSkillLearn;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2ShortCut;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.log.Log;
import l2server.util.Rnd;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Format (ch) dd
 * c: (id) 0xD0
 * h: (subid) 0x06
 * d: skill id
 * d: skill lvl
 *
 * @author -Wooden-
 */
public final class RequestExEnchantSkill extends L2GameClientPacket
{

	private int _type;
	private int _skillId;
	private int _skillLvl;
	private int _skillEnchant;

	@Override
	protected void readImpl()
	{
		_type = readD();
		_skillId = readD();
		_skillLvl = readH();
		_skillEnchant = readH();
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		if (_skillId <= 0 || _skillLvl <= 0 || _type < 0 || _type > 4) // minimal sanity check
		{
			return;
		}

		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}

		if (player.getCurrentClass().getLevel() < 76) // requires to have 3rd class quest completed
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_SKILL_ENCHANT_IN_THIS_CLASS);
			return;
		}

		if (player.getLevel() < 76)
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_SKILL_ENCHANT_ON_THIS_LEVEL);
			return;
		}

		if (!player.isAllowedToEnchantSkills())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_SKILL_ENCHANT_ATTACKING_TRANSFORMED_BOAT);
			return;
		}

		int enchantRoute = _skillEnchant / 1000;
		int enchantLevel = _skillEnchant % 1000;
		L2Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl, enchantRoute, enchantLevel);
		if (skill == null)
		{
			return;
		}

		L2EnchantSkillLearn s = EnchantCostsTable.getInstance().getSkillEnchantmentBySkillId(_skillId);
		if (s == null)
		{
			return;
		}

		if (s.isMaxEnchant(enchantRoute, enchantLevel - 1))
		{
			return;
		}

		L2Skill currentSkill = player.getKnownSkill(_skillId);
		int currentLevel = currentSkill.getLevel();
		int currentEnchantRoute = currentSkill.getEnchantRouteId();
		int currentEnchantLevel = currentSkill.getEnchantLevel();
		// do u have this skill enchanted?
		if (_type == 3 && (currentEnchantRoute < 1 || currentEnchantLevel < 1 || currentLevel != _skillLvl ||
				currentEnchantLevel < enchantLevel - 1))
		{
			return;
		}

		EnchantSkillDetail esd = s.getEnchantSkillDetail(enchantRoute, enchantLevel);
		int costMultiplier = EnchantCostsTable.NORMAL_ENCHANT_COST_MULTIPLIER;
		int reqItemId = esd.getRange().getNormalBook();
		switch (_type)
		{
			case 1:
				costMultiplier = EnchantCostsTable.SAFE_ENCHANT_COST_MULTIPLIER;
				reqItemId = esd.getRange().getSafeBook();
				break;
			case 2:
				reqItemId = esd.getRange().getUntrainBook();
				break;
			case 3:
				reqItemId = esd.getRange().getChangeBook();
				break;
			case 4:
				costMultiplier = EnchantCostsTable.IMMORTAL_ENCHANT_COST_MULTIPLIER;
				reqItemId = esd.getRange().getImmortalBook();
				break;
		}

		int requiredSp = esd.getSpCost() * costMultiplier;
		int requireditems = esd.getAdenaCost() * costMultiplier;
		int rate = esd.getRate(player);

		if (player.getSp() >= requiredSp || _type == 2)
		{
			// only first lvl requires book
			boolean firstLevel = enchantLevel % 10 == 1; // 101, 201, 301 ...
			L2ItemInstance spb = player.getInventory().getItemByItemId(reqItemId);

			boolean useBook = _type == 1 || Config.ES_SP_BOOK_NEEDED && (_type != 0 || firstLevel);
			if (useBook && spb == null)// Haven't spellbook
			{
				player.sendPacket(SystemMessage
						.getSystemMessage(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL));
				return;
			}

			if (player.getInventory().getAdena() < requireditems)
			{
				player.sendPacket(SystemMessage
						.getSystemMessage(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL));
				return;
			}

			boolean check = true;
			if (_type != 2 && requiredSp > 0)
			{
				check &= player.getStat().removeExpAndSp(0, requiredSp, false);
			}

			if (useBook)
			{
				check &= player.destroyItem("Consume", spb.getObjectId(), 1, player, true);
			}

			check &= player.destroyItemByItemId("Consume", 57, requireditems, player, true);

			if (!check)
			{
				player.sendPacket(SystemMessage
						.getSystemMessage(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL));
				return;
			}

			if (_type == 3)
			{
				int levelPenalty = Rnd.get(Math.min(4, currentEnchantLevel));
				enchantLevel -= levelPenalty;
				if (enchantLevel == 0)
				{
					enchantRoute = 0;
				}

				skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl, enchantRoute, enchantLevel);

				if (skill != null)
				{
					logSkillEnchant(player, skill, spb, rate);
					player.addSkill(skill, true);
					player.sendPacket(ExEnchantSkillResult.valueOf(true));
					if (levelPenalty == 0)
					{
						SystemMessage sm = SystemMessage
								.getSystemMessage(SystemMessageId.SKILL_ENCHANT_CHANGE_SUCCESSFUL_S1_LEVEL_WILL_REMAIN);
						sm.addSkillName(_skillId);
						player.sendPacket(sm);
					}
					else
					{
						SystemMessage sm = SystemMessage.getSystemMessage(
								SystemMessageId.SKILL_ENCHANT_CHANGE_SUCCESSFUL_S1_LEVEL_WAS_DECREASED_BY_S2);
						sm.addSkillName(_skillId);
						sm.addNumber(levelPenalty);
						player.sendPacket(sm);
					}
				}
				else
				{
					SystemMessage sm = SystemMessage
							.getSystemMessage(SystemMessageId.YOU_DONT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL);
					player.sendPacket(sm);
				}
			}
			if (_type == 2)
			{
				player.getStat().addSp((int) (requiredSp * 0.8));
				logSkillEnchant(player, skill, spb, rate);

				player.addSkill(skill, true);
				player.sendPacket(ExEnchantSkillResult.valueOf(true));

				if (Config.DEBUG)
				{
					Log.fine("Learned skill ID: " + _skillId + " Level: " + _skillLvl + " for " + requiredSp + " SP, " +
							requireditems + " Adena.");
				}

				if (enchantRoute > 0)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(
							SystemMessageId.UNTRAIN_SUCCESSFUL_SKILL_S1_ENCHANT_LEVEL_DECREASED_BY_ONE);
					sm.addSkillName(_skillId);
					player.sendPacket(sm);
				}
				else
				{
					SystemMessage sm = SystemMessage
							.getSystemMessage(SystemMessageId.UNTRAIN_SUCCESSFUL_SKILL_S1_ENCHANT_LEVEL_RESETED);
					sm.addSkillName(_skillId);
					player.sendPacket(sm);
				}
			}
			else if (_type == 4 || Rnd.get(100) <= rate)
			{
				logSkillEnchant(player, skill, spb, rate);

				player.addSkill(skill, true);

				if (Config.DEBUG)
				{
					Log.fine("Learned skill ID: " + _skillId + " Level: " + _skillLvl + " for " + requiredSp + " SP, " +
							requireditems + " Adena.");
				}

				player.sendPacket(ExEnchantSkillResult.valueOf(true));

				SystemMessage sm =
						SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_SUCCEEDED_IN_ENCHANTING_THE_SKILL_S1);
				sm.addSkillName(_skillId);
				player.sendPacket(sm);
			}
			else if (_type == 0)
			{
				skill = SkillTable.getInstance()
						.getInfo(_skillId, _skillLvl, esd.getRange().getStartLevel() > 0 ? enchantRoute : 0,
								esd.getRange().getStartLevel());
				player.addSkill(skill, true);
				player.sendPacket(
						SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_FAILED_TO_ENCHANT_THE_SKILL_S1));

				player.sendPacket(ExEnchantSkillResult.valueOf(false));
			}
			else
			{
				SystemMessage sm =
						SystemMessage.getSystemMessage(SystemMessageId.SKILL_ENCHANT_FAILED_S1_LEVEL_WILL_REMAIN);
				sm.addSkillName(_skillId);
				player.sendPacket(sm);
				player.sendPacket(ExEnchantSkillResult.valueOf(false));
			}

			currentSkill = player.getKnownSkill(_skillId);
			player.sendPacket(new UserInfo(player));
			player.sendSkillList();
			player.sendPacket(
					new ExEnchantSkillInfo(_skillId, currentSkill.getLevel(), currentSkill.getEnchantRouteId(),
							currentSkill.getEnchantLevel()));
			player.sendPacket(new ExEnchantSkillInfoDetail(_type, _skillId, currentSkill.getLevel(),
					currentSkill.getEnchantRouteId(), currentSkill.getEnchantLevel(), player));

			updateSkillShortcuts(player);
		}
		else
		{
			SystemMessage sm =
					SystemMessage.getSystemMessage(SystemMessageId.YOU_DONT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL);
			player.sendPacket(sm);
		}
	}

	private void updateSkillShortcuts(L2PcInstance player)
	{
		// update all the shortcuts to this skill
		L2ShortCut[] allShortCuts = player.getAllShortCuts();

		for (L2ShortCut sc : allShortCuts)
		{
			if (sc != null && sc.getId() == _skillId && sc.getType() == L2ShortCut.TYPE_SKILL)
			{
				L2ShortCut newsc = new L2ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(),
						player.getSkillLevelHash(_skillId), 1);
				player.sendPacket(new ShortCutRegister(newsc));
				player.registerShortCut(newsc);
			}
		}
	}

	private void logSkillEnchant(L2PcInstance player, L2Skill skill, L2ItemInstance spb, int rate)
	{
		if (Config.LOG_SKILL_ENCHANTS)
		{
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(
						"INSERT INTO log_enchant_skills(player_id, skill_id, skill_level, spb_id, rate, time) VALUES(?,?,?,?,?,?)");
				statement.setInt(1, player.getObjectId());
				statement.setInt(2, skill.getId());
				statement.setInt(3, skill.getLevelHash());
				statement.setInt(4, spb != null ? spb.getItemId() : 0);
				statement.setInt(5, rate);
				statement.setLong(6, System.currentTimeMillis());
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}
	}
}
