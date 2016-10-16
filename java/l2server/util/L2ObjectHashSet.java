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

package l2server.util;

import l2server.gameserver.model.L2Object;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This class is a highly optimized hashtable, where
 * keys are integers. The main goal of this class is to allow
 * concurent read/iterate and write access to this table,
 * plus minimal used memory.
 * <p>
 * This class uses plain array as the table of values, and
 * keys are used to get position in the table. If the position
 * is already busy, we iterate to the next position, unil we
 * find the needed element or null.
 * <p>
 * To iterate over the table (read access) we may simply iterate
 * throgh table array.
 * <p>
 * In case we remove an element from the table, we check - if
 * the next position is null, we reset table's slot to null,
 * otherwice we assign it to a dummy value
 *
 * @param <T> type of values stored in this hashtable
 * @author mkizub
 */
public final class L2ObjectHashSet<T extends L2Object> extends L2ObjectSet<T> implements Iterable<T>
{

	private static final boolean TRACE = false;
	private static final boolean DEBUG = false;

	private static final int[] PRIMES = {
			5,
			7,
			11,
			17,
			23,
			29,
			37,
			47,
			59,
			71,
			89,
			107,
			131,
			163,
			197,
			239,
			293,
			353,
			431,
			521,
			631,
			761,
			919,
			1103,
			1327,
			1597,
			1931,
			2333,
			2801,
			3371,
			4049,
			4861,
			5839,
			7013,
			8419,
			10103,
			12143,
			14591,
			17519,
			21023,
			25229,
			30293,
			36353,
			43627,
			52361,
			62851,
			75431,
			90523,
			108631,
			130363,
			156437,
			187751,
			225307,
			270371,
			324449,
			389357,
			467237,
			560689,
			672827,
			807403,
			968897,
			1162687,
			1395263,
			1674319,
			2009191,
			2411033,
			2893249,
			3471899,
			4166287,
			4999559,
			5999471,
			7199369
	};

	private T[] table;
	private int[] collisions;
	private int count;

	private static int getPrime(int min)
	{
		for (int element : PRIMES)
		{
			if (element >= min)
			{
				return element;
			}
		}
		throw new OutOfMemoryError();
	}

	@SuppressWarnings("unchecked")
	public L2ObjectHashSet()
	{
		int size = PRIMES[0];
		this.table = (T[]) new L2Object[size];
		this.collisions = new int[size + 31 >> 5];
		if (DEBUG)
		{
			check();
		}
	}

	/* (non-Javadoc)
	 * @see l2server.util.L2ObjectSet#size()
	 */
	@Override
	public int size()
	{
		return this.count;
	}

	/* (non-Javadoc)
	 * @see l2server.util.L2ObjectSet#isEmpty()
	 */
	@Override
	public boolean isEmpty()
	{
		return this.count == 0;
	}

	/* (non-Javadoc)
	 * @see l2server.util.L2ObjectSet#clear()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public synchronized void clear()
	{
		int size = PRIMES[0];
		this.table = (T[]) new L2Object[size];
		this.collisions = new int[size + 31 >> 5];
		this.count = 0;
		if (DEBUG)
		{
			check();
		}
	}

	private void check()
	{
		if (DEBUG)
		{
			int cnt = 0;
			assert this.collisions.length == this.table.length + 31 >> 5;
			for (T obj : this.table)
			{
				if (obj != null)
				{
					cnt++;
				}
			}
			assert cnt == this.count;
		}
	}

	/* (non-Javadoc)
	 * @see l2server.util.L2ObjectSet#put(T)
	 */
	@Override
	public synchronized void put(T obj)
	{
		if (obj == null)
		{
			return;
		}
		if (contains(obj))
		{
			return;
		}
		if (this.count >= this.table.length / 2)
		{
			expand();
		}
		final int hashcode = obj.getObjectId();
		assert hashcode > 0;
		int seed = hashcode;
		int incr = 1 + ((seed >> 5) + 1) % (this.table.length - 1);
		int ntry = 0;
		int slot = -1; // keep last found slot
		do
		{
			int pos = seed % this.table.length & 0x7FFFFFFF;
			if (this.table[pos] == null)
			{
				if (slot < 0)
				{
					slot = pos;
				}
				if ((this.collisions[pos >> 5] & 1 << (pos & 31)) == 0)
				{
					// found an empty slot without previous collisions,
					// but use previously found slot
					this.table[slot] = obj;
					this.count++;
					if (TRACE)
					{
						System.err.println("ht: put obj id=" + hashcode + " at slot=" + slot);
					}
					if (DEBUG)
					{
						check();
					}
					return;
				}
			}
			else
			{
				// check if we are adding the same object
				if (this.table[pos] == obj)
				{
					return;
				}
				// this should never happen
				assert obj.getObjectId() != this.table[pos].getObjectId();
				// if there was no collisions at this slot, and we found a free
				// slot previously - use found slot
				if (slot >= 0 && (this.collisions[pos >> 5] & 1 << (pos & 31)) == 0)
				{
					this.table[slot] = obj;
					this.count++;
					if (TRACE)
					{
						System.err.println("ht: put obj id=" + hashcode + " at slot=" + slot);
					}
					if (DEBUG)
					{
						check();
					}
					return;
				}
			}

			// set collision bit
			this.collisions[pos >> 5] |= 1 << (pos & 31);
			// calculate next slot
			seed += incr;
		}
		while (++ntry < this.table.length);
		if (DEBUG)
		{
			check();
		}
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see l2server.util.L2ObjectSet#remove(T)
	 */
	@Override
	public synchronized void remove(T obj)
	{
		if (obj == null)
		{
			return;
		}
		if (!contains(obj))
		{
			return;
		}
		int hashcode = obj.getObjectId();
		assert hashcode > 0;
		int seed = hashcode;
		int incr = 1 + ((seed >> 5) + 1) % (this.table.length - 1);
		int ntry = 0;
		do
		{
			int pos = seed % this.table.length & 0x7FFFFFFF;
			if (this.table[pos] == obj)
			{
				// found the object
				this.table[pos] = null;
				this.count--;
				if (TRACE)
				{
					System.err.println("ht: remove obj id=" + hashcode + " from slot=" + pos);
				}
				if (DEBUG)
				{
					check();
				}
				return;
			}
			// check for collision (if we previously deleted element)
			if (this.table[pos] == null && (this.collisions[pos >> 5] & 1 << (pos & 31)) == 0)
			{
				if (DEBUG)
				{
					check();
				}
				return; //throw new IllegalArgumentException();
			}
			// calculate next slot
			seed += incr;
		}
		while (++ntry < this.table.length);
		if (DEBUG)
		{
			check();
		}
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see l2server.util.L2ObjectSet#contains(T)
	 */
	@Override
	public boolean contains(T obj)
	{
		final int size = this.table.length;
		if (size <= 11)
		{
			// for small tables linear check is fast
			for (T a_table : this.table)
			{
				if (a_table == obj)
				{
					return true;
				}
			}
			return false;
		}
		int hashcode = obj.getObjectId();
		assert hashcode > 0;
		int seed = hashcode;
		int incr = 1 + ((seed >> 5) + 1) % (size - 1);
		int ntry = 0;
		do
		{
			int pos = seed % size & 0x7FFFFFFF;
			if (this.table[pos] == obj)
			{
				return true;
			}
			// check for collision (if we previously deleted element)
			if (this.table[pos] == null && (this.collisions[pos >> 5] & 1 << (pos & 31)) == 0)
			{
				return false;
			}
			// calculate next slot
			seed += incr;
		}
		while (++ntry < size);
		return false;
	}

	@SuppressWarnings("unchecked")
	private/*already synchronized in put()*/void expand()
	{
		int newSize = getPrime(this.table.length + 1);
		L2Object[] newTable = new L2Object[newSize];
		int[] newCollisions = new int[newSize + 31 >> 5];

		// over all old entries
		next_entry:
		for (int i = 0; i < this.table.length; i++)
		{
			L2Object obj = this.table[i];
			if (obj == null)
			{
				continue;
			}
			final int hashcode = obj.getObjectId();
			int seed = hashcode;
			int incr = 1 + ((seed >> 5) + 1) % (newSize - 1);
			int ntry = 0;
			do
			{
				int pos = seed % newSize & 0x7FFFFFFF;
				if (newTable[pos] == null)
				{
					// found an empty slot without previous collisions,
					// but use previously found slot
					newTable[pos] = obj;
					if (TRACE)
					{
						System.err.println("ht: move obj id=" + hashcode + " from slot=" + i + " to slot=" + pos);
					}
					continue next_entry;
				}
				// set collision bit
				newCollisions[pos >> 5] |= 1 << (pos & 31);
				// calculate next slot
				seed += incr;
			}
			while (++ntry < newSize);
			throw new IllegalStateException();
		}
		this.table = (T[]) newTable;
		this.collisions = newCollisions;
		if (DEBUG)
		{
			check();
		}
	}

	/* (non-Javadoc)
	 * @see l2server.util.L2ObjectSet#iterator()
	 */
	@Override
	public Iterator<T> iterator()
	{
		return new Itr(this.table);
	}

	class Itr implements Iterator<T>
	{
		private final T[] array;
		private int nextIdx;
		private T nextObj;
		private T lastRet;

		Itr(T[] pArray)
		{
			this.array = pArray;
			for (; this.nextIdx < this.array.length; this.nextIdx++)
			{
				this.nextObj = this.array[this.nextIdx];
				if (this.nextObj != null)
				{
					return;
				}
			}
		}

		@Override
		public boolean hasNext()
		{
			return this.nextObj != null;
		}

		@Override
		public T next()
		{
			if (this.nextObj == null)
			{
				throw new NoSuchElementException();
			}
			this.lastRet = this.nextObj;
			for (this.nextIdx++; this.nextIdx < this.array.length; this.nextIdx++)
			{
				this.nextObj = this.array[this.nextIdx];
				if (this.nextObj != null)
				{
					break;
				}
			}
			if (this.nextIdx >= this.array.length)
			{
				this.nextObj = null;
			}
			return this.lastRet;
		}

		@Override
		public void remove()
		{
			if (this.lastRet == null)
			{
				throw new IllegalStateException();
			}
			L2ObjectHashSet.this.remove(this.lastRet);
		}
	}
}
