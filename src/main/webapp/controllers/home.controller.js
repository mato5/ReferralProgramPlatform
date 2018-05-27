(function () {
    'use strict';

    angular
        .module('platform')
        .controller('HomeController', HomeController);

    HomeController.$inject = ['UserService', '$rootScope', 'InvitationService'];

    function HomeController(UserService, $rootScope, InvitationService) {
        var vm = this;

        vm.user = null;
        vm.allUsers = [];
        vm.logged = false;
        vm.location = {};

        initController();

        function initController() {
            vm.logged = false;
            loadCurrentUser();
        }

        function loadCurrentUser() {
            if (!$rootScope.globals.hasOwnProperty('currentUser')) {
                vm.logged = false;
            } else {
                vm.user = $rootScope.globals.currentUser;
                vm.logged = true;
            }
        }

    }

})();