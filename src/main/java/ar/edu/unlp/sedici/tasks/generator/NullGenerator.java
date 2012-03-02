package ar.edu.unlp.sedici.tasks.generator;

import ar.edu.unlp.sedici.tasks.api.TaskGenerator;

/**
 * task provider que no provee nada
 * 
 * @author ariel
 * 
 * @param <T>
 */
public class NullGenerator<T> implements TaskGenerator<T> {

	public T next() {
		return null;
	}

	public boolean hasNext() {
		return false;
	}

	public int size() {
		return 0;
	}

}