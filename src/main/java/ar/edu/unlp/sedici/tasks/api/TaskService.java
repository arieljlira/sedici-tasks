package ar.edu.unlp.sedici.tasks.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase encargada de controlar la ejecucion de los Taskmanagers 
 */
public class TaskService {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	private Map<String, AbstractTaskFactory<?>> managerFactories;
	private Map<String, TaskManager<?>> managers;

	public enum TaskServiceOrder{ START, STOP, KILL, STATUS}
	public TaskService() {
		log.trace("Se inicializa el TaskService");
		this.managers = Collections.synchronizedMap(new HashMap<String, TaskManager<?>>());
		this.managerFactories = Collections.synchronizedMap(new HashMap<String, AbstractTaskFactory<?>>());
	}

	public void registerTaskManagerFactory(AbstractTaskFactory<?> tmf){
		this.managerFactories.put(tmf.getCanonicalName(), tmf);
	}

	public void unregisterTaskManagerFactory(AbstractTaskFactory<?> tmf){
		this.managerFactories.remove(tmf.getCanonicalName());
	}
	
	public TaskManager<?> getTaskManager(String taskManagerCanonicalName) {
		if (!managers.containsKey(taskManagerCanonicalName)){
			AbstractTaskFactory<?> tmf = getTaskManagerFactory(taskManagerCanonicalName);
			TaskManager<?> mgr = tmf.newTaskManager(); 
			if (mgr == null)
				throw new IllegalStateException("A newly created taskManager (with name " + taskManagerCanonicalName + ") cannot be null");
			
			managers.put(taskManagerCanonicalName, mgr);	
		}
		
		return managers.get(taskManagerCanonicalName);
	}

	private AbstractTaskFactory<?> getTaskManagerFactory(String taskManagerCanonicalName) {
		AbstractTaskFactory<?> tmf = managerFactories.get(taskManagerCanonicalName);
		if (tmf == null)
			throw new IllegalArgumentException("Unknown TaskManager named " + taskManagerCanonicalName);
		return tmf;
	}
	
	public void destory() {
		log.info("Se recibe la orden de DESTROY desde spring, se frenan todos los droids en ejecucion");
		// se crea una copia del keySet para que no surja un ConcurrentModificationException
		for (String tmName : new HashSet<String>(managers.keySet())) {
			boolean isDead = managers.get(tmName).kill(100);
			log.info("Se envia la senal de kill al taskMaster '{}' (ya termino = {})", tmName, isDead);
		}

	}
	

	public DroidReport<?> sendOrder(String taskManagerCanonicalName, TaskServiceOrder order) {
		TaskManager<?> manager = this.getTaskManager(taskManagerCanonicalName);
		AbstractTaskFactory<?> factory = this.getTaskManagerFactory(taskManagerCanonicalName);
		log.trace("Se recibe la orden {} para el taskManager {}", order, taskManagerCanonicalName);
		
		switch (order) {
			case START:
				if (manager.isTerminated()){
					//piso el manager viejo porque esta terminado
					this.managers.put(factory.getCanonicalName(), factory.newTaskManager());
					manager = this.getTaskManager(factory.getCanonicalName());
				}
				if (manager.isReady())
					manager.start();
				else{
					//no-op, this manager was already running
				}
				break;
			case STOP:
				if (!manager.isTerminated())
					manager.stop(factory.getMaxAwaitTime());
				else{
					//no-op, this manager was already stoped
				}
				break;
			case KILL:
				if (!manager.isTerminated())
					manager.kill(factory.getMaxAwaitTime());
				else{
					//no-op, this manager was already stoped
				}
				break;
			default:
				break;
		}
		
		return manager.report();
	}

	public void broadcastOrder(TaskServiceOrder order) {
		log.info("Se recibe la orden {} para broadcast sobre todos los managers", order);

		for (String tmName : this.managers.keySet()) {
			this.sendOrder(tmName, order);
			log.info("Se envia la senal de {} al taskMaster '{}'", new Object[]{order, tmName});
		}
	}

}
