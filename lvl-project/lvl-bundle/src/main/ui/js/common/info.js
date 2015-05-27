/**
 * Creates informative dialogs using qTip2.
 */

define([ 'qtip', 'tpl!common/tpls/info' ], function(qtip, InfoTpl) {
	return function(title, message) {
		var title2 = title || '';
		var message2 = message || '';
		$('<div />').qtip({
			content : {
				text : InfoTpl({
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
				classes : 'qtip-bootstrap dialogue lvl-qtip-info'
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