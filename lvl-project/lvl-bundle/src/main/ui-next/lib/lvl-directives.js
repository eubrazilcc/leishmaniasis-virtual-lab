/* Directives */

angular.module('lvl.directives', [])
.directive('olMap', function() {
	'use strict';
	return {
		restrict: 'A',
		scope: {
			latitude : '=lat',
			longitude : '=lon',
			zoom : '=zoom'
		},
		link: function(scope, element, attrs) {
			var map = new ol.Map({
				layers: [
						new ol.layer.Tile({
							source: new ol.source.OSM()
						})
					],
					renderer: ol.RendererHints.createFromQueryData(),
					target: 'map',
					view: new ol.View2D({
						/* center: ol.proj.transform([3.7036, 40.4169], 'EPSG:4326', 'EPSG:3857'), */
						center: ol.proj.transform([scope.longitude, scope.latitude], 'EPSG:4326', 'EPSG:3857'),
						zoom: scope.zoom
					}),
					ol3Logo: false
			});
		}
	};
});
