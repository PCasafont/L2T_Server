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

import gnu.trove.TIntObjectHashMap;
import l2server.gameserver.model.L2Transformation;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.log.Log;

/**
 * @author KenM
 */
public class TransformationManager
{

	public static TransformationManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private TIntObjectHashMap<L2Transformation> _transformations;

	private TransformationManager()
	{
		_transformations = new TIntObjectHashMap<>();
	}

	public void report()
	{
		Log.info("Loaded: " + _transformations.size() + " transformations.");
	}

	public boolean transformPlayer(int id, L2PcInstance player)
	{
		L2Transformation template = getTransformationById(id);
		if (template != null)
		{
			L2Transformation trans = template.createTransformationForPlayer(player);
			trans.start();
			return true;
		}
		else
		{
			return false;
		}
	}

	public L2Transformation getTransformationById(int id)
	{
		return _transformations.get(id);
	}

	public L2Transformation registerTransformation(L2Transformation transformation)
	{
		return _transformations.put(transformation.getId(), transformation);
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final TransformationManager _instance = new TransformationManager();
	}

	/**
	 * @param transformId
	 * @return
	 */
	public boolean isMountable(int transformId)
	{
		switch (transformId)
		{
			case 109: //TawnyManedLion
			case 123: //GuardianStrider
			case 20001: //JetBike
			case 20008: //TamePrincessAnt
			case 110: //SteamBeatle
			case 20004: //ShinyPlatform
			case 20010: //HalloweenWitchsBroomstick
			case 20007: //WoodHorse
			case 20009: //BlackBear
			case 129: //KnightHorse
			case 130: //WarriorHorse
			case 131: //RustySteelHorse
			case 132: //ArcherHorse
			case 133: //PhantomHorse
			case 134: //CobaltHorse
			case 135: //EnchanterHorse
			case 136: //HealerHorse
			case 137: //ClockWorkCucuru
			case 138: //Kukurin
			case 106: //LightPurpleManedHorse
			case 147: //Lyn Draco
			case 146: //Air Bike
				return true;
		}
		return false;
	}
}
