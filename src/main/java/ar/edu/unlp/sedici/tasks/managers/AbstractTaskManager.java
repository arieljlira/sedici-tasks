package ar.edu.unlp.sedici.tasks.managers;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ar.edu.unlp.sedici.tasks.api.AbstractTaskFactory;
import ar.edu.unlp.sedici.tasks.api.DroidReport;
import ar.edu.unlp.sedici.tasks.api.Task;
import ar.edu.unlp.sedici.tasks.api.TaskEventListener;
import ar.edu.unlp.sedici.tasks.api.TaskManager;
import ar.edu.unlp.sedici.tasks.api.Worker;
import ar.edu.unlp.sedici.tasks.api.WorkerFactory;
import ar.edu.unlp.sedici.tasks.exception.InvalidTaskException;
import ar.edu.unlp.sedici.tasks.exception.StopExecutionException;
import ar.edu.unlp.sedici.tasks.listeners.CompositeEventListener;
import ar.edu.unlp.sedici.tasks.listeners.ReportListener;
import ar.edu.unlp.sedici.tasks.queues.UseAwareQueue;

public abstract class AbstractTaskManager<T extends Task> implements TaskManager<T> {
	protected Logger log = LoggerFactory.getLogger(this.getClass());

	private enum ExecutionState { NEW, RUNNING, TERMINATING, TERMINATED}
	private ExecutionState state = ExecutionState.NEW;
	private ReentrantLock stateLock = new ReentrantLock ();
	private Condition stateCondition = stateLock.newCondition();
	private Object workingLock = new Object();
	
	private Queue<T> myQueue = new LinkedList<T>();
	protected ExecutorService workerExecutor;
	
	private CompositeEventListener<T> listeners;
	private WorkerFactory<T> workerFactory;
	private ReportListener<T> reportListener;

	/**
	 * Indica si el Manager actual puede seguir aceptando trabajos en este momento o si se debe esperar
	 * @return
	 */
	protected abstract boolean needsMoreJobs();
	
	protected abstract boolean isStillWorking();
	
	public AbstractTaskManager(ExecutorService workerExecutorService) {
		if(workerExecutorService == null)
			throw new NullPointerException();
		
		this.workerExecutor = workerExecutorService;
		this.listeners = new CompositeEventListener<T>();
		this.reportListener = new ReportListener<T>();
		this.listeners.addListener(this.reportListener);
	}
	
	public void setWorkerFactory(WorkerFactory<T> workerFactory) {
		if (workerFactory == null )
			throw new NullPointerException();
		this.workerFactory = workerFactory;
	}
	
	@Override
	public void addTasks(Collection<T> tasks) {
		stateLock.lock();
		try{
			if(!isAlive()){
				throw new IllegalStateException("No se pueden agregar tareas a un manager que no esta vivo");	
			}
			this.myQueue.addAll(tasks);
		}finally{
			stateLock.unlock();
		}
	}
	
	public void setTaskQueue(Queue<T> queue) {
		if(queue == null)
			throw new NullPointerException("queue cannot be null");
		
		stateLock.lock();
		try{
			if (!isAlive())
				throw new IllegalStateException("No se pueden agregar tareas a un manager que no esta vivo");
			//habria que ver si en algun caso conviene agregar el contenido de la cola vieja a la cola nueva, 
			//hay que tener cuidado  porque la cola actual puede ser lazy o algo asi raro. Por ahi una buena
			//opcion seria this.myQueue = new LazyQueue<E>(newQueue, oldQueue);
			this.myQueue = queue; 
		}finally{
			stateLock.unlock();
		}
	}
	
	@Override
	public void addListener(TaskEventListener<T> aListener) {
		if(aListener == null)
			throw new NullPointerException("aListener cannot be null");
		
		this.listeners.addListener(aListener);
	}
	
	private long getDefaultStopTimeout() {
		if(this.workerFactory instanceof AbstractTaskFactory)
			return ((AbstractTaskFactory) this.workerFactory).getMaxAwaitTime();
		
		return 10000;
	}
	
	/////////////////////////////////////////////////////////////////
	
	@Override
	public final void start() {
		setRunningStatus();
		this.listeners.init();
		
		ExecutorService managerExecutor = Executors.newSingleThreadExecutor(new MTMThreadFactory("manager"));
		managerExecutor.execute(new Runnable() {
			public void run() {
				
				while(true){
					while ( !needsMoreJobs() && isRunning() ){ 	
						//sleep (no-op)
					}
					
					if ( !isRunning() ){
						log.info("El taskManager no esta mas en ejecucion, salgo del managerExecutor");
						break;
					}

					T task = myQueue.poll();
					if (task == null){
						
						log.debug("No hay mas trabajos por ahora. Espero que termine algun worker para ver si puedo seguir con un nuevo trabajo");
						synchronized (workingLock) {
							if(isStillWorking()){
								try{
									workingLock.wait(2000);
								} catch (InterruptedException e) {
									// Preserve interrupt status
									Thread.currentThread().interrupt();
									break;
								}
							}else{
								log.info("No hay mas trabajos pendientes ni workers en ejecucion, me retiro");
								break;
							}
						}
						
						
					} else { //task != null
						
						try {
							//Future<?> future  = (por ahora no me sirve para nada el future)
							workerExecutor.submit(new TaskRunner(task));
						} catch(RejectedExecutionException e) {
							if( isTerminating() || isTerminated())
								log.info("El task {} no se puede ejecutar porque se solicito la terminacion del manager", task);
							else
								log.error("No se permite ejecutar el Task {} pero al parecer nadie solicito la terminacion del Manager", task);
						}
					}
				}
				
				// Si el manager aun esta en ejecucion, solicita la terminacion
				if( isRunning() ) 
					stop(0);
			}
		});
		//ya se marca para terminar el managerExecutor porque solo ejecutara un job  
		managerExecutor.shutdown();
	}

	@Override
	public boolean stop(long timeout) {
		stateLock.lock();
		try {
			
			if ( isTerminated() )
				return true;
			
			if ( !isTerminating() ) {
				setTerminatingStatus();
			
				log.debug("Se comienza el proceso de terminacion normal del TaskManager");
				workerExecutor.shutdown(); // Disable new tasks from being submitted
	
				try{
					// Libero el lock para permitir el acceso mientras se espera la terminacion del ExecutorService
					stateLock.unlock(); 
					
					// Se queda esperando
					log.trace("Se comienza la espera de {}ms por la terminacion normal", timeout);
					boolean managerTerminated = workerExecutor.awaitTermination(timeout, TimeUnit.MILLISECONDS);

					// Retomo el lock
					stateLock.lock(); 
					
					// Si se logro la terminacion, marca el manager como TERMINATED
					if( managerTerminated ) {
						setTerminatedStatus();
					} else if(timeout > 0) {
						log.warn("La solicitud de terminacion no se pudo completar ya que se alcanzo el timeout, establecido en {}ms", timeout);
					}
					
					// Despierta a todos los que estan esperando la terminacion
					log.trace("Despierto a los otros que estan esperando la terminacion");
					stateCondition.signalAll();
					
				} catch (InterruptedException ie) {	// Preserve interrupt status
					Thread.currentThread().interrupt();
				}
			} else {
				try {
					log.trace("La solicitud de terminacion ya fue invocada. Me quedo esperando a que termine");
					stateCondition.await(timeout, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {	// Preserve interrupt status
					Thread.currentThread().interrupt();
				}
			}
	
			// Retorna el estado final del Manager
			return isTerminated();
			
		} finally {
			stateLock.unlock();
		}
	}

	@Override
	public boolean await(long timeout) {
		stateLock.lock();
		try{
			if ( isTerminated() )
				return true;
		
			log.info("Se comienza la espera de {}ms por la terminacion del TaskManager", timeout);
			try{	
				workerExecutor.awaitTermination(timeout, TimeUnit.MILLISECONDS);
			} catch (InterruptedException ie) {
				// Preserve interrupt status
				Thread.currentThread().interrupt();
			}
			log.info("WorkerExecutor.isTerminated()==" + workerExecutor.isTerminated());
			
			return isTerminated();
		}finally{
			stateLock.unlock();
		}
	}
	
	@Override
	public boolean kill(long timeout) {
		stateLock.lock();
		try {
			if ( isTerminated() )
				return true;
	
			// Si no esta TERMINATING, primero probamos con un stop() normal
			boolean normalTerminationSucceded = false;
			if ( !isTerminating() ) {
				// Libero el lock para permitir el acceso mientras se espera la terminacion del ExecutorService
				stateLock.unlock();
				
				log.trace("Antes de iniciar la Terminacion Forzada, se intenta con una terminacion normal");
				normalTerminationSucceded = stop( getDefaultStopTimeout() );

				// Retomo el lock
				stateLock.lock(); 
			} else {
				log.info("Haciendo KILL sobre un manager en proceso de terminacion");
			}
			 
			if( !normalTerminationSucceded ) {
				log.debug("Se comienza el proceso de terminacion forzada (KILL) del TaskManager");
				workerExecutor.shutdownNow(); // Cancel currently executing tasks
		
				try{
					// Libero el lock para permitir el acceso mientras se espera la terminacion del ExecutorService
					stateLock.unlock();
					
					// Se queda esperando
					log.trace("Se comienza la espera de {}ms para la terminacion forzada", timeout);
					boolean managerTerminated = workerExecutor.awaitTermination(timeout, TimeUnit.MILLISECONDS);
		
					// Retomo el lock
					stateLock.lock(); 
					
					// Si se logro la terminacion, marca el manager como TERMINATED
					if( managerTerminated ) {
						setTerminatedStatus();
					} else if(timeout > 0) {
						log.warn("La solicitud de terminacion forzada no se pudo completar ya que se alcanzo el timeout, establecido en {}ms", timeout);
					}
					
					// Despiera a todos los que estan esperando la terminacion
					log.trace("Despierto a los otros que estan esperando la terminacion");
					stateCondition.signalAll();
					
				} catch (InterruptedException ie) {	
					// (Re-)Cancel if current thread also interrupted
					log.warn("InterruptedException durante await en proceso de terminacion forzada (KILL). Se reintenta el shutdownNow()");
					workerExecutor.shutdownNow();
					
					// Preserve interrupt status
					Thread.currentThread().interrupt();
				}
			}
			
			// Retorna el estado final del Manager
			return isTerminated();
			
		} finally {
			stateLock.unlock();
		}
	}

	/////////////////////////////////////////////////////////////////

	private boolean isAlive() {
		stateLock.lock();
		try{
			return ExecutionState.NEW.equals(this.state) || ExecutionState.RUNNING.equals(this.state);
		}finally{
			stateLock.unlock();	
		}
	}
	
	@Override
	public boolean isReady() {
		stateLock.lock();
		try{
			return ExecutionState.NEW.equals(this.state);
		}finally{
			stateLock.unlock();	
		}
	}

	@Override
	public boolean isRunning(){
		stateLock.lock();
		try{
			return ExecutionState.RUNNING.equals(this.state);
		}finally{
			stateLock.unlock();	
		}
	}

	@Override
	public boolean isTerminating() {
		stateLock.lock();
		try {
			return ExecutionState.TERMINATING.equals(this.state);
		} finally {
			stateLock.unlock();	
		}
	}

	@Override
	public boolean isTerminated() {
		stateLock.lock();
		try {
			return ExecutionState.TERMINATED.equals(this.state);
		} finally {
			stateLock.unlock();	
		}
	}
	
	private void setRunningStatus() {
		stateLock.lock();
		try{
			if ( !isReady() )
				throw new IllegalStateException("No se puede invocar a start() mas de una vez");
			
			if (this.workerFactory == null)
				throw new IllegalStateException("No se puede invocar a start() sin haber seteado el workerFactory");
			
			state = ExecutionState.RUNNING;
		}finally{
			stateLock.unlock();
		}
	}
	
	public void setTerminatedStatus() {
		stateLock.lock();
		try {
			if( !isTerminated() ) {
				log.info("El manager ha finalizado");
				this.state = ExecutionState.TERMINATED;
				this.listeners.end();
			}
		} finally {
			stateLock.unlock();	
		}
	}
	
	public void setTerminatingStatus() {
		stateLock.lock();
		try {
			this.state = ExecutionState.TERMINATING;
		} finally {
			stateLock.unlock();	
		}
	}

	@Override
	public DroidReport<T> report() {
		return this.reportListener.getReportSnapshot();
	}
	
	//////////////////////////////////////////////////////////////////////
	
	// Registra que el manager termino
	protected final void workerExecutorTerminated(){
		setTerminatedStatus();
	}
	
	//////////////////////////////////////////////////////////////////////
	
	static class MTMThreadFactory implements ThreadFactory {
		final ThreadGroup group;
		final AtomicInteger threadNumber = new AtomicInteger(1);
		final String prefix;

		MTMThreadFactory(String descr) {
			prefix = "-" + descr + "-";
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
		}

		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r, prefix + threadNumber.getAndIncrement(), 0);
			if (t.isDaemon())
				t.setDaemon(false);
			if (t.getPriority() != Thread.NORM_PRIORITY)
				t.setPriority(Thread.NORM_PRIORITY);
			return t;
		}
	}


	class TaskRunner implements Runnable {
		private T task;
		
		TaskRunner(T task) {
			this.task = task;
		}

		@Override
		public void run() {
			log.trace("Running TaskRunner - Task {}", task);
			
			boolean terminateAll = false;
			Exception ex = null;
			try {
				
				Worker<T> w = workerFactory.newWorker(task);
				listeners.preExecute(task, w);
				
				boolean hasWorkToDo = task.prepare();
				boolean isAborted = false;

				try {
					if(hasWorkToDo)
						w.execute();
					else
						log.info("Se completo el procesamiento del Task {} sin problemas", task);
				} catch (StopExecutionException e) {
					// No hago nada. Simplemente evito que se cuente como Abortada	
					ex = e;
				} catch (Exception e) {
					ex = e;
					isAborted = true;
					throw e;
				}finally{
					task.close(); 
					if(!isAborted)
						listeners.postExecute(task, w, ex);
					ex = null;
				}

			} catch (InvalidTaskException e1) {
//				log.info("La tarea {} no se ejecutara, esta abortada", task);
				ex = e1;
			} catch (Exception e2) {
				terminateAll = true;
				ex = e2;
			} finally {
				if (ex != null){
					log.warn("La ejecucion del task {} genero el siguiente error [{}]:{}. Se " + (terminateAll ? "corta" : "continua")
							+ " la ejecucion ", new Object[]{task, ex.getClass().getSimpleName(), ex.getMessage()});
					//ex.printStackTrace();
					listeners.taskAborted(task);
				
					if (terminateAll)
						stop(2000); // 	sends stop signal and waits at most 2 secs
				}

				// Se informa a la cola que debe liberar el Task que acaba de finalizar
				if (myQueue instanceof UseAwareQueue<?>)
					((UseAwareQueue<T>) myQueue).release(task);
				
				synchronized (workingLock) {
					log.trace("Termina el worker. Despierta al bucle principal");
					workingLock.notify();
				}

			}
		}

	}

}