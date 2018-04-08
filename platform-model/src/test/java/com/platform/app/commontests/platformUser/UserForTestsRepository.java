package com.platform.app.commontests.platformUser;

import com.platform.app.common.utils.DateUtils;
import com.platform.app.common.utils.PasswordUtils;
import com.platform.app.platformUser.model.*;
import com.platform.app.platformUser.model.User.Roles;
import com.platform.app.platformUser.model.User.UserType;
import org.junit.Ignore;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.platform.app.commontests.invitation.InvitationForTestsRepository.*;

@Ignore
public class UserForTestsRepository {

    public static Customer johnDoe() {
        final User user = new Customer();
        user.setName("John doe");
        user.setEmail("john@domain.com");
        user.setPassword("123456");
        Customer customer = null;
        if (user.getUserType() == UserType.CUSTOMER) {
            customer = (Customer) user;
        }
        return customer;
    }

    public static Customer mary() {
        final User user = new Customer();
        user.setName("Mary");
        user.setEmail("mary@domain.com");
        user.setPassword("987789");
        Customer customer = null;
        if (user.getUserType() == UserType.CUSTOMER) {
            customer = (Customer) user;
        }
        return customer;
    }

    public static Admin admin() {
        final User user = new Admin();
        user.setName("Admin");
        user.setEmail("admin@domain.com");
        user.setPassword("654321");
        user.setRoles(Arrays.asList(Roles.EMPLOYEE, Roles.ADMINISTRATOR));
        Admin admin = null;
        if (user.getUserType() == UserType.EMPLOYEE) {
            admin = (Admin) user;
        }
        ;
        return admin;
    }

    public static List<User> allUsers() {
        return Arrays.asList(admin(), johnDoe(), mary());
    }

    public static List<Customer> allCustomers() {
        return Arrays.asList(johnDoe(), mary());
    }

    public static List<Admin> allAdmins() {
        return Arrays.asList(admin());
    }

    public static User userWithIdAndCreatedAt(final User user, final Long id) {
        user.setId(id);
        user.setCreatedAt(DateUtils.getAsDateTime("2015-01-03T22:35:42Z"));

        return user;
    }

    public static Customer customerWithIdAndCreatedAt(Customer user, Long id) {
        user.setId(id);
        user.setCreatedAt(DateUtils.getAsDateTime("2016-01-03T22:35:42Z"));

        return user;
    }

    public static Admin adminWithIdAndCreatedAt(Admin user, Long id) {
        user.setId(id);
        user.setCreatedAt(DateUtils.getAsDateTime("2017-01-03T22:35:42Z"));

        return user;
    }

    public static User userWithEncryptedPassword(final User user) {
        user.setPassword(PasswordUtils.encryptPassword(user.getPassword()));
        return user;
    }

}