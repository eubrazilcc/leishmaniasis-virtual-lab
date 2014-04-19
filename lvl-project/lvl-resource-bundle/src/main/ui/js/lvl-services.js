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
		revoke: function() {
			return $http({
				url: ENV.oauth2Endpoint + '/revoke',
				method: 'POST',
				data: 'client_id=' + encodeURIComponent(ENV.oauth2ClientApp.client_id)
				+ '&client_secret=' + encodeURIComponent(ENV.oauth2ClientApp.client_secret)
				+ '&token=' + encodeURIComponent($window.sessionStorage.token),
				headers: {'Content-Type': 'application/x-www-form-urlencoded'}			
			});
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
.factory('PendingUsersFactory', [ '$http', '$window', 'ENV', function($http, $window, ENV) {
	return {
		register: function(user) {		
			return $http({
				url: ENV.oauth2Endpoint + '/pending_users',
				method: 'POST',
				data: user,
				headers: {'Content-Type': 'application/json'}
			});
		},
		validate: function() {
			// TODO
			return null;
		},
		resendActivationCode: function(user) {
			return $http({
				url: ENV.oauth2Endpoint + '/pending_users/' + encodeURIComponent(user.email),
				method: 'GET',
				params: { 'send_activation': 'true' }
			});
		}
	};
}])
.factory('AccessTokenFactory', [ '$q', '$http', '$window', 'Oauth2Factory', function ($q, $http, $window, Oauth2Factory) {
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
.factory('UserRegistrationFactory', [ '$q', '$http', '$window', 'PendingUsersFactory', function ($q, $http, $window, PendingUsersFactory) {
	return function (user) {
		var defer = $q.defer();
		PendingUsersFactory.register(user).success(function (data, status) {
			// console.log("Registering new user...");
			if (data !== undefined) {
				// console.log("New user registered");
				defer.resolve();
			} else {
				// console.log("Failed to register new user");
				defer.reject(data);
			}
		}).error(function (data, status) {
			// console.log("400 Response. Rejecting defer");
			defer.reject(data);
		});
		return defer.promise;
	};
}])
.factory('ResendActivationCodeFactory', [ '$q', '$http', 'PendingUsersFactory', function ($q, $http, PendingUsersFactory) {
	return function (user) {
		var defer = $q.defer();
		PendingUsersFactory.resendActivationCode(user).success(function (data, status) {
			// console.log("Sending activation code...");
			if (data !== undefined) {
				// console.log("Activation code send to: " + user.email);
				defer.resolve();
			} else {
				// console.log("Failed to send activation code");
				defer.reject(data);
			}
		}).error(function (data, status) {
			// console.log("400 Response. Rejecting defer");
			defer.reject(data);
		});
		return defer.promise;
	};
}]);