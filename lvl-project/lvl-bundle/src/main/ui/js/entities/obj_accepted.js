/**
 * RequireJS module that defines the entity: object accepted.
 */

define([ 'app', 'backbone.picky', 'backbone.paginator' ], function(Lvl) {
	Lvl.module('Entities.ObjectAccepted', function(Entities, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Entities.ObjectAccepted = Backbone.Model.extend({			
			urlRoot : Lvl.config.get('service.url') + '/shares/accepted/~/',
			url : function() {
				return this.urlRoot + this.id;
			},
			defaults : {
				id : null,
				owner : null,
				user : null,
				collection : null,
				itemId : null,
				sharedDate : null,
				accessType : null				
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
				if (!attrs.owner) {
					errors.owner = 'can\'t be empty';
				}
				if (!attrs.user) {
					errors.user = 'can\'t be empty';
				}
				if (!attrs.collection) {
					errors.collection = 'can\'t be empty';
				}
				if (!attrs.itemId) {
					errors.itemId = 'can\'t be empty';
				}
				if (!_.isEmpty(errors)) {
					return errors;
				}
			}
		});
		Entities.ObjectAcceptedCreate = Backbone.Model.extend({
			url : function() {
				return Lvl.config.get('service.url') + '/shares/accepted/~';
			}
		});
		Entities.ObjectAcceptedCollection = Backbone.Collection.extend({
			model : Entities.ObjectAccepted,
			comparator : 'id',
			initialize : function() {
				var singleSelect = new Backbone.Picky.SingleSelect(this);
				_.extend(this, singleSelect);
			}
		});
		Entities.ObjectAcceptedPageableCollection = Backbone.PageableCollection.extend({
			model : Entities.ObjectAccepted,
			mode : 'server',
			url : function() {
				return Lvl.config.get('service.url') + '/shares/accepted/~';
			},
			initialize : function(options) {
				var collectionId;
				switch (options.collectionId) {
				case 'leishmania':
					collectionId = 'leishmaniaPending';
					break;
				case 'sandflies':
				default:
					collectionId = 'sandflyPending';
					break;
				}				
				this.oauth2_token = options.oauth2_token,
				this.collectionId = collectionId,
				this.itemId = options.itemId,
				this.queryParams.q = 'collection:' + collectionId + ' itemId:' + options.itemId
			},
			state : {
				pageSize : 1000,
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
	return Lvl.Entities.ObjectAccepted;
});