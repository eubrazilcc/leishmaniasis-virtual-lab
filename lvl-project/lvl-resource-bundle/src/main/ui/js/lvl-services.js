'use strict';

/* Services */

var authNHeaders = function($window) {
	return (typeof $window.sessionStorage.token !== undefined ? { 'Authorization': 'Bearer ' + $window.sessionStorage.token } : null);
}

angular.module('lvl.services', [])
.factory('CookieFactory', [ '$window', '$cookieStore', 'ENV', function($window, $cookieStore, ENV) {
	return {
		load: function() {
			if (typeof $window.sessionStorage.token === undefined || $window.sessionStorage.email === undefined) {
				var existingCookieUser = $cookieStore.get(ENV.lvlCookieId);
				if (existingCookieUser) {
					$window.sessionStorage.token = existingCookieUser.token;
					$window.sessionStorage.email = existingCookieUser.email;
				}
			}
		},
		store: function() {
			$cookieStore.put(ENV.lvlCookieId, {
				'token': $window.sessionStorage.token,
				'email': $window.sessionStorage.email
			});
		},		
		remove: function() {
			$cookieStore.remove(ENV.lvlCookieId);
		}
	};
}])
.factory('Oauth2Factory', [ '$http', '$window', 'ENV', function($http, $window, ENV) {
	return {
		token: function(user) {			
			return $http({
				url: ENV.oauth2Endpoint + '/token',
				method: 'POST',
				data: 'client_id=' + encodeURIComponent(ENV.oauth2ClientApp.client_id)
				+ '&client_secret=' + encodeURIComponent(ENV.oauth2ClientApp.client_secret)
				+ '&grant_type=password'
				+ '&username=' + encodeURIComponent(user.email)
				+ '&password=' + encodeURIComponent(user.password)
				+ '&use_email=true',
				headers: {'Content-Type': 'application/x-www-form-urlencoded'}			
			});
		},		
		disconnect: function() {

			// TODO

			return $http.post(ENV.oauth2Endpoint + '/disconnect');
		},
		user: function() {			
			return $http({
				url: ENV.oauth2Endpoint + '/users/' + encodeURIComponent($window.sessionStorage.email),
				method: 'GET',
				params: { 'use_email': 'true' },
				headers: authNHeaders($window)
			});
		}
	};
}])
.factory('AccessTokenFactory', [ '$q', '$timeout', '$http', '$window', '$location', 'Oauth2Factory', function ($q, $timeout, $http, $window, $location, Oauth2Factory) {
	return function (user) {
		var defer = $q.defer();
		if ($window.sessionStorage.token) {
			// console.log("Access token exists: " + $window.sessionStorage.token);
			defer.resolve($window.sessionStorage.token);
		} else {
			// console.log("Requesting access token...");
			Oauth2Factory.token(user).success(function (data, status) {				
				if (data["access_token"] !== undefined) {
					$window.sessionStorage.token = data["access_token"];
					// console.log("Access token obtained: " + $window.sessionStorage.token);
					defer.resolve($window.sessionStorage.token);
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
}])
.factory('UserAuthFactory', [ '$q', '$timeout', '$http', '$window', '$location', 'Oauth2Factory', function ($q, $timeout, $http, $window, $location, Oauth2Factory) {
	return function () {
		var defer = $q.defer();
		if ($window.sessionStorage.userInfo) {
			// console.log("User info exists: " + $window.sessionStorage.userInfo);
			defer.resolve($window.sessionStorage.userInfo);
		} else {
			// console.log("Requesting user info...");
			Oauth2Factory.user().success(function (data, status) {
				if (data.username != null) {
					$window.sessionStorage.userInfo = data;
					// console.log("User info obtained: " + $window.sessionStorage.userInfo);
					defer.resolve($window.sessionStorage.userInfo);
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
	};
}]);