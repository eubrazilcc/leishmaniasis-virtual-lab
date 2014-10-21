/**
 * RequireJS module that defines the entity: PubMed citation.
 */

define([ 'app', 'apps/config/marionette/configuration', 'backbone.picky' ], function(Lvl, Configuration) {
	Lvl.module('Entities.PmCitation', function(Entities, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		Entities.PmCitation = Backbone.Model.extend({
			urlRoot : config.get('service', '') + '/pm_citations/',
			defaults : {
				MedlineCitation : {
					PMID : ''
				}
			},
			initialize : function() {
				var selectable = new Backbone.Picky.Selectable(this);
				_.extend(this, selectable);
			},
			parse : function(resp) {
				resp.id = resp.MedlineCitation.PMID.value;
				return resp;
			}
		});
	});
	return Lvl.Entities.PmCitation;
});