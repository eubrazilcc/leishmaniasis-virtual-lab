'use strict';

/* Services */

angular.module('lvl.services', [])
.factory('Oauth2Factory', function($http, $window, ENV) {
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
.factory('UserAuthFactory', function ($q, $timeout, $http, $location, Oauth2Factory) {
	var userInfo = null;
	return function () {
		var defer = $q.defer();
		if (userInfo) {
			// console.log("User info exists: " + userInfo);
			defer.resolve(userInfo);
		} else {
			// console.log("Requesting user info...");
			Oauth2Factory.user().success(function (data, status) {
				
				
				// TODO
				for (var prop in data) {
					console.log(prop + " => " + data[prop]);
				}
				// TODO
				
				
				if (data.username != null) {
					userInfo = data;
					console.log("User info obtained: " + userInfo); // TODO
					defer.resolve(userInfo);
				} else {
					console.log("Failed to get user info"); // TODO
					defer.reject(data);
				}
			}).error(function (data, status) {
				console.log("401 Response. Rejecting defer"); // TODO
				defer.reject(data);
			});
		}
		return defer.promise;
	};
})
.factory('AccessTokenFactory', function ($q, $timeout, $http, $location, Oauth2Factory) {
	var accessToken = null;
	return function (user) {
		var defer = $q.defer();
		if (accessToken) {
			// console.log("Access token exists: " + accessToken);
			defer.resolve(accessToken);
		} else {
			// console.log("Requesting access token...");
			Oauth2Factory.token(user).success(function (data, status) {				
				if (data["access_token"] !== undefined) {
					accessToken = data["access_token"];
					console.log("Access token obtained: " + accessToken); // TODO
					defer.resolve(accessToken);
				} else {
					// console.log("Failed to get access token");
					defer.reject(data);
				}
			}).error(function (data, status) {
				// console.log("400 Response. Rejecting defer");
				defer.reject(data);
			});
		}		
		return defer.promise;
	};
});