package com.platform.app.invitation.repository;

import com.platform.app.common.repository.GenericRepository;
import com.platform.app.invitation.model.Invitation;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class InvitationRepository extends GenericRepository<Invitation> {

    @PersistenceContext
    EntityManager em;

    @Override
    protected Class<Invitation> getPersistentClass() {
        return Invitation.class;
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public boolean alreadyExists(Invitation invitation) {
        try {
            if (invitation.getId() != null) {
                Invitation found = this.findById(invitation.getId());
                if (found != null) {
                    return true;
                }
            }
        } catch (Exception ex) {
            return false;
        }
        return false;
        //return alreadyExists("byUserId","1",invitation.getId());
    }

    public List<Invitation> findByInvitor(Long invitorId){
        return em.createQuery("Select e from Invitation e where e.byUserId" + " = :propertyValue")
                .setParameter("propertyValue", invitorId).getResultList();
    }

    public List<Invitation> findByProgram(Long programId){
        return em.createQuery("Select e from Invitation e where e.programId" + " = :propertyValue")
                .setParameter("propertyValue", programId).getResultList();
    }

    public List<Invitation> findByInvitee(Long inviteeId){
        return em.createQuery("Select e from Invitation e where e.toUserId" + " = :propertyValue")
                .setParameter("propertyValue", inviteeId).getResultList();
    }

    public boolean alreadyInvited(Long customerId) {
        return em.createQuery("Select e from Invitation e where e.toUserId" + " = :propertyValue")
                .setParameter("propertyValue", customerId).getResultList().size() > 0;
    }
}