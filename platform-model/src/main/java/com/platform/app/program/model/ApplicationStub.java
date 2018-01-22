package com.platform.app.program.model;

import javax.persistence.Embeddable;
import java.util.Objects;

/**
 * A class representing a simple stub for a running application
 */
@Embeddable
public class ApplicationStub {
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationStub that = (ApplicationStub) o;
        return Objects.equals(getDescription(), that.getDescription());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getDescription());
    }
}
