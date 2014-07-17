/**
 * RequireJS module that defines the entity: navigation link.
 */

define([ 'app' ], function(Lvl) {

    Lvl.module('Entities', function(Entities, Lvl, Backbone, Marionette, $, _) {
        Entities.Navigation = Backbone.Model.extend({
            defaults : {
                href : '/#home',
                icon : 'fa-chain-broken',
                text : 'Unknown'
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
            comparator : 'id'
        });

        var iniNavigationLinks = function() {
            var links = new Entities.NavigationCollection([ {
                id : 1,
                href : '/#home',
                icon : 'fa-home',
                text : 'Home'
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
                href : '/#ecompendium',
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
            } ]);
            return links;
        };

        var iniSettingsLinks = function() {
            var links = new Entities.NavigationCollection([ {
                id : 1,
                href : '/#account',
                icon : 'fa-user',
                text : 'Account'
            }, {
                id : 2,
                href : '/#settings',
                icon : 'fa-cog',
                text : 'Settings'
            } ]);
            return links;
        };

        var iniExternalLinks = function() {
            var links = new Entities.NavigationCollection([ {
                id : 1,
                href : 'http://www.eubrazilcloudconnect.eu/',
                icon : 'fa-cloud',
                text : 'EU-Brazil Cloud Connect'
            }, {
                id : 2,
                href : '/doc/',
                icon : 'fa-book',
                text : 'Server documentation'
            } ]);
            return links;
        };

        var API = {
            getNavigationEntities : function() {
                var links = iniNavigationLinks();
                return links;
            },
            getSettingEntities : function() {
                var links = iniSettingsLinks();
                return links;
            },
            getExternalEntities : function() {
                var links = iniExternalLinks();
                return links;
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
    });

    return;
});