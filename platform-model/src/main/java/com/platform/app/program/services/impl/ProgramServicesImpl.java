package com.platform.app.program.services.impl;

import com.platform.app.common.exception.FieldNotValidException;
import com.platform.app.common.utils.ValidationUtils;
import com.platform.app.invitation.services.InvitationServices;
import com.platform.app.platformUser.exception.UserNotFoundException;
import com.platform.app.platformUser.model.Admin;
import com.platform.app.platformUser.model.Customer;
import com.platform.app.platformUser.repository.AdminRepository;
import com.platform.app.platformUser.repository.CustomerRepository;
import com.platform.app.program.exception.ProgramExistentException;
import com.platform.app.program.exception.ProgramNotFoundException;
import com.platform.app.program.exception.ProgramServiceException;
import com.platform.app.program.model.Application;
import com.platform.app.program.model.Program;
import com.platform.app.program.repository.ProgramRepository;
import com.platform.app.program.services.ProgramServices;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.Validator;
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
        if (program != null) {
            programRepository.delete(program);
        } else throw new ProgramNotFoundException();
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

    //Todo
    @Override
    public void registerApplication(Application stub, Long programId) {
        Program program = programRepository.findById(programId);
        if (program.getActiveApplications().contains(stub)) {
            throw new ProgramServiceException("This program already contains this application.");
        }
        program.addApplication(stub);
        programRepository.update(program);
    }

    //Todo
    @Override
    public void unregisterApplication(Application app, Long programId) {
        Program program = programRepository.findById(programId);
        if (!program.getActiveApplications().contains(app)) {
            throw new ProgramServiceException("This program does not contain the application.");
        }
        program.getActiveApplications();
        programRepository.update(program);
    }

    @Override
    public Program registerOnWaitingList(Long programId, Long custromerId) {
        Program program = programRepository.findById(programId);
        Customer customer = customerRepository.findById(custromerId);
        if (program == null) {
            throw new ProgramNotFoundException();
        }
        if (customer == null) {
            throw new UserNotFoundException();
        }
        program.getWaitingList().subscribe(custromerId);
        programRepository.update(program);
        return program;
    }

    @Override
    public SortedMap<Date, Customer> getCustomersOnWaitingList(Long programId) {
        Program program = programRepository.findById(programId);
        if (program == null) {
            throw new ProgramNotFoundException();
        }
        SortedMap<Date, Customer> result = new TreeMap<>();
        for (Map.Entry<Date, Long> entry : program.getWaitingList().getList().entrySet()) {
            Customer c = customerRepository.findById(entry.getValue());
            if (c != null) {
                result.put(entry.getKey(), c);
            }
        }
        return result;
    }

    @Override
    public void inviteFromWaitingList(Long adminId, Long programId, List<Long> userIds, Integer allowedInvitationsLeft) {
        List<Long> idsOnTheWaitingList = programRepository.findById(programId).getWaitingList().getOrderedIds();
        if (!idsOnTheWaitingList.containsAll(userIds)) {
            throw new ProgramServiceException("Inviting from the waiting list contains incorrect IDs");
        }
        if ((adminRepository.findById(adminId)) == null) {
            throw new ProgramServiceException("This admin does not exist");
        }
        List<Customer> customers = customerRepository.findByIdBatch(userIds);
        for (Customer item : customers) {
            item.setInvitationsLeft(allowedInvitationsLeft);
            customerRepository.update(item);
        }
        List<String> emails = customers.stream().map(Customer::getEmail).collect(Collectors.toList());
        invitationServices.sendInBatch(adminId, programId, emails);
    }

    private void validateProgram(Program program) throws FieldNotValidException, ProgramExistentException {
        if (programRepository.alreadyExists(program)) {
            throw new ProgramExistentException();
        }

        ValidationUtils.validateEntityFields(validator, program);
    }
}
