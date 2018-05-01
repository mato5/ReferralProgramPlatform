package com.platform.app.program.services.impl;

import com.platform.app.common.exception.FieldNotValidException;
import com.platform.app.common.utils.ValidationUtils;
import com.platform.app.program.exception.AppExistentException;
import com.platform.app.program.exception.AppNotFoundException;
import com.platform.app.program.exception.AppServiceException;
import com.platform.app.program.model.Application;
import com.platform.app.program.repository.ApplicationRepository;
import com.platform.app.program.services.ApplicationServices;

import javax.inject.Inject;
import javax.validation.Validator;
import java.util.List;
import java.util.UUID;

public class ApplicationServicesImpl implements ApplicationServices {

    @Inject
    ApplicationRepository applicationRepository;

    @Inject
    Validator validator;

    @Override
    public Application create(Application application) {
        validateApp(application);
        return applicationRepository.add(application);
    }

    @Override
    public void delete(Application application) {
        Application toBeDeleted = applicationRepository.findByApiKey(application.getApiKey());
        if (toBeDeleted == null) {
            throw new AppNotFoundException();
        }
        applicationRepository.delete(toBeDeleted);
    }

    @Override
    public List<Application> findAll(String orderField) {
        if (orderField == null) {
            orderField = "name";
        }
        return applicationRepository.findAll(orderField);
    }

    @Override
    public Application findByApiKey(UUID key) {
        Application app = applicationRepository.findByApiKey(key);
        if (app == null) {
            throw new AppNotFoundException();
        }
        return app;
    }

    @Override
    public Application findByURL(String URL) {
        Application app = applicationRepository.findByURL(URL);
        if (app == null) {
            throw new AppNotFoundException();
        }
        return app;
    }

    @Override
    public List<Application> findByName(String name) {
        return applicationRepository.findByName(name);
    }

    @Override
    public void changeName(String newName, UUID apiKey) {
        Application application = applicationRepository.findByApiKey(apiKey);
        if (application == null) {
            throw new AppNotFoundException();
        }
        if (newName == null) {
            throw new FieldNotValidException("name", "Application's name cannot be null value");
        }
        application.setName(newName);
        applicationRepository.update(application);
    }

    @Override
    public void changeDescription(String description, UUID apiKey) {
        Application application = applicationRepository.findByApiKey(apiKey);
        if (application == null) {
            throw new AppNotFoundException();
        }
        application.setDescription(description);
        applicationRepository.update(application);
    }

    @Override
    public void changeURL(String URL, UUID apiKey) {
        Application application = applicationRepository.findByApiKey(apiKey);
        if (application == null) {
            throw new AppNotFoundException();
        }
        if (applicationRepository.findByURL(URL) != null) {
            throw new AppServiceException("This URL is already used by an application");
        }
        application.setURL(URL);
        applicationRepository.update(application);
    }

    @Override
    public void changeInvitationURL(String invitationURL, UUID apiKey) {
        Application application = applicationRepository.findByApiKey(apiKey);
        if (application == null) {
            throw new AppNotFoundException();
        }
        if (applicationRepository.findByInvURL(invitationURL) != null) {
            throw new AppServiceException("This InvitationURL is already used by an application");
        }
        application.setURL(invitationURL);
        applicationRepository.update(application);
    }

    private void validateApp(Application app) throws FieldNotValidException, AppExistentException {
        if (applicationRepository.alreadyExists(app)) {
            throw new AppExistentException();
        }

        ValidationUtils.validateEntityFields(validator, app);
    }
}
