package com.platform.app.platformUser.services;

import com.platform.app.common.model.PaginatedData;
import com.platform.app.platformUser.exception.UserNotFoundException;
import com.platform.app.platformUser.model.User;
import com.platform.app.platformUser.model.filter.UserFilter;

import javax.ejb.Local;

@Local
public interface PlatformUserServices {
    User add(User user);

    User findById(Long id);

    void update(User user);

    void updatePassword(Long id, String password);

    void updateEmail(Long id, String newEmail);

    User findByEmail(String email) throws UserNotFoundException;

    User findByEmailAndPassword(String email, String password);

    PaginatedData<User> findByFilter(UserFilter userFilter);

    void delete(User user);

}
