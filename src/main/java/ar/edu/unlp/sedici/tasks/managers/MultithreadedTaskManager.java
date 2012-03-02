package ar.edu.unlp.sedici.tasks.managers;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ar.edu.unlp.sedici.tasks.api.Task;

public class MultithreadedTaskManager<T extends Task> extends AbstractTaskManager<T> {

	public MultithreadedTaskManager(int nThreads) {
		super(new MyThreadPoolExecutor(nThreads, new MTMThreadFactory("worker")));
		
		//me asocio con el threadPoolExecutor para que luego me avise cuando termina
		getWorkerExecutor().setMyManager(this);
	}

	private MyThreadPoolExecutor getWorkerExecutor(){
		return (MyThreadPoolExecutor) this.workerExecutor;
	}
	
	@Override
	protected boolean needsMoreJobs() {
		return getWorkerExecutor().getActiveCount() < getWorkerExecutor().getMaximumPoolSize();
	}
	
	@Override
	protected boolean isStillWorking() {
		return getWorkerExecutor().getActiveCount() > 0;
	}

	static class MyThreadPoolExecutor extends ThreadPoolExecutor {
		private AbstractTaskManager<?> myManager;
		
		public MyThreadPoolExecutor(int nThreads, ThreadFactory threadFactory) {
			super(nThreads, nThreads,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(),
                    threadFactory);
		}
		
		public void setMyManager(AbstractTaskManager<?> myManager) {
			this.myManager = myManager;
		}
		@Override
		protected void terminated() {
			if (myManager != null)
				myManager.workerExecutorTerminated();
		}	
	}
}