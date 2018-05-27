(function () {
    'use strict';

    angular
        .module('platform')
        .factory('UserService', UserService);

    UserService.$inject = ['$http'];

    function UserService($http) {

        var service = {};
        var baseURL = "http://localhost:8080/platform/api/users";
        service.Create = Create;
        service.Update = Update;
        service.UpdatePassword = UpdatePassword;
        service.GetUser = GetUser;
        service.Authenticate = Authenticate;
        service.GetAll = GetAll;
        service.GetByEmail = GetByEmail;

        return service;

        function Create(user) {
            return $http.post(baseURL, user).then(handleSuccess, handleError('Error creating a new user'));
        }

        function Update(id, newUser) {
            return $http.put(baseURL + "/" + id, newUser).then(handleSuccess, handleError('Error updating an existing user'));
        }

        function UpdatePassword(id, password) {
            return $http.put(baseURL + "/" + id + "/password", password).then(handleSuccess, handleError('Error updating a password'));
        }

        function GetUser(id) {
            return $http.get(baseURL + "/" + id).then(handleSuccess, handleError('Error getting user by their ID'));
        }

        function GetByEmail(email) {
            return $http.put(baseURL + "/email", email).then(handleSuccess, handleError('Error getting user by their email'));
        }

        function Authenticate(body) {
            return $http.post(baseURL + "/authenticate", body).then(handleSuccess, handleError('Error authenticating user'));
        }

        function GetAll() {
            return $http.get(baseURL).then(handleSuccess, handleError('Error getting all users'));
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