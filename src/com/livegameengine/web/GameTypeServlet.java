package com.livegameengine.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jdo.PersistenceManager;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import com.livegameengine.error.GameLoadException;
import com.livegameengine.model.Game;

import com.livegameengine.model.GameType;
import com.livegameengine.model.GameUser;
import com.livegameengine.persist.PMF;
import com.livegameengine.persist.PersistenceCommand;
import com.livegameengine.persist.PersistenceCommandException;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class GameTypeServlet extends HttpServlet {
	private Log log = LogFactory.getLog(GameTypeServlet.class);
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		UserService userService = UserServiceFactory.getUserService();
		
		boolean authenticated = (req.getUserPrincipal() != null);
		
		Pattern p = Pattern.compile("/t/([^/]+)/");
		
		final Matcher m = p.matcher(req.getPathInfo());
		
		if(m != null && m.matches()) {
			GameType t = null;
			try {
				t = (GameType)PMF.executeCommand(new PersistenceCommand() {
					
					@Override
					public Object exec(PersistenceManager pm) {
						Key k = KeyFactory.stringToKey(m.group(1));
						
						return pm.getObjectById(GameType.class, k);
					}
				});
			} catch (PersistenceCommandException e1) {
				t = null;
			}
			
			RequestDispatcher dispatcher = req.getRequestDispatcher("/WEB-INF/type.jsp");
			
			req.setAttribute("gameType", t);
			
			try {
				dispatcher.forward(req, resp);
			}
			catch(ServletException e) {
				throw new IOException(e);
			}
		}
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		log.info("pathinfo: " + req.getPathInfo());
		
		UserService userService = UserServiceFactory.getUserService();
		
		boolean authenticated = (req.getUserPrincipal() != null);
		
		
		if(!authenticated) {
			resp.setStatus(403);
			resp.getOutputStream().write("You are not signed in".getBytes());
			return;
		}		

		Pattern p = Pattern.compile("/-/([^/]+)/([^/]+)");
		
		
		final Matcher m = p.matcher(req.getPathInfo());
		
		if(req.getPathInfo().equals("/create")) {			
			GameType t = new GameType();
			
			GameUser gu = GameUser.findOrCreateGameUserByUser(userService.getCurrentUser());
			
			t.setCreator(gu);
			t.setDescription("Test");
			t.setTypeName("Test");
			t.setClientVersion("0.1");
							
			URL u = GameServlet.class.getResource("/tictac.xml");
			URLConnection conn = u.openConnection();
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			InputStream is = conn.getInputStream();
			byte[] buffer = new byte[0x1000];
			
			int count = 0;
			while((count = is.read(buffer)) > 0) {
				bos.write(buffer, 0, count);
			}
			
			t.setStateChart(bos.toByteArray());
			
			t.makePersistent();
			
			resp.sendRedirect("-/" + KeyFactory.keyToString(t.getKey()) + "/");
		}
		else if(m != null && m.matches()) {
			GameType t = null;
			try {
				t = (GameType)PMF.executeCommand(new PersistenceCommand() {
					
					@Override
					public Object exec(PersistenceManager pm) {
						Key k = KeyFactory.stringToKey(m.group(1));
						
						return pm.getObjectById(GameType.class, k);
					}
				});
			} catch (PersistenceCommandException e1) {
				t = null;
			}
			
			if(t == null) {
				resp.sendError(404);
				resp.getOutputStream().write(("game type not found: " + m.group(1)).getBytes());
				return;
			}
			
			if(m.group(2).equals("create")) {												
				Game h = null;
				try {
					h = new Game(t);
				} catch (GameLoadException e) {
					resp.setStatus(500);
					resp.setContentType("text/plain");
					resp.getWriter().println("Could not load game type: " + m.group(1));
					return;
				}
				
				h.setCreated(new Date());
				h.setOwner(userService.getCurrentUser());				
				h.addWatcher(userService.getCurrentUser());
				
				h.makePersistent();
				
				resp.sendRedirect(String.format("/game/-/%s/", KeyFactory.keyToString(h.getKey())));
			}
		}
		else {
			resp.setStatus(404);
			resp.getOutputStream().write(("URL Not found: " + req.getPathInfo()).getBytes());
		}
	}
}
