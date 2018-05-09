package com.platform.app.program.services.impl;

import com.platform.app.common.exception.FieldNotValidException;
import com.platform.app.program.exception.AppNotFoundException;
import com.platform.app.program.exception.AppServiceException;
import com.platform.app.program.model.Application;
import com.platform.app.program.repository.ApplicationRepository;
import com.platform.app.program.repository.ProgramRepository;
import com.platform.app.program.services.ApplicationServices;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.validation.Validation;
import javax.validation.Validator;
import java.util.UUID;

import static com.platform.app.commontests.program.ApplicationForTestsRepository.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;


public class ApplicationServicesUTest {

    private Validator validator;
    private ApplicationServices applicationServices;

    @Mock
    ApplicationRepository applicationRepository;

    @Mock
    ProgramRepository programRepository;

    @Before
    public void initTestCase() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();

        MockitoAnnotations.initMocks(this);

        applicationServices = new ApplicationServicesImpl();
        ((ApplicationServicesImpl) applicationServices).applicationRepository = applicationRepository;
        ((ApplicationServicesImpl) applicationServices).programRepository = programRepository;
        ((ApplicationServicesImpl) applicationServices).validator = validator;
    }

    @Test
    public void addAppCorrect() {
        Application app = app1();
        applicationServices.create(app);
        verify(applicationRepository).add(app);
    }

    @Test
    public void addAppWithNullName() {
        Application app = app1();
        app.setName(null);
        addAppWithInvalidField(app, "name");
    }

    @Test
    public void addAppWithNullUrl() {
        Application app = app1();
        app.setURL(null);
        addAppWithInvalidField(app, "URL");
    }

    @Test
    public void addAppWithNullInvUrl() {
        Application app = app1();
        app.setInvitationURL(null);
        addAppWithInvalidField(app, "invitationURL");
    }

    @Test
    public void deleteAppCorrect() {
        UUID uid = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");
        Application app = appWithId(app1(), uid);
        when(applicationRepository.findByApiKey(uid)).thenReturn(app);
        when(programRepository.findByApplication(app)).thenReturn(null);
        applicationServices.delete(app);

        verify(applicationRepository).delete(eq(app));
    }

    @Test(expected = AppNotFoundException.class)
    public void deleteAppNull() {
        UUID uid = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");
        Application app = appWithId(app1(), uid);
        when(applicationRepository.findByApiKey(uid)).thenReturn(null);

        applicationServices.delete(app);
    }

    @Test
    public void changeAppName() {
        UUID uid = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");
        Application app = appWithId(app1(), uid);
        when(applicationRepository.findByApiKey(uid)).thenReturn(app);

        applicationServices.changeName("Best APP", app.getApiKey());

        verify(applicationRepository).update(eq(app));
    }

    @Test(expected = AppNotFoundException.class)
    public void changeAppNameNotFound() {
        UUID uid = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");
        Application app = appWithId(app1(), uid);
        when(applicationRepository.findByApiKey(uid)).thenReturn(null);

        applicationServices.changeName("Best APP", app.getApiKey());
    }

    @Test(expected = FieldNotValidException.class)
    public void changeAppNameNull() {
        UUID uid = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");
        Application app = appWithId(app1(), uid);
        when(applicationRepository.findByApiKey(uid)).thenReturn(app);

        applicationServices.changeName(null, app.getApiKey());

    }

    @Test
    public void changeAppDescription() {
        UUID uid = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");
        Application app = appWithId(app1(), uid);
        when(applicationRepository.findByApiKey(uid)).thenReturn(app);

        applicationServices.changeDescription("Everyone enroll!", app.getApiKey());

        verify(applicationRepository).update(eq(app));
    }

    @Test
    public void changeAppURL() {
        UUID uid = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");
        Application app = appWithId(app1(), uid);
        when(applicationRepository.findByApiKey(uid)).thenReturn(app);
        when(applicationRepository.findByURL("www.appurl.com")).thenReturn(null);

        applicationServices.changeURL("www.appurl.com", app.getApiKey());

        verify(applicationRepository).update(eq(app));
    }

    @Test(expected = AppServiceException.class)
    public void changeAppURLAlreadyInUse() {
        UUID uid = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");
        Application app = appWithId(app1(), uid);
        when(applicationRepository.findByApiKey(uid)).thenReturn(app);
        when(applicationRepository.findByURL("www.appurl.com")).thenReturn(app2());

        applicationServices.changeURL("www.appurl.com", app.getApiKey());

    }

    @Test
    public void changeAppInvURL() {
        UUID uid = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");
        Application app = appWithId(app1(), uid);
        when(applicationRepository.findByApiKey(uid)).thenReturn(app);
        when(applicationRepository.findByInvURL("www.appurl.com")).thenReturn(null);

        applicationServices.changeInvitationURL("www.appurl.com", app.getApiKey());

        verify(applicationRepository).update(eq(app));
    }

    @Test(expected = AppServiceException.class)
    public void changeAppInvURLAlreadyInUse() {
        UUID uid = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");
        Application app = appWithId(app1(), uid);
        when(applicationRepository.findByApiKey(uid)).thenReturn(app);
        when(applicationRepository.findByInvURL("www.appurl.com")).thenReturn(app2());

        applicationServices.changeInvitationURL("www.appurl.com", app.getApiKey());

    }

    private void addAppWithInvalidField(Application application, String expectedInvalidFieldName) {
        try {
            applicationServices.create(application);
            fail("An error should have been thrown");
        } catch (final FieldNotValidException e) {
            assertThat(e.getFieldName(), is(equalTo(expectedInvalidFieldName)));
        }
    }

}
