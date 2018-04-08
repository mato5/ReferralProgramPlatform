package com.platform.app.invitation.model;

import com.platform.app.geoIP.model.GeoIP;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class Invitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private Long byUserId;
    @NotNull
    private Long toUserId;
    @NotNull
    private Long programId;
    private boolean declined;
    private LocalDateTime activated;
    private LocalDateTime sent;
    @Embedded
    private GeoIP activatedLocation;

    public Invitation() {
        this.sent = LocalDateTime.now();
        this.declined = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getByUserId() {
        return byUserId;
    }

    public Long getProgramId() {
        return programId;
    }

    public boolean isDeclined() {
        return declined;
    }

    public void setDeclined(boolean declined) {
        this.declined = declined;
    }

    public void setProgramId(Long programId) {
        this.programId = programId;
    }

    public void setByUserId(Long byUserId) {
        this.byUserId = byUserId;
    }

    public Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
    }

    public LocalDateTime getActivated() {
        return activated;
    }

    public void setActivated(LocalDateTime activated) {
        this.activated = activated;
    }

    public LocalDateTime getSent() {
        return sent;
    }

    public void setSent(LocalDateTime sent) {
        this.sent = sent;
    }

    public GeoIP getActivatedLocation() {
        return activatedLocation;
    }

    public void setActivatedLocation(GeoIP activatedLocation) {
        this.activatedLocation = activatedLocation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Invitation that = (Invitation) o;
        return declined == that.declined &&
                Objects.equals(byUserId, that.byUserId) &&
                Objects.equals(toUserId, that.toUserId) &&
                Objects.equals(programId, that.programId) &&
                Objects.equals(activatedLocation, that.activatedLocation);
    }

    @Override
    public int hashCode() {

        return Objects.hash(byUserId, toUserId, programId, declined, activatedLocation);
    }

    @Override
    public String toString() {
        return "Invitation{" +
                "id=" + id +
                ", byUserId=" + byUserId +
                ", toUserId=" + toUserId +
                ", programId=" + programId +
                ", declined=" + declined +
                ", activated=" + activated +
                ", sent=" + sent +
                ", activatedLocation=" + activatedLocation +
                '}';
    }
}
