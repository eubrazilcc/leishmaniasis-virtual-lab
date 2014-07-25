'use strict';

/* Controllers */

angular.module('lvl.controllers', [])
.controller('HomeCtrl', ['$scope', '$rootScope', '$window', '$location', 'UserRegistrationFactory', function($scope, $rootScope, $window, $location, UserRegistrationFactory) {
	$scope.alerts = [];
	$scope.user = {
			'username': '',
			'email': '',
			'password': ''
	};
	$scope.addAlert = function(msg, type) {
		$scope.alerts.push({'type': type || 'danger', 'msg': msg});
	};
	$scope.closeAlert = function(index) {
		$scope.alerts.splice(index, 1);
	};
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
					$scope.addAlert('<strong>Account registration failed.</strong> ' + (reason === 400 ? 'Username or email address are currently registered (<a class="alert-link" href="#/account_recovery/password_recovery‎">forgot password</a>).' : 'There is a temporary failure in the server. Please retry later.'), 'danger');
				},
				null
		);
	};
}])
.controller('NavBarCtrl', ['$scope', '$rootScope', '$window', 'LocalStorageFactory', 'Oauth2Factory', function($scope, $rootScope, $window, LocalStorageFactory, Oauth2Factory) {
	$scope.user = {
			'username': ''	
	};
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
		delete $window.sessionStorage.removeItem('user');
		// clear permanent storage
		LocalStorageFactory.remove();		
	};
}])
.controller('LoginCtrl', ['$scope', '$routeParams', '$window', '$location', 'AccessTokenFactory', 'LocalStorageFactory', function($scope, $routeParams, $window, $location, AccessTokenFactory, LocalStorageFactory) {
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
							LocalStorageFactory.store();
						} else {
							LocalStorageFactory.remove();
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
.controller('UserValidationCtrl', ['$scope', '$routeParams', '$location', '$timeout', 'UserRegistrationFactory', function($scope, $routeParams, $location, $timeout, UserRegistrationFactory) {
	$scope.alerts = [];
	$scope.addAlert = function(msg, type) {
		$scope.alerts.push({'type': type || 'danger', 'msg': msg});
	};
	$scope.closeAlert = function(index) {
		$scope.alerts.splice(index, 1);
	};
	$scope.user = {
			email: ($routeParams.email !== undefined ? $routeParams.email : ''),
			code: ($routeParams.code !== undefined ? $routeParams.code : '')
	};
	$scope.activate = function() {
		UserRegistrationFactory.activate($scope.user).then(
				function (data) {
					$scope.addAlert('<strong>Activation successful.</strong> You can now <a class="alert-link" href="#/login">log in</a> the portal directly using email and password used for account registration. You will be redirected to the login page automatically in 5 seconds.', 'success');
					var timer = $timeout(function() {
						$location.path('/login');
					}, 5000);
					$scope.$on(
							"$destroy",
							function(event) {
								$timeout.cancel(timer);
							}
					);
				},
				function (reason) {					
					$scope.addAlert('<strong>Account activation failed.</strong> Check that the email you entered coincides with the email address that you provided during registration and check your activation code.', 'danger');
				},
				null
		);
	};
	$scope.resend = function() {
		UserRegistrationFactory.resendActivationCode($scope.user).then(
				function (data) {					
					$scope.addAlert('<strong>The activation code has been successfully sent to your email.</strong> Please check your inbox. Within the next 10 minutes you should find a message from Leish VirtLab containing your activation code.', 'success');					
				},
				function (reason) {
					$scope.addAlert('<strong>The activation code has not been sent.</strong> '
							+ (reason === 404 ? 'No account is currently pending for activation that matches this email address.' : 'Please wait 5 minutes before trying to re-send the activation code.'), 'danger');					
				},
				null
		);
	};	
}])
.controller('UserProfileCtrl', ['$scope', '$routeParams', '$window', '$location', 'Oauth2Factory', function($scope, $routeParams, $window, $location, Oauth2Factory) {
	// default top entries to be visible
	$scope.isProfileDetailsVisible = true;	
	var profile = function(userObj) {
		if (userObj) {
			$scope.user = {
					'username': userObj['username'],
					'fullname': userObj['fullname'],
					'pictureUrl': userObj['pictureUrl'],
					'following': 10, // TODO
					'followers': 85  // TODO
			};
		} else {
			$location.path('/404');
		}
	};
	if ($routeParams.username !== undefined) {
		Oauth2Factory.profile($routeParams.username).then(
				function (result) {
					profile(result ? result['data'] : null);					
				},
				function (reason) {
					profile(null);
				},
				null
		);
	} else {
		profile(JSON.parse($window.sessionStorage.getItem('user')));
	}

	// TODO

}])
.controller('FileStoreCtrl', ['$scope', function($scope) {
	// default top entries to be visible
	$scope.isTopEntriesVisible = true;
	$scope.toggleTopEntries = function() {
		$scope.isTopEntriesVisible = ! $scope.isTopEntriesVisible;
	};

	// TODO
	$scope.user = {
			'username': 'ertorser',
			'fullname': 'Erik Torres',
			'following': 10,
			'followers': 85
	};
	// TODO

}])
.controller('SettingsCtrl', ['$scope', '$routeParams', 'TaskFactory', function($scope, $routeParams, TaskFactory) {	
	var tpl = '';
	if ($routeParams.section !== undefined && $routeParams.subsection !== undefined) {
		tpl = $routeParams.section + '-' + $routeParams.subsection;			
	}
	$scope.isUsersOpen = $routeParams.section === 'users';
	$scope.isSequencesOpen = $routeParams.section === 'sequences'; 
	$scope.isPapersOpen = $routeParams.section === 'papers'; 
	$scope.isIssuesOpen = $routeParams.section === 'issues';
	$scope.isTipsVisible = true;
	$scope.getTemplate = function() {
		var tplUrl = '';
		switch (tpl) {
		case 'users-active':
		case 'users-inactive':
		case 'sequences-public':
		case 'sequences-private':
		case 'papers-open_access':
		case 'papers-non_free':
		case 'issues-tasks':
		case 'issues-errors':
			tplUrl = 'partials/settings/' + tpl + '.html';
			break;
		default:
			tplUrl = 'partials/settings/index.html';
		}
		return tplUrl;
	}	
	$scope.resetProgress = function() {		
		$scope.progress = 0;
		$scope.max = 100;
		$scope.progressMsg = "";
		$scope.hasErrors = false;
	};
	$scope.isImportDisabled = false;
	$scope.resetProgress();
	$scope.importSequences = function() {		
		$scope.resetProgress();
		$scope.isImportDisabled = true;
		$scope.progressMsg = "Importing new sequences from external databases";
		TaskFactory.importSequences().then(				
				function (uri) {
					$scope.progress = 0;
					var eventSrc = TaskFactory.progress(uri.substring(uri.lastIndexOf("tasks/") + 6));
					eventSrc.addEventListener("progress", function(e) {
						var obj = JSON.parse(e.data);
						$scope.$apply(function () {
							$scope.progress = obj.progress.toFixed(0);
							$scope.progressMsg = obj.status;
							$scope.hasErrors = obj.hasErrors && obj.hasErrors === true;
						});
						if (obj.done && obj.done === true) {
							eventSrc.close();
							$scope.isImportDisabled = false;
						}

						// TODO
						console.log("EVENT: done=" + obj.done + "; progress=" + obj.progress + "; status=" + obj.status
								+ "; hasErrors=" + obj.hasErrors);
						// TODO

					}, false);
					eventSrc.onerror = function(e) {
						$scope.isImportDisabled = false;

						// TODO
						var obj = e;
						var str = "";
						for (var i in obj) {
							if (obj.hasOwnProperty(i)) {
								str += "Error" + "." + i + " = " + obj[i] + "\n";
							}
						}
						console.log("Error caught:\n" + str);
						// TODO

					};

					// TODO
					console.log("SUCCESS: " + uri);
					// TODO

				},
				function (reason) {
					$scope.isImportDisabled = false;
				}, 
				null
		);
	}

	// TODO

}])
.controller('SequencesCtrl', ['$scope', '$window', '$anchorScroll', 'SequencesFactory', 'UrlFactory', function($scope, $window, $anchorScroll, SequencesFactory, UrlFactory) {
	// tools
	$scope.showTools = false;
	$scope.toggleTools = function() {
		$scope.showTools = !$scope.showTools
	};
	// notifications
	$scope.showNotifications = ($window.sessionStorage.getItem('showNotifications') === 'false' ? false : true);
	$scope.toggleNotifications = function() {
		$scope.showNotifications = !$scope.showNotifications;
		$window.sessionStorage.setItem('showNotifications', $scope.showNotifications ? 'true' : 'false');
	};
	// selected items
	$scope.selectedSequences = [];
	// pagination	
	$scope.pageSizes = [ { 'size':20 }, { 'size':50 }, { 'size':100 } ];
	$scope.pageSize = $scope.pageSizes[0];
	$scope.totalItems = 0;
	$scope.currentPage = 1;
	$scope.maxSize = 7;
	$scope.itemsPerPage = $scope.pageSize.size;
	// data
	$scope.reload = function() {
		SequencesFactory.list(($scope.currentPage - 1) * $scope.pageSize.size, $scope.pageSize.size).then(
				function (resource) {
					var params = UrlFactory.linkParams(UrlFactory.getUrl(resource.last));
					$scope.itemsPerPage = $scope.pageSize.size;
					$scope.totalItems = (params && params.start ? params.start : $scope.totalItems);					
					$scope.sequences = resource.sequences;					
					var str = "";
					for (var i in resource) {
						if (resource.hasOwnProperty(i)) {
							str += "Sequence" + "." + i + " = " + resource[i] + "\n";
						}
					}
					// TODO console.log(">> Page " + $scope.currentPage + ":\n" + str);
				},
				function (reason) {
					// TODO
					console.log("ERROR: " + reason);
					// TODO
				}, 
				null
		);
		$anchorScroll();
	};
	// initial load
	$scope.reload();



	// TODO
	$scope.user = {
			'username': 'ertorser',
			'fullname': 'Erik Torres',
			'pictureUrl': 'https://www.gravatar.com/avatar/53f455aea79b19eaca5d54a35390bd49?s=80&r=g&d=mm',
			'following': 10,
			'followers': 85
	};
	// TODO

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
		layers: [ new ol.layer.Tile({
			preload: Infinity,
			source: new ol.source.OSM()
		}) ],
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
	$scope.showDownload = false;
	$scope.exportPng = function() {		
		var element = document.getElementById('export-png');
		if ('download' in element) {
			map.once('postcompose', function(event) {
				var canvas = event.context.canvas;
				element.href = canvas.toDataURL('image/png');
			});
			map.render();
			$scope.showDownload = true;
		} else {
			// display error message
			$scope.addAlert('Export PNG feature requires a browser that supports the <a class="alert-link" href="http://caniuse.com/#feat=download" target="_blank">link download</a> attribute.');
		}	
	};
	
	// add my location functionallity
	var geolocation = new ol.Geolocation();
	geolocation.bindTo('projection', map.getView());	
	geolocation.on('error', function(error) {
		$scope.addAlert('Location cannot be determined: ' + error.message);
	});		
	geolocation.setTracking(true);	
	$scope.myLocation = function() {		
		var mylocation = geolocation.getPosition();
		if (mylocation) {
			map.getView().getView2D().setCenter(mylocation);
			map.getView().getView2D().setZoom(12);
		}
	};	
}])
.controller('PageNotFoundCtrl', function() {
	// nothing yet
});