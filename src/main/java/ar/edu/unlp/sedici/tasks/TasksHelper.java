package ar.edu.unlp.sedici.tasks;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import ar.edu.unlp.sedici.tasks.api.ExclusiveElement;
import ar.edu.unlp.sedici.tasks.api.Task;
import ar.edu.unlp.sedici.tasks.api.TaskGenerator;
import ar.edu.unlp.sedici.tasks.api.TaskManager;
import ar.edu.unlp.sedici.tasks.api.WorkerFactory;
import ar.edu.unlp.sedici.tasks.managers.MultithreadedTaskManager;
import ar.edu.unlp.sedici.tasks.queues.ExclusiveTaskQueue;
import ar.edu.unlp.sedici.tasks.queues.LazyQueue;

public class TasksHelper {
	
	//////QUEUE FACTORY METHODS
	public static <T> Queue<T> getQueue(){
		return new ConcurrentLinkedQueue<T>();
	}
	
	public static <T> Queue<T> getQueue(Collection<T> col){
		return new ConcurrentLinkedQueue<T>(col);
	}
	
	public static <T> Queue<T> getLazyQueue(TaskGenerator<T> sqe){
		return new LazyQueue<T>(new ConcurrentLinkedQueue<T>(), sqe);
	}
	
	public static <T extends ExclusiveElement> Queue<T> getExclusiveQueue(TaskGenerator<T> sqe){
		return new LazyQueue<T>(new ExclusiveTaskQueue<T>(), sqe);
	}
	
	////////TASK MANAGER FCTORY METHODS
	public static <T extends Task> TaskManager<T> simpleTaskManager(int nThreads, WorkerFactory<T> workerFactory){
		Queue<T> q = getQueue();
		return simpleTaskManager(nThreads, workerFactory, q);
	}
	public static <T extends Task> TaskManager<T> simpleTaskManager(int nThreads, WorkerFactory<T> workerFactory, Queue<T> queue){
		MultithreadedTaskManager<T> master = new MultithreadedTaskManager<T>(nThreads);
		master.setWorkerFactory(workerFactory);
		master.setTaskQueue(queue);
		return master;
	}
	

	public static void main(String[] args) {

	}
	
}