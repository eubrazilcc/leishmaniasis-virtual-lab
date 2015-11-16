/**
 * RequireJS module that defines the entity: identifier.
 */

define([ 'app' ], function(Lvl) {
	Lvl.module('Entities.Identifier', function(Entities, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Entities.Identifier = Backbone.Model.extend({
			urlRoot : Lvl.config.get('service.url') + '/sequences/',
			url : function() {
				return this.urlRoot + this.get('dataSource') + '/project/identifiers?q=' + this.get('queryParam');
			},
			idAttribute : 'hash',
			defaults : {
				'hash' : '',
				'identifiers' : []
			},
			validate : function(attrs, options) {
				var errors = {};
				if (!attrs.hash) {
					errors.hash = 'can\'t be empty';
				}
				if (!_.isEmpty(errors)) {
					return errors;
				}
			}
		});		
	});
	return Lvl.Entities.Identifier;
});