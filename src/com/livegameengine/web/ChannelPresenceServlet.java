package com.livegameengine.web;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.ObjectState;
import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.channel.ChannelPresence;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.livegameengine.model.GameUser;
import com.livegameengine.model.Watcher;

public class ChannelPresenceServlet extends HttpServlet {
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		// In the handler for _ah/channel/connected/
		ChannelService channelService = ChannelServiceFactory.getChannelService();
		ChannelPresence presence = channelService.parsePresence(req);
		
		PersistenceManager pm = null;
		
		String channelKey = presence.clientId();
		
		Watcher w = Watcher.findWatcherByChannelKey(channelKey);

		if(w == null) return;
		
		GameUser gu = w.getGameUser();
		
		gu.setConnected(presence.isConnected());

		pm = JDOHelper.getPersistenceManager(gu);
		pm.makePersistent(gu);
		
		if(!presence.isConnected()) {
			pm = JDOHelper.getPersistenceManager(w);
			
			pm.deletePersistent(w);
		}
	}
}
