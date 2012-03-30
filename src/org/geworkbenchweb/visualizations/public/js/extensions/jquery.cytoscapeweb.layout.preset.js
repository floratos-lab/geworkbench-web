
/* jquery.cytoscapeweb.layout.preset.js */

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
	
	function PresetLayout(){
		$.cytoscapeweb("debug", "Creating preset layout with options");
	}
	
	PresetLayout.prototype.run = function(params){
		var options = $.extend(true, {}, defaults, params);
		$.cytoscapeweb("debug", "Running preset layout with options (%o)", options);

		var cy = params.cy;
		var nodes = cy.nodes();
		var edges = cy.edges();
		var container = cy.container();
		
		function getPosition(node){
			if( options.positions == null ){
				return null;
			}
			
			if( options.positions[node._private.data.id] == null ){
				return null;
			}
			
			return options.positions[node._private.data.id];
		}
		
		nodes.positions(function(i, node){
			
			var position = getPosition(node);
			
			if( node.locked() || position == null ){
				return false;
			}
			
			return position;
			
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
	
	$.cytoscapeweb("layout", "preset", PresetLayout);
	
	function PresetExporter(options){
		this.options = options;
		this.cy = options.cy;
		this.renderer = options.renderer;
	}
	
	PresetExporter.prototype.run = function(){
		var elements = {};
		
		this.cy.elements().each(function(i, ele){
			elements[ ele.data("id") ] = ele.position();
		});
		
		return elements;
	};
	
	$.cytoscapeweb("exporter", "preset", PresetExporter);
	
	
})(jQuery, jQuery.cytoscapeweb);
