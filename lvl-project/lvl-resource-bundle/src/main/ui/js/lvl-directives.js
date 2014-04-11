'use strict';

/* Directives */

angular.module('lvl.directives', [])
.directive('toggleGrid', [ function () {
	function link($scope, element, attributes) {
		var expr = attributes.toggleGrid;		
		var divC = (attributes.toggleGridComplement || "complement");
		var divT = (attributes.toggleGridToggleable || "toggleable");		
		var duration = (attributes.toggleGridDuration || "fast");
		$scope.$watch(expr, function(newVal, oldVal) {
			if (newVal === oldVal) {
				return;
			}
			if (!$scope.$eval(expr)) {
				// evaluate link-time value of the model
				// nothing to do
			}
			var complement = element.find("div[id='" + divC + "']");
			var toggleable = element.find("div[id='" + divT + "']");
			if (newVal) {				
				// show
				complement.removeClass('col-md-10');
				complement.addClass('col-md-7');				
				toggleable.addClass('col-md-3');
				toggleable.removeClass('hidden');
				toggleable.stop(true, true).slideToggle(duration);
			} else {				
				// hide
				toggleable.stop(true, true).slideToggle(duration, function() {
					complement.removeClass('col-md-7');
					complement.addClass('col-md-10');
					toggleable.removeClass('col-md-3');
					toggleable.addClass('hidden');
				});
			}
		});
	}
	return({
		link: link,
		restrict: "A"
	});
}])
.directive('goClick', function ($location) {
	return function (scope, element, attrs) {
		var path;
		attrs.$observe('goClick', function (val) {
			path = val;
		});
		element.bind('click', function () {
			scope.$apply(function () {
				$location.path(path);
			});
		});
	};
})
.directive('resize', ['$window', function ($window) { // <div ng-style="style()" resize></div>
	return function (scope, element) {
		var w = angular.element($window);
		scope.$watch(function () {
			return { 'h': w.height(), 'w': w.width() };
		}, function (newValue, oldValue) {
			scope.windowHeight = newValue.h;
			scope.windowWidth = newValue.w;
			scope.style = function () {
				return { 
					'height': (newValue.h - 124) + 'px',
					// 'width': '100%'
					'width': (newValue.w - 30) + 'px'
				};
			};
		}, true);	
		w.bind('resize', function () {
			scope.$apply();
		});
	}
}]);