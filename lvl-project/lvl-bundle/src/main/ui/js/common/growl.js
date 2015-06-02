/**
 * Creates Growl like notifications using qTip2.
 */

define([ 'qtip', 'imagesloaded' ], function(qtip) {
	return function(title, alert, persistent) {
		var target = $('.qtip.jgrowl:visible:last');
		var growlTitle = title || '';
		var growlAlert = alert || '';
		var growlPersistent = persistent || false;
		$('<div/>').qtip({
			content : {
				text : growlAlert,
				title : {
					text : growlTitle,
					button : true
				}
			},
			position : {
				target : [ 0, 0 ],
				container : $('#lvl-qtip-growl-container')
			},
			show : {
				event : false,
				ready : true,
				effect : function() {
					$(this).stop(0, 1).animate({
						height : 'toggle'
					}, 400, 'swing');
				},
				delay : 0,
				persistent : growlPersistent
			},
			hide : {
				event : false,
				effect : function(api) {
					$(this).stop(0, 1).animate({
						height : 'toggle'
					}, 400, 'swing');
				}
			},
			style : {
				width : 250,
				classes : 'qtip-bootstrap jgrowl',
				tip : false
			},
			events : {
				render : function(event, api) {
					if (!api.options.show.persistent) {
						$(this).bind('mouseover mouseout', function(e) {
							var lifespan = 5000;

							clearTimeout(api.timer);
							if (e.type !== 'mouseover') {
								api.timer = setTimeout(function() {
									api.hide(e)
								}, lifespan);
							}
						}).triggerHandler('mouseout');
					}
				}
			}
		});
	}
});