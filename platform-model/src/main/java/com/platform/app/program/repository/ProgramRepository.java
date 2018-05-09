package com.platform.app.program.repository;

import com.platform.app.common.repository.GenericRepository;
import com.platform.app.platformUser.model.User;
import com.platform.app.program.model.Application;
import com.platform.app.program.model.Program;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class ProgramRepository extends GenericRepository<Program> {

    @PersistenceContext
    EntityManager em;

    @Override
    protected Class<Program> getPersistentClass() {
        return Program.class;
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public List<Program> findByAdmin(User admin) {
        String query = "select p from Program p join p.admins a where a.id = :aId";

        return em.createQuery(query, Program.class)
                .setParameter("aId", admin.getId()).getResultList();
    }

    public Program findByApplication(Application application) {
        String query = "select p from Program p join p.activeApplications a where a.apiKey = :aId";

        try {
            return em.createQuery(query, Program.class)
                    .setParameter("aId", application.getApiKey()).getSingleResult();
        } catch (final NoResultException e) {
            return null;
        }
    }

    public Program findByName(String name) {
        String query = "select p from Program p where p.name = :name";

        try {
            return em.createQuery(query, Program.class)
                    .setParameter("name", name).getSingleResult();
        } catch (final NoResultException e) {
            return null;
        }
    }

    public List<Program> findByActiveUser(User user) {
        String query = "select p from Program p join p.activeCustomers a where a.id = :aId";

        return em.createQuery(query, Program.class)
                .setParameter("aId", user.getId()).getResultList();

    }

    public boolean alreadyExists(Program program) {
        return alreadyExists("name", program.getName(), null);
    }
}
