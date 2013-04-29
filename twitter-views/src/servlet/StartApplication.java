package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class StartApplication extends HttpServlet implements Servlet {
	
	public static Map<String, String> OAuthKeys = new HashMap<String, String>();
	
   public StartApplication() {
       super();
   }

   protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

   }

   protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
	   String xmldata = req.getParameter("xmlData");
	   
	   DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
	   DocumentBuilder builder;
	   Document document = null;
	   try  
	   {  
	       builder = factory.newDocumentBuilder();  
	       document = builder.parse(new InputSource(new StringReader(xmldata)));  
	   } catch (Exception e) {  
	       e.printStackTrace();  
	   }
	   boolean result = getKeys(document);
	   res.setContentType("text/plain");
	   PrintWriter out = res.getWriter();
	   if (result == true) {
		   out.println(1);
	   }
	   else {
		   out.println(0);
	   }
   }
   
   private static boolean getKeys (Document document) {
	   OAuthKeys.clear();
	   NodeList nList = document.getElementsByTagName("TwitterApplication");
	   try {
		   for (int i=0; i<nList.getLength(); i++) {
			   Node nNode = nList.item(i);
			   if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				   
					Element eElement = (Element) nNode;
					OAuthKeys.put("ConsumerKey"+Integer.toString(i), eElement.getElementsByTagName("ConsumerKey").item(0).getTextContent());
					OAuthKeys.put("ConsumerSecret"+Integer.toString(i), eElement.getElementsByTagName("ConsumerSecret").item(0).getTextContent());
					OAuthKeys.put("AccessToken"+Integer.toString(i), eElement.getElementsByTagName("AccessToken").item(0).getTextContent());
					OAuthKeys.put("AccessSecret"+Integer.toString(i), eElement.getElementsByTagName("AccessSecret").item(0).getTextContent());
					/*
					System.out.println("ConsumerKey : " + eElement.getElementsByTagName("ConsumerKey").item(0).getTextContent());
					System.out.println("ConsumerSecret : " + eElement.getElementsByTagName("ConsumerSecret").item(0).getTextContent());
					System.out.println("AccessToken : " + eElement.getElementsByTagName("AccessToken").item(0).getTextContent());
					System.out.println("AccessSecret : " + eElement.getElementsByTagName("AccessSecret").item(0).getTextContent());
					*/
				}
		   }
	   } catch (Exception e) {
		   return false;
	   }
	   return true;
   }
   
}