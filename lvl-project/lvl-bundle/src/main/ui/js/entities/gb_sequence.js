/**
 * RequireJS module that defines the entity: GenBank sequence.
 */

define([ 'app', 'backbone.picky' ], function(Lvl) {
	Lvl.module('Entities.GbSequence', function(Entities, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Entities.GbSequence = Backbone.Model.extend({
			urlRoot : Lvl.config.get('service.url') + '/sequences/',
			url : function() {
				return this.urlRoot + this.get('dataSource') + '/' + this.id + '/export/gb/xml';
			},
			idAttribute : 'gbSeqPrimaryAccession',
			defaults : {
				'gbSeqLocus' : '',
				'gbSeqPrimaryAccession' : '',
				'dataSource' : 'sandflies'
			},
			initialize : function(options) {
				var selectable = new Backbone.Picky.Selectable(this);
				_.extend(this, selectable);
			},
			validate : function(attrs, options) {
				var errors = {};
				if (!attrs.GBSeq_locus) {
					errors.GBSeq_locus = 'can\'t be empty';
				}
				if (!_.isEmpty(errors)) {
					return errors;
				}
			}
		});
	});
	return Lvl.Entities.GbSequence;
});