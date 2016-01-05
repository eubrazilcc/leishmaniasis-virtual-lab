/**
 * RequireJS module that defines the view: collection->view_pending.
 */

define([ 'app', 'tpl!apps/collection/pending_viewer/tpls/collection_pending_viewer', 'moment' ], function(Lvl, DisplaySampleTpl, moment) {
	Lvl.module('CollectionApp.Sample.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		View.Content = Marionette.ItemView.extend({
			template : DisplaySampleTpl,
			templateHelpers : {
				dwcField : function(id, encode) {
					var encode = encode || false;
					var sample = this['sample'] || {};
					return encode ? encodeURIComponent(sample[id]) : sample[id];
				},
				yearField : function() {
					var sample = this['sample'] || {};
					return (sample['year'] ? moment(sample['year'], 'x').format('YYYY') : '');
				},
				lastModified : function() {
					var sample = this['sample'] || {};
					return (sample['modified'] && sample['modified'].content && $.isArray(sample['modified'].content) 
							&& sample['modified'].content.length > 0 && sample['modified'].content[0] 
							? moment(sample['modified'].content[0]).format('MMM DD[,] YYYY [at] HH[:]mm') : '');
				},
				getSequence : function() {
					var seq = this['sequence'];
					return seq ? seq.split(/(.{10})/gm).filter(Boolean).map(
							function(e, i, a) {
								var pos = (i * 10) + 1;
								return (!(i % 6) ? '\n' + '    '.slice(0, 4 - ('' + pos).length) + (pos) + ' ' + e : e).replace(/(.{1})/g,
										'<b class="lvl-dna-\$1">\$1</b>')
							}).join(' ') : '';
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