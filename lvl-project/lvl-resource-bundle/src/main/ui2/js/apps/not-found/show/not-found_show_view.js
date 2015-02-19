/**
 * RequireJS module that defines the view: not-found->show.
 */

define([ 'app', 'tpl!apps/not-found/show/templates/not-found', 'tpl!apps/not-found/show/templates/not-found_nav',
		'tpl!apps/not-found/show/templates/not-found_extlink' ], function(Lvl, NotFoundTpl, NavigationTpl, ExternalTpl) {
	Lvl.module('NotFoundApp.Show.View', function(View, Lvl, Backbone, Marionette, $, _) {
		View.Navigation = Marionette.ItemView.extend({
			template : NavigationTpl
		});
		View.External = Marionette.ItemView.extend({
			template : ExternalTpl
		});
		View.Content = Marionette.LayoutView.extend({
			template : NotFoundTpl,
			regions : {
				navigation : '#section-navigation',
				settings : '#section-settings',
				external : '#section-external'
			},
			initialize : function(options) {
				this.navLinks = options.navigation;
				this.settingsLinks = options.settings;
				this.externalLinks = options.external;
			},
			onRender : function(options) {
				this.navigation.show(new View.Navigation({
					collection : options.navLinks
				}));
				this.settings.show(new View.Navigation({
					collection : options.settingsLinks
				}));
				this.external.show(new View.External({
					collection : options.externalLinks
				}));
			}
		});
	});
	return Lvl.NotFoundApp.Show.View;
});