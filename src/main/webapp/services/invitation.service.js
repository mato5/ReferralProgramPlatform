(function () {
    'use strict';

    angular
        .module('platform')
        .factory('InvitationService', InvitationService);

    InvitationService.$inject = ['$http'];

    function InvitationService($http) {

        var service = {};
        var baseURL = "http://localhost:8080/platform/api/invitations";
        service.Send = Send;
        service.SendInBatch = SendInBatch;
        service.GetById = GetById;
        service.GetMyInvitations = GetMyInvitations;
        service.GetSentInvitations = GetSentInvitations;
        service.GetByProgram = GetByProgram;
        service.Delete = Delete;
        service.Accept = Accept;
        service.Decline = Decline;
        service.GetTreeChart = GetTreeChart;
        service.GetInvitationsLeft = GetInvitationsLeft;
        service.GetLocation = GetLocation;

        return service;

        function GetAll() {
            return $http.get(baseURL + "/all").then(handleSuccess, handleError('Error getting all invitations'));
        }

        function Send(invitation) {
            return $http.post(baseURL, invitation).then(handleSuccess, handleError('Error sending an invitation'));
        }

        function SendInBatch(invitations) {
            return $http.post(baseURL + "/batch", invitations).then(handleSuccess, handleError('Error sending invitations in a batch'));
        }

        function GetById(id) {
            return $http.get(baseURL + "/" + id).then(handleSuccess, handleError('Error getting an invitation by ID'));
        }

        function GetMyInvitations() {
            return $http.get(baseURL).then(handleSuccess, handleError('Error getting my invitations'));
        }

        function GetSentInvitations() {
            return $http.get(baseURL + "/sent").then(handleSuccess, handleError('Error getting sent invitations'));
        }

        function GetInvitationsLeft(id) {
            return $http.get(baseURL + "/program/" + id + '/left').then(handleSuccess, handleError('Error getting invitations left'));
        }

        function GetByProgram(id) {
            return $http.get(baseURL + "/program/" + id).then(handleSuccess, handleError('Error getting invitations by program'));
        }

        function Delete(id) {
            return $http.delete(baseURL + "/" + id).then(handleSuccess, handleError('Error deleting an invitation'));
        }

        function Accept(id, geoLocation) {
            return $http.put(baseURL + "/" + id + "/accept", geoLocation).then(handleSuccess, handleError('Error accepting an invitation'));
        }

        function Decline(id) {
            return $http.put(baseURL + "/" + id + "/decline").then(handleSuccess, handleError("Error declining an invitation"));
        }

        function GetLocation() {
            var req = {
                method: 'GET',
                url: 'http://ip-api.com/json',
                headers: {
                    'Authorization': undefined
                }
            };
            return $http(req).then(handleSuccess, handleError('Error getting user location'));
        }

        function GetTreeChart(programId) {
            return $http.get(baseURL + "/chart/" + programId).then(handleSuccess, handleError('Error getting an invitation Tree Chart'));
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