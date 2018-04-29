package com.platform.app.platformUser.services.impl;

import com.platform.app.common.exception.FieldNotValidException;
import com.platform.app.common.model.PaginatedData;
import com.platform.app.common.utils.PasswordUtils;
import com.platform.app.common.utils.ValidationUtils;
import com.platform.app.invitation.model.Invitation;
import com.platform.app.invitation.repository.InvitationRepository;
import com.platform.app.platformUser.exception.UserExistentException;
import com.platform.app.platformUser.exception.UserNotFoundException;
import com.platform.app.platformUser.exception.UserServiceException;
import com.platform.app.platformUser.model.Admin;
import com.platform.app.platformUser.model.Customer;
import com.platform.app.platformUser.model.User;
import com.platform.app.platformUser.model.filter.UserFilter;
import com.platform.app.platformUser.repository.AdminRepository;
import com.platform.app.platformUser.repository.CustomerRepository;
import com.platform.app.platformUser.repository.PlatformUserRepository;
import com.platform.app.platformUser.services.PlatformUserServices;
import com.platform.app.program.exception.ProgramNotFoundException;
import com.platform.app.program.model.Program;
import com.platform.app.program.repository.ProgramRepository;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.Validator;
import java.util.List;

@Stateless
public class PlatformUserServicesImpl implements PlatformUserServices {

    @Inject
    PlatformUserRepository userRepository;

    @Inject
    CustomerRepository customerRepository;

    @Inject
    InvitationRepository invitationRepository;

    @Inject
    Validator validator;

    @Override
    public User add(final User user) {
        validateUser(user);
        user.setPassword(PasswordUtils.encryptPassword(user.getPassword()));

        return userRepository.add(user);
    }

    @Override
    public User findById(final Long id) {
        final User user = userRepository.findById(id);
        if (user == null) {
            throw new UserNotFoundException();
        }
        return user;
    }

    @Override
    public void update(final User user) {
        final User existentUser = findById(user.getId());
        user.setPassword(existentUser.getPassword());

        validateUser(user);

        userRepository.update(user);
    }

    @Override
    public void updatePassword(final Long id, final String password) {
        final User user = findById(id);
        user.setPassword(PasswordUtils.encryptPassword(password));

        userRepository.update(user);
    }

    @Override
    public void updateEmail(Long id, String newEmail) {
        User user = userRepository.findById(id);
        if (userRepository.findByEmail(newEmail) != null) {
            throw new UserServiceException("This email is already in use");
        }
        user.setEmail(newEmail);
        update(user);
    }

    @Override
    public User findByEmail(final String email) throws UserNotFoundException {
        final User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UserNotFoundException();
        }
        return user;
    }

    @Override
    public User findByEmailAndPassword(final String email, final String password) {
        final User user = findByEmail(email);

        if (!user.getPassword().equals(PasswordUtils.encryptPassword(password))) {
            throw new UserNotFoundException();
        }

        return user;
    }

    @Override
    public PaginatedData<User> findByFilter(final UserFilter userFilter) {
        return userRepository.findByFilter(userFilter);
    }

    @Override
    public void delete(User user) {
        user = userRepository.findById(user.getId());
        if (user == null) {
            throw new UserNotFoundException();
        }
        List<Invitation> invitationsOfUser = invitationRepository.findByInvitee(user.getId());
        for (Invitation item : invitationsOfUser) {
            invitationRepository.delete(item);
        }
        userRepository.delete(user);
    }

    private void validateUser(final User user) throws FieldNotValidException, UserExistentException {
        if (userRepository.alreadyExists(user)) {
            throw new UserExistentException();
        }

        ValidationUtils.validateEntityFields(validator, user);
    }


}
