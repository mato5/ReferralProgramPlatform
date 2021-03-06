package com.platform.app.platformUser.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Arrays;
import java.util.List;

@Entity
@DiscriminatorValue("CUSTOMER")
public class Customer extends User {

    private static final long serialVersionUID = -6100894877953675646L;

    /*@OneToOne(cascade = CascadeType.ALL)
    private Invitation invitation;*/
    /*private Integer allowedInvitations;
    private Integer invitationsLeft;*/

    public Customer() {
        setUserType(UserType.CUSTOMER);
    }

    @Override
    protected List<Roles> getDefaultRoles() {
        return Arrays.asList(Roles.CUSTOMER);
    }

    /*public Invitation getInvitation() {
        return invitation;
    }

    public void setInvitation(Invitation invitation) {
        this.invitation = invitation;
    }*/


}