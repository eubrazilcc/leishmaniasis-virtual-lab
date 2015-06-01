/**
 * RequireJS module that defines the entity: PubMed citation.
 */

define([ 'app', 'backbone.picky' ], function(Lvl) {
	Lvl.module('Entities.PmCitation', function(Entities, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Entities.PmCitation = Backbone.Model.extend({
			urlRoot : Lvl.config.get('service', '') + '/citations/',
			url : function() {
				return this.urlRoot + this.id + '/export/pubmed';
			},
			defaults : {
				'medlineCitation' : {
					'pmid' : ''
				}
			},
			initialize : function() {
				var selectable = new Backbone.Picky.Selectable(this);
				_.extend(this, selectable);
			},
			parse : function(resp) {
				resp.id = resp.medlineCitation.pmid.value;
				return resp;
			}
		});
	});
	return Lvl.Entities.PmCitation;
});