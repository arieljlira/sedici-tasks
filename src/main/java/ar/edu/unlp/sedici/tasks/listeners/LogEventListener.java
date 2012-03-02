package ar.edu.unlp.sedici.tasks.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ar.edu.unlp.sedici.tasks.api.TaskEventListener;
import ar.edu.unlp.sedici.tasks.api.Task;
import ar.edu.unlp.sedici.tasks.api.Worker;

public class LogEventListener<T extends Task> implements TaskEventListener<T> {
	protected Logger log = LoggerFactory.getLogger(this.getClass());

	public LogEventListener() {
	}
	
	@Override
	public void preExecute(T task, Worker<T> worker) {
		log.info("afterTaskExecute (task={}, worker={})", new Object[]{task.getId(), worker});
	}

	@Override
	public void postExecute(T task, Worker<T> worker, Exception exception) {
		log.info("afterTaskExecute (task={}, worker={}, ex={})", new Object[]{task.getId(), worker, exception});
	}

	@Override
	public void end() {
		log.info("droidEnded");
	}

	@Override
	public void init() {
		log.info("drooidStarted");
		
	}

	public void taskAborted(T task) {
		log.info("Task {} aborted", task.getId());
	};
}
