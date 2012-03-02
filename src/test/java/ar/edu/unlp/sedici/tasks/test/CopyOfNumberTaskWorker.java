package ar.edu.unlp.sedici.tasks.test;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ar.edu.unlp.sedici.tasks.api.Worker;
import ar.edu.unlp.sedici.tasks.exception.StopExecutionException;

public class CopyOfNumberTaskWorker implements Worker<CopyOfNumberTask> {

	private CopyOfNumberTask task;
	private static Logger log = LoggerFactory.getLogger(CopyOfNumberTaskWorker.class);
	

	public CopyOfNumberTaskWorker(CopyOfNumberTask  task) {
		log.info("Se crea el worker para tarea {}", task.getId());
		this.task = task;
	}
	
	@Override
	public void execute() throws StopExecutionException {
		int[] ns = task.getNumbers();
		int sum = 0;
		for (int i = 0; i < ns.length; i++) {
			sum += ns[i];
		}
		try {
			Thread.sleep(new Random().nextInt(1500));
		} catch (InterruptedException e) {
			
		}
		log.info("Tengo la tarea tarea {}, sum = {}", task.getId(), sum);
		if (sum < 0) throw new NullPointerException("pepe");
	}

}
