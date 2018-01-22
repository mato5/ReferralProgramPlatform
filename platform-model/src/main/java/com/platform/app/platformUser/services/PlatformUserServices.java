package com.platform.app.platformUser.services;

import com.platform.app.common.model.PaginatedData;
import com.platform.app.platformUser.exception.UserNotFoundException;
import com.platform.app.platformUser.model.Admin;
import com.platform.app.platformUser.model.User;
import com.platform.app.platformUser.model.filter.UserFilter;
import com.platform.app.program.model.Program;

import java.util.List;

public interface PlatformUserServices {
    User add(User user);

    User findById(Long id);

    void update(User user);

    void updatePassword(Long id, String password);

    User findByEmail(String email) throws UserNotFoundException;

    User findByEmailAndPassword(String email, String password);

    PaginatedData<User> findByFilter(UserFilter userFilter);

    void delete(User user);

    void addExistingProgram(Program program, Admin admin);

    void setInvitationsLeft(List<Long> customerIds, Integer invitationsLeft);
}
