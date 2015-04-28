/**
 * RequireJS module that defines the entity: entity: open->layout->Table of contents (ToC).
 */

define([ 'app', 'backbone.picky' ], function(Lvl) {
	Lvl.module('Entities.OpenContent.Toc', function(Entities, Lvl, Backbone, Marionette, $, _) {
		Entities.Toc = Backbone.Model.extend({
			defaults : {
				application : 'unknown',
				section : 'unknown',
				text : 'Unknown'
			},
			initialize : function() {
				var selectable = new Backbone.Picky.Selectable(this);
				_.extend(this, selectable);
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
				} else {
					if (attrs.section.length < 2) {
						errors.section = 'is too short';
					}
				}
				if (!attrs.text) {
					errors.text = 'can\'t be empty';
				} else {
					if (attrs.text.length < 2) {
						errors.text = 'is too short';
					}
				}
				if (!_.isEmpty(errors)) {
					return errors;
				}
			}
		});

		Entities.TocCollection = Backbone.Collection.extend({
			model : Entities.Toc,
			comparator : 'id',
			initialize : function() {
				var singleSelect = new Backbone.Picky.SingleSelect(this);
				_.extend(this, singleSelect);
			}
		});

	});
	return Lvl.Entities.OpenContent.Toc;
});