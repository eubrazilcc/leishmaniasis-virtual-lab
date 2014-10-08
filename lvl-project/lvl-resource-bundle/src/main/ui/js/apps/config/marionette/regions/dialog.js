/**
 * Defines a region to display modal windows (dialogs) within the application.
 */

define([ 'marionette' ], function(Marionette) {
	Marionette.Region.Dialog = Backbone.Marionette.Region.extend({
		constructor : function() {
			_.bindAll(this, 'getEl', 'showModal', 'hideModal');
			Backbone.Marionette.Region.prototype.constructor.apply(this, arguments);
			this.listenTo(this, 'show', this.showModal, this);
		},
		getEl : function(selector) {
			var $el = $(selector);
			$el.attr('class', 'modal fade');
			// allow keyboard access (e.g. escape key to close dialog)
			$el.attr('tabindex', '-1');
			// makes modal accessible
			$el.attr('role', 'dialog');
			$el.attr('aria-labelledby', 'myModalLabel');
			$el.attr('aria-hidden', 'true');
			// react when the dialog is closed
			$el.on('hidden.bs.modal', {
				'selector' : selector
			}, this.closeDialog);
			return $el;
		},
		showModal : function(view) {
			this.listenTo(view, 'close', this.hideModal, this);
			this.$el.modal('show');
		},
		hideModal : function() {
			this.$el.modal('hide');
		},
		closeDialog : function(event) {
			var $el = $(event.data.selector);
			$el.empty();
		}
	});
	return Marionette.Region.Dialog;
});