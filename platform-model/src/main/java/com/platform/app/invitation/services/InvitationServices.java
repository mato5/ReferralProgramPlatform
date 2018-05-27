package com.platform.app.invitation.services;

import com.platform.app.geoIP.model.GeoIP;
import com.platform.app.invitation.model.Invitation;

import javax.ejb.Local;
import java.util.List;

@Local
public interface InvitationServices {

    Invitation send(Invitation inv);

    int findInvitationsLeft(Long userId, Long programId);

    List<Invitation> sendInBatch(Long byUserId, Long programId, List<String> emails, Integer allowedInvitations);

    List<Invitation> sendInBatch(String email, Long programId, List<String> emails, Integer allowedInvitations);

    void accept(Invitation inv, GeoIP geoLocation);

    void decline(Invitation inv);

    void delete(Invitation inv);

    Invitation findById(Long id);

    List<Invitation> findByProgram(Long id);

    List<Invitation> findByInvitor(Long id);

    List<Invitation> findByInvitor(String email);

    List<Invitation> findByInvitee(Long id);

    List<Invitation> findByInvitee(String email);

    List<Invitation> findALl(String orderField);

    void setAllowedInvitationsBatch(List<String> emails, Long programId, Integer invitationsCount);
}
