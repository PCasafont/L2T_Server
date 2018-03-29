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

package l2server.gameserver.network;

import l2server.Config;

public class ClientStats
{
	public int processedPackets = 0;
	public int droppedPackets = 0;
	public int unknownPackets = 0;
	public int totalQueueSize = 0;
	public int maxQueueSize = 0;
	public int totalBursts = 0;
	public int maxBurstSize = 0;
	public int shortFloods = 0;
	public int longFloods = 0;
	public int totalQueueOverflows = 0;
	public int totalUnderflowExceptions = 0;

	private final int[] packetsInSecond;
	private long packetCountStartTick = 0;
	private int head;
	private int totalCount = 0;

	private int floodsInMin = 0;
	private long floodStartTick = 0;
	private int unknownPacketsInMin = 0;
	private long unknownPacketStartTick = 0;
	private int overflowsInMin = 0;
	private long overflowStartTick = 0;
	private int underflowReadsInMin = 0;
	private long underflowReadStartTick = 0;

	private volatile boolean floodDetected = false;
	private volatile boolean queueOverflowDetected = false;

	private final int BUFFER_SIZE;

	public ClientStats()
	{
		BUFFER_SIZE = Config.CLIENT_PACKET_QUEUE_MEASURE_INTERVAL;
		packetsInSecond = new int[BUFFER_SIZE];
		head = BUFFER_SIZE - 1;
	}

	/**
	 * Returns true if incoming packet need to be dropped
	 */
	protected final boolean dropPacket()
	{
		final boolean result = floodDetected || queueOverflowDetected;
		if (result)
		{
			droppedPackets++;
		}
		return result;
	}

	/**
	 * Returns true if flood detected first and ActionFailed packet need to be sent.
	 * Later during flood returns true (and send ActionFailed) once per second.
	 */
	protected final boolean countPacket(int queueSize)
	{
		processedPackets++;
		totalQueueSize += queueSize;
		if (maxQueueSize < queueSize)
		{
			maxQueueSize = queueSize;
		}
		if (queueOverflowDetected && queueSize < 2)
		{
			queueOverflowDetected = false;
		}

		return countPacket();
	}

	/**
	 * Counts unknown packets and return true if threshold is reached.
	 */
	protected final boolean countUnknownPacket()
	{
		unknownPackets++;

		final long tick = System.currentTimeMillis();
		if (tick - unknownPacketStartTick > 60000)
		{
			unknownPacketStartTick = tick;
			unknownPacketsInMin = 1;
			return false;
		}

		unknownPacketsInMin++;
		return unknownPacketsInMin > Config.CLIENT_PACKET_QUEUE_MAX_UNKNOWN_PER_MIN;
	}

	/**
	 * Counts burst length and return true if execution of the queue need to be aborted.
	 *
	 * @param count - current number of processed packets in burst
	 */
	protected final boolean countBurst(int count)
	{
		if (count > maxBurstSize)
		{
			maxBurstSize = count;
		}

		if (count < Config.CLIENT_PACKET_QUEUE_MAX_BURST_SIZE)
		{
			return false;
		}

		totalBursts++;
		return true;
	}

	/**
	 * Counts queue overflows and return true if threshold is reached.
	 */
	protected final boolean countQueueOverflow()
	{
		queueOverflowDetected = true;
		totalQueueOverflows++;

		final long tick = System.currentTimeMillis();
		if (tick - overflowStartTick > 60000)
		{
			overflowStartTick = tick;
			overflowsInMin = 1;
			return false;
		}

		overflowsInMin++;
		return overflowsInMin > Config.CLIENT_PACKET_QUEUE_MAX_OVERFLOWS_PER_MIN;
	}

	/**
	 * Counts underflow exceptions and return true if threshold is reached.
	 */
	protected final boolean countUnderflowException()
	{
		totalUnderflowExceptions++;

		final long tick = System.currentTimeMillis();
		if (tick - underflowReadStartTick > 60000)
		{
			underflowReadStartTick = tick;
			underflowReadsInMin = 1;
			return false;
		}

		underflowReadsInMin++;
		return underflowReadsInMin > Config.CLIENT_PACKET_QUEUE_MAX_UNDERFLOWS_PER_MIN;
	}

	/**
	 * Returns true if maximum number of floods per minute is reached.
	 */
	protected final boolean countFloods()
	{
		return floodsInMin > Config.CLIENT_PACKET_QUEUE_MAX_FLOODS_PER_MIN;
	}

	private boolean longFloodDetected()
	{
		return totalCount / BUFFER_SIZE > Config.CLIENT_PACKET_QUEUE_MAX_AVERAGE_PACKETS_PER_SECOND;
	}

	/**
	 * Returns true if flood detected first and ActionFailed packet need to be sent.
	 * Later during flood returns true (and send ActionFailed) once per second.
	 */
	private synchronized boolean countPacket()
	{
		totalCount++;
		final long tick = System.currentTimeMillis();
		if (tick - packetCountStartTick > 1000)
		{
			packetCountStartTick = tick;

			// clear flag if no more flooding during last seconds
			if (floodDetected && !longFloodDetected() &&
					packetsInSecond[head] < Config.CLIENT_PACKET_QUEUE_MAX_PACKETS_PER_SECOND / 2)
			{
				floodDetected = false;
			}

			// wrap head of the buffer around the tail
			if (head <= 0)
			{
				head = BUFFER_SIZE;
			}
			head--;

			totalCount -= packetsInSecond[head];
			packetsInSecond[head] = 1;
			return floodDetected;
		}

		final int count = ++packetsInSecond[head];
		if (!floodDetected)
		{
			if (count > Config.CLIENT_PACKET_QUEUE_MAX_PACKETS_PER_SECOND)
			{
				shortFloods++;
			}
			else if (longFloodDetected())
			{
				longFloods++;
			}
			else
			{
				return false;
			}

			floodDetected = true;
			if (tick - floodStartTick > 60000)
			{
				floodStartTick = tick;
				floodsInMin = 1;
			}
			else
			{
				floodsInMin++;
			}

			return true; // Return true only in the beginning of the flood
		}

		return false;
	}
}
