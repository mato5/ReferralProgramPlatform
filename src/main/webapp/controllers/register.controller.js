(function () {
    'use strict';

    var compareTo = function () {
        return {
            require: "ngModel",
            scope: {
                otherModelValue: "=compareTo"
            },
            link: function (scope, element, attributes, ngModel) {

                ngModel.$validators.compareTo = function (modelValue) {
                    return modelValue == scope.otherModelValue;
                };

                scope.$watch("otherModelValue", function () {
                    ngModel.$validate();
                });
            }
        };
    };

    angular
        .module('platform')
        .controller('RegisterController', RegisterController)
        .directive('compareTo', compareTo)
        .directive('emailNotUsed', function ($http, $q) {
            return {
                require: 'ngModel',
                link: function (scope, element, attrs, ngModel) {
                    ngModel.$asyncValidators.emailNotUsed = function (modelValue, viewValue) {
                        return $http.put('http://localhost:8080/platform/api/users/email_exists', viewValue).then(function (response) {
                            return response.data == true ? $q.reject('Email is already used.') : true;
                        });
                    };
                }
            };
        })
        .directive('emailUsed', function ($http, $q) {
            return {
                require: 'ngModel',
                link: function (scope, element, attrs, ngModel) {
                    ngModel.$asyncValidators.emailNotUsed = function (modelValue, viewValue) {
                        return $http.put('http://localhost:8080/platform/api/users/email_exists', viewValue).then(function (response) {
                            return response.data != true ? $q.reject('Email is not used.') : true;
                        });
                    };
                }
            };
        })
    ;

    RegisterController.$inject = ['UserService', '$location', '$rootScope', 'FlashService'];

    function RegisterController(UserService, $location, $rootScope, FlashService) {
        var vm = this;

        vm.register = register;

        function register() {

            vm.dataLoading = true;
            vm.user.type = "CUSTOMER";
            UserService.Create(vm.user)
                .then(function (response) {
                    if (response.success) {
                        FlashService.Success('Registration successful', true);
                        $location.path('/login');
                    } else {
                        FlashService.Error(response.message);
                        vm.dataLoading = false;
                    }
                })
        }
    }

})();