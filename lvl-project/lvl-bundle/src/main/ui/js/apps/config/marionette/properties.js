/**
 * RequireJS module that defines the entity model for configuration properties.
 */

define([ 'marionette', 'underscore', 'jquery' ], function(Marionette, _, $) {
	'use strict';
	var sessionId = Math.random();
	var Property = Backbone.Model.extend({
		urlRoot : '/js/data/config.json?bust=' + sessionId,
		defaults : {
			section : '',
			properties : []
		},
		idAttribute : 'section',
		validate : function(attrs, options) {
			var errors = {};
			if (!attrs.section) {
				errors.section = 'can\'t be empty';
			} else {
				if (attrs.section.length < 2) {
					errors.section = 'is too short';
				}
			}
			if (!attrs.properties) {
				errors.properties = 'can\'t be empty';
			} else {
				if (!$.isArray(attrs.properties)) {
					errors.properties = 'invalid properties';
				}
			}
			if (!_.isEmpty(errors)) {
				return errors;
			}
		}
	});
	var PropsCol = Backbone.Collection.extend({
		url : '/js/data/config.json?bust=' + sessionId,
		model : Property,
		getProperty : function(section, property) {
			var p = _(this.models).find(function(item) {
				return item.get('section') === section;
			});
			var res = null;
			if (p && $.isArray(p.get('properties'))) {
				res = _.find(p.get('properties'), function(item) {
					return item.name === property
				});
			}
			return res ? res.value : '';
		}
	});
	return {
		'sessionId' : sessionId,
		'PropsCol' : PropsCol
	};
});