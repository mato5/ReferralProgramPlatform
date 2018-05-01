package com.platform.app.user.resource;

import com.platform.app.common.resource.AbstractFilterExtractorFromUrl;
import com.platform.app.platformUser.model.User.UserType;
import com.platform.app.platformUser.model.filter.UserFilter;

import javax.ws.rs.core.UriInfo;

public class UserFilterExtractorFromUrl extends AbstractFilterExtractorFromUrl {

    public UserFilterExtractorFromUrl(final UriInfo uriInfo) {
        super(uriInfo);
    }

    public UserFilter getFilter() {
        final UserFilter userFilter = new UserFilter();
        userFilter.setPaginationData(extractPaginationData());
        userFilter.setName(getUriInfo().getQueryParameters().getFirst("name"));
        final String userType = getUriInfo().getQueryParameters().getFirst("type");
        if (userType != null) {
            userFilter.setUserType(UserType.valueOf(userType));
        }

        return userFilter;
    }

    @Override
    protected String getDefaultSortField() {
        return "name";
    }

}