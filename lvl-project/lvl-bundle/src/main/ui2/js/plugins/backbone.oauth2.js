// Backbone.OAuth2
// Copyright 2014-2015 EUBrazilCC (EU‐Brazil Cloud Connect)
// Licensed under the EUPL license
// Author: Erik Torres <ertorser@upv.es>
// For HTTP basic authentication with login and password see: https://github.com/fiznool/backbone.basicauth
// For explanation of error wrapper see: https://coderwall.com/p/b047sw/handling-non-200-status-codes-in-backbone

(function(root, factory) {
	if (typeof exports === 'object') {
		var underscore = require('underscore');
		var backbone = require('backbone');
		module.exports = factory(underscore, backbone);
	} else if (typeof define === 'function' && define.amd) {
		define([ 'underscore', 'backbone' ], factory);
	}
}(this, function(_, Backbone) {
	'option strict';

	Backbone.OAuth2 = {
		getHeader : function(token) {
			return {
				'Authorization' : 'Bearer ' + token
			};
		}
	};

	var backboneSync = Backbone.sync;

	// handle non-200 status codes, which are incorrectly considered errors in Backbone
	wrapError = function(method, success, error) {
		var successCodes = {
			create : 201
		};
		if (!successCodes[method]) {
			return error;
		}
		return function(jqXHR, textStatus, errorThrown) {
			var wasSuccessful = jqXHR.status === successCodes[method], response;
			if (wasSuccessful && _.isFunction(success)) {
				response = jqXHR.responseJSON ? jqXHR.responseJSON : {};
				success(response, textStatus, jqXHR);
			} else if (_.isFunction(error)) {
				error(jqXHR, textStatus, errorThrown);
			}
		};
	};

	Backbone.sync = function(method, model, options) {
		options = options || {};
		options.error = wrapError(method, options.success, options.error);
		var token;
		if (model.oauth2_token) {
			token = _.result(model, 'oauth2_token');
		}
		if (token != null) {
			options.headers = options.headers || {};
			_.extend(options.headers, Backbone.OAuth2.getHeader(token));
		}
		return backboneSync.call(model, method, model, options);
	};
}));