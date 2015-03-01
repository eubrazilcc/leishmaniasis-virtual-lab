/**
 * RequireJS module that defines the view: collection->stats.
 */

define([ 'app', 'tpl!apps/collection/stats/templates/collection_stats', 'apps/config/marionette/styles/style' ], function(Lvl, StatsTpl, Style) {
	Lvl.module('CollectionApp.Stats.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		View.Content = Marionette.ItemView.extend({
			id : 'stats',
			template : StatsTpl,
			onBeforeRender : function() {
				require([ 'entities/styles' ], function() {
					var stylesLoader = new Style();
					stylesLoader.loadCss(Lvl.request('styles:chartist:entities').toJSON());
				});
			},
			onRender : function() {
				var data1 = [ {
					value : 300,
					color : "rgba(220,220,220,1)", // "#F7464A",
					highlight : "#FF5A5E",
					label : "Red"
				}, {
					value : 50,
					color : "#46BFBD",
					highlight : "#5AD3D1",
					label : "Green"
				}, {
					value : 100,
					color : "#FDB45C",
					highlight : "#FFC870",
					label : "Yellow"
				} ];
				var data2 = [ {
					value : 300,
					color : "#F7464A",
					highlight : "#FF5A5E",
					label : "Red"
				}, {
					value : 50,
					color : "#46BFBD",
					highlight : "#5AD3D1",
					label : "Green"
				} ];
				
				// TODO
				console.log(this.collection);
				// TODO
				
				var data3 = this.collection.get('sandflies.source')
				if (data3) {
					this.drawChart(data3, "lvl-chart3");
				}
				
				
				/* this.drawChart(data1, "lvl-chart1");
				this.drawChart(data2, "lvl-chart2");
				this.drawChart(data2, "lvl-chart3");
				this.drawChart(data1, "lvl-chart4"); */
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
				this.listenTo(this.collection, 'change', this.render);
				var self = this;
				self.collection.fetch({
					reset : true
				});
			}
		});
	});
	return Lvl.CollectionApp.Stats.View;
});