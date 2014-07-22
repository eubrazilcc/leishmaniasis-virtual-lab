/**
 * RequireJS module that defines utility functions common to all the
 * application.
 */

define([ 'app' ], function(Lvl) {
    Lvl.module('Common.Utils', function(Utils, Lvl, Backbone, Marionette, $, _) {
        Utils.loadSingleCss = function(id, url) {
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
        };
        Utils.loadCss = function(list) {
            if (list !== undefined) {
                for (index = 0; index < list.length; ++index) {
                    this.loadSingleCss(list[index].id, list[index].url);
                }
            }
        };
        Utils.deleteCss = function(list) {
            if (list !== undefined) {
                for (index = 0; index < list.length; ++index) {
                    $('head #' + list[index].id).remove();
                }
            }
        };
    });
    return Lvl.Common.Utils;
});