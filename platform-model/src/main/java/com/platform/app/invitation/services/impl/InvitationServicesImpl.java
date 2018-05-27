package com.platform.app.invitation.services.impl;

import com.platform.app.common.exception.FieldNotValidException;
import com.platform.app.common.utils.ValidationUtils;
import com.platform.app.geoIP.model.GeoIP;
import com.platform.app.invitation.exception.InvitationExistentException;
import com.platform.app.invitation.exception.InvitationNotFoundException;
import com.platform.app.invitation.exception.InvitationServiceException;
import com.platform.app.invitation.model.Invitation;
import com.platform.app.invitation.repository.InvitationRepository;
import com.platform.app.invitation.services.InvitationServices;
import com.platform.app.platformUser.exception.UserNotFoundException;
import com.platform.app.platformUser.model.User;
import com.platform.app.platformUser.repository.PlatformUserRepository;
import com.platform.app.program.exception.ProgramNotFoundException;
import com.platform.app.program.model.Program;
import com.platform.app.program.repository.ProgramRepository;
import com.platform.app.program.services.ProgramServices;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Validator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Stateless
@Transactional
public class InvitationServicesImpl implements InvitationServices {

    @Inject
    InvitationRepository invitationRepository;

    @Inject
    PlatformUserRepository userRepository;

    @Inject
    ProgramRepository programRepository;

    @Inject
    ProgramServices programServices;

    @Inject
    Validator validator;

    @Override
    public Invitation send(Invitation inv) {
        validateInvitation(inv);
        User invited = userRepository.findById(inv.getToUserId());
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
        if (invitedTo.getAdmins().contains(invited)) {
            throw new InvitationServiceException("The invited user is already an admin of the specified program");
        }
        if (!invitedTo.getActiveCustomers().contains(invitedBy) && !invitedTo.getAdmins().contains(invitedBy) &&
                !invitedBy.getUserType().equals(User.UserType.EMPLOYEE)) {
            throw new InvitationServiceException("This operation is forbidden for the provided users");
        }
        if (invitedTo.getWaitingList().getOrderedIds().contains(invited.getId())) {
            programServices.unregisterOnWaitingList(invitedTo.getId(), invited.getId());
        }
        if (User.Roles.CUSTOMER.equals(programServices.getUsersRole(invitedBy.getId(), invitedTo.getId()))) {
            List<Invitation> usersInvitations = invitationRepository.findByInvitee(invitedBy.getId());
            Invitation lookingFor = null;
            for (Invitation item : usersInvitations) {
                if (item.getProgramId().equals(invitedTo.getId())) {
                    lookingFor = item;
                    break;
                }
            }
            if (lookingFor != null) {
                if (lookingFor.getInvitationsLeft() < 1 && (lookingFor.getInvitationsLeft() != -1)) {
                    throw new InvitationServiceException("This customer has no invitations left.");
                }
                lookingFor.setInvitationsLeft(lookingFor.getInvitationsLeft() - 1);
                invitationRepository.update(lookingFor);
            }
        }
        inv = invitationRepository.add(inv);
        return inv;
    }

    @Override
    public int findInvitationsLeft(Long userId, Long programId) {
        User user = userRepository.findById(userId);
        Program program = programRepository.findById(programId);
        if (user == null) {
            throw new UserNotFoundException();
        }
        if (program == null) {
            throw new ProgramNotFoundException();
        }
        if (programServices.getUsersRole(user, program).equals(User.Roles.ADMINISTRATOR)) {
            return -1;
        }
        List<Invitation> userInvs = invitationRepository.findByInvitee(user.getId());
        Invitation lookingFor = null;
        for (Invitation item : userInvs) {
            if (item.getProgramId().equals(program.getId()) && !item.isDeclined()) {
                lookingFor = item;
                break;
            }
        }
        if (lookingFor == null) {
            return 0;
        }
        return lookingFor.getInvitationsLeft();
    }

    @Override
    public List<Invitation> sendInBatch(Long byUserId, Long programId, List<String> emails, Integer allowedInvitations) {
        List<Invitation> sent = new ArrayList<>();
        for (String item : emails) {
            Invitation inv = new Invitation();
            inv.setByUserId(byUserId);
            inv.setProgramId(programId);
            inv.setInvitationsLeft(allowedInvitations);
            inv = sendWithEmail(inv, item);
            sent.add(inv);
        }
        return sent;
    }

    @Override
    public List<Invitation> sendInBatch(String email, Long programId, List<String> emails, Integer allowedInvitations) {
        User byUser = userRepository.findByEmail(email);
        if (byUser == null) {
            throw new UserNotFoundException();
        }
        return sendInBatch(byUser.getId(), programId, emails, allowedInvitations);
    }

    @Override
    public void accept(Invitation inv, GeoIP geoLocation) {
        inv = invitationRepository.findById(inv.getId());
        if (inv == null) {
            throw new InvitationNotFoundException();
        }
        if (geoLocation == null) {
            throw new InvitationServiceException("The geoLocation does not exist");
        }
        if (inv.isDeclined()) {
            throw new InvitationServiceException("This invitation has already been declined");
        }
        if (inv.getActivated() != null) {
            throw new InvitationServiceException("This invitation has already been accepted");
        }
        User invited = userRepository.findById(inv.getToUserId());
        User invitedBy = userRepository.findById(inv.getByUserId());
        Program invitedToProgram = programRepository.findById(inv.getProgramId());
        if (invitedToProgram == null) {
            throw new ProgramNotFoundException();
        }
        if (invited == null) {
            throw new UserNotFoundException();
        }
        if (invitedBy == null) {
            throw new UserNotFoundException();
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
            throw new InvitationNotFoundException();
        }
        if (inv.isDeclined()) {
            throw new InvitationServiceException("This invitation has already been declined");
        }
        User invited = userRepository.findById(inv.getToUserId());
        User invitedBy = userRepository.findById(inv.getByUserId());
        if (invited == null) {
            throw new UserNotFoundException();
        }
        if (invitedBy == null) {
            throw new UserNotFoundException();
        }
        Program program = programRepository.findById(inv.getProgramId());
        if (program == null) {
            throw new ProgramNotFoundException();
        }
        if (program.getActiveCustomers().contains(invited)) {
            programServices.removeCustomer(invited.getId(), program.getId());
        }
        inv.setDeclined(true);
        invitationRepository.update(inv);
    }

    @Override
    public void delete(Invitation inv) {
        inv = invitationRepository.findById(inv.getId());
        if (inv == null) {
            throw new InvitationNotFoundException();
        }
        invitationRepository.delete(inv);
    }

    @Override
    public Invitation findById(Long id) {
        Invitation inv = invitationRepository.findById(id);
        if (inv == null) {
            throw new InvitationNotFoundException();
        }
        return inv;
    }

    @Override
    public List<Invitation> findByProgram(Long id) {
        Program program = programRepository.findById(id);
        if (program == null) {
            throw new ProgramNotFoundException();
        }
        return invitationRepository.findByProgram(id);
    }

    @Override
    public List<Invitation> findByInvitor(Long id) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new UserNotFoundException();
        }
        return invitationRepository.findByInvitor(id);
    }

    @Override
    public List<Invitation> findByInvitor(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UserNotFoundException();
        }
        return invitationRepository.findByInvitor(user.getId());
    }

    @Override
    public List<Invitation> findByInvitee(Long id) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new UserNotFoundException();
        }
        return invitationRepository.findByInvitee(id);
    }

    @Override
    public List<Invitation> findByInvitee(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UserNotFoundException();
        }
        return invitationRepository.findByInvitee(user.getId());
    }

    @Override
    public List<Invitation> findALl(String orderField) {
        if (orderField == null || orderField.equals("")) {
            orderField = "id";
        }
        return invitationRepository.findAll(orderField);
    }

    @Override
    public void setAllowedInvitationsBatch(List<String> emails, Long programId, Integer invitationsCount) {
        List<Long> customerIds = new ArrayList<>();
        Program program = programRepository.findById(programId);
        for (String email : emails) {
            User toBeAdded = userRepository.findByEmail(email);
            if (toBeAdded == null) {
                throw new UserNotFoundException();
            }
            customerIds.add(toBeAdded.getId());
        }
        if (customerIds.size() != emails.size()) {
            throw new UserNotFoundException();
        }
        if (program == null) {
            throw new ProgramNotFoundException();
        }
        List<Invitation> invitations = invitationRepository.findByProgram(programId);
        for (Invitation inv : invitations) {
            for (Long id : customerIds) {
                if (id.equals(inv.getByUserId())) {
                    inv.setInvitationsLeft(invitationsCount);
                    invitationRepository.update(inv);
                }
            }
        }

    }

    private Invitation sendWithEmail(Invitation invitation, String email) {
        User to = userRepository.findByEmail(email);
        if (to == null) {
            throw new UserNotFoundException();
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
