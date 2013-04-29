/*
 * Created by: Matteo Pozza <teox86@virgilio.it>
 */

package twitter.streaming;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import servlet.AjaxServlet;
import servlet.StartApplication;

import backtype.storm.contrib.signals.spout.BaseSignalSpout;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;

@SuppressWarnings("serial")
public class FilteredTweetsSpout extends BaseSignalSpout {
	
	static String AccessToken = StartApplication.OAuthKeys.get("AccessToken0");
	static String AccessSecret = StartApplication.OAuthKeys.get("AccessSecret0");
	static String ConsumerKey = StartApplication.OAuthKeys.get("ConsumerKey0");
	static String ConsumerSecret = StartApplication.OAuthKeys.get("ConsumerSecret0");
		
	static JSONParser jsonParser = new JSONParser();
	static String STREAMING_API_URL = "https://stream.twitter.com/1.1/statuses/filter.json";
	
	private SpoutOutputCollector collector;
	private static final Logger LOG = LoggerFactory.getLogger(FilteredTweetsSpout.class);
	private static String zookeeperAddress;
	
	private static boolean filterChanged = false;
	private static int numberOfFilters = 0;
	private static ArrayList<String> filterArray = null;

 public FilteredTweetsSpout(String name) {
     super(name);
     filterArray = new ArrayList<String>();
 }

 @SuppressWarnings("rawtypes")
 @Override
 public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {

	 this.collector = collector;
	 super.open(conf, context, collector);
     LOG.info("Collector class: " + collector.getClass().getName());
     int zkPort = Utils.getInt(conf.get("storm.zookeeper.port"));
     List<String> zkServers = (List<String>) conf.get("storm.zookeeper.servers");

     Iterator<String> it = zkServers.iterator();
     StringBuffer sb = new StringBuffer();
     while (it.hasNext()) {
         sb.append(it.next());
         sb.append(":");
         sb.append(zkPort);
         if (it.hasNext()) {
             sb.append(",");
         }
     }
     zookeeperAddress = sb.toString();
     System.out.println("zookeeper: "+sb.toString());
 }

 // Update filter array after receiving a word through signal
 @Override
 public void onSignal(byte[] data) {
	 String filterWord = new String(data);
	 if (filterWord.equals("resetOntology")) {
		 filterArray.clear();
		 numberOfFilters = 0;
		 System.out.println("(ontology) Reset ontology. filterArray:"+filterArray.size());
		 return;
	 }
	 if (filterArray.contains(filterWord)) {
		 System.out.println("(ontology) Remove Filter: "+filterWord);
		 filterArray.remove(filterWord);
		 numberOfFilters--;
	 }
	 else {
		 System.out.println("(ontology) Add Filter: "+filterWord);
		 filterArray.add(filterWord);
		 numberOfFilters++;
	 }
	 filterChanged = true;
 }
 
 // Send a new POST request when the numberOfFilters>0 and the filter array is changed.
 @Override
 public void nextTuple() {
	 numberOfFilters = filterArray.size();
	 if (numberOfFilters > 0) {
		/*
		 * Create the client call
		 */
		filterChanged = false;
		System.out.println("Ontology ConsumerKey: "+ConsumerKey);
		OAuthConsumer consumer = new CommonsHttpOAuthConsumer(ConsumerKey, ConsumerSecret);
		consumer.setTokenWithSecret(AccessToken, AccessSecret);
		
		// Create post request
		HttpPost post = new HttpPost(STREAMING_API_URL);
		List<NameValuePair> parameters = new ArrayList<NameValuePair>(1);
		String trackParameter = new String();
		int filterSize = filterArray.size();
		for(int i=0; i<filterSize; i++) {
			trackParameter += filterArray.get(i);
			if((i+1)<filterSize) {
				// If the parameter is not the last one, add a comma
				trackParameter += ",";
			}
		}
		System.out.println("(ontology) "+trackParameter);
		parameters.add(new BasicNameValuePair("track", trackParameter));
		try {
			post.setEntity(new UrlEncodedFormEntity(parameters));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (filterChanged==false) {
			//Execute request
			try {
		        consumer.sign(post);
		        HttpClient client = new DefaultHttpClient();
			    HttpResponse response = client.execute(post);
				StatusLine status = response.getStatusLine();
				System.out.println("(ontology) Status: "+status.getStatusCode());
				if(status.getStatusCode() == 200){
					InputStream inputStream = response.getEntity().getContent();
					BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
					String in;
					//Read line by line
					while(((in = reader.readLine())!=null)&&filterChanged==false&&numberOfFilters>0){
						try{
							//Parse and emit
							if (in.length()==0) {
								continue;
							}
							Object json = jsonParser.parse(in);
							collector.emit(new Values(json,filterArray));
						}catch (ParseException e) {
							LOG.error("(ontology) Error parsing message from twitter",e);
						}
					}
				}
			} catch (IOException e) {
				LOG.error("(ontology) Error in communication with twitter api ["+post.getURI().toString()+"]");
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e1) {
				}
			} catch (OAuthMessageSignerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (OAuthExpectationFailedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (OAuthCommunicationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
		}
	 }
	 else {
		 try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 }
 }

 @Override
 public void declareOutputFields(OutputFieldsDeclarer declarer) {
	 declarer.declare(new Fields("tweet","filterWords"));
 }
 
public static String getZooAddress() {
	return zookeeperAddress;
}

}
