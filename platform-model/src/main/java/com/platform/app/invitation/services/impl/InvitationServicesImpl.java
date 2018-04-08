package com.platform.app.invitation.services.impl;

import com.platform.app.common.exception.FieldNotValidException;
import com.platform.app.common.utils.ValidationUtils;
import com.platform.app.geoIP.model.GeoIP;
import com.platform.app.invitation.exception.InvitationExistentException;
import com.platform.app.invitation.exception.InvitationServiceException;
import com.platform.app.invitation.model.Invitation;
import com.platform.app.invitation.repository.InvitationRepository;
import com.platform.app.invitation.services.InvitationServices;
import com.platform.app.platformUser.exception.UserNotFoundException;
import com.platform.app.platformUser.model.Admin;
import com.platform.app.platformUser.model.Customer;
import com.platform.app.platformUser.model.User;
import com.platform.app.platformUser.repository.AdminRepository;
import com.platform.app.platformUser.repository.CustomerRepository;
import com.platform.app.platformUser.repository.PlatformUserRepository;
import com.platform.app.platformUser.services.PlatformUserServices;
import com.platform.app.program.exception.ProgramNotFoundException;
import com.platform.app.program.model.Program;
import com.platform.app.program.repository.ProgramRepository;
import com.platform.app.program.services.ProgramServices;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.Validator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Stateless
public class InvitationServicesImpl implements InvitationServices {

    @Inject
    InvitationRepository invitationRepository;

    @Inject
    PlatformUserRepository userRepository;

    @Inject
    CustomerRepository customerRepository;

    @Inject
    ProgramRepository programRepository;

    @Inject
    ProgramServices programServices;

    @Inject
    Validator validator;

    @Override
    public Invitation send(Invitation inv) {
        validateInvitation(inv);
        Customer invited = customerRepository.findById(inv.getToUserId());
        User invitedBy = userRepository.findById(inv.getByUserId());
        Program invitedTo = programRepository.findById(inv.getProgramId());
        if (invited == null) {
            throw new UserNotFoundException();
        }
        if (invitedBy == null) {
            throw new UserNotFoundException();
        }
        if (invitedTo == null) {
            throw new ProgramNotFoundException();
        }
        if (invitationRepository.alreadyInvited(invited.getId(), inv.getProgramId())) {
            throw new InvitationServiceException("This customer has been already invited to the specified program.");
        }
        if (invitedTo.getActiveCustomers().contains(invited)) {
            throw new InvitationServiceException("This customer is currently participating in this program.");
        }
        Customer invitedByCustomer;
        if (invitedBy.getUserType().equals(User.UserType.CUSTOMER)) {
            invitedByCustomer = customerRepository.findById(invitedBy.getId());
            if (invitedByCustomer.getInvitationsLeft() < 1) {
                throw new InvitationServiceException("This customer has no invitations left.");
            }
            invitedByCustomer.setInvitationsLeft(invitedByCustomer.getInvitationsLeft() - 1);
            customerRepository.update(invitedByCustomer);
        }

        inv = invitationRepository.add(inv);
        return inv;
    }

    @Override
    public List<Invitation> sendInBatch(Long byUserId, Long programId, List<String> emails) {
        List<Invitation> sent = new ArrayList<>();
        for (String item : emails) {
            Invitation inv = new Invitation();
            inv.setByUserId(byUserId);
            inv.setProgramId(programId);
            inv = sendWithEmail(inv, item);
            sent.add(inv);
        }
        return sent;
    }

    @Override
    public void accept(Invitation inv, GeoIP geoLocation) {
        inv = invitationRepository.findById(inv.getId());
        if (inv == null) {
            throw new InvitationServiceException("This invitation does not exist");
        }
        if (geoLocation == null) {
            throw new InvitationServiceException("The geoLocation does not exist");
        }
        if (inv.getActivated() != null) {
            throw new InvitationServiceException("This invitation has already been accepted");
        }
        Customer invited = customerRepository.findById(inv.getToUserId());
        User invitedBy = userRepository.findById(inv.getByUserId());
        Program invitedToProgram = programRepository.findById(inv.getProgramId());
        if (invitedToProgram == null) {
            throw new InvitationServiceException("The program the customer has been invited to does not exist.");
        }
        if (invited == null) {
            throw new InvitationServiceException("The invited customer does not exist");
        }
        if (invitedBy == null) {
            throw new InvitationServiceException("The inviter does not exist");
        }
        inv.setActivatedLocation(geoLocation);
        inv.setActivated(LocalDateTime.now());
        invitationRepository.update(inv);
        programServices.addCustomer(invited.getId(), invitedToProgram.getId());
    }

    @Override
    public void decline(Invitation inv) {
        inv = invitationRepository.findById(inv.getId());
        if (inv == null) {
            throw new InvitationServiceException("This invitation does not exist");
        }
        if (inv.isDeclined()) {
            throw new InvitationServiceException("This invitation has already been declined");
        }
        Customer invited = customerRepository.findById(inv.getToUserId());
        User invitedBy = userRepository.findById(inv.getByUserId());
        if (invited == null) {
            throw new InvitationServiceException("The invited customer does not exist");
        }
        if (invitedBy == null) {
            throw new InvitationServiceException("The inviter does not exist");
        }
        List<Program> invitedToPrograms = programRepository.findByActiveUser(invited);
        if (invitedToPrograms == null) {
            throw new InvitationServiceException("The user has not been invited to any program");
        }
        for (Program item : invitedToPrograms) {
            if (item.getId().equals(inv.getProgramId())) {
                programServices.removeCustomer(invited.getId(), item.getId());
                break;
            }
        }
        inv.setDeclined(true);
        invitationRepository.update(inv);
    }

    @Override
    public void delete(Invitation inv) {
        inv = invitationRepository.findById(inv.getId());
        if (inv == null) {
            throw new InvitationServiceException("The invitation you are trying to delete does not exist.");
        }
        invitationRepository.delete(inv);
    }

    @Override
    public Invitation findById(Long id) {
        Invitation inv = invitationRepository.findById(id);
        if (inv == null) {
            throw new InvitationServiceException("This invitation does not exist");
        }
        return inv;
    }

    @Override
    public List<Invitation> findByProgram(Long id) {
        return invitationRepository.findByProgram(id);
    }

    @Override
    public List<Invitation> findByInvitor(Long id) {
        return invitationRepository.findByInvitor(id);
    }

    @Override
    public List<Invitation> findByInvitee(Long id) {
        return invitationRepository.findByInvitee(id);
    }

    @Override
    public List<Invitation> findALl(String orderField) {
        if (orderField == null || orderField.equals("")) {
            orderField = "id";
        }
        return invitationRepository.findAll(orderField);
    }

    private Invitation sendWithEmail(Invitation invitation, String email) {
        User to = userRepository.findByEmail(email);
        if (to == null) {
            throw new InvitationServiceException("The user you are trying to invite does not exist");
        }
        invitation.setToUserId(to.getId());
        return send(invitation);
    }

    private void validateInvitation(final Invitation inv) throws FieldNotValidException, InvitationExistentException {
        if (invitationRepository.alreadyExists(inv)) {
            throw new InvitationExistentException();
        }

        ValidationUtils.validateEntityFields(validator, inv);
    }
}
