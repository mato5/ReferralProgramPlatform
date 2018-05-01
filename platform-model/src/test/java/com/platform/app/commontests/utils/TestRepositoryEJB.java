package com.platform.app.commontests.utils;

import com.platform.app.geoIP.model.GeoIP;
import org.junit.Ignore;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;


@Ignore
@Stateless
public class TestRepositoryEJB {

	@PersistenceContext
	private EntityManager em;

	private static final List<Class<?>> ENTITIES_TO_REMOVE = Arrays.asList(GeoIP.class);

	public void deleteAll() {
		for (final Class<?> entityClass : ENTITIES_TO_REMOVE) {
			deleteAllForEntity(entityClass);
		}
	}

	@SuppressWarnings("unchecked")
	private void deleteAllForEntity(final Class<?> entityClass) {
		final List<Object> rows = em.createQuery("Select e From " + entityClass.getSimpleName() + " e").getResultList();
		for (final Object row : rows) {
			em.remove(row);
		}
	}

	public void add(final Object entity) {
		em.persist(entity);
	}

}