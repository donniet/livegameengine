
function replacePattern(pattern, dict) {
	var m = null;
	var start = 0;
	var ret = "";
	while((m = replacePattern.TemplatingRegex.exec(pattern)) !== null) {
		ret += pattern.substring(start, m.index);
		
		var str = "";
		
		with(dict) {
			try {
				str = eval(m[1]);
			}
			catch(e) {
				str = "[error: '" + e.toString() + "']";
			}
		}
				
		ret += str;
		start = m.index + m[0].length;
	}
	if(start < pattern.length) {
		ret += pattern.substring(start, pattern.length);
	}
	
	return ret;
}
replacePattern.TemplatingRegex = new RegExp("\{([^\}]+)\}", "g");

var iso8601datepattern = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})(\.\d+)((([-+])(\d{2}):(\d{2}))|UTC|GMT|Z)$/;

function dateFromString(s) {
	var m = iso8601datepattern.exec(s);
	
	if(m) {	
		//console.log("millis: " + m[7]);
		
		var millis = Math.floor(1000.0 * parseFloat(m[7]));
		
		//console.log("millis calc: " + millis);
		
		var d = Date.UTC(
			parseInt(m[1]), parseInt(m[2])-1, parseInt(m[3]),
			parseInt(m[4]), parseInt(m[5]), parseInt(m[6]), millis);
		
		switch(m[8]) {
		case "UTC":
		case "GMT":
		case "Z":
			break;
		default:
			var minutes = parseInt(m[11]) * 60 + parseInt(m[12]);
			
			if(m[10] == "-") {
				d -= minutes * 60000;
			}
			else {
				d += minutes * 60000;
			}
			break;
		}
		
		return new Date(d);
	}
	
	return null;
}
function stringFromDate(date) {
	function pad(n, digs) {
		if(typeof digs == "undefined") digs = 2;
		
		var s = n.toString();
		while(s.length < digs) {
			s = '0' + s;
		}
		return s;
	};
	
	//var months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

	var yyyy = date.getUTCFullYear();
	//var MMM  = months[date.getUTCMonth()];
	var MM  = pad(date.getMonth()+1);
	var dd   = pad(date.getUTCDate());
	var hh   = pad(date.getUTCHours());
	var mm  = pad(date.getUTCMinutes());
	var ss   = pad(date.getUTCSeconds());
	var ssss = pad(date.getUTCMilliseconds(),3);
	
	//return dd + ' ' + MMM + ' ' + yyyy + ' ' + hh + ':' + mm2 + ':' + ss + '.' + ssss +  'GMT';
	//return yyyy +'-' +mm1 +'-' +dd +'T' +hh +':' +mm2 +':' +ss +'.' +ssss;
	return yyyy + "-" + MM + "-" + dd + "T" + hh + ":" + mm + ":" + ss + "." + ssss + "GMT";
}


function nodeText(node) {
	if(!node) return null;
	
	var ret = "";
	for(var i = 0; i < node.childNodes.length; i++) {
		var n = node.childNodes[i];
		if(n.nodeType == Node.TEXT_NODE) {
			ret += n.nodeValue;
		}
	}
	return ret;
}

function emptyNode(node) {
	while(node.firstChild) {
		node.removeChild(node.firstChild);
	}
}

function forChildNodes(node, nodehandlers, scope) {
	if(!scope) scope = this;
	
	if(node && nodehandlers) for(var i = 0; i < node.childNodes.length; i++) {
		var n = node.childNodes[i];
		if(n.localName) {
			var handler = nodehandlers[n.localName];
			if(typeof handler == "function") {
				handler.apply(scope, [n]);
			}
		}
	}
}

function ClientMessage(n) {
	this.messageDate = null;
	this.key = null;
	this.event = null;
	this.params = new Object();
	this.content = null;
			
	this.parse(n);
}
ClientMessage.prototype.parse = function(node) {
	if(!node) return;
	
	var keyAttr = node.attributes["key"];
	if(keyAttr) this.key = keyAttr.nodeValue;
	
	forChildNodes(node, {
		"messageDate": function(node) {
			this.messageDate = dateFromString(nodeText(node));
		},
		"event": function(node) {
			this.event = nodeText(node);
		},
		"param": function(node) {
			var nameAttr = node.attributes["name"];
			//console.log("nameAttr: " + nameAttr);
			//console.log("this.params: " + this.params);
			if(nameAttr) {
				this.params[nameAttr.nodeValue] = nodeText(node);
			}
		},
		"content": function(node) {
			this.content = node;
		}
	}, this);
}

function ClientMessageChannel(messagesUrl, since) {
	this.messagesUrl = messagesUrl;
	this.messagesMethod = "GET";
	this.interval = null;
	this.requestInProgress = false;
	this.since = since;
	
	this.intervalTime = 2000;
	
	// add a millisecond buffer on all latest messages
	this.messageDelta = 1;
}
ClientMessageChannel.prototype.open = function() {
	//console.log("channel openning...");
	
	if(this.interval != null) return;
	
	var self = this;
	this.interval = setInterval(function() {
		self.oninterval();
	}, this.intervalTime)
}
ClientMessageChannel.prototype.close = function() {
	if(this.interval == null) return;
	
	clearInterval(this.interval);
	this.interval = null;
}
ClientMessageChannel.prototype.oninterval = function() {
	if(this.requestInProgress) return;
	
	this.requestInProgress = true;
	
	var xhr = new XMLHttpRequest();
	xhr.open(this.messagesMethod, this.messagesUrl + "?since=" + escape(stringFromDate(this.since)), true);
	
	var self = this;
	xhr.onreadystatechange = function() {
		//console.log("ready state: " + xhr.readyState);
		//console.log("status: " + xhr.status);
		
		if(xhr.readyState == 4) {
			if(xhr.status == 200) {
				self.onclientmessages(xhr.responseXML);
			}
			else if(xhr.status == 204) {
				// do nothing, no additional data
			}
			else if(xhr.status >= 400 && xhr.status < 500) {
				self.onerror(xhr.responseXML);
			}
			else if(xhr.status < 200 || xhr.status >= 500) {
				// server error
				self.onerror();
			}
			self.requestInProgress = false;
		}
	}
	
	try {
		xhr.send();
	}
	catch(e) {
		console.log("error openning connection, shutting down");
		this.close();
	}
}
ClientMessageChannel.prototype.onclientmessages = function(messagesXml) {
	var ser = new XMLSerializer();
	
	//console.log("messages: " + ser.serializeToString(messagesXml));
	
	forChildNodes(messagesXml, {
		"messages": function(messagesNode) {
			//console.log("latestDate: " +  messagesNode.attributes["latestDate"].nodeValue);
			
			var latestNode = messagesNode.attributes["latestDate"];
			if(latestNode) {
				var d = dateFromString(latestNode.nodeValue);
				
				this.since = new Date(d.getTime() + this.messageDelta);
				
				//console.log("this.since == " + stringFromDate(this.since));
			}
			
			forChildNodes(messagesNode, {
				"message": function(messageNode) {
					Event.fire(this, "clientmessage", [new ClientMessage(messageNode)]);
				}
			}, this);
		}
	}, this);
}
ClientMessageChannel.prototype.handleClientMessage
ClientMessageChannel.prototype.onerror = function(errorXml) {
	//console.log("there was an error, shutting down...");
	this.close();
}

function ViewConstructor() {	
	this.channel = null;
	this.clientMessageChannelUrl_ = null;
	this.serverLoadTime_ = new Date();
	this.events_ = new Object();
	this.handlers_ = new Object();
	this.eventEndpoint_ = null;
	this.gameEventEndpoint_ = null;
	
	var self = this;
	window.onload = function() { self.handleLoad(); }
}
ViewConstructor.prototype.setEventEndpoint = function(obj) {
	this.eventEndpoint_ = obj;
}
ViewConstructor.prototype.setGameEventEndpoint = function(obj) {
	this.gameEventEndpoint_ = obj;
}
ViewConstructor.prototype.registerEvents = function(arr) {
	for(var i = 0; i < arr.length; i++) {
		var e = arr[i];
		
		this.events_[e.id] = e;
	}
}
ViewConstructor.prototype.registerEventHandlers = function(arr) {
	for(var i = 0; i < arr.length; i++) {
		var h = arr[i];
		
		console.log("registering handler: " + h.event);
		
		if(typeof this.handlers_[h.event] == "undefined") {
			this.handlers_[h.event] = new Array();
		}
		
		this.handlers_[h.event].push(h);
	}
}
ViewConstructor.prototype.parseMessageResponse = function(parent, messageContent) {
	if(messageContent.namespaceURI == this.gameViewNamespace) {
		// handle these separately.
	}
	else {
		if(messageContent.namespaceURI != "") {}
	}
}
ViewConstructor.prototype.handleClientMessage = function(clientMessage) {
	//console.log("handling client message: " + clientMessage);
	
	if(!clientMessage) return;
	
	console.log("clientMessage event: " + clientMessage.event);
	
	var handlers = this.handlers_[clientMessage.event];
	
	console.log("handler: " + handlers);
	
	if(handlers && handlers.length > 0) {
		for(var i = 0; i < handlers.length; i++) {
			var h = handlers[i];
			
			if(h.condition != "") {
				console.log("condition: " + h.condition);
				var r = false;
				with(clientMessage) {
					try {
						r = eval(h.condition);
					}
					catch(e) {
						console.log("condition error: " + e);
						r = false;
					}
				}
				
				if(!r) {
					continue;
				}
			}
			
			//console.log("found a handler: " + h);
			
			var el = document.getElementById(h.contentId);
			
			if(!el) continue;
			
			if(h.mode == "replace") {
				emptyNode(el);
			}
			
			//TODO: replace this with actual parsing of the content.
			var adopted = document.importNode(clientMessage.content);
			
			el.appendChild(adopted);
		}
	}
}

ViewConstructor.prototype.handleEventSuccess = function(el, event, responseXML) {
	//console.log("event success: " + (responseXML ? responseXML.toString() : "null"));
	//this.channel.oninterval();
}
ViewConstructor.prototype.handleEventError = function(el, event, responseXML) {
	//console.log("event error: " + responseXML ? responseXML.toString() : "null");
}

ViewConstructor.prototype.trigger = function(el, eventid) {
	var e = this.events_[eventid];
	
	if(typeof e == "undefined") {
		//console.log("unknown event id: '" + eventid + "'");
		return;
	}
	
	var url = "";
	var method = "POST";
	if(!e.gameEvent || e.gameEvent == "") {		
		url = replacePattern(this.eventEndpoint_.url, e);
		method = this.eventEndpoint_.method;
	}
	else {
		url = replacePattern(this.gameEventEndpoint_.url, e);
		method = this.gameEventEndpoint_.method;
	}
	
	
	var self = this;
	
	var xhr = new XMLHttpRequest();
	xhr.open(method, url, true);
	xhr.onreadystatechange = function() {
		if(xhr.readyState == 4) {
			if(xhr.status == 200) {
				self.handleEventSuccess(el, e, xhr.responseXML);
			}
			else if(xhr.status == 400) {
				self.handleEventError(el, e, xhr.responseXML);
			}
		}
		
	};
	xhr.send(e.payload);
}
ViewConstructor.prototype.setClientMessageChannelUrl = function(url) {
	this.clientMessageChannelUrl_ = url;
}
ViewConstructor.prototype.setServerLoadTime = function(d) {
	this.serverLoadTime_ = dateFromString(d);
}
ViewConstructor.prototype.handleLoad = function() {
	var self = this;
	
	//console.log("View loading...");
	if(this.clientMessageChannelUrl_ != null) {
		if(this.serverLoadTime_ == null) this.serverLoadTime_ = new Date();
		
		this.channel = new ClientMessageChannel(this.clientMessageChannelUrl_, this.serverLoadTime_);
		Event.addListener(this.channel, "clientmessage", function(clientMessage) {
			self.handleClientMessage(clientMessage);
		});
		this.channel.open();
	}
}
View = new ViewConstructor();