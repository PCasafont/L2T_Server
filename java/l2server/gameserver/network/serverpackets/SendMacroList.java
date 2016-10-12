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

package l2server.gameserver.network.serverpackets;

import l2server.gameserver.model.L2Macro;

/**
 * packet type id 0xe7
 * <p>
 * sample
 * <p>
 * e7 d // unknown change of Macro edit,add,delete c // unknown c //count of
 * Macros c // unknown
 * <p>
 * d // id S // macro name S // desc S // acronym c // icon c // count
 * <p>
 * c // entry c // type d // skill id c // shortcut id S // command name
 * <p>
 * format: cdhcdSSScc (ccdcS)
 */
public class SendMacroList extends L2GameServerPacket
{

	private final int _rev;

	private final int _count;

	private final L2Macro _macro;

	public SendMacroList(int rev, int count, L2Macro macro)
	{
		_rev = rev;
		_count = count;
		_macro = macro;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_rev); // 0 - remove / 1 - add / 2 - edit
		writeD(_macro != null ? _macro.id : 0); // Macro ID
		writeC(_count); // count of Macros
		writeC(_rev == 0 ? 0 : 1); // unknown

		if (_macro != null && _rev != 0)
		{
			writeD(_macro.id); // Macro ID
			writeS(_macro.name); // Macro Name
			writeS(_macro.descr); // Desc
			writeS(_macro.acronym); // acronym
			writeH(104); // ???
			writeH(_macro.icon); // icon

			writeC(_macro.commands.length); // count

			for (int i = 0; i < _macro.commands.length; i++)
			{
				L2Macro.L2MacroCmd cmd = _macro.commands[i];
				writeC(i + 1); // i of count
				writeC(cmd.type); // type 1 = skill, 3 = action, 4 = shortcut
				writeD(cmd.d1); // skill id
				writeC(cmd.d2); // shortcut id
				writeS(cmd.cmd); // command name
			}
		}

		/*
		writeD(1); //unknown change of Macro edit,add,delete
		writeC(0); //unknown
		writeC(1); //count of Macros
		writeC(1); //unknown

		writeD(1430); //Macro ID
		writeS("Admin"); //Macro Name
		writeS("Admin Command"); //Desc
		writeS("ADM"); //acronym
		writeC(0); //icon
		writeC(2); //count

		writeC(1); //i of count
		writeC(3); //type 1 = skill, 3 = action, 4 = shortcut
		writeD(0); // skill id
		writeC(0); // shortcut id
		writeS("/loc");	// command name

		writeC(2);		//i of count
		writeC(3);		//type  1 = skill, 3 = action, 4 = shortcut
		writeD(0);		// skill id
		writeC(0);		// shortcut id
		writeS("//admin");	// command name
		 */
	}
}
