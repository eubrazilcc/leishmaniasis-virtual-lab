/**
 * RequireJS module that defines the entity: LVL instance.
 */

define([ 'app', 'backbone.picky', 'backbone.paginator' ], function(Lvl) {
	Lvl.module('Entities.Instance', function(Entities, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Entities.Instance = Backbone.Model.extend({
			defaults : {
				instanceId : '',
				roles : [],
				heartbeat : '',
				location : ''
			},
			initialize : function() {
				var selectable = new Backbone.Picky.Selectable(this);
				_.extend(this, selectable);
			},
			validate : function(attrs, options) {
				var errors = {};
				if (!attrs.instanceId) {
					errors.instanceId = 'can\'t be empty';
				}
				if (!_.isEmpty(errors)) {
					return errors;
				}
			}
		});
		Entities.InstanceCollection = Backbone.Collection.extend({
			model : Entities.Instance,
			comparator : 'instanceId',
			initialize : function() {
				var singleSelect = new Backbone.Picky.SingleSelect(this);
				_.extend(this, singleSelect);
			}
		});
		Entities.InstancePageableCollection = Backbone.PageableCollection.extend({
			model : Entities.Instance,
			mode : 'server',
			url : Lvl.config.get('service.url') + '/instances',
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
	return Lvl.Entities.Instance;
});