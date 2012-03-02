package ar.edu.unlp.sedici.tasks.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ar.edu.unlp.sedici.tasks.api.TaskEventListener;
import ar.edu.unlp.sedici.tasks.api.Task;
import ar.edu.unlp.sedici.tasks.api.Worker;

public abstract class AbstractListener<T extends Task> implements TaskEventListener<T> {

	protected Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public void postExecute(T task, Worker<T> worker, Exception exception) {
	}

	@Override
	public void end() {
	}

	@Override
	public void init() {
	}

	@Override
	public void preExecute(T task, Worker<T> worker) {
	}

	public void taskAborted(T task) {};

}
