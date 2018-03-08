package com.platform.app.program.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;

/**
 * A class representing an application
 */
@Entity
public class Application {

    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    private UUID apiKey;

    @NotNull
    private String name;

    private String description;

    @NotNull
    @Column(unique = true)
    private String URL;

    @NotNull
    @Column(unique = true)
    private String invitationURL;


    public UUID getApiKey() {
        return apiKey;
    }

    public void setApiKey(UUID apiKey) {
        this.apiKey = apiKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getInvitationURL() {
        return invitationURL;
    }

    public void setInvitationURL(String invitationURL) {
        this.invitationURL = invitationURL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Application that = (Application) o;
        return Objects.equals(getName(), that.getName()) &&
                Objects.equals(getURL(), that.getURL());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getName(), getURL());
    }
}
