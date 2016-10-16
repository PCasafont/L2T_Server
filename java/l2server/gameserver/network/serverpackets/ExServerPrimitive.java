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

package l2server.gameserver.network.serverpackets;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A packet used to draw points and lines on client.<br/>
 * <b>Note:</b> Names in points and lines are bugged they will appear even when not looking at them.
 *
 * @author Nos
 */
public class ExServerPrimitive extends L2GameServerPacket
{
	private final String name;
	private int x;
	private int y;
	private int z;
	private final List<Point> points = new ArrayList<>();
	private final List<Line> lines = new ArrayList<>();

	/**
	 * @param name A unique name this will be used to replace lines if second packet is sent
	 * @param x    the x coordinate usually middle of drawing area
	 * @param y    the y coordinate usually middle of drawing area
	 * @param z    the z coordinate usually middle of drawing area
	 */
	public ExServerPrimitive(String name, int x, int y, int z)
	{
		this.name = name;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * @param name A unique name this will be used to replace lines if second packet is sent
	 */
	public ExServerPrimitive(String name)
	{
		this.name = name;
	}

	/**
	 * @param x the x coordinate usually middle of drawing area
	 * @param y the y coordinate usually middle of drawing area
	 * @param z the z coordinate usually middle of drawing area
	 */
	public void setXYZ(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Adds a point to be displayed on client.
	 *
	 * @param name          the name that will be displayed over the point
	 * @param color         the color
	 * @param isNameColored if {@code true} name will be colored as well.
	 * @param x             the x coordinate for this point
	 * @param y             the y coordinate for this point
	 * @param z             the z coordinate for this point
	 */
	public void addPoint(String name, int color, boolean isNameColored, int x, int y, int z)
	{
		points.add(new Point(name, color, isNameColored, x, y, z));
	}

	/**
	 * Adds a point to be displayed on client.
	 *
	 * @param color the color
	 * @param x     the x coordinate for this point
	 * @param y     the y coordinate for this point
	 * @param z     the z coordinate for this point
	 */
	public void addPoint(int color, int x, int y, int z)
	{
		addPoint("", color, false, x, y, z);
	}

	/**
	 * Adds a point to be displayed on client.
	 *
	 * @param name          the name that will be displayed over the point
	 * @param color         the color
	 * @param isNameColored if {@code true} name will be colored as well.
	 * @param x             the x coordinate for this point
	 * @param y             the y coordinate for this point
	 * @param z             the z coordinate for this point
	 */
	public void addPoint(String name, Color color, boolean isNameColored, int x, int y, int z)
	{
		addPoint(name, color.getRGB(), isNameColored, x, y, z);
	}

	/**
	 * Adds a point to be displayed on client.
	 *
	 * @param color the color
	 * @param x     the x coordinate for this point
	 * @param y     the y coordinate for this point
	 * @param z     the z coordinate for this point
	 */
	public void addPoint(Color color, int x, int y, int z)
	{
		addPoint("", color, false, x, y, z);
	}

	/**
	 * Adds a line to be displayed on client
	 *
	 * @param name          the name that will be displayed over the middle of line
	 * @param color         the color
	 * @param isNameColored if {@code true} name will be colored as well.
	 * @param x             the x coordinate for this line start point
	 * @param y             the y coordinate for this line start point
	 * @param z             the z coordinate for this line start point
	 * @param x2            the x coordinate for this line end point
	 * @param y2            the y coordinate for this line end point
	 * @param z2            the z coordinate for this line end point
	 */
	public void addLine(String name, int color, boolean isNameColored, int x, int y, int z, int x2, int y2, int z2)
	{
		lines.add(new Line(name, color, isNameColored, x, y, z, x2, y2, z2));
	}

	/**
	 * Adds a line to be displayed on client
	 *
	 * @param color the color
	 * @param x     the x coordinate for this line start point
	 * @param y     the y coordinate for this line start point
	 * @param z     the z coordinate for this line start point
	 * @param x2    the x coordinate for this line end point
	 * @param y2    the y coordinate for this line end point
	 * @param z2    the z coordinate for this line end point
	 */
	public void addLine(int color, int x, int y, int z, int x2, int y2, int z2)
	{
		addLine("", color, false, x, y, z, x2, y2, z2);
	}

	/**
	 * Adds a line to be displayed on client
	 *
	 * @param name          the name that will be displayed over the middle of line
	 * @param color         the color
	 * @param isNameColored if {@code true} name will be colored as well.
	 * @param x             the x coordinate for this line start point
	 * @param y             the y coordinate for this line start point
	 * @param z             the z coordinate for this line start point
	 * @param x2            the x coordinate for this line end point
	 * @param y2            the y coordinate for this line end point
	 * @param z2            the z coordinate for this line end point
	 */
	public void addLine(String name, Color color, boolean isNameColored, int x, int y, int z, int x2, int y2, int z2)
	{
		addLine(name, color.getRGB(), isNameColored, x, y, z, x2, y2, z2);
	}

	/**
	 * Adds a line to be displayed on client
	 *
	 * @param color the color
	 * @param x     the x coordinate for this line start point
	 * @param y     the y coordinate for this line start point
	 * @param z     the z coordinate for this line start point
	 * @param x2    the x coordinate for this line end point
	 * @param y2    the y coordinate for this line end point
	 * @param z2    the z coordinate for this line end point
	 */
	public void addLine(Color color, int x, int y, int z, int x2, int y2, int z2)
	{
		addLine("", color, false, x, y, z, x2, y2, z2);
	}

	@Override
	protected final void writeImpl()
	{
		writeS(name);
		writeD(x);
		writeD(y);
		writeD(z);
		writeD(65535); // has to do something with display range and angle
		writeD(65535); // has to do something with display range and angle

		writeD(points.size() + lines.size());

		for (Point point : points)
		{
			writeC(1); // Its the type in this case Point
			writeS(point.getName());
			int color = point.getColor();
			writeD(color >> 16 & 0xFF); // R
			writeD(color >> 8 & 0xFF); // G
			writeD(color & 0xFF); // B
			writeD(point.isNameColored() ? 1 : 0);
			writeD(point.getX());
			writeD(point.getY());
			writeD(point.getZ());
		}

		for (Line line : lines)
		{
			writeC(2); // Its the type in this case Line
			writeS(line.getName());
			int color = line.getColor();
			writeD(color >> 16 & 0xFF); // R
			writeD(color >> 8 & 0xFF); // G
			writeD(color & 0xFF); // B
			writeD(line.isNameColored() ? 1 : 0);
			writeD(line.getX());
			writeD(line.getY());
			writeD(line.getZ());
			writeD(line.getX2());
			writeD(line.getY2());
			writeD(line.getZ2());
		}
	}

	private static class Point
	{
		private final String name;
		private final int color;
		private final boolean isNameColored;
		private final int x;
		private final int y;
		private final int z;

		public Point(String name, int color, boolean isNameColored, int x, int y, int z)
		{
			this.name = name;
			this.color = color;
			this.isNameColored = isNameColored;
			this.x = x;
			this.y = y;
			this.z = z;
		}

		/**
		 * @return the name
		 */
		public String getName()
		{
			return name;
		}

		/**
		 * @return the color
		 */
		public int getColor()
		{
			return color;
		}

		/**
		 * @return the isNameColored
		 */
		public boolean isNameColored()
		{
			return isNameColored;
		}

		/**
		 * @return the x
		 */
		public int getX()
		{
			return x;
		}

		/**
		 * @return the y
		 */
		public int getY()
		{
			return y;
		}

		/**
		 * @return the z
		 */
		public int getZ()
		{
			return z;
		}
	}

	private static class Line extends Point
	{
		private final int x2;
		private final int y2;
		private final int z2;

		public Line(String name, int color, boolean isNameColored, int x, int y, int z, int x2, int y2, int z2)
		{
			super(name, color, isNameColored, x, y, z);
			this.x2 = x2;
			this.y2 = y2;
			this.z2 = z2;
		}

		/**
		 * @return the x2
		 */
		public int getX2()
		{
			return x2;
		}

		/**
		 * @return the y2
		 */
		public int getY2()
		{
			return y2;
		}

		/**
		 * @return the z2
		 */
		public int getZ2()
		{
			return z2;
		}
	}
}
