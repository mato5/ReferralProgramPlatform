package com.platform.app.geoIP.model;

import javax.persistence.*;
import java.util.Objects;

@Embeddable
public class GeoIP {

    /*@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;*/

    private String ipAddress;
    private String city;
    private String latitude;
    private String longitude;

    public GeoIP() {

    }

    public GeoIP(String ipAddress, String city, String latitude, String longitude) {
        this.ipAddress = ipAddress;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeoIP geoIP = (GeoIP) o;
        return Objects.equals(getIpAddress(), geoIP.getIpAddress()) &&
                Objects.equals(getCity(), geoIP.getCity());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getIpAddress(), getCity());
    }

    @Override
    public String toString() {
        return "GeoIP{" +
                "ipAddress='" + ipAddress + '\'' +
                ", city='" + city + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                '}';
    }
}
