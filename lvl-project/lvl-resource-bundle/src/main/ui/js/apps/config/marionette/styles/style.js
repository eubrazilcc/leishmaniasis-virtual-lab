/**
 * Define styles that are dynamically loaded in the application. Note that this
 * module uses the 'requirejs.s.contexts._.config' hack to read values of
 * configuration that could change or disappear in the next versions of
 * RequireJS without warning.
 */

define([ 'marionette', 'jquery-ui' ], function(Marionette) {
    var bust = requirejs.s.contexts._.config.urlArgs ? '?' + requirejs.s.contexts._.config.urlArgs : '';
    Marionette.Controller.Style = Marionette.Controller.extend({
        initialize : function(options) {
            this.baseStyles = [ {
                id : 'bootstrap',
                url : '//maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css' + bust
            }, {
                id : 'font-awesome',
                url : '//maxcdn.bootstrapcdn.com/font-awesome/4.1.0/css/font-awesome.min.css' + bust
            }, {
                id : 'google_fonts',
                url : '//fonts.googleapis.com/css?family=Lato:300,400,700,900,400italic,700italic' + bust
            }, {
                id : 'flat-ui',
                url : '/css/flat-ui.css' + bust
            }, {
                id : 'lvl',
                url : '/css/lvl.css' + bust
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