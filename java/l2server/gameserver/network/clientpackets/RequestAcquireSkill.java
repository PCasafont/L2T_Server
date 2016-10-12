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
import l2server.gameserver.datatables.*;
import l2server.gameserver.datatables.SubPledgeSkillTree.SubUnitSkill;
import l2server.gameserver.model.*;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.*;
import l2server.gameserver.model.base.PlayerClass;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.util.Util;
import l2server.log.Log;

import java.util.List;

/**
 * This class ...
 *
 * @version $Revision: 1.7.2.1.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestAcquireSkill extends L2GameClientPacket
{

	private int _id;
	private int _level;
	@SuppressWarnings("unused")
	private int _enchantHash;
	private int _skillType;
	private int subType;

	@Override
	protected void readImpl()
	{
		_id = readD();
		_level = readH();
		_enchantHash = readH();
		_skillType = readD();
		if (_skillType == 3)
		{
			subType = readD();
		}
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}

		//LasTravel custom restriction
		if (player.isInOlympiadMode())
		{
			player.sendMessage("You can't learn skills right now!");
			return;
		}

		if (_level < 1 || _level > 100 || _id < 1 || _id > 32000)
		{
			Util.handleIllegalPlayerAction(player, "Wrong Packet Data in Aquired Skill", Config.DEFAULT_PUNISH);
			Log.warning(
					"Recived Wrong Packet Data in Aquired Skill - id: " + _id + " level: " + _level + " for " + player);
			return;
		}

		final L2Npc trainer = player.getLastFolkNPC();

		/* If current skill lvl + 1 is not equal to the skill lvl you wanna learn (eg: You have Aggression lvl 3 and the packet sends info that
		 * you want to learn Aggression lvl 5, thus skipping lvl 4.) or the packet sends the same level or lower (eg: Aggression lvl 3 and the
		 * packet sends info that you want to learn Aggression level 3).
		 */
		if (Math.max(player.getSkillLevel(_id), 0) + 1 != _level && !(_skillType == 3 || _skillType == 4))
		{
			return;
		}

		int knownEnchant = 0;
		L2Skill knownSkill = player.getKnownSkill(_id);
		if (knownSkill != null)
		{
			knownEnchant = knownSkill.getEnchantHash();
		}
		final L2Skill skill = SkillTable.getInstance().getInfo(_id, _level, knownEnchant);

		// Finding out if it's a clan skill
		if (player.getClan() != null)
		{
			L2PledgeSkillLearn[] tempSkills = PledgeSkillTree.getInstance().getAvailableSkills(player);
			for (L2PledgeSkillLearn s : tempSkills)
			{
				L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
				if (sk == skill)
				{
					_skillType = 2;
					break;
				}
			}
		}

		int counts = 0;
		int requiredSp = 10000000;

		switch (_skillType)
		{
			case 0:
			{
				if (trainer instanceof L2TransformManagerInstance) // transform skills
				{
					int costId = 0;

					// Skill Learn bug Fix
					L2TransformSkillLearn[] skillst = SkillTreeTable.getInstance().getAvailableTransformSkills(player);
					for (L2TransformSkillLearn s : skillst)
					{
						L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
						if (sk == null || sk != skill)
						{
							continue;
						}

						counts++;
						costId = s.getItemId();
						requiredSp = s.getSpCost();
					}

					if (counts == 0)
					{
						player.sendMessage("You are trying to learn skill that you can't..");
						Util.handleIllegalPlayerAction(player,
								"Player " + player.getName() + " tried to learn skill that he can't!!!",
								Config.DEFAULT_PUNISH);
						return;
					}

					if (player.getSp() >= requiredSp)
					{
						if (!player.destroyItemByItemId("Consume", costId, 1, trainer, false))
						{
							// Haven't spellbook
							player.sendPacket(
									SystemMessage.getSystemMessage(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL));
							showSkillList(trainer, player);
							return;
						}

						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
						sm.addItemName(costId);
						player.sendPacket(sm);
					}
					else
					{
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_SP_TO_LEARN_SKILL));
						showSkillList(trainer, player);
						return;
					}
					break;
				}

				// normal skills
				L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableClassSkills(player);
				for (L2SkillLearn s : skills)
				{
					if (!s.isLearnedFromPanel() || s.getLevel() > player.getLevel())
					{
						continue;
					}

					L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
					if (sk == null || sk.getId() != skill.getId() || sk.getLevel() != skill.getLevel())
					{
						continue;
					}

					counts++;
					requiredSp = SkillTreeTable.getInstance().getSkillCost(player, skill);
				}

				if (counts == 0 && !Config.ALT_GAME_SKILL_LEARN)
				{
					player.sendMessage("You are trying to learn skill which you can't..");
					Util.handleIllegalPlayerAction(player,
							"Player " + player.getName() + " tried to learn skill that he can't!!!",
							Config.DEFAULT_PUNISH);
					return;
				}

				if (player.getSp() >= requiredSp)
				{
					int spbId = -1;

					// divine inspiration require book for each level
					if (Config.DIVINE_SP_BOOK_NEEDED && skill.getId() == L2Skill.SKILL_DIVINE_INSPIRATION)
					{
						switch (_level)
						{
							case 1:
								spbId = 8618; // Ancient Book - Divine Inspiration (Modern Language Version)
							case 2:
								spbId = 8619; // Ancient Book - Divine Inspiration (Original Language Version)
							case 3:
								spbId = 8620; // Ancient Book - Divine Inspiration (Manuscript)
							case 4:
								spbId = 8621; // Ancient Book - Divine Inspiration (Original Version)
							default:
								spbId = -1;
						}
					}

					// spellbook required
					if (spbId > -1)
					{
						if (!player.destroyItemByItemId("Consume", spbId, 1, trainer, false))
						{
							// Haven't spellbook
							player.sendPacket(
									SystemMessage.getSystemMessage(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL));
							if (trainer != null)
							{
								showSkillList(trainer, player);
							}
							return;
						}

						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
						sm.addItemName(spbId);
						player.sendPacket(sm);
					}
				}
				else
				{
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_SP_TO_LEARN_SKILL));
					if (trainer != null)
					{
						showSkillList(trainer, player);
					}
					return;
				}
				break;
			}
			case 1:
			{
				int costId = 0;
				int costCount = 0;

				// Skill Learn bug Fix
				L2SkillLearn[] skillsc = SkillTreeTable.getInstance().getAvailableSkills(player);
				for (L2SkillLearn s : skillsc)
				{
					if (!s.isLearnedFromPanel())
					{
						continue;
					}

					L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
					if (sk == null || sk != skill)
					{
						continue;
					}

					counts++;
					costId = 0;
					costCount = 0;
					requiredSp = s.getSpCost();
				}

				if (counts == 0)
				{
					//player.sendMessage("You are trying to learn skill that you can't..");
					Util.handleIllegalPlayerAction(player,
							"Player " + player.getName() + " tried to learn skill that he can't!!!",
							Config.DEFAULT_PUNISH);
					return;
				}

				if (player.getSp() >= requiredSp)
				{
					if (!player.destroyItemByItemId("Consume", costId, costCount, trainer, false))
					{
						// Haven't spellbook
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL));
						showSkillList(trainer, player);
						return;
					}

					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
					sm.addItemName(costId);
					sm.addItemNumber(costCount);
					player.sendPacket(sm);
				}
				else
				{
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_SP_TO_LEARN_SKILL));
					showSkillList(trainer, player);
					return;
				}
				break;
			}
			case 2:
			{
				int repCost = 100000000;

				// Skill Learn bug Fix
				L2PledgeSkillLearn[] skills = PledgeSkillTree.getInstance().getAvailableSkills(player);
				for (L2PledgeSkillLearn s : skills)
				{
					L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
					if (sk == null || sk != skill)
					{
						continue;
					}

					counts++;
					repCost = s.getRepCost();
					break;
				}

				if (counts == 0)
				{
					//player.sendMessage("You are trying to learn skill that you can't..");
					Util.handleIllegalPlayerAction(player,
							"Player " + player + " tried to learn clan skill that he can't!!!", Config.DEFAULT_PUNISH);
					return;
				}

				if (player.getClan().getReputationScore() < repCost)
				{
					player.sendPacket(
							SystemMessage.getSystemMessage(SystemMessageId.ACQUIRE_SKILL_FAILED_BAD_CLAN_REP_SCORE));
					L2VillageMasterInstance.showPledgeSkillList(player);
					return;
				}

				player.getClan().takeReputationScore(repCost, true);
				player.getClan().addNewSkill(skill);

				if (Config.DEBUG)
				{
					Log.fine("Learned pledge skill " + _id + " for " + requiredSp + " SP.");
				}

				SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
				cr.addNumber(repCost);
				player.sendPacket(cr);

				player.sendPacket(new AcquireSkillDone());

				player.getClan().broadcastToOnlineMembers(new PledgeSkillList(player.getClan()));

				L2VillageMasterInstance.showPledgeSkillList(player); //Maybe we should add a check here...
				return;
			}
			case 3:
			{
				if (!player.isClanLeader())
				{
					return;
				}
				if (player.getClan().getHasFort() == 0 && player.getClan().getHasCastle() == 0)
				{
					return;
				}
				if (trainer instanceof L2SquadTrainer)
				{
					int id = 0;
					int count = 0;
					int rep = 100000000;
					boolean found = false;
					for (SubUnitSkill sus : SubPledgeSkillTree.getInstance().getAvailableSkills(player.getClan()))
					{
						if (sus.getSkill() == skill)
						{
							id = sus.getItemId();
							count = sus.getCount();
							rep = sus.getReputation();
							found = true;
							break;
						}
					}

					// skill not available for clan !?
					if (!found)
					{
						return;
					}

					// check if subunit can accept skill
					if (!player.getClan().isLearnableSubSkill(skill, subType))
					{
						return;
					}

					if (player.getClan().getReputationScore() < rep)
					{
						player.sendPacket(SystemMessage
								.getSystemMessage(SystemMessageId.ACQUIRE_SKILL_FAILED_BAD_CLAN_REP_SCORE));
						return;
					}

					if (!player.destroyItemByItemId("SubSkills", id, count, trainer, false))
					{
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL));
						return;
					}
					else
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
						sm.addItemName(id);
						sm.addItemNumber(count);
						player.sendPacket(sm);
					}

					player.getClan().takeReputationScore(rep, true);
					SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
					cr.addNumber(rep);
					player.sendPacket(cr);

					if (subType > -2)
					{
						player.getClan().addNewSkill(skill, subType);
					}

					player.getClan().broadcastToOnlineMembers(new PledgeSkillList(player.getClan()));

					((L2SquadTrainer) trainer).showSubUnitSkillList(player);
				}
				break;
			}
			case 4:
			case 5:
			{
				int reqItem;
				int cost;
				if (_skillType == 4)
				{
					reqItem = CertificateSkillTable.SUBCLASS_CERTIFICATE;
					cost = CertificateSkillTable.getInstance().getSubClassSkillCost(skill.getId());
				}
				else
				{
					reqItem = CertificateSkillTable.DUALCLASS_CERTIFICATE;
					cost = CertificateSkillTable.getInstance().getDualClassSkillCost(skill.getId());
				}

				if (cost <= 0)
				{
					return;
				}

				if (player.getClassIndex() != 0)
				{
					player.sendMessage("You must be on your main class to learn this skill.");
					return;
				}

				if (!player.destroyItemByItemId("CertSkill", reqItem, cost, trainer, false))
				{
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL));
					//showSkillList(trainer, player);
					return;
				}

				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
				sm.addItemName(reqItem);
				sm.addItemNumber(cost);
				player.sendPacket(sm);

				sm = SystemMessage.getSystemMessage(SystemMessageId.LEARNED_SKILL_S1);
				sm.addSkillName(skill);
				player.sendPacket(sm);

				player.sendPacket(new AcquireSkillDone());

				player.addSkill(skill, true);
				player.addCertificationSkill(skill);
				player.sendSkillList();

				return;
			}
			case 6:
			{
				int costId = 0;
				int costCount = 0;

				// Skill Learn bug Fix
				L2SkillLearn[] skillsc = SkillTreeTable.getInstance().getAvailableSpecialSkills(player);
				for (L2SkillLearn s : skillsc)
				{
					L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
					if (sk == null || sk != skill)
					{
						continue;
					}

					counts++;
					costId = 0;
					costCount = 0;
					requiredSp = s.getSpCost();
				}

				if (counts == 0)
				{
					player.sendMessage("You are trying to learn skill that you can't..");
					Util.handleIllegalPlayerAction(player,
							"Player " + player.getName() + " tried to learn skill that he can't!!!",
							Config.DEFAULT_PUNISH);
					return;
				}

				if (player.getSp() >= requiredSp)
				{
					if (!player.destroyItemByItemId("Consume", costId, costCount, trainer, false))
					{
						// Haven't spellbook
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL));
						showSkillList(trainer, player);
						return;
					}

					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
					sm.addItemName(costId);
					sm.addItemNumber(costCount);
					player.sendPacket(sm);
				}
				else
				{
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_SP_TO_LEARN_SKILL));
					showSkillList(trainer, player);
					return;
				}
				break;
			}
			default:
			{
				Log.warning("Recived Wrong Packet Data in Aquired Skill - unk1:" + _skillType);
				return;
			}
		}

		PlayerClass pClass = PlayerClassTable.getInstance().getClassById(player.getActiveClass());
		L2SkillLearn learned = null;
		if (pClass != null)
		{
			learned = pClass.getSkills().get(SkillTable.getSkillHashCode(_id, _level));
		}

		if (learned != null)
		{
			List<Integer> reqSkillIds = learned.getCostSkills();
			if (reqSkillIds != null && !reqSkillIds.isEmpty())
			{
				if (player.getEvent() != null)
				{
					player.sendMessage("You can't learn skills during an event");
					return;
				}

				for (L2Skill sk : player.getAllSkills())
				{
					for (int reqSkillId : reqSkillIds)
					{
						if (sk.getId() == reqSkillId)
						{
							player.removeSkill(sk);
						}
					}
				}
			}
		}
		else
		{
			if (!(trainer instanceof L2TransformManagerInstance))
			{
				Log.warning("RequestAcquireSkill: null skill (id: " + _id + " class: " + player.getActiveClass() + ")");
				return;
			}
		}

		if (Config.DEBUG)
		{
			Log.fine("Learned skill " + _id + " for " + requiredSp + " SP.");
		}

		if (_skillType != 3 && _skillType != 2)
		{
			player.setSp(player.getSp() - requiredSp);

			StatusUpdate su = new StatusUpdate(player);
			su.addAttribute(StatusUpdate.SP, (int) player.getSp());
			player.sendPacket(su);

			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.LEARNED_SKILL_S1);
			sm.addSkillName(skill);
			player.sendPacket(sm);

			player.sendPacket(new AcquireSkillDone());

			player.addSkill(skill, true);
			player.sendSkillList();

			updateShortCuts(player);
			if (trainer instanceof L2NpcInstance)
			{
				showSkillList(trainer, player);
			}
		}
		else if (_skillType == 4)
		{
			CertificateSkillTable.getInstance().sendSubClassSkillList(player);
		}
		else if (_skillType == 5)
		{
			CertificateSkillTable.getInstance().sendDualClassSkillList(player);
		}
	}

	private void updateShortCuts(L2PcInstance player)
	{
		// update all the shortcuts to this skill
		if (_level > 1)
		{
			L2ShortCut[] allShortCuts = player.getAllShortCuts();

			for (L2ShortCut sc : allShortCuts)
			{
				if (sc != null && sc.getId() == _id && sc.getType() == L2ShortCut.TYPE_SKILL)
				{
					L2ShortCut newsc = new L2ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), _level, 1);
					player.sendPacket(new ShortCutRegister(newsc));
					player.registerShortCut(newsc);
				}
			}
		}
	}

	private void showSkillList(L2Npc trainer, L2PcInstance player)
	{
		if (_skillType == 4)
		{
			Quest[] qlst = trainer.getTemplate().getEventQuests(Quest.QuestEventType.ON_SKILL_LEARN);
			qlst[0].notifyAcquireSkillList(trainer, player);
		}
		else if (trainer instanceof L2FishermanInstance)
		{
			L2FishermanInstance.showFishSkillList(player);
		}
		else if (trainer instanceof L2TransformManagerInstance)
		{
			L2TransformManagerInstance.showTransformSkillList(player);
		}

		// if skill is expand sendpacket :)
		if (_id >= 1368 && _id <= 1372)
		{
			player.sendPacket(new ExStorageMaxCount(player));
		}
	}
}
