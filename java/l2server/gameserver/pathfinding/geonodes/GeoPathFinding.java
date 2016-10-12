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

package l2server.gameserver.pathfinding.geonodes;

import l2server.Config;
import l2server.gameserver.GeoData;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.Location;
import l2server.gameserver.pathfinding.AbstractNode;
import l2server.gameserver.pathfinding.AbstractNodeLoc;
import l2server.gameserver.pathfinding.PathFinding;
import l2server.gameserver.pathfinding.utils.FastNodeList;
import l2server.log.Log;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.logging.Level;

/**
 * @author -Nemesiss-
 */
public class GeoPathFinding extends PathFinding
{

	private static Map<Short, ByteBuffer> _pathNodes = new HashMap<>();
	private static Map<Short, IntBuffer> _pathNodesIndex = new HashMap<>();

	public static GeoPathFinding getInstance()
	{
		return SingletonHolder._instance;
	}

	/**
	 */
	@Override
	public boolean pathNodesExist(short regionoffset)
	{
		return _pathNodesIndex.containsKey(regionoffset);
	}

	/**
	 */
	@Override
	public List<AbstractNodeLoc> findPath(int x, int y, int z, int tx, int ty, int tz, int instanceId, boolean playable)
	{
		int gx = x - L2World.MAP_MIN_X >> 4;
		int gy = y - L2World.MAP_MIN_Y >> 4;
		short gz = (short) z;
		int gtx = tx - L2World.MAP_MIN_X >> 4;
		int gty = ty - L2World.MAP_MIN_Y >> 4;
		short gtz = (short) tz;

		GeoNode start = readNode(gx, gy, gz);
		GeoNode end = readNode(gtx, gty, gtz);
		if (start == null || end == null)
		{
			return null;
		}
		if (Math.abs(start.getLoc().getZ() - z) > 55)
		{
			return null; // not correct layer
		}
		if (Math.abs(end.getLoc().getZ() - tz) > 55)
		{
			return null; // not correct layer
		}
		if (start == end)
		{
			return null;
		}

		// TODO: Find closest path node we CAN access. Now only checks if we can not reach the closest
		Location temp = GeoData.getInstance()
				.moveCheck(x, y, z, start.getLoc().getX(), start.getLoc().getY(), start.getLoc().getZ(), instanceId);
		if (temp.getX() != start.getLoc().getX() || temp.getY() != start.getLoc().getY())
		{
			return null; //   cannot reach closest...
		}

		// TODO: Find closest path node around target, now only checks if final location can be reached
		temp = GeoData.getInstance()
				.moveCheck(tx, ty, tz, end.getLoc().getX(), end.getLoc().getY(), end.getLoc().getZ(), instanceId);
		if (temp.getX() != end.getLoc().getX() || temp.getY() != end.getLoc().getY())
		{
			return null; //   cannot reach closest...
		}

		//return searchAStar(start, end);
		return searchByClosest2(start, end);
	}

	public List<AbstractNodeLoc> searchByClosest2(GeoNode start, GeoNode end)
	{
		// Always continues checking from the closest to target non-blocked
		// node from to_visit list. There's extra length in path if needed
		// to go backwards/sideways but when moving generally forwards, this is extra fast
		// and accurate. And can reach insane distances (try it with 800 nodes..).
		// Minimum required node count would be around 300-400.
		// Generally returns a bit (only a bit) more intelligent looking routes than
		// the basic version. Not a true distance image (which would increase CPU
		// load) level of intelligence though.

		// List of Visited Nodes
		FastNodeList visited = new FastNodeList(550);

		// List of Nodes to Visit
		LinkedList<GeoNode> to_visit = new LinkedList<>();
		to_visit.add(start);
		int targetX = end.getLoc().getNodeX();
		int targetY = end.getLoc().getNodeY();

		int dx, dy;
		boolean added;
		int i = 0;
		while (i < 550)
		{
			GeoNode node;
			try
			{
				node = to_visit.removeFirst();
			}
			catch (Exception e)
			{
				// No Path found
				return null;
			}
			if (node.equals(end)) //path found!
			{
				return constructPath2(node);
			}
			else
			{
				i++;
				visited.add(node);
				node.attachNeighbors();
				GeoNode[] neighbors = node.getNeighbors();
				if (neighbors == null)
				{
					continue;
				}
				for (GeoNode n : neighbors)
				{
					if (!visited.containsRev(n) && !to_visit.contains(n))
					{
						added = false;
						n.setParent(node);
						dx = targetX - n.getLoc().getNodeX();
						dy = targetY - n.getLoc().getNodeY();
						n.setCost(dx * dx + dy * dy);
						for (int index = 0; index < to_visit.size(); index++)
						{
							// supposed to find it quite early..
							if (to_visit.get(index).getCost() > n.getCost())
							{
								to_visit.add(index, n);
								added = true;
								break;
							}
						}
						if (!added)
						{
							to_visit.addLast(n);
						}
					}
				}
			}
		}
		//No Path found
		return null;
	}

	public List<AbstractNodeLoc> constructPath2(AbstractNode node)
	{
		LinkedList<AbstractNodeLoc> path = new LinkedList<>();
		int previousDirectionX = -1000;
		int previousDirectionY = -1000;
		int directionX;
		int directionY;

		while (node.getParent() != null)
		{
			// only add a new route point if moving direction changes
			directionX = node.getLoc().getNodeX() - node.getParent().getLoc().getNodeX();
			directionY = node.getLoc().getNodeY() - node.getParent().getLoc().getNodeY();

			if (directionX != previousDirectionX || directionY != previousDirectionY)
			{
				previousDirectionX = directionX;
				previousDirectionY = directionY;
				path.addFirst(node.getLoc());
			}
			node = node.getParent();
		}
		return path;
	}

	public GeoNode[] readNeighbors(GeoNode n, int idx)
	{
		int node_x = n.getLoc().getNodeX();
		int node_y = n.getLoc().getNodeY();
		//short node_z = n.getLoc().getZ();

		short regoffset = getRegionOffset(getRegionX(node_x), getRegionY(node_y));
		ByteBuffer pn = _pathNodes.get(regoffset);

		List<AbstractNode> Neighbors = new ArrayList<>(8);
		GeoNode newNode;
		short new_node_x, new_node_y;

		//Region for sure will change, we must read from correct file
		byte neighbor = pn.get(idx++); //N
		if (neighbor > 0)
		{
			neighbor--;
			new_node_x = (short) node_x;
			new_node_y = (short) (node_y - 1);
			newNode = readNode(new_node_x, new_node_y, neighbor);
			if (newNode != null)
			{
				Neighbors.add(newNode);
			}
		}
		neighbor = pn.get(idx++); //NE
		if (neighbor > 0)
		{
			neighbor--;
			new_node_x = (short) (node_x + 1);
			new_node_y = (short) (node_y - 1);
			newNode = readNode(new_node_x, new_node_y, neighbor);
			if (newNode != null)
			{
				Neighbors.add(newNode);
			}
		}
		neighbor = pn.get(idx++); //E
		if (neighbor > 0)
		{
			neighbor--;
			new_node_x = (short) (node_x + 1);
			new_node_y = (short) node_y;
			newNode = readNode(new_node_x, new_node_y, neighbor);
			if (newNode != null)
			{
				Neighbors.add(newNode);
			}
		}
		neighbor = pn.get(idx++); //SE
		if (neighbor > 0)
		{
			neighbor--;
			new_node_x = (short) (node_x + 1);
			new_node_y = (short) (node_y + 1);
			newNode = readNode(new_node_x, new_node_y, neighbor);
			if (newNode != null)
			{
				Neighbors.add(newNode);
			}
		}
		neighbor = pn.get(idx++); //S
		if (neighbor > 0)
		{
			neighbor--;
			new_node_x = (short) node_x;
			new_node_y = (short) (node_y + 1);
			newNode = readNode(new_node_x, new_node_y, neighbor);
			if (newNode != null)
			{
				Neighbors.add(newNode);
			}
		}
		neighbor = pn.get(idx++); //SW
		if (neighbor > 0)
		{
			neighbor--;
			new_node_x = (short) (node_x - 1);
			new_node_y = (short) (node_y + 1);
			newNode = readNode(new_node_x, new_node_y, neighbor);
			if (newNode != null)
			{
				Neighbors.add(newNode);
			}
		}
		neighbor = pn.get(idx++); //W
		if (neighbor > 0)
		{
			neighbor--;
			new_node_x = (short) (node_x - 1);
			new_node_y = (short) node_y;
			newNode = readNode(new_node_x, new_node_y, neighbor);
			if (newNode != null)
			{
				Neighbors.add(newNode);
			}
		}
		neighbor = pn.get(idx++); //NW
		if (neighbor > 0)
		{
			neighbor--;
			new_node_x = (short) (node_x - 1);
			new_node_y = (short) (node_y - 1);
			newNode = readNode(new_node_x, new_node_y, neighbor);
			if (newNode != null)
			{
				Neighbors.add(newNode);
			}
		}
		GeoNode[] result = new GeoNode[Neighbors.size()];
		return Neighbors.toArray(result);
	}

	//Private

	private GeoNode readNode(short node_x, short node_y, byte layer)
	{
		short regoffset = getRegionOffset(getRegionX(node_x), getRegionY(node_y));
		if (!pathNodesExist(regoffset))
		{
			return null;
		}
		short nbx = getNodeBlock(node_x);
		short nby = getNodeBlock(node_y);
		int idx = _pathNodesIndex.get(regoffset).get((nby << 8) + nbx);
		ByteBuffer pn = _pathNodes.get(regoffset);
		//reading
		byte nodes = pn.get(idx);
		idx += layer * 10 + 1;//byte + layer*10byte
		if (nodes < layer)
		{
			Log.warning("SmthWrong!");
		}
		short node_z = pn.getShort(idx);
		idx += 2;
		return new GeoNode(new GeoNodeLoc(node_x, node_y, node_z), idx);
	}

	private GeoNode readNode(int gx, int gy, short z)
	{
		short node_x = getNodePos(gx);
		short node_y = getNodePos(gy);
		short regoffset = getRegionOffset(getRegionX(node_x), getRegionY(node_y));
		if (!pathNodesExist(regoffset))
		{
			return null;
		}
		short nbx = getNodeBlock(node_x);
		short nby = getNodeBlock(node_y);
		int idx = _pathNodesIndex.get(regoffset).get((nby << 8) + nbx);
		ByteBuffer pn = _pathNodes.get(regoffset);
		//reading
		byte nodes = pn.get(idx++);
		int idx2 = 0; //create index to nearlest node by z
		short last_z = Short.MIN_VALUE;
		while (nodes > 0)
		{
			short node_z = pn.getShort(idx);
			if (Math.abs(last_z - z) > Math.abs(node_z - z))
			{
				last_z = node_z;
				idx2 = idx + 2;
			}
			idx += 10; //short + 8 byte
			nodes--;
		}
		return new GeoNode(new GeoNodeLoc(node_x, node_y, last_z), idx2);
	}

	private GeoPathFinding()
	{
		LineNumberReader lnr = null;
		try
		{
			Log.info("PathFinding Engine: - Loading Path Nodes...");
			File Data = new File("./data/pathnode/pn_index.txt");
			if (!Data.exists())
			{
				return;
			}

			lnr = new LineNumberReader(new BufferedReader(new FileReader(Data)));
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "", e);
			throw new Error("Failed to Load pn_index File.");
		}
		String line;
		try
		{
			while ((line = lnr.readLine()) != null)
			{
				if (line.trim().length() == 0)
				{
					continue;
				}
				StringTokenizer st = new StringTokenizer(line, "_");
				byte rx = Byte.parseByte(st.nextToken());
				byte ry = Byte.parseByte(st.nextToken());
				LoadPathNodeFile(rx, ry);
			}
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "", e);
			throw new Error("Failed to Read pn_index File.");
		}
		finally
		{
			try
			{
				lnr.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private void LoadPathNodeFile(byte rx, byte ry)
	{
		if (rx < Config.WORLD_X_MIN || rx > Config.WORLD_X_MAX || ry < Config.WORLD_Y_MIN || ry > Config.WORLD_Y_MAX)
		{
			Log.warning("Failed to Load PathNode File: invalid region " + rx + "," + ry + "\n");
			return;
		}
		String fname = "./data/pathnode/" + rx + "_" + ry + ".pn";
		short regionoffset = getRegionOffset(rx, ry);
		//Logozo.info("PathFinding Engine: - Loading: " + fname + " -> region offset: " + regionoffset + "X: " + rx + " Y: " + ry);
		File Pn = new File(fname);
		int node = 0, size, index = 0;
		FileChannel roChannel = null;
		try
		{
			// Create a read-only memory-mapped file
			RandomAccessFile raf = new RandomAccessFile(Pn, "r");
			roChannel = raf.getChannel();
			size = (int) roChannel.size();
			MappedByteBuffer nodes;
			if (Config.FORCE_GEODATA) //Force O/S to Loads this buffer's content into physical memory.
			//it is not guarantee, because the underlying operating system may have paged out some of the buffer's data
			{
				nodes = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, size).load();
			}
			else
			{
				nodes = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, size);
			}

			raf.close();

			// Indexing pathnode files, so we will know where each block starts
			IntBuffer indexs = IntBuffer.allocate(65536);

			while (node < 65536)
			{
				byte layer = nodes.get(index);
				indexs.put(node++, index);
				index += layer * 10 + 1;
			}
			_pathNodesIndex.put(regionoffset, indexs);
			_pathNodes.put(regionoffset, nodes);
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Failed to Load PathNode File: " + fname + " : " + e.getMessage(), e);
		}
		finally
		{
			try
			{
				roChannel.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final GeoPathFinding _instance = new GeoPathFinding();
	}
}
