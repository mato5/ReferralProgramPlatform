package com.platform.app.program.services.impl;

import com.platform.app.common.exception.FieldNotValidException;
import com.platform.app.common.utils.ValidationUtils;
import com.platform.app.invitation.model.Invitation;
import com.platform.app.invitation.repository.InvitationRepository;
import com.platform.app.invitation.services.InvitationServices;
import com.platform.app.platformUser.exception.UserNotFoundException;
import com.platform.app.platformUser.model.User;
import com.platform.app.platformUser.repository.PlatformUserRepository;
import com.platform.app.program.exception.AppNotFoundException;
import com.platform.app.program.exception.ProgramExistentException;
import com.platform.app.program.exception.ProgramNotFoundException;
import com.platform.app.program.exception.ProgramServiceException;
import com.platform.app.program.model.Application;
import com.platform.app.program.model.Program;
import com.platform.app.program.repository.ApplicationRepository;
import com.platform.app.program.repository.ProgramRepository;
import com.platform.app.program.services.ProgramServices;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Validator;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Stateless
@Transactional
public class ProgramServicesImpl implements ProgramServices {

    @Inject
    ProgramRepository programRepository;

    @Inject
    InvitationRepository invitationRepository;

    @Inject
    PlatformUserRepository userRepository;

    @Inject
    ApplicationRepository applicationRepository;

    @Inject
    InvitationServices invitationServices;

    @Inject
    Validator validator;

    @Override
    public Program create(Program program) {
        validateProgram(program);
        return programRepository.add(program);
    }

    @Override
    public void delete(Program program) {
        program = programRepository.findById(program.getId());
        if (program == null) {
            throw new ProgramNotFoundException();
        }
        List<Invitation> invitations = invitationRepository.findByProgram(program.getId());
        for (Invitation item : invitations) {
            invitationRepository.delete(item);
        }
        programRepository.delete(program);
    }

    @Override
    public void changeName(String newName, Long programId) {
        if (programRepository.findByName(newName) != null) {
            throw new ProgramExistentException();
        }
        Program program = programRepository.findById(programId);
        if (program == null) {
            throw new ProgramNotFoundException();
        }
        program.setName(newName);
        programRepository.update(program);
    }

    @Override
    public void addAdmin(Long adminId, Long programId) {
        Program program = programRepository.findById(programId);
        User admin = userRepository.findById(adminId);
        if (program == null) {
            throw new ProgramNotFoundException();
        }
        if (admin == null) {
            throw new UserNotFoundException();
        }
        if (program.getAdmins().contains(admin)) {
            throw new ProgramServiceException("This program already contains the specified admin");
        }
        program.addAdmin(admin);
        programRepository.update(program);

    }

    @Override
    public void removeAdmin(Long adminId, Long programId) {
        Program program = programRepository.findById(programId);
        User admin = userRepository.findById(adminId);
        if (program == null) {
            throw new ProgramNotFoundException();
        }
        if (admin == null) {
            throw new UserNotFoundException();
        }
        if (!program.getAdmins().contains(admin)) {
            throw new ProgramServiceException("This program does not contain the specified admin");
        }
        if (program.getAdmins().size() == 1) {
            throw new ProgramServiceException("The program will have no admins left");
        }
        program.removeAdmin(admin);
        programRepository.update(program);
    }

    @Override
    public void addCustomer(Long customerId, Long programId) {
        Program program = programRepository.findById(programId);
        User customer = userRepository.findById(customerId);
        if (program == null) {
            throw new ProgramNotFoundException();
        }
        if (customer == null) {
            throw new UserNotFoundException();
        }
        if (program.getActiveCustomers().contains(customer)) {
            throw new ProgramServiceException("This program already contains the specified customer");
        }
        if (program.getWaitingList().getOrderedIds().contains(customerId)) {
            program.getWaitingList().unsubscribe(customerId);
        }
        program.addActiveCustomers(customer);
        programRepository.update(program);
    }

    @Override
    public void removeCustomer(Long customerId, Long programId) {
        Program program = programRepository.findById(programId);
        User customer = userRepository.findById(customerId);
        if (program == null) {
            throw new ProgramNotFoundException();
        }
        if (customer == null) {
            throw new UserNotFoundException();
        }
        if (!program.getActiveCustomers().contains(customer)) {
            throw new ProgramServiceException("This program does not contain the specified customer");
        }
        program.removeActiveCustomers(customer);
        programRepository.update(program);
    }


    @Override
    public List<Program> findByAdmin(User admin) {
        admin = userRepository.findById(admin.getId());
        if (admin == null) {
            throw new UserNotFoundException();
        }
        return programRepository.findByAdmin(admin);
    }

    @Override
    public List<Program> findByActiveUser(User customer) {
        customer = userRepository.findById(customer.getId());
        if (customer == null) {
            throw new UserNotFoundException();
        }
        return programRepository.findByActiveUser(customer);
    }

    @Override
    public List<Program> findAll(String orderfield) {
        if (orderfield == null) {
            orderfield = "name";
        }
        return programRepository.findAll(orderfield);
    }

    @Override
    public Program findById(Long id) {
        Program program = programRepository.findById(id);
        if (program == null) {
            throw new ProgramNotFoundException();
        }
        return program;
    }

    @Override
    public List<Program> findByApplication(Application application) {
        List<Program> programs = programRepository.findByApplication(application);
        if (programs == null) {
            throw new ProgramNotFoundException();
        }
        return programs;
    }

    @Override
    public Program findByName(String name) {
        Program program = programRepository.findByName(name);
        if (program == null) {
            throw new ProgramNotFoundException();
        }
        return program;
    }

    @Override
    public void registerApplication(UUID apiKey, Long programId) {
        Program program = programRepository.findById(programId);
        Application app = applicationRepository.findByApiKey(apiKey);
        if (program == null) {
            throw new ProgramNotFoundException();
        }
        if (app == null) {
            throw new AppNotFoundException();
        }

        if (program.getActiveApplications().contains(app)) {
            throw new ProgramServiceException("This program already contains this application.");
        }
        program.addApplication(app);
        programRepository.update(program);
    }

    @Override
    public void unregisterApplication(UUID apiKey, Long programId) {
        Program program = programRepository.findById(programId);
        Application app = applicationRepository.findByApiKey(apiKey);
        if (program == null) {
            throw new ProgramNotFoundException();
        }
        if (app == null) {
            throw new AppNotFoundException();
        }
        if (!program.getActiveApplications().contains(app)) {
            throw new ProgramServiceException("This program does not contain the application.");
        }
        List<Program> programsAppUsedIn = programRepository.findByApplication(app);
        program.removeApplication(app);
        programRepository.update(program);
        if (programsAppUsedIn.size() == 1) {
            applicationRepository.delete(app);
        }
    }

    @Override
    public Program registerOnWaitingList(Long programId, Long customerId) {
        Program program = programRepository.findById(programId);
        User customer = userRepository.findById(customerId);
        if (program == null) {
            throw new ProgramNotFoundException();
        }
        if (customer == null) {
            throw new UserNotFoundException();
        }
        if (program.getActiveCustomers().contains(customer)) {
            throw new ProgramServiceException("This user is already participating in the specified program");
        }
        if (program.getAdmins().contains(customer)) {
            throw new ProgramServiceException("This user is the admin of the specified program");
        }
        if (program.getWaitingList().getOrderedIds().contains(customerId)) {
            throw new ProgramServiceException("This waiting list already contains the specified customer.");
        }
        program.getWaitingList().subscribe(customerId);
        programRepository.update(program);
        return program;
    }

    @Override
    public Program unregisterOnWaitingList(Long programId, Long customerId) {
        Program program = programRepository.findById(programId);
        User customer = userRepository.findById(customerId);
        if (program == null) {
            throw new ProgramNotFoundException();
        }
        if (customer == null) {
            throw new UserNotFoundException();
        }
        if (!program.getWaitingList().getOrderedIds().contains(customerId)) {
            throw new ProgramServiceException("This waiting list does not contain the specified customer.");
        }
        program.getWaitingList().unsubscribe(customerId);
        programRepository.update(program);
        return program;
    }

    @Override
    public Map<User, Instant> getCustomersOnWaitingList(Long programId) {
        Program program = programRepository.findById(programId);
        if (program == null) {
            throw new ProgramNotFoundException();
        }
        Map<User, Instant> result = new LinkedHashMap<>();
        for (Map.Entry<Long, Instant> entry : program.getWaitingList().getOrderedList().entrySet()) {
            User customer = userRepository.findById(entry.getKey());
            if (customer != null) {
                result.put(customer, entry.getValue());
            }
        }
        return result;
    }

    @Override
    public void inviteFromWaitingList(Long adminId, Long programId, List<Long> userIds, Integer
            allowedInvitationsLeft) {
        Program program = programRepository.findById(programId);
        if (program == null) {
            throw new ProgramNotFoundException();
        }
        List<Long> idsOnTheWaitingList = program.getWaitingList().getOrderedIds();
        if (!idsOnTheWaitingList.containsAll(userIds)) {
            throw new ProgramServiceException("Inviting from the waiting list contains incorrect user IDs");
        }
        User admin = userRepository.findById(adminId);
        if (admin == null) {
            throw new UserNotFoundException();
        }
        if (!program.getAdmins().contains(admin)) {
            throw new ProgramServiceException("This program is not managed by the provided admin account");
        }
        List<User> customers = userRepository.findByIdBatch(userIds);
        if (customers.size() != userIds.size()) {
            throw new UserNotFoundException();
        }
        List<String> emails = customers.stream().map(User::getEmail).collect(Collectors.toList());
        invitationServices.sendInBatch(adminId, programId, emails, allowedInvitationsLeft);
        idsOnTheWaitingList.forEach(x -> unregisterOnWaitingList(programId, x));
    }

    @Override
    public User.Roles getUsersRole(Long userId, Long programId) {
        User user = userRepository.findById(userId);
        Program program = programRepository.findById(programId);
        if (user == null) {
            throw new UserNotFoundException();
        }
        if (program == null) {
            throw new ProgramNotFoundException();
        }
        return getUsersRole(user, program);
    }

    @Override
    public User.Roles getUsersRole(String email, Long programId) {
        User user = userRepository.findByEmail(email);
        Program program = programRepository.findById(programId);
        if (user == null) {
            throw new UserNotFoundException();
        }
        if (program == null) {
            throw new ProgramNotFoundException();
        }
        return getUsersRole(user, program);
    }

    @Override
    public User.Roles getUsersRole(User user, Program program) {
        if (program.getAdmins().contains(user)) {
            return User.Roles.ADMINISTRATOR;
        }
        if (program.getActiveCustomers().contains(user)) {
            return User.Roles.CUSTOMER;
        }
        return User.Roles.NONE;
    }

    private void validateProgram(Program program) throws FieldNotValidException, ProgramExistentException {
        if (programRepository.alreadyExists(program)) {
            throw new ProgramExistentException();
        }

        ValidationUtils.validateEntityFields(validator, program);
    }
}
