package ar.edu.unlp.sedici.tasks.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;


public class DroidReport<T> {
	//fecha en la que se genero el reporte
	private Date reportDate;
	
	protected Date startDate = null;
	protected Date endDate = null;
	
	protected long finishedTaskCount = 0;
	protected long startedTaskCount = 0;
 	protected long abortedTaskCount = 0;
 	protected long failedTaskCount = 0;

 	protected List<T> unfinishedTasks = null;
 	protected List<T> abortedTasks = null;
 	protected Collection<T> lastFinishedTasks = null;
 	protected ExecutionStatus executionStatus;
 	
	public enum ExecutionStatus {WAITING, RUNNING, ENDED};
	
	public DroidReport() {
		this.reportDate = new Date();
	}

	public Date getReportDate() {
		return reportDate;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public long getElapsedTime() {
		if (this.startDate == null)
			return 0;
		Date from = (this.endDate == null)?new Date():this.endDate;
		return (from.getTime() - this.startDate.getTime())  / 1000;
	}

	public long getFinishedTaskCount() {
		return finishedTaskCount;
	}

	public long getStartedTaskCount() {
		return startedTaskCount;
	}

	public long getAbortedTaskCount() {
		return abortedTaskCount;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("DroidReport [").append(this.executionStatus).append("] ")
			.append("[abortedTaskCount=" ).append( abortedTaskCount ).append( ", Total elapsedTime (secs)=" ).append( getElapsedTime() )
			.append( ", endDate=" ).append( endDate	).append( ", finishedTaskCount=" ).append( finishedTaskCount )
			.append( ", reportDate=" + reportDate ).append( ", startDate=" ).append( startDate)
			.append( ", startedTaskCount=" ).append( startedTaskCount ).append( ", failedTaskCount=" ).append( failedTaskCount  )
			.append( "]");
		return buf.toString();
	}

	public long getFailedTaskCount() {
		return failedTaskCount;
	}
	
	public Collection<T> getLastFinishedTasks() {
		return lastFinishedTasks;
	}
		
	public List<T> getUnfinishedTasks() {
		return unfinishedTasks;
	}

	public List<T> getAbortedTasks() {
		return abortedTasks;
	}
	
	public ExecutionStatus getExecutionStatus() {
		return executionStatus;
	}

	public DroidReport<T> getReportSnapshot(){
		DroidReport<T> copyReport = new DroidReport<T>();
		copyReport.executionStatus = this.executionStatus;
		copyReport.startDate = this.startDate;
		copyReport.endDate = this.endDate;
		copyReport.startedTaskCount = this.startedTaskCount;
		copyReport.abortedTaskCount = this.abortedTaskCount;
		copyReport.failedTaskCount = this.failedTaskCount;
		copyReport.finishedTaskCount = this.finishedTaskCount;
		copyReport.lastFinishedTasks = new ArrayList<T>(this.lastFinishedTasks);
		copyReport.unfinishedTasks = new ArrayList<T>(this.unfinishedTasks);
		copyReport.abortedTasks = new ArrayList<T>(this.abortedTasks);
		return copyReport;
	}
		
	
}
