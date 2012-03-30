
/* jquery.cytoscapeweb.layout.null.js */

/**
 * This file is part of Cytoscape Web 2.0-prerelease-snapshot-2012.03.28-13.12.12.
 * 
 * Cytoscape Web is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * Cytoscape Web is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with
 * Cytoscape Web. If not, see <http://www.gnu.org/licenses/>.
 */
 
;(function($, $$){
		
	function NullLayout(){
		$.cytoscapeweb("debug", "Creating null layout");
	}
	
	// puts all nodes at (0, 0)
	NullLayout.prototype.run = function(params){
		$.cytoscapeweb("debug", "Running null layout with options (%o)", params);
		
		var cy = params.cy;
		
		cy.nodes().positions(function(){
			return {
				x: 0,
				y: 0
			};
		});
		
		function exec(fn){
			if( fn != null && typeof fn == typeof function(){} ){
				fn();
			}
		}
		
		cy.trigger("layoutready");
		exec( params.ready );
		
		cy.trigger("layoutstop");
		exec( params.stop );
	};
	
	$.cytoscapeweb("layout", "null", NullLayout);
	
})(jQuery, jQuery.cytoscapeweb);
