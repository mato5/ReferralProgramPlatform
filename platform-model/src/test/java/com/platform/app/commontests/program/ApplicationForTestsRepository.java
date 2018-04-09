package com.platform.app.commontests.program;

import com.platform.app.program.model.Application;
import org.junit.Ignore;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Ignore
public final class ApplicationForTestsRepository {

    private ApplicationForTestsRepository() {

    }

    public static Application app1() {
        Application app = new Application();
        app.setName("app1");
        app.setDescription("just a random description");
        app.setURL("www.domain.com/app1");
        app.setInvitationURL("www.domain.com/app1/inv");
        return app;
    }

    public static Application app2() {
        Application app = new Application();
        app.setName("app2");
        app.setURL("www.domain.com/app2");
        app.setInvitationURL("www.domain.com/app2/inv");
        return app;
    }

    public static Application appWithId(final Application application, final UUID id) {
        application.setApiKey(id);
        return application;
    }

    public static List<Application> allApps() {
        return Arrays.asList(app1(), app2());
    }
}
