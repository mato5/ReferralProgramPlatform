package com.platform.app.invitation.services.impl;

import com.platform.app.common.exception.FieldNotValidException;
import com.platform.app.invitation.exception.InvitationServiceException;
import com.platform.app.invitation.model.Invitation;
import com.platform.app.invitation.repository.InvitationRepository;
import com.platform.app.invitation.services.InvitationServices;
import com.platform.app.platformUser.exception.UserNotFoundException;
import com.platform.app.platformUser.model.Customer;
import com.platform.app.platformUser.model.User;
import com.platform.app.platformUser.repository.PlatformUserRepository;
import com.platform.app.program.exception.ProgramNotFoundException;
import com.platform.app.program.model.Program;
import com.platform.app.program.repository.ProgramRepository;
import com.platform.app.program.services.ProgramServices;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static com.platform.app.commontests.geoIP.GeoIPForTestsRepository.test1;
import static com.platform.app.commontests.invitation.InvitationForTestsRepository.inv2;
import static com.platform.app.commontests.invitation.InvitationForTestsRepository.invitationWithId;
import static com.platform.app.commontests.platformUser.UserForTestsRepository.*;
import static com.platform.app.commontests.program.ProgramForTestsRepository.program1;
import static com.platform.app.commontests.program.ProgramForTestsRepository.programWithId;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class InvitationServicesUTest {
    private Validator validator;
    private InvitationServices invitationServices;

    @Mock
    InvitationRepository invitationRepository;

    @Mock
    PlatformUserRepository userRepository;


    @Mock
    ProgramRepository programRepository;

    @Mock
    ProgramServices programServices;

    @Before
    public void initTestCase() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();

        MockitoAnnotations.initMocks(this);

        invitationServices = new InvitationServicesImpl();
        ((InvitationServicesImpl) invitationServices).userRepository = userRepository;
        ((InvitationServicesImpl) invitationServices).validator = validator;
        ((InvitationServicesImpl) invitationServices).invitationRepository = invitationRepository;
        ((InvitationServicesImpl) invitationServices).programRepository = programRepository;
        ((InvitationServicesImpl) invitationServices).programServices = programServices;
    }

    @Test
    public void sendInvitationWrongIdSent() {
        Invitation toBeSent = inv2();
        toBeSent.setByUserId(null);
        sendInvWithInvalidField(toBeSent, "byUserId");
    }

    @Test
    public void sendInvitationWrongIdTo() {
        Invitation toBeSent = inv2();
        toBeSent.setToUserId(null);
        sendInvWithInvalidField(toBeSent, "toUserId");
    }

    @Test
    public void sendInvitationWrongProgram() {
        Invitation toBeSent = inv2();
        toBeSent.setProgramId(null);
        sendInvWithInvalidField(toBeSent, "programId");
    }

    @Test
    public void sendInvitationCorrect() {
        Invitation toBeSent = inv2();
        when(userRepository.findById(toBeSent.getToUserId())).thenReturn(customerWithIdAndCreatedAt(johnDoe(), 3L));
        Customer invitedBy = mary();
        Program program = programWithId(program1(), 1L);
        program.setActiveCustomers(new HashSet<>(Collections.singletonList(customerWithIdAndCreatedAt(mary(), 2L))));
        when(userRepository.findById(toBeSent.getByUserId())).thenReturn(userWithIdAndCreatedAt(invitedBy, 2L));
        when(programRepository.findById(toBeSent.getProgramId())).thenReturn(program);
        when(userRepository.findById(toBeSent.getByUserId())).thenReturn(customerWithIdAndCreatedAt(invitedBy, 2L));
        when(invitationRepository.alreadyInvited(any(), any())).thenReturn(false);

        invitationServices.send(toBeSent);

        verify(invitationRepository).add(eq(toBeSent));
    }

    @Test(expected = InvitationServiceException.class)
    public void sendInvitationAlreadyInvited() {
        Invitation toBeSent = inv2();
        when(userRepository.findById(toBeSent.getToUserId())).thenReturn(customerWithIdAndCreatedAt(johnDoe(), 3L));
        Customer invitedBy = mary();
        when(userRepository.findById(toBeSent.getByUserId())).thenReturn(userWithIdAndCreatedAt(invitedBy, 2L));
        when(programRepository.findById(toBeSent.getProgramId())).thenReturn(programWithId(program1(), 1L));
        when(userRepository.findById(toBeSent.getByUserId())).thenReturn(customerWithIdAndCreatedAt(invitedBy, 2L));
        when(invitationRepository.alreadyInvited(any(), any())).thenReturn(true);

        invitationServices.send(toBeSent);

    }

    @Test(expected = UserNotFoundException.class)
    public void sendInvitationInvitorNotFound() {
        Invitation toBeSent = inv2();
        when(userRepository.findById(3L)).thenReturn(customerWithIdAndCreatedAt(johnDoe(), 3L));
        Customer invitedBy = mary();
        when(userRepository.findById(toBeSent.getByUserId())).thenReturn(null);
        when(programRepository.findById(toBeSent.getProgramId())).thenReturn(programWithId(program1(), 1L));
        when(invitationRepository.alreadyInvited(any(), any())).thenReturn(false);

        invitationServices.send(toBeSent);

    }

    @Test(expected = UserNotFoundException.class)
    public void sendInvitationInvitedNotFound() {
        Invitation toBeSent = inv2();
        when(userRepository.findById(toBeSent.getToUserId())).thenReturn(null);
        Customer invitedBy = mary();
        when(userRepository.findById(toBeSent.getByUserId())).thenReturn(userWithIdAndCreatedAt(invitedBy, 2L));
        when(programRepository.findById(toBeSent.getProgramId())).thenReturn(programWithId(program1(), 1L));
        when(userRepository.findById(toBeSent.getByUserId())).thenReturn(customerWithIdAndCreatedAt(invitedBy, 2L));
        when(invitationRepository.alreadyInvited(any(), any())).thenReturn(false);

        invitationServices.send(toBeSent);

    }

    @Test(expected = ProgramNotFoundException.class)
    public void sendInvitationProgramNotFound() {
        Invitation toBeSent = inv2();
        when(userRepository.findById(toBeSent.getToUserId())).thenReturn(customerWithIdAndCreatedAt(johnDoe(), 3L));
        Customer invitedBy = mary();
        when(userRepository.findById(toBeSent.getByUserId())).thenReturn(userWithIdAndCreatedAt(invitedBy, 2L));
        when(programRepository.findById(toBeSent.getProgramId())).thenReturn(null);
        when(userRepository.findById(toBeSent.getByUserId())).thenReturn(customerWithIdAndCreatedAt(invitedBy, 2L));
        when(invitationRepository.alreadyInvited(any(), any())).thenReturn(false);

        invitationServices.send(toBeSent);

    }

    @Test(expected = InvitationServiceException.class)
    public void sendInvitationUserAlreadyActive() {
        Invitation toBeSent = inv2();
        when(userRepository.findById(toBeSent.getToUserId())).thenReturn(customerWithIdAndCreatedAt(johnDoe(), 3L));
        Customer invitedBy = mary();
        when(userRepository.findById(toBeSent.getByUserId())).thenReturn(userWithIdAndCreatedAt(invitedBy, 2L));
        Program program = programWithId(program1(), 1L);
        program.addActiveCustomers(customerWithIdAndCreatedAt(johnDoe(), 3L));
        when(programRepository.findById(toBeSent.getProgramId())).thenReturn(program);
        when(userRepository.findById(toBeSent.getByUserId())).thenReturn(customerWithIdAndCreatedAt(invitedBy, 2L));
        when(invitationRepository.alreadyInvited(any(), any())).thenReturn(false);

        invitationServices.send(toBeSent);

    }

    @Test(expected = InvitationServiceException.class)
    public void sendInvitationNoInvsLeft() {
        Invitation toBeSent = inv2();
        when(userRepository.findById(toBeSent.getToUserId())).thenReturn(customerWithIdAndCreatedAt(johnDoe(), 3L));
        Customer invitedBy = mary();
        toBeSent.setInvitationsLeft(0);
        when(userRepository.findById(toBeSent.getByUserId())).thenReturn(userWithIdAndCreatedAt(invitedBy, 2L));
        when(programRepository.findById(toBeSent.getProgramId())).thenReturn(programWithId(program1(), 1L));
        when(invitationRepository.alreadyInvited(any(), any())).thenReturn(false);
        Invitation marysInv = new Invitation();
        marysInv.setByUserId(1L);
        marysInv.setInvitationsLeft(0);
        marysInv.setToUserId(2L);
        marysInv.setProgramId(1L);
        when(invitationRepository.findByInvitor(2L)).thenReturn(Collections.singletonList(marysInv));
        when(programServices.getUsersRole(2L, 1L)).thenReturn(User.Roles.CUSTOMER);
        invitationServices.send(toBeSent);

    }

    @Test
    public void sendInBatch() {
        Invitation toBeSent = inv2();
        when(userRepository.findById(3L)).thenReturn(customerWithIdAndCreatedAt(johnDoe(), 3L));
        User invitedBy = userWithIdAndCreatedAt(admin(), 2L);
        when(userRepository.findById(2L)).thenReturn(invitedBy);
        when(userRepository.findById(3L)).thenReturn(userWithIdAndCreatedAt(johnDoe(), 3L));
        when(userRepository.findById(1L)).thenReturn(userWithIdAndCreatedAt(mary(), 1L));
        Program program = program1();
        program.setActiveCustomers(new HashSet<>());
        program.setAdmins(new HashSet<>(Collections.singleton(invitedBy)));
        when(programRepository.findById(toBeSent.getProgramId())).thenReturn(programWithId(program, 1L));
        when(invitationRepository.alreadyInvited(any(), any())).thenReturn(false);
        when(userRepository.findByEmail("mary@domain.com")).thenReturn(userWithIdAndCreatedAt(mary(), 1L));
        when(userRepository.findByEmail("john@domain.com")).thenReturn(userWithIdAndCreatedAt(johnDoe(), 3L));
        List<String> emails = Arrays.asList("john@domain.com", "mary@domain.com");

        //Inv for John Doe
        Invitation result1 = new Invitation();
        result1.setProgramId(1L);
        result1.setByUserId(2L);
        result1.setToUserId(3L);
        //Inv for Mary
        Invitation result2 = new Invitation();
        result2.setProgramId(1L);
        result2.setByUserId(2L);
        result2.setToUserId(1L);

        List<Invitation> invs = invitationServices.sendInBatch(2L, 1L, emails, 5);
        assertThat(invs.size(), is(equalTo(2)));
        verify(invitationRepository).add(eq(result1));
        verify(invitationRepository).add(eq(result2));

    }

    @Test
    public void acceptInv() {
        Invitation toBeSent = invitationWithId(inv2(), 1L);
        when(userRepository.findById(toBeSent.getToUserId())).thenReturn(customerWithIdAndCreatedAt(johnDoe(), 3L));
        Customer invitedBy = mary();
        when(userRepository.findById(toBeSent.getByUserId())).thenReturn(userWithIdAndCreatedAt(invitedBy, 2L));
        when(programRepository.findById(toBeSent.getProgramId())).thenReturn(programWithId(program1(), 1L));
        when(userRepository.findById(toBeSent.getByUserId())).thenReturn(customerWithIdAndCreatedAt(invitedBy, 2L));
        when(invitationRepository.findById(1L)).thenReturn(toBeSent);
        invitationServices.accept(toBeSent, test1());

        verify(invitationRepository).update(toBeSent);
        verify(programServices).addCustomer(toBeSent.getToUserId(), toBeSent.getProgramId());
    }

    @Test
    public void declineInv() {
        Invitation toBeSent = invitationWithId(inv2(), 1L);
        when(userRepository.findById(toBeSent.getToUserId())).thenReturn(customerWithIdAndCreatedAt(johnDoe(), 3L));
        Customer invitedBy = mary();
        when(userRepository.findById(toBeSent.getByUserId())).thenReturn(userWithIdAndCreatedAt(invitedBy, 2L));
        when(programRepository.findById(toBeSent.getProgramId())).thenReturn(programWithId(program1(), 1L));
        when(userRepository.findById(toBeSent.getByUserId())).thenReturn(customerWithIdAndCreatedAt(invitedBy, 2L));
        when(programRepository.findByActiveUser(customerWithIdAndCreatedAt(johnDoe(), 3L))).thenReturn(Collections.singletonList(programWithId(program1(), 1L)));
        when(invitationRepository.findById(1L)).thenReturn(toBeSent);
        invitationServices.decline(toBeSent);

        verify(invitationRepository).update(eq(toBeSent));
    }

    private void sendInvWithInvalidField(final Invitation inv, final String expectedInvalidFieldName) {
        try {
            invitationServices.send(inv);
            fail("An error should have been thrown");
        } catch (final FieldNotValidException e) {
            assertThat(e.getFieldName(), is(equalTo(expectedInvalidFieldName)));
        }
    }


}
