// (function () {
//     'use strict';
//
//     AddAdminController.$inject = ['UserService', '$location', '$rootScope', 'FlashService'];
//
//     function AddAdminController(UserService, $location, $rootScope, FlashService) {
//         var vm = this;
//
//         vm.add = add;
//
//         function add() {
//
//             vm.dataLoading = true;
//             vm.user.type = "CUSTOMER";
//             UserService.Create(vm.user)
//                 .then(function (response) {
//                     if (response.success) {
//                         FlashService.Success('Registration successful', true);
//                         $location.path('/login');
//                     } else {
//                         FlashService.Error(response.message);
//                         vm.dataLoading = false;
//                     }
//                 })
//         }
//     }
// });