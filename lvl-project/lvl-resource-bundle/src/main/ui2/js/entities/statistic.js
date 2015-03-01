/**
 * RequireJS module that defines the entity: statistic.
 */

define([ 'app', 'apps/config/marionette/configuration', 'backbone.picky', 'backbone.paginator' ], function(Lvl, Configuration) {
	Lvl.module('Entities.Statistic', function(Entities, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		Entities.Statistic = Backbone.Model.extend({
			defaults : {
				label : '',
				data : ''
			},
			initialize : function() {
				var selectable = new Backbone.Picky.Selectable(this);
				_.extend(this, selectable);
			},
			validate : function(attrs, options) {
				var errors = {};
				if (!attrs.label) {
					errors.label = 'can\'t be empty';
				}
				if (!attrs.data) {
					errors.data = 'can\'t be empty';
				}
				if (!_.isEmpty(errors)) {
					return errors;
				}
			}
		});
		Entities.StatisticCollection = Backbone.Collection.extend({
			model : Entities.Statistic,
			comparator : 'id',
			initialize : function() {
				var singleSelect = new Backbone.Picky.SingleSelect(this);
				_.extend(this, singleSelect);
			}
		});
		Entities.StatisticPageableCollection = Backbone.PageableCollection.extend({
			model : Entities.Statistic,
			mode : 'server',
			// url : 'stats.json?burst=' + Math.random(),
			url : config.get('service', '') + '/instances/stats/collection',
			initialize : function(options) {
				this.oauth2_token = options.oauth2_token
			},
			state : {
				pageSize : 100,
				firstPage : 0
			},
			queryParams : {
				totalPages : null,
				totalRecords : null,
				currentPage : 'page',
				pageSize : 'per_page',
				sortKey : 'sort',
				order : 'order'
			},
			parseState : function(resp, queryParams, state, options) {
				return {
					totalRecords : resp.totalCount
				};
			},
			parseRecords : function(resp, options) {
				return resp.elements;
			}
		});
	});
	return Lvl.Entities.Statistic;
});