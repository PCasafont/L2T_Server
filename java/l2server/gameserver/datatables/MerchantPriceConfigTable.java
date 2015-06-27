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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.parsers.ParserConfigurationException;

import l2server.Config;
import l2server.gameserver.InstanceListManager;
import l2server.gameserver.instancemanager.CastleManager;
import l2server.gameserver.model.actor.instance.L2MerchantInstance;
import l2server.gameserver.model.entity.Castle;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import org.xml.sax.SAXException;

/**
 *
 * @author  KenM
 */
public class MerchantPriceConfigTable implements InstanceListManager
{
	
	public static MerchantPriceConfigTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static final String MPCS_FILE = "MerchantPriceConfig.xml";
	
	private Map<Integer, MerchantPriceConfig> _mpcs = new HashMap<Integer, MerchantPriceConfig>();
	private MerchantPriceConfig _defaultMpc;
	
	private MerchantPriceConfigTable()
	{
	}
	
	public MerchantPriceConfig getMerchantPriceConfig(L2MerchantInstance npc)
	{
		for (MerchantPriceConfig mpc : _mpcs.values())
		{
			if (npc.getWorldRegion() != null && npc.getWorldRegion().containsZone(mpc.getZoneId()))
			{
				return mpc;
			}
		}
		return _defaultMpc;
	}
	
	public MerchantPriceConfig getMerchantPriceConfig(int id)
	{
		return _mpcs.get(id);
	}
	
	public void loadXML() throws SAXException, IOException, ParserConfigurationException
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
					_mpcs.put(mpc.getId(), mpc);
				}
			}
			
			MerchantPriceConfig defaultMpc = this.getMerchantPriceConfig(defaultPriceConfigId);
			if (defaultMpc == null)
			{
				throw new IllegalStateException("'defaultPriceConfig' points to an non-loaded priceConfig");
			}
			_defaultMpc = defaultMpc;
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
				throw new IllegalStateException("Must define the priceConfig 'id'");
			else
				id = n.getInt("id");
			
			if (!n.hasAttribute("name"))
				throw new IllegalStateException("Must define the priceConfig 'name'");
			else
				name = n.getString("name");
			
			if (!n.hasAttribute("baseTax"))
				throw new IllegalStateException("Must define the priceConfig 'baseTax'");
			else
				baseTax = n.getInt("baseTax");

			int castleId = n.getInt("castleId", -1);
			int zoneId = n.getInt("zoneId", -1);
			
			return new MerchantPriceConfig(id, name, baseTax, castleId, zoneId);
		}
		return null;
	}
	
	public void load()
	{
		try
		{
			this.loadXML();
			Log.info("MerchantPriceConfigTable: Loaded " + _mpcs.size() + " merchant price configs.");
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Failed loading MerchantPriceConfigTable. Reason: " + e.getMessage(), e);
		}
	}
	
	public void updateReferences()
	{
		for (final MerchantPriceConfig mpc : _mpcs.values())
		{
			mpc.updateReferences();
		}
	}
	
	public void activateInstances()
	{
	}
	
	/**
	 * 
	 *
	 * @author  KenM
	 */
	public static final class MerchantPriceConfig
	{
		private final int _id;
		private final String _name;
		private final int _baseTax;
		private final int _castleId;
		private Castle _castle;
		private final int _zoneId;
		
		public MerchantPriceConfig(final int id, final String name, final int baseTax, final int castleId, final int zoneId)
		{
			_id = id;
			_name = name;
			_baseTax = baseTax;
			_castleId = castleId;
			_zoneId = zoneId;
		}
		
		/**
		 * @return Returns the id.
		 */
		public int getId()
		{
			return _id;
		}
		
		/**
		 * @return Returns the name.
		 */
		public String getName()
		{
			return _name;
		}
		
		/**
		 * @return Returns the baseTax.
		 */
		public int getBaseTax()
		{
			return _baseTax;
		}
		
		/**
		 * @return Returns the baseTax / 100.0.
		 */
		public double getBaseTaxRate()
		{
			return _baseTax / 100.0;
		}
		
		/**
		 * @return Returns the castle.
		 */
		public Castle getCastle()
		{
			return _castle;
		}
		
		/**
		 * @return Returns the zoneId.
		 */
		public int getZoneId()
		{
			return _zoneId;
		}
		
		public boolean hasCastle()
		{
			return getCastle() != null;
		}
		
		public double getCastleTaxRate()
		{
			return this.hasCastle() ? this.getCastle().getTaxRate() : 0.0;
		}
		
		public int getTotalTax()
		{
			return this.hasCastle() ? (getCastle().getTaxPercent() + getBaseTax()) : getBaseTax();
		}
		
		public double getTotalTaxRate()
		{
			return this.getTotalTax() / 100.0;
		}
		
		public void updateReferences()
		{
			_castle = CastleManager.getInstance().getCastleById(_castleId);
		}
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final MerchantPriceConfigTable _instance = new MerchantPriceConfigTable();
	}
}
