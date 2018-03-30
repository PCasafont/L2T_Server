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

package l2server.gameserver.model.entity;

import l2server.L2DatabaseFactory;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.logging.Level;

/**
 * @author evill33t
 */
public class Couple {

	// =========================================================
	// Data Field
	private int Id = 0;
	private int player1Id = 0;
	private int player2Id = 0;
	private boolean maried = false;
	private Calendar affiancedDate;
	private Calendar weddingDate;

	// =========================================================
	// Constructor
	public Couple(int coupleId) {
		Id = coupleId;

		Connection con = null;
		try {
			PreparedStatement statement;
			ResultSet rs;

			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("SELECT * FROM mods_wedding WHERE id = ?");
			statement.setInt(1, Id);
			rs = statement.executeQuery();

			while (rs.next()) {
				player1Id = rs.getInt("player1Id");
				player2Id = rs.getInt("player2Id");
				maried = rs.getBoolean("married");

				affiancedDate = Calendar.getInstance();
				affiancedDate.setTimeInMillis(rs.getLong("affianceDate"));

				weddingDate = Calendar.getInstance();
				weddingDate.setTimeInMillis(rs.getLong("weddingDate"));
			}
			statement.close();
		} catch (Exception e) {
			Log.log(Level.SEVERE, "Exception: Couple.load(): " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}

	public Couple(L2PcInstance player1, L2PcInstance player2) {
		int tempPlayer1Id = player1.getObjectId();
		int tempPlayer2Id = player2.getObjectId();

		player1Id = tempPlayer1Id;
		player2Id = tempPlayer2Id;

		affiancedDate = Calendar.getInstance();
		affiancedDate.setTimeInMillis(Calendar.getInstance().getTimeInMillis());

		weddingDate = Calendar.getInstance();
		weddingDate.setTimeInMillis(Calendar.getInstance().getTimeInMillis());

		Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			Id = IdFactory.getInstance().getNextId();
			statement = con.prepareStatement(
					"INSERT INTO mods_wedding (id, player1Id, player2Id, married, affianceDate, weddingDate) VALUES (?, ?, ?, ?, ?, ?)");
			statement.setInt(1, Id);
			statement.setInt(2, player1Id);
			statement.setInt(3, player2Id);
			statement.setBoolean(4, false);
			statement.setLong(5, affiancedDate.getTimeInMillis());
			statement.setLong(6, weddingDate.getTimeInMillis());
			statement.execute();
			statement.close();
		} catch (Exception e) {
			Log.log(Level.SEVERE, "Could not create couple: " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}

	public void marry() {
		Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;

			statement = con.prepareStatement("UPDATE mods_wedding SET married = ?, weddingDate = ? WHERE id = ?");
			statement.setBoolean(1, true);
			weddingDate = Calendar.getInstance();
			statement.setLong(2, weddingDate.getTimeInMillis());
			statement.setInt(3, Id);
			statement.execute();
			statement.close();
			maried = true;
		} catch (Exception e) {
			Log.log(Level.SEVERE, "Could not marry: " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}

	public void divorce() {
		Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;

			statement = con.prepareStatement("DELETE FROM mods_wedding WHERE id=?");
			statement.setInt(1, Id);
			statement.execute();
			statement.close();
		} catch (Exception e) {
			Log.log(Level.SEVERE, "Exception: Couple.divorce(): " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}

	public final int getId() {
		return Id;
	}

	public final int getPlayer1Id() {
		return player1Id;
	}

	public final int getPlayer2Id() {
		return player2Id;
	}

	public final boolean getMaried() {
		return maried;
	}

	public final Calendar getAffiancedDate() {
		return affiancedDate;
	}

	public final Calendar getWeddingDate() {
		return weddingDate;
	}
}
