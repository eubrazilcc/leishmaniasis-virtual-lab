/**
 * RequireJS module that defines configuration parameters that are globally
 * available to all the other modules of the application. Note that this module
 * uses the 'requirejs.s.contexts._.config' hack to read values of configuration
 * that could change or disappear in the next versions of RequireJS without
 * warning.
 */

define([ 'marionette', 'underscore', 'jquery' ], function(Marionette, _, $) {
    var bust = requirejs.s.contexts._.config.urlArgs ? '?' + requirejs.s.contexts._.config.urlArgs : '';
    Marionette.Controller.Configuration = Marionette.Controller.extend({
        initialize : function(options) {
            this.endpoint = 'http://lvl.i3m.upv.es';
            this.config = [ {
                id : 'bust',
                value : bust
            }, {
                id : 'endpoint',
                value : this.endpoint
            }, {
                id : 'auth',
                value : this.endpoint + '/lvl-auth/oauth2/v1'
            }, {
                id : 'service',
                value : this.endpoint + '/lvl-service/rest/v1'
            }, {
                id : 'oauth2_app',
                value : {
                    'client_id' : 'lvl_portal',
                    'client_secret' : 'changeit'
                }
            } ];
        },
        get : function(id, _def) {
            var res = _.find(this.config, function(obj) {
                return obj.id === id
            });
            return res ? res.value : _def;
        },
        getUserLocation : function(callback, error) {
            if ('function' === typeof callback) {
                $.ajax('http://freegeoip.net/json/').done(function(location) {
                    callback(location);
                }).fail(function(jqXHR, textStatus) {
                    if ('function' === typeof error) {
                        error(textStatus);
                    } else {
                        console.log('Failed to get user location', textStatus);
                    }
                });
            }
        },
        session : {
            get : function(key) {
                var value = sessionStorage.getItem(key);
                return value ? JSON.parse(value) : value;
            },
            set : function(key, value) {
                if (key && value) {
                    sessionStorage.setItem(key, JSON.stringify(value));
                }
            },
            check : function(key) {
                return key ? sessionStorage.getItem(key) == null : false;
            },
            clear : function(key) {
                if (key && value) {
                    sessionStorage.removeItem(key);
                }
            }
        },
        storage : {
            get : function(key) {
                var value = localStorage.getItem(key);
                return value ? JSON.parse(value) : value;
            },
            set : function(key, value) {
                if (key && value) {
                    localStorage.setItem(key, JSON.stringify(value));
                }
            },
            check : function(key) {
                return key ? localStorage.getItem(key) == null : false;
            },
            clear : function(key) {
                if (key && value) {
                    localStorage.removeItem(key);
                }
            }
        }
    });
    return Marionette.Controller.Configuration;
});