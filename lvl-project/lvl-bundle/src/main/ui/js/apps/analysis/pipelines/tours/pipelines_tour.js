/**
 * RequireJS module that defines the tour: analysis->pipelines->tour.
 */

define(
		[ 'app', 'hopscotch' ],
		function(Lvl, hopscotch) {
			return function() {				
				var tour = {
					id : 'analysis-pipelines',
					showPrevButton : false,
					skipIfNoElement : true,
					steps : [
							{
								target : document.querySelector('div#section-tab-list'),
								placement : 'bottom',
								fixedElement : true,
								title : 'This is the navigation menu',
								content : 'Use the links here to get around on the pipelines features.'
							},
							{
								target : document.querySelector('a#btnSearchToggle'),
								placement : 'left',
								fixedElement : true,
								title : 'This is the search button',
								content : 'Use it to search for items. <mark>Click</mark> on the search icon <i class="fa fa-search fa-fw"></i> to continue this tour.',
								showNextButton : false,
								nextOnTargetClick : true
							},
							{
								delay : 300,
								target : document.querySelector('input#lvl-search-form-input'),
								placement : 'bottom',
								fixedElement : true,
								title : 'This is the search box',
								content : 'It supports the <a href="/apidoc/" target="_blank">LvLQL</a> query language to search the pipelines. Enter <code>pipeline</code> in the input box and hit <kbd>enter</kbd>. Once the search is completed continue this tour.'
							},
							{
								target : document.querySelector('ul#lvl-search-terms-container'),
								placement : 'bottom',
								title : 'Your search terms are listed',
								content : 'Use the provided controls to clear/add search terms. Drag the icon <i class="fa fa-bookmark-o"></i><sub><i class="fa fa-plus-circle"></i></sub> to save your search terms.',
							},
							{
								target : document.querySelector('div#grid-container'),
								placement : 'top',
								yOffset : 100,
								title : 'This is the result dataset',
								content : 'Columns are sortable, rows are selectable. Additional controls are found at the right lateral side of the grid.'
							},
							{
								target : document.querySelector('button#lvl-toggle-toolbar-btn'),
								placement : 'left',
								fixedElement : true,
								title : 'This is the Tools menu',
								content : 'Use the controls here to perform additionl actions on the dataset and the selected items. <mark>Click</mark> on the menu icon <i class="fa fa-bars"></i> to continue this tour.',
								showNextButton : false,
								nextOnTargetClick : true
							}, {
								delay : 300,
								target : document.querySelector('div#lvl-floating-menu-toggle'),
								placement : 'left',
								xOffset : -10,
								fixedElement : true,
								title : 'Additional controls are displayed',
								content : 'Include deselecting all selected items.',
								onNext : function() {
									$('button#lvl-toggle-toolbar-btn').click();
								}
							}, {
								delay : 300,
								target : document.querySelector('button#lvl-feature-tour-btn'),
								placement : 'left',
								fixedElement : true,
								title : 'You\'re all set!',
								content : 'Run this tour as many times as you need.'
							} ]
				};
				hopscotch.startTour(tour);
			}
		});