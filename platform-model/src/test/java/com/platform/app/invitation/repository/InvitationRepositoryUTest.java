package com.platform.app.invitation.repository;

//import com.platform.app.category.model.Category;
//import com.platform.app.category.repository.CategoryRepository;

import com.platform.app.commontests.utils.TestBaseRepository;
import com.platform.app.invitation.model.Invitation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.platform.app.commontests.invitation.InvitationForTestsRepository.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

//import static com.platform.app.commontests.category.CategoryForTestsRepository.*;

public class InvitationRepositoryUTest extends TestBaseRepository {
    private InvitationRepository invitationRepository;

    @Before
    public void initTestCase() {
        initializeTestDB();

        invitationRepository = new InvitationRepository();
        invitationRepository.em = em;
    }

    @After
    public void setDownTestCase() {
        closeEntityManager();
    }

    @Test
    public void addInvitationAndFindIt() {
        final Long addedId = dbCommandExecutor.executeCommand(() -> {
            return invitationRepository.add(inv1()).getId();
        });

        assertThat(addedId, is(notNullValue()));

        final Invitation invitation = invitationRepository.findById(addedId);
        assertThat(invitation, is(notNullValue()));
        assertThat(invitation.getByUserId(), is(equalTo(inv1().getByUserId())));
    }

    @Test
    public void findInvByIdNotFound() {
        final Invitation invitation = invitationRepository.findById(999L);
        assertThat(invitation, is(nullValue()));
    }

    @Test
    public void findInvByIdWithNullId() {
        final Invitation invitation = invitationRepository.findById(null);
        assertThat(invitation, is(nullValue()));
    }

    @Test
    public void updateInv() {
        final Long addedId = dbCommandExecutor.executeCommand(() -> {
            return invitationRepository.add(inv1()).getId();
        });

        final Invitation invitationAfterAdd = invitationRepository.findById(addedId);
        assertThat(invitationAfterAdd.getByUserId(), is(equalTo(inv1().getByUserId())));

        invitationAfterAdd.setByUserId(inv2().getByUserId());
        dbCommandExecutor.executeCommand(() -> {
            invitationRepository.update(invitationAfterAdd);
            return null;
        });

        final Invitation invAfterUpdate = invitationRepository.findById(addedId);
        assertThat(invAfterUpdate.getByUserId(), is(equalTo(inv2().getByUserId())));
    }

    @Test
    public void findAllInvs() {
        dbCommandExecutor.executeCommand(() -> {
            allInvs().forEach(invitationRepository::add);
            return null;
        });

        final List<Invitation> invs = invitationRepository.findAll("toUserId");
        assertThat(invs.size(), is(equalTo(2)));
        assertThat(invs.get(0).getByUserId(), is(equalTo(inv1().getByUserId())));
        assertThat(invs.get(1).getByUserId(), is(equalTo(inv2().getByUserId())));
    }

    @Test
    public void alreadyExistsForAdd() {
        final Invitation invitation = dbCommandExecutor.executeCommand(() -> {
            return invitationRepository.add(inv1());
        });

        assertThat(invitationRepository.alreadyExists(invitation), is(equalTo(true)));
        assertThat(invitationRepository.alreadyExists(inv2()), is(equalTo(false)));
    }

    /*@Test
    public void alreadyExistsWithId() {
        final Invitation invitation = dbCommandExecutor.executeCommand(() -> {
            invitationRepository.add(inv2());
            return invitationRepository.add(inv1());
        });

        assertThat(invitationRepository.alreadyExists(invitation), is(equalTo(false)));

        invitation.setToUserId(inv().getIpAddress());
        assertThat(geoIPRepository.alreadyExists(geoIP), is(equalTo(true)));

        geoIP.setIpAddress(test3().getIpAddress());
        assertThat(geoIPRepository.alreadyExists(geoIP), is(equalTo(false)));
    }*/

    @Test
    public void existsById() {
        final Long addedId = dbCommandExecutor.executeCommand(() -> {
            return invitationRepository.add(inv1()).getId();
        });

        assertThat(invitationRepository.existsById(addedId), is(equalTo(true)));
        assertThat(invitationRepository.existsById(999L), is(equalTo(false)));
    }

    @Test
    public void deleteExistingInv() {
        final Invitation invitation = dbCommandExecutor.executeCommand(() -> {
            return invitationRepository.add(inv1());
        });
        dbCommandExecutor.executeCommand(() -> {
            invitationRepository.delete(invitationRepository.findById(invitation.getId()));
            return null;
        });

        assertThat(invitationRepository.findAll("id").size(),is(equalTo(0)));
    }

    @Test
    public void findAllByProgramId() {
        dbCommandExecutor.executeCommand(() -> {
            allInvs().forEach(invitationRepository::add);
            return null;
        });

        final List<Invitation> invs = invitationRepository.findByProgram(1L);
        assertThat(invs.size(), is(equalTo(2)));
        assertThat(invs.get(0).getByUserId(), is(equalTo(inv1().getByUserId())));
        assertThat(invs.get(1).getByUserId(), is(equalTo(inv2().getByUserId())));
    }

    @Test
    public void findAllByInvitor() {
        dbCommandExecutor.executeCommand(() -> {
            allInvs().forEach(invitationRepository::add);
            return null;
        });

        final List<Invitation> invs = invitationRepository.findByInvitor(1L);
        assertThat(invs.size(), is(equalTo(1)));
        assertThat(invs.get(0).getByUserId(), is(equalTo(inv1().getByUserId())));
    }

    @Test
    public void findAllByInvitee() {
        dbCommandExecutor.executeCommand(() -> {
            allInvs().forEach(invitationRepository::add);
            return null;
        });

        final List<Invitation> invs = invitationRepository.findByInvitee(3L);
        assertThat(invs.size(), is(equalTo(1)));
        assertThat(invs.get(0).getByUserId(), is(equalTo(inv2().getByUserId())));
    }

}
