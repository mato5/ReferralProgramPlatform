(function () {
    'use strict';

    angular
        .module('platform', ['ngRoute', 'ngCookies', 'ui.bootstrap'])
        .config(config)
        .run(run);

    config.$inject = ['$routeProvider', '$locationProvider'];

    function config($routeProvider, $locationProvider) {
        $routeProvider
            .when('/', {
                controller: 'HomeController',
                templateUrl: 'partials/home.view.html',
                controllerAs: 'vm'
            })

            .when('/login', {
                controller: 'LoginController',
                templateUrl: 'partials/login.view.html',
                controllerAs: 'vm'
            })

            .when('/register', {
                controller: 'RegisterController',
                templateUrl: 'partials/register.view.html',
                controllerAs: 'vm'
            })

            .when('/create_program', {
                controller: 'CreateProgramController',
                templateUrl: 'partials/create_program.html',
                controllerAs: 'vm'
            })

            .when('/programs', {
                controller: 'AllProgramsController',
                templateUrl: 'partials/programs.html',
                controllerAs: 'vm'
            })

            .when('/programs/:id', {
                controller: 'ProgramDetailController',
                templateUrl: 'partials/program.html',
                controllerAs: 'vm'
            })

            .when('/programs/:id/register_app', {
                controller: 'RegisterAppController',
                templateUrl: 'partials/create_application.html',
                controllerAs: 'vm'
            })

            .when('/programs/:id/add_admin', {
                controller: 'AddAdminController',
                templateUrl: 'partials/add_user.html',
                controllerAs: 'vm'
            })

            .when('/programs/:id/invite_friend', {
                controller: 'InviteFriendController',
                templateUrl: 'partials/invite_friend.html',
                controllerAs: 'vm'
            })

            .when('/programs/:id/invite_list', {
                controller: 'InviteListController',
                templateUrl: 'partials/invite_list.html',
                controllerAs: 'vm'
            })

            .when('/programs/:id/invitations', {
                controller: 'ProgramInvController',
                templateUrl: 'partials/stats.html',
                controllerAs: 'vm'
            })

            .when('/programs/:id/invitations/chart', {
                controller: 'TreeChartController',
                templateUrl: 'partials/tree_chart.html',
                controllerAs: 'vm'
            })

            .when('/my_programs', {
                controller: 'MyProgramsController',
                templateUrl: 'partials/programs.html',
                controllerAs: 'vm'
            })

            .when('/received', {
                controller: 'ReceivedInvController',
                templateUrl: 'partials/received.html',
                controllerAs: 'vm'
            })

            .when('/sent', {
                controller: 'SentInvController',
                templateUrl: 'partials/sent.html',
                controllerAs: 'vm'
            })

            .when('/logout', {
                template: " ",
                controller: 'LogoutController'
            })

            .otherwise({redirectTo: '/'});
    }

    run.$inject = ['$rootScope', '$location', '$cookies', '$http'];

    function run($rootScope, $location, $cookies, $http) {
        // keep user logged in after page refresh
        $rootScope.globals = $cookies.getObject('globals') || {};
        if ($rootScope.globals.currentUser) {
            $http.defaults.headers.common['Authorization'] = 'Basic ' + $rootScope.globals.currentUser.authdata;
        }

        $rootScope.$on('$locationChangeStart', function (event, next, current) {
            // redirect to login page if not logged in and trying to access a restricted page
            //var restrictedPage = $.inArray($location.path(), ['/login', '/register', '/', '/!#']) === -1;
            var restrictedPage = false;
            var loggedIn = $rootScope.globals.currentUser;
            if (restrictedPage && !loggedIn) {
                $location.path('/login');
            }
        });
    }

})();