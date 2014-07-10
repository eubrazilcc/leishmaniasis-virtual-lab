'use strict';

/* Directives */

angular.module('lvl.directives', [])
.directive('refreshGrid', [ '$timeout', function ($timeout) {
	return function (scope, element, attrs) {
		var divGrid = (attrs.refreshGrid || "dataGrid");
		var trigger = (attrs.refreshGridTrigger || "showNotifications");		
		var grid = element.find("div[id='" + divGrid + "']");		
		scope.$watch(function () {
			return { 'h': element.height(), 'w': element.width() };
		}, function(newVal, oldVal) {
			if (newVal === oldVal) {
				return;
			}
			grid.css('opacity', '0.5');
			// refresh the grid after the DOM has finished rendering, considering the animations
			var timer = $timeout(function() {				
				grid.css('opacity', '1');
			}, 200);
			scope.$on(
					"$destroy",
					function(event) {
						$timeout.cancel(timer);
					}
			);
		}, true);
		scope.$watch(trigger, function(newVal, oldVal) {
			if (newVal === oldVal) {
				return;
			}
			element.css('opacity', '0.5');
			// refresh the grid after the DOM has finished rendering, considering the animations
			var timer = $timeout(function() {				
				element.css('opacity', '1');
			}, 200);
			scope.$on(
					"$destroy",
					function(event) {
						$timeout.cancel(timer);
					}
			);					
		});
	};
}])
.directive('toggleGrid', [ function () { // possible layouts: 3 or 4
	function link(scope, element, attrs) {
		var expr = attrs.toggleGrid;
		var layout = (attrs.toggleLayout || 3);
		var divC = (attrs.toggleGridComplement || "complement");
		var divT = (attrs.toggleGridToggleable || "toggleable");		
		var duration = (attrs.toggleGridDuration || "fast");
		scope.$watch(expr, function(newVal, oldVal) {
			if (newVal === oldVal) {
				return;
			}
			if (!scope.$eval(expr)) {
				// evaluate link-time value of the model
				// nothing to do
			}
			var complement = element.find("div[id='" + divC + "']");
			var toggleable = element.find("div[id='" + divT + "']");
			if (newVal) {				
				// show
				complement.removeClass(layout == 3 ? 'col-md-10' : 'col-md-12');
				complement.addClass(layout == 3 ? 'col-md-7' : 'col-md-9');
				toggleable.addClass('col-md-3');
				toggleable.removeClass('hidden');
				toggleable.stop(true, true).slideToggle(duration);
			} else {				
				// hide
				toggleable.stop(true, true).slideToggle(duration, function() {
					complement.removeClass(layout == 3 ? 'col-md-7' : 'col-md-9');
					complement.addClass(layout == 3 ? 'col-md-10' : 'col-md-12');
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
.directive('goClick', ['$location', function ($location) {	
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
}])
.directive('htmlPopover', ['$compile', '$templateCache', function ($compile, $templateCache) {
	return {
		restrict: "A",
		link: function (scope, element, attrs) {
			var tplId = (attrs.htmlPopoverTemplate || 'shortProfileTemplate.html');
			var placement = (attrs.htmlPopoverPlacement || 'right');
			var trigger = (attrs.htmlPopoverTrigger || 'click');

			var content = $templateCache.get(tplId);
			content = $compile("<div>" + content + "</div>")(scope);

			var options = {
					'content': content,
					'placement': placement,
					'trigger': trigger,
					'html': true,
					'date': scope.date
			};			
			$(element).popover(options);
		}
	};
}])
.directive('resize', ['$window', function ($window) { // <div ng-style="style()" resize></div>
	return function (scope, element, attrs) {		
		var resizeHeightOffset = (attrs.resizeHeightOffset || 124);
		var resizeWidth = (attrs.resizeWidth || '100%');
		var w = angular.element($window);
		scope.$watch(function () {
			return { 'h': w.height(), 'w': w.width() };
		}, function (newValue, oldValue) {
			scope.windowHeight = newValue.h;
			scope.windowWidth = newValue.w;
			scope.style = function () {
				return { 
					// 'width': (newValue.w - 30) + 'px'
					'height': (newValue.h - resizeHeightOffset) + 'px',
					'width': resizeWidth,
					// 'border': 'solid red 1px',
					'overflow': 'auto'
				};
			};
		}, true);
		w.bind('resize', function () {
			scope.$apply();
		});
	}
}]);