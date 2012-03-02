package ar.edu.unlp.sedici.tasks.api;

public interface TaskEventListener<T extends Task> {

	void init();
	
	void preExecute(T task, Worker<T> worker);
	
	void taskAborted(T task);
	
	void postExecute(T task, Worker<T> worker, Exception exception);
	
	void end();
	
}
