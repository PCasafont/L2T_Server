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

package handlers.skillhandlers;

import l2server.Config;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.handler.ISkillHandler;
import l2server.gameserver.model.L2Manor;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.MonsterInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.PlaySound;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.skills.SkillType;
import l2server.util.Rnd;

import java.util.logging.Logger;

/**
 * @author l3x
 */
public class Sow implements ISkillHandler {
	private static Logger log = Logger.getLogger(Sow.class.getName());

	private static final SkillType[] SKILL_IDS = {SkillType.SOW};

	/**
	 * @see l2server.gameserver.handler.ISkillHandler#useSkill(Creature, Skill, WorldObject[])
	 */
	@Override
	public void useSkill(Creature activeChar, Skill skill, WorldObject[] targets) {
		if (!(activeChar instanceof Player)) {
			return;
		}

		final WorldObject[] targetList = skill.getTargetList(activeChar);
		if (targetList == null || targetList.length == 0) {
			return;
		}

		if (Config.DEBUG) {
			log.info("Casting sow");
		}

		MonsterInstance target;

		for (WorldObject tgt : targetList) {
			if (!(tgt instanceof MonsterInstance)) {
				continue;
			}

			target = (MonsterInstance) tgt;
			if (target.isDead() || target.isSeeded() || target.getSeederId() != activeChar.getObjectId()) {
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				continue;
			}

			final int seedId = target.getSeedType();
			if (seedId == 0) {
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				continue;
			}

			//Consuming used seed
			if (!activeChar.destroyItemByItemId("Consume", seedId, 1, target, false)) {
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			SystemMessage sm;
			if (calcSuccess(activeChar, target, seedId)) {
				activeChar.sendPacket(new PlaySound("Itemsound.quest_itemget"));
				target.setSeeded((Player) activeChar);
				sm = SystemMessage.getSystemMessage(SystemMessageId.THE_SEED_WAS_SUCCESSFULLY_SOWN);
			} else {
				sm = SystemMessage.getSystemMessage(SystemMessageId.THE_SEED_WAS_NOT_SOWN);
			}

			if (activeChar.getParty() == null) {
				activeChar.sendPacket(sm);
			} else {
				activeChar.getParty().broadcastToPartyMembers(sm);
			}

			//TODO: Mob should not aggro on player, this way doesn't work really nice
			target.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
	}

	private boolean calcSuccess(Creature activeChar, Creature target, int seedId) {
		// TODO: check all the chances
		int basicSuccess = L2Manor.getInstance().isAlternative(seedId) ? 20 : 90;
		final int minlevelSeed = L2Manor.getInstance().getSeedMinLevel(seedId);
		final int maxlevelSeed = L2Manor.getInstance().getSeedMaxLevel(seedId);
		final int levelPlayer = activeChar.getLevel(); // Attacker Level
		final int levelTarget = target.getLevel(); // target Level

		// seed level
		if (levelTarget < minlevelSeed) {
			basicSuccess -= 5 * (minlevelSeed - levelTarget);
		}
		if (levelTarget > maxlevelSeed) {
			basicSuccess -= 5 * (levelTarget - maxlevelSeed);
		}

		// 5% decrease in chance if player level
		// is more than +/- 5 levels to target's_ level
		int diff = levelPlayer - levelTarget;
		if (diff < 0) {
			diff = -diff;
		}
		if (diff > 5) {
			basicSuccess -= 5 * (diff - 5);
		}

		//chance can't be less than 1%
		if (basicSuccess < 1) {
			basicSuccess = 1;
		}

		return Rnd.nextInt(99) < basicSuccess;
	}

	/**
	 * @see l2server.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	@Override
	public SkillType[] getSkillIds() {
		return SKILL_IDS;
	}
}
