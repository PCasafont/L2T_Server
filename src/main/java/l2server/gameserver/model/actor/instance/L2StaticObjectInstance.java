/*
 * $Header: /cvsroot/l2j/L2_Gameserver/java/net/sf/l2j/gameserver/model/L2StaticObjectInstance.java,v 1.3.2.2.2.2 2005/02/04 13:05:27 maximas Exp $
 *
 *
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

package l2server.gameserver.model.actor.instance;

import l2server.gameserver.ai.L2CharacterAI;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.knownlist.StaticObjectKnownList;
import l2server.gameserver.model.actor.stat.StaticObjStat;
import l2server.gameserver.model.actor.status.StaticObjStatus;
import l2server.gameserver.network.serverpackets.ShowTownMap;
import l2server.gameserver.network.serverpackets.StaticObject;
import l2server.gameserver.templates.chars.L2CharTemplate;
import l2server.gameserver.templates.item.L2Weapon;

/**
 * GODSON ROX!
 */
public class L2StaticObjectInstance extends L2Character
{
	/**
	 * The interaction distance of the L2StaticObjectInstance
	 */
	public static final int INTERACTION_DISTANCE = 150;

	private int staticObjectId;
	private int meshIndex = 0; // 0 - static objects, alternate static objects
	private int type = -1; // 0 - map signs, 1 - throne , 2 - arena signs
	private ShowTownMap map;

	@Override
	protected L2CharacterAI initAI()
	{
		return null;
	}

	/**
	 * @return Returns the StaticObjectId.
	 */
	public int getStaticObjectId()
	{
		return staticObjectId;
	}

	/**
	 */
	public L2StaticObjectInstance(int objectId, L2CharTemplate template, int staticId)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2StaticObjectInstance);
		staticObjectId = staticId;
	}

	@Override
	public final StaticObjectKnownList getKnownList()
	{
		return (StaticObjectKnownList) super.getKnownList();
	}

	@Override
	public void initKnownList()
	{
		setKnownList(new StaticObjectKnownList(this));
	}

	@Override
	public final StaticObjStat getStat()
	{
		return (StaticObjStat) super.getStat();
	}

	@Override
	public void initCharStat()
	{
		setStat(new StaticObjStat(this));
	}

	@Override
	public final StaticObjStatus getStatus()
	{
		return (StaticObjStatus) super.getStatus();
	}

	@Override
	public void initCharStatus()
	{
		setStatus(new StaticObjStatus(this));
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}

	public void setMap(String texture, int x, int y)
	{
		map = new ShowTownMap("town_map." + texture, x, y);
	}

	public ShowTownMap getMap()
	{
		return map;
	}

	@Override
	public final int getLevel()
	{
		return 1;
	}

	/**
	 * Return null.<BR><BR>
	 */
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return null;
	}

	@Override
	public L2Weapon getActiveWeaponItem()
	{
		return null;
	}

	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}

	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.model.L2Object#isAttackable()
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}

	/**
	 * Set the meshIndex of the object<BR><BR>
	 * <p>
	 * <B><U> Values </U> :</B><BR><BR>
	 * <li> default textures : 0</li>
	 * <li> alternate textures : 1 </li><BR><BR>
	 *
	 * @param meshIndex
	 */
	public void setMeshIndex(int meshIndex)
	{
		this.meshIndex = meshIndex;
		this.broadcastPacket(new StaticObject(this));
	}

	/**
	 * Return the meshIndex of the object.<BR><BR>
	 * <p>
	 * <B><U> Values </U> :</B><BR><BR>
	 * <li> default textures : 0</li>
	 * <li> alternate textures : 1 </li><BR><BR>
	 */
	public int getMeshIndex()
	{
		return meshIndex;
	}

	@Override
	public void updateAbnormalEffect()
	{
	}

	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		activeChar.sendPacket(new StaticObject(this));
	}
}
