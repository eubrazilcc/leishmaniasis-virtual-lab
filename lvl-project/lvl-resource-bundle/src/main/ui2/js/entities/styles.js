/**
 * RequireJS module that defines the entity: dynamic styles.
 */

define([ 'app', 'apps/config/marionette/configuration' ], function(Lvl, Configuration) {
	Lvl.module('Entities', function(Entities, Lvl, Backbone, Marionette, $, _) {
		var bust = new Configuration().get('bust', '');

		Entities.Style = Backbone.Model.extend({
			defaults : {
				id : 'none',
				url : ''
			},
			validate : function(attrs, options) {
				var errors = {};
				if (!attrs.id) {
					errors.id = 'can\'t be empty';
				} else {
					if (attrs.id.length < 2) {
						errors.id = 'is too short';
					}
				}
				if (!attrs.url) {
					errors.url = 'can\'t be empty';
				}
				if (!_.isEmpty(errors)) {
					return errors;
				}
			}
		});

		Entities.StyleCollection = Backbone.Collection.extend({
			model : Entities.Style,
			comparator : 'id'
		});

		var iniFlagsSpriteStyles = function() {
			Entities.flagsSpriteStyles = new Entities.StyleCollection([ {
				id : 'flags',
				url : '/css/flags.css' + bust
			} ]);
		};

		var iniFormValidationStyles = function() {
			Entities.formValidationStyles = new Entities.StyleCollection([ {
				id : 'bootstrapvalidator',
				url : '//cdnjs.cloudflare.com/ajax/libs/jquery.bootstrapvalidator/0.5.3/css/bootstrapValidator.min.css' + bust
			} ]);
		};				

		var iniBackgridStyles = function() {
			Entities.backgridStyles = new Entities.StyleCollection([ {
				id : 'backgrid',
				url : '//cdnjs.cloudflare.com/ajax/libs/backgrid.js/0.3.5/backgrid.min.css' + bust
			}, {
				id : 'backgrid-paginator',
				url : '/css/backgrid-paginator.min.css' + bust
			}, {
				id : 'backgrid-select-all',
				url : '/css/backgrid-select-all.min.css' + bust
			}, {
				id : 'backgrid-filter',
				url : '/css/backgrid-filter.min.css' + bust
			} ]);
		};

		var iniOpenLayersStyles = function() {
			Entities.openLayersStyles = new Entities.StyleCollection([ {
				id : 'ol',
				url : '//cdnjs.cloudflare.com/ajax/libs/ol3/3.2.1/ol.min.css' + bust
			} ]);
		};

		var iniPaceStyles = function() {
			Entities.paceStyles = new Entities.StyleCollection([ {
				id : 'pace',
				url : '//cdnjs.cloudflare.com/ajax/libs/pace/1.0.2/themes/black/pace-theme-center-simple.css' + bust
			} ]);
		};

		var iniQtipStyles = function() {
			Entities.qtipStyles = new Entities.StyleCollection([ {
				id : 'qtip',
				url : '//cdnjs.cloudflare.com/ajax/libs/qtip2/2.2.0/jquery.qtip.min.css' + bust
			} ]);
		};

		var iniChartistStyles = function() {
			Entities.chartistStyles = new Entities.StyleCollection([ {
				id : 'chartist',
				url : '//cdn.jsdelivr.net/chartist.js/latest/chartist.min.css' + bust
			} ]);
		};

		var iniHopscotchStyles = function() {
			Entities.hopscotchStyles = new Entities.StyleCollection([ {
				id : 'hopscotch',
				url : '//cdnjs.cloudflare.com/ajax/libs/hopscotch/0.2.3/css/hopscotch.min.css' + bust
			} ]);
		};

		var API = {
			getFlagsSpriteStyles : function() {
				if (Entities.flagsSpriteStyles === undefined) {
					iniFlagsSpriteStyles();
				}
				return Entities.flagsSpriteStyles;
			},
			getFormValidationStyles : function() {
				if (Entities.formValidationStyles === undefined) {
					iniFormValidationStyles();
				}
				return Entities.formValidationStyles;
			},			
			getBackgridStyles : function() {
				if (Entities.backgridStyles === undefined) {
					iniBackgridStyles();
				}
				return Entities.backgridStyles;
			},
			getOpenLayersStyles : function() {
				if (Entities.openLayersStyles === undefined) {
					iniOpenLayersStyles();
				}
				return Entities.openLayersStyles;
			},
			getPaceStyles : function() {
				if (Entities.paceStyles === undefined) {
					iniPaceStyles();
				}
				return Entities.paceStyles;
			},
			getQtipStyles : function() {
				if (Entities.qtipStyles === undefined) {
					iniQtipStyles();
				}
				return Entities.qtipStyles;
			},
			getChartistStyles : function() {
				if (Entities.chartistStyles === undefined) {
					iniChartistStyles();
				}
				return Entities.chartistStyles;
			},
			getHopscotchStyles : function() {
				if (Entities.hopscotchStyles === undefined) {
					iniHopscotchStyles();
				}
				return Entities.hopscotchStyles;
			}
		}

		Lvl.reqres.setHandler('styles:flags:entities', function() {
			return API.getFlagsSpriteStyles();
		});

		Lvl.reqres.setHandler('styles:form-validation:entities', function() {
			return API.getFormValidationStyles();
		});		

		Lvl.reqres.setHandler('styles:backgrid:entities', function() {
			return API.getBackgridStyles();
		});

		Lvl.reqres.setHandler('styles:openlayers:entities', function() {
			return API.getOpenLayersStyles();
		});

		Lvl.reqres.setHandler('styles:pace:entities', function() {
			return API.getPaceStyles();
		});

		Lvl.reqres.setHandler('styles:qtip:entities', function() {
			return API.getQtipStyles();
		});

		Lvl.reqres.setHandler('styles:chartist:entities', function() {
			return API.getChartistStyles();
		});

		Lvl.reqres.setHandler('styles:hopscotch:entities', function() {
			return API.getHopscotchStyles();
		});

		return;
	});
});