(function() {
    'use strict';
    angular
        .module('21PointsApp')
        .factory('Points', Points);

    Points.$inject = ['$resource', 'DateUtils'];

    function Points ($resource, DateUtils) {
        var resourceUrl =  'api/points/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.timestamp = DateUtils.convertLocalDateFromServer(data.timestamp);
                    }
                    return data;
                }
            },
            'update': {
                method: 'PUT',
                transformRequest: function (data) {
                    var copy = angular.copy(data);
                    copy.timestamp = DateUtils.convertLocalDateToServer(copy.timestamp);
                    return angular.toJson(copy);
                }
            },
            'save': {
                method: 'POST',
                transformRequest: function (data) {
                    var copy = angular.copy(data);
                    copy.timestamp = DateUtils.convertLocalDateToServer(copy.timestamp);
                    return angular.toJson(copy);
                }
            },
            'thisWeek': {
                method: 'GET',
                isArray: false,
                url: 'api/points-this-week'
            }
        });
    }


})();
