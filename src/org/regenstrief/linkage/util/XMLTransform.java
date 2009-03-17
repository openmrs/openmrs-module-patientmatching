package org.regenstrief.linkage.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

/**
 * Class takes an XML DOM, XSLT file name, and output file name and transforms the
 * given XML document using the XSLT document and writes the results to the given
 * output file
 * 
 * @author jegg
 *
 */

public class XMLTransform {
	
	public static boolean writeTransformedXML(Document source, File xslt_transform, File output){
		try{
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer(new StreamSource(xslt_transform));
			transformer.transform(new DOMSource(source), new StreamResult(new FileOutputStream(output)));
		}
		catch(IOException ioe){
			return false;
		}
		catch(TransformerConfigurationException tce){
			return false;
		}
		catch(TransformerException te){
			return false;
		}
		
		return true;
	}
	
	public static void main(String[] args){
		File input = new File("test out2.xml");
		File xslt = new File("test_xslt.xsl");
		File out = new File("test_output.html");
		
		Document input_dom = XMLTranslator.getXMLDocFromFile(input);
		
		if(writeTransformedXML(input_dom, xslt, out)){
			System.out.println("success");
		} else {
			System.out.println("error creating " + out);
		}
	}
}
