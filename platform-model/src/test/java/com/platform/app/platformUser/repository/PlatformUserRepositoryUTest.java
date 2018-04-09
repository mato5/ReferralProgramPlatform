package com.platform.app.platformUser.repository;

import com.platform.app.common.model.PaginatedData;
import com.platform.app.common.model.filter.PaginationData;
import com.platform.app.commontests.utils.TestBaseRepository;
import com.platform.app.platformUser.model.User;
import com.platform.app.platformUser.model.User.UserType;
import com.platform.app.platformUser.model.filter.UserFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static com.platform.app.commontests.platformUser.UserForTestsRepository.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class PlatformUserRepositoryUTest extends TestBaseRepository {
    private PlatformUserRepository platformUserRepository;

    @Before
    public void initTestCase() {
        initializeTestDB();

        platformUserRepository = new PlatformUserRepository();
        platformUserRepository.em = em;
    }

    @After
    public void setDownTestCase() {
        closeEntityManager();
    }

    @Test
    public void addCustomerAndFindIt() {
        final Long userAddedId = dbCommandExecutor.executeCommand(() -> {
            return platformUserRepository.add(johnDoe()).getId();
        });
        assertThat(userAddedId, is(notNullValue()));

        final User user = platformUserRepository.findById(userAddedId);
        assertUser(user, johnDoe(), UserType.CUSTOMER);
    }

    @Test
    public void findUserByIdNotFound() {
        final User user = platformUserRepository.findById(999L);
        assertThat(user, is(nullValue()));
    }

    @Test
    public void findByIdBatch() {
        List<Long> ids = new ArrayList<>();
        for (User item : allUsers()) {
            Long userAddedId = dbCommandExecutor.executeCommand(() -> {
                return platformUserRepository.add(item).getId();
            });
            ids.add(userAddedId);
        }
        List<User> users = dbCommandExecutor.executeCommand(() -> {
            return platformUserRepository.findByIdBatch(ids);
        });

        for (User item : users) {
            for (User another : allUsers()) {
                if (item.getEmail().equals(another.getEmail())) {
                    assertUser(item, another);
                }
            }
        }

    }

    @Test
    public void updateCustomer() {
        final Long userAddedId = dbCommandExecutor.executeCommand(() -> {
            return platformUserRepository.add(johnDoe()).getId();
        });
        assertThat(userAddedId, is(notNullValue()));

        final User user = platformUserRepository.findById(userAddedId);
        assertThat(user.getName(), is(equalTo(johnDoe().getName())));

        user.setName("New name");
        dbCommandExecutor.executeCommand(() -> {
            platformUserRepository.update(user);
            return null;
        });

        final User userAfterUpdate = platformUserRepository.findById(userAddedId);
        assertThat(userAfterUpdate.getName(), is(equalTo("New name")));
    }

    @Test
    public void alreadyExistsUserWithoutId() {
        dbCommandExecutor.executeCommand(() -> {
            return platformUserRepository.add(johnDoe()).getId();
        });

        assertThat(platformUserRepository.alreadyExists(johnDoe()), is(equalTo(true)));
        assertThat(platformUserRepository.alreadyExists(admin()), is(equalTo(false)));
    }

    @Test
    public void alreadyExistsCategoryWithId() {
        final User customer = dbCommandExecutor.executeCommand(() -> {
            platformUserRepository.add(admin());
            return platformUserRepository.add(johnDoe());
        });

        assertFalse(platformUserRepository.alreadyExists(customer));

        customer.setEmail(admin().getEmail());
        assertThat(platformUserRepository.alreadyExists(customer), is(equalTo(true)));

        customer.setEmail("newemail@domain.com");
        assertThat(platformUserRepository.alreadyExists(customer), is(equalTo(false)));
    }

    @Test
    public void findUserByEmail() {
        dbCommandExecutor.executeCommand(() -> {
            return platformUserRepository.add(johnDoe());
        });

        final User user = platformUserRepository.findByEmail(johnDoe().getEmail());
        assertUser(user, johnDoe(), UserType.CUSTOMER);
    }

    @Test
    public void findUserByEmailNotFound() {
        final User user = platformUserRepository.findByEmail(johnDoe().getEmail());
        assertThat(user, is(nullValue()));
    }

    @Test
    public void findByFilterWithPagingOrderingByNameDescending() {
        loadDataForFindByFilter();

        UserFilter userFilter = new UserFilter();
        userFilter.setPaginationData(new PaginationData(0, 2, "name", PaginationData.OrderMode.DESCENDING));

        PaginatedData<User> result = platformUserRepository.findByFilter(userFilter);
        assertThat(result.getNumberOfRows(), is(equalTo(3)));
        assertThat(result.getRows().size(), is(equalTo(2)));
        assertThat(result.getRow(0).getName(), is(equalTo(mary().getName())));
        assertThat(result.getRow(1).getName(), is(equalTo(johnDoe().getName())));

        userFilter = new UserFilter();
        userFilter.setPaginationData(new PaginationData(2, 2, "name", PaginationData.OrderMode.DESCENDING));

        result = platformUserRepository.findByFilter(userFilter);
        assertThat(result.getNumberOfRows(), is(equalTo(3)));
        assertThat(result.getRows().size(), is(equalTo(1)));
        assertThat(result.getRow(0).getName(), is(equalTo(admin().getName())));
    }

    @Test
    public void findByFilterFilteringByName() {
        loadDataForFindByFilter();

        final UserFilter userFilter = new UserFilter();
        userFilter.setName("m");
        userFilter.setPaginationData(new PaginationData(0, 2, "name", PaginationData.OrderMode.ASCENDING));

        final PaginatedData<User> result = platformUserRepository.findByFilter(userFilter);
        assertThat(result.getNumberOfRows(), is(equalTo(2)));
        assertThat(result.getRows().size(), is(equalTo(2)));
        assertThat(result.getRow(0).getName(), is(equalTo(admin().getName())));
        assertThat(result.getRow(1).getName(), is(equalTo(mary().getName())));
    }

    @Test
    public void findByFilterFilteringByNameAndType() {
        loadDataForFindByFilter();

        final UserFilter userFilter = new UserFilter();
        userFilter.setName("m");
        userFilter.setUserType(UserType.EMPLOYEE);
        userFilter.setPaginationData(new PaginationData(0, 2, "name", PaginationData.OrderMode.ASCENDING));

        final PaginatedData<User> result = platformUserRepository.findByFilter(userFilter);
        assertThat(result.getNumberOfRows(), is(equalTo(1)));
        assertThat(result.getRows().size(), is(equalTo(1)));
        assertThat(result.getRow(0).getName(), is(equalTo(admin().getName())));
    }

    @Test
    public void deleteExistingUser() {
        final User user = dbCommandExecutor.executeCommand(() -> {
            return platformUserRepository.add(johnDoe());
        });
        dbCommandExecutor.executeCommand(() -> {
            platformUserRepository.delete(platformUserRepository.findById(user.getId()));
            return null;
        });

        assertThat(platformUserRepository.findAll("id").size(), is(equalTo(0)));
    }


    private void loadDataForFindByFilter() {
        dbCommandExecutor.executeCommand(() -> {
            allUsers().forEach(platformUserRepository::add);
            return null;
        });
    }

    private void assertUser(final User actualUser, final User expectedUser, final UserType expectedUserType) {
        assertThat(actualUser.getName(), is(equalTo(expectedUser.getName())));
        assertThat(actualUser.getEmail(), is(equalTo(expectedUser.getEmail())));
        assertThat(actualUser.getRoles().toArray(), is(equalTo(expectedUser.getRoles().toArray())));
        assertThat(actualUser.getCreatedAt(), is(notNullValue()));
        assertThat(actualUser.getPassword(), is(expectedUser.getPassword()));
        assertThat(actualUser.getUserType(), is(equalTo(expectedUserType)));
    }

    private void assertUser(final User actualUser, final User expectedUser) {
        assertThat(actualUser.getName(), is(equalTo(expectedUser.getName())));
        assertThat(actualUser.getEmail(), is(equalTo(expectedUser.getEmail())));
        assertThat(listEqualsIgnoreOrder(actualUser.getRoles(), expectedUser.getRoles()), is(equalTo(true)));
        assertThat(actualUser.getCreatedAt(), is(notNullValue()));
        assertThat(actualUser.getPassword(), is(expectedUser.getPassword()));
    }

    private <T> boolean listEqualsIgnoreOrder(List<T> list1, List<T> list2) {
        return new HashSet<>(list1).equals(new HashSet<>(list2));
    }

}
