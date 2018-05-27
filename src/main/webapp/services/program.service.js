(function () {
    'use strict';

    angular
        .module('platform')
        .factory('ProgramService', ProgramService);

    ProgramService.$inject = ['$http'];

    function ProgramService($http) {

        var service = {};
        var baseURL = "http://localhost:8080/platform/api/programs";
        service.GetById = GetById;
        service.Delete = Delete;
        service.Create = Create;
        service.GetAll = GetAll;
        service.GetByAdmin = GetByAdmin;
        service.GetByApp = GetByApp;
        service.GetByName = GetByName;
        service.GetByUser = GetByUser;
        service.ChangeName = ChangeName;
        service.AddAdmin = AddAdmin;
        service.RemoveAdmin = RemoveAdmin;
        service.AddCustomer = AddCustomer;
        service.RemoveCustomer = RemoveCustomer;
        service.RegisterOnWaitingList = RegisterOnWaitingList;
        service.UnregisterOnWaitingList = UnregisterOnWaitingList;
        service.RegisterApp = RegisterApp;
        service.UnregisterApp = UnregisterApp;
        service.InviteWaitingList = InviteWaitingList;
        service.GetUserRole = GetUserRole;
        service.GetByWaitingUser = GetByWaitingUser;

        return service;

        function GetById(id) {
            return $http.get(baseURL + "/" + id).then(handleSuccess, handleError("Error retrieving a program"));
        }

        function Delete(id) {
            return $http.delete(baseURL + "/" + id).then(handleSuccess, handleError('Error removing a program'));
        }

        function Create(program) {
            return $http.post(baseURL, program).then(handleSuccess, handleError('Error creating a new program'));
        }

        function GetAll() {
            return $http.get(baseURL).then(handleSuccess, handleError('Error retrieving all programs'));
        }

        function GetByAdmin(id) {
            return $http.get(baseURL + "/admin/" + id)
                .then(handleSuccess, handleError('Error retrieving programs by their admin'));
        }

        function GetByApp(id) {
            return $http.get(baseURL + "/application/" + id)
                .then(handleSuccess, handleError('Error retrieving programs by application'));
        }

        function GetByName(name) {
            return $http.get(baseURL + "/name/" + encodeURIComponent(name))
                .then(handleSuccess, handleError('Error getting program by its name'));
        }

        function GetByUser(id) {
            return $http.get(baseURL + "/user/" + id)
                .then(handleSuccess, handleError('Error getting program by its user'));
        }

        function GetByWaitingUser(id) {
            return $http.get(baseURL + "/waiting/" + id)
                .then(handleSuccess, handleError('Error getting program by its waiting user'));
        }

        function ChangeName(id, name) {
            return $http.put(baseURL + "/" + id + "/name", name)
                .then(handleSuccess, handleError('Error changing program name'));
        }

        function AddAdmin(id, admin) {
            return $http.put(baseURL + "/" + id + "/add_admin", admin)
                .then(handleSuccess, handleError('Error adding a program admin'));
        }

        function RemoveAdmin(id, admin) {
            return $http.put(baseURL + "/" + id + "/remove_admin", admin)
                .then(handleSuccess, handleError('Error removing a program admin'));
        }

        function AddCustomer(id, customer) {
            return $http.put(baseURL + "/" + id + "/add_customer", customer)
                .then(handleSuccess, handleError('Error adding a program customer'));
        }

        function RemoveCustomer(id, customer) {
            return $http.put(baseURL + "/" + id + "/remove_customer", customer)
                .then(handleSuccess, handleError('Error removing a program customer'));
        }

        function RegisterOnWaitingList(id) {
            return $http.put(baseURL + "/" + id + "/register")
                .then(handleSuccess, handleError('Error registering on a waitling list'));
        }

        function UnregisterOnWaitingList(id) {
            return $http.put(baseURL + "/" + id + "/unregister")
                .then(handleSuccess, handleError('Error unregistering on a waitling list'));
        }

        function RegisterApp(id, appId) {
            return $http.put(baseURL + "/" + id + "/register_app/" + appId)
                .then(handleSuccess, handleError('Error registering a program app'));
        }

        function UnregisterApp(id, appId) {
            return $http.put(baseURL + "/" + id + "/unregister_app/" + appId)
                .then(handleSuccess, handleError('Error unregistering a program app'));
        }

        function InviteWaitingList(id, amount, body) {
            return $http.put(baseURL + "/" + id + "/invite_waitinglist/" + amount, body)
                .then(handleSuccess, handleError('Error inviting from a waiting list'));
        }

        function GetUserRole(name, email) {
            return $http.put(baseURL + "/" + encodeURIComponent(name) + "/role", email)
                .then(handleSuccess, handleError('Error fetching user role'));
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