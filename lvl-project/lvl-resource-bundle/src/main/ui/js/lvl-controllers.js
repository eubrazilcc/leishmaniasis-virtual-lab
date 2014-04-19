'use strict';

/* Controllers */

angular.module('lvl.controllers', [])
.controller('HomeCtrl', ['$scope', '$rootScope', '$window', '$location', 'UserRegistrationFactory', function($scope, $rootScope, $window, $location, UserRegistrationFactory) {
	$rootScope.$watch( 
			function() {
				return $window.sessionStorage.token; 
			}, 
			function(newValue, oldValue) {		
				$scope.isAuthenticated = (typeof $window.sessionStorage.token !== undefined && $window.sessionStorage.email !== undefined);
				$scope.style1 = $scope.isAuthenticated ? 'col-xs-12' : 'col-md-8 col-xs-12';
				$scope.style2 = 'col-md-4 col-xs-12';
			});
	$scope.signup = function() {
		UserRegistrationFactory.register($scope.user).then(
				function (data) {
					$location.path('/user/validate/' + $scope.user.email);
				},
				function (reason) {

					// TODO
					console.log("FAILED: " + reason);
					// TODO

				},
				null
		);
	};
}])
.controller('NavBarCtrl', ['$scope', '$rootScope', '$window', 'ENV', 'CookieFactory', 'Oauth2Factory', function($scope, $rootScope, $window, ENV, CookieFactory, Oauth2Factory) {
	$rootScope.$watch( 
			function() {
				return $window.sessionStorage.token; 
			}, 
			function(newValue, oldValue) {		
				$scope.isAuthenticated = (typeof $window.sessionStorage.token !== undefined && $window.sessionStorage.email !== undefined);
			});
	$scope.logout = function() {
		$scope.isAuthenticated = false;
		// delete token from remote OAuth service
		Oauth2Factory.revoke();
		// clear session
		delete $window.sessionStorage.token;
		delete $window.sessionStorage.email;
		delete $window.sessionStorage.userInfo;
		// clear cookies
		CookieFactory.remove();		
	};
}])
.controller('LoginCtrl', ['$scope', '$routeParams', '$window', '$location', 'ENV', 'AccessTokenFactory', 'CookieFactory', function($scope, $routeParams, $window, $location, ENV, AccessTokenFactory, CookieFactory) {
	$scope.rememberme = true;
	var referrer = $routeParams.ref !== undefined ? $routeParams.ref : '/';
	$scope.showAlert = $routeParams.fail !== undefined;
	switch ($routeParams.fail) {
	case "refused":
		$scope.alertMessage = 'Authorization has been refused for the provided credentials.';
		break;
	case "unauthenticated":
	default:
		$scope.alertMessage = 'The webpage you are trying to access requires additional authentication.';
	}
	$scope.login = function() {		
		// workaround to solve the problem with AJAX, remember password and single-page applications
		if ($scope.user === undefined) {
			$scope.user = {
					email: $("#email").val(),
					password: $("#password").val()
			};
		}
		AccessTokenFactory($scope.user).then(
				function (accessToken) {					
					if (accessToken !== undefined) {
						$window.sessionStorage.token = accessToken;
						$window.sessionStorage.email = $scope.user.email;
						if ($scope.rememberme === true) {
							CookieFactory.store();
						} else {
							CookieFactory.remove();
						}
						$location.path(referrer);
					} else {
						$location.path('/login/' + encodeURIComponent(referrer) + '/refused');
					}
				},
				function (reason) {
					$location.path('/login/' + encodeURIComponent(referrer) + '/refused');
				},
				null
		);
	};
}])
.controller('UserValidationCtrl', ['$scope', '$routeParams', 'UserRegistrationFactory', function($scope, $routeParams, UserRegistrationFactory) {
	$scope.showAlert = false;
	$scope.user = {
			email: ($routeParams.email !== undefined ? $routeParams.email : ''),
			code: ($routeParams.code !== undefined ? $routeParams.code : '')
	};
	$scope.activate = function() {
		UserRegistrationFactory.activate($scope.user).then(
				function (data) {

					// TODO
					console.log("HERE IS OK!");		
					// TODO

				},
				function (reason) {

					// TODO
					console.log("HERE IS BAD!");		
					// TODO

				},
				null
		);

		/*		
		$scope.msgHeader = 'Account validation failed';
        $scope.msgBody = 'Check that the email you entered coincides with the email address that you provided during registration, check your activation code and try again.';		
		 */

	};
	$scope.resend = function() {
		UserRegistrationFactory.resendActivationCode($scope.user).then(
				function (data) {
					$scope.msgHeader = 'The activation code has been successfully sent to your email';
					$scope.msgBody = 'Please check your inbox. Within the next 10 minutes you should find a message from Leish VirtLab containing your activation code.';
					$scope.style1 = 'alert alert-success fade in';
					$scope.style2 = 'btn btn-success';
					$scope.showButtons = false;
					$scope.showAlert = true;
				},
				function (reason) {
					$scope.msgHeader = 'The activation code has not been sent';
					$scope.msgBody = 'Please wait a few minutes before trying to re-send the activation code.';
					$scope.style1 = 'alert alert-danger fade in';
					$scope.style2 = 'btn btn-danger';
					$scope.showButtons = true;
					$scope.showAlert = true;
				},
				null
		);
	};
}])
.controller('FileStoreCtrl', ['$scope', function($scope) {
	$scope.toggleTopEntries = function() {
		$scope.isTopEntriesVisible = ! $scope.isTopEntriesVisible;
	};
	// default top entries to be visible
	$scope.isTopEntriesVisible = true;
}])
.controller('MapViewerCtrl', ['$scope', function($scope) {
	// initial setup
	$scope.longitude = 3.7036;
	$scope.latitude = 40.4169;
	$scope.zoom = 5.5;
	$scope.alerts = [];
	$scope.addAlert = function(msg) {
		$scope.alerts.push({type: 'danger', msg: msg});
	};
	$scope.closeAlert = function(index) {
		$scope.alerts.splice(index, 1);
	};
	// setup map
	var center = ol.proj.transform([$scope.longitude, $scope.latitude], 'EPSG:4326', 'EPSG:3857');
	var map = new ol.Map({
		controls: ol.control.defaults().extend([ new ol.control.FullScreen(), new ol.control.ScaleLine({ units: 'metric' }) ]),
		layers: [
		         new ol.layer.Tile({
		        	 preload: Infinity,
		        	 source: new ol.source.OSM()
		         })
		         ],
		         renderer: 'canvas', // fastest renderer, other renderers: 'webgl', ol.RendererHints.createFromQueryData()
		         target: 'map',      // div HTML element with id='map'
		         view: new ol.View2D({
		        	 center: center,
		        	 zoom: $scope.zoom
		         }),
		         ol3Logo: false
	});
	/*// add zoom to extent control
	var extent = map.getView().getView2D().calculateExtent(map.getSize());
	var bottomLeft = ol.extent.getBottomLeft(extent);
	var topRight = ol.extent.getTopRight(extent);
	map.addControl(new ol.control.ZoomToExtent({ extent: [ bottomLeft[0], bottomLeft[1], topRight[0], topRight[1] ] })); */
	// add custom control to return map home
	var MapHomeControl = function(opt_options) {
		var options = opt_options || {};

		var anchor = document.createElement('a');
		anchor.href = '#map-home';
		anchor.innerHTML = '<i class="fa fa-home"></i><span role="tooltip">Return home</span>';		

		var handleMapHome = function(e) {
			e.preventDefault();
			map.getView().getView2D().setCenter(center);
			map.getView().getView2D().setZoom($scope.zoom);
		}

		anchor.addEventListener('click', handleMapHome, false);
		anchor.addEventListener('touchstart', handleMapHome, false);

		var element = document.createElement('div');
		element.className = 'map-home ol-has-tooltip ol-unselectable';
		element.appendChild(anchor);

		ol.control.Control.call(this, {
			element: element,
			target: options.target
		});
	};
	ol.inherits(MapHomeControl, ol.control.Control);
	map.addControl(new MapHomeControl());
	// add export PNG functionality
	var exportPNGElement = document.getElementById('export-png');
	if ('download' in exportPNGElement) {
		exportPNGElement.addEventListener('click', function(e) {
			map.once('postcompose', function(event) {
				var canvas = event.context.canvas;
				exportPNGElement.href = canvas.toDataURL('image/png');
			});
			map.render();			
		}, false);
	} else {
		// display error message
		$scope.addAlert('Export PNG feature requires a browser that supports the <a class="alert-link" href="http://caniuse.com/#feat=download" target="_blank">link download</a> attribute.');
	}
}])
.controller('PageNotFoundCtrl', function() {
	// nothing yet
});