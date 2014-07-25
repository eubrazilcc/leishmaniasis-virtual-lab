/**
 * RequireJS module that defines the view: collection->map.
 */

define([ 'app', 'tpl!apps/collection/map/templates/collection_map', 'apps/config/marionette/styles/style', 'flatui-checkbox', 'flatui-radio' ], function(Lvl,
        MapTpl, Style) {
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
                    new Style().loadCss(Lvl.request('styles:openlayers:entities').toJSON());
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
                        /*
                         * fastest renderer, other renderers: 'webgl',
                         * ol.RendererHints.createFromQueryData()
                         */
                        renderer : 'canvas',
                        /* div HTML element with id='map-container' */
                        target : 'map-container',
                        view : new ol.View({
                            center : this.map_center,
                            zoom : this.map_zoom
                        }),
                        ol3Logo : false
                    });
                    // add custom control to return map home
                    var MapHomeControl = function(opt_options) {
                        var options = opt_options || {};

                        var button = document.createElement('button');
                        button.className = 'ol-has-tooltip';
                        button.setAttribute('type', 'button');
                        button.setAttribute('id', 'map-home-button');
                        button.innerHTML = '<span role="tooltip">Return home</span><i class="fa fa-home"></i>';

                        var this_ = this;
                        var handleMapHome = function(e) {
                            e.preventDefault();
                            $.when(this_.getMap().getView().setCenter(options.center)).done(function() {
                                this_.getMap().getView().setZoom(options.zoom);
                            });                            
                            $('#map-home-button').blur();
                        }

                        button.addEventListener('click', handleMapHome, false);
                        button.addEventListener('touchstart', handleMapHome, false);

                        var element = document.createElement('div');
                        element.className = 'lvl-map-home ol-unselectable ol-control';                        
                        element.appendChild(button);

                        ol.control.Control.call(this, {
                            element : element,
                            target : options.target
                        });
                    };
                    ol.inherits(MapHomeControl, ol.control.Control);
                    this.map.addControl(new MapHomeControl({
                        center : this.map_center,
                        zoom : this.map_zoom
                    }));
                });
                this.resize();
            },
            onClose : function() {
                $(window).off('resize', this.resize);
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