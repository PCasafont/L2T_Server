/*

 */

package handlers.itemhandlers;

import l2server.gameserver.handler.IItemHandler;
import l2server.gameserver.instancemanager.HandysBlockCheckerManager;
import l2server.gameserver.instancemanager.HandysBlockCheckerManager.ArenaParticipantsHolder;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.instance.BlockInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;

public class EventItem implements IItemHandler {

	/* (non-Javadoc)
	 * @see l2server.gameserver.handler.IItemHandler#useItem(l2server.gameserver.model.actor.Playable, l2server.gameserver.model.Item, boolean)
	 */
	@Override
	public void useItem(Playable playable, Item item, boolean forceUse) {
		if (!(playable instanceof Player)) {
			return;
		}

		final Player activeChar = (Player) playable;

		final int itemId = item.getItemId();
		switch (itemId) {
			case 13787: // Handy's Block Checker Bond
				useBlockCheckerItem(activeChar, item);
				break;
			case 13788: // Handy's Block Checker Land Mine
				useBlockCheckerItem(activeChar, item);
				break;
			default:
				log.warn("EventItemHandler: Item with id: " + itemId + " is not handled");
		}
	}

	private final void useBlockCheckerItem(final Player castor, Item item) {
		final int blockCheckerArena = castor.getBlockCheckerArena();
		if (blockCheckerArena == -1) {
			SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			msg.addItemName(item);
			castor.sendPacket(msg);
			return;
		}

		final Skill sk = item.getEtcItem().getSkills()[0].getSkill();
		if (sk == null) {
			return;
		}

		if (!castor.destroyItem("Consume", item, 1, castor, true)) {
			return;
		}

		final BlockInstance block = (BlockInstance) castor.getTarget();

		final ArenaParticipantsHolder holder = HandysBlockCheckerManager.getInstance().getHolder(blockCheckerArena);
		if (holder != null) {
			final int team = holder.getPlayerTeam(castor);
			for (final Player pc : block.getKnownList().getKnownPlayersInRadius(sk.getEffectRange())) {
				final int enemyTeam = holder.getPlayerTeam(pc);
				if (enemyTeam != -1 && enemyTeam != team) {
					sk.getEffects(castor, pc);
				}
			}
		} else {
			log.warn("Char: " + castor.getName() + "[" + castor.getObjectId() + "] has unknown block checker arena");
		}
	}
}
