(function() {
    'use strict';

    angular
        .module('21PointsApp')
        .controller('HomeController', HomeController);

    HomeController.$inject = ['$scope', 'Principal', 'LoginService', '$state', 'Points', 'Preferences'];

    function HomeController ($scope, Principal, LoginService, $state, Points, Preferences) {
        var vm = this;

        vm.account = null;
        vm.isAuthenticated = null;
        vm.login = LoginService.open;
        vm.register = register;
        vm.today = new Date();

        $scope.$on('authenticationSuccess', function() {
            getAccount();
        });

        getAccount();

        function register () {
            $state.go('register');
        }

        function getAccount() {
            Principal.identity().then(function(account) {
                vm.account = account;
                vm.isAuthenticated = Principal.isAuthenticated;
            });

            Points.thisWeek(function(data) {
                vm.pointsThisWeek = data;
                vm.pointsPercentage = (data.points / 21) * 100;
            });

            Preferences.user(function(data) {
                vm.preferences = data;
            })
        }
    }
})();
