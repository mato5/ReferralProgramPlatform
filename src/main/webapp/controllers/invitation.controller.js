(function () {
    'use strict';

    angular
        .module('platform')
        .controller('InviteFriendController', InviteFriendController)
        .controller('ReceivedInvController', ReceivedInvController)
        .controller('InviteListController', InviteListController)
        .controller('SentInvController', SentInvController)
        .controller('ProgramInvController', ProgramInvController)
        .controller('TreeChartController', TreeChartController);

    InviteFriendController.$inject = ['InvitationService', '$location', '$rootScope', 'FlashService', '$routeParams', 'UserService'];

    function InviteFriendController(InvitationService, $location, $rootScope, FlashService, $routeParams, UserService) {
        var vm = this;
        vm.inv = {};
        vm.invLeft = 0;

        initController();

        function initController() {
            vm.inv.programId = $routeParams.id;
            InvitationService.GetInvitationsLeft($routeParams.id).then(function (value) {
                if (value.success) {
                    vm.invLeft = value.data;
                } else {
                    vm.invLeft = 0;
                }
            })
        }

        vm.invite = invite;

        function invite() {

            vm.dataLoading = true;
            vm.inv.invitationsLeft = 0;
            UserService.GetByEmail(vm.email).then(function (response) {
                if (response.success) {
                    vm.inv.toUserId = response.data.id;
                    InvitationService.Send(vm.inv).then(function (value) {
                        if (value.success) {
                            FlashService.Success('User invited sucessfully', true);
                            $location.path('/programs/' + $routeParams.id);
                        } else {
                            FlashService.Error(value.message + ', the user you are trying to invite is probably already invited', true);
                            vm.dataLoading = false;
                        }
                    })
                } else {
                    FlashService.Error(response.message, true);
                    $location.path('/programs/' + $routeParams.id);
                }
            });
        }
    }

    ReceivedInvController.$inject = ['InvitationService', 'ProgramService', '$location', '$rootScope', 'FlashService', '$routeParams', 'UserService', '$q'];

    function ReceivedInvController(InvitationService, ProgramService, $location, $rootScope, FlashService, $routeParams, UserService, $q) {
        var vm = this;
        vm.received = [];
        vm.logged = false;

        vm.accept = accept;
        vm.decline = decline;

        initController();

        function accept(id, programId) {
            InvitationService.GetLocation().then(function (response) {
                if (response.success) {
                    var location = {};
                    location.ipAddress = response.data.query;
                    location.city = response.data.city + ', ' + response.data.country;
                    location.latitude = response.data.lat;
                    location.longitude = response.data.lon;
                    InvitationService.Accept(id, location).then(function (value) {
                        if (value.success) {
                            FlashService.Success('Invitation accepted', true);
                            $location.path('/programs/' + programId);
                        } else {
                            FlashService.Error(value.message, true);
                        }
                    })
                } else {
                    FlashService.Error(response.message, true);
                }
            })
        }

        function decline(id) {
            InvitationService.Decline(id).then(function (value) {
                if (value.success) {
                    FlashService.Success('Invitation declined', true);
                    getReceivedInvs();
                } else {
                    FlashService.Error(response.message, true);
                }
            })
        }

        function initController() {
            loadCurrentUser();
            getReceivedInvs();
        }

        function loadCurrentUser() {
            if (!$rootScope.globals.hasOwnProperty('currentUser')) {
                vm.logged = false;
            } else {
                vm.user = $rootScope.globals.currentUser;
                vm.logged = true;
            }
        }

        function getReceivedInvs() {
            vm.received = [];
            InvitationService.GetMyInvitations().then(function (response) {
                if (response.success) {
                    var result = response.data.entries;
                    var userPromises = [];
                    var programPromises = [];
                    var timeSent = [];
                    var invs = [];
                    for (var i = 0; i < result.length; i++) {
                        if (!result[i].declined && !result[i].hasOwnProperty('activated')) {
                            userPromises.push(UserService.GetUser(result[i].byUserId));
                            programPromises.push(ProgramService.GetById(result[i].programId));
                            timeSent.push(new Date(result[i].sent));
                            invs.push(result[i]);
                        }
                    }
                    for (var j = 0; j < userPromises.length; j++) {
                        $q.all([userPromises[j], programPromises[j]]).then(function (value) {
                            vm.received.push({
                                byUser: value[0].data,
                                program: value[1].data,
                                sent: timeSent[0],
                                inv: invs[0]
                            });
                            timeSent.shift();
                            invs.shift();
                        }).catch(function (e) {
                            console.log('This should never happen');
                        });
                    }
                }
            });
        }
    }

    InviteListController.$inject = ['InvitationService', '$location', '$rootScope', 'FlashService', '$routeParams', 'UserService', 'ProgramService', '$q'];

    function InviteListController(InvitationService, $location, $rootScope, FlashService, $routeParams, UserService, ProgramService, $q) {
        var vm = this;
        vm.inv = {};
        vm.program = {};
        vm.waiting = [];
        vm.allowedInvs = 0;
        vm.emails = [];

        vm.invite = invite;
        vm.checkSelected = checkSelected;
        vm.addUser = addUser;

        initController();

        function initController() {
            ProgramService.GetById($routeParams.id).then(function (value) {
                if (value.success) {
                    vm.program = value.data;
                    getProgram();
                }
            })
        }

        function checkSelected(id) {
            var idx = vm.emails.indexOf(id);
            return idx > -1;
        }

        function addUser(id) {
            var idx = vm.emails.indexOf(id);
            if (vm.emails.indexOf(id) == -1) {
                vm.emails.push(id);
            } else {
                vm.emails.splice(idx, 1);
            }
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

        function invite() {

            vm.dataLoading = true;
            var toBeSent = {programId: $routeParams.id, invitationsLeft: vm.allowedInvs, emails: vm.emails};

            InvitationService.SendInBatch(toBeSent).then(function (value) {
                if (value.success) {
                    FlashService.Success('Invitations sent successfully', true);
                    $location.path('/programs/' + $routeParams.id);
                } else {
                    FlashService.Error(response.message);
                    vm.dataLoading = false;
                }
            })

        }
    }

    SentInvController.$inject = ['InvitationService', 'ProgramService', '$location', '$rootScope', 'FlashService', '$routeParams', 'UserService', '$q'];

    function SentInvController(InvitationService, ProgramService, $location, $rootScope, FlashService, $routeParams, UserService, $q) {
        var vm = this;
        vm.sent = [];
        vm.logged = false;
        vm.user = {};

        initController();

        function initController() {
            loadCurrentUser();
            getSentInvs();
        }

        function loadCurrentUser() {
            if (!$rootScope.globals.hasOwnProperty('currentUser')) {
                vm.logged = false;
            } else {
                vm.user = $rootScope.globals.currentUser;
                vm.logged = true;
            }
        }

        function getInvStatus(inv) {
            if (inv.hasOwnProperty('activated')) {
                return 'ACCEPTED';
            } else if (inv.declined) {
                return 'DECLINED';
            } else return 'PENDING';
        }

        function getSentInvs() {
            vm.sent = [];
            InvitationService.GetSentInvitations().then(function (response) {
                if (response.success) {
                    var result = response.data.entries;
                    var userPromises = [];
                    var programPromises = [];
                    var timeSent = [];
                    var invs = [];
                    for (var i = 0; i < result.length; i++) {
                        userPromises.push(UserService.GetUser(result[i].toUserId));
                        programPromises.push(ProgramService.GetById(result[i].programId));
                        timeSent.push(new Date(result[i].sent));
                        invs.push(result[i]);
                    }
                    for (var j = 0; j < userPromises.length; j++) {
                        $q.all([userPromises[j], programPromises[j]]).then(function (value) {
                            vm.sent.push({
                                toUser: value[0].data,
                                program: value[1].data,
                                sent: timeSent[0],
                                inv: invs[0],
                                status: getInvStatus(invs[0])
                            });
                            timeSent.shift();
                            invs.shift();
                        }).catch(function (e) {
                            console.log('This should never happen');
                        });
                    }
                }
            });
        }
    }

    ProgramInvController.$inject = ['InvitationService', 'ProgramService', '$location', '$rootScope', 'FlashService', '$routeParams', 'UserService', '$q', '$scope'];

    function ProgramInvController(InvitationService, ProgramService, $location, $rootScope, FlashService, $routeParams, UserService, $q, $scope) {
        var vm = this;
        vm.invites = [];
        vm.logged = false;
        vm.user = {};

        initController();

        function initController() {
            loadCurrentUser();
            getProgramInvs();
        }

        function loadCurrentUser() {
            if (!$rootScope.globals.hasOwnProperty('currentUser')) {
                vm.logged = false;
            } else {
                vm.user = $rootScope.globals.currentUser;
                vm.logged = true;
            }
        }

        function getInvStatus(inv) {
            if (inv.hasOwnProperty('activated')) {
                return 'Accepted';
            } else if (inv.declined) {
                return 'Declined';
            } else return 'Pending';
        }

        function getProgramInvs() {
            vm.invites = [];
            vm.programId = $routeParams.id;
            ProgramService.GetById($routeParams.id).then(function (programResp) {
                if (programResp.success) {
                    InvitationService.GetByProgram($routeParams.id).then(function (response) {
                        if (response.success) {
                            var result = response.data.entries;
                            var byUserPromises = [];
                            var toUserPromises = [];
                            var timeSent = [];
                            var invs = [];
                            for (var i = 0; i < result.length; i++) {
                                byUserPromises.push(UserService.GetUser(result[i].byUserId));
                                toUserPromises.push(UserService.GetUser(result[i].toUserId));
                                timeSent.push(new Date(result[i].sent));
                                invs.push(result[i]);
                            }
                            for (var j = 0; j < byUserPromises.length; j++) {
                                $q.all([byUserPromises[j], toUserPromises[j]]).then(function (value) {
                                    vm.invites.push({
                                        byUser: value[0].data,
                                        toUser: value[1].data,
                                        program: programResp.data,
                                        sent: timeSent[0],
                                        inv: invs[0],
                                        status: getInvStatus(invs[0])
                                    });
                                    timeSent.shift();
                                    invs.shift();
                                }).catch(function (e) {
                                    console.log('This should never happen');
                                });
                            }
                        }
                    });
                }
            });
        }

        /* Expand invitation to see its status */

        $scope.tableRowExpanded = false;
        $scope.tableRowIndexExpandedCurr = "";
        $scope.tableRowIndexExpandedPrev = "";
        $scope.storeIdExpanded = "";

        $scope.dayDataCollapseFn = function () {
            $scope.dayDataCollapse = [];
            for (var i = 0; i < vm.invites.length; i += 1) {
                $scope.dayDataCollapse.push(false);
            }
        };


        $scope.selectTableRow = function (index, storeId) {
            if (typeof $scope.dayDataCollapse === 'undefined') {
                $scope.dayDataCollapseFn();
            }

            if ($scope.tableRowExpanded === false && $scope.tableRowIndexExpandedCurr === "" && $scope.storeIdExpanded === "") {
                $scope.tableRowIndexExpandedPrev = "";
                $scope.tableRowExpanded = true;
                $scope.tableRowIndexExpandedCurr = index;
                $scope.storeIdExpanded = storeId;
                $scope.dayDataCollapse[index] = true;
            } else if ($scope.tableRowExpanded === true) {
                if ($scope.tableRowIndexExpandedCurr === index && $scope.storeIdExpanded === storeId) {
                    $scope.tableRowExpanded = false;
                    $scope.tableRowIndexExpandedCurr = "";
                    $scope.storeIdExpanded = "";
                    $scope.dayDataCollapse[index] = false;
                } else {
                    $scope.tableRowIndexExpandedPrev = $scope.tableRowIndexExpandedCurr;
                    $scope.tableRowIndexExpandedCurr = index;
                    $scope.storeIdExpanded = storeId;
                    $scope.dayDataCollapse[$scope.tableRowIndexExpandedPrev] = false;
                    $scope.dayDataCollapse[$scope.tableRowIndexExpandedCurr] = true;
                }
            }

        };


    }

    TreeChartController.$inject = ['InvitationService', '$location', '$rootScope', 'FlashService', '$compile', '$timeout', '$routeParams', '$scope', '$window', '$route'];

    function TreeChartController(InvitationService, $location, $rootScope, FlashService, $compile, $timeout, $routeParams, $scope, $window, $route) {
        var vm = this;
        vm.chartScript = {};
        vm.logged = false;
        vm.user = {};

        initController();

        vm.reloadData = reloadData;

        function initController() {
            loadCurrentUser();
            InvitationService.GetTreeChart($routeParams.id).then(function (value) {
                if (value.success) {
                    vm.chartScript = 'var config = {\n' +
                        '\tcontainer: "#tree_chart"\n' +
                        '};';
                    vm.chartScript = getScript(value.data);
                    getChart();
                }
            })
        }

        function loadCurrentUser() {
            if (!$rootScope.globals.hasOwnProperty('currentUser')) {
                vm.logged = false;
            } else {
                vm.user = $rootScope.globals.currentUser;
                vm.logged = true;
            }
        }

        function reloadData() {
            $window.location.reload();
        }

        function getScript(respData) {
            var text = 'var config = {\n' +
                '\tcontainer: "#tree_chart"\n' +
                '};';
            for (var i = 1; i < respData.chart_config.length; i++) {
                var toBeAdded = respData.chart_config[i];
                if (i == 1) {
                    text = text.concat('\nvar ' + toBeAdded + ' = {\n' + ' text: { name: "' + respData[toBeAdded].text.name + '"}}; \n')
                } else {
                    text = text.concat('\nvar ' + toBeAdded + ' = {\n' + ' parent: ' + respData[toBeAdded].parent + ', text: { name: "' + respData[toBeAdded].text.name + '"}}; \n');
                }
            }
            text = text.concat('\nvar chart_config = [\n');
            for (var j = 0; j < respData.chart_config.length; j++) {
                var added = respData.chart_config[j];
                text = text.concat(added + ',\n');
            }
            text = text.concat(']; \nnew Treant (chart_config);');
            return text;
        }

        function getChart() {
            $timeout(function () {
                $timeout(function () {
                    var element = $("#chart");
                    element.empty();
                    var elem = $("#toRemove");
                    elem.remove();
                    $compile(elem)($scope);
                    element.append('<script id="toRemove">' + vm.chartScript + '</script>');
                    $compile(element)($scope)
                })
            }, 100);
        }
    }


})();