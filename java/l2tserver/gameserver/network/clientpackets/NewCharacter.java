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
package l2tserver.gameserver.network.clientpackets;

import l2tserver.Config;
import l2tserver.gameserver.datatables.CharTemplateTable;
import l2tserver.gameserver.network.serverpackets.NewCharacterSuccess;
import l2tserver.gameserver.templates.chars.L2PcTemplate;
import l2tserver.log.Log;

/**
 * This class ...
 *
 * @version $Revision: 1.3.4.5 $ $Date: 2005/03/27 15:29:30 $
 */
public final class NewCharacter extends L2GameClientPacket
{
	private static final String _C__0E_NEWCHARACTER = "[C] 0E NewCharacter";
	
	@Override
	protected void readImpl()
	{
		
	}
	
	@Override
	protected void runImpl()
	{
		if (Config.DEBUG) Log.fine("CreateNewChar");
		
		NewCharacterSuccess ct = new NewCharacterSuccess();
		
		L2PcTemplate template = CharTemplateTable.getInstance().getTemplate(0);
		ct.addChar(template);
		
		template = CharTemplateTable.getInstance().getTemplate(0);	// human fighter
		ct.addChar(template);
		
		template = CharTemplateTable.getInstance().getTemplate(1);	// human mage
		ct.addChar(template);
		
		template = CharTemplateTable.getInstance().getTemplate(2);	// elf fighter
		ct.addChar(template);
		
		template = CharTemplateTable.getInstance().getTemplate(3);	// elf mage
		ct.addChar(template);
		
		template = CharTemplateTable.getInstance().getTemplate(4);	// dark elf fighter
		ct.addChar(template);
		
		template = CharTemplateTable.getInstance().getTemplate(5);	// dark elf mage
		ct.addChar(template);
		
		template = CharTemplateTable.getInstance().getTemplate(6);	// orc fighter
		ct.addChar(template);
		
		template = CharTemplateTable.getInstance().getTemplate(7);	// orc mage
		ct.addChar(template);
		
		template = CharTemplateTable.getInstance().getTemplate(8);	// dwarf fighter
		ct.addChar(template);
		
		template = CharTemplateTable.getInstance().getTemplate(9); //kamael male soldier
		ct.addChar(template);
		
		template = CharTemplateTable.getInstance().getTemplate(9); // kamael female soldier
		ct.addChar(template);
		
		sendPacket(ct);
	}
	
	/* (non-Javadoc)
	 * @see l2tserver.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__0E_NEWCHARACTER;
	}
}
