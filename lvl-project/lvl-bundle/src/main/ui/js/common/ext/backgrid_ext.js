/**
 * RequireJS module that extends Backgrid with handy functions.
 */

define([ 'backbone', 'backgrid', 'underscore' ], function(Backbone, Backgrid, _) {
	'use strict';
	/**
	 * Helper method to retrieve the header cell with the select all function.
	 * @return {<Object>}
	 */
	Backgrid.Grid.prototype._getSelectAllHeaderCell = function () {
		var selectAllHeaderCell;
		var headerCells = this.header.row.cells;
	    for (var i = 0, l = headerCells.length; i < l; i++) {
	      var headerCell = headerCells[i];
	      if (headerCell instanceof Backgrid.Extension.SelectAllHeaderCell) {
	        selectAllHeaderCell = headerCell;
	        break;
	      }
	    }
	    return selectAllHeaderCell;
	};
	/**
	 * Convenient method to retrieve a list of identifiers selected across all pages. 
	 * This method only exists when the `backgrid_ext` extension has been included. 
	 * @member Backgrid.Grid
	 * @return {Array.<String>}
	 */
	Backgrid.Grid.prototype.getAllSelectedIds = function () {
		var selectAllHeaderCell = this._getSelectAllHeaderCell();
	    var keys = [];
	    if (selectAllHeaderCell) {
	    	_.each(selectAllHeaderCell.selectedModels, function(val, key) {
	    		if (key) keys.push({ id: key });
	    	});
	    }
	    return keys;
	};
	Backgrid.Grid.prototype.addSelectedIds = function ( idArr ) {
		if ($.isArray( idArr )) {
			var selectAllHeaderCell = this._getSelectAllHeaderCell();
			_.each(idArr, function(id) {
				selectAllHeaderCell.selectedModels[id] = 1;				
			});
			// console.log('Selected items: ', _.keys(selectAllHeaderCell.selectedModels).length);
		}
	};
	Backgrid.Grid.prototype.deselectAll = function () {
		var selectAllHeaderCell = this._getSelectAllHeaderCell();
	    _.each(_.keys(selectAllHeaderCell.selectedModels), function(id) {
	    	delete selectAllHeaderCell.selectedModels[id];
		});
	};
});