package com.platform.app.program.services;

import com.platform.app.program.model.Application;

import java.util.List;
import java.util.UUID;

public interface ApplicationServices {

    Application create(Application application);

    void delete(Application application);

    List<Application> findAll(String orderField);

    Application findByApiKey(UUID key);

    Application findByURL(String URL);

    List<Application> findByName(String name);

    void changeName(String newName, UUID apiKey);

    void changeDescription(String description, UUID apiKey);

    void changeURL(String URL, UUID apiKey);

    void changeInvitationURL(String invitationURL, UUID apiKey);
}
