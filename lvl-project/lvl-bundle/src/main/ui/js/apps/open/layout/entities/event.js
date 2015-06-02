/**
 * RequireJS module that defines the entity: open->layout->event.
 */

define([ 'app' ], function(Lvl) {
	Lvl.module('Entities.OpenContent.Event', function(Entities, Lvl, Backbone, Marionette, $, _) {		
		'use strict';
		
		Entities.Event = Backbone.Model.extend({
			urlRoot : '/js/data/events.json?bust=' + Lvl.config.get('global.sessid'),
			defaults : {
				event : 'Unknown',
				link : '',
				where : '',
				when : ''
			},
			validate : function(attrs, options) {
				var errors = {};
				if (!attrs.event) {
					errors.event = 'can\'t be empty';
				} else {
					if (attrs.event.length < 2) {
						errors.event = 'is too short';
					}
				}
				if (!attrs.link) {
					errors.link = 'can\'t be empty';
				}
				if (!attrs.where) {
					errors.where = 'can\'t be empty';
				}
				if (!attrs.when) {
					errors.when = 'can\'t be empty';
				}
				if (!_.isEmpty(errors)) {
					return errors;
				}
			}
		});

		Entities.EventCollection = Backbone.Collection.extend({
			url : '/js/data/events.json?bust=' + Lvl.config.get('global.sessid'),
			model : Entities.Event
		});
	});
	return Lvl.Entities.OpenContent.Event;
});