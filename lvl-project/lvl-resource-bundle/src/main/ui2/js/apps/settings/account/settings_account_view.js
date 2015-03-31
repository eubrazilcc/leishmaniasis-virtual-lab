/**
 * RequireJS module that defines the view: settings->account.
 */

define([ 'marionette', 'tpl!apps/settings/account/templates/settings_account' ], function(Marionette, AccountTpl) {
	return {
		Content : Marionette.ItemView.extend({
			id : 'account',
			template : AccountTpl,
			templateHelpers : function() {
				return {
					username : this.model.get('userid'),
					provider : this.model.get('provider') || 'lvl',
					formattedname : this.model.get('firstname') + ' ' + this.model.get('lastname'),
					email : this.model.get('email'),
					firstname : this.model.get('firstname'),
					lastname : this.model.get('lastname'),
					pictureUrl : this.model.get('pictureUrl'),
					roles : (this.model.get('roles') || [ 'user' ]).join(', '),					
					positions : (this.model.get('positions') || [ '' ]).join('; '),
					industry : this.model.get('industry') || ''					
				}
			},
			initialize : function() {
				this.listenTo(this.model, 'change', this.render);
				var search_params = {
					'plain' : 'true'
				};
				var self = this;
				self.model.fetch({
					reset : true,
					data : $.param(search_params)
				});
			}
		})
	};
});