/**
 * RequireJS module that defines the view: collection->view_sample.
 */

define([ 'app', 'tpl!apps/collection/sample_viewer/tpls/collection_sample_viewer', 'moment' ], function(Lvl, DisplaySampleTpl, moment) {
	Lvl.module('CollectionApp.Sample.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		View.Content = Marionette.ItemView.extend({
			template : DisplaySampleTpl,
			templateHelpers : {
				dwcField : function(id, encode) {
					var encode = encode || false;					
					return encode ? encodeURIComponent(this[id]) : this[id];
				},
				yearField : function() {					
					return (this['year'] ? moment(this['year'], 'x').format('YYYY') : '');
				},
				lastModified : function() {
					return (this['modified'] && this['modified'].content && $.isArray(this['modified'].content) && this['modified'].content.length > 0 
							&& this['modified'].content[0] ? moment(this['modified'].content[0]).format('MMM DD[,] YYYY [at] HH[:]mm') : '');
				},
				getCatalogUrl : function() {
					var url = '';
					switch (this['collectionCode']) {
					case 'Fiocruz-CLIOC':
						url = 'http://clioc.fiocruz.br/index?catalogue';
						break;
					case 'Fiocruz-COLFLEB':
						url = 'http://colfleb.fiocruz.br/catalogue';
						break;
					default:						
						break;
					}
					return url;
				},
				getCatalogName : function() {
					return this['collectionCode'];
				}
			},
			initialize : function() {
				this.listenTo(this.model, 'change', this.render);
				var self = this;
				self.model.fetch({
					reset : true
				});
			}
		});
	});
	return Lvl.CollectionApp.Sample.View;
});