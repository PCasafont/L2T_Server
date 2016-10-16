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

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * Highly concurrent List wrapping class, thread-safe<br>
 * The default constructor sets a wrapped class to ArrayList<T> to minimize code line
 * when wrapped class type is not that importent.<br>
 * <br>
 * Note: Iterator returned does not support element removal!
 *
 * @author Julian
 */
public class L2SyncList<T> implements List<T>
{
	private final List<T> list;
	private final ReentrantReadWriteLock rw = new ReentrantReadWriteLock();
	private final ReadLock rl = this.rw.readLock();
	private final WriteLock wl = this.rw.writeLock();

	/**
	 * Default constructor use ArrayList as it internal
	 */
	public L2SyncList()
	{
		this.list = new ArrayList<>();
	}

	public L2SyncList(List<T> list)
	{
		this.list = list;
	}

	@Override
	public T get(int index)
	{
		this.rl.lock();
		try
		{
			return this.list.get(index);
		}
		finally
		{
			this.rl.unlock();
		}
	}

	@Override
	public boolean equals(Object o)
	{
		this.rl.lock();
		try
		{
			return this.list.equals(o);
		}
		finally
		{
			this.rl.unlock();
		}
	}

	@Override
	public int hashCode()
	{
		this.rl.lock();
		try
		{
			return this.list.hashCode();
		}
		finally
		{
			this.rl.unlock();
		}
	}

	@Override
	public T set(int index, T element)
	{
		this.wl.lock();
		try
		{
			return this.list.set(index, element);
		}
		finally
		{
			this.wl.unlock();
		}
	}

	@Override
	public void add(int index, T element)
	{
		this.wl.lock();
		try
		{
			this.list.add(index, element);
		}
		finally
		{
			this.wl.unlock();
		}
	}

	@Override
	public boolean add(T element)
	{
		this.wl.lock();
		try
		{
			return this.list.add(element);
		}
		finally
		{
			this.wl.unlock();
		}
	}

	@Override
	public T remove(int index)
	{
		this.wl.lock();
		try
		{
			return this.list.remove(index);
		}
		finally
		{
			this.wl.unlock();
		}
	}

	@Override
	public boolean remove(Object value)
	{
		this.wl.lock();
		try
		{
			return this.list.remove(value);
		}
		finally
		{
			this.wl.unlock();
		}
	}

	@Override
	public boolean removeAll(Collection<?> list)
	{
		this.wl.lock();
		try
		{
			return this.list.removeAll(list);
		}
		finally
		{
			this.wl.unlock();
		}
	}

	@Override
	public boolean retainAll(Collection<?> list)
	{
		this.wl.lock();
		try
		{
			return this.list.retainAll(list);
		}
		finally
		{
			this.wl.unlock();
		}
	}

	@Override
	public int indexOf(Object o)
	{
		this.rl.lock();
		try
		{
			return this.list.indexOf(o);
		}
		finally
		{
			this.rl.unlock();
		}
	}

	@Override
	public boolean contains(Object o)
	{
		this.rl.lock();
		try
		{
			return this.list.contains(o);
		}
		finally
		{
			this.rl.unlock();
		}
	}

	@Override
	public boolean containsAll(Collection<?> list)
	{
		this.rl.lock();
		try
		{
			return this.list.containsAll(list);
		}
		finally
		{
			this.rl.unlock();
		}
	}

	@Override
	public int lastIndexOf(Object o)
	{
		this.rl.lock();
		try
		{
			return this.list.lastIndexOf(o);
		}
		finally
		{
			this.rl.unlock();
		}
	}

	@Override
	public boolean addAll(Collection<? extends T> list)
	{
		this.wl.lock();
		try
		{
			return this.list.addAll(list);
		}
		finally
		{
			this.wl.unlock();
		}
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c)
	{
		this.wl.lock();
		try
		{
			return this.list.addAll(index, c);
		}
		finally
		{
			this.wl.unlock();
		}
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex)
	{
		this.rl.lock();
		try
		{
			return new L2SyncList<>(this.list.subList(fromIndex, toIndex));
		}
		finally
		{
			this.rl.unlock();
		}
	}

	@Override
	public void clear()
	{
		this.wl.lock();
		try
		{
			this.list.clear();
		}
		finally
		{
			this.wl.unlock();
		}
	}

	@Override
	public int size()
	{
		this.rl.lock();
		try
		{
			return this.list.size();
		}
		finally
		{
			this.rl.unlock();
		}
	}

	@Override
	public boolean isEmpty()
	{
		this.rl.lock();
		try
		{
			return this.list.isEmpty();
		}
		finally
		{
			this.rl.unlock();
		}
	}

	/**
	 * <FONT color="#FF0000">WARNING: Unsupported</FONT>
	 */
	@Override
	public ListIterator<T> listIterator()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * <FONT color="#FF0000">WARNING: Unsupported</FONT>
	 *
	 * @see java.util.List#listIterator(int)
	 */
	@Override
	public ListIterator<T> listIterator(int index)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * <FONT color="#FF0000">WARNING: Returned iterator use cloned List</FONT>
	 *
	 * @see java.util.List#iterator()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Iterator<T> iterator()
	{
		return new Itr((T[]) this.list.toArray());
	}

	private class Itr implements Iterator<T>
	{
		int cursor; // index of next element to return
		int lastRet = -1; // index of last element returned; -1 if no such
		int size;
		T[] elementData;

		public Itr(T[] data)
		{
			elementData = data;
			if (data != null)
			{
				size = data.length;
			}
			else
			{
				size = 0;
			}
		}

		@Override
		public boolean hasNext()
		{
			return cursor != size;
		}

		@Override
		public T next()
		{
			int i = cursor;
			if (i >= size)
			{
				throw new NoSuchElementException();
			}
			cursor = i + 1;
			lastRet = i;
			return elementData[lastRet];
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Object[] toArray()
	{
		this.rl.lock();
		try
		{
			return this.list.toArray();
		}
		finally
		{
			this.rl.unlock();
		}
	}

	@Override
	@SuppressWarnings("hiding")
	public <T> T[] toArray(T[] a)
	{
		this.rl.lock();
		try
		{
			return this.list.toArray(a);
		}
		finally
		{
			this.rl.unlock();
		}
	}
}
