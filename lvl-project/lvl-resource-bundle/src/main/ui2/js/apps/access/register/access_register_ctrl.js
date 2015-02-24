/**
 * RequireJS module that defines the controller: access->register.
 */

define([ 'app', 'apps/access/register/access_register_view' ], function(Lvl, View) {
	Lvl.module('AccessApp.Register', function(Register, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Register.Controller = {
			register : function() {
				var view = new View.Content();
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
				Lvl.mainRegion.show(view);
			}
		}
	});
	return Lvl.AccessApp.Register.Controller;
});