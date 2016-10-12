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

import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.L2GameClient;
import l2server.util.StringUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class AccountingFormatter extends Formatter
{
	private static final String CRLF = "\r\n";
	private SimpleDateFormat dateFmt = new SimpleDateFormat("dd MMM H:mm:ss");

	@Override
	public String format(LogRecord record)
	{
		final Object[] params = record.getParameters();
		final StringBuilder output = StringUtil
				.startAppend(30 + record.getMessage().length() + (params == null ? 0 : params.length * 10), "[",
						dateFmt.format(new Date(record.getMillis())), "] ", record.getMessage());
		for (Object p : params)
		{
			if (p == null)
			{
				continue;
			}

			StringUtil.append(output, ", ");

			if (p instanceof L2GameClient)
			{
				final L2GameClient client = (L2GameClient) p;
				String address = null;
				try
				{
					if (!client.isDetached())
					{
						address = client.getConnection().getInetAddress().getHostAddress();
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				switch (client.getState())
				{
					case IN_GAME:
						if (client.getActiveChar() != null)
						{
							StringUtil.append(output, client.getActiveChar().getName());
							StringUtil.append(output, "(", String.valueOf(client.getActiveChar().getObjectId()), ") ");
						}
					case AUTHED:
						if (client.getAccountName() != null)
						{
							StringUtil.append(output, client.getAccountName(), " ");
						}
					case CONNECTED:
						if (address != null)
						{
							StringUtil.append(output, address);
						}
						break;
					default:
						throw new IllegalStateException("Missing state on switch");
				}
			}
			else if (p instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) p;
				StringUtil.append(output, player.getName());
				StringUtil.append(output, "(", String.valueOf(player.getObjectId()), ")");
			}
			else
			{
				StringUtil.append(output, p.toString());
			}
		}
		output.append(CRLF);
		return output.toString();
	}
}
