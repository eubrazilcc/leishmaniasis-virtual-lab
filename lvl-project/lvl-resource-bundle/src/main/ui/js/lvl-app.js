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
	$routeProvider.when('/', {templateUrl: 'partials/home.html', controller: 'HomeCtrl'});
	$routeProvider.when('/login/:ref?/:fail?', {templateUrl: 'partials/login.html', controller: 'LoginCtrl'});	
	$routeProvider.when('/user/validate/:email?/:code?', {templateUrl: 'partials/user_validation.html', controller: 'UserValidationCtrl'});	
	$routeProvider.when('/files', {templateUrl: 'partials/filestore.html', controller: 'FileStoreCtrl', reqAuth: true});
	$routeProvider.when('/map', {templateUrl: 'partials/mapviewer.html', controller: 'MapViewerCtrl'});
	$routeProvider.when('/404', {templateUrl: 'partials/404.html', controller: 'PageNotFoundCtrl'});
	$routeProvider.otherwise({redirectTo: '/404'});
}])
.run(['$rootScope', 'CookieFactory', function($rootScope, CookieFactory) {
	CookieFactory.load();
}])
.run(['$rootScope', '$location', 'UserAuthFactory', function($rootScope, $location, UserAuthFactory) {
	$rootScope.$on('$routeChangeStart', function(event, next, current) {		
		if (next.reqAuth) {
			UserAuthFactory().catch(function(reason) {
				$location.path('/login/' + encodeURIComponent($location.path()) + '/unauthenticated');
			});
		}
	});
}])
.run(['$rootScope', '$location', '$anchorScroll', '$routeParams', function($rootScope, $location, $anchorScroll, $routeParams) {
	$rootScope.$on('$routeChangeSuccess', function(next, current) {
		$location.hash($routeParams.scrollTo);
		$anchorScroll();  
	});
}]);