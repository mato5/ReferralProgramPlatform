package com.platform.app.program.repository;

import com.platform.app.commontests.utils.TestBaseRepository;
import com.platform.app.program.model.Application;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static com.platform.app.commontests.program.ApplicationForTestsRepository.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class ApplicationRepositoryUTest extends TestBaseRepository {

    private ApplicationRepository applicationRepository;

    @Before
    public void initTestCase() {
        initializeTestDB();

        applicationRepository = new ApplicationRepository();
        applicationRepository.em = em;
    }

    @After
    public void setDownTestCase() {
        closeEntityManager();
    }

    @Test
    public void addAppAndFindIt() {
        final UUID addedId = dbCommandExecutor.executeCommand(() -> {
            return applicationRepository.add(app1()).getApiKey();
        });

        assertThat(addedId, is(notNullValue()));

        final Application application = applicationRepository.findByApiKey(addedId);
        assertThat(application, is(notNullValue()));
        assertThat(application.getName(), is(equalTo("app1")));
    }

    @Test
    public void findAllApps() {
        dbCommandExecutor.executeCommand(() -> {
            allApps().forEach(applicationRepository::add);
            return null;
        });

        final List<Application> apps = applicationRepository.findAll("name");
        assertThat(apps.size(), is(equalTo(2)));
        assertThat(apps.get(0).getName(), is(equalTo("app1")));
        assertThat(apps.get(1).getName(), is(equalTo("app2")));
    }

    @Test
    public void findByURL() {
        dbCommandExecutor.executeCommand(() -> {
            allApps().forEach(applicationRepository::add);
            return null;
        });

        final Application application = applicationRepository.findByURL(app2().getURL());
        assertThat(application, is(notNullValue()));
        assertThat(application.getName(), is(equalTo("app2")));

    }

    @Test
    public void findByWrongURL() {
        dbCommandExecutor.executeCommand(() -> {
            allApps().forEach(applicationRepository::add);
            return null;
        });

        final Application application = applicationRepository.findByURL("wrong URL");
        assertThat(application, is(nullValue()));
    }

    @Test
    public void findByInvURL() {
        dbCommandExecutor.executeCommand(() -> {
            allApps().forEach(applicationRepository::add);
            return null;
        });

        final Application application = applicationRepository.findByInvURL(app2().getInvitationURL());
        assertThat(application, is(notNullValue()));
        assertThat(application.getName(), is(equalTo("app2")));

    }

    @Test
    public void findByWrongInvURL() {
        dbCommandExecutor.executeCommand(() -> {
            allApps().forEach(applicationRepository::add);
            return null;
        });

        final Application application = applicationRepository.findByURL("wrong inv URL");
        assertThat(application, is(nullValue()));
    }

    @Test
    public void findByName() {
        dbCommandExecutor.executeCommand(() -> {
            allApps().forEach(applicationRepository::add);
            return null;
        });

        final List<Application> applications = applicationRepository.findByName(app1().getName());
        assertThat(applications, is(notNullValue()));
        assertThat(applications.size(), is(equalTo(1)));
    }

    @Test
    public void findByWrongName() {
        dbCommandExecutor.executeCommand(() -> {
            allApps().forEach(applicationRepository::add);
            return null;
        });

        final List<Application> applications = applicationRepository.findByName("wrong name");
        assertThat(applications.size(), is(0));
    }

    @Test
    public void findAppByIdNotFound() {
        final Application app = applicationRepository.findByApiKey(UUID.fromString("44e128a5-ac7a-4c9a-be4c-224b6bf81b20"));
        assertThat(app, is(nullValue()));
    }

    @Test
    public void findAppByIdWithNullId() {
        final Application app = applicationRepository.findById(null);
        assertThat(app, is(nullValue()));
    }

    @Test
    public void updateApp() {
        final UUID addedId = dbCommandExecutor.executeCommand(() -> {
            return applicationRepository.add(app2()).getApiKey();
        });

        final Application applicationAfterAdd = applicationRepository.findByApiKey(addedId);
        assertThat(applicationAfterAdd.getDescription(), is(equalTo(app2().getDescription())));

        applicationAfterAdd.setDescription(app1().getDescription());
        dbCommandExecutor.executeCommand(() -> {
            applicationRepository.update(applicationAfterAdd);
            return null;
        });

        final Application appAfterUpdate = applicationRepository.findByApiKey(addedId);
        assertThat(appAfterUpdate.getDescription(), is(equalTo(app1().getDescription())));
    }

    @Test
    public void deleteExistingApp() {
        dbCommandExecutor.executeCommand(() -> {
            allApps().forEach(applicationRepository::add);
            return null;
        });
        assertThat(applicationRepository.findAll("name").size(), is(equalTo(2)));
        dbCommandExecutor.executeCommand(() -> {
            applicationRepository.delete(applicationRepository.findByURL(app1().getURL()));
            return null;
        });
        assertThat(applicationRepository.findAll("name").size(), is(equalTo(1)));
    }


}
