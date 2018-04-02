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

package l2server.log;

import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Summon;
import l2server.gameserver.model.actor.instance.Player;
import l2server.util.StringUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class DamageFormatter extends Formatter {
	private static final String CRLF = "\r\n";
	private SimpleDateFormat dateFmt = new SimpleDateFormat("yy.MM.dd H:mm:ss");

	@Override
	public String format(LogRecord record) {
		final Object[] params = record.getParameters();
		final StringBuilder output = StringUtil.startAppend(30 + record.getMessage().length() + (params == null ? 0 : params.length * 10),
				"[",
				dateFmt.format(new Date(record.getMillis())),
				"] '---': ",
				record.getMessage());
		for (Object p : params) {
			if (p == null) {
				continue;
			}

			if (p instanceof Creature) {
				if (p instanceof Attackable && ((Attackable) p).isRaid()) {
					StringUtil.append(output, "RaidBoss ");
				}

				StringUtil.append(output, ((Creature) p).getName(), "(", String.valueOf(((Creature) p).getObjectId()), ") ");
				StringUtil.append(output, String.valueOf(((Creature) p).getLevel()), " lvl");

				if (p instanceof Summon) {
					Player owner = ((Summon) p).getOwner();
					if (owner != null) {
						StringUtil.append(output, " Owner:", owner.getName(), "(", String.valueOf(owner.getObjectId()), ")");
					}
				}
			} else if (p instanceof Skill) {
				StringUtil.append(output, " with skill ", ((Skill) p).getName(), "(", String.valueOf(((Skill) p).getId()), ")");
			} else {
				StringUtil.append(output, p.toString());
			}
		}
		output.append(CRLF);
		return output.toString();
	}
}
