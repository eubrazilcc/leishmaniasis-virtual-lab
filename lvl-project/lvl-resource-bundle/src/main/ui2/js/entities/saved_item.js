/**
 * RequireJS module that defines the entity: saved_item.
 */

define([ 'app', 'apps/config/marionette/configuration', 'backbone.picky', 'backbone.paginator' ], function(Lvl, Configuration) {
	Lvl.module('Entities.SavedItem', function(Entities, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		Entities.SavedItem = Backbone.Model.extend({
			defaults : {
				id : '',
				type : '',
				description : '',
				pocket : {}
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
				if (!attrs.type) {
					errors.type = 'can\'t be empty';
				}
				if (!attrs.pocket) {
					errors.pocket = 'can\'t be empty';
				}
				if (!_.isEmpty(errors)) {
					return errors;
				}
			}
		});
		Entities.SavedItemCollection = Backbone.Collection.extend({
			model : Entities.SavedItem,
			comparator : 'id',
			initialize : function() {
				var singleSelect = new Backbone.Picky.SingleSelect(this);
				_.extend(this, singleSelect);
			}
		});
		Entities.SavedItemPageableCollection = Backbone.PageableCollection.extend({
			model : Entities.SavedItem,
			mode : 'server',
			url : function() {
				return config.get('service', '') + '/saved_items';
			},
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
				// set additional properties before returning to caller
				this.formattedQuery = resp.formattedQuery;
				return {
					totalRecords : resp.totalCount
				};
			},
			parseRecords : function(resp, options) {
				return resp.elements;
			}
		});
	});
	return Lvl.Entities.SavedItem;
});