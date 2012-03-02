package ar.edu.unlp.sedici.tasks.queues;

import java.util.AbstractQueue;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public class UnboundedDiscardingQueue<T> extends AbstractQueue<T>{
	private int maxAllowed;
	
	private Deque<T> internal;
	public UnboundedDiscardingQueue(int maxAllowed) {
		this.maxAllowed = maxAllowed;
		this.internal = new ArrayDeque<T>(maxAllowed);
	}
	
	@Override
	public Iterator<T> iterator() {
		return internal.iterator();
	}

	@Override
	public int size() {
		return internal.size();
	}

	@Override
	public boolean offer(T e) {
		if (internal.size() >= maxAllowed)
			internal.removeLast();
		internal.addFirst(e);
		return true;
	}

	@Override
	public T peek() {
		return internal.peek();
		
	}

	@Override
	public T poll() {
		return internal.poll();
	}
	

}
