/*
 * $Header: BitSetIDFactory.java, 24/08/2005 22:44:26 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 24/08/2005 22:44:26 $
 * $Revision: 1 $
 * $Log: BitSetIDFactory.java,v $
 * Revision 1  24/08/2005 22:44:26  luisantonioa
 * Added copyright notice
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

package l2server.gameserver.idfactory;

import l2server.gameserver.ThreadPoolManager;
import l2server.util.PrimeFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class ..
 *
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */

public class BitSetIDFactory extends IdFactory {
	private static Logger log = LoggerFactory.getLogger(BitSetIDFactory.class.getName());
	
	private BitSet freeIds;
	private AtomicInteger freeIdCount;
	private AtomicInteger nextFreeId;
	
	protected class BitSetCapacityCheck implements Runnable {
		@Override
		public void run() {
			synchronized (BitSetIDFactory.this) {
				if (reachingBitSetCapacity()) {
					increaseBitSetCapacity();
				}
			}
		}
	}
	
	protected BitSetIDFactory() {
		super();
		
		synchronized (BitSetIDFactory.class) {
			ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new BitSetCapacityCheck(), 30000, 30000);
			initialize();
		}
        log.info(freeIds.size() + " ids available.");
	}
	
	public void initialize() {
		try {
			freeIds = new BitSet(PrimeFinder.nextPrime(100000));
			freeIds.clear();
			freeIdCount = new AtomicInteger(FREE_OBJECT_ID_SIZE);
			
			for (int usedObjectId : extractUsedObjectIDTable()) {
				int objectID = usedObjectId - FIRST_OID;
				if (objectID < 0 || usedObjectId < FIRST_OID) {
					log.warn("Object ID " + usedObjectId + " in DB is less than minimum ID of " + FIRST_OID);
					continue;
				}
				freeIds.set(usedObjectId - FIRST_OID);
				freeIdCount.decrementAndGet();
			}
			
			nextFreeId = new AtomicInteger(freeIds.nextClearBit(0));
			initialized = true;
		} catch (Exception e) {
			initialized = false;
			log.error("BitSet ID Factory could not be initialized correctly: " + e.getMessage(), e);
		}
	}
	
	/**
	 * @see IdFactory#releaseId(int)
	 */
	@Override
	public synchronized void releaseId(int objectID) {
		if (objectID - FIRST_OID > -1) {
			freeIds.clear(objectID - FIRST_OID);
			freeIdCount.incrementAndGet();
		} else {
			log.warn("BitSet ID Factory: release objectID " + objectID + " failed (< " + FIRST_OID + ")");
		}
	}
	
	/**
	 * @see IdFactory#getNextId()
	 */
	@Override
	public synchronized int getNextId() {
		int newID = nextFreeId.get();
		freeIds.set(newID);
		freeIdCount.decrementAndGet();
		
		int nextFree = freeIds.nextClearBit(newID);
		
		if (nextFree < 0) {
			nextFree = freeIds.nextClearBit(0);
		}
		if (nextFree < 0) {
			if (freeIds.size() < FREE_OBJECT_ID_SIZE) {
				increaseBitSetCapacity();
				nextFree = freeIds.nextClearBit(0);
			} else {
				throw new NullPointerException("Ran out of valid Id's.");
			}
		}
		
		nextFreeId.set(nextFree);
		
		return newID + FIRST_OID;
	}
	
	/**
	 * @see IdFactory#size()
	 */
	@Override
	public synchronized int size() {
		return freeIdCount.get();
	}
	
	protected synchronized int usedIdCount() {
		return size() - FIRST_OID;
	}
	
	protected synchronized boolean reachingBitSetCapacity() {
		return PrimeFinder.nextPrime(usedIdCount() * 11 / 10) > freeIds.size();
	}
	
	protected synchronized void increaseBitSetCapacity() {
		BitSet newBitSet = new BitSet(PrimeFinder.nextPrime(usedIdCount() * 11 / 10));
		newBitSet.or(freeIds);
		freeIds = newBitSet;
	}
}
