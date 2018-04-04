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
public class L2SyncList<T> implements List<T> {
	private final List<T> list;
	private final ReentrantReadWriteLock rw = new ReentrantReadWriteLock();
	private final ReadLock rl = rw.readLock();
	private final WriteLock wl = rw.writeLock();
	
	/**
	 * Default constructor use ArrayList as it internal
	 */
	public L2SyncList() {
		list = new ArrayList<>();
	}
	
	public L2SyncList(List<T> list) {
		this.list = list;
	}
	
	@Override
	public T get(int index) {
		rl.lock();
		try {
			return list.get(index);
		} finally {
			rl.unlock();
		}
	}
	
	@Override
	public boolean equals(Object o) {
		rl.lock();
		try {
			return list.equals(o);
		} finally {
			rl.unlock();
		}
	}
	
	@Override
	public int hashCode() {
		rl.lock();
		try {
			return list.hashCode();
		} finally {
			rl.unlock();
		}
	}
	
	@Override
	public T set(int index, T element) {
		wl.lock();
		try {
			return list.set(index, element);
		} finally {
			wl.unlock();
		}
	}
	
	@Override
	public void add(int index, T element) {
		wl.lock();
		try {
			list.add(index, element);
		} finally {
			wl.unlock();
		}
	}
	
	@Override
	public boolean add(T element) {
		wl.lock();
		try {
			return list.add(element);
		} finally {
			wl.unlock();
		}
	}
	
	@Override
	public T remove(int index) {
		wl.lock();
		try {
			return list.remove(index);
		} finally {
			wl.unlock();
		}
	}
	
	@Override
	public boolean remove(Object value) {
		wl.lock();
		try {
			return list.remove(value);
		} finally {
			wl.unlock();
		}
	}
	
	@Override
	public boolean removeAll(Collection<?> list) {
		wl.lock();
		try {
			return list.removeAll(list);
		} finally {
			wl.unlock();
		}
	}
	
	@Override
	public boolean retainAll(Collection<?> list) {
		wl.lock();
		try {
			return list.retainAll(list);
		} finally {
			wl.unlock();
		}
	}
	
	@Override
	public int indexOf(Object o) {
		rl.lock();
		try {
			return list.indexOf(o);
		} finally {
			rl.unlock();
		}
	}
	
	@Override
	public boolean contains(Object o) {
		rl.lock();
		try {
			return list.contains(o);
		} finally {
			rl.unlock();
		}
	}
	
	@Override
	public boolean containsAll(Collection<?> list) {
		rl.lock();
		try {
			return list.containsAll(list);
		} finally {
			rl.unlock();
		}
	}
	
	@Override
	public int lastIndexOf(Object o) {
		rl.lock();
		try {
			return list.lastIndexOf(o);
		} finally {
			rl.unlock();
		}
	}
	
	@Override
	public boolean addAll(Collection<? extends T> list) {
		wl.lock();
		try {
			return this.list.addAll(list);
		} finally {
			wl.unlock();
		}
	}
	
	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		wl.lock();
		try {
			return list.addAll(index, c);
		} finally {
			wl.unlock();
		}
	}
	
	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		rl.lock();
		try {
			return new L2SyncList<>(list.subList(fromIndex, toIndex));
		} finally {
			rl.unlock();
		}
	}
	
	@Override
	public void clear() {
		wl.lock();
		try {
			list.clear();
		} finally {
			wl.unlock();
		}
	}
	
	@Override
	public int size() {
		rl.lock();
		try {
			return list.size();
		} finally {
			rl.unlock();
		}
	}
	
	@Override
	public boolean isEmpty() {
		rl.lock();
		try {
			return list.isEmpty();
		} finally {
			rl.unlock();
		}
	}
	
	/**
	 * <FONT color="#FF0000">WARNING: Unsupported</FONT>
	 */
	@Override
	public ListIterator<T> listIterator() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * <FONT color="#FF0000">WARNING: Unsupported</FONT>
	 *
	 * @see List#listIterator(int)
	 */
	@Override
	public ListIterator<T> listIterator(int index) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * <FONT color="#FF0000">WARNING: Returned iterator use cloned List</FONT>
	 *
	 * @see List#iterator()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Iterator<T> iterator() {
		return new Itr((T[]) list.toArray());
	}
	
	private class Itr implements Iterator<T> {
		int cursor; // index of next element to return
		int lastRet = -1; // index of last element returned; -1 if no such
		int size;
		T[] elementData;
		
		public Itr(T[] data) {
			elementData = data;
			if (data != null) {
				size = data.length;
			} else {
				size = 0;
			}
		}
		
		@Override
		public boolean hasNext() {
			return cursor != size;
		}
		
		@Override
		public T next() {
			int i = cursor;
			if (i >= size) {
				throw new NoSuchElementException();
			}
			cursor = i + 1;
			lastRet = i;
			return elementData[lastRet];
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	@Override
	public Object[] toArray() {
		rl.lock();
		try {
			return list.toArray();
		} finally {
			rl.unlock();
		}
	}
	
	@Override
	@SuppressWarnings("hiding")
	public <T> T[] toArray(T[] a) {
		rl.lock();
		try {
			return list.toArray(a);
		} finally {
			rl.unlock();
		}
	}
}
