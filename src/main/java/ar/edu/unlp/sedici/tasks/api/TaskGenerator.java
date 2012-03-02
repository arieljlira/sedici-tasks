package ar.edu.unlp.sedici.tasks.api;

public interface TaskGenerator<T> {

	public T next();

	/**
	 * How many elements do we have left. Note: depending on
	 * the queue implementation, this number would be approximate
	 * 
	 * @return number of elements we have left.
	 */
	public int size();

}
