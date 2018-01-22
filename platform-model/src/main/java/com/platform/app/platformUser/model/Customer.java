package com.platform.app.platformUser.model;

import com.platform.app.invitation.model.Invitation;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Entity
@DiscriminatorValue("CUSTOMER")
public class Customer extends User{

    private static final long serialVersionUID = -6100894877953675646L;

    /*@OneToOne(cascade = CascadeType.ALL)
    private Invitation invitation;*/
    private Integer allowedInvitations;
    private Integer invitationsLeft;

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

    public Integer getAllowedInvitations() {
        return allowedInvitations;
    }

    public void setAllowedInvitations(Integer allowedInvitations) {
        this.allowedInvitations = allowedInvitations;
    }

    public Integer getInvitationsLeft() {
        return invitationsLeft;
    }

    public void setInvitationsLeft(Integer invitationsLeft) {
        this.invitationsLeft = invitationsLeft;
    }

}