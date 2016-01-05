/**
 * RequireJS module that defines the view: collection->view_pending_shares.
 */

define([ 'app', 'tpl!apps/collection/pending_shares_viewer/tpls/collection_pending_shares_viewer', 'tpl!apps/collection/pending_shares_viewer/tpls/shares', 'pace', 'moment', 'backbone.oauth2', 'bootstrapvalidator', 'backbone.syphon' ],
		function(Lvl, DisplaySharesTpl, SharesTpl, pace, moment) {
	Lvl.module('CollectionApp.SharePending.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		View.Content = Marionette.ItemView.extend({
			template : DisplaySharesTpl,			
			initialize : function() {
				this.listenTo(this.collection, 'request', this.displaySpinner);
				this.listenTo(this.collection, 'sync error', this.removeSpinner);				
			},
			events : {
				'focus #lvlSharePendingSequenceForm input.form-control' : function(e) {
					var form = $('#lvlSharePendingSequenceForm');
					if (Boolean(form.attr('data-pristine') === 'true')) {
						form.attr('data-pristine', 'false');
						form.on('init.form.bv', function(e, data) {
							data.bv.disableSubmitButtons(true);
						}).bootstrapValidator({
							submitButtons : 'button[type="submit"]',
							fields : {
								'inputEmail' : {
									verbose : false,
									validators : {
										notEmpty : {
											message : 'The email is required and cannot be empty'
										},
										emailAddress : {											
											message : 'The value is not a valid email address'
										}
									}
								}														
							}
						}).on('success.field.bv', function(e, data) {
							var isValid = data.bv.isValid();
							data.bv.disableSubmitButtons(!isValid);
						});
					}
				}, 'click #lvlSharePendingSequenceBtn' : function(e) {
					e.preventDefault();
					pace.restart();
					$('#lvlSharePendingSequenceForm button[type="submit"]').attr('disabled', 'disabled');
					$('#lvlSharePendingSequenceBtn').tooltip('hide');
					var self = this, formData = Backbone.Syphon.serialize(this);
					require([ 'entities/obj_granted' ], function(ObjGrantedModel) {						
						var newObjGranted = new ObjGrantedModel.ObjectGrantedCreate();
						newObjGranted.oauth2_token = Lvl.config.authorizationToken();
						newObjGranted.save({
							'user' : formData.inputEmail,
							'collection' : self.collection.collectionId,
							'itemId' : self.collection.itemId
						}, {							
							success : function(model, resp, options) {
								require([ 'common/growl' ], function(createGrowl) {
									var anchor = $('<a>', {
										href : options.xhr.getResponseHeader('Location')
									})[0];
									var id = anchor.pathname.substring(anchor.pathname.lastIndexOf('/') + 1);
									self.trigger('destroy');
									require([ 'common/growl' ], function(createGrowl) {										
										createGrowl('Invitation sent', 'Revoke ' + id
												+ ' at <a href="/#drive/granted"><i class="fa fa-arrow-circle-right fa-fw"></i> granted objects</a>', false);
									});
								});
							},
							error : function(model, resp, options) {
								self.trigger('destroy');
								require([ 'common/alert' ], function(alertDialog) {
									alertDialog('Error', 'Failed to share sequence.');
								});
							}
						}).always(function() {
							pace.stop();
							var form = self.$('#lvlSharePendingSequenceForm');
							form[0].reset();
							form.bootstrapValidator('resetForm', true);
							form.bootstrapValidator('disableSubmitButtons', true);
						});
					});					
				}
			},
			displaySpinner : function() {
				pace.restart();
				$('#shares-container').fadeTo('fast', 0.4);
			},
			removeSpinner : function(reset) {
				pace.stop();
				$('#shares-container').fadeTo('fast', 1);
			},
			onDestroy : function() {
				pace.stop();
				this.stopListening();
			},
			onRender : function() {
				var self = this;
				pace.start();
			},
			onShow : function() {
				var _self = this;
				this.collection.fetch({
					reset : true,
					success : function(model, resp, options) {
						if (resp && resp.elements) {
							resp.elements.sort(function(a, b) {
								return b.sharedDate - a.sharedDate;
							});
							var shares = [];
							_.each(resp.elements, function(item) {
								shares.push({
									user : item.user,
									sharedDate : moment(item.sharedDate, 'x').format('MMM DD[,] YYYY [at] HH[:]mm')
								});
							});							
							$('#shares-container').html(SharesTpl({
								shares : shares
							}));
						}						
					}
				});
			}
		});
	});
	return Lvl.CollectionApp.SharePending.View;
});