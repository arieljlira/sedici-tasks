package ar.edu.unlp.sedici.tasks.queues;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

import ar.edu.unlp.sedici.tasks.api.ExclusiveElement;


/**
 * Exclusive Queue with FIFO order between elements of the same subject
 */
public class ExclusiveTaskQueue<T extends ExclusiveElement> extends AbstractQueue<T> implements UseAwareQueue<T>{

	private final ReentrantLock lock = new ReentrantLock();
	private Queue<T> tasksReadyQueue;
	private Map<String, List<T>> tasksPendingMap;
	private Map<String, T> busySubjectsMap;

	public ExclusiveTaskQueue() {
		this.tasksReadyQueue = new ConcurrentLinkedQueue<T>();
		this.tasksPendingMap = new ConcurrentHashMap<String, List<T>>();
		this.busySubjectsMap = new ConcurrentHashMap<String, T>();
	}

	
	@Override
	public Iterator<T> iterator() {
		return toList().iterator();
	}

	private List<T> toList(){
		lock.lock();
		try {
			List<T> elements = new LinkedList<T>();
			elements.addAll(tasksReadyQueue);
			for (List<T> l : tasksPendingMap.values()) {
				elements.addAll(l);
			}
			return elements;
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	public int size() {
		lock.lock();
		try {
			return tasksReadyQueue.size() + tasksPendingMap.size();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean offer(T e) {
		lock.lock();
		try {
			String subject = e.getSubject();

			// Si esta en ejecucion o en cola una tarea del mismo subject, la
			// dejo en pending, sino la agrego a la cola.
			if (this.busySubjectsMap.containsKey(subject))
				this.addPendingTask(e);
			else
				this.addReadyTask(e);

			return true;
			
		} finally {
			lock.unlock();
		}
		
	}

	@Override
	public T peek() {
		lock.lock();
		try {
			return tasksReadyQueue.peek();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public T poll() {
		lock.lock();
		try {
			T task = tasksReadyQueue.poll();
			return task;
		} finally {
			lock.unlock();
		}
	}
	
	

	// ------------------------------------------------------
	// ------------------------------------------------------

	public void release(T e) {
		// ha completado la ejecucion de task en el worker
		lock.lock();
		try {
			String subject = e.getSubject();
			this.busySubjectsMap.remove(subject);

			// Agrega a la cola una de las tareas que estaban en espera 
			T newTask = this.getPendingTask(subject);
			if (newTask != null)
				this.addReadyTask(newTask);
			
		} finally {
			lock.unlock();
		}
	}
	


	// ------------------------------------------------------
	// ------------------------------------------------------


	private void addReadyTask(T task) {
		this.tasksReadyQueue.add(task);
		this.busySubjectsMap.put(task.getSubject(), task);
	}

	private boolean addPendingTask(T task) {
		List<T> taskList = this.tasksPendingMap.get(task.getSubject());
		if (taskList == null) {
			taskList = new LinkedList<T>();
			this.tasksPendingMap.put(task.getSubject(), taskList);
		}
		return taskList.add(task);
	}

	private T getPendingTask(String subject) {
		T nextPendingTask = null;
		List<T> taskList = this.tasksPendingMap.get(subject);

		if (taskList != null && !taskList.isEmpty()) {
			nextPendingTask = taskList.remove(0);
		}

		return nextPendingTask;
	}


	
}
