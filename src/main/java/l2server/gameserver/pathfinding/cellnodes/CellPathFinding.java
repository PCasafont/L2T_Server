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

package l2server.gameserver.pathfinding.cellnodes;

import l2server.Config;
import l2server.gameserver.GameApplication;
import l2server.gameserver.GeoData;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.World;
import l2server.gameserver.pathfinding.AbstractNode;
import l2server.gameserver.pathfinding.AbstractNodeLoc;
import l2server.gameserver.pathfinding.PathFinding;
import l2server.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Sami, DS
 * Credits to Diamond
 */
public class CellPathFinding extends PathFinding {
	private static Logger log = LoggerFactory.getLogger(GameApplication.class.getName());
	private BufferInfo[] allBuffers;
	private int findSuccess = 0;
	private int findFails = 0;
	private int postFilterUses = 0;
	private int postFilterPlayableUses = 0;
	private int postFilterPasses = 0;
	private long postFilterElapsed = 0;

	private ArrayList<Item> debugItems = null;

	public static CellPathFinding getInstance() {
		return SingletonHolder.instance;
	}

	private CellPathFinding() {
		try {
			String[] array = Config.PATHFIND_BUFFERS.split(";");

			allBuffers = new BufferInfo[array.length];

			String buf;
			String[] args;
			for (int i = 0; i < array.length; i++) {
				buf = array[i];
				args = buf.split("x");
				if (args.length != 2) {
					throw new Exception("Invalid buffer definition: " + buf);
				}

				allBuffers[i] = new BufferInfo(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
			}
		} catch (Exception e) {
			log.warn("CellPathFinding: Problem during buffer init: " + e.getMessage(), e);
			throw new Error("CellPathFinding: load aborted");
		}
	}

	/**
	 */
	@Override
	public boolean pathNodesExist(short regionoffset) {
		return false;
	}

	/**
	 */
	@Override
	public List<AbstractNodeLoc> findPath(int x, int y, int z, int tx, int ty, int tz, int instanceId, boolean playable) {
		int gx = x - World.MAP_MIN_X >> 4;
		int gy = y - World.MAP_MIN_Y >> 4;
		if (!GeoData.getInstance().hasGeo(x, y)) {
			return null;
		}
		short gz = GeoData.getInstance().getHeight(x, y, z);
		int gtx = tx - World.MAP_MIN_X >> 4;
		int gty = ty - World.MAP_MIN_Y >> 4;
		if (!GeoData.getInstance().hasGeo(tx, ty)) {
			return null;
		}
		short gtz = GeoData.getInstance().getHeight(tx, ty, tz);
		CellNodeBuffer buffer = alloc(64 + 2 * Math.max(Math.abs(gx - gtx), Math.abs(gy - gty)), playable);
		if (buffer == null) {
			return null;
		}

		boolean debug = playable && Config.DEBUG_PATH;

		if (debug) {
			if (debugItems == null) {
				debugItems = new ArrayList<>();
			} else {
				for (Item item : debugItems) {
					if (item == null) {
						continue;
					}
					item.decayMe();
				}

				debugItems.clear();
			}
		}

		List<AbstractNodeLoc> path = null;
		try {
			CellNode result = buffer.findPath(gx, gy, gz, gtx, gty, gtz);

			if (debug) {
				for (CellNode n : buffer.debugPath()) {
					if (n.getCost() < 0) // calculated path
					{
						dropDebugItem(1831, (int) (-n.getCost() * 10), n.getLoc());
					} else
					// known nodes
					{
						dropDebugItem(57, (int) (n.getCost() * 10), n.getLoc());
					}
				}
			}

			if (result == null) {
				findFails++;
				return null;
			}

			path = constructPath(result);
		} catch (Exception e) {
			log.warn("", e);
			return null;
		} finally {
			buffer.free();
		}

		if (path.size() < 3 || Config.MAX_POSTFILTER_PASSES <= 0) {
			findSuccess++;
			return path;
		}

		long timeStamp = System.currentTimeMillis();
		postFilterUses++;
		if (playable) {
			postFilterPlayableUses++;
		}

		int currentX, currentY, currentZ;
		ListIterator<AbstractNodeLoc> middlePoint;
		AbstractNodeLoc locMiddle, locEnd;
		boolean remove;
		int pass = 0;
		do {
			pass++;
			postFilterPasses++;

			remove = false;
			middlePoint = path.listIterator();
			locEnd = null;
			currentX = x;
			currentY = y;
			currentZ = z;

			while (middlePoint.nextIndex() < path.size() - 1) {
				locMiddle = middlePoint.next();
				locEnd = path.get(middlePoint.nextIndex());
				if (GeoData.getInstance()
						.canMoveFromToTarget(currentX, currentY, currentZ, locEnd.getX(), locEnd.getY(), locEnd.getZ(), instanceId)) {
					middlePoint.remove();
					remove = true;
					if (debug) {
						dropDebugItem(735, 1, locMiddle);
					}
				} else {
					currentX = locMiddle.getX();
					currentY = locMiddle.getY();
					currentZ = locMiddle.getZ();
				}
			}
		}
		// only one postfilter pass for AI
		while (playable && remove && path.size() > 2 && pass < Config.MAX_POSTFILTER_PASSES);

		if (debug) {
			middlePoint = path.listIterator();
			while (middlePoint.hasNext()) {
				locMiddle = middlePoint.next();
				dropDebugItem(65, 1, locMiddle);
			}
		}

		findSuccess++;
		postFilterElapsed += System.currentTimeMillis() - timeStamp;
		return path;
	}

	private List<AbstractNodeLoc> constructPath(AbstractNode node) {
		List<AbstractNodeLoc> path = new ArrayList<>();
		int previousDirectionX = Integer.MIN_VALUE;
		int previousDirectionY = Integer.MIN_VALUE;
		int directionX, directionY;

		while (node.getParent() != null) {
			if (!Config.ADVANCED_DIAGONAL_STRATEGY && node.getParent().getParent() != null) {
				int tmpX = node.getLoc().getNodeX() - node.getParent().getParent().getLoc().getNodeX();
				int tmpY = node.getLoc().getNodeY() - node.getParent().getParent().getLoc().getNodeY();
				if (Math.abs(tmpX) == Math.abs(tmpY)) {
					directionX = tmpX;
					directionY = tmpY;
				} else {
					directionX = node.getLoc().getNodeX() - node.getParent().getLoc().getNodeX();
					directionY = node.getLoc().getNodeY() - node.getParent().getLoc().getNodeY();
				}
			} else {
				directionX = node.getLoc().getNodeX() - node.getParent().getLoc().getNodeX();
				directionY = node.getLoc().getNodeY() - node.getParent().getLoc().getNodeY();
			}

			// only add a new route point if moving direction changes
			if (directionX != previousDirectionX || directionY != previousDirectionY) {
				previousDirectionX = directionX;
				previousDirectionY = directionY;

				path.add(0, node.getLoc());
				node.setLoc(null);
			}

			node = node.getParent();
		}

		return path;
	}

	private CellNodeBuffer alloc(int size, boolean playable) {
		CellNodeBuffer current = null;
		for (BufferInfo i : allBuffers) {
			if (i.mapSize >= size) {
				for (CellNodeBuffer buf : i.bufs) {
					if (buf.lock()) {
						i.uses++;
						if (playable) {
							i.playableUses++;
						}
						i.elapsed += buf.getElapsedTime();
						current = buf;
						break;
					}
				}
				if (current != null) {
					break;
				}

				// not found, allocate temporary buffer
				current = new CellNodeBuffer(i.mapSize);
				current.lock();
				if (i.bufs.size() < i.count) {
					i.bufs.add(current);
					i.uses++;
					if (playable) {
						i.playableUses++;
					}
					break;
				} else {
					i.overflows++;
					if (playable) {
						i.playableOverflows++;
					}
					//System.err.println("Overflow, size requested: " + size + " playable:"+playable);
				}
			}
		}

		return current;
	}

	private void dropDebugItem(int itemId, int num, AbstractNodeLoc loc) {
		final Item item = new Item(IdFactory.getInstance().getNextId(), itemId);
		item.setCount(num);
		item.spawnMe(loc.getX(), loc.getY(), loc.getZ());
		debugItems.add(item);
	}

	private static final class BufferInfo {
		final int mapSize;
		final int count;
		ArrayList<CellNodeBuffer> bufs;
		int uses = 0;
		int playableUses = 0;
		int overflows = 0;
		int playableOverflows = 0;
		long elapsed = 0;

		public BufferInfo(int size, int cnt) {
			mapSize = size;
			count = cnt;
			bufs = new ArrayList<>(count);
		}

		@Override
		public String toString() {
			final StringBuilder stat = new StringBuilder(100);
			StringUtil.append(stat,
					String.valueOf(mapSize),
					"x",
					String.valueOf(mapSize),
					" num:",
					String.valueOf(bufs.size()),
					"/",
					String.valueOf(count),
					" uses:",
					String.valueOf(uses),
					"/",
					String.valueOf(playableUses));
			if (uses > 0) {
				StringUtil.append(stat, " total/avg(ms):", String.valueOf(elapsed), "/", String.format("%1.2f", (double) elapsed / uses));
			}

			StringUtil.append(stat, " ovf:", String.valueOf(overflows), "/", String.valueOf(playableOverflows));

			return stat.toString();
		}
	}

	@Override
	public String[] getStat() {
		final String[] result = new String[allBuffers.length + 1];
		for (int i = 0; i < allBuffers.length; i++) {
			result[i] = allBuffers[i].toString();
		}

		final StringBuilder stat = new StringBuilder(100);
		StringUtil.append(stat, "LOS postfilter uses:", String.valueOf(postFilterUses), "/", String.valueOf(postFilterPlayableUses));
		if (postFilterUses > 0) {
			StringUtil.append(stat,
					" total/avg(ms):",
					String.valueOf(postFilterElapsed),
					"/",
					String.format("%1.2f", (double) postFilterElapsed / postFilterUses),
					" passes total/avg:",
					String.valueOf(postFilterPasses),
					"/",
					String.format("%1.1f", (double) postFilterPasses / postFilterUses),
					"\r\n");
		}
		StringUtil.append(stat, "Pathfind success/fail:", String.valueOf(findSuccess), "/", String.valueOf(findFails));
		result[result.length - 1] = stat.toString();

		return result;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final CellPathFinding instance = new CellPathFinding();
	}
}
