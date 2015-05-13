/**
 * RequireJS module that defines the view: e-compendium->map.
 */

define([ 'marionette', 'tpl!apps/e-compendium/map/tpls/e-compendium_map' ], function(Marionette, MapTpl) {
	return {
		Content : Marionette.ItemView.extend({
			id : 'map',
			template : MapTpl
		})
	};
});