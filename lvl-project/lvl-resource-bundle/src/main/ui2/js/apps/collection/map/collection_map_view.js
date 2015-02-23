/**
 * RequireJS module that defines the view: collection->map.
 */

define([ 'app', 'tpl!apps/collection/map/templates/collection_map', 'apps/config/marionette/styles/style', 'apps/config/marionette/configuration' ], function(
		Lvl, MapTpl, Style, Configuration) {
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
				});
			},
			onRender : function() {
				var self = this;			
				// find user location, load the map and resize it to fit the screen area
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
			onDestroy : function() {
				// unsubscribe from events
				$(window).off('resize', this.resize);
				// TODO : more events? (see commented code below)
			},
			loadMap : function() {
				require([ 'openlayers' ], function(ol) {
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
							url : config.get('service', '') + '/sequences/sandflies/nearby/0.0/0.0?maxDistance=6500000.0&group=true&heatmap=false&'
									+ config.authorizationQuery()
						}),
						style : function(feature, resolution) {
							var text = resolution < 5000 ? feature.get('count') : '';
							if (!styleCache[text]) {
								styleCache[text] = createFeatureStyle(feature.getGeometry().getType(), text, resolution);
							}
							return styleCache[text];
						},
						visible : false
					});

					var heatmapLayer = new ol.layer.Heatmap({
						source : new ol.source.GeoJSON({
							extractStyles : false,
							projection : 'EPSG:3857',
							url : config.get('service', '') + '/sequences/sandflies/nearby/0.0/0.0?maxDistance=6500000.0&group=true&heatmap=true&'
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
						source : new ol.source.OSM(),
						visible : false
					});

					var tonerRaster = new ol.layer.Tile({
						preload : Infinity,
						source : new ol.source.Stamen({
							layer : 'toner'
						})
					});					

					// custom control to restore initial map view
					var RestoreMapControl = function(opt_options) {
						var options = opt_options || {};

						var button = document.createElement('button');
						button.innerHTML = '<i class="fa fa-home"></i>';
						button.title = "Restore";

						var this_ = this;
						var handleRestore = function(e) {
							var map_center = ol.proj.transform([ center.lon, center.lat ], 'EPSG:4326', 'EPSG:3857');
							$.when(this_.getMap().getView().setCenter(map_center)).done(function() {
								this_.getMap().getView().setZoom(zoom);
							});
						}

						button.addEventListener('click', handleRestore, false);
						button.addEventListener('touchstart', handleRestore, false);

						var element = document.createElement('div');
						element.className = 'lvl-restore-map ol-unselectable ol-control';
						element.appendChild(button);

						ol.control.Control.call(this, {
							element : element,
							target : options.target
						});
					};
					ol.inherits(RestoreMapControl, ol.control.Control);

					// setup map
					this.map = new ol.Map({
						controls : ol.control.defaults({
							attribution : false
						}).extend([ new ol.control.ScaleLine({
							units : 'metric'
						}), new ol.control.OverviewMap({
							layers : [ new ol.layer.Tile({
								source : new ol.source.OSM({
									'url' : '//{a-c}.tile.opencyclemap.org/cycle/{z}/{x}/{y}.png'
								})
							}) ],
							collapsed : true
						}), new RestoreMapControl() ]),
						interactions : ol.interaction.defaults().extend([ new ol.interaction.DragRotateAndZoom() ]),
						layers : [ new ol.layer.Group({
							layers : [ osmRaster, vectorLayer, tonerRaster, heatmapLayer ]
						}) ],
						// fastest renderer
						renderer : 'canvas',
						// div HTML element with id='map-container'
						target : 'map-container',
						view : new ol.View({
							'center' : ol.proj.transform([ center.lon, center.lat ], 'EPSG:4326', 'EPSG:3857'),
							'zoom' : zoom
						}),
						ol3Logo : false
					});
					new ol.dom.Input($('#opts_map_types_heatmap')[0]).bindTo('checked', tonerRaster, 'visible');
					new ol.dom.Input($('#opts_map_types_heatmap')[0]).bindTo('checked', heatmapLayer, 'visible');
					new ol.dom.Input($('#opts_map_types_vectormap')[0]).bindTo('checked', osmRaster, 'visible');
					new ol.dom.Input($('#opts_map_types_vectormap')[0]).bindTo('checked', vectorLayer, 'visible');
					
					// add popup
					var createSeqLinks = function(name) {
						var text = '';
						var seqs = name.split(',');
						for (i = 0; i < seqs.length; i++) {
							text += '<a href="#" data-seq_id="' + seqs[i] + '">' + seqs[i] + '</a> ';
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
					var offset = $('#section-tab-content').offset().top + $('#map-toolbar').height();
					$('#map-container').height(windowHeight - offset);
					this.map.updateSize();
				});
			}
		});
	});
	return Lvl.CollectionApp.Map.View;
});