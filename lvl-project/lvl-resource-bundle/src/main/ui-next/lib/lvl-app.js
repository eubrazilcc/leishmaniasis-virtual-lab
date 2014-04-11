/* Application */

/* integrates FastClick */
angular.module('lvl.fastclick', []).run(function() {
	'use strict';
	FastClick.attach(document.body);
});

angular.module('lvl', [ 'ngRoute', 'lvl.filters', 'lvl.services', 'lvl.directives', 'lvl.controllers', 'lvl.fastclick'])
.config(['$routeProvider', function($routeProvider) {
	'use strict';
	$routeProvider.when('/', {templateUrl: 'partials/home.html', controller: 'HomeController'});
	$routeProvider.when('/404', {templateUrl: 'partials/404.html', controller: 'PageNotFoundController'});
	$routeProvider.otherwise({redirectTo: '/404'});
}]);
