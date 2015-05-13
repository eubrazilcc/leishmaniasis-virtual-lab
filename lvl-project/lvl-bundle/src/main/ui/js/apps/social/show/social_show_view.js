/**
 * RequireJS module that defines the view: social->show.
 */

define([ 'marionette', 'tpl!apps/social/show/tpls/social' ], function(Marionette, SocialTpl) {
	return {
		Content : Marionette.ItemView.extend({
			template : SocialTpl
		})
	};
});