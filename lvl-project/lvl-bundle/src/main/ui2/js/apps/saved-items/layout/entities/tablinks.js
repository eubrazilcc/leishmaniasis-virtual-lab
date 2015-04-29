/**
 * RequireJS module that defines the entity: saved-items->tab-link.
 */

define([ 'app', 'backbone.picky' ], function(Lvl) {
	Lvl.module('SavedItemsApp.Entities', function(Entities, Lvl, Backbone, Marionette, $, _) {
		Entities.Navigation = Backbone.Model.extend({
			defaults : {
				link : '',
				icon : 'fa-chain-broken',
				text : 'Unknown'
			},
			initialize : function() {
				var selectable = new Backbone.Picky.Selectable(this);
				_.extend(this, selectable);
			},
			validate : function(attrs, options) {
				var errors = {};
				if (!attrs.link) {
					errors.link = 'can\'t be empty';
				}
				if (!attrs.icon) {
					errors.icon = 'can\'t be empty';
				}
				if (!attrs.text) {
					errors.text = 'can\'t be empty';
				} else {
					if (attrs.text.length < 2) {
						errors.text = 'is too short';
					}
				}
				if (!_.isEmpty(errors)) {
					return errors;
				}
			}
		});

		Entities.NavigationSavedItems = Backbone.Collection.extend({
			model : Entities.Navigation,
			comparator : 'id',
			initialize : function() {
				var singleSelect = new Backbone.Picky.SingleSelect(this);
				_.extend(this, singleSelect);
			}
		});

		var iniNavigationLinks = function() {
			Entities.navigationLinks = new Entities.NavigationSavedItems([ {
				id : 1,
				link : 'searches',
				icon : 'fa-search',
				text : 'Searches'
			} ]);
		};

		var API = {
			getNavigationEntities : function() {
				if (Entities.navigationLinks === undefined) {
					iniNavigationLinks();
				}
				return Entities.navigationLinks;
			}
		}

		Lvl.reqres.setHandler('saved-items:navigation:entities', function() {
			return API.getNavigationEntities();
		});
	});
	return;
});