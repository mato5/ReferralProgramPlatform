package com.platform.app.program.model;

import com.platform.app.platformUser.model.Admin;
import com.platform.app.platformUser.model.Customer;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
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
    @ManyToMany(cascade = CascadeType.PERSIST)
    private Set<Admin> admins = new HashSet<>();
    @OneToMany
    private Set<Customer> activeCustomers = new HashSet<Customer>();
    @Embedded
    private WaitingList waitingList = new WaitingList();
    @OneToMany(cascade = CascadeType.PERSIST)
    private Set<Application> activeApplications = new HashSet<>();


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

    public Set<Application> getActiveApplications() {
        return activeApplications;
    }

    public void setActiveApplications(Set<Application> activeApplications) {
        this.activeApplications = activeApplications;
    }

    public void addApplication(Application app) {
        this.activeApplications.add(app);
    }

    public void removeApplication(Application app) {
        this.activeApplications.remove(app);
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

    public Set<Admin> getAdmins() {
        return admins;
    }

    public void setAdmins(Set<Admin> admins) {
        this.admins = admins;
    }

    public void addAdmin(Admin admin){
        this.admins.add(admin);
    }

    public void removeAdmin(Admin admin){
        this.admins.remove(admin);
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
