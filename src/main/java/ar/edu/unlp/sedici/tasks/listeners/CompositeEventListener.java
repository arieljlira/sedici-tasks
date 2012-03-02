package ar.edu.unlp.sedici.tasks.listeners;

import java.util.LinkedList;
import java.util.List;

import ar.edu.unlp.sedici.tasks.api.TaskEventListener;
import ar.edu.unlp.sedici.tasks.api.Task;
import ar.edu.unlp.sedici.tasks.api.Worker;

public class CompositeEventListener<T extends Task> implements TaskEventListener<T> {

	private List<TaskEventListener<T>> listeners;
	
	public CompositeEventListener() {
		this(new LinkedList<TaskEventListener<T>>());
	}
	
	public CompositeEventListener(List<TaskEventListener<T>> listeners) {
		super();
		this.listeners = listeners;
	}
	
	public void addListener(TaskEventListener<T> aListener){
		this.listeners.add(aListener);
	}
	
	@Override
	public void postExecute(T task, Worker<T> worker, Exception exception) {
		for (TaskEventListener<T> listener : listeners) 
			listener.postExecute(task, worker, exception);
	}

	@Override
	public void end() {
		for (TaskEventListener<T> listener : listeners) 
			listener.end();
	}

	@Override
	public void init() {
		for (TaskEventListener<T> listener : listeners) 
			listener.init();
	}

	@Override
	public void preExecute(T task, Worker<T> worker) {
		for (TaskEventListener<T> listener : listeners) 
			listener.preExecute(task, worker);
	}

	public void taskAborted(T task) {
		for (TaskEventListener<T> listener : listeners) 
			listener.taskAborted(task);
	};
}
