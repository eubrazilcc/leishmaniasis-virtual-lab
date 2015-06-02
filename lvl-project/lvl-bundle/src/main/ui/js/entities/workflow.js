/**
 * RequireJS module that defines the entity: workflow.
 */

define([ 'app', 'backbone.picky', 'backbone.paginator' ], function(Lvl) {
	Lvl.module('Entities.Workflow', function(Entities, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Entities.WorkflowDefinition = Backbone.Model.extend({
			defaults : {
				id : '',
				name : '',
				description : ''
			},
			initialize : function() {
				var selectable = new Backbone.Picky.Selectable(this);
				_.extend(this, selectable);
			},
			validate : function(attrs, options) {
				var errors = {};
				if (!attrs.id) {
					errors.id = 'can\'t be empty';
				}
				if (!attrs.name) {
					errors.name = 'can\'t be empty';
				}
				if (!_.isEmpty(errors)) {
					return errors;
				}
			}
		});
		Entities.WorkflowDefinitionCollection = Backbone.Collection.extend({
			model : Entities.WorkflowDefinition,
			comparator : 'id',
			initialize : function() {
				var singleSelect = new Backbone.Picky.SingleSelect(this);
				_.extend(this, singleSelect);
			}
		});
		Entities.WorkflowDefinitionPageableCollection = Backbone.PageableCollection.extend({
			model : Entities.WorkflowDefinition,
			mode : 'server',
			url : Lvl.config.get('service.url') + '/pipelines/definitions',
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
	return Lvl.Entities.Workflow;
});