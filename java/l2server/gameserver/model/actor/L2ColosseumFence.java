/*
 * Copyright (C) 2004-2014 L2J Server
 *
 * This file is part of L2J Server.
 *
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.model.actor;

import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.ExColosseumFenceInfo;

import java.awt.*;

/**
 * @author FBIagent
 */
public final class L2ColosseumFence extends L2Object
{
	public enum FenceState
	{
		HIDDEN, // the fene isn't shown at all
		OPEN, // the 4 edges of the fence is shown only
		CLOSED // full fence
	}

	private final int _minZ;
	private final int _maxZ;
	private final FenceState _state;
	private final Rectangle _bounds;

	private L2ColosseumFence(int objectId, int instanceId, int x, int y, int z, int minZ, int maxZ, int width, int height, FenceState state)
	{
		super(objectId);
		setInstanceId(instanceId);
		setXYZ(x, y, z);
		_minZ = minZ;
		_maxZ = maxZ;
		_state = state;
		_bounds = new Rectangle(x - width / 2, y - height / 2, width, height);
	}

	/**
	 * Creates a new fence with auto generated object id.
	 *
	 * @param instanceId the instance
	 * @param x          the middle point x
	 * @param y          the middle point y
	 * @param z          the middle point z
	 * @param minZ       min z
	 * @param maxZ       max z
	 * @param width      the width along the x axis
	 * @param height     the height along the y axis
	 * @param state      the fence state
	 */
	public L2ColosseumFence(int instanceId, int x, int y, int z, int minZ, int maxZ, int width, int height, FenceState state)
	{
		this(IdFactory.getInstance().getNextId(), instanceId, x, y, z, minZ, maxZ, width, height, state);
	}

	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		activeChar.sendPacket(new ExColosseumFenceInfo(this));
	}

	public int getFenceX()
	{
		return _bounds.x;
	}

	public int getFenceY()
	{
		return _bounds.y;
	}

	public int getFenceMinZ()
	{
		return _minZ;
	}

	public int getFenceMaxZ()
	{
		return _maxZ;
	}

	public int getFenceWidth()
	{
		return _bounds.width;
	}

	public int getFenceHeight()
	{
		return _bounds.height;
	}

	public FenceState getFenceState()
	{
		return _state;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}

	public boolean isInsideFence(int x, int y, int z)
	{
		return x >= _bounds.x && y >= _bounds.y && z >= _minZ && z <= _maxZ && x <= _bounds.x + _bounds.width &&
				y <= _bounds.y + _bounds.width;
	}
}
