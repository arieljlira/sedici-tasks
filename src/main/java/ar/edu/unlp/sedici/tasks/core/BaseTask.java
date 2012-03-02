package ar.edu.unlp.sedici.tasks.core;

import java.util.Date;

import ar.edu.unlp.sedici.tasks.api.Task;
import ar.edu.unlp.sedici.tasks.exception.InvalidTaskException;

public abstract class BaseTask implements Task {
	
	private static final long serialVersionUID = 1L;
	private Date createdAt;
	
	
	public BaseTask() {
		this.createdAt = new Date();
	}
	
	@Override
	public abstract String getId() ;

	public Date getCreatedAt() {
		return createdAt;
	}

	@Override
	public String toString() {
		return "Task["+this.getId()+"]";
	}
	
	/*
	 * Implementacion por default que retorna true
	 */
	@Override
	public boolean prepare() throws InvalidTaskException {
		return true;
	}

	/*
	 * Implementacion por default que no hace nada
	 */
	@Override 
	public void close() { 
		
	}
}
