/**
 * RequireJS module that defines the view: maps->show.
 */

define([ 'app', 'tpl!apps/maps/show/tpls/maps-show', 'backbone.oauth2' ], function(Lvl, MapShowTpl) {
	Lvl.module('MapsApp.Show.View', function(View, Lvl, Backbone, Marionette, $, _) {
		var center = {
				lon : 3.7036,
				lat : 40.4169
		};
		var zoom = 5.5;
		View.Content = Marionette.ItemView.extend({
			id : 'map',
			template : MapShowTpl,
			initialize : function() {
				$(window).on('resize', this.resize);
				// setup search
				Lvl.vent.on('search:form:submitted', this.searchUnavailable);
			},
			events : {
				'click a[data-item_id]' : 'showItemRecord'
			},
			showItemRecord : function(e) {
				e.preventDefault();
				var self = this;
				var target = $(e.target);
				var itemId = target.is('i') ? target.parent('a').get(0).getAttribute('data-item_id') : target.attr('data-item_id');
				this.trigger('sequences:view:sequence', itemId);				
				$('#map-popup').popover('destroy');				
			},
			searchUnavailable : function(search) {				
				require([ 'common/growl' ], function(createGrowl) {
					createGrowl('Operation unavailable', 'Search tool is not available in this section.', false);
				});
			},
			onRender : function() {
				var self = this;
				// find user location, load the map and resize it to fit the screen area
				var userLocation = Lvl.config.session.get('user.location');
				if (userLocation) {
					center = userLocation.center;
					zoom = userLocation.zoom;
					self.loadMap();
					self.resize();
				} else {
					Lvl.config.getUserLocation(function(location) {
						if (location && location.longitude && location.latitude && (location.longitude != 0 || location.latitude != 0)) {
							center = {
								lon : location.longitude,
								lat : location.latitude
							};
							zoom = 7;
							Lvl.config.session.set('user.location', {
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
				Lvl.vent.off('search:form:submitted');
				this.stopListening();
				$('#opts_map_types_heatmap')[0].removeEventListener();
				$('#opts_map_types_vectormap')[0].removeEventListener();
				$('#opts_layer_sandfly_seqs')[0].removeEventListener();
				$('#opts_layer_leishmania_seqs')[0].removeEventListener();
				$('#opts_layer_sandfly_samples')[0].removeEventListener();
				$('#opts_layer_leishmania_samples')[0].removeEventListener();
			},
			loadMap : function() {
				require([ 'openlayers' ], function(ol) {
					
					var gradient = [ '#00f', '#0ff', '#0f0', '#ff0', '#f00' ]; // [ '#862197', '#0d87e9', '#439a46', '#e08600', '#e51c23' ]
					
					var textColor = function(color) {
						var code;
						switch (color) {
						case 'ocean':
							code = '#1a237e';
							break;
						case 'green':
							code = '#29512c';
							break;
						case 'yellow':
							code = '#ff6f00';
							break;
						case 'red':
						default:
							code = '#C0392B';
							break;
						}
						return code;
					}
					
					var fillColor = function(color) {
						var code;
						switch (color) {
						case 'ocean':
							code = 'rgba(26, 35, 126, 0.5)';
							break;
						case 'green':
							code = 'rgba(41, 81, 44, 0.5)';
							break;
						case 'yellow':
							code = 'rgba(255, 111, 0, 0.5)';
							break;
						case 'red':
						default:
							code = 'rgba(192, 57, 43, 0.5)';
							break;
						}
						return code;
					}
					
					var strokeColor = function(color) {
						var code;
						switch (color) {
						case 'ocean':
							code = '#2a37cb';
							break;
						case 'green':
							code = '#468b4b';
							break;
						case 'yellow':
							code = '#ffab00';
							break;
						case 'red':
						default:
							code = '#E74C3C';
							break;
						}
						return code;
					}
					
					var createTextStyle = function(text, color) {
						return new ol.style.Text({
							font : '12px Lato, Helvetica, Arial, sans-serif',
							text : text,
							fill : new ol.style.Fill({
								color : textColor(color)
							}),
							stroke : new ol.style.Stroke({
								color : '#fff',
								width : 3
							})
						});
					};

					var createFeatureStyle = function(type, text, resolution, color) {
						var style;
						if (type === 'Point') {
							style = [ new ol.style.Style({
								image : new ol.style.Circle({
									fill : new ol.style.Fill({
										color : fillColor(color)
									}),
									radius : resolution < 5000 ? 7 : 5,
									stroke : new ol.style.Stroke({
										color : strokeColor(color),
										width : 1
									})
								}),
								text : createTextStyle(text, color)
							}) ];
						}
						return style;
					}

					var styleCache = {};
					
					// layers for the heat map

					var sandfliesSeqHeatmap = new ol.layer.Heatmap({
						gradient : gradient,
						source : new ol.source.Vector({
							url : Lvl.config.get('service.url') + '/sequences/sandflies/nearby/0.0/0.0?maxDistance=6500000.0&group=true&heatmap=true&' + Lvl.config.authorizationQuery(),
							format: new ol.format.GeoJSON({ featureProjection : 'EPSG:3857'})
						}),						
						radius : 5
					});

					sandfliesSeqHeatmap.getSource().on('addfeature', function(event) {
						var count = event.feature.get('count');
						var magnitude = parseFloat(count);
						event.feature.set('weight', magnitude);
					});
					
					var leishmaniaSeqHeatmap = new ol.layer.Heatmap({
						gradient : gradient,
						source : new ol.source.Vector({
							url : Lvl.config.get('service.url') + '/sequences/leishmania/nearby/0.0/0.0?maxDistance=6500000.0&group=true&heatmap=true&' + Lvl.config.authorizationQuery(),
							format: new ol.format.GeoJSON({ featureProjection : 'EPSG:3857'})
						}),						
						radius : 5
					});

					leishmaniaSeqHeatmap.getSource().on('addfeature', function(event) {
						var count = event.feature.get('count');
						var magnitude = parseFloat(count);
						event.feature.set('weight', magnitude);
					});
					
					var sandfliesSamplesHeatmap = new ol.layer.Heatmap({
						gradient : gradient,
						source : new ol.source.Vector({
							url : Lvl.config.get('service.url') + '/samples/sandflies/nearby/0.0/0.0?maxDistance=6500000.0&group=true&heatmap=true&' + Lvl.config.authorizationQuery(),
							format: new ol.format.GeoJSON({ featureProjection : 'EPSG:3857'})
						}),						
						radius : 5
					});

					sandfliesSamplesHeatmap.getSource().on('addfeature', function(event) {
						var count = event.feature.get('count');
						var magnitude = parseFloat(count);
						event.feature.set('weight', magnitude);
					});
					
					var leishmaniaSamplesHeatmap = new ol.layer.Heatmap({
						gradient : gradient,
						source : new ol.source.Vector({
							url : Lvl.config.get('service.url') + '/samples/leishmania/nearby/0.0/0.0?maxDistance=6500000.0&group=true&heatmap=true&' + Lvl.config.authorizationQuery(),
							format: new ol.format.GeoJSON({ featureProjection : 'EPSG:3857'})
						}),						
						radius : 5
					});

					leishmaniaSamplesHeatmap.getSource().on('addfeature', function(event) {
						var count = event.feature.get('count');
						var magnitude = parseFloat(count);
						event.feature.set('weight', magnitude);
					});
					
					// layers for the vector map
									
					var sandfliesSeqVector = new ol.layer.Vector({
						source : new ol.source.Vector({
							url : Lvl.config.get('service.url') + '/sequences/sandflies/nearby/0.0/0.0?maxDistance=6500000.0&group=true&heatmap=false&' + Lvl.config.authorizationQuery(),
							format: new ol.format.GeoJSON({ featureProjection : 'EPSG:3857'})
						}),
						style : function(feature, resolution) {
							var color = 'red';
							var text = resolution < 5000 ? feature.get('count') : '';
							if (!styleCache[text + color]) {
								styleCache[text + color] = createFeatureStyle(feature.getGeometry().getType(), text, resolution, color);
							}
							return styleCache[text + color];
						},
						visible : false
					});
					
					var leishmaniaSeqVector = new ol.layer.Vector({
						source : new ol.source.Vector({
							url : Lvl.config.get('service.url') + '/sequences/leishmania/nearby/0.0/0.0?maxDistance=6500000.0&group=true&heatmap=false&' + Lvl.config.authorizationQuery(),
							format: new ol.format.GeoJSON({ featureProjection : 'EPSG:3857'})
						}),						
						style : function(feature, resolution) {
							var color = 'ocean';
							var text = resolution < 5000 ? feature.get('count') : '';
							if (!styleCache[text + color]) {
								styleCache[text + color] = createFeatureStyle(feature.getGeometry().getType(), text, resolution, color);
							}
							return styleCache[text + color];
						},
						visible : false
					});
					
					var sandfliesSamplesVector = new ol.layer.Vector({
						source : new ol.source.Vector({
							url : Lvl.config.get('service.url') + '/samples/sandflies/nearby/0.0/0.0?maxDistance=6500000.0&group=true&heatmap=false&' + Lvl.config.authorizationQuery(),
							format: new ol.format.GeoJSON({ featureProjection : 'EPSG:3857'})
						}),						
						style : function(feature, resolution) {
							var color = 'green';
							var text = resolution < 5000 ? feature.get('count') : '';
							if (!styleCache[text + color]) {
								styleCache[text + color] = createFeatureStyle(feature.getGeometry().getType(), text, resolution, color);
							}
							return styleCache[text + color];
						},
						visible : false
					});
					
					var leishmaniaSamplesVector = new ol.layer.Vector({
						source : new ol.source.Vector({
							url : Lvl.config.get('service.url') + '/samples/leishmania/nearby/0.0/0.0?maxDistance=6500000.0&group=true&heatmap=false&' + Lvl.config.authorizationQuery(),
							format: new ol.format.GeoJSON({ featureProjection : 'EPSG:3857'})
						}),						
						style : function(feature, resolution) {
							var color = 'yellow';
							var text = resolution < 5000 ? feature.get('count') : '';
							if (!styleCache[text + color]) {
								styleCache[text + color] = createFeatureStyle(feature.getGeometry().getType(), text, resolution, color);
							}
							return styleCache[text + color];
						},
						visible : false
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
							layers : [ osmRaster, tonerRaster, 
							           sandfliesSeqVector, leishmaniaSeqVector, sandfliesSamplesVector, leishmaniaSamplesVector,
							           sandfliesSeqHeatmap, leishmaniaSeqHeatmap, sandfliesSamplesHeatmap, leishmaniaSamplesHeatmap ]							
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
					var radios = [ $('#opts_map_types_heatmap')[0], $('#opts_map_types_vectormap')[0] ];
					var checkboxes = [ $('#opts_layer_sandfly_seqs')[0], $('#opts_layer_leishmania_seqs')[0],
					                   $('#opts_layer_sandfly_samples')[0], $('#opts_layer_leishmania_samples')[0] ];
					radios[0].addEventListener('change', function() {
						var checked = this.checked;
						if (checked !== tonerRaster.getVisible()) {							
							tonerRaster.setVisible(checked);
							sandfliesSeqHeatmap.setVisible(checkboxes[0].checked && checked);
							leishmaniaSeqHeatmap.setVisible(checkboxes[1].checked && checked);
							sandfliesSamplesHeatmap.setVisible(checkboxes[2].checked && checked);
							leishmaniaSamplesHeatmap.setVisible(checkboxes[3].checked && checked);
							osmRaster.setVisible(!checked);
							sandfliesSeqVector.setVisible(checkboxes[0].checked && !checked);
							leishmaniaSeqVector.setVisible(checkboxes[1].checked && !checked);
							sandfliesSamplesVector.setVisible(checkboxes[2].checked && !checked);
							leishmaniaSamplesVector.setVisible(checkboxes[3].checked && !checked);
						}
					});
					tonerRaster.on('change:visible', function() {
						var visible = this.getVisible();
						if (visible !== radios[0].checked) {
							radios[0].checked = visible;
							radios[1].checked = !visible;
						}
					});
					radios[1].addEventListener('change', function() {
						var checked = this.checked;
						if (checked !== osmRaster.getVisible()) {							
							osmRaster.setVisible(checked);
							sandfliesSeqVector.setVisible(checkboxes[0].checked && checked);
							leishmaniaSeqVector.setVisible(checkboxes[1].checked && checked);
							sandfliesSamplesVector.setVisible(checkboxes[2].checked && checked);
							leishmaniaSamplesVector.setVisible(checkboxes[3].checked && checked);
							tonerRaster.setVisible(!checked);
							sandfliesSeqHeatmap.setVisible(checkboxes[0].checked && !checked);							
							leishmaniaSeqHeatmap.setVisible(checkboxes[1].checked && !checked);
							sandfliesSamplesHeatmap.setVisible(checkboxes[2].checked && !checked);
							leishmaniaSamplesHeatmap.setVisible(checkboxes[3].checked && !checked);
						}
					});
					osmRaster.on('change:visible', function() {
						var visible = this.getVisible();
						if (visible !== radios[1].checked) {
							radios[0].checked = !visible;
							radios[1].checked = visible;
						}
					});
					checkboxes[0].addEventListener('change', function() {
						var checked = this.checked;
						if (osmRaster.getVisible()) sandfliesSeqVector.setVisible(checked);
						else sandfliesSeqHeatmap.setVisible(checked);
					});
					checkboxes[1].addEventListener('change', function() {
						var checked = this.checked;
						if (osmRaster.getVisible()) leishmaniaSeqVector.setVisible(checked);
						else leishmaniaSeqHeatmap.setVisible(checked);						
					});
					checkboxes[2].addEventListener('change', function() {
						var checked = this.checked;
						if (osmRaster.getVisible()) sandfliesSamplesVector.setVisible(checked);
						else sandfliesSamplesHeatmap.setVisible(checked);
					});
					checkboxes[3].addEventListener('change', function() {
						var checked = this.checked;
						if (osmRaster.getVisible()) leishmaniaSamplesVector.setVisible(checked);
						else leishmaniaSamplesHeatmap.setVisible(checked);
					});
					
					// add popup
					var createItemLinks = function(name) {
						var text = '';
						var seqs = name.split(',');
						for (i = 0; i < seqs.length; i++) {
							text += '<a href="#" data-item_id="' + seqs[i] + '">' + seqs[i] + '</a> ';
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
								'content' : createItemLinks(feature.get('name'))
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
					var offset = $('#map-toolbar').offset().top + $('#map-toolbar').height() + 2;
					$('#map-container').height(windowHeight - offset);
					this.map.updateSize();
				});
			}
		});
	});
	return Lvl.MapsApp.Show.View;
});