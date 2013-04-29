package servlet;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.servlet.AsyncContext;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import twitter.streaming.HashtagResult;
import twitter.streaming.SignalClient;
import twitter.streaming.FilteredTweetsSpout;
import twitter.streaming.TweetsCount;
import twitter.streaming.UserSocialGraph;

public class AjaxServlet extends HttpServlet implements Servlet {
  
	private final static Queue<AsyncContext> asyncContexts = new ConcurrentLinkedQueue<AsyncContext>();
	private final static Queue<AsyncContext> asyncContexts2 = new ConcurrentLinkedQueue<AsyncContext>();
	
   public AjaxServlet() {
       super();
   }

   private String handleRequest(String param){

       return null;
   }

   protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
	   String service = req.getParameter("service");
	   if(service.equalsIgnoreCase("hashtagCount")) {
		   String hashtagCount = HashtagResult.getJsonHashtagCount();
		   	/*
		   	 *  Output:
		   	 *  {"children":[{"name":"happybirthdaymaura","count":2},
		   	 *  				{"name":"слоу","count":1},
		   	 *  				{"name":"winwithcc","count":1},
		   	 *  				{"name":"علينا","count":1},
		   	 *  				{"name":"ночнойпиар","count":1},
		   	 *  				{"name":"yeeyeeyee","count":1},
		   	 *  				{"name":"premierjourdelannéeet","count":1},
		   	 *  				{"name":"newyearsresolution","count":1},
		   	 *  				{"name":"mu","count":1},
		   	 *  				{"name":"education","count":1}
		   	 *  			]
		   	 *  }
		   	 */
		   res.setContentType("application/json; charset=UTF-8");
		   
		   PrintWriter out = new PrintWriter(
				   new OutputStreamWriter(res.getOutputStream(), "UTF8"), true);
		   out.println(hashtagCount);
		   out.flush();
		   out.close();
	   }
	   else if(service.equalsIgnoreCase("tweetsCount")) {
		   int hashtagCount = TweetsCount.getTweeetsCount();
		   res.setContentType("text/plain");
		   PrintWriter out = res.getWriter();
		   out.println(hashtagCount);
		   out.flush();
		   out.close();
	   }
	   // Create the social graph
	   else if(service.equalsIgnoreCase("socialGraph")) {
		   String username = req.getParameter("searchUser");
		   //System.out.println(username);
		   String userSocialGraph = null;
		   if (username==null) {	// construct authenticated user social graph
			   userSocialGraph = UserSocialGraph.requestMyUserSocialRelations();
		   }
		   else {	// construct user's social graph
			   userSocialGraph = UserSocialGraph.requestUserSocialRelationsJSON(username);
		   }
		   //System.out.println(userSocialGraph);
		   res.setContentType("application/json");
		   PrintWriter out = res.getWriter();
		   out.println(userSocialGraph);
		   out.flush();
		   out.close();
	   }
	   // Submit new filter word
	   else if(service.equalsIgnoreCase("ontology")) {
		   String filterWord = req.getParameter("filterWord");
		   System.out.println("Servlet: filterWord->"+filterWord);
		   String address = FilteredTweetsSpout.getZooAddress();
		   SignalClient sc = new SignalClient(address, "test-signal-spout");
		   sc.start();
		    try {
		        try {
					sc.send(filterWord.getBytes());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    } finally {
		        sc.close();
		    }
	   }
	   // Reverse AJAX to return the filtered tweets
	   else if(service.equalsIgnoreCase("ontologyResult")) {
		   if (asyncContexts!=null) {
		    AsyncContext asyncContext = req.startAsync(req,res); 
		    asyncContext.setTimeout(0); 
		    asyncContexts.offer(asyncContext);
		   }
	   }
	   // Reverse AJAX to update tweets and filters count
	   else if(service.equalsIgnoreCase("ontologyResultCounter")) {
		   if (asyncContexts2!=null) {
		    AsyncContext asyncContext2 = req.startAsync(req,res); 
		    asyncContext2.setTimeout(0); 
		    asyncContexts2.offer(asyncContext2);
		   }
	   }
   }

   protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
	   // Start the topology
	   String service = req.getParameter("service");
	   if(service.equalsIgnoreCase("startTopology")) {
		   System.out.println("Start Topology...");
		   ServletContext context = getServletContext();
		   String servletContextPath = context.getRealPath("/");
		   String username = req.getParameter("username");
		   String password = req.getParameter("password");
		   twitter.streaming.Topology.execute(username, password);
		   res.setContentType("text/plain");
		   PrintWriter out = res.getWriter();
		   out.println(1);
	   }

   }

   // Update ontology counters through reverse AJAX
   public static void updateOntologyCounter(JSONArray outputJson) {
	   while (!asyncContexts2.isEmpty()) { 
		    AsyncContext asyncContext = asyncContexts2.poll();
		    if (asyncContext != null) {
			    ServletResponse sr = asyncContext.getResponse();
			    if (sr!=null) {
				    HttpServletResponse peer = (HttpServletResponse) sr; 
				    try {
				    	peer.getWriter().write(outputJson.toString());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
				    peer.setStatus(HttpServletResponse.SC_OK); 
				    peer.setContentType("application/json"); 
				    asyncContext.complete(); 
			    }
			    else {
			    	System.out.println("ServletResponse = null");
			    }
		    }
		    else {
		    	System.out.println("asyncContext = null");
		    }
		}	
}
   
    // Return ontology tweets through reverse AJAX
	public static void updateOntology(JSONObject outputJson) {
		   while (!asyncContexts.isEmpty()) { 
			    AsyncContext asyncContext = asyncContexts.poll();
			    if (asyncContext != null) {
				    ServletResponse sr = asyncContext.getResponse();
				    if (sr!=null) {
					    HttpServletResponse peer = (HttpServletResponse) sr; 
					    try {
					    	peer.getWriter().write(outputJson.toString());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} 
					    peer.setStatus(HttpServletResponse.SC_OK); 
					    peer.setContentType("application/json"); 
					    asyncContext.complete(); 
				    }
				    else {
				    	System.out.println("ServletResponse = null");
				    }
			    }
			    else {
			    	System.out.println("asyncContext = null");
			    }
			}	
	}

} 