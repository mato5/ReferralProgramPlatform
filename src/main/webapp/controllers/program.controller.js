(function () {
    'use strict';

    angular
        .module('platform')
        .controller('CreateProgramController', CreateProgramController)
        .controller('ProgramDetailController', ProgramDetailController)
        .controller('AllProgramsController', AllProgramsController)
        .controller('MyProgramsController', MyProgramsController)
        .controller('RegisterAppController', RegisterAppController)
        .controller('AddAdminController', AddAdminController)
        .directive('appUrlNotUsed', function ($http, $q) {
            return {
                require: 'ngModel',
                link: function (scope, element, attrs, ngModel) {
                    ngModel.$asyncValidators.appUrlNotUsed = function (modelValue, viewValue) {
                        return $http.put('http://localhost:8080/platform/api/applications/url', {URL: viewValue}).then(function (response) {
                            return response.status != 404 ? $q.reject('App URL is already used.') : true;
                        }, function () {
                            return true;
                        });
                    };
                }
            };
        })
        .directive('programNameNotUsed', function ($http, $q) {
            return {
                require: 'ngModel',
                link: function (scope, element, attrs, ngModel) {
                    ngModel.$asyncValidators.programNameNotUsed = function (modelValue, viewValue) {
                        return $http.get('http://localhost:8080/platform/api/programs/name/' + viewValue).then(function (response) {
                            return response.status != 404 ? $q.reject('Program name is already used.') : true;
                        }, function () {
                            return true;
                        });
                    };
                }
            };
        });

    CreateProgramController.$inject = ['ProgramService', '$location', '$rootScope', 'FlashService'];

    function CreateProgramController(ProgramService, $location, $rootScope, FlashService) {
        var vm = this;

        vm.createProgram = createProgram;

        function createProgram() {

            vm.dataLoading = true;
            vm.program.admins = [];
            vm.program.activeCustomers = [];
            vm.program.activeApplications = [];

            ProgramService.Create(vm.program)
                .then(function (response) {
                    if (response.success) {
                        FlashService.Success('Program creation successful', true);
                        $location.path('/');
                    } else {
                        FlashService.Error(response.message);
                        vm.dataLoading = false;
                    }
                })
        }
    }

    ProgramDetailController.$inject = ['ProgramService', '$location', '$rootScope', 'FlashService', '$routeParams', 'UserService', '$q', 'InvitationService'];

    function ProgramDetailController(ProgramService, $location, $rootScope, FlashService, $routeParams, UserService, $q, InvitationService) {
        var vm = this;
        vm.logged = false;
        vm.waiting = [];
        vm.admin = false;
        vm.role = {};
        vm.user = {};
        vm.checkBox = false;

        vm.deleteApp = deleteApp;
        vm.removeCustomer = removeCustomer;
        vm.registerOnWaitingList = registerOnWaitingList;
        vm.unregisterOnWaitingList = unregisterOnWaitingList;
        vm.leaveProgram = leaveProgram;
        vm.check = check;
        vm.uncheck = uncheck;

        initController();

        function initController() {
            loadCurrentUser();
            getRawData();
        }

        function check() {
            vm.checkBox = true;
        }

        function uncheck() {
            vm.checkBox = false;
        }

        function deleteApp(id) {
            ProgramService.UnregisterApp($routeParams.id, id).then(function (response) {
                if (response.success) {
                    FlashService.Success('Aplication removed successfully', true);
                    $location.path('/my_programs');
                }
            }, function (reason) {
                FlashService.Error(reason.message);
            })
        }

        function leaveProgram() {
            InvitationService.GetMyInvitations().then(function (response) {
                if (response.success) {
                    var result = response.data.entries;
                    var invitationId = {};
                    for (var i = 0; i < result.length; i++) {
                        if (!result[i].declined && result[i].hasOwnProperty('activated')) {
                            if (result[i].programId == $routeParams.id) {
                                invitationId = result[i].id;
                                break;
                            }
                        }
                    }
                    InvitationService.Decline(invitationId).then(function (value) {
                        if (value.success) {
                            FlashService.Success('Program left successfully', true);
                            $location.path('/my_programs');
                        }
                    }, function (reason) {
                        FlashService.Error(reason.message);
                    })
                }
            })
        }

        function removeCustomer(email) {
            var customer = {email: email, type: "CUSTOMER"};
            ProgramService.RemoveCustomer($routeParams.id, customer).then(function (response) {
                if (response.success) {
                    FlashService.Success('Customer removed successfully', true);
                    $location.path('/my_programs');
                }
            }, function (reason) {
                FlashService.Error(reason.message);
            })
        }

        function registerOnWaitingList() {
            ProgramService.RegisterOnWaitingList($routeParams.id).then(function (response) {
                if (response.success) {
                    FlashService.Success('Successfully registered on the waiting list', true);
                    $location.path('/my_programs');
                }
            }, function (reason) {
                FlashService.Error(reason.message);
            })
        }

        function unregisterOnWaitingList() {
            ProgramService.UnregisterOnWaitingList($routeParams.id).then(function (response) {
                if (response.success) {
                    FlashService.Success('Successfully unregistered from the waiting list', true);
                    $location.path('/my_programs');
                }
            }, function (reason) {
                FlashService.Error(reason.message);
            })
        }

        function loadCurrentUser() {
            if (!$rootScope.globals.hasOwnProperty('currentUser')) {
                vm.logged = false;
            } else {
                /*UserService.GetUser($rootScope.globals.currentUser.id).then(function (value) {
                    vm.user = value.data;
                    vm.logged = true;
                });*/
                vm.user = $rootScope.globals.currentUser;
                vm.logged = true;
            }
        }

        function getRawData() {

            ProgramService.GetById($routeParams.id)
                .then(function (response) {
                    if (response.success) {
                        vm.program = response.data;
                        vm.applications = response.data.activeApplications;
                        vm.admins = response.data.admins;
                        vm.customers = response.data.activeCustomers;
                        if (vm.logged) {
                            getUserRole();
                        }
                        getProgram();
                    } else {
                        FlashService.Error(response.message);
                    }
                })
        }

        function getUserRole() {

            ProgramService.GetUserRole(vm.program.name, vm.user.username).then(function (value) {
                vm.role = value.data.role;
            })
        }

        function getProgram() {
            var promises = [];
            var order = vm.program.waitingList.order;
            for (var i = 0; i < order.length; i++)
                promises.push(UserService.GetUser(order[i]));

            $q.all(promises).then(function (results) {
                var list = vm.program.waitingList.list;
                if (list !== undefined && vm.program.waitingList.size !== 0) {
                    var order = vm.program.waitingList.order;
                    for (var key in order) {
                        var pom = order[key];
                        var user = {};
                        user["email"] = results[key].data.email;
                        user["name"] = results[key].data.name;
                        var date = list[pom].date;
                        var time = list[pom].time;
                        user["since"] = new Date(
                            date.year,
                            date.month - 1,
                            date.day,
                            time.hour,
                            time.minute,
                            time.second,
                            time.nano / 1000000);
                        vm.waiting.push(user);
                    }
                }

            }).catch(function (e) {
                console.log('This should never happen');
            });
        }
    }

    AllProgramsController.$inject = ['ProgramService', '$location', '$rootScope'];

    function AllProgramsController(ProgramService, $location, $rootScope) {
        var vm = this;
        vm.logged = false;
        vm.programs = [];

        initController();

        vm.redirectToProgramPage = redirectToProgramPage;

        function redirectToProgramPage(id) {
            $location.path('/programs/' + id);
        }

        function initController() {
            getAllPrograms();
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

        function getAllPrograms() {
            ProgramService.GetAll().then(function (response) {
                for (var item in response.data.entries) {
                    var result = response.data.entries[item];
                    vm.programs.push({
                        id: result.id,
                        name: result.name,
                        adminSize: result.admins.length,
                        customerSize: result.activeCustomers.length,
                        waitingSize: result.waitingList.size
                    })
                }
            })
        }


    }

    MyProgramsController.$inject = ['ProgramService', '$location', '$rootScope', 'FlashService'];

    function MyProgramsController(ProgramService, $location, $rootScope, FlashService) {
        var vm = this;
        vm.logged = false;
        vm.myPrograms = true;
        vm.programs = [];

        initController();

        vm.redirectToProgramPage = redirectToProgramPage;

        function redirectToProgramPage(id) {
            $location.path('/programs/' + id);
        }

        function initController() {
            loadCurrentUser();
            getAllAdminPrograms();
            getAllCustomerPrograms();
            getAllWaitingPrograms();
        }


        function loadCurrentUser() {
            if (!$rootScope.globals.hasOwnProperty('currentUser')) {
                vm.logged = false;
            } else {
                vm.user = $rootScope.globals.currentUser;
                vm.logged = true;
            }
        }

        function getAllAdminPrograms() {
            ProgramService.GetByAdmin(vm.user.id).then(function (response) {
                for (var item in response.data.entries) {
                    var result = response.data.entries[item];
                    vm.programs.push({
                        id: result.id,
                        name: result.name,
                        adminSize: result.admins.length,
                        customerSize: result.activeCustomers.length,
                        waitingSize: result.waitingList.size,
                        participatingAs: 'Admin'
                    })
                }
            })
        }

        function getAllCustomerPrograms() {
            ProgramService.GetByUser(vm.user.id).then(function (response) {
                for (var item in response.data.entries) {
                    var result = response.data.entries[item];
                    vm.programs.push({
                        id: result.id,
                        name: result.name,
                        adminSize: result.admins.length,
                        customerSize: result.activeCustomers.length,
                        waitingSize: result.waitingList.size,
                        participatingAs: 'Customer'
                    })
                }
            })
        }

        function getAllWaitingPrograms() {
            ProgramService.GetByWaitingUser(vm.user.id).then(function (response) {
                for (var item in response.data.entries) {
                    var result = response.data.entries[item];
                    vm.programs.push({
                        id: result.id,
                        name: result.name,
                        adminSize: result.admins.length,
                        customerSize: result.activeCustomers.length,
                        waitingSize: result.waitingList.size,
                        participatingAs: 'Waiting'
                    })
                }
            })
        }

    }

    RegisterAppController.$inject = ['ProgramService', '$location', '$rootScope', 'FlashService', '$routeParams', 'ApplicationService'];

    function RegisterAppController(ProgramService, $location, $rootScope, FlashService, $routeParams, ApplicationService) {
        var vm = this;
        vm.application = {};
        vm.registerApp = registerApp;

        function registerApp() {

            vm.dataLoading = true;
            vm.application.invitationURL = vm.application.URL;
            ApplicationService.Create(vm.application)
                .then(function (response) {
                    if (response.success) {
                        ProgramService.RegisterApp($routeParams.id, response.data.id).then(function (value) {
                            if (value.success) {
                                FlashService.Success('Application registered successfully', true);
                                $location.path('/my_programs');
                            }
                        })
                    } else {
                        FlashService.Error(response.message);
                        vm.dataLoading = false;
                    }
                })
        }

    }

    AddAdminController.$inject = ['ProgramService', '$location', '$rootScope', 'FlashService', '$routeParams'];

    function AddAdminController(ProgramService, $location, $rootScope, FlashService, $routeParams) {
        var vm = this;
        vm.user = {};
        vm.message = "Add a new admin";
        vm.info = "Please note that once a user is promoted to an admin, they cannot be removed from the program by any other user but themselves";
        vm.add = add;

        function add() {

            vm.dataLoading = true;
            vm.user.type = "CUSTOMER";
            ProgramService.AddAdmin($routeParams.id, vm.user).then(function (response) {
                if (response.success) {
                    FlashService.Success('Admin added successfully', true);
                    $location.path('/my_programs');
                } else {
                    FlashService.Error(response.message + ', the user you are trying to add is already an admin of this program', true);
                    $location.path('/programs/' + $routeParams.id);
                }
            }, function (reason) {
                FlashService.Error(reason.message);
            })

        }

    }

})();