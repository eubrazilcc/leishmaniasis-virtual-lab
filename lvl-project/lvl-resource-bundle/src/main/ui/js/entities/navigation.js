/**
 * RequireJS module that defines the entity: navigation link.
 */

define([ 'app', 'backbone.picky' ], function(Lvl) {
    Lvl.module('Entities', function(Entities, Lvl, Backbone, Marionette, $, _) {
        Entities.Navigation = Backbone.Model.extend({
            defaults : {
                href : '/#home',
                icon : 'fa-chain-broken',
                text : 'Unknown',
                isFirst : '' /* labels the first element of a collection */
            },
            initialize : function() {
                var selectable = new Backbone.Picky.Selectable(this);
                _.extend(this, selectable);
            },
            validate : function(attrs, options) {
                var errors = {};
                if (!attrs.href) {
                    errors.href = 'can\'t be empty';
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

        Entities.NavigationCollection = Backbone.Collection.extend({
            model : Entities.Navigation,
            comparator : 'id',
            initialize : function() {
                var singleSelect = new Backbone.Picky.SingleSelect(this);
                _.extend(this, singleSelect);
            }
        });

        var iniNavigationLinks = function() {
            Entities.navigationLinks = new Entities.NavigationCollection([ {
                id : 1,
                href : '/#home',
                icon : 'fa-home',
                text : 'Home',
                isFirst : 'navigation'
            }, {
                id : 2,
                href : '/#social',
                icon : 'fa-comments',
                text : 'Community'
            }, {
                id : 3,
                href : '/#collection',
                icon : 'fa-database',
                text : 'Collection'
            }, {
                id : 4,
                href : '/#e-compendium',
                icon : 'fa-file-text',
                text : 'e-Compendium'
            }, {
                id : 5,
                href : '/#analysis',
                icon : 'fa-barcode',
                text : 'Molecular Analysis'
            }, {
                id : 6,
                href : '/#enm',
                icon : 'fa-globe',
                text : 'Ecological Niche Modelling'
            }, {
                id : 7,
                href : '/#links',
                icon : 'fa-link',
                text : 'Public links'
            } ]);
        };

        var iniSettingsLinks = function() {
            Entities.settingsLinks = new Entities.NavigationCollection([ {
                id : 8,
                href : '/#account',
                icon : 'fa-user',
                text : 'Account',
                isFirst : 'settings'
            }, {
                id : 9,
                href : '/#settings',
                icon : 'fa-cog',
                text : 'Settings'
            } ]);
        };

        var iniExternalLinks = function() {
            Entities.externalLinks = new Entities.NavigationCollection([ {
                id : 9,
                href : 'http://www.eubrazilcloudconnect.eu/',
                icon : 'fa-cloud',
                text : 'EU-Brazil Cloud Connect',
                isFirst : 'external'
            }, {
                id : 10,
                href : '/doc/',
                icon : 'fa-book',
                text : 'Server documentation'
            } ]);
        };

        var API = {
            getNavigationEntities : function() {
                if (Entities.navigationLinks === undefined) {
                    iniNavigationLinks();
                }
                return Entities.navigationLinks;
            },
            getSettingEntities : function() {
                if (Entities.settingsLinks === undefined) {
                    iniSettingsLinks();
                }
                return Entities.settingsLinks;
            },
            getExternalEntities : function() {
                if (Entities.externalLinks === undefined) {
                    iniExternalLinks();
                }
                return Entities.externalLinks;
            },
            getNavigationSettingEntities : function() {
                if (Entities.navigationLinks === undefined) {
                    iniNavigationLinks();
                }
                if (Entities.settingsLinks === undefined) {
                    iniSettingsLinks();
                }
                return new Entities.NavigationCollection(Entities.navigationLinks.toJSON().concat(Entities.settingsLinks.toJSON()));
            }
        }

        Lvl.reqres.setHandler('navigation:links:entities', function() {
            return API.getNavigationEntities();
        });

        Lvl.reqres.setHandler('navigation:settings:entities', function() {
            return API.getSettingEntities();
        });

        Lvl.reqres.setHandler('navigation:external:entities', function() {
            return API.getExternalEntities();
        });

        Lvl.reqres.setHandler('navigation:links+settings:entities', function() {
            return API.getNavigationSettingEntities();
        });
    });

    return;
});