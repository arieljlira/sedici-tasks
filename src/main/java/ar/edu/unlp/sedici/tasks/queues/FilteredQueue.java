package ar.edu.unlp.sedici.tasks.queues;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

public class FilteredQueue<E> implements Queue<E> ,UseAwareQueue<E> {

	private Queue<E> queue;

	public FilteredQueue(Queue<E> master){
		this.queue = master;
	}
	
	@Override
	public boolean add(E e) {
		return queue.add(e);
	}

	@Override
	public E element() {
		return queue.element();
	}

	@Override
	public boolean offer(E e) {
		return queue.offer(e);
	}

	@Override
	public E peek() {
		return queue.peek();
	}

	@Override
	public E poll() {
		return queue.poll();
	}

	@Override
	public E remove() {
		return queue.remove();
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return queue.addAll(c);
	}

	@Override
	public void clear() {
		queue.clear();
	}

	@Override
	public boolean contains(Object o) {
		return queue.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return queue.containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return queue.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return queue.iterator();
	}

	@Override
	public boolean remove(Object o) {
		return queue.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return queue.remove(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return queue.retainAll(c);
	}

	@Override
	public int size() {
		return queue.size();
	}

	@Override
	public Object[] toArray() {
		return queue.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return queue.toArray(a);
	}


    @SuppressWarnings("unchecked")
	public void release(E e) {
    	if (this.queue instanceof UseAwareQueue)
    		((UseAwareQueue<E>)this.queue).release(e);
    };
}
