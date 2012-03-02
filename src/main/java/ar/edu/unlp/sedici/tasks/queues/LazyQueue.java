package ar.edu.unlp.sedici.tasks.queues;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

import ar.edu.unlp.sedici.tasks.api.TaskGenerator;
import ar.edu.unlp.sedici.tasks.generator.IteratorGenerator;


public class LazyQueue<E> extends FilteredQueue<E>{

	private TaskGenerator<E> sequence;
	private int copySliceSize;
	
	public LazyQueue(Queue<E> queue, Iterable<E> iterable) {
		this(queue, new IteratorGenerator<E>(iterable), 10);
	}
	
	public LazyQueue(Queue<E> queue, TaskGenerator<E> sequence) {
		this(queue, sequence, 10);
	}
	
	public LazyQueue(Queue<E> queue, TaskGenerator<E> sequence, int copySliceSize) {
		super(queue);
		this.sequence = sequence;
		if (copySliceSize <= 0 )
			throw new IllegalArgumentException("copySliceSize must be greater than zero");
		this.copySliceSize = copySliceSize;
	}

	@Override
	public int size() {
		return super.size() + sequence.size();
	}

	@Override
	public Iterator<E> iterator() {
		final Iterator<E> iterator2 = super.iterator();
		
		return new Iterator<E>() {
			
			private Iterator<E> auxIt = iterator2;
			private E aux;
			
			@Override
			public boolean hasNext() {
				if (auxIt != null && auxIt.hasNext())
					return true;
				
				if (aux == null)
					aux = sequence.next();
				
				return (aux != null);
			}

			@Override
			public E next() {
				
				if (auxIt != null && auxIt.hasNext())
					return auxIt.next();
				else if (auxIt != null)
					auxIt = null;
				
				if (aux != null){
					E res = aux;
					aux = null;
					return res;
				}else
					return 	sequence.next();
				
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("remove not supported");
			}
			
			
		};
	}

    /**
     * Retrieves and removes the head of this queue,
     * or returns <tt>null</tt> if this queue is empty.
     *
     * @return the head of this queue, or <tt>null</tt> if this queue is empty
     */
    public E poll() {
		E e = super.poll();
		if (e == null){
			if (repopulateQueue())
				e = this.poll();
		}
		return e;
	}
    

    /**
     * Retrieves, but does not remove, the head of this queue,
     * or returns <tt>null</tt> if this queue is empty.
     *
     * @return the head of this queue, or <tt>null</tt> if this queue is empty
     */
    public E peek() {
    	E e = super.peek();
		if (e == null){
			if (repopulateQueue())
				e = this.peek();
		}
		return e;
	}
    
    /**
     * 
     * @return true if at least one element has been copied from the sequence into the queue
     */
    private boolean repopulateQueue(){
    	int copied = 0;
    	while(copied < this.copySliceSize){
    		E e = sequence.next();
    		if (e == null)
    			break;
    		try{
	    		if (super.add(e))
	    			copied++;
    		}catch(IllegalStateException ex){
    			break;
    		}
    	}
    	return (copied > 0);
    }
    
    
    /**
     * Retrieves and removes the head of this queue.  This method differs
     * from {@link #poll poll} only in that it throws an exception if this
     * queue is empty.
     *
     * @return the head of this queue
     * @throws NoSuchElementException if this queue is empty
     */
	public E remove() {
        E x = poll();
        if (x != null)
            return x;
        else
            throw new NoSuchElementException();
    }

    /**
     * Retrieves, but does not remove, the head of this queue.  This method
     * differs from {@link #peek peek} only in that it throws an exception
     * if this queue is empty.
     *
     * @return the head of this queue
     * @throws NoSuchElementException if this queue is empty
     */
    public E element() {
        E x = peek();
        if (x != null)
            return x;
        else
            throw new NoSuchElementException();
    }

}
