(function () {
    'use strict';

    angular.module('platform').controller('LogoutController', LogoutController);

    LogoutController.$inject = ['$location', 'AuthenticationService', 'FlashService'];

    function LogoutController($location, AuthenticationService, FlashService) {
        (function logout() {
            AuthenticationService.ClearCredentials();
            FlashService.Success('Successfully logged out', true);
            $location.path('/');
        })();
    }
})();