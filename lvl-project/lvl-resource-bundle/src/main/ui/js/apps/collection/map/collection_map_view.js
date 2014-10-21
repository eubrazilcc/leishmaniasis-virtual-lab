/**
 * RequireJS module that defines the view: collection->map.
 */

define([ 'app', 'tpl!apps/collection/map/templates/collection_map', 'apps/config/marionette/styles/style', 'apps/config/marionette/configuration',
		'openlayers', 'jquery.toolbar' ], function(Lvl, MapTpl, Style, Configuration) {
	Lvl.module('CollectionApp.Map.View', function(View, Lvl, Backbone, Marionette, $, _) {
		var config = new Configuration();
		var center = {
			lon : 3.7036,
			lat : 40.4169
		};
		var zoom = 5.5;
		View.Content = Marionette.ItemView.extend({
			id : 'map',
			template : MapTpl,
			initialize : function() {
				$(window).on('resize', this.resize);
			},
			events : {
				'click a[data-seq_id]' : 'showSequenceRecord'
			},
			showSequenceRecord : function(e) {
				e.preventDefault();
				var self = this;
				var target = $(e.target);
				var itemId = target.is('i') ? target.parent('a').get(0).getAttribute('data-seq_id') : target.attr('data-seq_id');				
				this.trigger('sequences:view:sequence', itemId);
			},
			onBeforeRender : function() {
				require([ 'entities/styles' ], function() {
					var styleLoader = new Style();
					styleLoader.loadCss(Lvl.request('styles:openlayers:entities').toJSON());
					styleLoader.loadCss(Lvl.request('styles:jquery.toolbar:entities').toJSON());
				});
			},
			onRender : function() {
				var self = this;
				// detect full-screen change mode and hide menu
				$(document).on('webkitfullscreenchange mozfullscreenchange fullscreenchange', function() {
					if (document.webkitCurrentFullScreenElement || document.mozFullScreenElement || document.fullscreenElement) {
						$('#map-menu-button').hide();
					} else {
						$('#map-menu-button').show();
					}
				});
				// find user location, load the map and resize it to fit the
				// screen area
				var userLocation = config.session.get('user.location');
				if (userLocation) {
					center = userLocation.center;
					zoom = userLocation.zoom;
					self.loadMap();
					self.resize();
				} else {
					config.getUserLocation(function(location) {
						if (location && location.longitude && location.latitude && (location.longitude != 0 || location.latitude != 0)) {
							center = {
								lon : location.longitude,
								lat : location.latitude
							};
							zoom = 7;
							config.session.set('user.location', {
								'center' : center,
								'zoom' : zoom
							});
						}
						self.loadMap();
						self.resize();
					});
				}
			},
			onClose : function() {
				// unsubscribe from events
				$(window).off('resize', this.resize);
				$(document).off('webkitfullscreenchange mozfullscreenchange fullscreenchange');
				$('#map-return-home').off('click');
				$('#map-my-location').off('click');
				$('#map-switch').off('click');
				$('#map-export').off('click');
			},
			loadMap : function() {
				require([ 'openlayers' ], function() {

					var createTextStyle = function(text) {
						return new ol.style.Text({
							font : '12px Lato, Helvetica, Arial, sans-serif',
							text : text,
							fill : new ol.style.Fill({
								color : '#C0392B'
							}),
							stroke : new ol.style.Stroke({
								color : '#fff',
								width : 3
							})
						});
					};

					var createFeatureStyle = function(type, text, resolution) {
						var style;
						if (type === 'Point') {
							style = [ new ol.style.Style({
								image : new ol.style.Circle({
									fill : new ol.style.Fill({
										color : 'rgba(192,57,43,0.5)'
									}),
									radius : resolution < 5000 ? 7 : 5,
									stroke : new ol.style.Stroke({
										color : '#E74C3C',
										width : 1
									})
								}),
								text : createTextStyle(text)
							}) ];
						}
						return style;
					}

					var styleCache = {};

					var vectorLayer = new ol.layer.Vector({
						source : new ol.source.GeoJSON({
							projection : 'EPSG:3857',
							url : config.get('service', '') + '/sandflies/nearby/0.0/0.0?maxDistance=6500000.0&group=true&heatmap=false&'
									+ config.authorizationQuery()
						}),
						style : function(feature, resolution) {
							var text = resolution < 5000 ? feature.get('count') : '';
							if (!styleCache[text]) {
								styleCache[text] = createFeatureStyle(feature.getGeometry().getType(), text, resolution);
							}
							return styleCache[text];
						}
					});

					var heatmapLayer = new ol.layer.Heatmap({
						source : new ol.source.GeoJSON({
							extractStyles : false,
							projection : 'EPSG:3857',
							url : config.get('service', '') + '/sandflies/nearby/0.0/0.0?maxDistance=6500000.0&group=true&heatmap=true&'
									+ config.authorizationQuery()
						}),
						radius : 5
					});

					heatmapLayer.getSource().on('addfeature', function(event) {
						var count = event.feature.get('count');
						var magnitude = parseFloat(count);
						event.feature.set('weight', magnitude);
					});

					var osmRaster = new ol.layer.Tile({
						preload : Infinity,
						source : new ol.source.OSM()
					});

					var tonerRaster = new ol.layer.Tile({
						source : new ol.source.Stamen({
							preload : Infinity,
							layer : 'toner'
						})
					});

					// setup map
					this.map = new ol.Map({
						controls : ol.control.defaults({
							attribution : false
						}).extend([ new ol.control.FullScreen(), new ol.control.ScaleLine({
							units : 'metric'
						}) ]),
						layers : [ tonerRaster, heatmapLayer ],
						// TODO layers : [ osmRaster, vectorLayer ],
						/* fastest renderer */
						renderer : 'canvas',
						/* div HTML element with id='map-container' */
						target : 'map-container',
						view : new ol.View({
							'center' : ol.proj.transform([ center.lon, center.lat ], 'EPSG:4326', 'EPSG:3857'),
							'zoom' : zoom
						}),
						ol3Logo : false
					});
					// add custom control to show map menu
					var MapToolbar = function(opt_options) {
						var options = opt_options || {};

						var button = document.createElement('button');
						button.className = 'ol-has-tooltip';
						button.setAttribute('type', 'button');
						button.setAttribute('id', 'map-menu-button');
						button.innerHTML = '<span role="tooltip">Tools</span><i class="fa fa-cog"></i>';

						var this_ = this;
						var showToolbar = function(e) {
							e.preventDefault();
							$('#map-menu-button').blur();
						}

						button.addEventListener('click', showToolbar, false);
						button.addEventListener('touchstart', showToolbar, false);

						var element = document.createElement('div');
						element.className = 'lvl-map-menu ol-unselectable ol-control';
						element.appendChild(button);

						ol.control.Control.call(this, {
							element : element,
							target : options.target
						});

						var this_ = this;

						// add event to return map home
						$('#map-return-home').on('click', function(e) {
							e.preventDefault();
							var map_center = ol.proj.transform([ center.lon, center.lat ], 'EPSG:4326', 'EPSG:3857');
							$.when(this_.getMap().getView().setCenter(map_center)).done(function() {
								this_.getMap().getView().setZoom(zoom);
							});
							$('#dummy').trigger('click');
						});

						// add event to track user position
						$('#map-my-location').on('click', function(e) {
							e.preventDefault();
							// TODO
							$('#dummy').trigger('click');
						});

						// add event to change the type of map displayed
						$('#map-switch').on('click', function(e) {
							e.preventDefault();

							require([ 'common/views' ], function(CommonViews) {
								var loadingView = new CommonViews.Loading();
								Lvl.fullpageRegion.show(loadingView);
							});

							setTimeout(function() {
								Lvl.fullpageRegion.close();
							}, 3000);

							// TODO
							$('#dummy').trigger('click');
						});

						// add event to export map as a PNG file
						$('#map-export').on('click', function(e) {
							e.preventDefault();
							// TODO
							$('#dummy').trigger('click');
						});
					};
					ol.inherits(MapToolbar, ol.control.Control);
					this.map.addControl(new MapToolbar());
					$('#map-menu-button').toolbar({
						content : '#map-menu-options',
						position : 'right',
						hideOnClick : true
					});
					// add popup
					var createSeqLinks = function(name) {
						var text = '';
						var seqs = name.split(',');
						for (i = 0; i < seqs.length; i++) {
							text += '<a href="#" data-seq_id="' + seqs[i] + '">' + seqs[i] + '</a> '; // TODO
						}
						return text;
					}
					var popupElem = document.getElementById('map-popup');
					var popup = new ol.Overlay({
						element : popupElem,
						positioning : 'bottom-center',
						stopEvent : false
					});
					this.map.addOverlay(popup);
					var this_ = this;
					this.map.on('click', function(evt) {
						$(popupElem).popover('destroy');
						var feature = this_.map.forEachFeatureAtPixel(evt.pixel, function(feature, layer) {
							return feature;
						});
						if (feature) {
							var geometry = feature.getGeometry();
							var coord = geometry.getCoordinates();
							popup.setPosition(coord);
							$(popupElem).popover({
								'container' : '#map-container',
								'placement' : 'top',
								'animation' : false,
								'html' : true,
								'content' : createSeqLinks(feature.get('name'))
							});
							$(popupElem).popover('show');
						}
					});
					$(this.map.getViewport()).on('mousemove', function(e) {
						var pixel = this_.map.getEventPixel(e.originalEvent);
						var hit = this_.map.forEachFeatureAtPixel(pixel, function(feature, layer) {
							return true;
						});
						var mapElem = document.getElementById(this_.map.getTarget());
						if (hit) {
							mapElem.style.cursor = 'pointer';
						} else {
							mapElem.style.cursor = '';
						}
					});
				});
			},
			resize : function() {
				require([ 'openlayers' ], function() {
					var windowHeight = $(window).height();
					var offset = $('#section-tab-content').offset().top + $('#tab-content-top-separator').height();
					$('#map-container').height(windowHeight - offset);
					this.map.updateSize();
				});
			}
		});
	});
	return Lvl.CollectionApp.Map.View;
});