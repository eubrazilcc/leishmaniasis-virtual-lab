/**
 * RequireJS module that defines the entity model for configuration properties.
 */

define([ 'marionette', 'underscore', 'jquery', 'text!data/config.json' ], function(Marionette, _, $, ConfigJson) {
	'use strict';
	var Property = Backbone.Model.extend({
		defaults : {
			property : '',
			values : []
		},
		validate : function(attrs, options) {
			var errors = {};
			if (!attrs.property) {
				errors.property = 'can\'t be empty';
			} else {
				if (attrs.property.length < 2) {
					errors.property = 'is too short';
				}
			}
			if (!attrs.values) {
				errors.values = 'can\'t be empty';
			} else {
				if (!$.isArray(attrs.values) || attrs.values.length == 0) {
					errors.values = 'property values are empty';
				}
			}
			if (!_.isEmpty(errors)) {
				return errors;
			}
		}
	});
	var PropsCol = Backbone.Collection.extend({
		model : Property,
		getProperty : function(property, name, _def) {
			var p = _(this.models).find(function(item) {
				return item.get('property') === property;
			});
			var res = null;
			if (p && $.isArray(p.get('values'))) {
				res = _.find(p.get('values'), function(item) {
					return item.name === name
				});
			}
			return res ? res.value : _def;
		}
	});
	var configObj;
	try {
		configObj = JSON.parse(ConfigJson);
	} catch (err) {
		alert('Failed to load configuration: ' + err);
		throw err;
	}
	return {
		Properties : new PropsCol(configObj),
	};
});