package com.platform.app.program.services;

import com.platform.app.platformUser.model.User;
import com.platform.app.program.model.Application;
import com.platform.app.program.model.Program;

import javax.ejb.Local;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Local
public interface ProgramServices {
    Program create(Program program);

    void delete(Program program);

    void changeName(String newName, Long programId);

    void addAdmin(Long adminId, Long programId);

    void removeAdmin(Long adminId, Long programId);

    void addCustomer(Long customerId, Long programId);

    void removeCustomer(Long customerId, Long programId);

    List<Program> findByAdmin(User admin);

    List<Program> findAll(String orderfield);

    Program findById(Long id);

    Program findByApplication(Application application);

    Program findByName(String name);

    void registerApplication(UUID apiKey, Long programId);

    void unregisterApplication(UUID apiKey, Long programId);

    Program registerOnWaitingList(Long programId, Long customerId);

    Program unregisterOnWaitingList(Long programId, Long customerId);

    Map<User, Instant> getCustomersOnWaitingList(Long programId);

    void inviteFromWaitingList(Long adminId, Long programId, List<Long> userIds, Integer allowedInvitationsLeft);

    User.Roles getUsersRole(Long userId, Long programId);

    User.Roles getUsersRole(String email, Long programId);

    User.Roles getUsersRole(User user, Program program);
}
