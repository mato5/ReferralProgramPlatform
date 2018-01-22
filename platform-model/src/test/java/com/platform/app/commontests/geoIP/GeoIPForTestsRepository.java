package com.platform.app.commontests.geoIP;

import com.platform.app.geoIP.model.GeoIP;
import org.junit.Ignore;

import java.util.Arrays;
import java.util.List;

@Ignore
public class GeoIPForTestsRepository {

    public static GeoIP test1() {

        return new GeoIP("test1", "1", "1", "1");
    }

    public static GeoIP test2() {
        return new GeoIP("test2", "2", "2", "2");
    }

    public static GeoIP test3() {
        return new GeoIP("test3", "3", "3", "3");
    }

    public static GeoIP test4() {
        return new GeoIP("test4", "4", "4", "4");
    }

    /*public static GeoIP geoWithId(final GeoIP geoIP, final Long id) {
        geoIP.setId(id);
        return geoIP;
    }*/

    public static List<GeoIP> allGeos() {
        return Arrays.asList(test1(), test2(), test3(), test4());
    }
}
