package com.platform.app.commontests.platformUser;

import com.platform.app.platformUser.model.User;
import org.mockito.ArgumentMatcher;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.argThat;

public class UserArgumentMatcher implements ArgumentMatcher<User> {
    private User expectedUser;

    public static User userEq(final User expectedUser) {
        return argThat(new UserArgumentMatcher(expectedUser));
    }

    public UserArgumentMatcher(final User expectedUser) {
        this.expectedUser = expectedUser;
    }

	/*@Override
	public boolean matches(final Object argument) {
		final User actualUser = (User) argument;

		assertThat(actualUser.getId(), is(equalTo(expectedUser.getId())));
		assertThat(actualUser.getName(), is(equalTo(expectedUser.getName())));
		assertThat(actualUser.getEmail(), is(equalTo(expectedUser.getEmail())));
		assertThat(actualUser.getPassword(), is(equalTo(expectedUser.getPassword())));

		return true;
	}*/

    @Override
    public boolean matches(User actualUser) {
        assertThat(actualUser.getId(), is(equalTo(expectedUser.getId())));
        assertThat(actualUser.getName(), is(equalTo(expectedUser.getName())));
        assertThat(actualUser.getEmail(), is(equalTo(expectedUser.getEmail())));
        assertThat(actualUser.getPassword(), is(equalTo(expectedUser.getPassword())));

        return true;
    }
}