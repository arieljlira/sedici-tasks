package ar.edu.unlp.sedici.tasks.queues;
import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.NoSuchElementException;


public class EmptyQueue <T> extends AbstractQueue<T>{
	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {

			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public T next() {
				throw new NoSuchElementException();
			}

			@Override
			public void remove() {
				throw new NoSuchElementException();
			}
		};
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public boolean offer(T e) {
		return false;
	}

	@Override
	public T peek() {
		return null;
	}

	@Override
	public T poll() {
		return null;
	}
		
}