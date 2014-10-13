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
				if (key) {
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
				if (key) {
					localStorage.removeItem(key);
				}
			}
		},
		loadPreviousSession : function() {
			var session = this.storage.get('user.session');
			if (session) {
				this.session.set('user.session', session);
			}
			return session;
		},
		saveSession : function(email, token, permanent) {
			var session = {
					'email' : email,
					'token' : token
			};
			this.session.set('user.session', session);
			if (permanent === true) {
				this.storage.set('user.session', session);
			}
		},
		deleteSession : function() {
			this.storage.clear('user.session');
			this.session.clear('user.session');
		},
		isAuthenticated : function() {
			var session = this.session.get('user.session') || this.loadPreviousSession();
			return session !== undefined && session !== null;
		},
		authorizationToken : function() {
			var session = this.session.get('user.session');
			return (session !== undefined && session !== null && session.token !== undefined ? session.token : null);
		},
		authorizationHeader : function() {
			var token = this.authorizationToken();
			return (token !== null ? {
				'Authorization' : 'Bearer ' + token
			} : null);
		},
		authorizationQuery : function() {
			var token = this.authorizationToken();
			return (token !== null ? 'access_token=' + encodeURIComponent(token) : null);
		}
	});
	return Marionette.Controller.Configuration;
});