package com.livegameengine.util;

import java.io.ByteArrayOutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.NodeList;

import com.livegameengine.config.Config;


public class Util {

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
