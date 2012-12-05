package com.livegameengine.web;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;

import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.utils.SystemProperty;
import com.livegameengine.config.Config;
import com.livegameengine.error.GameLoadException;
import com.livegameengine.model.Game;
import com.livegameengine.model.GameState;
import com.livegameengine.model.GameUser;
import com.livegameengine.model.Watcher;
import com.livegameengine.persist.PMF;
import com.livegameengine.persist.PersistenceCommand;
import com.livegameengine.persist.PersistenceCommandException;
import com.sun.org.apache.xerces.internal.dom.DocumentTypeImpl;

public class GameServlet extends HttpServlet {
	Log log = LogFactory.getLog(GameServlet.class);
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		handleRequest(req, resp);
	}
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		handleRequest(req, resp);
	}

	public void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		UserService userService = UserServiceFactory.getUserService();
		User u = userService.getCurrentUser();
		GameUser gu = GameUser.findOrCreateGameUserByUser(u);		
		
		Pattern p = Pattern.compile("/-/([^/]+)/(meta|data|raw_view|join|start|(event/([^/]+)))?");
		
		final Matcher m = p.matcher(req.getPathInfo());
		
		log.info("path info: " + req.getPathInfo());
		
		if(m != null && m.matches()) {
			Game g;
			try {
				g = Game.findGameByKey(KeyFactory.stringToKey(m.group(1)));
			} catch (GameLoadException e1) {
				g = null;
			}
			
			if(g == null) {
				resp.setStatus(404);
				resp.setContentType("text/plain");
				resp.getWriter().write("game not found: " + m.group(1));
				return;
			}
			
			GameState gs = g.getMostRecentState();
			
			if(gs == null) {
				resp.setStatus(404);
				resp.setContentType("text/plain");
				resp.getWriter().write("game has no recent state: " + m.group(1));
				return;				
			}
			
			Document doc = null;
			Document doc1 = null;
			
			try {
				doc = Config.getInstance().newXmlDocument();
				doc1 = Config.getInstance().newXmlDocument();
				
				OutputStream responseStream = resp.getOutputStream();
				StreamResult responseResult = new StreamResult(responseStream);
				
				if(m.group(2) == null) {
					if(req.getMethod().equals("GET")) {
						resp.setContentType("application/xhtml+xml");
						Config.getInstance().transformDatamodel(gs, new DOMResult(doc), gu.getHashedUserId());
						Config.getInstance().transformFromResource("/tictactoe_view2.xslt", new DOMSource(doc), new DOMResult(doc1));
						
						Map<String,Object> params = new HashMap<String,Object>();
						
						params.put("eventEndpointUrl", "event/{endpointEvent}");
						params.put("startEndpointUrl", "start");
						params.put("joinEndpointUrl", "join");
						params.put("doctype-public", Config.getInstance().getViewDoctypePublic());
						params.put("doctype-system", Config.getInstance().getViewDoctypeSystem());
						params.put("jsapiUrl", "/_ah/channel/jsapi");
						//params.put("userToken")
						
						Watcher w = Watcher.findWatcherByGameAndGameUser(g, gu);
						if(w != null && w.getChannelkey() != null && !w.getChannelkey().equals("")) {
							ChannelService channelService = ChannelServiceFactory.getChannelService();
							String token = channelService.createChannel(w.getChannelkey());
							
							params.put("userToken", token);
						}
						
						Config.getInstance().transformFromResource(String.format("/%s/game_view.xslt", g.getGameType().getClientVersion()), new DOMSource(doc1), responseResult, params);						
					}
					else {
						resp.setStatus(405);
					}
				}
				else if(m.group(2).equals("data")) {
					if(req.getMethod().equals("GET")) {
						resp.setContentType("text/xml");
						Config.getInstance().transformDatamodel(gs, responseResult, "");
					}
					else {
						resp.setStatus(405);
					}
				}
				else if(m.group(2).equals("meta")) {
					if(req.getMethod().equals("GET")) {
						resp.setContentType("text/xml");
						XMLOutputFactory factory = XMLOutputFactory.newFactory();
						XMLStreamWriter writer = null;
						try {
							writer = factory.createXMLStreamWriter(responseStream, Config.getInstance().getEncoding());
							writer.setDefaultNamespace(Config.getInstance().getGameEngineNamespace());
							
							
							writer.writeStartDocument();
							
							g.serializeToXml("game", writer);
							writer.writeEndDocument();
							writer.flush();
							
						} catch (XMLStreamException e) {
							resp.setStatus(500);
						}						
					}
					else {
						resp.setStatus(405);
					}
				}
				else if(m.group(2).equals("raw_view")) {
					if(req.getMethod().equals("GET")) {
						resp.setContentType("text/xml");
						Config.getInstance().transformDatamodel(gs, new DOMResult(doc), "");
						Config.getInstance().transformFromResource("/tictactoe_view2.xslt", new DOMSource(doc), responseResult);
					}
					else {
						resp.setStatus(405);
					}
				}
				else if(m.group(2).equals("start")) {
					if(req.getMethod().equals("POST")) {
						boolean success = g.sendStartGameRequest(u); 

						if(!success) {
							resp.setStatus(400);
						}
						else {
							resp.setStatus(200);
						}
					}
					else resp.setStatus(405);
					
				}
				else if(m.group(2).equals("join")) {
					if(req.getMethod().equals("POST")) {
						boolean success = g.sendPlayerJoinRequest(u); 

						if(!success) {
							resp.setStatus(400);
						}
						else {
							resp.setStatus(200);
						}
					}
					else resp.setStatus(405);
				}
				else if(m.group(2).startsWith("event")) {
					if(req.getMethod() == "POST") {
						String eventName = "board." + m.group(4);
						
						Node data = doc.createElementNS(Config.getInstance().getGameEngineNamespace(), "data");
						
						Node player = doc.createElementNS(Config.getInstance().getGameEngineNamespace(), "player");
						player.appendChild(doc.createTextNode(gu.getHashedUserId()));
						
						data.appendChild(player);
						doc.appendChild(data);
						
						DOMResult dr = new DOMResult(data);
										
						if(req.getContentLength() > 0) {
							try {
								Transformer trans = Config.getInstance().newTransformer();
								
								trans.transform(new StreamSource(req.getInputStream()), dr);
							} catch (TransformerException e) {
								// ignore error
							}
						}
						
						boolean ret = g.triggerEvent(eventName, data);
						
						if(!ret) {
							String errorMessage = g.getErrorMessage();
							
							Node error = doc1.createElementNS(Config.getInstance().getGameEngineNamespace(), "error");
							error.appendChild(doc1.createTextNode(errorMessage));
							doc1.appendChild(error);
													
							resp.setContentType("application/xml");
							resp.setStatus(400);
							Transformer trans = Config.getInstance().newTransformer();
							trans.transform(new DOMSource(doc1), responseResult);
						}
						else {
							resp.setStatus(200);
						}
					}
					else {
						resp.setStatus(405);
					}
				}
				else {
					resp.setStatus(404);
					resp.setContentType("text/plain");
					//resp.getWriter().write("unknown url");
				}
				
			} catch (TransformerConfigurationException e) {
				resp.setStatus(500);
				//e.printStackTrace(resp.getWriter());
			} catch (TransformerException e) {
				resp.setStatus(500);
				//e.printStackTrace(resp.getWriter());
			}
		}
		else {
			resp.setStatus(404);
			resp.getWriter().write("game not found.");
		}
	}
}
