// Backbone.OAuth2
// Copyright 2014 EUBrazilCC (EU‐Brazil Cloud Connect)
// Licensed under the EUPL license
// Author: Erik Torres <ertorser@upv.es>
// For HTTP basic authentication with login and password see: https://github.com/fiznool/backbone.basicauth

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

    Backbone.sync = function(method, model, options) {
        options = options || {};
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