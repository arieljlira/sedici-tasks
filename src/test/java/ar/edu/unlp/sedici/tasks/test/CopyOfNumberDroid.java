package ar.edu.unlp.sedici.tasks.test;

import java.util.Queue;

import ar.edu.unlp.sedici.tasks.TasksHelper;
import ar.edu.unlp.sedici.tasks.api.TaskEventListener;
import ar.edu.unlp.sedici.tasks.api.TaskGenerator;
import ar.edu.unlp.sedici.tasks.api.TaskManager;
import ar.edu.unlp.sedici.tasks.api.Worker;
import ar.edu.unlp.sedici.tasks.api.WorkerFactory;
import ar.edu.unlp.sedici.tasks.listeners.LogEventListener;

public class CopyOfNumberDroid{
	
	public static void main(String[] args) {
		int nThreads = 9;
		
		WorkerFactory<CopyOfNumberTask> workerFactory = new WorkerFactory<CopyOfNumberTask>() {
			@Override
			public Worker<CopyOfNumberTask> newWorker(CopyOfNumberTask task) {
				return new CopyOfNumberTaskWorker(task);
			}
		};

		TaskGenerator<CopyOfNumberTask> provider = new CopyOfNumberDroidTaskProvider(20);
		Queue<CopyOfNumberTask> q = TasksHelper.getExclusiveQueue(provider);
		TaskManager<CopyOfNumberTask> d = TasksHelper.simpleTaskManager(nThreads, workerFactory, q);
		TaskEventListener<CopyOfNumberTask> log_listener = new LogEventListener<CopyOfNumberTask>();
		d.addListener(log_listener);
		
		
		d.start();
		while(!d.isTerminated()){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.out.println("El thread fue interrumpido");
				break;
			}
		}
		System.out.println("Resultado de la ejecucion con" + nThreads + " threads");
		System.out.println(d.report());
		

	}
}
