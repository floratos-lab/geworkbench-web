
/* jquery.cytoscapeweb.layout.random.js */

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
	
	var defaults = {
		fit: true
	};
	
	function RandomLayout(){
		$.cytoscapeweb("debug", "Creating random layout with options");
	}
	
	RandomLayout.prototype.run = function(params){
		var options = $.extend(true, {}, defaults, params);
		var cy = params.cy;
		var nodes = cy.nodes();
		var edges = cy.edges();
		var container = cy.container();
		
		$.cytoscapeweb("debug", "Running random layout with options (%o)", params);
		
		var width = container.width();
		var height = container.height();
			
		$.cytoscapeweb("debug", "Random layout found (w, h, d) = (%i, %i)", width, height);
		
		nodes.positions(function(i, element){
			
			if( element.locked() ){
				return false;
			}

			return {
				x: Math.round( Math.random() * width ),
				y: Math.round( Math.random() * height )
			};
		});

		function exec(fn){
			if( fn != null && typeof fn == typeof function(){} ){
				fn();
			}
		}
		
		cy.trigger("layoutready");
		exec( params.ready );
		
		if( options.fit ){
			cy.fit();
		}
		
		cy.trigger("layoutstop");
		exec( params.stop );
	};
	
	$.cytoscapeweb("layout", "random", RandomLayout);
	
})(jQuery, jQuery.cytoscapeweb);
