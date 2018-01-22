package com.platform.app.platformUser.repository;

import com.platform.app.common.model.PaginatedData;
import com.platform.app.common.repository.GenericRepository;
import com.platform.app.platformUser.model.Admin;
import com.platform.app.platformUser.model.User;
import com.platform.app.platformUser.model.filter.UserFilter;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.Map;

@Stateless
public class AdminRepository extends GenericRepository<Admin> {

    @PersistenceContext
    EntityManager em;

    @Override
    protected Class<Admin> getPersistentClass() {
        return Admin.class;
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public boolean alreadyExists(final Admin admin) {
        return alreadyExists("email", admin.getEmail(), admin.getId());
    }

    public Admin findByEmail(final String email) {
        try {
            return (Admin) em.createQuery("Select e From User e where e.email = :email")
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (final NoResultException e) {
            return null;
        }
    }

    public PaginatedData<Admin> findByFilter(final UserFilter userFilter) {
        final StringBuilder clause = new StringBuilder("WHERE e.id is not null");
        final Map<String, Object> queryParameters = new HashMap<>();
        if (userFilter.getName() != null) {
            clause.append(" And Upper(e.name) Like Upper(:name)");
            queryParameters.put("name", "%" + userFilter.getName() + "%");
        }
        if (userFilter.getUserType() != null) {
            clause.append(" And e.userType = :userType");
            queryParameters.put("userType", userFilter.getUserType());
        }
        return findByParameters(clause.toString(), userFilter.getPaginationData(), queryParameters, "name ASC");
    }

}
