package com.platform.app.program.model;

import com.platform.app.platformUser.model.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class Program {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Column(unique = true)
    private String name;
    @Size(min = 1)
    @ManyToMany(cascade = {CascadeType.MERGE})
    @JoinTable(name = "PROGRAM_ADMIN", joinColumns = @JoinColumn(name = "PROGRAM_ID"),
            inverseJoinColumns = @JoinColumn(name = "ADMIN_ID"))
    private Set<User> admins = new HashSet<>();
    @ManyToMany
    private Set<User> activeCustomers = new HashSet<>();
    @Embedded
    private WaitingList waitingList = new WaitingList();
    @OneToMany(cascade = CascadeType.MERGE)
    private Set<Application> activeApplications = new HashSet<>();


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<User> getActiveCustomers() {
        return activeCustomers;
    }

    public void setActiveCustomers(Set<User> activeCustomers) {
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

    public void addActiveCustomers(User customer) {
        this.activeCustomers.add(customer);
    }

    public void removeActiveCustomers(User customer) {
        this.activeCustomers.remove(customer);
    }

    public Set<User> getAdmins() {
        return admins;
    }

    public void setAdmins(Set<User> admins) {
        this.admins = admins;
    }

    public void addAdmin(User admin) {
        this.admins.add(admin);
    }

    public void removeAdmin(User admin) {
        this.admins.remove(admin);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Program program = (Program) o;
        return Objects.equals(name, program.name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name);
    }
}
