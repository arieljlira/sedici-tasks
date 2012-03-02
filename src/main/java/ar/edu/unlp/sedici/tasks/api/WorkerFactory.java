package ar.edu.unlp.sedici.tasks.api;

public interface WorkerFactory<T extends Task> {
	Worker<T> newWorker(T task);
}
