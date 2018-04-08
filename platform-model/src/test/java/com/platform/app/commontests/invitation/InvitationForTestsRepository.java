package com.platform.app.commontests.invitation;

import static com.platform.app.commontests.geoIP.GeoIPForTestsRepository.*;

import com.platform.app.geoIP.model.GeoIP;
import com.platform.app.invitation.model.Invitation;
import org.junit.Ignore;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Ignore
public class InvitationForTestsRepository {

    public static Invitation inv1() {
        Invitation temp = new Invitation();
        LocalDateTime a = LocalDateTime.of(2013, 1, 1, 14, 30);
        LocalDateTime b = LocalDateTime.of(2015, 1, 1, 14, 30);
        temp.setSent(a);
        temp.setByUserId(1L);
        temp.setToUserId(2L);
        temp.setActivated(b);
        temp.setProgramId(1L);
        temp.setActivatedLocation(test1());
        return temp;
    }

    public static Invitation inv2() {
        Invitation temp = new Invitation();
        LocalDateTime a = LocalDateTime.of(2013, 1, 1, 14, 30);
        LocalDateTime b = LocalDateTime.of(2015, 1, 1, 14, 30);
        temp.setSent(a);
        temp.setByUserId(2L);
        temp.setToUserId(3L);
        temp.setProgramId(1L);
        return temp;
    }

    public static Invitation invitationWithId(final Invitation invitation, final Long id) {
        invitation.setId(id);
        return invitation;
    }

    public static List<Invitation> allInvs() {
        return Arrays.asList(inv1(), inv2());
    }

}
