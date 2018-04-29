package com.platform.app.program.services;

import com.platform.app.platformUser.model.Admin;
import com.platform.app.platformUser.model.Customer;
import com.platform.app.program.model.Application;
import com.platform.app.program.model.Program;

import javax.ejb.Local;
import java.time.Instant;
import java.util.*;

@Local
public interface ProgramServices {
    Program create(Program program);

    void delete(Program program);

    void changeName(String newName, Long programId);

    void addAdmin(Long adminId, Long programId);

    void removeAdmin(Long adminId, Long programId);

    void addCustomer(Long customerId, Long programId);

    void removeCustomer(Long customerId, Long programId);

    List<Program> findByAdmin(Admin admin);

    List<Program> findAll(String orderfield);

    Program findById(Long id);

    Program findByApplication(Application application);

    Program findByName(String name);

    void registerApplication(UUID apiKey, Long programId);

    void unregisterApplication(UUID apiKey, Long programId);

    Program registerOnWaitingList(Long programId, Long customerId);

    Program unregisterOnWaitingList(Long programId, Long customerId);

    Map<Customer, Instant> getCustomersOnWaitingList(Long programId);

    void inviteFromWaitingList(Long adminId, Long programId, List<Long> userIds, Integer allowedInvitationsLeft);
}
