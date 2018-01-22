package com.platform.app.platformUser.model;

import com.platform.app.platformUser.model.User;
import com.platform.app.program.model.Program;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@DiscriminatorValue("EMPLOYEE")
public class Admin extends User {
    private static final long serialVersionUID = 8976498066151628068L;

    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL)
    private Set<Program> programs = new HashSet<Program>();

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

	public Set<Program> getPrograms() {
		return programs;
	}

	public void addProgram(Program program){
		this.programs.add(program);
	}

	public void removeProgram(Program program){
		this.programs.remove(program);
	}

	public void setPrograms(Set<Program> programs) {
		this.programs = programs;
	}

    public Admin() {
        setUserType(UserType.EMPLOYEE);
    }

    @Override
    protected List<Roles> getDefaultRoles() {
        return Arrays.asList(Roles.ADMINISTRATOR);
    }

}