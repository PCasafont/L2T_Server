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

import l2server.gameserver.instancemanager.HandysBlockCheckerManager;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.log.Log;

/**
 * @author mrTJO
 *         Format: chddd
 *         <p>
 *         d: Arena
 *         d: Answer
 */
public final class RequestExCubeGameReadyAnswer extends L2GameClientPacket
{

	int _arena;
	int _answer;

	@Override
	protected void readImpl()
	{
		// client sends -1,0,1,2 for arena parameter
		_arena = readD() + 1;
		// client sends 1 if clicked confirm on not clicked, 0 if clicked cancel
		_answer = readD();
	}

	@Override
	public void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();

		if (player == null)
		{
			return;
		}

		switch (_answer)
		{
			case 0:
				// Cancel - Answer No
				break;
			case 1:
				// OK or Time Over
				HandysBlockCheckerManager.getInstance().increaseArenaVotes(_arena);
				break;
			default:
				Log.warning("Unknown Cube Game Answer ID: " + _answer);
				break;
		}
	}
}
