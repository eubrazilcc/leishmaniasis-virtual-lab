/**
 * RequireJS module that defines the entity: entity: open->layout->section.
 */

define([ 'app', 'backbone.picky' ], function(Lvl) {
	Lvl.module('Entities.OpenContent.Section', function(Entities, Lvl, Backbone, Marionette, $, _) {
		Entities.Section = Backbone.Model.extend({
			defaults : {
				section : 'unknown',
				name : 'Unknown',
				htmlContent : ''
			},
			initialize : function() {
				var selectable = new Backbone.Picky.Selectable(this);
				_.extend(this, selectable);
			},
			validate : function(attrs, options) {
				var errors = {};				
				if (!attrs.section) {
					errors.section = 'can\'t be empty';
				} else {
					if (attrs.section.length < 2) {
						errors.section = 'is too short';
					}
				}
				if (!attrs.name) {
					errors.name = 'can\'t be empty';
				} else {
					if (attrs.name.length < 2) {
						errors.name = 'is too short';
					}
				}
				if (!attrs.htmlContent) {
					errors.htmlContent = 'can\'t be empty';
				}
				if (!_.isEmpty(errors)) {
					return errors;
				}
			}
		});

		Entities.SectionCollection = Backbone.Collection.extend({
			model : Entities.Section,
			comparator : 'id',
			initialize : function() {
				var singleSelect = new Backbone.Picky.SingleSelect(this);
				_.extend(this, singleSelect);
			}
		});

	});
	return Lvl.Entities.OpenContent.Section;
});