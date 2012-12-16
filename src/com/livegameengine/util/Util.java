package com.livegameengine.util;

import java.io.ByteArrayOutputStream;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.livegameengine.config.Config;


public class Util {
	public static void writeNode(Node n, XMLStreamWriter writer) throws DOMException, XMLStreamException {
		writeNode(n, writer, false, 0);
	}
	
	public static void writeNode(Node n, XMLStreamWriter writer, boolean includeDocument, int prefixCounter) throws DOMException, XMLStreamException {
		String prefix = null;
		
		switch(n.getNodeType()) {
		case Node.ATTRIBUTE_NODE:
			if(n.getLocalName() == "xmlns") break;
			
			prefix = writer.getPrefix(n.getNamespaceURI());
			
			if(prefix == null && n.getNamespaceURI() != null) {
				prefix = "ns" + (prefixCounter++);
				writer.setPrefix(prefix, n.getNamespaceURI());
			}
			if(n.getNamespaceURI() != null)
				writer.writeAttribute(prefix, n.getNamespaceURI(), n.getLocalName(), n.getNodeValue());
			else 
				writer.writeAttribute(n.getLocalName(), n.getNodeValue());
			break;
		case Node.CDATA_SECTION_NODE:
			writer.writeCData(n.getNodeValue());
			break;
		case Node.COMMENT_NODE:
			writer.writeComment(n.getNodeValue());
			break;
		case Node.DOCUMENT_NODE:
			if(includeDocument) {
				writer.writeStartDocument();
			}
			for(int i = 0; i < n.getChildNodes().getLength(); i++) {
				Node m = n.getChildNodes().item(i);
				writeNode(m, writer, false, prefixCounter);
			}
			if(includeDocument) {
				writer.writeEndDocument();
			}
			break;
		case Node.DOCUMENT_TYPE_NODE:
			writer.writeDTD(n.getNodeValue());
			break;
		case Node.ELEMENT_NODE:
			prefix = writer.getPrefix(n.getNamespaceURI());
			
			if(prefix == null) {
				prefix = "ns" + (prefixCounter++);
				writer.setPrefix(prefix, n.getNamespaceURI());
			}
			for(int i = 0; i < n.getAttributes().getLength(); i++) {
				Node m = n.getAttributes().item(i);
				writeNode(m, writer, false, prefixCounter);
			}
			for(int i = 0; i < n.getChildNodes().getLength(); i++) {
				Node m = n.getChildNodes().item(i);
				writeNode(m, writer, false, prefixCounter);
			}
			break;
		case Node.ENTITY_REFERENCE_NODE:
			writer.writeEntityRef(n.getNodeName());
			break;
		case Node.TEXT_NODE:
			writer.writeCharacters(n.getNodeValue());
			break;
		}
	}

	public static String escapeJS(String in) {
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < in.length(); i++) {
			switch(in.charAt(i)) {
			case '\'':
			case '\"':
			case '\\':
				sb.append('\\').append(in.charAt(i));
				break;
			default:
				sb.append(in.charAt(i));
				break;
			}
		}
		
		return sb.toString();
	}
	
	public static String serializeXml(NodeList list) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		StreamResult sr = new StreamResult(bos);
		
		Transformer trans = null;
		try {
			trans = Config.getInstance().newTransformer();
		} catch (TransformerConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return "";
		}
		
		trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		
		for(int i = 0; i < list.getLength(); i++) {
			try {
				trans.transform(new DOMSource(list.item(i)), sr);
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return bos.toString();
	}
}
