/**
 * RequireJS module that defines the view: home->show.
 */

define([ 'marionette', 'tpl!apps/home/show/templates/home' ], function(Marionette, HomeTpl) {
	return {
		Content : Marionette.ItemView.extend({
			template : HomeTpl,
			templateHelpers : {
				encodeEndpoint : function() {
					return encodeURIComponent(this.endpoint)
				}
			}
		})
	};
});