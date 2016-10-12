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

package l2server.gameserver.templates;

/**
 * @author Pere
 */
public class SpawnData
{
	public int X;
	public int Y;
	public int Z;
	public int Heading;
	public int Respawn;
	public int RandomRespawn;
	public String DbName = null;

	public SpawnData(int x, int y, int z, int heading, int respawn, int randomRespawn)
	{
		X = x;
		Y = y;
		Z = z;
		Heading = heading;
		Respawn = respawn;
		RandomRespawn = randomRespawn;
	}
}
