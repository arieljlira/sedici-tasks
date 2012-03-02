package ar.edu.unlp.sedici.tasks.generator;
/*
import javax.persistence.Query;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unlp.sedici.tasks.api.Task;
import ar.edu.unlp.sedici.tasks.api.TaskGenerator;

@Configurable
public abstract class PersistenceTaskGenerator<T extends Task> implements TaskGenerator<T> {
	protected Logger log = LoggerFactory.getLogger(this.getClass());
	private boolean initialized = false;
	
	@PersistenceContext
	private transient EntityManager entityManager;

	protected abstract void init();
	protected abstract T createTask(Object o) ;
	protected abstract Query getNextTasksQuery(boolean isCount);

	
	public boolean hasNext() {
		 return (this.size() > 0);
	}
	
	@Transactional(readOnly=true)
	@Override
	public int size() {
		checkInit();
		Long l = (Long) this.getNextTasksQuery(true).getSingleResult();
		log.trace("Tengo {} tasks para levantar", l);
		return l.intValue();
	}
	
	@Override
	public T next() {
		checkInit();
		Object o; 
		try{
			// Recupera el listado de tasks que estan listos para ejecutarse.
			o = this.getNextTasksQuery(false).setMaxResults(1).getSingleResult();
		}catch (EmptyResultDataAccessException e) {
			o = null;
		}
		if (o == null){
			log.debug("No hay mas tasks para levantar ");
			return null;
		}
		return this.createTask(o);
	}
	
	
	public EntityManager getEntityManager() {
		return entityManager;
	}

	
	public void checkInit(){
		if (!this.initialized){
			this.init();
			this.initialized = true;
			//throw new RuntimeException("fin");
		}
	};
	
}
*/