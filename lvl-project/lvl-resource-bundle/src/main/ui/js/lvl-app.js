'use strict';

/* integrates FastClick */
angular.module('lvl.fastclick', [])
.run(function() {
	FastClick.attach(document.body);
});

/* global configuration */
angular.module('lvl.config', [])
.constant('ENV', {
	'oauth2Endpoint'    : 'http://lvl.i3m.upv.es/lvl-auth/oauth2/v1',
	'oauth2ClientApp'   : {
		'client_id'     : 'lvl_portal',
		'client_secret' : 'changeit'
	},
	'lvlCookieId': 'LVL-LeishVirtLab'
});

angular.module('lvl', [ 'ngRoute', 'ngSanitize', 'ngCookies', 'chieffancypants.loadingBar', 'lvl.config', 'lvl.filters', 'lvl.services', 'lvl.directives', 'lvl.controllers', 'lvl.fastclick', 'ui.bootstrap' ])
.config(['$routeProvider', function($routeProvider) {
	var isLoggedIn = ['$q', '$http', '$window', 'Oauth2Factory', function($q, $http, $window, Oauth2Factory) {
		var defer = $q.defer();
		if ($window.sessionStorage.getItem('user')) {
			// console.log("User info exists: " + $window.sessionStorage.getItem('user'));
			defer.resolve($window.sessionStorage.getItem('user'));
		} else {
			// console.log("Requesting user info...");
			Oauth2Factory.user().success(function (data, status) {
				if (data.username != null) {
					$window.sessionStorage.setItem('user', JSON.stringify(data));
					/* var obj = JSON.parse($window.sessionStorage.getItem('user'));
					var str = "";
					for (var i in obj) {
						if (obj.hasOwnProperty(i)) {
							str += "User" + "." + i + " = " + obj[i] + "\n";
						}
					}
					console.log("User info obtained:\n" + str); */					
					defer.resolve($window.sessionStorage.getItem('user'));
				} else {
					// console.log("Failed to get user info");
					defer.reject({ needsAuthentication: true });
				}
			}).error(function (data, status) {
				// console.log("401 Response. Rejecting defer");
				defer.reject({ needsAuthentication: true });
			});
		}
		return defer.promise;		
	}];
	$routeProvider.whenAuthenticated = function(path, route, controller) {
		route.resolve = route.resolve || {};
		angular.extend(route.resolve, { isLoggedIn: isLoggedIn });
		return $routeProvider.when(path, route, controller);
	};
	$routeProvider.when('/', {templateUrl: 'partials/home.html', controller: 'HomeCtrl'});
	$routeProvider.when('/login/:ref?/:fail?', {templateUrl: 'partials/login.html', controller: 'LoginCtrl'});	
	$routeProvider.when('/user/validate/:email?/:code?', {templateUrl: 'partials/user_validation.html', controller: 'UserValidationCtrl'});	
	$routeProvider.whenAuthenticated('/user/profile/:username?', {templateUrl: 'partials/user_profile.html', controller: 'UserProfileCtrl'});
	$routeProvider.whenAuthenticated('/files', {templateUrl: 'partials/filestore.html', controller: 'FileStoreCtrl'});
	$routeProvider.whenAuthenticated('/settings', {templateUrl: 'partials/settings.html', controller: 'SettingsCtrl'});
	$routeProvider.when('/map', {templateUrl: 'partials/mapviewer.html', controller: 'MapViewerCtrl'});
	$routeProvider.when('/404', {templateUrl: 'partials/404.html', controller: 'PageNotFoundCtrl'});
	$routeProvider.otherwise({redirectTo: '/404'});
}])
.run(['$rootScope', 'CookieFactory', function($rootScope, CookieFactory) {
	CookieFactory.load();
}])
.run(['$rootScope', '$location', '$route', function($rootScope, $location, $route) {
	$rootScope.$on('$routeChangeError', function(ev, current, previous, rejection) {
		if (rejection && rejection.needsAuthentication === true) {
			var returnUrl = $location.url();
			$location.path('/login/' + encodeURIComponent($location.path()) + '/unauthenticated');
		}
	});
}])
.run(['$rootScope', '$location', '$anchorScroll', '$routeParams', 
      function($rootScope, $location, $anchorScroll, $routeParams) {
	$rootScope.$on('$routeChangeSuccess', function(next, current) {
		$location.hash($routeParams.scrollTo);
		$anchorScroll();  
	});
}]);