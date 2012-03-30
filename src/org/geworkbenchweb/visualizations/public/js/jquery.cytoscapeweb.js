
/* jquery.cytoscapeweb.js */

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
 

;(function($){
	
	// make the jQuery plugin grab what we define init to be later
	$.cytoscapeweb = function(){
		return $.cytoscapeweb.init.apply(this, arguments);
	};
	
	// define the function namespace here, since it has members in many places
	$.cytoscapeweb.fn = {};
	
})(jQuery);

;(function($, $$){
	
	$$.is = {
		string: function(obj){
			return obj != null && typeof obj == typeof "";
		},
		
		fn: function(obj){
			return obj != null && typeof obj == typeof function(){};
		},
		
		array: function(obj){
			return obj != null && obj instanceof Array;
		},
		
		plainObject: function(obj){
			return obj != null && typeof obj == typeof {} && !$$.is.array(obj);
		},
		
		number: function(obj){
			return obj != null && typeof obj == typeof 1 && !isNaN(obj);
		},
		
		color: function(obj){
			return obj != null && typeof obj == typeof "" && $.Color(obj).toString() != "";
		},
		
		bool: function(obj){
			return obj != null && typeof obj == typeof true;
		},
		
		elementOrCollection: function(obj){
			return $$.is.element(obj) || $$.is.collection(obj);
		},
		
		element: function(obj){
			return obj instanceof $$.CyElement;
		},
		
		collection: function(obj){
			return obj instanceof $$.CyCollection;
		},
		
		emptyString: function(obj){
			if( obj == null ){ // null is empty
				return true; 
			} else if( $$.is.string(obj) ){
				return obj.match(/^\s+$/) != null; // all whitespace is empty
			}
			
			return false; // otherwise, we don't know what we've got
		},
		
		nonemptyString: function(obj){
			return obj != null && $$.is.string(obj) && obj.match(/^\s+$/) == null;
		}
	};	
	
})(jQuery, jQuery.cytoscapeweb);

;(function($, $$){
	
	$$.util = {
			
		// gets a deep copy of the argument
		copy: function( obj ){
			if( obj == null ){
				return obj;
			} if( $$.is.array(obj) ){
				return $.extend(true, [], obj);
			} else if( $$.is.plainObject(obj) ){
				return $.extend(true, {}, obj);
			} else {
				return obj;
			}
		},
		
		// sets the value in a map (map may not be built)
		setMap: function( options ){
			var obj = options.map;
			
			$.each(options.keys, function(i, key){
				if( i < options.keys.length - 1 ){
					
					// extend the map if necessary
					if( obj[key] == null ){
						obj[key] = {};
					}
					
					obj = obj[key];
				} else {
					// set the value
					obj[key] = options.value;
				}
			});
		},
		
		// gets the value in a map even if it's not built in places
		getMap: function( options ){
			var obj = options.map;
			
			for(var i = 0; i < options.keys.length; i++){
				var key = options.keys[i];
				obj = obj[key];
				
				if( obj == null ){
					return obj;
				}
			}
			
			return obj;
		},
		
		capitalize: function(str){
			if( $$.is.emptyString(str) ){
				return str;
			}
			
			return str.charAt(0).toUpperCase() + str.substring(1);
		}
			
	};
	
})(jQuery, jQuery.cytoscapeweb);

;(function($, $$){
	
	var quiet = false;
	var debug = false;
	
	var console = $$.console = {
		option: function(name, val){
			if( name == quiet ){
				return quiet = ( val === undefined ? quiet : val );
			} else if( name == debug ){
				return debug = ( val === undefined ? debug : val );
			}
		},
			
		debug: function(){
			if( quiet || !debug || $.browser.msie ){ return; }
			
			if( window.console != null && window.console.debug != null ){
				window.console.debug.apply(window.console, arguments);
			} else if( window.console != null && window.console.log != null ){
				window.console.log.apply(window.console, arguments);
			}
		},
			
		log: function(){
			if( quiet || $.browser.msie ){ return; }
			
			if( window.console != null && window.console.log != null ){
				window.console.log.apply(window.console, arguments);
			}
		},
		
		warn: function(){
			if( quiet || $.browser.msie ){ return; }
			
			if( window.console != null && window.console.warn != null ){
				window.console.warn.apply(window.console, arguments);
			} else {
				console.log.apply(window.console, arguments);
			}
		},
		
		error: function(){
			if( quiet || $.browser.msie ){ return; }
			
			if( window.console != null && window.console.error != null ){
				window.console.error.apply(window.console, arguments);
				
				if( window.console.trace != null ){
					window.console.trace();
				}
				
			} else {
				console.log.apply(window.console, arguments);
				throw "Cytoscape Web encountered the previously logged error";
				
				if( window.console.trace != null ){
					window.console.trace();
				}
			}
		}
	};
	
})(jQuery, jQuery.cytoscapeweb);

;(function($, $$){
	
	// registered extensions to cytoweb, indexed by name
	var extensions = {};
	$$.extensions = extensions;
	
	// registered modules for extensions, indexed by name
	var modules = {};
	$$.modules = modules;
	
	function setExtension(type, name, registrant){
		var impl = {};
		impl[name] = registrant;
		
		switch( type ){
		case "core":
		case "collection":
			$$.fn[type]( impl );
		}
		
		return $$.util.setMap({
			map: extensions,
			keys: [ type, name ],
			value: registrant
		});
	}
	
	function getExtension(type, name){
		return $$.util.getMap({
			map: extensions,
			keys: [ type, name ]
		});
	}
	
	function setModule(type, name, moduleType, moduleName, registrant){
		return $$.util.setMap({
			map: modules,
			keys: [ type, name, moduleType, moduleName ],
			value: registrant
		});
	}
	
	function getModule(type, name, moduleType, moduleName){
		return $$.util.getMap({
			map: modules,
			keys: [ type, name, moduleType, moduleName ]
		});
	}
	
	$$.extension = function(){
		// e.g. $$.extension("renderer", "svg")
		if( arguments.length == 2 ){
			return getExtension.apply(this, arguments);
		}
		
		// e.g. $$.extension("renderer", "svg", { ... })
		else if( arguments.length == 3 ){
			return setExtension.apply(this, arguments);
		}
		
		// e.g. $$.extension("renderer", "svg", "nodeShape", "ellipse")
		else if( arguments.length == 4 ){
			return getModule.apply(this, arguments);
		}
		
		// e.g. $$.extension("renderer", "svg", "nodeShape", "ellipse", { ... })
		else if( arguments.length == 5 ){
			return setModule.apply(this, arguments);
		}
		
		else {
			$$.console.error("Invalid extension access syntax");
		}
	
	};
	
})(jQuery, jQuery.cytoscapeweb);

;(function($, $$){
	
	// allow calls on a jQuery selector by proxying calls to $.cytoscapeweb
	// e.g. $("#foo").cytoscapeweb(options) => $.cytoscapeweb(options) on #foo
	$.fn.cytoscapeweb = function(opts){
		
		// get object
		if( opts == "get" ){
			var data = $(this).data("cytoscapeweb");
			return data.cy;
		}
		
		// bind to ready
		else if( $$.is.fn(opts) ){
			var ready = opts;
			var data = $(this).data("cytoscapeweb");
			
			if( data != null && data.cy != null && data.ready ){
				// already ready so just trigger now
				ready.apply(data.cy, []);
			} else {
				// not yet ready, so add to readies list
				
				if( data == null ){
					data = {}
				}
				
				if( data.readies == null ){
					data.readies = [];
				}
				
				data.readies.push(ready);
				$(this).data("cytoscapeweb", data);
			} 
			
		}
		
		// proxy to create instance
		else if( $$.is.plainObject(opts) ){
			return $(this).each(function(){
				var options = $.extend({}, opts, {
					container: $(this)
				});
			
				$.cytoscapeweb(options);
			});
		}
		
		// proxy a function call
		else {
			var rets = [];
			var args = [];
			for(var i = 1; i < arguments.length; i++){
				args[i - 1] = arguments[i];
			}
			
			$(this).each(function(){
				var data = $(this).data("cytoscapeweb");
				var cy = data.cy;
				var fnName = opts;
				
				if( cy != null && $$.is.fn( cy[fnName] ) ){
					var ret = cy[fnName].apply(cy, args);
					rets.push(ret);
				}
			});
			
			// if only one instance, don't need to return array
			if( rets.length == 1 ){
				rets = rets[0];
			} else if( rets.length == 0 ){
				rets = $(this);
			}
			
			return rets;
		}

	};
	
	// allow functional access to cytoweb
	// e.g. var cytoweb = $.cytoscapeweb({ selector: "#foo", ... });
	//      var nodes = cytoweb.nodes();
	$$.init = function( options ){
		
		// create instance
		if( $$.is.plainObject( options ) ){
			return new $$.CyCore( options );
		} 
		
		// allow for registration of extensions
		// e.g. $.cytoscapeweb("renderer", "svg", SvgRenderer);
		// e.g. $.cytoscapeweb("renderer", "svg", "nodeshape", "ellipse", SvgEllipseNodeShape);
		// e.g. $.cytoscapeweb("core", "doSomething", function(){ /* doSomething code */ });
		// e.g. $.cytoscapeweb("collection", "doSomething", function(){ /* doSomething code */ });
		else if( $$.is.string( options ) ) {
			return $$.extension.apply($$.extension, arguments);
		}
	};
	
	// use short alias (cy) if not already defined
	if( $.fn.cy == null && $.cy == null ){
		$.fn.cy = $.fn.cytoscapeweb;
		$.cy = $.cytoscapeweb;
	}
	
})(jQuery, jQuery.cytoscapeweb);

;(function($, $$){
	
	$$.fn.core = function( impl, options ){
		$.each(impl, function(name, fn){
			CyCore.prototype[ name ] = fn;
		});
	};
	
	function CyCore( opts ){
		var cy = this;
		
		var defaults = {
			layout: {
				name: "grid"
			},
			renderer: {
				name: "svg"
			},
			style: { // actual default style later specified by renderer
			}
		};
		
		var options = $.extend(true, {}, defaults, opts);
		
		if( options.container == null ){
			$$.console.error("Cytoscape Web must be called on an element; specify `container` in options or call on selector directly with jQuery, e.g. $('#foo').cy({...});");
			return;
		} else if( $(options.container).size() > 1 ){
			$$.console.error("Cytoscape Web can not be called on multiple elements in the functional call style; use the jQuery selector style instead, e.g. $('.foo').cy({...});");
			return;
		}
		
		this._private = {
			options: options, // cached options
			style: options.style,
			nodes: {}, // id => node object
			edges: {}, // id => edge object
			continuousMapperBounds: { // data attr name => { min, max }
				nodes: {},
				edges: {}
			},
			continuousMapperUpdates: [],
			live: {}, // event name => array of callback defns
			selectors: {}, // selector string => selector for live
			listeners: {}, // cy || background => event name => array of callback functions
			animation: { 
				// normally shouldn't use collections here, but animation is not related
				// to the functioning of CySelectors, so it's ok
				elements: null // elements queued or currently animated
			},
			scratch: {}, // scratch object for core
			layout: null,
			renderer: null,
			notificationsEnabled: true, // whether notifications are sent to the renderer
			zoomEnabled: true,
			panEnabled: true
		};

		cy.initRenderer( options.renderer );
		
		// initial load
		cy.load(options.elements, function(){ // onready
			var data = $(options.container).data("cytoscapeweb");
			
			if( data == null ){
				data = {};
			}
			data.cy = cy;
			data.ready = true;
			
			if( data.readies != null ){
				$.each(data.readies, function(i, ready){
					cy.bind("ready", ready);
				});
				
				data.readies = [];
			}
			
			$(options.container).data("cytoscapeweb", data);
			
			cy.startAnimationLoop();
			
			if( $$.is.fn( options.ready ) ){
				options.ready.apply(cy, [cy]);
			}
			
			cy.trigger("ready");
		}, function(){ // ondone
			if( $$.is.fn( options.done ) ){
				options.done.apply(cy, [cy]);
			}
			
			cy.trigger("done");
		});
	}
	$$.CyCore = CyCore; // expose
	
	$$.fn.core({
		container: function(){
			return $( this._private.options.container );
		}
	});
	
})(jQuery, jQuery.cytoscapeweb);

(function($, $$){
	
	$$.fn.core({
		add: function(opts){
			
			var elements = [];
			
			this.noNotifications(function(){
				
				// add the element
				if( $$.is.element(opts) ){
					var element = opts;
					elements.push(element);
					
					element.restore();
				}
				
				// add the collection
				else if( $$.is.collection(opts) ){
					var collection = opts;
					collection.each(function(i, ele){
						elements.push(ele);
					});
					
					collection.restore();
				} 
				
				// specify an array of options
				else if( $$.is.array(opts) ){
					$.each(opts, function(i, elementParams){
						elements.push(new $$.CyElement( cy, elementParams ));
					});
				}
				
				// specify via opts.nodes and opts.edges
				else if( $$.is.plainObject(opts) && ($$.is.array(opts.nodes) || $$.is.array(opts.edges)) ){
					$.each(["nodes", "edges"], function(i, group){
						if( $$.is.array(opts[group]) ){
							$.each(opts[group], function(i, eleOpts){
								elements.push(new $$.CyElement( cy, $.extend({}, eleOpts, { group: group }) ));
							});
						} 
					});
				}
				
				// specify options for one element
				else {
					elements.push(new $$.CyElement( cy, opts ));
				}
			});
			
			this.notify({
				type: "add",
				collection: elements
			});
			
			
			return new $$.CyCollection( cy, elements );
		},
		
		remove: function(collection){
			if( !$$.is.elementOrCollection(collection) ){
				collection = collection;
			} else if( $$.is.string(collection) ){
				var selector = collection;
				collection = this.$( selector );
			}
			
			return collection.remove();
		},
		
		load: function(elements, onload, ondone){
			var cy = this;
			
			// remove old elements
			cy.elements().remove();

			cy.notifications(false);
			
			if( elements != null ){
				if( $$.is.plainObject(elements) ){
					$.each(["nodes", "edges"], function(i, group){

						var elementsInGroup = elements[group];
						if( elementsInGroup != null ){
							$.each(elementsInGroup, function(i, params){
								var element = new $$.CyElement( cy, $.extend({}, params, { group: group }) );
							});
						}
					});
				} else if( $$.is.array(elements) ){
					$.each(elements, function(i, params){
						var element = new $$.CyElement( cy, params );
					});
				}

			}
			
			function callback(){
				var layoutReady = cy._private.options.layout.ready;
				var layoutStop = cy._private.options.layout.stop;
				
				cy.layout( $.extend({}, cy._private.options.layout, {
					ready: function(){
						cy.notifications(true);

						cy.notify({
							type: "load",
							collection: cy.elements(),
							style: cy._private.style
						});

						if( $$.is.fn( layoutReady ) ){
							layoutReady.apply(cy, [cy]);
						}
						if( $$.is.fn(onload) ){
							onload.apply(cy, [cy]);
						}
						cy.trigger("load");
						cy.trigger("layoutready");
					},
					stop: function(){
						if( $$.is.fn( layoutStop ) ){
							layoutStop.apply(cy, [cy]);
						}
						if( $$.is.fn(ondone) ){
							ondone.apply(cy, [cy]);
						}
						cy.trigger("layoutdone");
					}
				}) );


			}

			// TODO remove timeout when chrome reports dimensions onload properly
			// only affects when loading the html from localhost, i think...
			if( window.chrome ){
				setTimeout(function(){
					callback();
				}, 30);
			} else {
				callback();
			}

			return this;
		}
	});
	
})(jQuery, jQuery.cytoscapeweb);

;(function($, $$){
	
	$$.fn.core({
		
		startAnimationLoop: function(){
			var cy = this;
			var structs = this._private;
			var stepDelay = 10;
			var useTimeout = false;
			var useRequestAnimationFrame = true;
			
			// initialise the list
			structs.animation.elements = new $$.CyCollection( cy );
			
			// TODO change this when standardised
			var requestAnimationFrame = window.requestAnimationFrame || window.mozRequestAnimationFrame ||  
				window.webkitRequestAnimationFrame || window.msRequestAnimationFrame;
			
			if( requestAnimationFrame == null || !useRequestAnimationFrame ){
				requestAnimationFrame = function(fn){
					window.setTimeout(function(){
						fn(+new Date);
					}, stepDelay);
				};
			}
			
			var containerDom = cy.container()[0];
			
			function globalAnimationStep(){
				function exec(){
					requestAnimationFrame(function(now){
						handleElements(now);
						globalAnimationStep();
					}, containerDom);
				}
				
				if( useTimeout ){
					setTimeout(function(){
						exec();
					}, stepDelay);
				} else {
					exec();
				}
				
			}
			
			globalAnimationStep(); // first call
			
			function handleElements(now){
				
				structs.animation.elements.each(function(i, ele){
					
					// we might have errors if we edit animation.queue and animation.current
					// for ele (i.e. by stopping)
					try{
						ele = ele.element(); // make sure we've actually got a CyElement
						var current = ele._private.animation.current;
						var queue = ele._private.animation.queue;
						
						// if nothing currently animating, get something from the queue
						if( current.length == 0 ){
							var q = queue;
							var next = q.length > 0 ? q.shift() : null;
							
							if( next != null ){
								next.callTime = +new Date; // was queued, so update call time
								current.push( next );
							}
						}
						
						// step all currently running anis
						$.each(current, function(i, ani){
							step( ele, ani, now );
						});
						
						// remove done anis in current
						var completes = [];
						for(var i = 0; i < current.length; i++){
							if( current[i].done ){
								completes.push( current[i].params.complete );
								
								current.splice(i, 1);
								i--;
							}
						}
						
						// call complete callbacks
						$.each(completes, function(i, fn){
							if( $$.is.fn(complete) ){
								complete.apply( ele );
							}
						});
						
					} catch(e){
						// do nothing
					}
					
				}); // each element
				
				
				// notify renderer
				if( structs.animation.elements.size() > 0 ){
					cy.notify({
						type: "draw",
						collection: structs.animation.elements
					});
				}
				
				// remove elements from list of currently animating if its queues are empty
				structs.animation.elements = structs.animation.elements.filter(function(){
					var ele = this;
					var queue = ele._private.animation.queue;
					var current = ele._private.animation.current;
					
					return current.length > 0 || queue.length > 0;
				});
			} // handleElements
				
			function step( self, animation, now ){
				var properties = animation.properties;
				var params = animation.params;
				var startTime = animation.callTime;
				var percent;
				
				if( params.duration == 0 ){
					percent = 1;
				} else {
					percent = Math.min(1, (now - startTime)/params.duration);
				}
				
				function update(p){
					if( p.end != null ){
						var start = p.start;
						var end = p.end;
						
						// for each field in end, update the current value
						$.each(end, function(name, val){
							if( valid(start[name], end[name]) ){
								self._private[p.field][name] = ease( start[name], end[name], percent );
							}
						});					
					}
				}
				
				if( properties.delay == null ){
					update({
						end: properties.position,
						start: animation.startPosition,
						field: "position"
					});
					
					update({
						end: properties.bypass,
						start: animation.startStyle,
						field: "bypass"
					});
				}
				
				if( $$.is.fn(params.step) ){
					params.step.apply( self, [ now ] );
				}
				
				if( percent >= 1 ){
					animation.done = true;
				}
				
				return percent;
			}
			
			function valid(start, end){
				if( start == null || end == null ){
					return false;
				}
				
				if( $$.is.number(start) && $$.is.number(end) ){
					return true;
				} else if( (start) && (end) ){
					return true;
				}
				
				return false;
			}
			
			function ease(start, end, percent){
				if( $$.is.number(start) && $$.is.number(end) ){
					return start + (end - start) * percent;
				} else if( (start) && (end) ){
					var c1 = $.Color(start).fix().toRGB();
					var c2 = $.Color(end).fix().toRGB();

					function ch(ch1, ch2){
						var diff = ch2 - ch1;
						var min = ch1;
						return Math.round( percent * diff + min );
					}
					
					var r = ch( c1.red(), c2.red() );
					var g = ch( c1.green(), c2.green() );
					var b = ch( c1.blue(), c2.blue() );
					
					return $.Color([r, g, b], "RGB").toHEX().toString();
				}
				
				return undefined;
			}
			
		}
		
	});
	
})(jQuery, jQuery.cytoscapeweb);


	
		

;(function($, $$){
	
	$$.fn.core({	
		one: defineBind({
			target: "cy",
			one: true
		}),
		
		bind: defineBind({
			target: "cy"
		}),
		
		unbind: defineUnbind({
			target: "cy"
		}),
		
		trigger: defineTrigger({
			target: "cy"
		}),
		
		delegate: function(selector, events, data, handler){
			this.$(selector).live(events, data, handler);
			
			return this;
		},
		
		undelegate: function(selector, events, handler){
			this.$(selector).die(events, handler);
			
			return this;
		},
		
		on: function( events, selector, data, handler ){
			if( $$.is.string(selector) ){
				this.$(selector).live(events, data, handler);
			} else {
				selector = undefined;
				data = selector;
				handler = data;
				
				this.bind(events, data, handler);
			}
			
			return this;
		},
		
		off: function(event, selector, handler){
			
			if( $$.is.string(selector) ){
				this.$(selector).live(events, handler);
			} else {
				handler = selector;
				selector = undefined;
				
				this.unbind(events, handler);
			}
				
			return this;
		},
		
		background: function(){
			var cy = this;
			
			if( cy._private.background == null ){
				var fns = ["on", "off", "bind", "unbind", "one", "trigger"];
				
				cy._private.background = {};
				$.each(fns, function(i, fnName){
					cy._private.background[fnName] = function(){
						return cy["bg" + $$.util.capitalize(fnName)].apply(cy, arguments);
					};
				});
			}
			
			return cy._private.background;
		},
		
		bgOne: defineBind({
			target: "bg",
			one: true
		}),
		
		bgOn: defineBind({
			target: "bg"
		}),
		
		bgOff: defineUnbind({
			target: "bg"
		}),
		
		bgBind: defineBind({
			target: "bg"
		}),
		
		bgUnbind: defineUnbind({
			target: "bg"
		}),
		
		bgTrigger: defineTrigger({
			target: "bg"
		})
		
	});
	
	function defineBind( params ){
		var defaults = {
			target: "cy",
			one: false
		};
		params = $.extend( {}, defaults, params );
		
		return function(events, data, handler){
			var cy = this;
			var listeners = cy._private.listeners;
			
			if( handler === undefined ){
				handler = data;
				data = undefined;
			}
			
			events = events.split(/\s+/);
			$.each(events, function(i, event){
				if( $$.is.emptyString(event) ){ return; }
				
				if( listeners[ params.target ] == null ){
					listeners[ params.target ] = {};
				}
				
				if( listeners[ params.target ][ event ] == null ){
					listeners[ params.target ][ event ] = [];
				}
				
				listeners[ params.target ][ event ].push({
					callback: handler,
					data: data,
					one: params.one
				});
			});
			
			return cy;
		};
	};

	function defineUnbind( params ){
		var defaults = {
			target: "cy"
		};
		params = $.extend({}, defaults, params);
		
		return function(events, handler){
			var cy = this;
			var listeners = cy._private.listeners;
			
			if( listeners[ params.target ] == null ){
				return;
			}
			
			events = events.split(/\s+/);
			$.each(events, function(i, event){
				if( $$.is.emptyString(event) ){ return; }
				
				// unbind all
				if( handler === undefined ){
					delete listeners[ params.target ][ event ];
					return;
				}
				
				// unbind specific handler
				else {
					for(var i = 0; i < listeners[params.target][event].length; i++){
						var listener = listeners[params.target][event][i];
						
						if( listener.callback == handler ){
							listeners[params.target][event].splice(i, 1);
							i--;
						}
					}
				}
			});
			
			return cy;
		}
	}
	
	function defineTrigger( params ){
		var defaults = {
			target: "cy"
		};
		params = $.extend( {}, defaults, params );
		
		return function( event, data ){
			var cy = this;
			var listeners = cy._private.listeners;
			var types;
			var target = params.target;
			
			if( $$.is.plainObject(event) ){
				types = [ event.type ];
			} else {
				types = event.split(/\s+/);
			}
			
			$.each(types, function(t, type){
				if( listeners[target] == null || listeners[target][type] == null ){
					return;
				}
				
				for(var i = 0; i < listeners[target][type].length; i++){
					var listener = listeners[target][type][i];
					
					var eventObj;
					if( $$.is.plainObject(event) ){
						eventObj = event;
						event = eventObj.type;
					} else {
						eventObj = $.Event(event);
					}
					eventObj.data = listener.data;
					eventObj.cy = eventObj.cytoscapeweb = cy;
					
					var args = [ eventObj ];
					
					if( data != null ){
						$.each(data, function(i, arg){
							args.push(arg);
						});
					}
					
					if( listener.one ){
						listeners[target][type].splice(i, 1);
						i--;
					}
					
					listener.callback.apply(cy, args);
				}
			});
		}
	}
		
})(jQuery, jQuery.cytoscapeweb);

;(function($, $$){
	
	$$.fn.core({
		
		exportTo: function(params){
			var format = params.name;
			var exporterDefn = $$.extension("exporter", format);
			
			if( exporterDefn == null ){
				$$.console.error("No exporter with name `%s` found; did you remember to register it?", format);
			} else {
				var exporter = new exporterDefn({
					cy: cy,
					renderer: this.renderer()
				});
				
				return exporter.run();
			}
		}
		
	});	
	
})(jQuery, jQuery.cytoscapeweb);

;(function($, $$){
	
	$$.fn.core({
		
		layout: function( params ){
			var cy = this;
			
			// if no params, use the previous ones
			if( params == null ){
				params = this._private.options.layout;
			}
			
			this.initLayout( params );
			
			cy.trigger("layoutstart");
			
			this._private.layout.run( $.extend({}, params, {
				renderer: cy._private.renderer,
				cy: cy
			}) );
			
			return this;
			
		},
		
		initLayout: function( options ){
			if( options == null ){
				$$.console.error("Layout options must be specified to run a layout");
				return;
			}
			
			if( options.name == null ){
				$$.console.error("A `name` must be specified to run a layout");
				return;
			}
			
			var name = options.name;
			var layoutProto = $$.extension("layout", name);
			
			if( layoutProto == null ){
				$$.console.error("Can not apply layout: No such layout `%s` found; did you include its JS file?", name);
				return;
			}
			
			this._private.layout = new layoutProto();
			this._private.options.layout = options; // save options
		}
		
	});
	
})(jQuery, jQuery.cytoscapeweb);

(function($, $$){
	
	$$.fn.core({
		notify: function( params ){
			if( !this._private.notificationsEnabled ){ return; } // exit on disabled
			
			var renderer = this.renderer();
			
			if( $$.is.element(params.collection) ){
				var element = params.collection;
				params.collection = new $$.CyCollection(cy, [ element ]);	
			
			} else if( $$.is.array(params.collection) ){
				var elements = params.collection;
				params.collection = new $$.CyCollection(cy, elements);	
			} 
			
			if( this.getContinuousMapperUpdates().length != 0 ){
				params.updateMappers = true;
				this.clearContinuousMapperUpdates();
			}
			
			renderer.notify(params);
		},
		
		notifications: function( bool ){
			var p = this._private;
			
			if( bool === undefined ){
				return p.notificationsEnabled;
			} else {
				p.notificationsEnabled = bool ? true : false;
			}
		},
		
		noNotifications: function( callback ){
			this.notifications(false);
			callback();
			this.notifications(true);
		}
	});
	
})(jQuery, jQuery.cytoscapeweb);

;(function($, $$){
	
	$$.fn.core({
		
		renderer: function(){
			return this._private.renderer;
		},
		
		initRenderer: function( options ){
			var cy = this;
			
			var rendererProto = $$.extension("renderer", options.name);
			if( rendererProto == null ){
				$$.console.error("Can not initialise: No such renderer `$s` found; did you include its JS file?", options.name);
				return;
			}
			
			this._private.renderer = new rendererProto( $.extend({}, options, {
				cy: cy,
				style: cy._private.style,
				
				styleCalculator: {
					calculate: function(element, styleVal){

						if( $$.is.plainObject(styleVal) ){
							
							var ret;
							
							if( styleVal.customMapper != null ){
								
								ret = styleVal.customMapper.apply( element, [ element.data() ] );
								
							} else if( styleVal.passthroughMapper != null ){
								
								var attrName = styleVal.passthroughMapper;
								ret = element._private.data[attrName];
								
							} else if( styleVal.discreteMapper != null ){
								
								var attrName = styleVal.discreteMapper.attr;
								var entries = styleVal.discreteMapper.entries;
								var elementVal = element.data(attrName);
								
								$.each(entries, function(i, entry){
									var attrVal = entry.attrVal;
									var mappedVal = entry.mappedVal;
									
									if( attrVal == elementVal ){
										ret = mappedVal;
									}
								});
								
							} else if( styleVal.continuousMapper != null ){
								
								var map = styleVal.continuousMapper;
								
								if( map.attr.name == null || typeof map.attr.name != typeof "" ){
									$$.console.error("For style.%s.%s, `attr.name` must be defined as a string since it's a continuous mapper", element.group(), styleName);
									return;
								}
								
								var attrBounds = cy._private.continuousMapperBounds[element._private.group][map.attr.name];
								attrBounds = {
									min: attrBounds == null ? 0 : attrBounds.min,
									max: attrBounds == null ? 0 : attrBounds.max
								};
								
								// use defined attr min & max if set in mapper
								if( map.attr.min != null ){
									attrBounds.min = map.attr.min;
								}
								if( map.attr.max != null ){
									attrBounds.max = map.attr.max;
								}
								
								if( attrBounds != null ){
								
									var data = element.data(map.attr.name);
									var percent = ( data - attrBounds.min ) / (attrBounds.max - attrBounds.min);
									
									if( attrBounds.max == attrBounds.min ){
										percent = 1;
									}
									
									if( percent > 1 ){
										percent = 1;
									} else if( percent < 0 || data == null || isNaN(percent) ){
										percent = 0;
									}
									
									if( data == null && styleVal.defaultValue != null ){
										ret = styleVal.defaultValue;
									} else if( $$.is.number(map.mapped.min) && $$.is.number(map.mapped.max) ){
										ret = percent * (map.mapped.max - map.mapped.min) + map.mapped.min;
									} else if( (map.mapped.min) && (map.mapped.max) ){
										
										var cmin = $.Color(map.mapped.min).fix().toRGB();
										var cmax = $.Color(map.mapped.max).fix().toRGB();

										var red = Math.round( cmin.red() * (1 - percent) + cmax.red() * percent );
										var green  = Math.round( cmin.green() * (1 - percent) + cmax.green() * percent );
										var blue  = Math.round( cmin.blue() * (1 - percent) + cmax.blue() * percent );

										ret = $.Color([red, green, blue], "RGB").toHEX().toString();
									} else {
										$$.console.error("Unsupported value used in mapper for `style.%s.%s` with min mapped value `%o` and max `%o` (neither number nor colour)", element.group(), map.styleName, map.mapped.min, map.mapped.max);
										return;
									}
								} else {
									$$.console.error("Attribute values for `%s.%s` must be numeric for continuous mapper `style.%s.%s` (offending %s: `%s`)", element.group(), map.attr.name, element.group(), styleName, element.group(), element.data("id"));
									return;
								}
								
							} // end if
							
							var defaultValue = styleVal.defaultValue;
							if( ret == null ){
								ret = defaultValue;
							}
							
						} else {
							ret = styleVal;
						} // end if
						
						return ret;
					} // end calculate
				} // end styleCalculator
			}) );
			
			
		}
		
	});	
	
})(jQuery, jQuery.cytoscapeweb);

;(function($, $$){
	
	$$.fn.core({
		
		scratch: function( name, value ){
			if( value === undefined ){
				return eval( "this._private.scratch." + name );
			} else {
				eval( "this._private.scratch." + name + " = " + value + ";" );
				return this;
			}
		},
		
		removeScratch: function( name ){
			if( name === undefined ){
				structs.scratch = {};
			} else {
				eval( "delete this._private.scratch." + name + ";" );
			}
			
			return this;
		}
		
	});	
	
})(jQuery, jQuery.cytoscapeweb);

;(function($, $$){
	
	$$.fn.core({
		collection: function(){
			return new $$.CyCollection( this );
		},
		
		getElementById: function(id){
			return this._private.nodes[id] || this._private.edges[id] || new $$.CyCollection( this );
		},
		
		nodes: defineSearch({
			addLiveFunction: true,
			group: "nodes"
		}),
		
		edges: defineSearch({
			addLiveFunction: true,
			group: "edges"
		}),
		
		elements: function(){
			return this.$.apply( this, arguments );
		},
		
		$: defineSearch({
			addLiveFunction: true
		}),
		
		filter: function(selector){
			if( $$.is.string(selector) ){
				return this.$(selector);
			} else if( $$.is.fn(selector) ) {
				return this.$(selector).filter(selector);
			}
		}
		
	});	
	
	
	function defineSearch( params ){
		var defaults = {
			group: undefined, // implicit group filter
			addLiveFunction: false
		};
		params = $.extend( {}, defaults, params );
		
		var groups = [];
		if( params.group == null ){
			groups = [ "nodes", "edges" ];
		} else {
			groups = [ params.group ];
		}
		
		return function( selector ){
			var cy = this;
			var elements = [];
			
			if( selector == null ){
				selector = "";
			}
			
			$.each(groups, function(i, group){
				$.each(cy._private[group], function(id, element){
					elements.push( element );
				});
			});
			
			var collection = new $$.CyCollection( cy, elements );
			
			var selector;
			if(params.group != null){
				selector = new $$.CySelector( cy, params.group, selector );
			} else {
				selector = new $$.CySelector( cy, selector );
			}
			
			return selector.filter( collection, params.addLiveFunction );
		};
	};
	
	
})(jQuery, jQuery.cytoscapeweb);

;(function($, $$){
	
	$$.fn.core({
		
		style: function(val){
			var ret;
			
			if( val === undefined ){
				ret = $$.util.copy( this._private.style );
			} else {
				this._private.style = $$.util.copy( val );
				ret = this;
				
				this.notify({
					type: "style",
					style: this._private.style
				});
			}
			
			return ret;
		},
		
		getContinuousMapperUpdates: function(){
			var cy = this;
			var structs = cy._private;
			
			return structs.continuousMapperUpdates;
		},
		
		clearContinuousMapperUpdates: function(){
			var cy = this;
			var structs = cy._private;
			
			structs.continuousMapperUpdates = [];
		},
		
		// update continuous mapper bounds when new data is added
		addContinuousMapperBounds: function(element, name, val){
			var cy = this;
			var structs = cy._private;
			var group = element._private.group;
			
			if( $$.is.number(val) ){
				if( structs.continuousMapperBounds[ group ][ name ] == null ){
					structs.continuousMapperBounds[ group ][ name ] = {
						min: val,
						max: val,
						vals: []
					};
				}
				
				var bounds = structs.continuousMapperBounds[ group ][ name ];
				var vals = bounds.vals;
				var inserted = false;
				var oldMin = null, oldMax = null;
				
				if( vals.length > 0 ){
					oldMin = vals[0];
					oldMax = vals[ vals.length - 1 ];
				}
				
				for(var i = 0; i < vals.length; i++){
					if( val <= vals[i] ){
						vals.splice(i, 0, val);
						inserted = true;
						break;
					}
				}
				
				if(!inserted){
					vals.push(val);
				}
				
				bounds.min = vals[0];
				bounds.max = vals[vals.length - 1];
				
				if( oldMin != bounds.min || oldMax != bounds.max ){
					structs.continuousMapperUpdates.push({
						group: element.group(),
						element: element
					});
				}
			}
		},
		
		// update continuous mapper bounds for a change in data value
		updateContinuousMapperBounds: function(element, name, oldVal, newVal){
			var cy = this;
			var structs = cy._private;
			var group = element._private.group;
			var bounds = structs.continuousMapperBounds[ group ][ name ];
			
			if( bounds == null ){
				this.addContinuousMapperBounds(element, name, newVal);
				return;
			}
			
			var vals = bounds.vals;
			var oldMin = null, oldMax = null;
			
			if( vals.length > 0 ){
				oldMin = vals[0];
				oldMax = vals[ vals.length - 1 ];
			}
			
			this.removeContinuousMapperBounds(element, name, oldVal);
			this.addContinuousMapperBounds(element, name, newVal);
			
			if( oldMin != bounds.min || oldMax != bounds.max ){
				structs.continuousMapperUpdates.push({
					group: element.group(),
					element: element
				});
			}
		},
		
		// update the continuous mapper bounds for a removal of data
		removeContinuousMapperBounds: function(element, name, val){
			var cy = this;
			var structs = cy._private;
			var group = element._private.group;
			var bounds = structs.continuousMapperBounds[ group ][ name ];
			
			if( bounds == null ){
				return;
			}
			
			var oldMin = null, oldMax = null;
			var vals = bounds.vals;
			
			if( vals.length > 0 ){
				oldMin = vals[0];
				oldMax = vals[ vals.length - 1 ];
			}
			
			
			for(var i = 0; i < vals.length; i++){
				if( val == vals[i] ){
					vals.splice(i, 1);
					break;
				}
			}
			
			if( vals.length > 0 ){
				bounds.min = vals[0];
				bounds.max = vals[vals.length - 1];
			} else {
				bounds.min = null;
				bounds.max = null;
			}
		
			if( oldMin != bounds.min || oldMax != bounds.max ){
				structs.continuousMapperUpdates.push({
					group: element.group(),
					element: element
				});
			}
		}	
	});
	
})(jQuery, jQuery.cytoscapeweb);

		
		
		
		

;(function($, $$){
	
	$$.fn.core({
		
		panning: function(bool){
			if( bool !== undefined ){
				this._private.panEnabled = bool ? true : false;
			} else {
				return this._private.panEnabled;
			}
			
			return this;
		},
		
		zooming: function(bool){
			if( bool !== undefined ){
				this._private.zoomEnabled = bool ? true : false;
			} else {
				return this._private.zoomEnabled;
			}
			
			return this;
		},
		
		pan: function(params){
			var ret = this.renderer().pan(params);
			
			if( ret == null ){
				this.trigger("pan");
				return this;
			}
			
			return ret;
		},
		
		panBy: function(params){
			var ret = this.renderer().panBy(params);
			
			if( ret == null ){
				this.trigger("pan");
				return this;
			}
			
			return ret;
		},
		
		fit: function(elements){
			var ret = this.renderer().fit({
				elements: elements,
				zoom: true
			});
			
			if( ret == null ){
				this.trigger("zoom");
				this.trigger("pan");
				return this;
			}
			
			return ret;
		},
		
		zoom: function(params){
			var ret = this.renderer().zoom(params);
			
			if( ret != null ){
				return ret;
			} else {
				this.trigger("zoom");
				return this;
			}
		},
		
		center: function(elements){
			this.renderer().fit({
				elements: elements,
				zoom: false
			});
			
			this.trigger("pan");
			return this;
		},
		
		centre: function(){ // alias to center
			return this.center.apply(cy, arguments); 
		},
		
		reset: function(){
			this.renderer().pan({ x: 0, y: 0 });
			this.renderer().zoom(1);
			
			this.trigger("zoom");
			this.trigger("pan");
			
			return this;
		}
	});	
	
})(jQuery, jQuery.cytoscapeweb);

;(function($, $$){
	
	// Use this interface to define functions for collections/elements.
	// This interface is good, because it forces you to think in terms
	// of the collections case (more than 1 element), so we don't need
	// notification blocking nonsense everywhere.
	//
	// Other collection-*.js files depend on this being defined first.
	// It's a trade off: It simplifies the code for CyCollection and 
	// CyElement integration so much that it's worth it to create the
	// JS dependency.
	//
	// Having this integration guarantees that we can call any
	// collection function on an element and vice versa.
	$$.fn.collection = function( impl, options ){
		for(var name in impl){
			
			// When adding a function, write it from the perspective of a
			// collection -- it's more generic.
			$$.CyCollection.prototype[ name ] = impl[name];
			
			// The element version of the function is then the trivial
			// case of a collection of size 1.
			$$.CyElement.prototype[ name ] = function(){
				var self = this.collection();
				return self[ name ].apply(self, arguments);
			};
			
		}
	};
	
	// factory for generating edge ids when no id is specified for a new element
	var idFactory = {
		prefix: {
			nodes: "n",
			edges: "e"
		},
		id: {
			nodes: 0,
			edges: 0
		},
		generate: function(element, tryThisId){
			var group = element._private.group;
			var structs = element._private.cy._private;
			var id = tryThisId != null ? tryThisId : this.prefix[group] + this.id[group];
			
			while( structs[group][id] != null ){
				id = this.prefix[group] + ( ++this.id[group] );
			}
			
			return id;
		}
	};
	
	// CyElement
	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	// represents a node or an edge
	function CyElement(cy, params){
		var self = this;
		
		if( cy === undefined || params === undefined || cy.$ == null ){
			$$.console.error("An element must have a core reference and parameters set");
			return;
		}
		
		// validate group
		if( params.group != "nodes" && params.group != "edges" ){
			$$.console.error("An element must be of type `nodes` or `edges`; you specified `%s`", params.group);
			return;
		}
		
		this.length = 1;
		this[0] = this;
		
		this._private = {
			cy: cy,
			data: $$.util.copy( params.data ) || {}, // data object
			position: $$.util.copy( params.position ) || {}, // fields x, y, etc (could be 3d or radial coords; renderer decides)
			listeners: {}, // map ( type => array of function spec objects )
			group: params.group, // string; "nodes" or "edges"
			bypass: $$.util.copy( params.bypass ) || {}, // the bypass object
			style: {}, // the rendered style populated by the renderer
			removed: true, // whether it's inside the vis; true if removed (set true here since we call restore)
			selected: params.selected ? true : false, // whether it's selected
			locked: params.locked ? true : false, // whether the element is locked (cannot be moved)
			grabbed: false, // whether the element is grabbed by the mouse; renderer sets this privately
			grabbable: params.grabbable || params.grabbable === undefined ? true : false, // whether the element can be grabbed
			classes: {}, // map ( className => true )
			animation: { // object for currently-running animations
				current: [],
				queue: []
			},
			renderer: {}, // object in which the renderer can store information
			scratch: {}, // scratch objects
			edges: {} // map of connected edges ( otherNodeId: { edgeId: { source: true|false, target: true|false, edge: edgeRef } } )
		};
		
		// renderedPosition overrides if specified
		// you shouldn't and can't use this option with cy.load() since we don't have access to the renderer yet
		// AND the initial state of the graph is such that renderedPosition and position are the same
		if( params.renderedPosition != null ){
			this._private.position = this.cy().renderer().modelPoint(params.renderedPosition);
		}
		
		if( $$.is.string(params.classes) ){
			$.each(params.classes.split(/\s+/), function(i, cls){
				if( cls != "" ){
					self._private.classes[cls] = true;
				}
			});
		}
		
		this.restore();
	}
	$$.CyElement = CyElement; // expose
	
	CyElement.prototype.cy = function(){
		return this._private.cy;
	};
	
	CyElement.prototype.element = function(){
		return this;
	};
	
	CyElement.prototype.collection = function(){
		return new CyCollection(this.cy(), [ this ]);
	};

	
	// CyCollection
	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	// represents a set of nodes, edges, or both together
	function CyCollection(cy, elements){
		
		if( cy === undefined || cy.$ == null ){
			$$.console.error("A collection must have a reference to the core");
			return;
		}
		
		var ids = {};
		var uniqueElements = [];
		
		if( elements == null ){
			elements = [];
		}
		
		$.each(elements, function(i, element){
			if( element == null ){
				return;
			}
			
			var id = element.element()._private.data.id;
			
			if( ids[ id ] == null ){
				ids[ id ] = true;
				uniqueElements.push( element );
			}
		});
		
		for(var i = 0; i < uniqueElements.length; i++){
			this[i] = uniqueElements[i];
		}
		this.length = uniqueElements.length;
		
		this._private = {
			cy: cy,
			ids: ids
		};
	}
	$$.CyCollection = CyCollection; // expose

	CyCollection.prototype.cy = function(){
		return this._private.cy;
	};
	
	CyCollection.prototype.element = function(){
		return this[0];
	};
	
	CyCollection.prototype.collection = function(){
		return this;
	};

	
	
	// Functions
	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	$$.fn.collection({
		restore: function( notifyRenderer ){
			var restored = new CyCollection(this.cy());
			
			this.each(function(){
				if( !this.removed() ){
					// don't need to do anything
					return;
				}
				
				var structs = this.cy()._private; // TODO remove ref to `structs` after refactoring
				
				// set id and validate
				if( this._private.data.id == null ){
					this._private.data.id = idFactory.generate( this, this._private.group );
				} else if( this.cy().getElementById( this._private.data.id ).size() != 0 ){
					$$.console.error("Can not create element: an element in the visualisation already has ID `%s`", this.element()._private.data.id);
					return this;
				}
				
				// validate source and target for edges
				if( this.isEdge() ){
					
					var fields = ["source", "target"];
					for(var i = 0; i < fields.length; i++){
						
						var field = fields[i];
						var val = this._private.data[field];
						
						if( val == null || val == "" ){
							$$.console.error("Can not create edge with id `%s` since it has no `%s` attribute set in `data` (must be non-empty value)", this._private.data.id, field);
							return;
						} else if( structs.nodes[val] == null ){ 
							$$.console.error("Can not create edge with id `%s` since it specifies non-existant node as its `%s` attribute with id `%s`",  this._private.data.id, field, val);
							return;
						}
					}
					
					var src = this.cy().getElementById( this._private.data.source );
					var tgt = this.cy().getElementById( this._private.data.target );
					
					function connect( node, otherNode, edge ){
						var otherId = otherNode.element()._private.data.id;
						var edgeId = edge.element()._private.data.id;
						
						if( node._private.edges[ otherId ] == null ){
							 node._private.edges[ otherId ] = {};
						}
						
						node._private.edges[ otherId ][ edgeId ] = {
							edge: edge,
							source: src,
							target: tgt
						};
					}
					
					// connect reference to source
					connect( src, tgt, this );
					
					// connect reference to target
					connect( tgt, src, this );
				} 
				 
				this._private.removed = false;
				structs[ this._private.group ][ this._private.data.id ] = this;
				
				// update mapper structs
				var self = this;
				$.each(this._private.data, function(name, val){
					self.cy().addContinuousMapperBounds(self, name, val);
				});
				
				restored = restored.add(this);
			});
			
			if( restored.size() > 0 ){
				if( notifyRenderer ){
					restored.rtrigger("add");
				} else {
					restored.trigger("add");
				}
				
			}
			
			return this;
		}
	});
	
	$$.fn.collection({
		removed: function(){
			return this.element()._private.removed;
		}
	});
	
	$$.fn.collection({
		remove: function( notifyRenderer ){
			var removed = [];
			var edges = {};
			var nodes = {};
			
			if( notifyRenderer === undefined ){
				notifyRenderer = true;
			}
			
			this.each(function(){
				if( this.isNode() ){
					var node = this.element();
					nodes[ node.id() ] = this;
					
					$.each(node._private.edges, function(otherNodeId, map){
						$.each(map, function(edgeId, struct){
							edges[ edgeId ] = struct.edge;
						});
					});
				}
				
				if( this.isEdge() ){
					edges[ this.id() ] = this;
				}
			});
			
			$.each( [edges, nodes], function(i, elements){
				$.each(elements, function(id, element){
					var ele = element.element();
					var group = ele._private.group;
					
					// mark self as removed via flag
					ele._private.removed = true;
					
					// remove reference from core
					delete ele.cy()._private[ ele.group() ][ ele.id() ];
					
					// remove mapper bounds for all data removed
					$.each(ele._private.data, function(attr, val){
						ele.cy().removeContinuousMapperBounds(ele, attr, val);
					});
					
					// add to list of removed elements
					removed.push( ele );
					
					// if edge, delete references in nodes
					if( ele.isEdge() ){
						var src = ele.source().element();
						var tgt = ele.target().element();
						
						delete src._private.edges[ tgt.id() ][ ele.id() ];
						delete tgt._private.edges[ src.id() ][ ele.id() ];
					}
				});
			} );
			
			var removedElements = new $$.CyCollection( this.cy(), removed );
			if( removedElements.size() > 0 ){
				// must manually notify since trigger won't do this automatically once removed
				
				if( notifyRenderer ){
					this.cy().notify({
						type: "remove",
						collection: removedElements
					});
				}
				
				removedElements.trigger("remove");
			}
			
			return this;
		}
	});
	
})(jQuery, jQuery.cytoscapeweb);


;(function($, $$){
	
	$$.fn.collection({
		animated: function(){
			return this.element()._private.animation.current.length > 0;
		}
	});
	
	$$.fn.collection({
		clearQueue: function(){
			return this.each(function(){
				this.element()._private.animation.queue = [];
			});
		}
	});
	
	$$.fn.collection({
		delay: function( time ){
			return this.animate({
				delay: time
			}, {
				duration: time
			});
		}
	});
	
	$$.fn.collection({
		animate: function( properties, params ){
			var callTime = +new Date;
			
			return this.each(function(){
				var self = this;
				var startPosition = $$.util.copy( self._private.position );
				var startStyle = $$.util.copy( self.style() );
				var structs = this.cy()._private; // TODO remove ref to `structs` after refactoring
				
				params = $.extend(true, {}, {
					duration: 400
				}, params);
				
				switch( params.duration ){
				case "slow":
					params.duration = 600;
					break;
				case "fast":
					params.duration = 200;
					break;
				}
				
				if( properties == null || (properties.position == null && properties.bypass == null && properties.delay == null) ){
					return; // nothing to animate
				}
				
				if( self.animated() && (params.queue === undefined || params.queue) ){
					enqueue();
				} else {
					run();
				}
				
				var q;
				
				function enqueue(){
					q = self._private.animation.queue;
					add();
				}
				
				function run(){
					q = self._private.animation.current;
					add();
				} 
				
				function add(){
					q.push({
						properties: properties,
						params: params,
						callTime: callTime,
						startPosition: startPosition,
						startStyle: startStyle
					});
					
					structs.animation.elements = structs.animation.elements.add( self );
				}
			});
		}
	});
	
	$$.fn.collection({
		stop: function(clearQueue, jumpToEnd){
			this.each(function(){
				var self = this;
				
				$.each(self._private.animation.current, function(i, animation){				
					if( jumpToEnd ){
						$.each(animation.properties, function(propertyName, property){
							$.each(property, function(field, value){
								self._private[propertyName][field] = value;
							});
						});
					}
				});
				
				self._private.animation.current = [];
				
				if( clearQueue ){
					self._private.animation.queue = [];
				}
			});
			
			// we have to notify (the animation loop doesn't do it for us on `stop`)
			this.cy().notify({
				collection: this,
				type: "draw"
			});
			
			return this;
		}
	});
	
	$$.fn.collection({
		show: function(){
			this.cy().renderer().showElements( this.collection() );
			
			return this;
		}
	});
	
	$$.fn.collection({
		hide: function(){
			this.cy().renderer().hideElements( this.collection() );
			
			return this;
		}
	});
	
	$$.fn.collection({
		visible: function(){
			return this.cy().renderer().elementIsVisible( this.element() );
		}
	});
	
})(jQuery, jQuery.cytoscapeweb);	

;(function($, $$){
	
	$$.fn.collection({
		addClass: function(classes){
			classes = classes.split(/\s+/);
			var self = this;
			
			$.each(classes, function(i, cls){
				if( $$.is.emptyString(cls) ){ return; }
				
				self.each(function(){
					this._private.classes[cls] = true;
				});
			});
			
			self.rtrigger("class");
			return self;
		}
	});
	
	$$.fn.collection({	
		hasClass: function(className){
			return this.element()._private.classes[className] == true;
		}
	});
	
	$$.fn.collection({
		toggleClass: function(classesStr, toggle){
			var classes = classesStr.split(/\s+/);
			var self = this;
			var toggledElements = [];
			
			function remove(self, cls){
				var toggled = self._private.classes[cls] !== undefined;
				delete self._private.classes[cls];
				
				if( toggled ){
					toggledElements.push( self );
				}
			}
			
			function add(self, cls){
				var toggled = self._private.classes[cls] === undefined;
				self._private.classes[cls] = true;
				
				if( toggled ){
					toggledElements.push( self );
				}
			}
			
			self.each(function(){
				var self = this;
				
				$.each(classes, function(i, cls){
					if( cls == null || cls == "" ){ return; }
					
					if( toggle === undefined ){
						if( self.hasClass(cls) ){
							remove(self, cls);
						} else {
							add(self, cls);
						}
					} else if( toggle ){
						add(self, cls);
					} else {
						remove(self, cls);
					}
				});
			});
			
			if( toggledElements.length > 0 ){
				var collection = new $$.CyCollection( self.cy(), toggledElements );
				collection.rtrigger("class");
			}
			
			return self;
		}
	});
	
	$$.fn.collection({
		removeClass: function(classes){
			classes = classes.split(/\s+/);
			var self = this;
			var removedElements = [];
			
			$.each(classes, function(i, cls){
				if( cls == null || cls == "" ){ return; }
				
				self.each(function(){
					var removed = this._private.classes[cls] !== undefined;
					delete this._private.classes[cls];
					
					if( removed ){
						removedElements.push( this );
					}
				});
			});
			
			if( removedElements.length > 0 ){
				var collection = new $$.CyCollection( self.cy(), removedElements );
				collection.rtrigger("class");
			}
			
			return self;
		}
	});
	
	
})(jQuery, jQuery.cytoscapeweb);

;(function($, $$){

	$$.fn.collection({
		allAre: function(selector){
			return this.filter(selector).size() == this.size();
		}
	});
	
	$$.fn.collection({
		is: function(selector){
			return new $$.CySelector(this.cy(), selector).filter(this).size() > 0;
		}
	});

	$$.fn.collection({
		same: function( other ){
			return this.element() === other.element();
		}
	});
	
	$$.fn.collection({
		anySame: function(collection){
			collection = collection.collection();
			
			var ret = false;
			for(var i = 0; i < collection.size(); i++){
				var collectionElement = collection.eq(i).element();
				
				for(var j = 0; j < this.size(); j++){
					var thisElement = this.eq(j);
					
					ret = ret || thisElement.same(collectionElement);
					if(ret) break;
				}
				if(ret) break;
			}
			
			return ret;
		}
	});

	$$.fn.collection({
		allSame: function(collection){
			collection = collection.collection();
			
			// cheap check to make sure A.allSame(B) == B.allSame(A)
			if( collection.size() != this.size() ){
				return false;
			}
			
			var ret = true;
			for(var i = 0; i < collection.size(); i++){
				var collectionElement = collection.eq(i);
				
				var hasCollectionElement = false;
				for(var j = 0; j < this.size(); j++){
					var thisElement = this.eq(j);
					
					hasCollectionElement = thisElement.same(collectionElement);
					if(hasCollectionElement) break;
				}
				
				ret = ret && hasCollectionElement;
				if(!ret) break;
			}
			
			return ret;
		}
	});
	
	$$.fn.collection({
		allAreNeighbors: function(collection){
			collection = collection.collection();
			
			var neighborhood = this.neighborhood();
			for(var i = 0; i < collection.size(); i++){
				var element = collection.eq(i);
				
				if( element.intersect(neighborhood).size() == 0 ){
					return false;
				}
			}
			
			return true;
		}
	});
	$$.fn.collection({ // English spelling variant
		allAreNeighbours: function(){
			return this.allAreNeighbors.apply(this, [arguments]);
		}
	});


	
})(jQuery, jQuery.cytoscapeweb);

;(function($, $$){
	
	$$.fn.collection({
		data: defineAccessor({ // defaults serve as example (data)
			attr: "data",
			allowBinding: true,
			bindingEvent: "data",
			settingTriggersEvent: true, 
			settingEvent: "data",
			validKey: { // already guaranteed that key is a string; `this` refers to the element
				forSet: function( key ){
					switch( key ){
					case "id":
					case "source":
					case "target":
						return false;
					default:
						return true;
					}
				}
			},
			onSet: function( key, oldVal, newVal ){ // callback function to call when setting for an element
				this.cy().updateContinuousMapperBounds(this, key, oldVal, newVal);
			}
		}) 
	});
	
	$$.fn.collection({
		removeData: defineRemover({
			attr: "data",
			event: "data",
			triggerEvent: true,
			onRemove: function( key, val ){ // callback after removing; `this` refers to the element
				this.cy().removeContinuousMapperBounds(this, key, val);
			},
			validKey: function( key ){
				switch(key){
				case "id":
				case "source":
				case "target":
					return false;
				default:
					return true;
				}
			},
			essentialKeys: [ "id", "source", "target" ] // keys that remain even when deleting all
		}) 
	});
	
	$$.fn.collection({
		id: function(){
			return this.element()._private.data.id;
		}
	});
	
	$$.fn.collection({
		position: defineAccessor({
			attr: "position",
			allowBinding: true,
			bindingEvent: "position",
			settingTriggersEvent: true, 
			settingEvent: "position",
			validKey: {
				forSet: function( key ){
					return this.isNode();
				},
				forGet: function( key ){
					return this.isNode();
				}
			},
			validValue: function( key, val ){
				return true;
			},
			onSet: function( key, oldVal, newVal ){
				// do nothing
			},
			onGet: function( key, val ){
				// do nothing
			}
		})
	});
	
	$$.fn.collection({
		positions: function(pos){
			if( $$.is.plainObject(pos) ){
				
				this.each(function(i, ele){
					$.each(pos, function(key, val){
						ele._private.position[ key ] = val;
					});
				});
				
				this.rtrigger("position");
				
			} else if( $$.is.fn(pos) ){
				var fn = pos;
				
				this.each(function(i, ele){
					var pos = fn.apply(ele, [i, ele]);
					
					$.each(pos, function(key, val){
						ele._private.position[ key ] = val;
					});
				});
				
				this.rtrigger("position");
			}
		}
	});
	
	$$.fn.collection({
		renderedPosition: defineAccessor({
			attr: "position",
			allowBinding: false,
			settingTriggersEvent: true, 
			settingEvent: "position",
			validKey: {
				forSet: function( key ){
					return this.isNode();
				},
				forGet: function( key ){
					return this.isNode();
				}
			},
			validValue: function( key, val ){
				return true;
			},
			override: {
				forSet: function( key, val ){ 
					var rpos = {};
					rpos[ key ] = val;
					
					var mpos = this.cy().renderer().modelPoint( rpos );
					this.element()._private.position[key] = mpos[key];
				},
				forGet: function( key ){
					var mpos = this.position(false);
					var rpos = this.cy().renderer().renderedPoint( mpos );
					return rpos[ key ];
				},
				forObjectGet: function(){
					return this.cy().renderer().renderedPosition( this.element() );
				}
			}
		})
	});
	
	$$.fn.collection({
		renderedDimensions: function( dimension ){
			var ele = this.element();
			var renderer = ele.cy().renderer(); // TODO remove reference after refactoring
			var dim = renderer.renderedDimensions(ele);
			
			if( dimension === undefined ){
				return dim;
			} else {
				return dim[dimension];
			}
		}
	});
	
	$$.fn.collection({
		style: function( key ){
			var ele = this.element();
			
			if( key === undefined ){
				return $$.util.copy( ele._private.style );
			}
			
			// on false, return whole obj but w/o copying
			else if( key === false ){
				return ele._private.style;
			}
			
			else if( $$.is.string(key) ){
				return $$.util.copy( ele._private.style[key] );
			}
		}
	});
	
	$$.fn.collection({
		bypass: defineAccessor({
			attr: "bypass",
			allowBinding: true,
			bindingEvent: "bypass",
			settingTriggersEvent: true, 
			settingEvent: "bypass"
		})
	});
	
	$$.fn.collection({
		removeBypass: defineRemover({
			attr: "bypass",
			event: "bypass",
			triggerEvent: true
		})
	});
	
	$$.fn.collection({
		json: function(){
			var p = this.element()._private;
			
			var json = $$.util.copy({
				data: p.data,
				position: p.position,
				group: p.group,
				bypass: p.bypass,
				removed: p.removed,
				selected: p.selected,
				locked: p.locked,
				grabbed: p.grabbed,
				grabbable: p.grabbable,
				classes: "",
				scratch: p.scratch
			});
			
			var classes = [];
			$.each(p.classes, function(cls, bool){
				classes.push(cls);
			});
			
			$.each(classes, function(i, cls){
				json.classes += cls + ( i < classes.length - 1 ? " " : "" );
			});
			
			return json;
		}
	});
	
	// Generic metacode for defining data function behaviour follows
	//////////////////////////////////////////////////////////////////////////////////////
	
	function defineAccessor( opts ){
		var defaults = { // defaults serve as example (data)
			attr: "foo",
			allowBinding: false,
			bindingEvent: "foo",
			settingTriggersEvent: false, 
			settingEvent: "foo",
			validKey: { // already guaranteed that key is a string; `this` refers to the element
				forGet: function( key ){
					return true;
				},
				forSet: function( key ){
					return true;
				}
			},
			override: {
				forSet: null, // function(key, val){ return val; },
				forGet: null, // function(key){ return val; },
				forObjectGet: null // function( obj ){ return obj; }
			},
			validValue: function( key, val ){
				return true;
			},
			onSet: null, // function( key, oldVal, newVal ){},
			onGet: null, // function( key, val ){}
		};
		var params = $.extend(true, {}, defaults, opts);
				
		return function(key, val){
			var ele = this.element();
			var eles = this;
			
			function getter(key){
				if( params.validKey.forGet.apply(ele, [key]) ){
					var ret;
					
					if( $$.is.fn( params.override.forGet ) ){
						ret = params.override.forGet.apply( ele, [ key ] );
					} else {
						ret = $$.util.copy(  ele._private[ params.attr ] [ key ] );
					}
					
					if( $$.is.fn(params.onGet) ){
						params.onGet.apply( ele, [key, ret] );
					}
					
					return ret;
				} else {
					//$$.console.warn( "Can not access field `%s` for `%s` for collection with element `%s`", key, params.attr, ele._private.data.id );
				}
			}
			
			function setter(key, val){
				eles.each(function(){
					if( params.validKey.forSet.apply(this, [key]) && params.validValue.apply(this, [key, val]) ){
						var oldVal = this.element()._private[ params.attr ][ key ];
						
						if( $$.is.fn( params.override.forSet ) ){
							params.override.forSet.apply( this, [ key, val ] );
						} else {
							this.element()._private[ params.attr ][ key ] = $$.util.copy( val );
						}
						
						if( $$.is.fn(params.onSet) ){
							params.onSet.apply( ele, [key, oldVal, val] );
						}
					} else {
						//$$.console.warn( "Can not set field `%s` for `%s` for element `%s` to value `%o` : invalid value", key, params.attr, ele._private.data.id );
					}
				});
			}
			
			function bind(fn, data){
				if( data === undefined ){
					eles.bind( params.bindingEvent, fn );
				} else {
					eles.bind( params.bindingEvent, data, fn );
				}
			}
			
			function trigger(){
				if( params.settingTriggersEvent ){
					eles.rtrigger( params.settingEvent );
				}
			}
			
			function objGetter( copy ){
				var ret;
				var obj = ele._private[ params.attr ];
				
				if( $$.is.fn( params.override.forObjectGet ) ){
					ret = params.override.forObjectGet.apply( ele, [ ] );
				} else {
					ret = obj;
				}
				
				if( copy || copy === undefined ){
					ret = $$.util.copy( ret );
				}
				
				return ret;
			}
			
			// CASE: no parameters
			// get whole attribute object
			if( key === undefined ){
				return objGetter(false);
			}
			
			// if passed false, just get the whole object without copying
			else if( key === false ){
				return objGetter(false);
			}
			
			// CASE: single parameter
			else if( val === undefined ){
				
				// get attribute with specified key
				if( $$.is.string(key) ){
					return getter(key);
				}
				
				// set fields with an object
				else if( $$.is.plainObject(key) ) {
					var obj = key;
					
					$.each(obj, function(key, val){
						setter(key, val);
					});
					
					trigger();
				}
				
				// bind with a handler function
				else if( params.allowBinding && $$.is.fn(key) ){
					var fn = key;
					
					bind(fn);
				}
				
				else {
					$$.console.warn("Invalid first parameter for `%s()` for collection with element `%s` : expect a key string or an object" + ( params.allowBinding ?  " or a handler function for binding" : "" ), params.attr, ele._private.data.id);
				}

			}
			
			// CASE: two parameters
			else {
				
				// bind to event with data object
				if( params.allowBinding && $$.is.plainObject(key) && $$.is.fn(val) ){
					var data = key;
					var fn = val;
					
					bind(fn, data);
				}
				
				// set field with key to val
				else if( $$.is.string(key) ){
					setter(key, val);
					trigger();
				}
				
				else {
					$$.console.warn("Invalid parameters for `%s()` for collection with element `%s` : expect a key string and a value" + ( params.allowBinding ?  " or a data object and a handler function for binding" : "" ), params.attr, ele._private.data.id);
				}
				
			}
			
			return this; // chaining
		};
	}
	
	function defineRemover( opts ){
		var defaults = {
			attr: "foo",
			event: "foo",
			triggerEvent: false,
			onRemove: function( key, val ){ // callback after removing; `this` refers to the element
				// do nothing
			},
			validKey: function( key ){
				return true;
			},
			essentialKeys: [  ] // keys that remain even when deleting all
		};
		
		var params = $.extend(true, {}, defaults, opts);
		
		return function(keys){
			var ele = this.element();
			var eles = this;
			
			function removeAll(){
				eles.each(function(){
					var ele = this.element();
					var oldObj = ele._private[ params.attr ];
					var newObj = {};
					
					// copy essential keys to new obj
					$.each( params.essentialKeys, function(i, key){
						if( oldObj[ key ] !== undefined ){
							newObj[ key ] = oldObj[ key ];
						}
					} );
					
					ele._private[ params.attr ] = newObj;
				});
			}
			
			function remove( key ){
				eles.each(function(){
					var ele = this.element();
					
					if( params.validKey.apply(ele, [key]) ){
						var val = ele._private[ params.attr ][ key ];
						delete ele._private[ params.attr ][ key ];
						
						if( $$.is.fn( params.onRemove ) ){
							params.onRemove.apply(ele, [key, val]);
						}
					}
				});
			}
			
			function trigger(){
				if( params.triggerEvent ){
					eles.rtrigger( params.event );
				}
			}
			
			// remove all 
			if( keys === undefined ){
				removeAll();
				trigger();
			}
			
			else if( $$.is.string(keys) ){
				var keysArray = keys.split(/\s+/);
				
				$.each( keysArray, function(i, key){
					if( $$.is.emptyString(key) ) return; // ignore empty keys
					remove( key );
				} );
				
				trigger();
			} 
			
			else {
				$$.console.warn("Invalid parameters to `%s()` for collection with element `%s` : %o", params.attr, ele._private.data.id, arguments);
			}
			
			return this; // chaining
		};
	}

	
})(jQuery, jQuery.cytoscapeweb);

;(function($, $$){
	
	// Regular degree functions (works on single element)
	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	function defineDegreeFunction(callback){
		return function(){
			var self = this.element();
			
			if( self.isNode() && !self.removed() ){
				var degree = 0;
				var node = this;
				
				node.connectedEdges().each(function(i, edge){
					degree += callback( node, edge );
				});
				
				return degree;
			} else {
				return undefined;
			}
		};
	}
	
	$$.fn.collection({
		degree: defineDegreeFunction(function(node, edge){
			if( edge.source().same( edge.target() ) ){
				return 2;
			} else {
				return 1;
			}
		})
	});
	
	$$.fn.collection({
		indegree: defineDegreeFunction(function(node, edge){
			if( edge.target().same(node) ){
				return 1;
			} else {
				return 0;
			}
		})
	});
	
	$$.fn.collection({
		outdegree: defineDegreeFunction(function(node, edge){
			if( edge.source().same(node) ){
				return 1;
			} else {
				return 0;
			}
		})
	});
	
	
	// Collection degree stats
	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	function defineDegreeBoundsFunction(degreeFn, callback){
		return function(){
			var ret = null;
			
			this.nodes().each(function(i, ele){
				var degree = ele[degreeFn]();
				if( degree != null && (ret == null || callback(degree, ret)) ){
					ret = degree;
				}
			});
			
			return ret;
		};
	}
	
	$$.fn.collection({
		minDegree: defineDegreeBoundsFunction("degree", function(degree, min){
			return degree < min;
		})
	});
	
	$$.fn.collection({
		maxDegree: defineDegreeBoundsFunction("degree", function(degree, max){
			return degree > max;
		})
	});
	
	$$.fn.collection({
		minIndegree: defineDegreeBoundsFunction("indegree", function(degree, min){
			return degree < min;
		})
	});
	
	$$.fn.collection({
		maxIndegree: defineDegreeBoundsFunction("indegree", function(degree, max){
			return degree > max;
		})
	});
	
	$$.fn.collection({
		minOutdegree: defineDegreeBoundsFunction("outdegree", function(degree, min){
			return degree < min;
		})
	});
	
	$$.fn.collection({
		maxOutdegree: defineDegreeBoundsFunction("outdegree", function(degree, max){
			return degree > max;
		})
	});
	
	$$.fn.collection({
		totalDegree: function(){
			var total = 0;
			
			this.nodes().each(function(i, ele){
				total += ele.degree();
			});

			return total;
		}
	});
	
})(jQuery, jQuery.cytoscapeweb);

	

;(function($, $$){
	
	// Functions for binding & triggering events
	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	$$.fn.collection({
		trigger: function(event, data){
			this.each(function(){
				var self = this;
				var type = $$.is.plainObject(event) ? event.type : event;
				var structs = this.cy()._private; // TODO remove ref to `structs` after refactoring
				
				var listeners = this._private.listeners[type];
				
				function fire(listener, eventData){
					if( listener != null && $$.is.fn(listener.callback) ){
						var eventData = $$.is.plainObject(event) ? event : $.Event(type);
						eventData.data = listener.data;
						eventData.cy = eventData.cytoscapeweb = cy;
						
						var args = [eventData];
						
						if( data != null ){
							$.each(data, function(i, arg){
								args.push(arg);
							});
						}
						
						listener.callback.apply(self, args);
					}
				}
				
				// trigger regularly bound listeners
				if( listeners != null ){
					$.each(listeners, function(i, listener){
						fire(listener);
					});
					
					for(var i = 0; i < listeners.length; i++){
						function remove(){
							listeners.splice(i, 1);
							i--;
						}
						
						if( listeners[i].one ){
							remove();
						} else if( listeners[i].once ){
							var listener = listeners[i];
							
							// remove listener for other elements
							listener.collection.each(function(j, ele){
								if( !ele.same(self) ){
									ele.unbind(type, listener.callback);
								}
							});
							
							// remove listener for self
							remove();
						}
					}
				}
				
				// trigger element live events
				if( structs.live[type] != null ){
					$.each(structs.live[type], function(key, callbackDefns){
						
						var selector = new $$.CySelector( self.cy(), key );
						var filtered = selector.filter( self.collection() );
						
						if( filtered.size() > 0 ){
							$.each(callbackDefns, function(i, listener){
								fire(listener);
							});
						}
					});
				}
				
				// bubble up element events to the core
				self.cy().trigger(event, data);
				
			});
			
			return this;
		}
	});
	
	$$.fn.collection({
		rtrigger: function(event, data){
			// notify renderer unless removed
			this.cy().notify({
				type: event,
				collection: this.filter(function(){
					return !this.removed();
				})
			});
			
			this.trigger(event, data);
		}
	});
	
	$$.fn.collection({
		live: function(){
			$$.console.warn("`live()` can be called only on collections made from top-level selectors");
			return this;
		}
	});
	
	$$.fn.collection({
		die: function(){
			$$.console.warn("`die()` can be called only on collections made from top-level selectors");
			return this;
		}
	});
	
	$$.fn.collection({
		bind: defineBinder({
			listener: function(){
				return {};
			}
		})
	});
	
	$$.fn.collection({
		one: defineBinder({
			listener: function(){
				return { one: true };
			}
		})
	});
	
	$$.fn.collection({
		once: defineBinder({
			listener: function( collection, element ){
				return {
					once: true,
					collection: collection
				}
			},
			after: function( collection, callback, data ){
				// do nothing
			}
		})
	});
	
	$$.fn.collection({
		on: function(events, data, callback){
			return this.bind(events, data, callback);
		}
	});
	
	$$.fn.collection({
		off: function(events, callback){
			return this.unbind(events, callback);
		}
	});
	
	$$.fn.collection({
		unbind: function(events, callback){
			var eventsArray = (events || "").split(/\s+/);
			
			this.each(function(){
				var self = this;
				
				if( events === undefined ){
					self._private.listeners = {};
					return;
				}
				
				$.each(eventsArray, function(j, event){
					if( $$.is.emptyString(event) ) return this;
				
					var listeners = self._private.listeners[event];
					
					if( listeners != null ){
						for(var i = 0; i < listeners.length; i++){
							var listener = listeners[i];
							
							if( callback == null || callback == listener.callback ){
								listeners.splice(i, 1);
								i--;
							}
						}
					}
				
				});
			});
			
			return this;
		}
	});
	
	// Metaprogramming to define a bunch of functions
	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	// add events to the list here IF AND ONLY IF there is no corresponding getter/setter function
	// e.g. it doesn't make sense to have `data` here, since it's also a getter/setter
	var aliases = "mousedown mouseup click mouseover mouseout mousemove touchstart touchmove touchend grab drag free";
	
	$.each(aliases.split(/\s+/), function(i, alias){
		var impl = {};
		impl[ alias ] = defineBindAlias({
			event: alias
		});
		
		$$.fn.collection(impl);
	});
	
	function defineBindAlias( params ){
		var defaults = {
			event: ""
		};
		
		params = $.extend({}, defaults, params);
		
		return function(data, callback){
			if( $$.is.fn(callback) ){
				return this.bind(params.event, data, callback);
			} else if( $$.is.fn(data) ){
				var handler = data;
				return this.bind(params.event, handler);						
			} else {
				return this.rtrigger(params.event, data);
			}
		};
	}
	
	function defineBinder( params ){
		var defaults = {
			listener: function(){},
			after: function(){}
		};
		params = $.extend({}, defaults, params);
		
		return function(events, data, callback){
			var self = this;
			
			if( callback === undefined ){
				callback = data;
				data = undefined;
			}
			
			$.each(events.split(/\s+/), function(i, event){
				if( $$.is.emptyString(event) ) return;
				
				self.each(function(){
					if( this._private.listeners[event] == null ){
						this._private.listeners[event] = [];
					}
					
					var listener = params.listener.apply(this, [ self, this ]);
					
					listener.callback = callback; // add the callback
					listener.data = data; // add the data
					this._private.listeners[event].push( listener );
				});
				
				params.after.apply(self, [self, callback, data]);
			});
			
			return this;
		};
	}
	
	
	
})(jQuery, jQuery.cytoscapeweb);

;(function($, $$){

	$$.fn.collection({
		isNode: function(){
			return this.group() == "nodes";
		}
	});
	
	$$.fn.collection({
		isEdge: function(){
			return this.group() == "edges";
		}
	});
	
	$$.fn.collection({
		group: function(){
			return this.element()._private.group;
		}
	});

	
})(jQuery, jQuery.cytoscapeweb);

;(function($, $$){
	
	// Functions for iterating over collections
	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	$$.fn.collection({
		each: function(fn){
			if( $$.is.fn(fn) ){
				for(var i = 0; i < this.size(); i++){
					var ele = this.eq(i).element();
					fn.apply( ele, [ i, ele ] );				
				}
			}
			return this;
		}
	});
	
	
	$$.fn.collection({
		toArray: function(){
			var array = [];
			
			for(var i = 0; i < this.size(); i++){
				array.push( this.eq(i).element() );
			}
			
			return array;
		}
	});
	
	$$.fn.collection({
		slice: function(start, end){
			var array = [];
			
			if( end == null ){
				end = this.size();
			}
			
			if( start < 0 ){
				start = this.size() + start;
			}
			
			for(var i = start; i >= 0 && i < end && i < this.size(); i++){
				array.push( this.eq(i) );
			}
			
			return new $$.CyCollection(this.cy(), array);
		}
	});
	
	$$.fn.collection({
		size: function(){
			return this.length;
		}
	});
	
	$$.fn.collection({
		eq: function(i){
			return this[i];
		}
	});
	
	$$.fn.collection({
		empty: function(){
			return this.size() == 0;
		}
	});
	
})(jQuery, jQuery.cytoscapeweb);

;(function($, $$){

	// Functions for scratchpad data for extensions & plugins
	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	$$.fn.collection({
		scratch: defineAccessor({ attr: "scratch" })
	});
	
	$$.fn.collection({
		removeScratch: defineRemover({ attr: "scratch" })
	});
	
	$$.fn.collection({
		renderer: defineAccessor({ attr: "renderer" })
	});
	
	$$.fn.collection({
		removeRenderer: defineRemover({ attr: "renderer" })
	});

	function defineAccessor( params ){
		var defaults = {
			attr: "scratch"
		};
		params = $.extend(true, {}, defaults, params);
		
		return function( name, val ){
			var self = this;
			
			if( name === undefined ){
				return self.element()._private[ params.attr ];
			}
			
			var fields = name.split(".");
			
			function set(){
				self.each(function(){
					var self = this;
					
					var obj = self._private[ params.attr ];
					$.each(fields, function(i, field){
						if( i == fields.length - 1 ){ return; }
						
						obj = obj[field];
					});
					
					var lastField = fields[ fields.length - 1 ];
					obj[ lastField ] = val;
				});
			}
			
			function get(){
				var obj = self.element()._private[ params.attr ];
				$.each(fields, function(i, field){
					obj = obj[field];
				});
				
				return obj;
			}
			
			if( val === undefined ){
				return get(); 
			} else {
				set();
			}
			
			return this;
		};
	}
	
	function defineRemover( params ){
		var defaults = {
			attr: "scratch"
		};
		params = $.extend(true, {}, defaults, params);
		
		return function( name ){
			var self = this;
			
			// remove all
			if( name === undefined ){
				self.each(function(){
					this._private[ params.attr ] = {};
				});
			} 
			
			// remove specific
			else {
				var names = name.split(/\s+/);
				$.each(names, function(i, name){
					self.each(function(){
						eval( "delete this._private." + params.attr + "." + name + ";" );
					});
				});
			}
			
			return this;
		};
	}
	
})(jQuery, jQuery.cytoscapeweb);

;(function($, $$){
	
	// Collection functions that toggle a boolean value
	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	function defineSwitchFunction(params){
		return function(){
			var args = arguments;
			
			// e.g. cy.nodes().select( data, handler )
			if( args.length == 2 ){
				this.bind( params.event, args[0], args[1] );
			} 
			
			// e.g. cy.nodes().select( handler )
			else if( args.length == 1 ){
				this.bind( params.event, args[0] );
			}
			
			// e.g. cy.nodes().select()
			else if( args.length == 0 ){
				this.each(function(){
					this.element()._private[params.field] = params.value;
				});
				this.rtrigger(params.event);
			}

			return this;
		};
	}
	
	function defineSwitchSet( params ){
		function impl(name, fn){
			var impl = {};
			impl[ name ] = fn;
			
			return impl;
		}
		
		$$.fn.collection(
			impl( params.field, function(){
				return this.element()._private[ params.field ];
			})
		);
		
		$$.fn.collection(
			impl( params.on, defineSwitchFunction({
					event: params.on,
					field: params.field,
					value: true
				})
			)
		);
	
		$$.fn.collection(
			impl( params.off, defineSwitchFunction({
					event: params.off,
					field: params.field,
					value: false
				})
			)
		);
	}
	
	defineSwitchSet({
		field: "locked",
		on: "lock",
		off: "unlock"
	});
	
	defineSwitchSet({
		field: "grabbable",
		on: "grabify",
		off: "ungrabify"
	});
	
	defineSwitchSet({
		field: "selected",
		on: "select",
		off: "unselect"
	});
	
	$$.fn.collection({
		grabbed: function(){
			return this.element()._private.grabbed;
		}
	});
	
})(jQuery, jQuery.cytoscapeweb);

;(function($, $$){
	
	$$.fn.collection({
		nodes: function(selector){
			return this.filter(function(i, element){
				return element.isNode();
			});
		}
	});

	$$.fn.collection({
		edges: function(selector){
			return this.filter(function(i, element){
				return element.isEdge();
			});
		}
	});

	$$.fn.collection({
		filter: function(filter){
			var cy = this.cy();
			
			if( $$.is.fn(filter) ){
				var elements = [];
				this.each(function(i, element){
					element = element.element();
					
					if( filter.apply(element, [i, element]) ){
						elements.push(element);
					}
				});
				
				return new $$.CyCollection(this.cy(), elements);
			} else if( $$.is.string(filter) ){
				return new $$.CySelector(this.cy(), filter).filter(this);
			} else if( filter === undefined ){
				return this;
			}

			$$.console.warn("You must pass a function or a selector to `filter`");
			return new $$.CyCollection( this.cy() );
		}
	});

	$$.fn.collection({	
		not: function(toRemove){
			
			if( toRemove == null ){
				return this;
			} else {
			
				if( $$.is.string(toRemove) ){
					toRemove = this.filter(toRemove);
				}
				
				var elements = [];
				toRemove = toRemove.collection();
				
				this.each(function(i, element){
					
					var remove = toRemove._private.ids[ element.id() ];					
					if( !remove ){
						elements.push( element.element() );
					}
					
				});
				
				return new $$.CyCollection(this.cy(), elements);
			}
			
		}
	});
	
	$$.fn.collection({
		intersect: function( other ){
			var self = this;
			
			// if a selector is specified, then filter by it
			if( $$.is.string(other) ){
				var selector = other;
				return this.filter( selector );
			}
			
			if( $$.is.element(other) ){
				other = other.collection();
			}
			
			var elements = [];
			var col1 = this;
			var col2 = other;
			var col1Smaller = this.size() < other.size();
			var ids1 = col1Smaller ? col1._private.ids : col2._private.ids;
			var ids2 = col1Smaller ? col2._private.ids : col1._private.ids;
			
			$.each(ids1, function(id){
				if( ids2[ id ] ){
					elements.push( self.cy().getElementById(id) );
				}
			});
			
			return new $$.CyCollection( this.cy(), elements );
		}
	});
	
	$$.fn.collection({
		add: function(toAdd){
			var self = this;			
			
			if( toAdd == null ){
				return this;
			}
			
			if( $$.is.string(toAdd) ){
				var selector = toAdd;
				toAdd = this.cy().elements(selector);
			}
			toAdd = toAdd.collection();
			
			var elements = [];
			var ids = {};
		
			function add(element){
				if( element == null ){
					return;
				}
				
				if( ids[ element.id() ] == null ){
					elements.push(element);
					ids[ element.id() ] = true;
				}
			}
			
			// add own
			this.each(function(i, element){
				add(element);
			});
			
			// add toAdd
			var collection = toAdd.collection();
			collection.each(function(i, element){
				add(element);
			});
			
			return new $$.CyCollection(this.cy(), elements);
		}
	});

	$$.fn.collection({
		neighborhood: function(selector){
			var elements = [];
			
			this.nodes().each(function(i, node){
				node.connectedEdges().each(function(j, edge){
					var otherNode = edge.connectedNodes().not(node).element();
					elements.push( otherNode ); // add node 1 hop away
					
					// add connected edge
					elements.push( edge.element() );
				});
			});
			
			return this.connectedNodes().add( new $$.CyCollection( this.cy(), elements ) ).filter( selector );
		}
	});
	$$.fn.collection({ neighbourhood: function(selector){ return this.neighborhood(selector); } });
	
	$$.fn.collection({
		closedNeighborhood: function(selector){
			return new $$.CySelector(this.cy(), selector).filter( this.neighborhood().add(this) );
		}
	});
	$$.fn.collection({ closedNeighbourhood: function(selector){ return this.closedNeighborhood(selector); } });
	
	$$.fn.collection({
		openNeighborhood: function(selector){
			return this.neighborhood(selector);
		}
	});
	$$.fn.collection({ openNeighbourhood: function(selector){ return this.openNeighborhood(selector); } });
	
	$$.fn.collection({
		source: function(){
			var ele = this.element();

			if( ele.isNode() ){
				$$.console.warn("Can call `source()` only on edges---tried to call on node `%s`", ele._private.data.id);
				return new $$.CyCollection( ele.cy() );
			}
			
			return ele.cy().getElementById( ele._private.data.source ).collection();
		}
	});
	
	$$.fn.collection({
		target: function(){
			var ele = this.element();
			
			if( ele.isNode() ){
				$$.console.warn("Can call `target()` only on edges---tried to call on node `%s`", ele._private.data.id);
				return new $$.CyCollection( ele.cy() );
			}
			
			return ele.cy().getElementById( ele._private.data.target ).collection();
		}
	});
	
	$$.fn.collection({
		edgesWith: defineEdgesWithFunction()
	});
	
	$$.fn.collection({
		edgesTo: defineEdgesWithFunction({
			include: function( node, otherNode, edgeStruct ){
				return edgeStruct.target.same( otherNode );
			}
		})
	});
	
	$$.fn.collection({
		edgesFrom: defineEdgesWithFunction({
			include: function( node, otherNode, edgeStruct ){
				return edgeStruct.source.same( node );
			}
		})
	});
	
	function defineEdgesWithFunction( params ){
		var defaults = {
			include: function( node, otherNode, edgeStruct ){
				return true;
			}
		};
		params = $.extend(true, {}, defaults, params);
		
		return function(otherNodes){
			var elements = [];
			
			this.nodes().each(function(i, node){
				otherNodes.nodes().each(function(j, otherNode){
					$.each( node.element()._private.edges[ otherNode.id() ], function(edgeId, edgeStruct){
						if( params.include( node, otherNode, edgeStruct ) ){
							elements.push( edgeStruct.edge );
						}
					} );
				});
			});
			
			return new $$.CyCollection( this.cy(), elements );
		};
	}
	
	$$.fn.collection({
		connectedEdges: function( selector ){
			var elements = [];
			
			this.nodes().each(function(i, node){
				$.each(node.element()._private.edges, function(otherNodeId, edgesById){
					$.each(edgesById, function(edgeId, edgeStruct){
						elements.push( edgeStruct.edge );
					});
				});
			});
			
			return new $$.CyCollection( this.cy(), elements ).filter( selector );
		}
	});
	
	$$.fn.collection({
		connectedNodes: function( selector ){
			var elements = [];
			
			this.edges().each(function(i, edge){
				elements.push( edge.source().element() );
				elements.push( edge.target().element() );
			});
			
			return new $$.CyCollection( this.cy(), elements ).filter( selector );
		}
	});
	
	$$.fn.collection({
		parallelEdges: defineParallelEdgesFunction()
	});
	
	$$.fn.collection({
		codirectedEdges: defineParallelEdgesFunction({
			include: function( source, target, edgeStruct ){
				return edgeStruct.source;
			}
		})
	});
	
	function defineParallelEdgesFunction(params){
		var defaults = {
			include: function( source, target, edgeStruct ){
				return true;
			}
		};
		params = $.extend(true, {}, defaults, params);
		
		return function( selector ){
			var elements = [];
			
			this.edges().each(function(i, edge){
				var src = edge.source().element();
				var tgt = edge.target().element();
				
				// look at edges between src and tgt
				$.each( src._private.edges[ tgt.id() ], function(id, edgeStruct){
					if( params.include(src, tgt, edgeStruct) ){
						elements.push( edgeStruct.edge );
					}
				});
			});
			
			return new $$.CyCollection( this.cy(), elements ).filter( selector );
		};
	
	}
	
})(jQuery, jQuery.cytoscapeweb);

;(function($){
	
	// define the json exporter
	function JsonExporter(options){
		this.options = options;
		this.cy = options.cy;
		this.renderer = options.renderer;
	}
	
	JsonExporter.prototype.run = function(){
		var elements = {};
		
		this.cy.elements().each(function(i, ele){
			var group = ele.group();
			
			if( elements[group] == null ){
				elements[group] = [];
			}
			
			elements[group].push( ele.json() );
		});
		
		return elements;
	};
	
	$.cytoscapeweb("exporter", "json", JsonExporter);
	
})(jQuery);

;(function($, $$){
		
	// CySelector
	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	function CySelector(cy, onlyThisGroup, selector){
		
		if( cy === undefined || cy.$ == null ){
			$$.console.error("A selector must have a reference to the core");
			return;
		}
		
		if( selector === undefined && onlyThisGroup !== undefined ){
			selector = onlyThisGroup;
			onlyThisGroup = undefined;
		}
		
		var self = this;
		
		self._private = {
			selectorText: null,
			invalid: true,
			cy: cy
		}
	
		function newQuery(){
			return {
				classes: [],
				colonSelectors: [],
				data: [],
				group: onlyThisGroup,
				ids: [],
				meta: [],
				collection: null,
				filter: null
			};
		}
		
		if( selector == null || ( $$.is.string(selector) && selector.match(/^\s*$/) ) ){
			
			if( onlyThisGroup == null ){
				// ignore
				self.length = 0;
			} else {
				
				// NOTE: need to update this with fields as they are added to logic in else if
				self[0] = newQuery();
				self.length = 1;
			}
							
		} else if( $$.is.element( selector ) ){
			var collection = new $$.CyCollection(self.cy(), [ selector ]);
			
			self[0] = newQuery();
			self[0].collection = collection;
			self.length = 1;
			
		} else if( $$.is.collection( selector ) ){
			self[0] = newQuery();
			self[0].collection = selector;
			self.length = 1;
			
		} else if( $$.is.fn(selector) ) {
			self[0] = newQuery();
			self[0].filter = selector;
			self.length = 1;
			
		} else if( $$.is.string(selector) ){
		
			// these are the actual tokens in the query language
			var metaChar = "[\\!\\\"\\#\\$\\%\\&\\\'\\(\\)\\*\\+\\,\\.\\/\\:\\;\\<\\=\\>\\?\\@\\[\\]\\^\\`\\{\\|\\}\\~]"; // chars we need to escape in var names, etc
			var variable = "(?:[\\w-]|(?:\\\\"+ metaChar +"))+"; // a variable name
			var comparatorOp = "=|\\!=|>|>=|<|<=|\\$=|\\^=|\\*="; // binary comparison op (used in data selectors)
			var boolOp = "\\?|\\!|\\^"; // boolean (unary) operators (used in data selectors)
			var string = '"(?:\\\\"|[^"])+"' + "|" + "'(?:\\\\'|[^'])+'"; // string literals (used in data selectors) -- doublequotes | singlequotes
			var number = "\\d*\\.\\d+|\\d+|\\d*\\.\\d+[eE]\\d+"; // number literal (used in data selectors) --- e.g. 0.1234, 1234, 12e123
			var value = string + "|" + number; // a value literal, either a string or number
			var meta = "degree|indegree|outdegree"; // allowed metadata fields (i.e. allowed functions to use from $$.CyCollection)
			var separator = "\\s*,\\s*"; // queries are separated by commas; e.g. edge[foo = "bar"], node.someClass
			var className = variable; // a class name (follows variable conventions)
			var id = variable; // an element id (follows variable conventions)
			
			// when a token like a variable has escaped meta characters, we need to clean the backslashes out
			// so that values get compared properly in CySelector.filter()
			function cleanMetaChars(str){
				return str.replace(new RegExp("\\\\(" + metaChar + ")", "g"), "\1");
			}
			
			// add @ variants to comparatorOp
			$.each( comparatorOp.split("|"), function(i, op){
				comparatorOp += "|@" + op;
			} );
			
			// NOTE: add new expression syntax here to have it recognised by the parser;
			// a query contains all adjacent (i.e. no separator in between) expressions;
			// the current query is stored in self[i] --- you can use the reference to `this` in the populate function;
			// you need to check the query objects in CySelector.filter() for it actually filter properly, but that's pretty straight forward
			var exprs = {
				group: {
					regex: "(node|edge)",
					populate: function( group ){
						this.group = group + "s";
					}
				},
				
				state: {
					regex: "(:selected|:unselected|:locked|:unlocked|:visible|:hidden|:grabbed|:free|:removed|:inside|:grabbable|:ungrabbable|:animated|:unanimated)",
					populate: function( state ){
						this.colonSelectors.push( state );
					}
				},
				
				id: {
					regex: "\\#("+ id +")",
					populate: function( id ){
						this.ids.push( cleanMetaChars(id) );
					}
				},
				
				className: {
					regex: "\\.("+ className +")",
					populate: function( className ){
						this.classes.push( cleanMetaChars(className) );
					}
				},
				
				dataExists: {
					regex: "\\[\\s*("+ variable +")\\s*\\]",
					populate: function( variable ){
						this.data.push({
							field: cleanMetaChars(variable)
						});
					}
				},
				
				dataCompare: {
					regex: "\\[\\s*("+ variable +")\\s*("+ comparatorOp +")\\s*("+ value +")\\s*\\]",
					populate: function( variable, comparatorOp, value ){
						this.data.push({
							field: cleanMetaChars(variable),
							operator: comparatorOp,
							value: value
						});
					}
				},
				
				dataBool: {
					regex: "\\[\\s*("+ boolOp +")\\s*("+ variable +")\\s*\\]",
					populate: function( boolOp, variable ){
						this.data.push({
							field: cleanMetaChars(variable),
							operator: boolOp
						});
					}
				},
				
				metaCompare: {
					regex: "\\{\\s*("+ meta +")\\s*("+ comparatorOp +")\\s*("+ number +")\\s*\\}",
					populate: function( meta, comparatorOp, number ){
						this.meta.push({
							field: cleanMetaChars(meta),
							operator: comparatorOp,
							value: number
						});
					}
				}
			};
			
			self._private.selectorText = selector;
			var remaining = selector;
			var i = 0;
			
			// of all the expressions, find the first match in the remaining text
			function consumeExpr(){
				var expr;
				var match;
				var name;
				
				$.each(exprs, function(n, e){
					var m = remaining.match(new RegExp( "^" + e.regex ));
					
					if( m != null ){
						match = m;
						expr = e;
						name = n;
						
						var consumed = m[0];
						remaining = remaining.substring( consumed.length );								
						
						return false;
					}
				});
				
				return {
					expr: expr,
					match: match,
					name: name
				};
			}
			
			// consume all leading whitespace
			function consumeWhitespace(){
				var match = remaining.match(/^\s+/);
				
				if( match ){
					var consumed = match[0];
					remaining = remaining.substring( consumed.length );
				}
			}
			
			// consume query separators
			function consumeSeparators(){
				var match = remaining.match(new RegExp( "^" + separator ));
				
				// if we've matched to a separator, consume it
				if( match ){
					var consumed = match[0];
					remaining = remaining.substring( consumed.length );
					self[++i] = newQuery();
				}
			}
			
			self[0] = newQuery(); // get started
			
			consumeWhitespace(); // get rid of leading whitespace
			for(;;){
				consumeSeparators();
				
				var check = consumeExpr();
				
				if( check.name == "group" && onlyThisGroup != null && self[i].group != onlyThisGroup ){
					$$.console.error("Group `%s` conflicts with implicit group `%s` in selector `%s`", self[i].group, onlyThisGroup, selector);
					return;
				}
				
				if( check.expr == null ){
					$$.console.error("The selector `%s` is invalid", selector);
					return;
				} else {
					var args = [];
					for(var j = 1; j < check.match.length; j++){
						args.push( check.match[j] );
					}
					
					// let the token populate the selector object (i.e. in self[i])
					check.expr.populate.apply( self[i], args );
				}
				
				// we're done when there's nothing left to parse
				if( remaining.match(/^\s*$/) ){
					break;
				}
			}
			
			self.length = i + 1;
			
		} else {
			$$.console.error("A selector must be created from a string; found %o", selector);
			return;
		}

		self._private.invalid = false;
		
	}
	$.cytoscapeweb.CySelector = CySelector; // expose
	
	CySelector.prototype.cy = function(){
		return this._private.cy;
	};
	
	CySelector.prototype.size = function(){
		return this.length;
	};
	
	CySelector.prototype.eq = function(i){
		return this[i];
	};
	
	// get elements from the core and then filter them
	CySelector.prototype.find = function(){
		// TODO impl
	};
	
	// filter an existing collection
	CySelector.prototype.filter = function(collection, addLiveFunction){
		var self = this;
		
		// don't bother trying if it's invalid
		if( self._private.invalid ){
			return new $$.CyCollection( self.cy() );
		}
		
		var selectorFunction = function(i, element){
			for(var j = 0; j < self.length; j++){
				var query = self[j];
				
				// check group
				if( query.group != null && query.group != element._private.group ){
					continue;
				}
				
				// check colon selectors
				var allColonSelectorsMatch = true;
				for(var k = 0; k < query.colonSelectors.length; k++){
					var sel = query.colonSelectors[k];
					var renderer = self.cy().renderer(); // TODO remove reference after refactoring
					
					switch(sel){
					case ":selected":
						allColonSelectorsMatch = element.selected();
						break;
					case ":unselected":
						allColonSelectorsMatch = !element.selected();
						break;
					case ":locked":
						allColonSelectorsMatch = element.locked();
						break;
					case ":unlocked":
						allColonSelectorsMatch = !element.locked();
						break;
					case ":visible":
						allColonSelectorsMatch = renderer.elementIsVisible(element);
						break;
					case ":hidden":
						allColonSelectorsMatch = !renderer.elementIsVisible(element);
						break;
					case ":grabbed":
						allColonSelectorsMatch = element.grabbed();
						break;
					case ":free":
						allColonSelectorsMatch = !element.grabbed();
						break;
					case ":removed":
						allColonSelectorsMatch = element.removed();
						break;
					case ":inside":
						allColonSelectorsMatch = !element.removed();
						break;
					case ":grabbable":
						allColonSelectorsMatch = element.grabbable();
						break;
					case ":ungrabbable":
						allColonSelectorsMatch = !element.grabbable();
						break;
					case ":animated":
						allColonSelectorsMatch = element.animated();
						break;
					case ":unanimated":
						allColonSelectorsMatch = !element.animated();
						break;
					}
					
					if( !allColonSelectorsMatch ) break;
				}
				if( !allColonSelectorsMatch ) continue;
				
				// check id
				var allIdsMatch = true;
				for(var k = 0; k < query.ids.length; k++){
					var id = query.ids[k];
					var actualId = element._private.data.id;
					
					allIdsMatch = allIdsMatch && (id == actualId);
					
					if( !allIdsMatch ) break;
				}
				if( !allIdsMatch ) continue;
				
				// check classes
				var allClassesMatch = true;
				for(var k = 0; k < query.classes.length; k++){
					var cls = query.classes[k];
					
					allClassesMatch = allClassesMatch && element.hasClass(cls);
					
					if( !allClassesMatch ) break;
				}
				if( !allClassesMatch ) continue;
				
				// generic checking for data/metadata
				function operandsMatch(params){
					var allDataMatches = true;
					for(var k = 0; k < query[params.name].length; k++){
						var data = query[params.name][k];
						var operator = data.operator;
						var value = data.value;
						var field = data.field;
						var matches;
						
						if( operator != null && value != null ){
							
							var fieldStr = "" + params.fieldValue(field);
							var valStr = "" + eval(value);
							
							var caseInsensitive = false;
							if( operator.charAt(0) == "@" ){
								fieldStr = fieldStr.toLowerCase();
								valStr = valStr.toLowerCase();
								
								operator = operator.substring(1);
								caseInsensitive = true;
							}
							
							if( operator == "=" ){
								operator = "==";
							}
							
							switch(operator){
							case "*=":
								matches = fieldStr.search(valStr) >= 0;
								break;
							case "$=":
								matches = new RegExp(valStr + "$").exec(fieldStr) != null;
								break;
							case "^=":
								matches = new RegExp("^" + valStr).exec(fieldStr) != null;
								break;
							default:
								// if we're doing a case insensitive comparison, then we're using a STRING comparison
								// even if we're comparing numbers
								if( caseInsensitive ){
									// eval with lower case strings
									var expr = "fieldStr " + operator + " valStr";
									matches = eval(expr);
								} else {
									// just eval as normal
									var expr = params.fieldRef(field) + " " + operator + " " + value;
									matches = eval(expr);
								}
								
							}
						} else if( operator != null ){
							switch(operator){
							case "?":
								matches = params.fieldTruthy(field);
								break;
							case "!":
								matches = !params.fieldTruthy(field);
								break;
							case "^":
								matches = params.fieldUndefined(field);
								break;
							}
						} else { 	
							matches = !params.fieldUndefined(field);
						}
						
						if( !matches ){
							allDataMatches = false;
							break;
						}
					} // for
					
					return allDataMatches;
				} // operandsMatch
				
				// check data matches
				var allDataMatches = operandsMatch({
					name: "data",
					fieldValue: function(field){
						return element._private.data[field];
					},
					fieldRef: function(field){
						return "element._private.data." + field;
					},
					fieldUndefined: function(field){
						return element._private.data[field] === undefined;
					},
					fieldTruthy: function(field){
						if( element._private.data[field] ){
							return true;
						}
						return false;
					}
				});
				
				if( !allDataMatches ){
					continue;
				}
				
				// check metadata matches
				var allMetaMatches = operandsMatch({
					name: "meta",
					fieldValue: function(field){
						return element[field]();
					},
					fieldRef: function(field){
						return "element." + field + "()";
					},
					fieldUndefined: function(field){
						return element[field]() == undefined;
					},
					fieldTruthy: function(field){
						if( element[field]() ){
							return true;
						}
						return false;
					}
				});
				
				if( !allMetaMatches ){
					continue;
				}
				
				// check collection
				if( query.collection != null ){
					var matchesAny = query.collection._private.ids[ element.id() ] != null;
					
					if( !matchesAny ){
						continue;
					}
				}
				
				// check filter function
				if( query.filter != null && element.collection().filter( query.filter ).size() == 0 ){
					continue;
				}
				
				// we've reached the end, so we've matched everything for this query
				return true;
			}
			
			return false;
		};
		
		if( self._private.selectorText == null ){
			selectorFunction = function(){ return true; };
		}
		
		var filteredCollection = collection.filter(selectorFunction);
		
		if(addLiveFunction){
			
			var key = self.selector();
			var structs = self.cy()._private; // TODO remove ref to `structs` after refactoring
			
			filteredCollection.live = function(events, data, callback){
				
				var evts = events.split(/\s+/);
				$.each(evts, function(i, event){
				
					if( $$.is.emptyString(event) ){
						return;
					}
					
					if( callback === undefined ){
						callback = data;
						data = undefined;
					}
					
					if( structs.live[event] == null ){
						structs.live[event] = {};
					}
					
					if( structs.live[event][key] == null ){
						structs.live[event][key] = [];
					}
					
					structs.live[event][key].push({
						callback: callback,
						data: data
					});
					
				});						
				
				return this;
			};
			
			filteredCollection.die = function(event, callback){
				if( event == null ){
					$.each(structs.live, function(event){
						if( structs.live[event] != null ){
							delete structs.live[event][key];
						}
					});
				} else {
					var evts = event.split(/\s+/);
					
					$.each(evts, function(j, event){
						if( callback == null ){
							if( structs.live[event] != null ){
								delete structs.live[event][key];
							}
						} else if( structs.live[event] != null && structs.live[event][key] != null ) {
							for(var i = 0; i < structs.live[event][key].length; i++){
								if( structs.live[event][key][i].callback == callback ){
									structs.live[event][key].splice(i, 1);
									i--;
								}
							}
						}
					});
					
				}
				
				return this;
			};
		}
		
		return filteredCollection;
	};
	
	// ith query to string
	CySelector.prototype.toString = CySelector.prototype.selector = function(){
		
		var str = "";
		
		function clean(obj){
			if( $$.is.string(obj) ){
				return obj;
			} 
			return "";
		}
		
		for(var i = 0; i < this.length; i++){
			var query = this[i];
			
			var group = clean(query.group);
			str += group.substring(0, group.length - 1);
			
			for(var j = 0; j < query.data.length; j++){
				var data = query.data[j];
				str += "[" + data.field + clean(data.operator) + clean(data.value) + "]"
			}
			
			for(var j = 0; j < query.colonSelectors.length; j++){
				var sel = query.colonSelectors[i];
				str += sel;
			}
			
			for(var j = 0; j < query.ids.length; j++){
				var sel = "#" + query.ids[i];
				str += sel;
			}
			
			for(var j = 0; j < query.classes.length; j++){
				var sel = "." + query.classes[i];
				str += sel;
			}
			
			if( this.length > 1 && i < this.length - 1 ){
				str += ", ";
			}
		}
		
		return str;
	};
	
})(jQuery, jQuery.cytoscapeweb);
