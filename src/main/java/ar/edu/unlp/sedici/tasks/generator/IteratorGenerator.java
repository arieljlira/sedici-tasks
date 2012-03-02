package ar.edu.unlp.sedici.tasks.generator;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import ar.edu.unlp.sedici.tasks.api.TaskGenerator;

public class IteratorGenerator<E> implements TaskGenerator<E> {

	private Iterator<E> iterator;
	private int size = 0;

	public IteratorGenerator(Collection<E> collection) {
		this.iterator = collection.iterator();
		this.size = collection.size();
	}
	
	public IteratorGenerator(Iterable<E> iterable) {
		this.iterator = iterable.iterator();
	}
	
	@Override
	public E next() {
		try{
			return iterator.next();
		}catch(NoSuchElementException e){
			return null;
		}
	}

	@Override
	public int size() {
		return size ;
	}

}
