function View() {
	this.eventEndpoint_ = null;
	this.joinEndpoint_ = null;
	this.startEndpoint_ = null;
	this.gameNamespace_ = "";
	this.viewNamespace_ = "";
	this.channel_ = null;
	this.socket_ = null;
	this.userToken_ = "";
	this.socketConnected_ = false;
	
	if(document.selectNodes) {
		document.setProperty("SelectionLanguage", "XPath");
	}
	
	var self = this;
	window.onload = function() { self.init(); }
}
View.instance_ = null;
View.getInstance = function() {
	if(View.instance_ == null) {
		View.instance_ = new View();
	}
	return View.instance_;
}
View.prototype.init = function() {
	this.createChannel();
}
View.prototype.reload = function() {
	window.location.reload();
}
View.prototype.handleSocketMessage = function(msg) {
	console.log("entire socket message: " + msg.data);	
	this.reload();
}
View.prototype.handleSocketError = function() {
	
}
View.prototype.handleSocketClose = function() {
	self.connected_ = false;
}
View.prototype.createChannel = function() {
	if(this.userToken_ == "") return;
	
	this.channel_ = new goog.appengine.Channel(this.userToken_);
	this.socket_ = this.channel_.open();
	
	var self = this;
	this.socket_.onopen = function() {
		self.socketConnected_ = true;
	}
	this.socket_.onmessage = function() {
		self.handleSocketMessage.apply(self, arguments);
	}
	this.socket_.onerror = function() {
		self.handleSocketError.apply(self, arguments);
	}
	this.socket_.onclose = function() {
		self.handleSocketClose.apply(self, arguments);
	}
}
View.prototype.setGameNamespace = function(str) {
	this.gameNamespace_ = str;
}
View.prototype.setUserToken = function(token) {
	this.userToken_ = token;
}
View.prototype.setViewNamespace = function(str) {
	this.viewNamespace_ = str;
}
View.prototype.registerEventEndpoint = function(obj) {
	this.eventEndpoint_ = obj;
};
View.prototype.registerStartEndpoint = function(obj) {
	this.startEndpoint_ = obj;
};
View.prototype.registerJoinEndpoint = function(obj) {
	this.joinEndpoint_ = obj;
};
View.prototype.handleEventSuccess = function(el, event, responseXML) {
	this.reload();
}
View.prototype.handleEventError = function(el, event, responseXML) {
	var el = responseXML.getElementsByTagNameNS(this.gameNamespace_, "error");
	
	var message = "unknown";
	if(el.length > 0) {
		message = nodeText(el[0]);
	}
	alert("there was an error: " + message);
}
View.prototype.createAttributeDict = function(node) {
	var dict = new Object();
	for(var i = 0; i < node.attributes.length; i++) {
		var a = node.attributes[i];
		dict[a.localName] = a.nodeValue;
	}
	return dict;
}
View.prototype.fireEvent = function(el, eventid) {
	console.log("firing event.");
	if(this.eventEndpoint_ == null) return;
	
	var event = document.getElementById(eventid);
	if(!event && document.selectNodes) {
		console.log("selectNodes found...");
		var nodes = document.selectNodes("*[@id = '" + eventid + "']");
		if(nodes.length > 0) event = nodes[0];
	}
	
	if(!event) {
		console.log("event not found.");
		return;
	}
	
	var data = "";
	var dict = this.createAttributeDict(event);
	var url = "";
	
	if(dict["gameEvent"] == "start") {
		url = replacePattern(this.startEndpoint_.url, dict);
	}		
	else if(dict["gameEvent"] == "join") {
		url = replacePattern(this.joinEndpoint_.url, dict);
	}
	else {
		url = replacePattern(this.eventEndpoint_.url, dict);
	}
	
	var ser = new XMLSerializer();
	for(var i = 0; i < event.childNodes.length; i++) {
		var n = event.childNodes[i];
		
		data = data + ser.serializeToString(n);
	}
		
	console.log("data: " + data);
	
	var self = this;
	
	var xhr = new XMLHttpRequest();
	xhr.open(this.eventEndpoint_.method, url, true);
	xhr.onreadystatechange = function() {
		if(xhr.readyState == 4) {
			if(xhr.status == 200) {
				self.handleEventSuccess(el, event, xhr.responseXML);
			}
			else if(xhr.status == 400) {
				self.handleEventError(el, event, xhr.responseXML);
			}
		}
		
	};
	xhr.send(data);
};
