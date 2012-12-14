package com.livegameengine.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.mozilla.javascript.Scriptable;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.livegameengine.config.Config;


@PersistenceCapable
public class ClientMessage implements Scriptable, XmlSerializable {
	private static final long serialVersionUID = 4649802893267737142L;
	
	private static final String LOCAL_NAME = "clientMessage";
	private static final String NAMESPACE_PREFIX = "game";
	
	@NotPersistent
	private Config config_ = Config.getInstance();
	
	@Persistent
	private Key key;
	
	@Persistent
	private List<String> parameterNames;
	
	@Persistent
	private List<String> parameterValues;
	
	@Persistent
	private Blob content;
	
	@Persistent
	private Date messageDate;
	
	
	public NameValuePair<String> getParameter(int i) {
		if(i >= 0 && i < parameterNames.size()) {
			return new NameValuePair<String>(parameterNames.get(i), parameterValues.get(i));
		}
		
		return null;
	}
	public NameValuePair<String> getParameter(String s) {
		//TODO: make this more efficient (if I ever use it...)
		return getParameter(parameterNames.indexOf(s));
	}
	
	public Node getContent() {
		try {
			Document doc = config_.newXmlDocument();
			Transformer trans = config_.newTransformer();
			
			trans.transform(new StreamSource(new ByteArrayInputStream(content.getBytes())), new DOMResult(doc));
			
			for(int i = 0; i < doc.getChildNodes().getLength(); i++) {
				Node n = doc.getChildNodes().item(i);
				
				if(n.getNodeType() == Node.ELEMENT_NODE) return n;
			}
			
			return null;
		} catch (TransformerConfigurationException e) {
			return null;
		} catch (TransformerException e) {
			//TODO: add error handling here
			return null;
		}
	}
	public void setContent(Node content) {
		if(content == null) {
			this.content = null;
			return;
		}
		
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			Transformer trans = config_.newTransformer();
			
			trans.transform(new DOMSource(content), new StreamResult(bos));
			
			this.content = new Blob(bos.toByteArray());
		} catch (TransformerConfigurationException e) {
			return;
		} catch (TransformerException e) {
			//TODO: add error handling here
			return;
		}
	}
		
	@Override
	public void serializeToXml(String elementName, ContentHandler writer)
			throws XMLStreamException {
		String ns = config_.getGameEngineNamespace();
		
		writer.writeStartElement(ns, elementName);
		writer.writeAttribute("key", KeyFactory.keyToString(key));
		
		int min = Math.min(parameterNames.size(), parameterValues.size());
		for(int i = 0; i < min; i++) {
			writer.writeStartElement(ns, "param");
			writer.writeAttribute("name", parameterNames.get(i));
			writer.writeCharacters(parameterValues.get(i));
			writer.writeEndElement();
		}
		
		if(content != null) {
			writer.writeStartElement(ns, "content");
						
			try {
				Transformer trans = config_.newTransformer();
				trans.transform(new StreamSource(new ByteArrayInputStream(content.getBytes())), new SAXResult(writer));
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
						
			writer.writeEndElement();
		}
		
		writer.writeEndElement();
	}

	@Override
	public void serializeToXml(XMLStreamWriter writer)
			throws XMLStreamException {
		serializeToXml(getDefaultLocalName(), writer);		
	}

	@Override
	public String getDefaultLocalName() {
		return LOCAL_NAME;
	}

	@Override
	public String getNamespaceUri() {
		return config_.getGameEngineNamespace();
	}

	@Override
	public void delete(String arg0) {}

	@Override
	public void delete(int arg0) {}

	@Override
	public Object get(String arg0, Scriptable arg1) {
		if(arg0.equals("content"))
			return null;
		else if(arg0.equals("key"))
			return KeyFactory.keyToString(getKey());
		else if(arg0.equals("messageDate"))
			return getMessageDate();
		else if(arg0.equals("getParameter"))
			return null;
	}

	private Date getMessageDate() {
		return messageDate;
	}

	private Key getKey() {
		return key;
	}

	@Override
	public Object get(int arg0, Scriptable arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getClassName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getDefaultValue(Class<?> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] getIds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Scriptable getParentScope() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Scriptable getPrototype() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean has(String arg0, Scriptable arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean has(int arg0, Scriptable arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasInstance(Scriptable arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void put(String arg0, Scriptable arg1, Object arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void put(int arg0, Scriptable arg1, Object arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setParentScope(Scriptable arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPrototype(Scriptable arg0) {
		// TODO Auto-generated method stub
		
	}
	
}
