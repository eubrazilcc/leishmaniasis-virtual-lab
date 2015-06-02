/**
 * RequireJS module that defines configuration parameters that are globally
 * available to all the other modules of the application. Note that this module
 * uses the 'requirejs.s.contexts._.config' hack to read values of configuration
 * that could change or disappear in the next versions of RequireJS without
 * warning.
 */

define([ 'marionette', 'underscore', 'jquery', 'apps/config/marionette/properties' ], function(Marionette, _, $, PropsEntity) {
	'use strict';
	var bust = requirejs.s.contexts._.config.urlArgs ? '?' + requirejs.s.contexts._.config.urlArgs : '';
	Marionette.Object.Configuration = Marionette.Object.extend({
		initialize : function() {
			this.props = new PropsEntity.PropsCol();
		},
		loadProperties : function() {
			var _self = this;
			return _self.props.fetch({
				reset : true
			}).done(function() {
				_self.props.add([ {
					section : 'global',
					properties : [ {
						name : 'bust',
						value : bust
					}, {
						name : 'sessid',
						value : PropsEntity.sessionId
					} ]
				}, {
					section : 'auth',
					properties : [ {
						name : 'url',
						value : _self.props.getProperty('endpoint', 'url') + '/lvl-auth/oauth2/v' + _self.props.getProperty('endpoint', 'api_version')
					} ]
				}, {
					section : 'service',
					properties : [ {
						name : 'url',
						value : _self.props.getProperty('endpoint', 'url') + '/lvl-service/rest/v' + _self.props.getProperty('endpoint', 'api_version')
					} ]
				} ], {
					merge : true
				});
			});
		},
		get : function(id) {
			var res = id.split('.', 2);
			return this.props.getProperty(res[0], res[1]);
		},
		getUserLocation : function(callback, error) {
			if ('function' === typeof callback) {
				$.ajax('https://freegeoip.net/json/').done(function(location) {
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
		saveSession : function(email, token, provider, permanent) {
			var provider2 = provider || 'lvl';
			var session = {
				'email' : email,
				'token' : token,
				'provider' : provider2
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
		},
		redirectUri : function() {
			return this.get('auth') + '/linkedin/callback';
		},
		linkedInAuthEndpoint : function(state) {
			return 'https://www.linkedin.com/uas/oauth2/authorization?response_type=code&client_id=' + this.props.getProperty('linkedin', 'api_key')
					+ '&redirect_uri=' + encodeURIComponent(this.redirectUri()) + '&state=' + state + '&scope=r_basicprofile%20r_emailaddress';
		}
	});
	return Marionette.Object.Configuration;
});