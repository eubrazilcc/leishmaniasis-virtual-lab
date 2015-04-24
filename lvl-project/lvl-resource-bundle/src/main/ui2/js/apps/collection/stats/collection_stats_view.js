/**
 * RequireJS module that defines the view: collection->stats.
 */

define([ 'app', 'tpl!apps/collection/stats/templates/collection_stats' ], function(Lvl, StatsTpl) {
	Lvl.module('CollectionApp.Stats.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		View.Content = Marionette.ItemView.extend({
			id : 'stats',
			template : StatsTpl,			
			onRender : function() {
				var bin_colors = [ 'rgba(13, 162, 23, 1)', 'rgba(229, 28, 33, 1)' ];
				var bin_highlights = [ 'rgba(13, 162, 23, 0.8)', 'rgba(229, 28, 33, 0.8)' ];
				var colors = [ 'rgba(1, 87, 154, 1)', 'rgba(33, 152, 243, 1)', 'rgba(0, 134, 9, 1)', 'rgba(13, 162, 23, 1)', 'rgba(106, 1, 124, 1)',
						'rgba(156, 39, 176, 1)', 'rgba(153, 92, 0, 1)', 'rgba(255, 153, 0, 1)', 'rgba(147, 0, 4, 1)', 'rgba(229, 28, 33, 1)',
						'rgba(193, 191, 191, 1)' ];
				var highlights = [ 'rgba(1, 87, 154, 0.8)', 'rgba(33, 152, 243, 0.8)', 'rgba(0, 134, 9, 0.8)', 'rgba(13, 162, 23, 0.8)',
						'rgba(106, 1, 124, 0.8)', 'rgba(156, 39, 176, 0.8)', 'rgba(153, 92, 0, 0.8)', 'rgba(255, 153, 0, 0.8)', 'rgba(147, 0, 4, 0.8)',
						'rgba(229, 28, 33, 0.8)', 'rgba(193, 191, 191, 0.8)' ];
				// data sources
				var data = this.model.get('sandflies.source') || undefined;
				if (data) {
					for (var i = 0; i < data.length && colors.length; i++) {
						data[i].color = colors[i];
						data[i].highlight = highlights[i];
					}
					this.drawChart(data, 'sandflies-sources');
				}
				data = this.model.get('leishmania.source') || undefined;
				if (data) {
					for (var i = 0; i < data.length && colors.length; i++) {
						data[i].color = colors[i];
						data[i].highlight = highlights[i];
					}
					this.drawChart(data, 'leishmania-sources');
				}
				// genes
				data = this.model.get('sandflies.gene') || undefined;
				if (data) {
					for (var i = 0; i < data.length && colors.length; i++) {
						data[i].color = colors[i];
						data[i].highlight = highlights[i];
					}
					this.drawChart(data, 'sandflies-genes');
				}
				data = this.model.get('leishmania.gene') || undefined;
				if (data) {
					for (var i = 0; i < data.length && colors.length; i++) {
						data[i].color = colors[i];
						data[i].highlight = highlights[i];
					}
					this.drawChart(data, 'leishmania-genes');
				}
				// geo-referenced
				data = this.model.get('sandflies.gis') || undefined;
				if (data) {
					for (var i = 0; i < data.length; i++) {
						if ('Yes' === data[i].label) {
							data[i].color = bin_colors[0];
							data[i].highlight = bin_highlights[0];
						} else if ('No' === data[i].label) {
							data[i].color = bin_colors[1];
							data[i].highlight = bin_highlights[1];
						}
					}
					this.drawChart(data, 'sandflies-gis');
				}
				data = this.model.get('leishmania.gis') || undefined;
				if (data) {
					for (var i = 0; i < data.length; i++) {
						if ('Yes' === data[i].label) {
							data[i].color = bin_colors[0];
							data[i].highlight = bin_highlights[0];
						} else if ('No' === data[i].label) {
							data[i].color = bin_colors[1];
							data[i].highlight = bin_highlights[1];
						}
					}
					this.drawChart(data, 'leishmania-gis');
				}
			},
			drawChart : function(data, container) {
				require([ 'chartjs' ], function(Chart) {
					var options = {
						tooltipTemplate : "<%if (label){%><%=label%>: <%}%><%= value %>",
						tooltipFillColor : "rgba(255,255,255,0.8)",
						tooltipFontColor : "#000",
						animationEasing : "easeOutQuart",
						animateRotate : true,
						animateScale : true,
						animationSteps : 100
					};
					var helpers = Chart.helpers;
					var chart = new Chart(document.getElementById(container).getContext("2d")).Doughnut(data, options);
					var legendHolder = document.createElement('div');
					legendHolder.innerHTML = chart.generateLegend();
					helpers.each(legendHolder.firstChild.childNodes, function(legendNode, index) {
						helpers.addEvent(legendNode, 'mouseover', function() {
							var activeSegment = chart.segments[index];
							activeSegment.save();
							activeSegment.fillColor = activeSegment.highlightColor;
							chart.showTooltip([ activeSegment ]);
							activeSegment.restore();
						});
					});
					helpers.addEvent(legendHolder.firstChild, 'mouseout', function() {
						chart.draw();
					});
					chart.chart.canvas.parentNode.parentNode.appendChild(legendHolder.firstChild);
					var Chartjs = Chart.noConflict();
				});
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
	return Lvl.CollectionApp.Stats.View;
});