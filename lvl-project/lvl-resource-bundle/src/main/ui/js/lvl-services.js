'use strict';

/* Services */

angular.module('lvl.services', [])
.factory('Oauth2Service', function($http, ENV) {
	return {
		signIn: function(authResult) {
			return $http.post(ENV.oauth2Endpoint + '/connect', authResult);
		},
		disconnect: function() {
			return $http.post(ENV.oauth2Endpoint + '/disconnect');
		},
		user: function(callback) {
			$http.defaults.headers.common.Authorization = "Bearer 12345";
			return $http.get(ENV.oauth2Endpoint + '/users');
		}
	};
})
.factory('UserAuthService', function ($q, $timeout, $http, $rootScope, $location, Oauth2Service) {
	return {
		userInfo: null,
		isAuthenticated: function () {
			var defer = $q.defer();
			if (this.userInfo) {
				console.log("User info exists: " + userInfo); // TODO : comment out in production
				defer.resolve(this.userInfo);
			} else {
				console.log("Requesting user info...");
				Oauth2Service.user().success(function (data, status) {
					if (data.id != null) {
						this.userInfo = data;
						console.log("User info obtained: " + this.userInfo); // TODO : comment out in production
						defer.resolve(this.userInfo);
					} else {
						console.log("Failed to get user info"); // TODO : comment out in production
						defer.reject(data);
					}
				}).error(function (data, status, headers) {
					console.log("401 Response. Rejecting defer"); // TODO : comment out in production
					defer.reject(data);
				});
			}
			return defer.promise;
		}
	}
});