/**
 * RequireJS module that defines the controller: footer->show.
 */

define([ 'app', 'apps/footer/show/footer_show_view' ], function(Lvl, View) {
	Lvl.module('FooterApp.Show', function(Show, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Show.Controller = {
			showFooter : function() {
				var view = new View.Footer();
				view.on('access:view:privacy_policy', function(accession) {
					require([ 'apps/access/legal/privacy_policy_viewer' ], function(PrivacyPolicyView) {						
						var dialogView = new PrivacyPolicyView.Content();
						Lvl.dialogRegion.show(dialogView);
					});
				});
				view.on('access:view:terms_and_conditions', function(accession) {
					require([ 'apps/access/legal/terms_of_use_viewer' ], function(TermsOfUseView) {						
						var dialogView = new TermsOfUseView.Content();
						Lvl.dialogRegion.show(dialogView);
					});
				});
				Lvl.footerRegion.show(view);
				return View.Footer.id;
			}
		}
	});
	return Lvl.FooterApp.Show.Controller;
});