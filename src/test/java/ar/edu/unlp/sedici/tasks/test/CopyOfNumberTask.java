package ar.edu.unlp.sedici.tasks.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ar.edu.unlp.sedici.tasks.api.ExclusiveElement;
import ar.edu.unlp.sedici.tasks.core.BaseTask;
import ar.edu.unlp.sedici.tasks.exception.InvalidTaskException;

public class CopyOfNumberTask extends BaseTask implements ExclusiveElement {
	
	private static final long serialVersionUID = 1L;
	private static int numberTaskNumber = 0;
	private static Logger log = LoggerFactory.getLogger(CopyOfNumberTask.class);
	
	private int[] numbers;
	private int id;
	
	public CopyOfNumberTask(int ... numbers) {
		this.id = numberTaskNumber++;
		log.info("se creo la cosecha " + this.id);
		this.numbers =numbers;
	}
	
	public int[] getNumbers() {
		return numbers;
	}
	
	@Override
	public String getId() {
		return String.valueOf(id);
	}

	@Override
	public String getSubject() {
		return String.valueOf(this.id);
	}

}
