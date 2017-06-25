(function() {
    'use strict';

    angular
        .module('21PointsApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('home', {
            parent: 'app',
            url: '/',
            data: {
            authorities: []
            },
            views: {
                'content@': {
                    templateUrl: 'app/home/home.html',
                    controller: 'HomeController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate,$translatePartialLoader) {
                    $translatePartialLoader.addPart('home');
                    $translatePartialLoader.addPart('points');
                    $translatePartialLoader.addPart('bloodPressure');
                    $translatePartialLoader.addPart('weight');
                    return $translate.refresh();
                }]
            }
        })
        .state('points.add', {
            parent: 'home',
            url: 'add/points',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/points/points-dialog.html',
                    controller: 'PointsDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                date: null,
                                exercise: null,
                                meals: null,
                                alcohol: null,
                                notes: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('home', null, { reload: true });
                }, function() {
                    $state.go('home');
                });
            }]
        })
        .state('preferences.setting', {
            parent: 'home',
            url: 'preference/setting',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/preferences/preferences-dialog.html',
                    controller: 'PreferencesDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        /*
                        entity: ['Preferences', function(Preferences) {
                            var result =  Preferences.user;
                            return result;
                        }]
                        */
                        entity: function () {
                            return {
                                weeklyGoal: null,
                                weightUnits: null,
                                user: null
                            };
                        }
                    }
                }).result.then(function() {
                        $state.go('home', {}, { reload: true });
                    }, function() {
                        $state.go('home');
                    });
            }]
        })
        .state('blood-pressure.add', {
            parent: 'home',
            url: 'add/bloodpressure',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/blood-pressure/blood-pressure-dialog.html',
                    controller: 'BloodPressureDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                timestamp: null,
                                systolic: null,
                                diastolic: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                        $state.go('home', null, { reload: true });
                    }, function() {
                        $state.go('home');
                    });
            }]
        })
        .state('weights-add', {
            parent: 'home',
            url: 'add/weight',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/weight/weight-dialog.html',
                    controller: 'WeightDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                timestamp: null,
                                weight: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                        $state.go('home', null, { reload: true });
                    }, function() {
                        $state.go('home');
                    });
            }]
        });
    }
})();
