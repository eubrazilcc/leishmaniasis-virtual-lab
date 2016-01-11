/**
 * RequireJS module that defines the entity: pending citation.
 */

define([ 'app', 'backbone.picky', 'backbone.paginator' ], function(Lvl) {
	Lvl.module('Entities.PendingCitation', function(Entities, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Entities.PendingCitation = Backbone.Model.extend({			
			urlRoot : Lvl.config.get('service.url') + '/pending/citations',
			url : function() {
				return this.urlRoot + '/~/' + this.id;
			},
			defaults : {
				pubmedId : null				
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
				if (!attrs.pubmedId) {
					errors.pubmedId = 'can\'t be empty';
				}				
				if (!_.isEmpty(errors)) {
					return errors;
				}
			}
		});
		Entities.PendingCitationCreate = Backbone.Model.extend({
			url : function() {
				return Lvl.config.get('service.url') + '/pending/citations/~';
			}
		});
		Entities.PendingCitationCollection = Backbone.Collection.extend({
			model : Entities.PendingCitation,
			comparator : 'id',
			initialize : function() {
				var singleSelect = new Backbone.Picky.SingleSelect(this);
				_.extend(this, singleSelect);
			}
		});
		Entities.PendingCitationPageableCollection = Backbone.PageableCollection.extend({
			model : Entities.PendingCitation,
			mode : 'server',
			url : function() {
				return Lvl.config.get('service.url') + '/pending/citations/~';
			},
			initialize : function(options) {
				this.oauth2_token = options.oauth2_token;
				this.queryParams.onlySubmitted = (options.curator === true);
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
				this.lvlOpHash = resp.hash;
				return {
					totalRecords : resp.totalCount || 0
				};
			},
			parseRecords : function(resp, options) {				
				return resp.elements;
			}
		});
	});
	return Lvl.Entities.PendingCitation;
});