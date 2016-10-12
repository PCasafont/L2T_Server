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

package l2server.gameserver.model;

import l2server.gameserver.model.actor.instance.L2PcInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class ...
 *
 * @version $Revision: 1.2.4.2 $ $Date: 2005/03/27 15:29:33 $
 */
public final class L2SkillLearn
{
	// these two build the primary key
	private final int _id;
	private final int _level;

	private final int _spCost;
	private final int _minLevel;
	private final int _minDualLevel;

	private final boolean _learnedFromPanel;
	private final boolean _learnedByFs;
	private final boolean _isTransfer;
	private final boolean _isAutoGet;

	private final Map<Integer, Integer> _costItems = new HashMap<>();
	private final List<Integer> _costSkills = new ArrayList<>();

	private boolean _isRemember = false;

	public L2SkillLearn(int id, int lvl, int cost, int minLvl, int minDualLvl, boolean panel, boolean fs, boolean transfer, boolean autoget)
	{
		_id = id;
		_level = lvl;
		_minLevel = minLvl;
		_minDualLevel = minDualLvl;
		_spCost = cost;
		_learnedFromPanel = panel;
		_learnedByFs = fs;
		_isTransfer = transfer;
		_isAutoGet = autoget;
	}

	/**
	 * @return Returns the id.
	 */
	public int getId()
	{
		return _id;
	}

	/**
	 * @return Returns the level.
	 */
	public int getLevel()
	{
		return _level;
	}

	/**
	 * @return Returns the minLevel.
	 */
	public int getMinLevel()
	{
		return _minLevel;
	}

	public int getMinDualLevel()
	{
		return _minDualLevel;
	}

	/**
	 * @return Returns the spCost.
	 */
	public int getSpCost()
	{
		return _spCost;
	}

	/**
	 * Return true if skill can be learned by teachers
	 */
	public boolean isLearnedFromPanel()
	{
		return _learnedFromPanel;
	}

	/**
	 * Return true if skill can be learned by forgotten scroll
	 */
	public boolean isLearnedByFS()
	{
		return _learnedByFs;
	}

	public boolean isTransferSkill()
	{
		return _isTransfer;
	}

	public boolean isAutoGetSkill()
	{
		return _isAutoGet;
	}

	public void addCostItem(int itemId, int count)
	{
		_costItems.put(itemId, count);
	}

	public Map<Integer, Integer> getCostItems()
	{
		return _costItems;
	}

	public void addCostSkill(int skillId)
	{
		_costSkills.add(skillId);
	}

	public List<Integer> getCostSkills()
	{
		return _costSkills;
	}

	public Map<Integer, Integer> getCostSkills(L2PcInstance player)
	{
		Map<Integer, Integer> costSkills = new HashMap<>();
		for (int skillId : _costSkills)
		{
			int skillLevel = player.getSkillLevelHash(skillId);
			if (skillLevel > 0)
			{
				costSkills.put(skillId, skillLevel);
			}
		}
		return costSkills;
	}

	public void setIsRemember(boolean remember)
	{
		_isRemember = remember;
	}

	public boolean isRemember()
	{
		return _isRemember;
	}
}
