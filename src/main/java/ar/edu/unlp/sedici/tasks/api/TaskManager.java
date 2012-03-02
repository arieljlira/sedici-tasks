package ar.edu.unlp.sedici.tasks.api;

import java.util.Collection;


public interface TaskManager<T extends Task> {

	void addTasks(Collection<T> tasks);
	void addListener(TaskEventListener<T> listener);
//	void setExceptionHandler(TaskExceptionHandler<T> exHandler);
	
	void start();
	boolean kill(long timeout);
	boolean stop(long timeout);
	boolean await(long timeout);

	boolean isReady();
	boolean isRunning();
	boolean isTerminated();
	boolean isTerminating();
	DroidReport<T> report();
	
	
}
