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

package l2server.gameserver.datatables;

import l2server.Config;
import l2server.gameserver.InstanceListManager;
import l2server.gameserver.instancemanager.CastleManager;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.entity.Castle;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;
import lombok.Getter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author KenM
 */
public class MerchantPriceConfigTable implements InstanceListManager
{

	public static MerchantPriceConfigTable getInstance()
	{
		return SingletonHolder.instance;
	}

	private static final String MPCS_FILE = "MerchantPriceConfig.xml";

	private Map<Integer, MerchantPriceConfig> mpcs = new HashMap<>();
	private MerchantPriceConfig defaultMpc;

	private MerchantPriceConfigTable()
	{
	}

	public MerchantPriceConfig getMerchantPriceConfig(L2Character cha)
	{
		for (MerchantPriceConfig mpc : mpcs.values())
		{
			if (cha.getWorldRegion() != null && cha.getWorldRegion().containsZone(mpc.getZoneId()))
			{
				return mpc;
			}
		}
		return defaultMpc;
	}

	public MerchantPriceConfig getMerchantPriceConfig(int id)
	{
		return mpcs.get(id);
	}

	public void loadXML()
	{
		File file = new File(Config.DATAPACK_ROOT + "/" + Config.DATA_FOLDER + "" + MPCS_FILE);
		if (file.exists())
		{
			int defaultPriceConfigId;
			XmlDocument doc = new XmlDocument(file);

			XmlNode n = doc.getFirstChild();
			if (!n.hasAttribute("defaultPriceConfig"))
			{
				throw new IllegalStateException("merchantPriceConfig must define an 'defaultPriceConfig'");
			}
			else
			{
				defaultPriceConfigId = n.getInt("defaultPriceConfig");
			}
			MerchantPriceConfig mpc;
			for (XmlNode subn : n.getChildren())
			{
				mpc = parseMerchantPriceConfig(subn);
				if (mpc != null)
				{
					mpcs.put(mpc.getId(), mpc);
				}
			}

			MerchantPriceConfig defaultMpc = getMerchantPriceConfig(defaultPriceConfigId);
			if (defaultMpc == null)
			{
				throw new IllegalStateException("'defaultPriceConfig' points to an non-loaded priceConfig");
			}
			this.defaultMpc = defaultMpc;
		}
	}

	private MerchantPriceConfig parseMerchantPriceConfig(XmlNode n)
	{
		if (n.getName().equals("priceConfig"))
		{
			final int id;
			final int baseTax;
			final String name;

			if (!n.hasAttribute("id"))
			{
				throw new IllegalStateException("Must define the priceConfig 'id'");
			}
			else
			{
				id = n.getInt("id");
			}

			if (!n.hasAttribute("name"))
			{
				throw new IllegalStateException("Must define the priceConfig 'name'");
			}
			else
			{
				name = n.getString("name");
			}

			if (!n.hasAttribute("baseTax"))
			{
				throw new IllegalStateException("Must define the priceConfig 'baseTax'");
			}
			else
			{
				baseTax = n.getInt("baseTax");
			}

			int castleId = n.getInt("castleId", -1);
			int zoneId = n.getInt("zoneId", -1);

			return new MerchantPriceConfig(id, name, baseTax, castleId, zoneId);
		}
		return null;
	}

	@Override
	public void load()
	{
		try
		{
			loadXML();
			Log.info("MerchantPriceConfigTable: Loaded " + mpcs.size() + " merchant price configs.");
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Failed loading MerchantPriceConfigTable. Reason: " + e.getMessage(), e);
		}
	}

	@Override
	public void updateReferences()
	{
		for (final MerchantPriceConfig mpc : mpcs.values())
		{
			mpc.updateReferences();
		}
	}

	@Override
	public void activateInstances()
	{
	}

	/**
	 * @author KenM
	 */
	public static final class MerchantPriceConfig
	{
		@Getter private final int id;
		@Getter private final String name;
		@Getter private final int baseTax;
		private final int castleId;
		@Getter private Castle castle;
		@Getter private final int zoneId;

		public MerchantPriceConfig(final int id, final String name, final int baseTax, final int castleId, final int zoneId)
		{
			this.id = id;
			this.name = name;
			this.baseTax = baseTax;
			this.castleId = castleId;
			this.zoneId = zoneId;
		}

		/**
		 * @return Returns the baseTax / 100.0.
		 */
		public double getBaseTaxRate()
		{
			return baseTax / 100.0;
		}

		/**
		 * @return Returns the zoneId.
		 */

		public boolean hasCastle()
		{
			return getCastle() != null;
		}

		public double getCastleTaxRate()
		{
			return hasCastle() ? getCastle().getTaxRate() : 0.0;
		}

		public int getTotalTax()
		{
			return hasCastle() ? getCastle().getTaxPercent() + getBaseTax() : getBaseTax();
		}

		public double getTotalTaxRate()
		{
			return getTotalTax() / 100.0;
		}

		public void updateReferences()
		{
			castle = CastleManager.getInstance().getCastleById(castleId);
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final MerchantPriceConfigTable instance = new MerchantPriceConfigTable();
	}
}
