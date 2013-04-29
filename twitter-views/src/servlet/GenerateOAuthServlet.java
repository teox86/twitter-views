/*
 * Created by: Matteo Pozza <teox86@virgilio.it>
 */

package servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import utils.generateXmlFile;

public class GenerateOAuthServlet extends HttpServlet implements Servlet {
	
   public GenerateOAuthServlet() {
       super();
   }

   protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
	   doPost(req, res);
   }

   protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
	   int nparam = 3;
	   ByteArrayOutputStream byteArray = generateXmlFile.generateFile(req, nparam);
	   res.setHeader("Content-Disposition","attachment; filename=OAuthCredentials.xml");
	   res.setContentType("text/xml");
	   ServletOutputStream sos = res.getOutputStream();
	   sos.print(byteArray.toString());
	   sos.flush();
	   sos.close();
   }
}