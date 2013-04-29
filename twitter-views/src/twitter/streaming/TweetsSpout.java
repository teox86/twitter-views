/*
 * Created by: Matteo Pozza <teox86@virgilio.it>
 */

package twitter.streaming;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import servlet.StartApplication;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

public class TweetsSpout extends BaseRichSpout{
	
	static String AccessToken = StartApplication.OAuthKeys.get("AccessToken1");
	static String AccessSecret = StartApplication.OAuthKeys.get("AccessSecret1");
	static String ConsumerKey = StartApplication.OAuthKeys.get("ConsumerKey1");
	static String ConsumerSecret = StartApplication.OAuthKeys.get("ConsumerSecret1");

	static Logger LOG = Logger.getLogger(TweetsSpout.class);
	static JSONParser jsonParser = new JSONParser();
	static String STREAMING_API_URL = "https://stream.twitter.com/1.1/statuses/sample.json";
	private String track;
	private String user;
	private String password;
	private DefaultHttpClient client;
	private SpoutOutputCollector collector;
	private UsernamePasswordCredentials credentials;
	private BasicCredentialsProvider credentialProvider;

	private int tweetsCount, hundredsTweets = 0;

	// NextTuple(): When this method is called, Storm requests Spout to emit tuples to the output collector.
	@Override
	public void nextTuple() {
		/*
		 * Create the client call
		 */
		System.out.println("Hashtag ConsumerKey: "+ConsumerKey);
		OAuthConsumer consumer = new CommonsHttpOAuthConsumer(ConsumerKey, ConsumerSecret);
		consumer.setTokenWithSecret(AccessToken, AccessSecret);

		HttpGet get = new HttpGet(STREAMING_API_URL);		
		try {
			//Execute
	        consumer.sign(get);
	        HttpClient client = new DefaultHttpClient();
			System.out.println("(hashtag) Execute get ("+get+")");
	        HttpResponse response = client.execute(get);
			StatusLine status = response.getStatusLine();
			System.out.println("(hashtag) Status: "+status.getStatusCode());
			
			HttpEntity entity = response.getEntity();

		      if (entity != null) {
		        InputStream inputStream = entity.getContent();
		        try {
		          BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		          String in;
				while ((in = reader.readLine()) != null) {
		        	
						try{
							//Parse and emit
							Object json = jsonParser.parse(in);
							JSONObject jsontweet = (JSONObject)json;
							if(jsontweet.containsKey("disconnect")){
								System.out.println("(message) "+jsontweet);
								return;
							}
							collector.emit(new Values(track,json));
						}catch (ParseException e) {
							LOG.error("(hashtag) Error parsing message from twitter",e);
						}

		          }
		        } catch (IOException ioException) {
		          // In case of an IOException the connection will be released
		          // back to the connection manager automatically
		          ioException.printStackTrace();
		        } catch (RuntimeException runtimeException) {
		          // In case of an unexpected exception you may want to abort
		          // the HTTP request in order to shut down the underlying
		          // connection immediately.
		        	get.abort();
		          runtimeException.printStackTrace();
		        } finally {
		          // Closing the input stream will trigger connection release
		          try {
		            inputStream.close();
		          } catch (Exception ignore) {
		          }
		        }
		      }
		} catch (IOException e) {
			LOG.error("(hashtag) Error in communication with twitter api ["+get.getURI().toString()+"]");
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

	// Open(): Called when a task for this component is initialized within a worker on the cluster.
	// It provides the spout with the environment in which the spout executes.
	@Override
	public void open(Map conf, TopologyContext context,	SpoutOutputCollector collector) {
		int spoutsSize = context.getComponentTasks(context.getThisComponentId()).size();
		int myIdx = context.getThisTaskIndex();
		this.collector = collector;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("criteria","tweet"));
	}
}
