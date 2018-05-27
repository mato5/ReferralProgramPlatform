(function () {
    'use strict';

    angular
        .module('platform')
        .factory('ApplicationService', ApplicationService);

    ApplicationService.$inject = ['$http'];

    function ApplicationService($http) {

        var service = {};
        var baseURL = "http://localhost:8080/platform/api/applications";
        service.Create = Create;
        service.Delete = Delete;
        service.GetAll = GetAll;
        service.GetAppsByUser = GetAppsByUser;
        service.GetApp = GetApp;
        service.GetAppByUrl = GetAppByUrl;
        service.GetAppByName = GetAppByName;
        service.ChangeName = ChangeName;
        service.CHangeDescription = ChangeDescription;
        service.ChangeUrl = ChangeUrl;
        service.ChangeInvUrl = ChangInvUrl;

        return service;

        function Create(app) {
            return $http.post(baseURL, app).then(handleSuccess, handleError('Error creating an app'));
        }

        function Delete(id) {
            return $http.delete(baseURL + "/" + id).then(handleSuccess, handleError('Error removing an app'));
        }

        function GetAll() {
            return $http.get(baseURL).then(handleSuccess, handleError('Error retrieving all apps'));
        }

        function GetAppsByUser(id) {
            return $http.get(baseURL + "/user/" + id)
                .then(handleSuccess, handleError('Error retreving all apps by user'));
        }

        function GetApp(id) {
            return $http.get(baseURL + "/" + id).then(handleSuccess, handleError('Error retrieving an app'));
        }

        function GetAppByUrl(url) {
            return $http.put(baseURL + "/url", url).then(handleSuccess, handleError('Error getting an app by its URL'));
        }

        function GetAppByName(name) {
            return $http.get(baseURL + "/name/" + name)
                .then(handleSuccess, handleError('Error getting an app by its name'));
        }

        function ChangeName(id, name) {
            return $http.put(baseURL + "/" + id + "/name", name)
                .then(handleSuccess, handleError('Error changing application name'));
        }

        function ChangeDescription(id, description) {
            return $http.put(baseURL + "/" + id + "/description", description)
                .then(handleSuccess, handleError('Error changing application description'));
        }

        function ChangeUrl(id, url) {
            return $http.put(baseURL + "/" + id + "/url", url)
                .then(handleSuccess, handleError('Error changing application URL'));
        }

        function ChangInvUrl(id, invUrl) {
            return $http.put(baseURL + "/" + id + "/invitation_url", invUrl)
                .then(handleSuccess, handleError('Error changing application invitation URL'));
        }

        // private functions

        function handleSuccess(res) {
            return {success: true, data: res.data};
        }

        function handleError(error) {
            return function () {
                return {success: false, message: error};
            };
        }
    }

})();