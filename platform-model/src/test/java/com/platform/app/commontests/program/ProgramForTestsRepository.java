package com.platform.app.commontests.program;

import com.platform.app.platformUser.model.Admin;
import com.platform.app.platformUser.model.Customer;
import com.platform.app.program.model.ApplicationStub;
import com.platform.app.program.model.Program;
import org.hibernate.boot.jaxb.SourceType;
import org.junit.Ignore;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.platform.app.commontests.platformUser.UserForTestsRepository.admin;
import static com.platform.app.commontests.platformUser.UserForTestsRepository.johnDoe;
import static com.platform.app.commontests.platformUser.UserForTestsRepository.mary;
import static com.platform.app.commontests.utils.TestRepositoryUtils.findByPropertyNameAndValue;

@Ignore
public final class ProgramForTestsRepository {

    private ProgramForTestsRepository() {
    }

    /**
     * Program with 1 user on the waiting list and 1 active user and an application
     *
     * @return program
     */
    public static Program program1() {
        final Program program = new Program();
        program.setName("program1");
        program.setAdmin(admin());
        ApplicationStub stub = new ApplicationStub();
        stub.setDescription("application of program1");
        program.setActiveApplication(stub);
        program.getWaitingList().subscribe(johnDoe().getId());
        program.addActiveCustomers(mary());

        return program;
    }

    /**
     * Empty program with only the required fields prefilled
     *
     * @return program
     */
    public static Program program2() {
        final Program program = new Program();
        program.setAdmin(admin());
        program.setName("program2");

        return program;
    }

    public static Program ProgramWithId(final Program program, final Long id) {
        program.setId(id);
        return program;
    }

    public static List<Program> allPrograms() {
        return Arrays.asList(program1(), program2());
    }

    public static Program normalizeDependencies(final Program program, final EntityManager em) {
        //Admin
        final Admin associatedAdmin = findByPropertyNameAndValue(em, Admin.class, "email", program.getAdmin()
                .getEmail());
        program.setAdmin(associatedAdmin);
        System.out.println("Admin:");
        System.out.println(associatedAdmin);

        //Active customers
        for (Customer item : program.getActiveCustomers()) {
            Customer associatedActiveCustomer = findByPropertyNameAndValue(em, Customer.class, "email", item.getEmail());
            item.setId(associatedActiveCustomer.getId());
            System.out.println("Active:");
            System.out.println(associatedActiveCustomer);
            System.out.println(item);
        }

        /*//Waiting list
        for (Customer item : program.getWaitingList()) {
            Customer associatedActiveCustomer = findByPropertyNameAndValue(em, Customer.class, "email", item.getEmail());
            item.setId(associatedActiveCustomer.getId());
            System.out.println("Waiting lis:");
            System.out.println(associatedActiveCustomer);
            System.out.println(item);
        }*/

        return program;
    }

}