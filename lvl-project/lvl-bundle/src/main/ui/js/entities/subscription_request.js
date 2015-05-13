/**
 * RequireJS module that defines the entity: subscription request.
 */

define([ 'app', 'apps/config/marionette/configuration', 'backbone.picky', 'backbone.paginator' ], function(Lvl, Configuration) {
	Lvl.module('Entities.SubscrReq', function(Entities, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		Entities.SubscrReq = Backbone.Model.extend({
			urlRoot : config.get('service', '') + '/support/subscriptions/requests',
			defaults : {
				id : '',
				email : '',
				requested : '',
				channels : [],
				fulfilled : ''
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
				if (!attrs.email) {
					errors.email = 'can\'t be empty';
				}
				if (!attrs.requested) {
					errors.requested = 'can\'t be empty';
				}
				if (!attrs.channels) {
					errors.channels = 'can\'t be empty';
				}
				if (!_.isEmpty(errors)) {
					return errors;
				}
			}
		});
		Entities.SubscrReqCollection = Backbone.Collection.extend({
			model : Entities.SubscrReq,
			comparator : 'id',
			initialize : function() {
				var singleSelect = new Backbone.Picky.SingleSelect(this);
				_.extend(this, singleSelect);
			}
		});
		Entities.SubscrReqPageableCollection = Backbone.PageableCollection.extend({
			model : Entities.SubscrReq,
			mode : 'server',
			url : config.get('service', '') + '/support/subscriptions/requests',
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
	return Lvl.Entities.SubscrReq;
});