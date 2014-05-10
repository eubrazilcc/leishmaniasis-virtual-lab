'use strict';

/* Services */

var authNHeaders = function($window) {
	return (typeof $window.sessionStorage.token !== undefined 
			? { 'Authorization': 'Bearer ' + $window.sessionStorage.token } : null );
}

angular.module('lvl.services', [])
.factory('LocalStorageFactory', [ '$window', function($window) {
	return {
		load: function() {
			if (typeof $window.sessionStorage.token === undefined || $window.sessionStorage.email === undefined) {
				var obj = $window.localStorage.getItem('user');
				if (obj) {					
					var existingUser = JSON.parse(obj);
					$window.sessionStorage.token = existingUser.token;
					$window.sessionStorage.email = existingUser.email;
					$window.sessionStorage.setItem('showNotifications', existingUser.showNotifications);
				}				
			}
		},
		store: function() {			
			$window.localStorage.setItem('user', JSON.stringify({
				'token': $window.sessionStorage.token,
				'email': $window.sessionStorage.email,
				'showNotifications': $window.sessionStorage.getItem('showNotifications')
			}));			
		},		
		remove: function() {
			$window.localStorage.removeItem('user');
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
		},
		profile: function(username) {
			return $http({
				url: ENV.oauth2Endpoint + '/users/' + encodeURIComponent(username),
				method: 'GET',
				headers: authNHeaders($window)
			});
		}
	};
}])
.factory('PendingUsersFactory', [ '$http', 'ENV', function($http, ENV) {
	return {
		register: function(user) {		
			return $http({
				url: ENV.oauth2Endpoint + '/pending_users',
				method: 'POST',
				data: user,
				headers: {'Content-Type': 'application/json'}
			});
		},
		activate: function(user) {
			return $http({
				url: ENV.oauth2Endpoint + '/pending_users/' + user.email,
				method: 'PUT',
				data: { 'activationCode' : user.code, 'user' : { 'email' : user.email } },
				headers: {'Content-Type': 'application/json'}
			});
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
.factory('TaskFactory', [ '$http', '$q', '$window', 'ENV', function($http, $q, $window, ENV) {	
	return {
		importSequences: function() {
			var defer = $q.defer();
			$http({
				url: ENV.lvlEndpoint + '/tasks',
				method: 'POST',
				data: { 'type' : 'IMPORT_SEQUENCES', 'ids' : [ '353470160', '353483325', '384562886' ] },
				headers: authNHeaders($window)				
			}).success(function (data, status, headers) {
				if (headers('Location')) {
					defer.resolve(headers('Location'));					
				} else {
					defer.reject(data);
				}
				defer.resolve(data);
			}).error(function (data, status) {
				defer.reject(data);
			});
			return defer.promise;
		},
		progress: function(id) {
			return new EventSource(ENV.lvlEndpoint + '/tasks/progress/' + id + "?refresh=" + ENV.refresh);
		}
	};
}])
.factory('AccessTokenFactory', [ '$q', '$window', 'Oauth2Factory', function ($q, $window, Oauth2Factory) {
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
.factory('UserRegistrationFactory', [ '$q', 'PendingUsersFactory', function ($q, PendingUsersFactory) {	
	var service = function(http, op, defer) {
		http.success(function (data, status) {
			// console.log("Executing operation " + op + "...");
			if (data !== undefined) {
				// console.log("Successful operation " + op);
				defer.resolve();
			} else {
				// console.log("Failed operation " + op);
				defer.reject(500);
			}
		}).error(function (data, status) {
			// console.log(status + " response in operation " + op);
			defer.reject(status);
		});
	}
	return {		
		register : function(user) {
			var defer = $q.defer();
			service(PendingUsersFactory.register(user), 'register new user', defer);
			return defer.promise;
		},
		activate : function(user) {
			var defer = $q.defer();
			service(PendingUsersFactory.activate(user), 'activate user account', defer);
			return defer.promise;
		},
		resendActivationCode: function(user) {
			var defer = $q.defer();
			service(PendingUsersFactory.resendActivationCode(user), 'resend activation code', defer);
			return defer.promise;
		}
	};
}]);