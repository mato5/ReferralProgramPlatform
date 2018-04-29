package com.platform.app.program.services.impl;

import com.platform.app.common.exception.FieldNotValidException;
import com.platform.app.common.utils.ValidationUtils;
import com.platform.app.invitation.services.InvitationServices;
import com.platform.app.platformUser.exception.UserNotFoundException;
import com.platform.app.platformUser.model.Admin;
import com.platform.app.platformUser.model.Customer;
import com.platform.app.platformUser.model.User;
import com.platform.app.platformUser.repository.AdminRepository;
import com.platform.app.platformUser.repository.CustomerRepository;
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
import javax.validation.Validator;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Stateless
public class ProgramServicesImpl implements ProgramServices {

    @Inject
    ProgramRepository programRepository;

    @Inject
    AdminRepository adminRepository;

    @Inject
    CustomerRepository customerRepository;

    @Inject
    PlatformUserRepository userRepository;

    @Inject
    InvitationServices invitationServices;

    @Inject
    ApplicationRepository applicationRepository;


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
        if (program != null) {
            programRepository.delete(program);
        } else throw new ProgramNotFoundException();
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
        Admin admin = adminRepository.findById(adminId);
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
        Admin admin = adminRepository.findById(adminId);
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
        Customer customer = customerRepository.findById(customerId);
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
        Customer customer = customerRepository.findById(customerId);
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
    public List<Program> findByAdmin(Admin admin) {
        admin = adminRepository.findById(admin.getId());
        return programRepository.findByAdmin(admin);
    }

    @Override
    public List<Program> findAll(String orderfield) {
        return programRepository.findAll(orderfield);
    }

    @Override
    public Program findById(Long id) {
        return programRepository.findById(id);
    }

    @Override
    public Program findByApplication(Application application) {
        return programRepository.findByApplication(application);
    }

    @Override
    public Program findByName(String name) {
        return programRepository.findByName(name);
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
        program.removeApplication(app);
        programRepository.update(program);
    }

    @Override
    public Program registerOnWaitingList(Long programId, Long customerId) {
        Program program = programRepository.findById(programId);
        Customer customer = customerRepository.findById(customerId);
        if (program == null) {
            throw new ProgramNotFoundException();
        }
        if (customer == null) {
            throw new UserNotFoundException();
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
        Customer customer = customerRepository.findById(customerId);
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
    public Map<Customer, Instant> getCustomersOnWaitingList(Long programId) {
        Program program = programRepository.findById(programId);
        if (program == null) {
            throw new ProgramNotFoundException();
        }
        Map<Customer, Instant> result = new LinkedHashMap<>();
        for (Map.Entry<Long, Instant> entry : program.getWaitingList().getOrderedList().entrySet()) {
            Customer c = customerRepository.findById(entry.getKey());
            if (c != null) {
                result.put(c, entry.getValue());
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
        Admin admin = adminRepository.findById(adminId);
        if (admin == null) {
            throw new UserNotFoundException();
        }
        if (!program.getAdmins().contains(admin)) {
            throw new ProgramServiceException("This program is not managed by the provided admin account");
        }
        List<Customer> customers = customerRepository.findByIdBatch(userIds);
        if (customers.size() != userIds.size()) {
            throw new UserNotFoundException();
        }
        List<String> emails = customers.stream().map(Customer::getEmail).collect(Collectors.toList());
        invitationServices.sendInBatch(adminId, programId, emails, allowedInvitationsLeft);
        idsOnTheWaitingList.forEach(x -> unregisterOnWaitingList(programId, x));
    }

    @Override
    public User.Roles getUsersRole(Long userId, Long programId) {
        Program program = programRepository.findById(programId);
        User user = userRepository.findById(userId);
        if (program.getAdmins().contains(user)) {
            return User.Roles.ADMINISTRATOR;
        }
        if (program.getActiveCustomers().contains(user)) {
            return User.Roles.CUSTOMER;
        }
        return null;
    }

    private void validateProgram(Program program) throws FieldNotValidException, ProgramExistentException {
        if (programRepository.alreadyExists(program)) {
            throw new ProgramExistentException();
        }

        ValidationUtils.validateEntityFields(validator, program);
    }
}
