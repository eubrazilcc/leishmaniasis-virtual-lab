/**
 * Creates alert dialogs using qTip2.
 */

define([ 'qtip', 'tpl!common/tpls/alert', 'imagesloaded' ], function(qtip, AlertTpl) {
	return function(title, message) {
		var title2 = title || '';
		var message2 = message || '';
		$('<div />').qtip({
			content : {
				text : AlertTpl({
					message : message2
				}),
				title : {
					text : title2,
					button : true
				}
			},
			position : {
				my : 'center',
				at : 'center',
				target : $(window)
			},
			show : {
				ready : true,
				modal : {
					on : true,
					blur : false
				}
			},
			hide : false,
			style : {
				classes : 'qtip-bootstrap dialogue lvl-qtip-alert'
			},
			events : {
				render : function(event, api) {
					$('button', api.elements.content).click(function(e) {
						api.hide(e);
					});
				},
				hide : function(event, api) {
					api.destroy();
				}
			}
		});
	}
});