package pc.set;

import java.util.LinkedList;
import java.util.concurrent.locks.*;



public class LHashSet3<E> implements ISet<E> {

	private final ReentrantReadWriteLock locksArray[];
	private static final int NUMBER_OF_BUCKETS = 16; // should not be changed
	private LinkedList<E>[] table;

	/**
	 * Constructor.
	 */
	@SuppressWarnings("unchecked")
	public LHashSet3() {
		locksArray = new ReentrantReadWriteLock[NUMBER_OF_BUCKETS];
		table = (LinkedList<E>[]) new LinkedList[NUMBER_OF_BUCKETS];
		for (int i = 0; i < table.length; i++) {
			table[i] = new LinkedList<>();
			locksArray[i] = new ReentrantReadWriteLock();
		}

	}

	@Override
	public int size() {
		int size = 0;
		for (int i = 0; i < NUMBER_OF_BUCKETS; i++) {
			locksArray[i].readLock().lock();
		}

		try {
			for (int i = 0; i < NUMBER_OF_BUCKETS; i++)
				size += table[i].size();
			return size;
		} finally {
			for (int i = 0; i < NUMBER_OF_BUCKETS; i++)
				locksArray[i].readLock().unlock();

		}
	}

	// get List
	private LinkedList<E> getEntry(E elem) {
		return table[Math.abs(elem.hashCode() % table.length)];
	}

	// get Index
	private int getIndexLinked(E elem) {
		return (Math.abs(elem.hashCode() % table.length));
	}

	@Override
	public boolean add(E elem) {
		if (elem == null) {
			throw new IllegalArgumentException();
		}

		locksArray[getIndexLinked(elem)].writeLock().lock();
		try {
			LinkedList<E> list = getEntry(elem);
			boolean r = !list.contains(elem);
			if (r) {
				list.addFirst(elem);
			}
			return r;
		} finally {
			locksArray[getIndexLinked(elem)].writeLock().unlock();
		}
	}

	@Override
	public boolean remove(E elem) {
		if (elem == null) {
			throw new IllegalArgumentException();
		}
		locksArray[getIndexLinked(elem)].writeLock().lock();
		try {
			boolean r = getEntry(elem).remove(elem);
			return r;
		} finally {
			locksArray[getIndexLinked(elem)].writeLock().unlock();
		}
	}

	@Override
	public boolean contains(E elem) {
		if (elem == null) {
			throw new IllegalArgumentException();
		}
		locksArray[getIndexLinked(elem)].writeLock().lock();
		try {
			return getEntry(elem).contains(elem);
		} finally {
			locksArray[getIndexLinked(elem)].writeLock().unlock();
		}
	}
}
    
