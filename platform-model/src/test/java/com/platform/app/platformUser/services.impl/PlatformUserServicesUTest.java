package com.platform.app.platformUser.services.impl;

import static com.platform.app.commontests.platformUser.UserArgumentMatcher.userEq;
import static com.platform.app.commontests.platformUser.UserForTestsRepository.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;

import com.platform.app.common.exception.FieldNotValidException;
import com.platform.app.common.model.PaginatedData;
import com.platform.app.common.utils.PasswordUtils;
import com.platform.app.platformUser.exception.UserExistentException;
import com.platform.app.platformUser.exception.UserNotFoundException;
import com.platform.app.platformUser.exception.UserServiceException;
import com.platform.app.platformUser.model.Customer;
import com.platform.app.platformUser.model.User;
import com.platform.app.platformUser.model.filter.UserFilter;
import com.platform.app.platformUser.repository.CustomerRepository;
import com.platform.app.platformUser.repository.PlatformUserRepository;
import com.platform.app.platformUser.services.PlatformUserServices;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PlatformUserServicesUTest {
    private Validator validator;
    private PlatformUserServices userServices;

    @Mock
    private PlatformUserRepository userRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Before
    public void initTestCase() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();

        MockitoAnnotations.initMocks(this);

        userServices = new PlatformUserServicesImpl();
        ((PlatformUserServicesImpl) userServices).userRepository = userRepository;
        ((PlatformUserServicesImpl) userServices).customerRepository = customerRepository;
        ((PlatformUserServicesImpl) userServices).validator = validator;
    }

    @Test
    public void addUserWithNullName() {
        final User user = johnDoe();
        user.setName(null);
        addUserWithInvalidField(user, "name");
    }

    @Test
    public void addUserWithShortName() {
        final User user = johnDoe();
        user.setName("Jo");
        addUserWithInvalidField(user, "name");
    }

    @Test
    public void addUserWithNullEmail() {
        final User user = johnDoe();
        user.setEmail(null);
        addUserWithInvalidField(user, "email");
    }

    @Test
    public void addUserWithInvalidEmail() {
        final User user = johnDoe();
        user.setEmail("invalidemail");
        addUserWithInvalidField(user, "email");
    }

    @Test
    public void addUserWithNullPassword() {
        final User user = johnDoe();
        user.setPassword(null);
        addUserWithInvalidField(user, "password");
    }

    @Test(expected = UserExistentException.class)
    public void addExistentUser() {
        when(userRepository.alreadyExists(johnDoe())).thenThrow(new UserExistentException());

        userServices.add(johnDoe());
    }

    @Test
    public void addValidUser() {
        when(userRepository.alreadyExists(johnDoe())).thenReturn(false);
        when(userRepository.add(userEq(userWithEncryptedPassword(johnDoe()))))
                .thenReturn(userWithIdAndCreatedAt(johnDoe(), 1L));

        final User user = userServices.add(johnDoe());
        assertThat(user.getId(), is(equalTo(1L)));
    }

    @Test(expected = UserNotFoundException.class)
    public void findUserByIdNotFound() {
        when(userRepository.findById(1L)).thenReturn(null);

        userServices.findById(1L);
    }

    @Test
    public void findUserById() {
        when(userRepository.findById(1L)).thenReturn(userWithIdAndCreatedAt(johnDoe(), 1L));

        final User user = userServices.findById(1L);
        assertThat(user, is(notNullValue()));
        assertThat(user.getName(), is(equalTo(johnDoe().getName())));
    }

    @Test
    public void updateUserWithNullName() {
        when(userRepository.findById(1L)).thenReturn(userWithIdAndCreatedAt(johnDoe(), 1L));

        final User user = userWithIdAndCreatedAt(johnDoe(), 1L);
        user.setName(null);

        try {
            userServices.update(user);
        } catch (final FieldNotValidException e) {
            assertThat(e.getFieldName(), is(equalTo("name")));
        }
    }

    @Test(expected = UserExistentException.class)
    public void updateUserExistent() throws Exception {
        when(userRepository.findById(1L)).thenReturn(userWithIdAndCreatedAt(johnDoe(), 1L));

        final User user = userWithIdAndCreatedAt(johnDoe(), 1L);
        when(userRepository.alreadyExists(user)).thenReturn(true);

        userServices.update(user);
    }

    @Test(expected = UserNotFoundException.class)
    public void updateUserNotFound() throws Exception {
        final User user = userWithIdAndCreatedAt(johnDoe(), 1L);
        when(userRepository.findById(1L)).thenReturn(null);

        userServices.update(user);
    }

    @Test
    public void updateValidUser() throws Exception {
        final User user = userWithIdAndCreatedAt(johnDoe(), 1L);
        user.setPassword(null);
        when(userRepository.findById(1L)).thenReturn(userWithIdAndCreatedAt(johnDoe(), 1L));

        userServices.update(user);

        final User expectedUser = userWithIdAndCreatedAt(johnDoe(), 1L);
        verify(userRepository).update(userEq(expectedUser));
    }

    @Test(expected = UserNotFoundException.class)
    public void updatePasswordUserNotFound() {
        when(userRepository.findById(1L)).thenThrow(new UserNotFoundException());

        userServices.updatePassword(1L, "123456");
    }

    @Test
    public void updatePassword() throws Exception {
        final User user = userWithIdAndCreatedAt(johnDoe(), 1L);
        when(userRepository.findById(1L)).thenReturn(user);

        userServices.updatePassword(1L, "654654");

        final User expectedUser = userWithIdAndCreatedAt(johnDoe(), 1L);
        expectedUser.setPassword(PasswordUtils.encryptPassword("654654"));
        verify(userRepository).update(userEq(expectedUser));
    }

    @Test(expected = UserNotFoundException.class)
    public void findUserByEmailNotFound() throws UserNotFoundException {
        when(userRepository.findByEmail(johnDoe().getEmail())).thenReturn(null);

        userServices.findByEmail(johnDoe().getEmail());
    }

    @Test
    public void findUserByEmail() throws UserNotFoundException {
        when(userRepository.findByEmail(johnDoe().getEmail())).thenReturn(userWithIdAndCreatedAt(johnDoe(), 1L));

        final User user = userServices.findByEmail(johnDoe().getEmail());
        assertThat(user, is(notNullValue()));
        assertThat(user.getName(), is(equalTo(johnDoe().getName())));
    }

    @Test(expected = UserNotFoundException.class)
    public void findUserByEmailAndPasswordNotFound() {
        final User user = johnDoe();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(null);

        userServices.findByEmailAndPassword(user.getEmail(), user.getPassword());
    }

    @Test(expected = UserNotFoundException.class)
    public void findUserByAndPasswordEmailWithInvalidPassword() throws UserNotFoundException {
        final User user = johnDoe();
        user.setPassword("1111");

        User userReturned = userWithIdAndCreatedAt(johnDoe(), 1L);
        userReturned = userWithEncryptedPassword(userReturned);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(userReturned);

        userServices.findByEmailAndPassword(user.getEmail(), user.getPassword());
    }

    @Test
    public void findUserByAndPasswordEmail() throws UserNotFoundException {
        User user = johnDoe();

        User userReturned = userWithIdAndCreatedAt(johnDoe(), 1L);
        userReturned = userWithEncryptedPassword(userReturned);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(userReturned);

        user = userServices.findByEmailAndPassword(user.getEmail(), user.getPassword());
        assertThat(user, is(notNullValue()));
        assertThat(user.getName(), is(equalTo(johnDoe().getName())));
    }

    @Test
    public void findUserByFilter() {
        final PaginatedData<User> users = new PaginatedData<>(1,
                Collections.singletonList(userWithIdAndCreatedAt(johnDoe(), 1L)));
        when(userRepository.findByFilter((UserFilter) any())).thenReturn(users);

        final PaginatedData<User> usersReturned = userServices.findByFilter(new UserFilter());
        assertThat(usersReturned.getNumberOfRows(), is(equalTo(1)));
        assertThat(usersReturned.getRow(0).getName(), is(equalTo(johnDoe().getName())));
    }


    @Test
    public void updateUserCorrectEmail() {
        final User user = userWithIdAndCreatedAt(johnDoe(), 1L);
        when(userRepository.findById(1L)).thenReturn(user);
        when(userRepository.findByEmail(any())).thenReturn(null);

        userServices.updateEmail(1L, "myMail@mail.com");

        final User expectedUser = userWithIdAndCreatedAt(johnDoe(), 1L);
        expectedUser.setEmail("myMail@mail.com");
        verify(userRepository).update(userEq(expectedUser));
    }

    @Test
    public void updateUserWrongEmail() {
        User user = userWithIdAndCreatedAt(johnDoe(), 1L);
        when(userRepository.findById(1L)).thenReturn(user);
        when(userRepository.findByEmail(any())).thenReturn(null);

        try {
            userServices.updateEmail(1L, "");
            fail("An error should have been thrown");
        } catch (final FieldNotValidException e) {
            assertThat(e.getFieldName(), is(equalTo("email")));
        }

    }

    private void addUserWithInvalidField(final User user, final String expectedInvalidFieldName) {
        try {
            userServices.add(user);
            fail("An error should have been thrown");
        } catch (final FieldNotValidException e) {
            assertThat(e.getFieldName(), is(equalTo(expectedInvalidFieldName)));
        }
    }

}
