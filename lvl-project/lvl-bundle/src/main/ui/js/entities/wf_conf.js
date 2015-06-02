/**
 * RequireJS module that defines the entity: workflow configuration.
 */

define([ 'app' ], function(Lvl) {
	Lvl.module('Entities.WfConf', function(Entities, Lvl, Backbone, Marionette, $, _) {
		'use strict';

		Entities.WfConf = Backbone.Model.extend({
			urlRoot : '/js/data/pipelines.json?bust=' + Lvl.config.get('global.sessid'),
			defaults : {
				id : '',
				stable : '',
				parameters : []
			},
			validate : function(attrs, options) {
				var errors = {};
				if (!attrs.id) {
					errors.id = 'can\'t be empty';
				}
				if (!attrs.stable) {
					errors.stable = 'can\'t be empty';
				}
				if (!_.isEmpty(errors)) {
					return errors;
				}
			}
		});

		Entities.WfConfCollection = Backbone.Collection.extend({
			url : '/js/data/pipelines.json?bust=' + Lvl.config.get('global.sessid'),
			model : Entities.WfConf,
			findPipeline : function(id) {
				return _(this.models).find(function(item) {
					return item.id === id;
				});
			}
		});
	});
	return Lvl.Entities.WfConf;
});