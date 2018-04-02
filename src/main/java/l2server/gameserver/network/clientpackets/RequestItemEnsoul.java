package l2server.gameserver.network.clientpackets;

import l2server.gameserver.datatables.EnsoulDataTable;
import l2server.gameserver.model.EnsoulEffect;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.SoulCrystal;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.serverpackets.ExEnsoulResult;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.network.serverpackets.ItemList;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pere
 */
public class RequestItemEnsoul extends L2GameClientPacket {
	private class CrystalEffectData {
		public int type;
		public int order;
		public int crystalId;
		public int effectId;
	}

	private int targetItem;
	private List<CrystalEffectData> effectData = new ArrayList<>();

	@Override
	public void readImpl() {
		targetItem = readD();
		int count = readC();
		for (int i = 0; i < count; i++) {
			CrystalEffectData ced = new CrystalEffectData();
			ced.type = readC(); // Crystal type
			ced.order = readC(); // Crystal order
			ced.crystalId = readD();
			ced.effectId = readD();
			effectData.add(ced);
		}
	}

	@Override
	public void runImpl() {
		Player activeChar = getClient().getActiveChar();
		if (activeChar == null) {
			return;
		}

		Item targetItem = activeChar.getInventory().getItemByObjectId(this.targetItem);
		if (targetItem == null || targetItem.isEquipped()) {
			activeChar.sendPacket(new ExShowScreenMessage("Please, unequip your " + targetItem.getName() + " before giving it any soul enhancement.",
					5000));
			activeChar.sendPacket(new ExEnsoulResult(false));
			return;
		}

		for (CrystalEffectData ced : effectData) {
			int index = ced.order - 1;
			if (index < 0 || index >= 2) {
				break;
			}

			if (ced.type == 2) {
				index = 2;
			}

			Item soulCrystalItem = activeChar.getInventory().getItemByObjectId(ced.crystalId);
			if (soulCrystalItem == null) {
				continue;
			}

			SoulCrystal soulCrystal = EnsoulDataTable.getInstance().getCrystal(soulCrystalItem.getItemId());
			if (soulCrystal == null || index < 2 && soulCrystal.isSpecial() || index == 2 && !soulCrystal.isSpecial()) {
				continue;
			}

			EnsoulEffect effect = EnsoulDataTable.getInstance().getEffect(ced.effectId);
			if (effect == null || !soulCrystal.getEffects().contains(effect)) {
				continue;
			}

			if (!activeChar.destroyItem("Ensoul", soulCrystalItem, 1, activeChar, true)) {
				continue;
			}

			targetItem.setEnsoulEffect(index, effect);
		}

		int gemstoneCount = 335;
		if (targetItem.getEnsoulEffects()[1] != null) {
			gemstoneCount = 5266;
		}

		activeChar.destroyItemByItemId("Ensoul", 19440, gemstoneCount, activeChar, true);

		activeChar.sendPacket(new ExEnsoulResult(true));
		activeChar.sendPacket(new ItemList(activeChar, false));
	}
}
