
function dateFromString(s) {
	var bits = s.split(/[-T:]/g);
	var d = new Date(bits[0], bits[1]-1, bits[2]);
	d.setHours(bits[3], bits[4], bits[5]);
	
	return d;
}
function stringFromDate(d) {
	function pad(n) {
		var s = n.toString();
		return s.length < 2 ? '0'+s : s;
	};

	var yyyy = date.getFullYear();
	var mm1  = pad(date.getMonth()+1);
	var dd   = pad(date.getDate());
	var hh   = pad(date.getHours());
	var mm2  = pad(date.getMinutes());
	var ss   = pad(date.getSeconds());
	
	return yyyy +'-' +mm1 +'-' +dd +'T' +hh +':' +mm2 +':' +ss;
}

function ClientMessageChannel(messagesUrl, since) {
	this.messagesUrl = messagesUrl;
	this.messagesMethod = "GET";
	this.interval = null;
	this.requestInProgress = false;
	this.since = since;
	
	this.intervalTime = 500;
}
ClientMessageChannel.prototype.open = function() {
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
			else if(xhr.status == 304) {
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
}
ClientMessageChannel.prototype.onclientmessage = function(messagesXml) {
	console.log("found some messages!" + messagesXml);
	
	for(var i = 0; i < messagesXML.childNodes.length; i++) {
		var n = messagesXML.childNodes[i];
		
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
	
	var self = this;
	window.onload = function() { self.handleLoad(); }
}
ViewConstructor.prototype.addEvents = function(arr) {
	for(var i = 0; i < arr.length; i++) {
		var e = arr[i];
		
		this.events_[e.id] = e;
	}
}
ViewConstructor.prototype.addEventHandlers = function(arr) {
	for(var i = 0; i < arr.length; i++) {
		var h = arr[i];
		
		if(typeof this.handlers_[h.event] == "undefined") {
			this.handlers_[h.event] = new Array();
		}
		
		this.handlers_[h.event].push(h);
	}
}
ViewConstructor.prototype.setClientMessageChannelUrl = function(url) {
	this.clientMessageChannelUrl_ = url;
}
ViewConstructor.prototype.setServerLoadTime = function(d) {
	this.serverLoadTime_ = dateFromString(d);
}
ViewConstructor.prototype.handleLoad = function() {
	if(this.clientMessageChannelUrl_ != null) {
		if(this.serverLoadTime_ == null) this.serverLoadTime_ = new Date();
		
		this.channel = new ClientMessageChannel(this.clientMessageChannelUrl_, this.serverLoadTime_);
		this.channel.open();
	}
}
View = new ViewConstructor();