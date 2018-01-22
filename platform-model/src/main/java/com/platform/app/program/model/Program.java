package com.platform.app.program.model;

import com.platform.app.platformUser.model.Admin;
import com.platform.app.platformUser.model.Customer;

import javax.inject.Inject;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.*;

@Entity
public class Program {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Column(unique = true)
    private String name;
    @NotNull
    @ManyToOne
    private Admin admin;
    @OneToMany
    private Set<Customer> activeCustomers = new HashSet<Customer>();
    @Embedded
    private WaitingList waitingList = new WaitingList();
    @Embedded
    private ApplicationStub activeApplication;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<Customer> getActiveCustomers() {
        return activeCustomers;
    }

    public void setActiveCustomers(Set<Customer> activeCustomers) {
        this.activeCustomers = activeCustomers;
    }

    public String getName() {
        return name;
    }

    public WaitingList getWaitingList() {
        return waitingList;
    }

    public void setWaitingList(WaitingList waitingList) {
        this.waitingList = waitingList;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ApplicationStub getActiveApplication() {
        return activeApplication;
    }

    public void setActiveApplication(ApplicationStub activeApplication) {
        this.activeApplication = activeApplication;
    }

    public void addActiveCustomers(Customer customer) {
        if (!this.activeCustomers.contains(customer)) {
            this.activeCustomers.add(customer);
        }
    }

    public void removeActiveCustomers(Customer customer) {
        if (this.activeCustomers.contains(customer)) {
            this.activeCustomers.remove(customer);
        }
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Program program = (Program) o;
        return Objects.equals(getId(), program.getId()) &&
                Objects.equals(getName(), program.getName());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getId(), getName());
    }
}
