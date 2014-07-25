/**
 * RequireJS module that defines the view: collection->map.
 */

define(
        [ 'app', 'tpl!apps/collection/map/templates/collection_map', 'apps/config/marionette/styles/style', 'flatui-checkbox', 'flatui-radio', 'jquery.toolbar' ],
        function(Lvl, MapTpl, Style) {
            Lvl.module('CollectionApp.Map.View', function(View, Lvl, Backbone, Marionette, $, _) {
                View.Content = Marionette.ItemView.extend({
                    id : 'map',
                    template : MapTpl,
                    initialize : function() {
                        $(window).on('resize', this.resize);
                        require([ 'openlayers' ], function() {
                            // map defaults
                            this.map_center = ol.proj.transform([ 3.7036, 40.4169 ], 'EPSG:4326', 'EPSG:3857');
                            this.map_zoom = 5.5;
                        });
                    },
                    onBeforeRender : function() {
                        require([ 'entities/styles' ], function() {
                            var styleLoader = new Style();
                            styleLoader.loadCss(Lvl.request('styles:openlayers:entities').toJSON());
                            styleLoader.loadCss(Lvl.request('styles:jquery.toolbar:entities').toJSON());
                        });
                    },
                    onRender : function() {
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
                                    center : this.map_center,
                                    zoom : this.map_zoom
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
                                    $.when(this_.getMap().getView().setCenter(options.center)).done(function() {
                                        this_.getMap().getView().setZoom(options.zoom);
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
                            this.map.addControl(new MapToolbar({
                                center : this.map_center,
                                zoom : this.map_zoom
                            }));
                            $('#map-menu-button').toolbar({
                                content : '#map-menu-options',
                                position : 'right',
                                hideOnClick : true
                            });
                        });
                        // resize after render
                        this.resize();
                        // detect full-screen change mode and hide menu
                        $(document).on('webkitfullscreenchange mozfullscreenchange fullscreenchange', function() {
                            if (document.webkitCurrentFullScreenElement || document.mozFullScreenElement || document.fullscreenElement) {
                                $('#map-menu-button').hide();
                            } else {
                                $('#map-menu-button').show();
                            }
                        });
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