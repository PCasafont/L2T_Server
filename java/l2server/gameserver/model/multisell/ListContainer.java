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

package l2server.gameserver.model.multisell;

import java.util.ArrayList;
import java.util.List;

/**
 * @author DS
 */
public class ListContainer
{
    private int _listId;
    boolean _applyTaxes = false;
    boolean _maintainEnchantment = false;
    boolean _isChance = false;
    int _timeLimit;

    List<MultiSellEntry> _entries;

    public ListContainer()
    {
        _entries = new ArrayList<MultiSellEntry>();
    }

    /**
     * This constructor used in PreparedListContainer only
     * ArrayList not created
     */
    ListContainer(int listId)
    {
        _listId = listId;
    }

    public final List<MultiSellEntry> getEntries()
    {
        return _entries;
    }

    public final void setListId(int listId)
    {
        _listId = listId;
    }

    public final int getListId()
    {
        return _listId;
    }

    public final void setApplyTaxes(boolean applyTaxes)
    {
        _applyTaxes = applyTaxes;
    }

    final boolean getApplyTaxes()
    {
        return _applyTaxes;
    }

    public final void setMaintainEnchantment(boolean maintainEnchantment)
    {
        _maintainEnchantment = maintainEnchantment;
    }

    public final boolean getMaintainEnchantment()
    {
        return _maintainEnchantment;
    }

    public final void setIsChance(boolean isChance)
    {
        _isChance = isChance;
    }

    public final boolean isChance()
    {
        return _isChance;
    }

    public final void setTimeLimit(final int timeLimit)
    {
        _timeLimit = timeLimit;
    }

    final int getTimeLimit()
    {
        return _timeLimit;
    }
}
