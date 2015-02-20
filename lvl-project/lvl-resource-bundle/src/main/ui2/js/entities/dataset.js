/**
 * RequireJS module that defines the entity: dataset.
 */

define([ 'app', 'apps/config/marionette/configuration', 'backbone.paginator' ], function(Lvl, Configuration) {
	Lvl.module('Entities.Dataset', function(Entities, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		Entities.Dataset = Backbone.Model.extend({
			urlRoot : config.get('service', '') + '/datasets/objects/files/',
			idAttribute : 'filename',
			defaults : {
				description : '',
				downloadUri : '',
				mime : '',
				path : '',
				target : {}
			},
			initialize : function() {
				var selectable = new Backbone.Picky.Selectable(this);
				_.extend(this, selectable);
			},
			validate : function(attrs, options) {
				var errors = {};
				if (!attrs.downloadUri) {
					errors.downloadUri = 'can\'t be empty';
				}
				if (!attrs.mime) {
					errors.mime = 'can\'t be empty';
				}
				if (!attrs.path) {
					errors.path = 'can\'t be empty';
				}
				if (!_.isEmpty(errors)) {
					return errors;
				}
			}
		});
		Entities.DatasetPageableCollection = Backbone.PageableCollection.extend({
			model : Entities.Dataset,
			mode : 'server',
			// url : 'datasets/objects/files.json?burst=' + Math.random(),
			url : config.get('service', '') + '/datasets/objects/files',
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
		Entities.DatasetAllCollection = Backbone.PageableCollection.extend({
			model : Entities.Dataset,
			url : config.get('service', '') + '/datasets/objects/files',
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
	return Lvl.Entities.Dataset;
});