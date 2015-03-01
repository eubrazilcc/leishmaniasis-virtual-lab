/**
 * RequireJS module that defines the entity: statistic.
 */

define([ 'app', 'apps/config/marionette/configuration' ], function(Lvl, Configuration) {
	Lvl.module('Entities.Statistic', function(Entities, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		Entities.Statistic = Backbone.Model.extend({
			urlRoot : config.get('service', '') + '/instances/stats/collection',
			defaults : {
				'leishmania.gene' : [ {
					"label" : "",
					"value" : 0
				} ],
				'leishmania.gis' : [ {
					"label" : "No",
					"value" : 0
				} ],
				'leishmania.source' : [ {
					"label" : "GenBank",
					"value" : 0
				} ],
				'sandflies.gene' : [ {
					"label" : "",
					"value" : 0
				} ],
				'sandflies.gis' : [ {
					"label" : "No",
					"value" : 0
				} ],
				'sandflies.source' : [ {
					"label" : "GenBank",
					"value" : 0
				} ]
			},
			validate : function(attrs, options) {
				var errors = {};
				if (!_.isEmpty(errors)) {
					return errors;
				}
			}
		});
	});
	return Lvl.Entities.Statistic;
});