/**
 * RequireJS module that defines the entity: open->layout->layout.
 */

define([ 'app' ], function(Lvl) {
	Lvl.module('Entities.OpenContent.Layout', function(Entities, Lvl, Backbone, Marionette, $, _) {
		Entities.Layout = Backbone.Model.extend({
			defaults : {
				name : 'Unknown',
				application : 'unknown',
				section : 'unknown',
				agenda : ''
			},
			validate : function(attrs, options) {
				var errors = {};
				if (!attrs.application) {
					errors.application = 'can\'t be empty';
				} else {
					if (attrs.application.length < 2) {
						errors.application = 'is too short';
					}
				}
				if (!attrs.section) {
					errors.section = 'can\'t be empty';
				}	 else {
					if (attrs.section.length < 2) {
						errors.section = 'is too short';
					}
				}			
				if (!_.isEmpty(errors)) {
					return errors;
				}
			}
		});
	});
	return Lvl.Entities.OpenContent.Layout;
});