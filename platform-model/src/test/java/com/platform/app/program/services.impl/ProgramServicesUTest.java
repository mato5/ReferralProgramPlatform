package com.platform.app.program.services.impl;

import com.platform.app.common.exception.FieldNotValidException;
import com.platform.app.invitation.services.InvitationServices;
import com.platform.app.platformUser.exception.UserNotFoundException;
import com.platform.app.platformUser.model.Admin;
import com.platform.app.platformUser.model.Customer;
import com.platform.app.platformUser.model.User;
import com.platform.app.platformUser.repository.AdminRepository;
import com.platform.app.platformUser.repository.CustomerRepository;
import com.platform.app.program.exception.AppNotFoundException;
import com.platform.app.program.exception.ProgramExistentException;
import com.platform.app.program.exception.ProgramNotFoundException;
import com.platform.app.program.exception.ProgramServiceException;
import com.platform.app.program.model.Application;
import com.platform.app.program.model.Program;
import com.platform.app.program.model.WaitingList;
import com.platform.app.program.repository.ApplicationRepository;
import com.platform.app.program.repository.ProgramRepository;
import com.platform.app.program.services.ProgramServices;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.validation.Validation;
import javax.validation.Validator;

import java.lang.reflect.Array;
import java.time.Instant;
import java.util.*;

import static com.platform.app.commontests.platformUser.UserForTestsRepository.*;
import static com.platform.app.commontests.program.ApplicationForTestsRepository.app2;
import static com.platform.app.commontests.program.ApplicationForTestsRepository.appWithId;
import static com.platform.app.commontests.program.ProgramForTestsRepository.program1;
import static com.platform.app.commontests.program.ProgramForTestsRepository.programWithId;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;

public class ProgramServicesUTest {

    private Validator validator;
    private ProgramServices programServices;

    @Mock
    ProgramRepository programRepository;

    @Mock
    AdminRepository adminRepository;

    @Mock
    CustomerRepository customerRepository;

    @Mock
    InvitationServices invitationServices;

    @Mock
    ApplicationRepository applicationRepository;

    @Before
    public void initTestCase() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();

        MockitoAnnotations.initMocks(this);

        programServices = new ProgramServicesImpl();
        ((ProgramServicesImpl) programServices).applicationRepository = applicationRepository;
        ((ProgramServicesImpl) programServices).validator = validator;
        ((ProgramServicesImpl) programServices).programRepository = programRepository;
        ((ProgramServicesImpl) programServices).adminRepository = adminRepository;
        ((ProgramServicesImpl) programServices).customerRepository = customerRepository;
        ((ProgramServicesImpl) programServices).invitationServices = invitationServices;
    }

    @Test
    public void addProgramNullName() {
        Program program = program1();
        program.setName(null);
        addProgramWithInvalidField(program, "name");
    }

    @Test
    public void addProgramNoAdmin() {
        Program program = program1();
        program.setAdmins(new HashSet<>());
        addProgramWithInvalidField(program, "admins");
    }

    @Test
    public void addProgramCorrect() {
        Program program = program1();
        programServices.create(program);
        verify(programRepository).add(program);
    }

    @Test
    public void deleteProgramCorrect() {
        Program program = programWithId(program1(), 1L);
        when(programRepository.findById(1L)).thenReturn(program);
        programServices.delete(program);
        verify(programRepository).delete(program);
    }

    @Test(expected = ProgramNotFoundException.class)
    public void deleteProgramNotFound() {
        Program program = programWithId(program1(), 1L);
        when(programRepository.findById(1L)).thenReturn(null);
        programServices.delete(program);
    }

    @Test
    public void changeProgramName() {
        Program program = programWithId(program1(), 1L);
        when(programRepository.findByName("new name")).thenReturn(null);
        when(programRepository.findById(1L)).thenReturn(program);
        programServices.changeName("new name", 1L);
        verify(programRepository).update(program);
    }

    @Test(expected = ProgramExistentException.class)
    public void changeProgramNameAlreadyUsed() {
        Program program = programWithId(program1(), 1L);
        when(programRepository.findByName("new name")).thenReturn(program);
        when(programRepository.findById(1L)).thenReturn(program);
        programServices.changeName("new name", 1L);
    }

    @Test(expected = ProgramNotFoundException.class)
    public void changeProgramNameProgramNotFound() {
        Program program = programWithId(program1(), 1L);
        when(programRepository.findByName("new name")).thenReturn(null);
        when(programRepository.findById(1L)).thenReturn(null);
        programServices.changeName("new name", 1L);
    }

    @Test
    public void addAdmin() {
        Program program = programWithId(program1(), 1L);
        Admin user = new Admin();
        user.setEmail("new@mail.com");
        user.setCreatedAt(new Date());
        user.setName("John Smith");
        user.setPassword("111111");
        user.setRoles(Arrays.asList(User.Roles.EMPLOYEE, User.Roles.ADMINISTRATOR));
        user = adminWithIdAndCreatedAt(user, 1L);
        when(adminRepository.findById(1L)).thenReturn(user);
        when(programRepository.findById(1L)).thenReturn(program);
        programServices.addAdmin(1L, 1L);
        verify(programRepository).update(program);
    }

    @Test(expected = ProgramNotFoundException.class)
    public void addAdminProgramNotFound() {
        Program program = programWithId(program1(), 1L);
        Admin user = new Admin();
        user.setEmail("new@mail.com");
        user.setCreatedAt(new Date());
        user.setName("John Smith");
        user.setPassword("111111");
        user.setRoles(Arrays.asList(User.Roles.EMPLOYEE, User.Roles.ADMINISTRATOR));
        user = adminWithIdAndCreatedAt(user, 1L);
        when(adminRepository.findById(1L)).thenReturn(user);
        when(programRepository.findById(1L)).thenReturn(null);
        programServices.addAdmin(1L, 1L);
    }

    @Test(expected = UserNotFoundException.class)
    public void addAdminNotFound() {
        Program program = programWithId(program1(), 1L);
        Admin user = new Admin();
        user.setEmail("new@mail.com");
        user.setCreatedAt(new Date());
        user.setName("John Smith");
        user.setPassword("111111");
        user.setRoles(Arrays.asList(User.Roles.EMPLOYEE, User.Roles.ADMINISTRATOR));
        user = adminWithIdAndCreatedAt(user, 1L);
        when(adminRepository.findById(1L)).thenReturn(null);
        when(programRepository.findById(1L)).thenReturn(program);
        programServices.addAdmin(1L, 1L);
    }

    @Test(expected = ProgramServiceException.class)
    public void addAdminAlreadyContains() {
        Program program = programWithId(program1(), 1L);
        Admin admin = adminWithIdAndCreatedAt(admin(), 1L);
        program.setAdmins(new HashSet<>(Collections.singleton(admin)));
        when(adminRepository.findById(1L)).thenReturn(admin);
        when(programRepository.findById(1L)).thenReturn(program);
        programServices.addAdmin(1L, 1L);
    }

    @Test
    public void removeAdmin() {
        Program program = programWithId(program1(), 1L);
        Admin admin1 = adminWithIdAndCreatedAt(admin(), 1L);
        Admin admin2 = new Admin();
        admin2.setEmail("new@mail.com");
        admin2.setCreatedAt(new Date());
        admin2.setName("John Smith");
        admin2.setPassword("111111");
        admin2.setRoles(Arrays.asList(User.Roles.EMPLOYEE, User.Roles.ADMINISTRATOR));
        admin2 = adminWithIdAndCreatedAt(admin2, 1L);
        program.setAdmins(new HashSet<>(Arrays.asList(admin1, admin2)));
        when(adminRepository.findById(1L)).thenReturn(admin1);
        when(programRepository.findById(1L)).thenReturn(program);
        programServices.removeAdmin(1l, 1L);
        verify(programRepository).update(program);
    }

    @Test(expected = ProgramNotFoundException.class)
    public void removeAdminProgramNotFound() {
        Program program = programWithId(program1(), 1L);
        Admin admin1 = adminWithIdAndCreatedAt(admin(), 1L);
        Admin admin2 = new Admin();
        admin2.setEmail("new@mail.com");
        admin2.setCreatedAt(new Date());
        admin2.setName("John Smith");
        admin2.setPassword("111111");
        admin2.setRoles(Arrays.asList(User.Roles.EMPLOYEE, User.Roles.ADMINISTRATOR));
        admin2 = adminWithIdAndCreatedAt(admin2, 1L);
        program.setAdmins(new HashSet<>(Arrays.asList(admin1, admin2)));
        when(adminRepository.findById(1L)).thenReturn(admin1);
        when(programRepository.findById(1L)).thenReturn(null);
        programServices.removeAdmin(1l, 1L);
    }

    @Test(expected = UserNotFoundException.class)
    public void removeAdminNotFound() {
        Program program = programWithId(program1(), 1L);
        Admin admin1 = adminWithIdAndCreatedAt(admin(), 1L);
        Admin admin2 = new Admin();
        admin2.setEmail("new@mail.com");
        admin2.setCreatedAt(new Date());
        admin2.setName("John Smith");
        admin2.setPassword("111111");
        admin2.setRoles(Arrays.asList(User.Roles.EMPLOYEE, User.Roles.ADMINISTRATOR));
        admin2 = adminWithIdAndCreatedAt(admin2, 1L);
        program.setAdmins(new HashSet<>(Arrays.asList(admin1, admin2)));
        when(adminRepository.findById(1L)).thenReturn(null);
        when(programRepository.findById(1L)).thenReturn(program);
        programServices.removeAdmin(1l, 1L);
    }

    @Test(expected = ProgramServiceException.class)
    public void removeAdminNotContained() {
        Program program = programWithId(program1(), 1L);
        Admin admin1 = adminWithIdAndCreatedAt(admin(), 1L);
        Admin admin2 = new Admin();
        admin2.setEmail("new@mail.com");
        admin2.setCreatedAt(new Date());
        admin2.setName("John Smith");
        admin2.setPassword("111111");
        admin2.setRoles(Arrays.asList(User.Roles.EMPLOYEE, User.Roles.ADMINISTRATOR));
        admin2 = adminWithIdAndCreatedAt(admin2, 1L);
        program.setAdmins(new HashSet<>(Collections.singletonList(admin2)));
        when(adminRepository.findById(1L)).thenReturn(admin1);
        when(programRepository.findById(1L)).thenReturn(program);
        programServices.removeAdmin(1l, 1L);
    }

    @Test(expected = ProgramServiceException.class)
    public void removeAdminNoneLeft() {
        Program program = programWithId(program1(), 1L);
        Admin admin1 = adminWithIdAndCreatedAt(admin(), 1L);
        program.setAdmins(new HashSet<>(Collections.singletonList(admin1)));
        when(adminRepository.findById(1L)).thenReturn(admin1);
        when(programRepository.findById(1L)).thenReturn(program);
        programServices.removeAdmin(1l, 1L);
    }

    @Test
    public void addCustomer() {
        Program program = programWithId(program1(), 1L);
        Customer customer = customerWithIdAndCreatedAt(johnDoe(), 1L);
        when(programRepository.findById(1L)).thenReturn(program);
        when(customerRepository.findById(1L)).thenReturn(customer);
        programServices.addCustomer(1L, 1L);
        verify(programRepository).update(program);
    }

    @Test(expected = ProgramNotFoundException.class)
    public void addCustomerProgramNotFound() {
        Program program = programWithId(program1(), 1L);
        Customer customer = customerWithIdAndCreatedAt(johnDoe(), 1L);
        when(programRepository.findById(1L)).thenReturn(null);
        when(customerRepository.findById(1L)).thenReturn(customer);
        programServices.addCustomer(1L, 1L);
    }

    @Test(expected = UserNotFoundException.class)
    public void addCustomerNotFound() {
        Program program = programWithId(program1(), 1L);
        Customer customer = customerWithIdAndCreatedAt(johnDoe(), 1L);
        when(programRepository.findById(1L)).thenReturn(program);
        when(customerRepository.findById(1L)).thenReturn(null);
        programServices.addCustomer(1L, 1L);
    }

    @Test(expected = ProgramServiceException.class)
    public void addCustomerAlreadyContains() {
        Program program = programWithId(program1(), 1L);
        Customer customer = customerWithIdAndCreatedAt(mary(), 1L);
        program.setActiveCustomers(new HashSet<>(Collections.singleton(customer)));
        when(programRepository.findById(1L)).thenReturn(program);
        when(customerRepository.findById(1L)).thenReturn(customer);
        programServices.addCustomer(1L, 1L);
    }

    @Test
    public void removeCustomer() {
        Program program = programWithId(program1(), 1L);
        Customer customer = customerWithIdAndCreatedAt(johnDoe(), 1L);
        program.setActiveCustomers(new HashSet<>(Collections.singleton(customer)));
        when(programRepository.findById(1L)).thenReturn(program);
        when(customerRepository.findById(1L)).thenReturn(customer);
        programServices.removeCustomer(1L, 1L);
        verify(programRepository).update(program);
    }

    @Test(expected = ProgramNotFoundException.class)
    public void removeCustomerProgramNotFound() {
        Customer customer = customerWithIdAndCreatedAt(johnDoe(), 1L);
        when(programRepository.findById(1L)).thenReturn(null);
        when(customerRepository.findById(1L)).thenReturn(customer);
        programServices.removeCustomer(1L, 1L);
    }

    @Test(expected = UserNotFoundException.class)
    public void removeCustomerNotFound() {
        Program program = programWithId(program1(), 1L);
        when(programRepository.findById(1L)).thenReturn(program);
        when(customerRepository.findById(1L)).thenReturn(null);
        programServices.removeCustomer(1L, 1L);
    }

    @Test(expected = ProgramServiceException.class)
    public void removeCustomerNotContains() {
        Program program = programWithId(program1(), 1L);
        Customer customer = customerWithIdAndCreatedAt(johnDoe(), 1L);
        when(programRepository.findById(1L)).thenReturn(program);
        when(customerRepository.findById(1L)).thenReturn(customer);
        programServices.removeCustomer(1L, 1L);
    }

    @Test
    public void registerApplication() {
        Program program = programWithId(program1(), 1L);
        UUID uid = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");
        Application application = appWithId(app2(), uid);
        when(programRepository.findById(1L)).thenReturn(program);
        when(applicationRepository.findByApiKey(uid)).thenReturn(application);
        programServices.registerApplication(uid, 1L);
        verify(programRepository).update(program);
    }

    @Test(expected = ProgramNotFoundException.class)
    public void registerApplicationProgramNotFound() {
        Program program = programWithId(program1(), 1L);
        UUID uid = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");
        Application application = appWithId(app2(), uid);
        when(programRepository.findById(1L)).thenReturn(null);
        when(applicationRepository.findByApiKey(uid)).thenReturn(application);
        programServices.registerApplication(uid, 1L);
    }

    @Test(expected = AppNotFoundException.class)
    public void registerApplicationNotFound() {
        Program program = programWithId(program1(), 1L);
        UUID uid = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");
        Application application = appWithId(app2(), uid);
        when(programRepository.findById(1L)).thenReturn(program);
        when(applicationRepository.findByApiKey(uid)).thenReturn(null);
        programServices.registerApplication(uid, 1L);
    }

    @Test(expected = ProgramServiceException.class)
    public void registerApplicationAlreadyContains() {
        Program program = programWithId(program1(), 1L);
        UUID uid = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");
        Application application = appWithId(app2(), uid);
        program.setActiveApplications(new HashSet<>(Collections.singleton(application)));
        when(programRepository.findById(1L)).thenReturn(program);
        when(applicationRepository.findByApiKey(uid)).thenReturn(application);
        programServices.registerApplication(uid, 1L);
    }

    @Test
    public void unregisterApplication() {
        Program program = programWithId(program1(), 1L);
        UUID uid = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");
        Application application = appWithId(app2(), uid);
        program.setActiveApplications(new HashSet<>(Collections.singleton(application)));
        when(programRepository.findById(1L)).thenReturn(program);
        when(applicationRepository.findByApiKey(uid)).thenReturn(application);
        programServices.unregisterApplication(uid, 1L);
        verify(programRepository).update(program);
    }

    @Test(expected = ProgramNotFoundException.class)
    public void unregisterApplicationProgramNotFound() {
        Program program = programWithId(program1(), 1L);
        UUID uid = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");
        Application application = appWithId(app2(), uid);
        program.setActiveApplications(new HashSet<>(Collections.singleton(application)));
        when(programRepository.findById(1L)).thenReturn(null);
        when(applicationRepository.findByApiKey(uid)).thenReturn(application);
        programServices.unregisterApplication(uid, 1L);
    }

    @Test(expected = AppNotFoundException.class)
    public void unregisterApplicationNotFound() {
        Program program = programWithId(program1(), 1L);
        UUID uid = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");
        Application application = appWithId(app2(), uid);
        program.setActiveApplications(new HashSet<>(Collections.singleton(application)));
        when(programRepository.findById(1L)).thenReturn(program);
        when(applicationRepository.findByApiKey(uid)).thenReturn(null);
        programServices.unregisterApplication(uid, 1L);
    }

    @Test(expected = ProgramServiceException.class)
    public void unregisterApplicationNotContains() {
        Program program = programWithId(program1(), 1L);
        UUID uid = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");
        Application application = appWithId(app2(), uid);
        program.setActiveApplications(new HashSet<>());
        when(programRepository.findById(1L)).thenReturn(program);
        when(applicationRepository.findByApiKey(uid)).thenReturn(application);
        programServices.unregisterApplication(uid, 1L);
    }

    @Test
    public void registerOnWaitingList() {
        Program program = programWithId(program1(), 1L);
        Customer customer = customerWithIdAndCreatedAt(johnDoe(), 3L);
        when(programRepository.findById(1L)).thenReturn(program);
        when(customerRepository.findById(3L)).thenReturn(customer);
        programServices.registerOnWaitingList(1L, 3L);
        verify(programRepository).update(program);
    }

    @Test(expected = ProgramNotFoundException.class)
    public void registerOnWaitingListProgramNotFound() {
        Customer customer = customerWithIdAndCreatedAt(johnDoe(), 3L);
        when(programRepository.findById(1L)).thenReturn(null);
        when(customerRepository.findById(3L)).thenReturn(customer);
        programServices.registerOnWaitingList(1L, 3L);
    }

    @Test(expected = UserNotFoundException.class)
    public void registerOnWaitingListUserNotFound() {
        Program program = programWithId(program1(), 1L);
        when(programRepository.findById(1L)).thenReturn(program);
        when(customerRepository.findById(3L)).thenReturn(null);
        programServices.registerOnWaitingList(1L, 3L);
    }

    @Test(expected = ProgramServiceException.class)
    public void registerOnWaitingListAlreadyContains() {
        Program program = programWithId(program1(), 1L);
        Customer customer = customerWithIdAndCreatedAt(johnDoe(), 1L);
        when(programRepository.findById(1L)).thenReturn(program);
        when(customerRepository.findById(1L)).thenReturn(customer);
        programServices.registerOnWaitingList(1L, 1L);
    }

    @Test
    public void unregisterOnWaitingList() {
        Program program = programWithId(program1(), 1L);
        Customer customer = customerWithIdAndCreatedAt(johnDoe(), 1L);
        when(programRepository.findById(1L)).thenReturn(program);
        when(customerRepository.findById(1L)).thenReturn(customer);
        programServices.unregisterOnWaitingList(1L, 1L);
        verify(programRepository).update(program);
    }

    @Test(expected = ProgramNotFoundException.class)
    public void unregisterOnWaitingListProgramNotFound() {
        Program program = programWithId(program1(), 1L);
        Customer customer = customerWithIdAndCreatedAt(johnDoe(), 1L);
        when(programRepository.findById(1L)).thenReturn(null);
        when(customerRepository.findById(1L)).thenReturn(customer);
        programServices.unregisterOnWaitingList(1L, 1L);
    }

    @Test(expected = UserNotFoundException.class)
    public void unregisterOnWaitingListUserNotFound() {
        Program program = programWithId(program1(), 1L);
        Customer customer = customerWithIdAndCreatedAt(johnDoe(), 1L);
        when(programRepository.findById(1L)).thenReturn(program);
        when(customerRepository.findById(1L)).thenReturn(null);
        programServices.unregisterOnWaitingList(1L, 1L);
    }

    @Test(expected = ProgramServiceException.class)
    public void unregisterOnWaitingListNotContains() {
        Program program = programWithId(program1(), 1L);
        Customer customer = customerWithIdAndCreatedAt(johnDoe(), 3L);
        when(programRepository.findById(1L)).thenReturn(program);
        when(customerRepository.findById(3L)).thenReturn(customer);
        programServices.unregisterOnWaitingList(1L, 3L);
    }

    @Test
    public void getCustomersWaitingList() {
        Program program = programWithId(program1(), 1L);
        program.setWaitingList(new WaitingList());
        program.getWaitingList().subscribe(1L);
        program.getWaitingList().subscribe(2L);
        Customer customer1 = customerWithIdAndCreatedAt(johnDoe(), 1L);
        Customer customer2 = customerWithIdAndCreatedAt(mary(), 2L);
        when(customerRepository.findById(1L)).thenReturn(customer1);
        when(customerRepository.findById(2L)).thenReturn(customer2);
        when(programRepository.findById(1L)).thenReturn(program);
        Map<Customer, Instant> resultMap = programServices.getCustomersOnWaitingList(1L);
        assertThat(resultMap.size(), is(equalTo(2)));
        assertThat(new ArrayList<>(resultMap.keySet()), is(equalTo(Arrays.asList(customer1, customer2))));
    }

    @Test(expected = ProgramNotFoundException.class)
    public void getCustomersWaitingListProgramNotFound() {
        Program program = programWithId(program1(), 1L);
        program.setWaitingList(new WaitingList());
        program.getWaitingList().subscribe(1L);
        program.getWaitingList().subscribe(2L);
        Customer customer1 = customerWithIdAndCreatedAt(johnDoe(), 1L);
        Customer customer2 = customerWithIdAndCreatedAt(mary(), 2L);
        when(customerRepository.findById(1L)).thenReturn(customer1);
        when(customerRepository.findById(2L)).thenReturn(customer2);
        when(programRepository.findById(1L)).thenReturn(null);
        Map<Customer, Instant> resultMap = programServices.getCustomersOnWaitingList(1L);
    }

    @Test
    public void inviteFromWaitingList() {
        Program program = programWithId(program1(), 1L);
        Admin admin = adminWithIdAndCreatedAt(admin(), 100L);
        program.setAdmins(new HashSet<>(Collections.singleton(admin)));
        List<Customer> customers = Collections.singletonList(customerWithIdAndCreatedAt(johnDoe(), 1L));
        when(programRepository.findById(1L)).thenReturn(program);
        when(adminRepository.findById(100L)).thenReturn(admin);
        when(customerRepository.findByIdBatch(Collections.singletonList(1L))).thenReturn(customers);
        when(customerRepository.findById(1L)).thenReturn(customers.get(0));
        programServices.inviteFromWaitingList(100L, 1L, Collections.singletonList(1L), 5);
        verify(customerRepository).update(customers.get(0));
        verify(invitationServices).sendInBatch(100L, 1L, Collections.singletonList(johnDoe().getEmail()));
        verify(programRepository).update(program);
    }

    @Test(expected = ProgramNotFoundException.class)
    public void inviteFromWaitingListProgramNotFound() {
        Program program = programWithId(program1(), 1L);
        Admin admin = adminWithIdAndCreatedAt(admin(), 100L);
        List<Customer> customers = Collections.singletonList(customerWithIdAndCreatedAt(johnDoe(), 1L));
        when(programRepository.findById(1L)).thenReturn(null);
        when(adminRepository.findById(100L)).thenReturn(admin);
        when(customerRepository.findByIdBatch(Collections.singletonList(1L))).thenReturn(customers);
        when(customerRepository.findById(1L)).thenReturn(customers.get(0));
        programServices.inviteFromWaitingList(100L, 1L, Collections.singletonList(1L), 5);
    }

    @Test(expected = ProgramServiceException.class)
    public void inviteFromWaitingListIncorrectIds() {
        Program program = programWithId(program1(), 1L);
        program.setWaitingList(new WaitingList());
        Admin admin = adminWithIdAndCreatedAt(admin(), 100L);
        List<Customer> customers = Collections.singletonList(customerWithIdAndCreatedAt(johnDoe(), 1L));
        when(programRepository.findById(1L)).thenReturn(program);
        when(adminRepository.findById(100L)).thenReturn(admin);
        when(customerRepository.findByIdBatch(Collections.singletonList(1L))).thenReturn(customers);
        when(customerRepository.findById(1L)).thenReturn(customers.get(0));
        programServices.inviteFromWaitingList(100L, 1L, Collections.singletonList(1L), 5);
    }

    @Test(expected = UserNotFoundException.class)
    public void inviteFromWaitingListAdminNotFound() {
        Program program = programWithId(program1(), 1L);
        Admin admin = adminWithIdAndCreatedAt(admin(), 100L);
        List<Customer> customers = Collections.singletonList(customerWithIdAndCreatedAt(johnDoe(), 1L));
        when(programRepository.findById(1L)).thenReturn(program);
        when(adminRepository.findById(100L)).thenReturn(null);
        when(customerRepository.findByIdBatch(Collections.singletonList(1L))).thenReturn(customers);
        when(customerRepository.findById(1L)).thenReturn(customers.get(0));
        programServices.inviteFromWaitingList(100L, 1L, Collections.singletonList(1L), 5);
    }

    @Test(expected = ProgramServiceException.class)
    public void inviteFromWaitingListAdminNotManaging() {
        Program program = programWithId(program1(), 1L);
        Admin admin = adminWithIdAndCreatedAt(admin(), 100L);
        List<Customer> customers = Collections.singletonList(customerWithIdAndCreatedAt(johnDoe(), 1L));
        when(programRepository.findById(1L)).thenReturn(program);
        when(adminRepository.findById(100L)).thenReturn(admin);
        when(customerRepository.findByIdBatch(Collections.singletonList(1L))).thenReturn(customers);
        when(customerRepository.findById(1L)).thenReturn(customers.get(0));
        programServices.inviteFromWaitingList(100L, 1L, Collections.singletonList(1L), 5);
    }

    private void addProgramWithInvalidField(Program program, String expectedInvalidFieldName) {
        try {
            programServices.create(program);
            fail("An error should have been thrown");
        } catch (final FieldNotValidException e) {
            assertThat(e.getFieldName(), is(equalTo(expectedInvalidFieldName)));
        }
    }

}
