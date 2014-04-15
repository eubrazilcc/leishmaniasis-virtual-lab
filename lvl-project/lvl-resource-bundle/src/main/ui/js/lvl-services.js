'use strict';

/* Services */

angular.module('lvl.services', [])
.factory('Oauth2Service', function($http, $window, ENV) {
	return {
		token: function(user) {
			return $http({
				url: ENV.oauth2Endpoint + '/token',
				method: "POST",
				data: 'client_id=' + encodeURIComponent(ENV.oauth2ClientApp.client_id)
				+ '&client_secret=' + encodeURIComponent(ENV.oauth2ClientApp.client_secret)
				+ '&grant_type=password'
				+ '&username=' + encodeURIComponent(user.email)
				+ '&password=' + encodeURIComponent(user.password)
				+ '&use_email=true',
				headers: {'Content-Type': 'application/x-www-form-urlencoded'}			
			});
		},
		signIn: function(authResult) {

			// TODO

			return $http.post(ENV.oauth2Endpoint + '/connect', authResult);
		},
		disconnect: function() {

			// TODO

			return $http.post(ENV.oauth2Endpoint + '/disconnect');
		},
		user: function() {
			if (typeof $window.sessionStorage.token !== undefined) {
				$http.defaults.headers.common.Authorization = "Bearer " + $window.sessionStorage.token;
			}
			return $http.get(ENV.oauth2Endpoint + '/users');
		}
	};
})
.factory('UserAuthService', function ($q, $timeout, $http, $rootScope, $location, Oauth2Service) {
	return {
		userInfo: null,
		token: function (user) {
			var defer = $q.defer();
			Oauth2Service.token(user).success(function (data, status) {				
				if (data["access_token"] !== undefined) {
					var access_token = data["access_token"];
					console.log("Access token obtained: " + access_token); // TODO
					defer.resolve(access_token);
				} else {
					console.log("Failed to get access token"); // TODO
					defer.reject(data);
				}
			}).error(function (data, status) {
				console.log("401 Response. Rejecting defer");  // TODO
				defer.reject(data);
			});
			return defer.promise;
		},
		isAuthenticated: function () {
			var defer = $q.defer();
			if (this.userInfo) {
				// console.log("User info exists: " + userInfo);
				defer.resolve(this.userInfo);
			} else {
				// console.log("Requesting user info...");
				Oauth2Service.user().success(function (data, status) {
					if (data.id != null) {
						this.userInfo = data;
						// console.log("User info obtained: " + this.userInfo);
						defer.resolve(this.userInfo);
					} else {
						// console.log("Failed to get user info");
						defer.reject(data);
					}
				}).error(function (data, status) {
					// console.log("401 Response. Rejecting defer");
					defer.reject(data);
				});
			}
			return defer.promise;
		}
	}
});