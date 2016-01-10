/**
 * RequireJS module that defines the entity: drive->tab-link.
 */

define([ 'app', 'backbone.picky' ], function(Lvl) {
	Lvl.module('DriveApp.Entities', function(Entities, Lvl, Backbone, Marionette, $, _) {
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

		Entities.NavigationDrive = Backbone.Collection.extend({
			model : Entities.Navigation,
			comparator : 'id',
			initialize : function() {
				var singleSelect = new Backbone.Picky.SingleSelect(this);
				_.extend(this, singleSelect);
			}
		});

		var iniNavigationLinks = function() {
			Entities.navigationLinks = new Entities.NavigationDrive([ {
				id : 1,
				link : 'datasets',
				icon : 'fa-cloud',
				text : 'Datasets'
			}, {
				id : 2,
				link : 'links',
				icon : 'fa-link',
				text : 'Links'
			}, {
				id : 3,
				link : 'granted',
				icon : 'fa-share',
				text : 'Granted'
			}, {
				id : 4,
				link : 'accepted',
				icon : 'fa-reply',
				text : 'Accepted'
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

		Lvl.reqres.setHandler('drive:navigation:entities', function() {
			return API.getNavigationEntities();
		});
	});
	return;
});