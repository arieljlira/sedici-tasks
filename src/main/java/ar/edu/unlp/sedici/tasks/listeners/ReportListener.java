package ar.edu.unlp.sedici.tasks.listeners;

import java.util.Date;
import java.util.LinkedList;

import ar.edu.unlp.sedici.tasks.api.DroidReport;
import ar.edu.unlp.sedici.tasks.api.Task;
import ar.edu.unlp.sedici.tasks.api.TaskEventListener;
import ar.edu.unlp.sedici.tasks.api.Worker;
import ar.edu.unlp.sedici.tasks.queues.UnboundedDiscardingQueue;

public class ReportListener<T extends Task> extends DroidReport<T> implements TaskEventListener<T>{
	
	public ReportListener() {
		this.executionStatus = ExecutionStatus.WAITING;
		this.lastFinishedTasks = new UnboundedDiscardingQueue<T>(100);
		this.unfinishedTasks = new LinkedList<T>();
		this.abortedTasks = new LinkedList<T>();
	}
	
	@Override
	public void init() {
		this.startDate = new Date();
		this.executionStatus = ExecutionStatus.RUNNING;
	}

	@Override
	public synchronized void postExecute(T task, Worker<T> worker, Exception exception) {
		if (exception != null)
			this.failedTaskCount++;
		this.finishedTaskCount++;
		this.unfinishedTasks.remove(task);
		this.lastFinishedTasks.add(task);
	}

	@Override
	public synchronized void preExecute(T task, Worker<T> worker) {
		this.startedTaskCount++;
		this.unfinishedTasks.add(task);
	}

	@Override
	public synchronized void taskAborted(T task) {
		this.abortedTaskCount++;
		this.unfinishedTasks.remove(task);
		this.abortedTasks.add(task);
	}

	@Override
	public void end() {
		this.endDate = new Date();
		this.executionStatus = ExecutionStatus.ENDED;
	}

}