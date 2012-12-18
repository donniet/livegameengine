
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

function dateFromString(s) {
	return new Date(s);
	/*
	var bits = s.split(/[-T:]/g);
	var d = new Date(bits[0], bits[1]-1, bits[2]);
	d.setHours(bits[3], bits[4], bits[5]);
	
	return d;
	*/
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
	
	var months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

	var yyyy = date.getUTCFullYear();
	var MMM  = months[date.getUTCMonth()];
	//var mm1  = pad(date.getMonth()+1);
	var dd   = pad(date.getUTCDate());
	var hh   = pad(date.getUTCHours());
	var mm2  = pad(date.getUTCMinutes());
	var ss   = pad(date.getUTCSeconds());
	var ssss = pad(date.getUTCMilliseconds(),4);
	
	return dd + ' ' + MMM + ' ' + yyyy + ' ' + hh + ':' + mm2 + ':' + ss + '.' + ssss +  'UTC';
	//return yyyy +'-' +mm1 +'-' +dd +'T' +hh +':' +mm2 +':' +ss +'.' +ssss;
}

function ClientMessageChannel(messagesUrl, since) {
	this.messagesUrl = messagesUrl;
	this.messagesMethod = "GET";
	this.interval = null;
	this.requestInProgress = false;
	this.since = since;
	
	this.intervalTime = 2000;
}
ClientMessageChannel.prototype.open = function() {
	console.log("channel openning...");
	
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
			else if(xhr.status >= 500) {
				// server error
				self.onerror();
			}
			self.requestInProgress = false;
		}
	}
	xhr.send();
}
ClientMessageChannel.prototype.onclientmessages = function(messagesXml) {
	console.log("found some messages!" + messagesXml);
	
	for(var i = 0; i < messagesXml.childNodes.length; i++) {
		var n = messagesXml.childNodes[i];
		
		if(n.localName == "messages") {
			var latestNode = n.attributes["latestDate"];
			if(latestNode) {
				this.since = dateFromString(latestNode.value);
			}
		}
	}
}
ClientMessageChannel.prototype.onerror = function(errorXml) {
	console.log("there was an error, shutting down...");
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
		
		if(typeof this.handlers_[h.event] == "undefined") {
			this.handlers_[h.event] = new Array();
		}
		
		this.handlers_[h.event].push(h);
	}
}
ViewConstructor.prototype.handleEventSuccess = function(el, event, responseXML) {
	console.log("event success: " + (responseXML ? responseXML.toString() : "null"));
	//this.channel.oninterval();
}
ViewConstructor.prototype.handleEventError = function(el, event, responseXML) {
	console.log("event error: " + responseXML ? responseXML.toString() : "null");
}

ViewConstructor.prototype.trigger = function(el, eventid) {
	var e = this.events_[eventid];
	
	if(typeof e == "undefined") {
		console.log("unknown event id: '" + eventid + "'");
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
	console.log("View loading...");
	if(this.clientMessageChannelUrl_ != null) {
		if(this.serverLoadTime_ == null) this.serverLoadTime_ = new Date();
		
		this.channel = new ClientMessageChannel(this.clientMessageChannelUrl_, this.serverLoadTime_);
		this.channel.open();
	}
}
View = new ViewConstructor();