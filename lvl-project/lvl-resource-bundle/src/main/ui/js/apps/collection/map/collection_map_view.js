/**
 * RequireJS module that defines the view: collection->map.
 */

define([ 'app', 'tpl!apps/collection/map/templates/collection_map', 'apps/config/marionette/styles/style', 'apps/config/marionette/configuration',
        'flatui-checkbox', 'flatui-radio', 'jquery.toolbar' ], function(Lvl, MapTpl, Style, Configuration) {
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
                $('#map-export').off('click');
                $('#map-share').off('click');
            },
            loadMap : function() {
                require([ 'openlayers' ], function() {
                    // setup map
                    this.map = new ol.Map({
                        controls : ol.control.defaults({
                            attribution : false
                        }).extend([ new ol.control.FullScreen(), new ol.control.ScaleLine({
                            units : 'metric'
                        }) ]),
                        layers : [ new ol.layer.Tile({
                            preload : Infinity,
                            source : new ol.source.OSM()
                        }) ],
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

                        // add event to export map as a PNG file
                        $('#map-export').on('click', function(e) {
                            e.preventDefault();
                            // TODO
                            $('#dummy').trigger('click');
                        });

                        // add event to share map (center, zoom)
                        $('#map-share').on('click', function(e) {
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