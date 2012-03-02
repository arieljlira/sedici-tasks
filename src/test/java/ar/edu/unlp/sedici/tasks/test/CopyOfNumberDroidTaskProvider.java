package ar.edu.unlp.sedici.tasks.test;

import java.util.Random;

import ar.edu.unlp.sedici.tasks.api.TaskGenerator;

public class CopyOfNumberDroidTaskProvider implements TaskGenerator<CopyOfNumberTask> {

	private int max;
	
	public CopyOfNumberDroidTaskProvider(int max) {
		this.max = max;
	}
	
	@Override
	public CopyOfNumberTask next() {
		
		if (max <=0)
			return null;
		
		Random r = new Random();
		int[] numbers = new int[r.nextInt(10)];
		for (int i = 0; i < numbers.length; i++) {
			numbers[i]=r.nextInt();
		}
		max--;
		return new CopyOfNumberTask(numbers);
	}

	@Override
	public int size() {
		return max;
	}

}
