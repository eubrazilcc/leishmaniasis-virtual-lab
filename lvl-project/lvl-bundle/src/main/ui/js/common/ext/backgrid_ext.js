/**
 * RequireJS module that extends Backgrid with handy functions.
 */

define([ 'backbone', 'backgrid', 'underscore' ], function(Backbone, Backgrid, _) {
	'use strict';
	/**
	 * Convenient method to retrieve a list of identifiers selected across all pages. 
	 * This method only exists when the `backgrid_ext` extension has been included. 
	 * @member Backgrid.Grid
	 * @return {Array.<String>}
	 */
	Backgrid.Grid.prototype.getAllSelectedIds = function () {
		var selectAllHeaderCell;
		var headerCells = this.header.row.cells;
	    for (var i = 0, l = headerCells.length; i < l; i++) {
	      var headerCell = headerCells[i];
	      if (headerCell instanceof Backgrid.Extension.SelectAllHeaderCell) {
	        selectAllHeaderCell = headerCell;
	        break;
	      }
	    }
	    var keys = [];
	    if (selectAllHeaderCell) {
	    	_.each(selectAllHeaderCell.selectedModels, function(val, key) {
	    		if (key) keys.push({ id: key });
	    	});
	    }	    
	    return keys;
	};
});