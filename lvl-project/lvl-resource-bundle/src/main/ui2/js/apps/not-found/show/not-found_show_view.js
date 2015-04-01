/**
 * RequireJS module that defines the view: not-found->show.
 */

define([ 'app', 'tpl!apps/not-found/show/templates/not-found', 'tpl!apps/not-found/show/templates/not-found_nav' ], function(Lvl, NotFoundTpl, NavigationTpl) {
	Lvl.module('NotFoundApp.Show.View', function(View, Lvl, Backbone, Marionette, $, _) {
		View.Navigation = Marionette.ItemView.extend({
			template : NavigationTpl
		});
		View.Support = Marionette.ItemView.extend({
			template : NavigationTpl
		});
		View.Software = Marionette.ItemView.extend({
			template : NavigationTpl
		});
		View.Content = Marionette.LayoutView.extend({
			template : NotFoundTpl,
			regions : {
				navigation : '#section-navigation',
				settings : '#section-settings',
				documentation : '#section-documentation',
				support : '#section-support',
				software : '#section-software'
			},
			initialize : function(options) {
				this.navLinks = options.navigation;
				this.settingsLinks = options.settings;
				this.documentationLinks = options.documentation;
				this.supportLinks = options.support;
				this.softwareLinks = options.software;
			},
			onRender : function(options) {
				this.navigation.show(new View.Navigation({
					collection : options.navLinks
				}));
				this.settings.show(new View.Navigation({
					collection : options.settingsLinks
				}));
				this.documentation.show(new View.Navigation({
					collection : options.documentationLinks
				}));
				this.support.show(new View.Support({
					collection : options.supportLinks
				}));
				this.software.show(new View.Software({
					collection : options.softwareLinks
				}));
			}
		});
	});
	return Lvl.NotFoundApp.Show.View;
});