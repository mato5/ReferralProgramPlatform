package com.platform.app.geoIP.repository;

import com.platform.app.common.repository.GenericRepository;
import com.platform.app.geoIP.model.GeoIP;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

//@Stateless
@Deprecated
public class GeoIPRepository
{

/*    @PersistenceContext
    EntityManager em;

    @Override
    protected Class<GeoIP> getPersistentClass() {
        return GeoIP.class;
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public boolean alreadyExists(GeoIP geoIP){
        return alreadyExists("ipAddress",geoIP.getIpAddress(),geoIP.getId());
    }*/
}
