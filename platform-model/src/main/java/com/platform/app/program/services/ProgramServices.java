package com.platform.app.program.services;

import com.platform.app.platformUser.model.Admin;
import com.platform.app.platformUser.model.Customer;
import com.platform.app.program.model.Application;
import com.platform.app.program.model.Program;

import java.util.Date;
import java.util.List;
import java.util.SortedMap;

public interface ProgramServices {
    Program create(Program program);

    void delete(Program program);

    List<Program> findByAdmin(Admin admin);

    List<Program> findAll(String orderfield);

    Program findById(Long id);

    void registerApplication(Application stub, Long programId);

    void unregisterApplication(Application app, Long programId);

    Program registerOnWaitingList(Long programId, Long custromerId);

    SortedMap<Date, Customer> getCustomersOnWaitingList(Long programId);

    void inviteFromWaitingList(Long adminId, Long programId, List<Long> userIds, Integer allowedInvitationsLeft);
}
