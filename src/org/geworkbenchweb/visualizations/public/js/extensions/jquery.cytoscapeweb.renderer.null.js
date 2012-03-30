
/* jquery.cytoscapeweb.renderer.null.js */

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
		
	function NullRenderer(options){
		$.cytoscapeweb("debug", "Creating null renderer with options (%o)", options);
	}
	
	NullRenderer.prototype.notify = function(params){
		$.cytoscapeweb("debug", "Notify null renderer with params (%o)", params);
	};
	
	NullRenderer.prototype.zoom = function(params){
		$.cytoscapeweb("debug", "Zoom null renderer with params (%o)", params);
	};
	
	NullRenderer.prototype.fit = function(params){
		$.cytoscapeweb("debug", "Fit null renderer with params (%o)", params);
	};
	
	NullRenderer.prototype.pan = function(params){
		$.cytoscapeweb("debug", "Pan null renderer with params (%o)", params);
	};
	
	NullRenderer.prototype.panBy = function(params){
		$.cytoscapeweb("debug", "Relative pan null renderer with params (%o)", params);
	};
	
	NullRenderer.prototype.showElements = function(element){
		element.collection().each(function(){
			this._private.visible = true;
		});
	};
	
	NullRenderer.prototype.hideElements = function(element){
		element.collection().each(function(){
			this._private.visible = false;
		});
	};
	
	NullRenderer.prototype.elementIsVisible = function(element){
		return element._private.visible;
	};
	
	NullRenderer.prototype.renderedDimensions = function(){
		return {};
	};
	
	$.cytoscapeweb("renderer", "null", NullRenderer);
	
})(jQuery, jQuery.cytoscapeweb);
