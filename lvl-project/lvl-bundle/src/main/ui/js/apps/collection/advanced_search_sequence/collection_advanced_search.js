/**
 * RequireJS module that defines the view: collection->advanced_search_sequence.
 */

define([ 'app', 'tpl!apps/collection/advanced_search_sequence/tpls/collection_advanced_search', 'backbone.syphon' ], function(Lvl, AdvancedSearchTpl) {
	Lvl.module('CollectionApp.AdvancedSearchSequence.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		View.Content = Marionette.ItemView.extend({
			template : AdvancedSearchTpl,			
			events : {
				'click button#advanced_search_sequence-btn' : 'submitSearch'
			},
			submitSearch : function(e) {
				e.preventDefault();
				var self = this;
				var formData = Backbone.Syphon.serialize(this), search = '';
				_.each([{ key: 'source', val: formData.source_input },
					{ key: 'definition', val: formData.definition_input },
					{ key: 'accession', val: formData.accession_input },
					{ key: 'length', val: formData.length_input },
					{ key: 'gene', val: formData.gene_input },
					{ key: 'organism', val: formData.organism_input },
					{ key: 'country', val: formData.country_input },
					{ key: 'locale', val: formData.locale_input ? '_' + formData.locale_input.trim().toUpperCase() : formData.locale_input }], 
				function(item) {
					var val;					
					if (item.val && (val = item.val.trim()) !== '') search += item.key + ':"' + val + '" ';
				});
				if (formData.free_text_input) search += ' "' + formData.free_text_input.trim() + '"';
				Lvl.vent.trigger('search:form:submitted', search);
				self.trigger('destroy');
			}
		});
	});
	return Lvl.CollectionApp.AdvancedSearchSequence.View;
});