(function() {
    'use strict';

    angular
        .module('21PointsApp')
        .controller('HomeController', HomeController);

    HomeController.$inject = ['$scope', 'Principal', 'LoginService', '$state', 'Points', 'Preferences', 'BloodPressure', 'Weight', 'Chart'];

    function HomeController ($scope, Principal, LoginService, $state, Points, Preferences, BloodPressure, Weight, Chart) {
        var vm = this;

        vm.account = null;
        vm.isAuthenticated = null;
        vm.login = LoginService.open;
        vm.register = register;
        vm.today = new Date();

        $scope.$on('authenticationSuccess', function() {
            getAccount();
            getBpLast30Days();
            getWeightLast30Days();
        });

        getAccount();
        getBpLast30Days();
        getWeightLast30Days();

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

        function getBpLast30Days() {
            BloodPressure.last30Days(function(bpReadings) {
                vm.bpReadings = bpReadings;

                if (bpReadings.readings.length) {
                    vm.bpOptions = angular.copy(Chart.getBpChartConfig());
                    vm.bpOptions.title.text = bpReadings.period;
                    vm.bpOptions.chart.yAxis.axisLabel = "Blood Pressure";

                    var systolics, diastolics, upperValues, lowerValues;
                    systolics = [];
                    diastolics = [];
                    upperValues = [];
                    lowerValues = [];
                    bpReadings.readings.forEach(function(item) {
                        systolics.push({
                            x: new Date(item.timestamp),
                            y: item.systolic
                        });
                        diastolics.push({
                            x: new Date(item.timestamp),
                            y: item.diastolic
                        });
                        upperValues.push(item.systolic);
                        lowerValues.push(item.diastolic);
                    });

                    vm.bpData = [{
                        values: systolics,
                        key: 'Systolic',
                        color: '#673ab7'
                    },
                        {
                            values: diastolics,
                            key: 'Diastolic',
                            color: '#03a9f4'
                        }];

                    // set y scale to be 10 more than max and min
                    vm.bpOptions.chart.yDomain = [Math.min.apply(Math, lowerValues) - 10, Math.max.apply(Math, upperValues) + 10]
                }
            });
        }

        function getWeightLast30Days() {
            Weight.last30Days(function(weightReadings) {
                vm.weightReadings = weightReadings;

                if (weightReadings.readings.length) {
                    vm.weightOptions = angular.copy(Chart.getWeightChartConfig());
                    vm.weightOptions.title.text = weightReadings.period;
                    vm.weightOptions.chart.yAxis.axisLabel = "Weight";

                    var weights;
                    weights = [];
                    weightReadings.readings.forEach(function(item) {
                        weights.push({
                            x: new Date(item.timestamp),
                            y: item.weight
                        });
                    });

                    vm.weightData = [{
                        values: weights,
                        key: 'weight',
                        color: '#673ab7'
                        }];

                    // set y scale to be 10 more than max and min
                    vm.weightOptions.chart.yDomain = [0, 100]
                }
            });
        }
    }
})();
