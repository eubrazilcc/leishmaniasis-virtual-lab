/**
 * RequireJS module that defines styles that are dynamically loaded in the
 * application.
 */

define([ 'marionette', 'apps/config/marionette/configuration' ], function(Marionette, Configuration) {
	Marionette.Controller.Style = Marionette.Controller.extend({
		initialize : function(options) {
			this.bust = new Configuration().get('bust', '');
			this.baseStyles = [ {
				id : 'bootstrap',
				url : '//maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css' + this.bust
			}, {
				id : 'font-awesome',
				url : '//maxcdn.bootstrapcdn.com/font-awesome/4.2.0/css/font-awesome.min.css' + this.bust				
			}, {
				id : 'google_fonts',
				url : '//fonts.googleapis.com/css?family=Lato:300,400,700,900,400italic,700italic' + this.bust
			}, {
				id : 'flat-ui',
				url : '/css/flat-ui.css' + this.bust
			}, {
				id : 'lvl',
				url : '/css/lvl.css' + this.bust
			} ];
		},
		loadSingleCss : function(id, url) {
			if (id !== undefined && url !== undefined) {
				var element = $('head #' + id);
				if (element.length === 0) {
					var link = document.createElement('link');
					link.type = 'text/css';
					link.rel = 'stylesheet';
					link.href = url;
					link.id = id;
					document.getElementsByTagName('head')[0].appendChild(link);
				}
			}
		},
		loadCss : function(list) {
			if (list !== undefined) {
				for (index = 0; index < list.length; ++index) {
					this.loadSingleCss(list[index].id, list[index].url);
				}
			}
		},
		deleteCss : function(list) {
			if (list !== undefined) {
				for (index = 0; index < list.length; ++index) {
					$('head #' + list[index].id).remove();
				}
			}
		},
		loadBaseStyles : function() {
			this.loadCss(this.baseStyles);
		}
	});
	return Marionette.Controller.Style;
});