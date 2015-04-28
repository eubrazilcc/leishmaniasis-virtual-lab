/**
 * Creates confirm dialogs using qTip2.
 */

define([ 'qtip', 'tpl!common/templates/confirm' ], function(qtip, ConfirmTpl) {
	return function(title, message, callback, options) {
		var title2 = title || '';
		var message2 = message || '';
		var o_icon = 'fa-warning', o_icon_color = 'text-warning', o_btn_text = 'OK';
		if (options) {
			o_icon = options['icon'] || o_icon;
			o_icon_color = options['icon_color'] || o_icon_color;
			o_btn_text = options['btn_text'] || o_btn_text;
		}
		$('<div />').qtip({
			content : {
				text : ConfirmTpl({
					message : message2,
					icon : o_icon,
					icon_color : o_icon_color,
					btn_text : o_btn_text
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
				classes : 'qtip-bootstrap dialogue'
			},
			events : {
				render : function(event, api) {
					$('#confirmOkBtn', api.elements.content).click(function(e) {
						api.hide(e);
						if (callback) {
							callback();
						}
					});
					$('#confirmCancelBtn', api.elements.content).click(function(e) {
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