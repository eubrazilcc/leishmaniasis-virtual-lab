/**
 * RequireJS module that defines the view: collection->view_sequence.
 * 
 * @see: Sequence Viewer: https://gist.github.com/lsauer/2763251
 * @see: DRuMS Color Codes: http://www.umass.edu/molvis/drums/codes.html#nucleic
 */

define([ 'app', 'tpl!apps/collection/sequence_viewer/tpls/collection_sequence_viewer' ], function(Lvl, DisplaySequenceTpl) {
	Lvl.module('CollectionApp.Sequence.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		View.Content = Marionette.ItemView.extend({
			template : DisplaySequenceTpl,
			templateHelpers : {
				gbField : function(id, encode) {
					var encode = encode || false;
					return encode ? encodeURIComponent(this[id]) : this[id];
				},
				gbGI : function() {
					var gi = '';
					if (this['gbseqOtherSeqids'] && this['gbseqOtherSeqids'].gbseqid) {
						var seqIds = this['gbseqOtherSeqids'].gbseqid;
						for (var i = 0; i < seqIds.length; i++) {
							var seqId = seqIds[i].value.trim().toLowerCase();
							if (seqId.substring(0, 3) === 'gi|') {
								gi = seqId.substr(3);
							}
						}
					}
					return gi;
				},
				gbSequence : function() {
					var seq = this['gbseqSequence'];
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
	return Lvl.CollectionApp.Sequence.View;
});