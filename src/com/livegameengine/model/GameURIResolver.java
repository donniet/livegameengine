package com.livegameengine.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import com.livegameengine.config.Config;

public class GameURIResolver implements URIResolver {
	private Game game_;
	private XMLOutputFactory factory_ = null;
	
	private static String CURRENT_GAME_URI = "game://current/meta";
	
	public GameURIResolver(Game game) {
		game_ = game;
		factory_ = XMLOutputFactory.newFactory();
	}
	
	@Override
	public Source resolve(String href, String base) throws TransformerException {
		
		Source ret = null;
		
		if(href.equals(CURRENT_GAME_URI)) {
			try {
				ret = createGameSourceXml(game_);
			} catch (XMLStreamException e) {
				throw new TransformerException("could not construct stream from current game", e);
			}
				
		}
		
		
		return ret;
	}
	
	public Source createGameSourceXml(Game game) throws XMLStreamException {
		//TODO: this is inefficient.  All the model classes should implement org.w3c.dom.Node, then we could just use a DOMSource here.
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		XMLStreamWriter writer = factory_.createXMLStreamWriter(baos);
		writer.setDefaultNamespace(Config.getInstance().getGameEngineNamespace());
			
		writer.writeStartDocument();
		
		game.serializeToXml("game", writer);
		
		writer.writeEndDocument();
		writer.flush();
		
		return new StreamSource(new ByteArrayInputStream(baos.toByteArray()));
	}

}
