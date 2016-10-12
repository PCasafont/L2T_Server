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

package l2server.gameserver.instancemanager;

import l2server.L2DatabaseFactory;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Couple;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * @author evill33t
 */
public class CoupleManager
{

	private CoupleManager()
	{
		Log.info("L2JMOD: Initializing CoupleManager");
		load();
	}

	public static CoupleManager getInstance()
	{
		return SingletonHolder._instance;
	}

	// =========================================================

	// =========================================================
	// Data Field
	private ArrayList<Couple> _couples;

	// =========================================================
	// Method - Public
	public void reload()
	{
		getCouples().clear();
		load();
	}

	// =========================================================
	// Method - Private
	private void load()
	{
		Connection con = null;
		try
		{
			PreparedStatement statement;
			ResultSet rs;

			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("Select id from mods_wedding order by id");
			rs = statement.executeQuery();

			while (rs.next())
			{
				getCouples().add(new Couple(rs.getInt("id")));
			}

			statement.close();

			Log.info("Loaded: " + getCouples().size() + " couples(s)");
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Exception: CoupleManager.load(): " + e.getMessage(), e);
		}

		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	// =========================================================
	// Property - Public
	public final Couple getCouple(int coupleId)
	{
		int index = getCoupleIndex(coupleId);
		if (index >= 0)
		{
			return getCouples().get(index);
		}
		return null;
	}

	public void createCouple(L2PcInstance player1, L2PcInstance player2)
	{
		if (player1 != null && player2 != null)
		{
			if (player1.getPartnerId() == 0 && player2.getPartnerId() == 0)
			{
				int _player1id = player1.getObjectId();
				int _player2id = player2.getObjectId();

				Couple _new = new Couple(player1, player2);
				getCouples().add(_new);
				player1.setPartnerId(_player2id);
				player2.setPartnerId(_player1id);
				player1.setCoupleId(_new.getId());
				player2.setCoupleId(_new.getId());
			}
		}
	}

	public void deleteCouple(int coupleId)
	{
		int index = getCoupleIndex(coupleId);
		Couple couple = getCouples().get(index);
		if (couple != null)
		{
			L2PcInstance player1 = L2World.getInstance().getPlayer(couple.getPlayer1Id());
			L2PcInstance player2 = L2World.getInstance().getPlayer(couple.getPlayer2Id());
			if (player1 != null)
			{
				player1.setPartnerId(0);
				player1.setMarried(false);
				player1.setCoupleId(0);
			}
			if (player2 != null)
			{
				player2.setPartnerId(0);
				player2.setMarried(false);
				player2.setCoupleId(0);
			}
			couple.divorce();
			getCouples().remove(index);
		}
	}

	public final int getCoupleIndex(int coupleId)
	{
		int i = 0;
		for (Couple temp : getCouples())
		{
			if (temp != null && temp.getId() == coupleId)
			{
				return i;
			}
			i++;
		}
		return -1;
	}

	public final ArrayList<Couple> getCouples()
	{
		if (_couples == null)
		{
			_couples = new ArrayList<>();
		}
		return _couples;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final CoupleManager _instance = new CoupleManager();
	}
}
