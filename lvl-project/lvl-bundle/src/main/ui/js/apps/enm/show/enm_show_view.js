/**
 * RequireJS module that defines the view: enm->show.
 */

define([ 'marionette', 'tpl!apps/enm/show/tpls/enm' ], function(Marionette, EnmTpl) {
	return {
		Content : Marionette.ItemView.extend({
			template : EnmTpl
		})
	};
});