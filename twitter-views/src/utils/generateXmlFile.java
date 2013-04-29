package utils;

import java.io.ByteArrayOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class generateXmlFile {
	
	public static ByteArrayOutputStream generateFile (HttpServletRequest req, int nparam) {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		
			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("OAuthSettings");
			doc.appendChild(rootElement);
		
			// applications element
			for (int i=1; i<=nparam; i++) {
				Element application = doc.createElement("TwitterApplication");
				rootElement.appendChild(application);
				// application counter
				application.setAttribute("id", Integer.toString(i));
				// ConsumerKey
				Element el = doc.createElement("ConsumerKey");
				el.appendChild(doc.createTextNode(req.getParameter("ConsumerKey"+i)));
				application.appendChild(el);
				// ConsumerSecret
				el = doc.createElement("ConsumerSecret");
				el.appendChild(doc.createTextNode(req.getParameter("ConsumerSecret"+i)));
				application.appendChild(el);
				// AccessToken
				el = doc.createElement("AccessToken");
				System.out.println(req.getParameter("AccessToken"+i));
				el.appendChild(doc.createTextNode(req.getParameter("AccessToken"+i)));
				application.appendChild(el);
				// AccessSecret
				el = doc.createElement("AccessSecret");
				el.appendChild(doc.createTextNode(req.getParameter("AccessSecret"+i)));
				application.appendChild(el);
			}
			// transform document into byte array and return it
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(byteArray);
			transformer.transform(source, result);
			return byteArray;
		}
		catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
		return null;
	}

}
