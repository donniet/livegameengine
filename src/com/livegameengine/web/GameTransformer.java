package com.livegameengine.web;

import java.io.ByteArrayInputStream;
import java.security.InvalidParameterException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

import com.livegameengine.config.Config;
import com.livegameengine.model.Game;
import com.livegameengine.model.GameState;
import com.livegameengine.model.GameType;
import com.livegameengine.model.GameURIResolver;
import com.livegameengine.model.GameUser;

public class GameTransformer extends Transformer {
	public static String PROPERTY_RAW_VIEW = "game.raw_view_property";
	
	private static Config config = Config.getInstance();
	
	GameUser gameUser_ = null;
	boolean raw_ = false;
	
	Map<String,Object> params_ = new HashMap<String,Object>();
	Properties props_ = new Properties();
	ErrorListener listener_ = null;
	
	public GameTransformer(GameUser player) {
		gameUser_ = player;
	}
	
	@Override
	public void transform(Source xmlSource, Result outputTarget)
			throws TransformerException {
		
		if(!GameSource.class.isAssignableFrom(xmlSource.getClass())) {
			throw new TransformerException("Source not of type 'GameSource'");
		}
		
		//TODO: add clientmessage handler here as well.
		
		GameSource s = (GameSource)xmlSource;
		
		Document doc = config.newXmlDocument();
		Document doc1 = config.newXmlDocument();
		
		Map<String,Object> params = new HashMap<String,Object>();
		
		GameType gt = s.getGame().getGameType();
		
		params.put("eventEndpointUrl", "event/{event}");
		params.put("gameEventEndpointUrl", "{gameEvent}");
		params.put("doctype-public", config.getViewDoctypePublic());
		params.put("doctype-system", config.getViewDoctypeSystem());
		params.put("jsapiUrl", "/_ah/channel/jsapi");
		params.put("version", gt.getClientVersion());
		params.put("serverTime", config.getDateFormat().format(new Date()));
		params.put("clientMessageUrl", "message");
		
		config.transformDatamodel(s.getGameState(), new DOMResult(doc), gameUser_.getHashedUserId());
		//TODO: replace with pulling the game view from the gametype
		Source frontEndSource = new StreamSource(GameTransformer.class.getResourceAsStream("/tictactoe_view5.xslt"));
		/*
		ByteArrayInputStream bis = new ByteArrayInputStream(gt.getFrontEnd());
		Source frontEndSource = new StreamSource(bis);
		*/
						
		Transformer t = config.newTransformer(frontEndSource);
		t.setURIResolver(new GameURIResolver(s.getGame()));
		
		if(raw_) {
			t.transform(new DOMSource(doc), outputTarget);
		}
		else {
			t.transform(new DOMSource(doc), new DOMResult(doc1));
			config.transformFromResource(String.format("/%s/game_view.xslt", gt.getClientVersion()), new DOMSource(doc1), outputTarget, params);
		}
	}

	@Override
	public void setParameter(String name, Object value) {
		params_.put(name, value);
	}

	@Override
	public Object getParameter(String name) {
		return params_.get(name);
	}

	@Override
	public void clearParameters() {
		params_.clear();
	}

	@Override
	public void setURIResolver(URIResolver resolver) {}

	@Override
	public URIResolver getURIResolver() {
		return null;
	}

	@Override
	public void setOutputProperties(Properties oformat) {
		props_.clear();
		this.raw_ = false;
		
		if(oformat != null) {
			for(Iterator<Object> i = oformat.keySet().iterator(); i.hasNext();) {
				String key = (String)i.next();
				
				setOutputProperty(key, oformat.getProperty(key));
			}
		}
	}

	@Override
	public Properties getOutputProperties() {
		return props_;
	}

	@Override
	public void setOutputProperty(String key, String value)
			throws IllegalArgumentException {

		if(key.equals(PROPERTY_RAW_VIEW)) {
			this.raw_ = Boolean.parseBoolean(value);
		}
		else {
			props_.put(key, value);
		}
	}

	@Override
	public String getOutputProperty(String name)
			throws IllegalArgumentException {
		return props_.getProperty(name);
	}

	@Override
	public void setErrorListener(ErrorListener listener)
			throws IllegalArgumentException {
		listener_ = listener;
	}

	@Override
	public ErrorListener getErrorListener() {
		return listener_;
	}

}
