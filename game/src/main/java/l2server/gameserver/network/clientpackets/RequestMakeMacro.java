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
import l2server.gameserver.model.L2Macro;
import l2server.gameserver.model.L2Macro.L2MacroCmd;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;

public final class RequestMakeMacro extends L2GameClientPacket {
	private L2Macro macro;
	private int commandsLenght = 0;
	
	private static final int MAX_MACRO_LENGTH = 12;
	
	/**
	 * packet type id 0xc1
	 * <p>
	 * sample
	 * <p>
	 * c1
	 * d // id
	 * S // macro name
	 * S // unknown  desc
	 * S // unknown  acronym
	 * c // icon
	 * c // count
	 * <p>
	 * c // entry
	 * c // type
	 * d // skill id
	 * c // shortcut id
	 * S // command name
	 * <p>
	 * format:		cdSSScc (ccdcS)
	 */
	@Override
	protected void readImpl() {
		int id = readD();
		String name = readS();
		String desc = readS();
		String acronym = readS();
		readH(); // ???
		int icon = readH();
		int count = readC();
		if (count > MAX_MACRO_LENGTH) {
			count = MAX_MACRO_LENGTH;
		}
		
		L2MacroCmd[] commands = new L2MacroCmd[count];
		
		if (Config.DEBUG) {
			log.info("Make macro id:" + id + "\tname:" + name + "\tdesc:" + desc + "\tacronym:" + acronym + "\ticon:" + icon + "\tcount:" + count);
		}
		for (int i = 0; i < count; i++) {
			int entry = readC();
			int type = readC(); // 1 = skill, 3 = action, 4 = shortcut
			int d1 = readD(); // skill or page number for shortcuts
			int d2 = readC();
			String command = readS();
			commandsLenght += command.length();
			commands[i] = new L2MacroCmd(entry, type, d1, d2, command);
			if (Config.DEBUG) {
				log.info("entry:" + entry + "\ttype:" + type + "\td1:" + d1 + "\td2:" + d2 + "\tcommand:" + command);
			}
		}
		macro = new L2Macro(id, icon, name, desc, acronym, commands);
	}
	
	@Override
	protected void runImpl() {
		Player player = getClient().getActiveChar();
		if (player == null) {
			return;
		}
		if (commandsLenght > 255) {
			//Invalid macro. Refer to the Help file for instructions.
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INVALID_MACRO));
			return;
		}
		if (player.getMacroses().getAllMacroses().length > 48) {
			//You may create up to 48 macros.
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_MAY_CREATE_UP_TO_48_MACROS));
			return;
		}
		if (macro.name.length() == 0) {
			//Enter the name of the macro.
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ENTER_THE_MACRO_NAME));
			return;
		}
		if (macro.descr.length() > 32) {
			//Macro descriptions may contain up to 32 characters.
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.MACRO_DESCRIPTION_MAX_32_CHARS));
			return;
		}
		player.registerMacro(macro);
	}
}
