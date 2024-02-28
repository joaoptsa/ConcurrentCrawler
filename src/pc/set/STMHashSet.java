package pc.set;

import scala.concurrent.stm.Ref;
import scala.concurrent.stm.TArray;
import scala.concurrent.stm.japi.STM;


public class STMHashSet<E> implements ISet<E> {

	private static class Node<T> {
		T value;
		Ref.View<Node<T>> prev = STM.newRef(null);
		Ref.View<Node<T>> next = STM.newRef(null);
	}

	private static final int NUMBER_OF_BUCKETS = 16; // should not be changed
	private final TArray.View<Node<E>> table;
	private final Ref.View<Integer> size;

	/**
	 * Constructor.
	 */

public STMHashSet() {
		table = STM.newTArray(NUMBER_OF_BUCKETS);
		size = STM.newRef(0);
	}

	private int getIndex(E elem) {
		return Math.abs(elem.hashCode() % table.length());
	}

	@Override
	public int size() {
		return size.get();
	}

	@Override
	public boolean contains(E elem) {
		if (elem == null) {
			throw new IllegalArgumentException();
		}
		try {
		return STM.atomic(() -> {
			Node<E> curr = table.apply(getIndex(elem));
			while (curr != null) {
				if (elem.equals(curr)) {
					return true;
				}
				curr = curr.next.get();
			}
			return false;
		});
		}
		catch (Exception e) {
			return false;
		} 

	}

	@Override
	public boolean add(E elem) {
		if (elem == null) {
			throw new IllegalArgumentException();
		}
		try{return STM.atomic(() -> {

			Node<E> curr = table.apply(getIndex(elem));
			Node<E> newN = new Node<E>();
			if (curr != null)
				curr.prev.set(newN);
			newN.next.set(curr);
			table.update(getIndex(elem), newN);
			STM.increment(size, 1);
			return true;
		});}
		catch(Exception e) {
		return false;
		}
	}

	@Override
	public boolean remove(E elem) {
		if (elem == null) {
			throw new IllegalArgumentException();
		}

		try{
			return STM.atomic(() -> {
			
			Node<E> curr = table.apply(getIndex(elem));
			while (curr != null) {
				if (elem.equals(curr.value)) {

					Node<E> next = curr.next.get();
					Node<E> prev = curr.prev.get();

					if (prev == null && next == null)
						table.update(getIndex(elem), next);

					if (prev != null)
						prev.next.set(next);

					if (next != null)
						next.prev.set(prev);

					STM.increment(size, -1);
					return true;
				}
				curr = curr.next.get();
			}
			return false;
		});}
		catch(Exception e) {
			return false;
		}
		
	//}

}
	}
	//}
  
	