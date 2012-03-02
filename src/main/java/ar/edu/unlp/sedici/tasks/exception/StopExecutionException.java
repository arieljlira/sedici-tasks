package ar.edu.unlp.sedici.tasks.exception;


public class StopExecutionException extends Exception {

	public StopExecutionException(Exception e) {
		super(e);
	}

}
