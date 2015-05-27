/**
 * RequireJS module that defines the entity: workflow parameters.
 */

define([ 'app', 'apps/config/marionette/configuration', 'backbone.picky', 'backbone.paginator' ], function(Lvl, Configuration) {
	Lvl.module('Entities.WorkflowParameters', function(Entities, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		Entities.WorkflowParameters = Backbone.Model.extend({
			defaults : {
				description : '',
				name : '',
				type : '',
				value : ''
			},
			initialize : function(options) {
				var selectable = new Backbone.Picky.Selectable(this);
				_.extend(this, selectable);
			},
			validate : function(attrs, options) {
				var errors = {};
				if (!attrs.name) {
					errors.name = 'can\'t be empty';
				}
				if (!_.isEmpty(errors)) {
					return errors;
				}
			}
		});
		Entities.WorkflowParametersCollection = Backbone.Collection.extend({
			urlRoot : config.get('service', '') + '/pipelines/definitions/',
			url : function() {
				return this.urlRoot + this.workflowId + '/' + this.versionId + '/params';
			},
			model : Entities.WorkflowParameters,
			comparator : 'name',
			initialize : function(options) {
				var singleSelect = new Backbone.Picky.SingleSelect(this);
				_.extend(this, singleSelect);
				this.oauth2_token = options.oauth2_token;
				this.workflowId = options.workflowId;
				this.versionId = options.versionId,
				this.wfOpts = options.wfOpts
			}
		});
	});
	return Lvl.Entities.WorkflowParameters;
});