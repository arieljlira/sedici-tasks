package ar.edu.unlp.sedici.tasks.api;

public interface AbstractTaskFactory<T extends Task> extends WorkerFactory<T> {
	String getCanonicalName();
	long getMaxAwaitTime();
	
	TaskManager<T> newTaskManager();
	
	Worker<T> newWorker(T task);
}
