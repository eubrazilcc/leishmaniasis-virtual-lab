'use strict';

/* Controllers */

angular.module('lvl.controllers', [])
.controller('HomeCtrl', ['$scope', '$rootScope', '$window', '$location', 'UserRegistrationFactory', function($scope, $rootScope, $window, $location, UserRegistrationFactory) {
	$scope.alerts = [];
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
.controller('NavBarCtrl', ['$scope', '$rootScope', '$window', 'ENV', 'CookieFactory', 'Oauth2Factory', function($scope, $rootScope, $window, ENV, CookieFactory, Oauth2Factory) {
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
.controller('SettingsCtrl', ['$scope', function($scope) {
	$scope.isUsersOpen = true;
	$scope.isSequencesOpen = false;
	$scope.isPapersOpen = false;
	$scope.isIssuesOpen = false;


	// TODO

}])
.controller('SequencesCtrl', ['$scope', '$sce', '$window', function($scope, $sce, $window) {
	var countryFn = function(countryCode) {
		var country = '';
		if (countryCode && typeof countryCode === 'string') {
			var countryName = cnMap()[countryCode.toUpperCase()];
			if (countryName) {
				country = $sce.trustAsHtml('<img src="img/blank.gif" class="flag flag-' + countryCode.toLowerCase() + '" alt="' + countryName + '" /> ' + countryName);
			}
		}
		return country;
	};

	$scope.showNotifications = ($window.sessionStorage.getItem('showNotifications') === 'true' ? true : false);

	$scope.toggleNotifications = function() {
		$scope.showNotifications = !$scope.showNotifications;
		$window.sessionStorage.setItem('showNotifications', $scope.showNotifications ? 'true' : 'false');
	};

	$scope.selectedSequences = [];

	$scope.seqData = [{accession: 'U49845', version: 1, definition: 'Saccharomyces cerevisiae TCP1-beta gene, partial cds, and Axl2p (AXL2) and Rev7p (REV7) genes, complete cds.', organism: 'Saccharomyces cerevisiae', country: countryFn('es')},
	                  {accession: 'A78394', version: 1, definition: 'Saccharomyces cerevisiae TCP1-beta gene, partial cds, and Axl2p (AXL2) and Rev7p (REV7) genes, complete cds.', organism: 'Saccharomyces cerevisiae', country: countryFn('')},
	                  {accession: 'D34981', version: 1, definition: 'Saccharomyces cerevisiae TCP1-beta gene, partial cds, and Axl2p (AXL2) and Rev7p (REV7) genes, complete cds.', organism: 'Saccharomyces cerevisiae', country: countryFn('in')},
	                  {accession: 'B89091', version: 2, definition: 'Saccharomyces cerevisiae TCP1-beta gene, partial cds, and Axl2p (AXL2) and Rev7p (REV7) genes, complete cds.', organism: 'Saccharomyces cerevisiae', country: countryFn('es')},
	                  {accession: 'E23688', version: 1, definition: 'Saccharomyces cerevisiae TCP1-beta gene, partial cds, and Axl2p (AXL2) and Rev7p (REV7) genes, complete cds.', organism: 'Saccharomyces cerevisiae', country: countryFn('bi')}];

	var hdrCellTpl = '<div class="ngHeaderSortColumn {{col.headerClass}}" ng-style="{\'cursor\': col.cursor}" ng-class="{ \'ngSorted\': !noSortVisible }"><div ng-click="col.sort($event)" ng-class="\'colt\' + col.index" class="ngHeaderText">{{col.displayName}}</div><div class="ngSortButtonDown" ng-show="col.showSortButtonDown()"></div><div class="ngSortButtonUp" ng-show="col.showSortButtonUp()"></div><div class="ngSortPriority">{{col.sortPriority}}</div><div ng-class="{ ngPinnedIcon: col.pinned, ngUnPinnedIcon: !col.pinned }" ng-click="togglePin(col)" ng-show="col.pinnable"></div></div><div ng-show="col.resizable" class="ngHeaderGrip" ng-click="col.gripClick($event)" ng-mousedown="col.gripOnMouseDown($event)"></div>';
	var linkCellTpl = '<div class="ngCellText" ng-class="col.colIndex()"><span ng-cell-text>' 
		+ '<code><a ng-href="#/sequences/{{row.getProperty(col.field)}}">{{row.getProperty(col.field)}}</a></code> '
		+ '<a ng-href="#/sequences/{{row.getProperty(col.field)}}/edit" tooltip-append-to-body="true" tooltip-placement="right" tooltip="edit"><i class="fa fa-pencil-square-o fa-fw"></i></a>'
		+ '<a ng-href="#/sequences/{{row.getProperty(col.field)}}/delete" tooltip-append-to-body="true" tooltip-placement="right" tooltip="delete"><i class="fa fa-trash-o fa-fw"></i></a>'
		+ '</span></div>';
	var definitionCellTpl = '<div class="ngCellText" ng-class="col.colIndex()">' 
		+ '<span ng-cell-text><a href popover-trigger="mouseenter" popover-append-to-body="true" popover-placement="right" popover="{{row.getProperty(col.field)}}"><i class="fa fa-plus-square-o fa-fw"></i></a> {{row.getProperty(col.field)}}</span></div>';
	var countryCellTpl = '<div class="ngCellText" ng-class="col.colIndex()"><span ng-cell-text>'
		+ '<div ng-bind-html="row.getProperty(col.field)"></div>'
		+ '</span></div>';

	$scope.gridLayoutPlugin = new ngGridLayoutPlugin();

	$scope.gridOptions = {
			data: 'seqData',
			selectedItems: $scope.selectedSequences,
			multiSelect: false,
			keepLastSelected: false,
			showSelectionCheckbox: true,
			selectWithCheckboxOnly: true,
			enableColumnResize: false,
			showFooter: true,
			plugins: [$scope.gridLayoutPlugin],
			columnDefs: [{field:'accession', displayName:'Accession', headerCellTemplate:hdrCellTpl, width:'***', cellTemplate:linkCellTpl}, 
			             {field:'version', displayName:'Version', headerCellTemplate:hdrCellTpl, width:'*'},
			             {field:'definition', displayName:'Definition', headerCellTemplate:hdrCellTpl, width:'***', cellTemplate:definitionCellTpl},			             
			             {field:'organism', displayName:'Organism', headerCellTemplate:hdrCellTpl, width:'**'},
			             {field:'country', displayName:'Country', headerCellTemplate:hdrCellTpl, width:'**', cellTemplate:countryCellTpl}]			             
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