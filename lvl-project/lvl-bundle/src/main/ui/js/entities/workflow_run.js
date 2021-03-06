/**
 * RequireJS module that defines the entity: workflow_run.
 */

define([ 'app', 'backbone.picky', 'backbone.paginator' ], function(Lvl) {
	Lvl.module('Entities.WorkflowRun', function(Entities, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Entities.WorkflowRun = Backbone.Model.extend({
			urlRoot : Lvl.config.get('service.url') + '/pipelines/runs/~/',
			defaults : {
				id : '',
				workflowId : '',
				invocationId : '',
				parameters : null,
				submitter : '',
				submitted : '',
				status : {
					'completeness' : 0,
					'status' : 'Retrieving status...'
				},
				products : null
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
				if (!attrs.workflowId) {
					errors.workflowId = 'can\'t be empty';
				}
				if (!attrs.invocationId) {
					errors.invocationId = 'can\'t be empty';
				}
				if (!attrs.submitter) {
					errors.submitter = 'can\'t be empty';
				}
				if (!attrs.submitted) {
					errors.submitted = 'can\'t be empty';
				}
				if (!_.isEmpty(errors)) {
					return errors;
				}
			}
		});
		Entities.WorkflowRunCollection = Backbone.Collection.extend({
			model : Entities.WorkflowRun,
			comparator : 'id',
			initialize : function() {
				var singleSelect = new Backbone.Picky.SingleSelect(this);
				_.extend(this, singleSelect);
			}
		});
		Entities.WorkflowRunPageableCollection = Backbone.PageableCollection.extend({
			model : Entities.WorkflowRun,
			mode : 'server',
			url : Lvl.config.get('service.url') + '/pipelines/runs/~',
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
		Entities.WorkflowRunAllCollection = Backbone.PageableCollection.extend({
			model : Entities.WorkflowRun,
			url : Lvl.config.get('service.url') + '/pipelines/runs/~',
			initialize : function(options) {
				this.oauth2_token = options.oauth2_token
			},
			state : {
				pageSize : 1000000,
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
	return Lvl.Entities.WorkflowRun;
});