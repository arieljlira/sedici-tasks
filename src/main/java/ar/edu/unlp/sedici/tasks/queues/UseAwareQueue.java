package ar.edu.unlp.sedici.tasks.queues;

import java.util.Queue;

public interface UseAwareQueue<E> extends Queue<E>{
	/**
	 * Indicates that previously requested Element e is
	 * not under use anymore and can be discarded if
	 * necessary
	 */
	void release(E e);
}
