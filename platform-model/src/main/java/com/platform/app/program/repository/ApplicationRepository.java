package com.platform.app.program.repository;

import com.platform.app.common.repository.GenericRepository;
import com.platform.app.program.model.Application;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;

@Stateless
public class ApplicationRepository extends GenericRepository<Application> {

    @PersistenceContext
    EntityManager em;

    @Override
    protected Class<Application> getPersistentClass() {
        return Application.class;
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public boolean alreadyExists(Application application) {
        return alreadyExists("URL", application.getURL(), null);
    }

    public Application findByApiKey(UUID key) {
        if (key == null) {
            return null;
        }
        return em.find(Application.class, key);
    }

    public Application findByURL(String URL) {
        return em.createQuery("Select e from Application e where e.URL" + " = :propertyValue", Application.class)
                .setParameter("propertyValue", URL).getSingleResult();
    }

    public List<Application> findByName(String name) {
        return em.createQuery("Select e from Application e where e.name" + " = :propertyValue", Application.class)
                .setParameter("propertyValue", name).getResultList();
    }
}
