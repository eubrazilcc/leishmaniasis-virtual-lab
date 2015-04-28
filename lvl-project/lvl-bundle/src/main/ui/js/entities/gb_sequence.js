/**
 * RequireJS module that defines the entity: GenBank sequence.
 */

define([ 'app', 'apps/config/marionette/configuration', 'backbone.picky' ], function(Lvl, Configuration) {
	Lvl.module('Entities.GbSequence', function(Entities, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		Entities.GbSequence = Backbone.Model.extend({
			urlRoot : config.get('service', '') + '/gb_nucleotides/',
			idAttribute : 'GBSeq_primary-accession',
			defaults : {
				'GBSeq_locus' : '',
				'GBSeq_primary-accession' : ''
			},
			initialize : function() {
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