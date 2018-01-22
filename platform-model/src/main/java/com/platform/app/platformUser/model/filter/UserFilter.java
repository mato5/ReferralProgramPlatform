package com.platform.app.platformUser.model.filter;

import com.platform.app.common.model.filter.GenericFilter;
import com.platform.app.platformUser.model.User.UserType;

public class UserFilter extends GenericFilter {
	private String name;
	private UserType userType;

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public UserType getUserType() {
		return userType;
	}

	public void setUserType(final UserType userType) {
		this.userType = userType;
	}

	@Override
	public String toString() {
		return "UserFilter [name=" + name + ", userType=" + userType + ", toString()=" + super.toString() + "]";
	}

}