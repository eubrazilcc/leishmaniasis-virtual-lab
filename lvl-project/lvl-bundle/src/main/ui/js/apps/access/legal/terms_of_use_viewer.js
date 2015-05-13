/**
 * RequireJS module that defines the view: access->legal->terms of use viewer.
 */

define([ 'app', 'tpl!apps/access/legal/tpls/terms_of_use_viewer' ], function(Lvl, TermsOfUseTpl) {
	Lvl.module('AccessApp.TermsOfUse.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		View.Content = Marionette.ItemView.extend({
			template : TermsOfUseTpl			
		});
	});
	return Lvl.AccessApp.TermsOfUse.View;
});