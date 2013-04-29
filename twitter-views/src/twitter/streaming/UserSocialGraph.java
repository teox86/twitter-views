/*
 * Created by: Matteo Pozza <teox86@virgilio.it>
 */

package twitter.streaming;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import servlet.StartApplication;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

public class UserSocialGraph{

	static String USER_TWEETS = "https://api.twitter.com/1.1/statuses/user_timeline.json";

	static String AccessToken = StartApplication.OAuthKeys.get("AccessToken2");
	static String AccessSecret = StartApplication.OAuthKeys.get("AccessSecret2");
	static String ConsumerKey = StartApplication.OAuthKeys.get("ConsumerKey2");
	static String ConsumerSecret = StartApplication.OAuthKeys.get("ConsumerSecret2");
		
	static String STREAMING_API_URL="https://stream.twitter.com/1/statuses/sample.json";
	private static DefaultHttpClient client;

	private static BasicCredentialsProvider credentialProvider;

	LinkedBlockingQueue<String> tweets = new LinkedBlockingQueue<String>();
	
	public static String requestMyUserSocialRelations() {
		JSONParser jsonParser = new JSONParser();
		/*
		 * Create the client call
		 */
		client = new DefaultHttpClient();
		client.setCredentialsProvider(credentialProvider);
		
		System.out.println("SocialGraph ConsumerKey: "+ConsumerKey);
		OAuthConsumer consumer = new CommonsHttpOAuthConsumer(ConsumerKey, ConsumerSecret);
		consumer.setTokenWithSecret(AccessToken, AccessSecret);
		
		// Getting USER id and name
		String path = "https://api.twitter.com/1.1/account/verify_credentials.json";
		HttpGet request = new HttpGet(path);
	    JSONObject jsonUser = null;
	    try {
	        consumer.sign(request);
	        
	        HttpClient client = new DefaultHttpClient();
	        HttpResponse response = client.execute(request);
	        int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode == 200){
				InputStream inputStream = response.getEntity().getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
				jsonUser = (JSONObject)jsonParser.parse(reader.readLine());
				String screen_name = jsonUser.get("screen_name").toString();
				String userSocialGraph = requestUserSocialRelationsJSON(screen_name);
				return userSocialGraph;
			}
			else {
				return null;
			}
	    } catch (Exception e) {
			e.printStackTrace();
			return null;
        }
	}
	
	public static String requestUserSocialRelationsJSON(String username) {
	
		Map<String, Map> usersMapList = new ConcurrentHashMap<String, Map>();
		JSONParser jsonParser = new JSONParser();
		/*
		 * Create the client call
		 */
		client = new DefaultHttpClient();
		client.setCredentialsProvider(credentialProvider);
		
		OAuthConsumer consumer = new CommonsHttpOAuthConsumer(ConsumerKey, ConsumerSecret);
		consumer.setTokenWithSecret(AccessToken, AccessSecret);
		
		Map mapResults = new LinkedHashMap();
		List nodes = new LinkedList();
		List links = new LinkedList();
		
		String user_name = null;
		String user_id = null;
		String user_screen_name = username;
		String user_profile_picture = null;
		
		// Getting USER id and name
		String path = "https://api.twitter.com/1.1/users/show.json?screen_name="+user_screen_name;
		HttpGet request = new HttpGet(path);
	    JSONObject jsonUser = null;
	    try {
	        consumer.sign(request);
	        
	        HttpClient client = new DefaultHttpClient();
	        HttpResponse response = client.execute(request);
	        int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode == 200){
				InputStream inputStream = response.getEntity().getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
				jsonUser = (JSONObject)jsonParser.parse(reader.readLine());
				user_id = jsonUser.get("id").toString();
				user_name = jsonUser.get("name").toString();
				user_profile_picture = jsonUser.get("profile_image_url").toString();
			    Map mapUser = new LinkedHashMap();
			    mapUser.put("id", user_id);
			    mapUser.put("screen_name", user_screen_name);
			    mapUser.put("name", user_name);
			    mapUser.put("profile_picture", user_profile_picture);
			    mapUser.put("follower", (int)1);
			    mapUser.put("friend", (int)1);
			    mapUser.put("mentioned", (int)1);
	    		mapUser.put("text", "-");
	    		mapUser.put("counter", (int)1);
	    		mapUser.put("main", (int)1);
			    usersMapList.put(user_id, mapUser);
			}
        } catch (Exception e) {
			e.printStackTrace();
        }	 
			
	    //The Twitter REST API utilizes a technique called ‘cursoring’ to paginate large result sets.
	    //Use cursors to navigate collections.
		long cursor = -1;
		
		// FOLLOWERS
	    path = "http://api.twitter.com/1.1/followers/list.json?screen_name="+user_screen_name+"&skip_status=false&include_user_entities=true";
	    do {
	    	String url_cursor = path+"&cursor="+Long.toString(cursor);
		    request = new HttpGet(url_cursor);
		    JSONObject jsonFollowers = null;
		    try {
		        consumer.sign(request);
		        HttpClient client = new DefaultHttpClient();
		        HttpResponse response = client.execute(request);
		        int statusCode = response.getStatusLine().getStatusCode();
		        
				if(statusCode == 200){
					InputStream inputStream = response.getEntity().getContent();
					BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
					jsonFollowers = (JSONObject)jsonParser.parse(reader.readLine());
					//Update cursor
					cursor=(Long)jsonFollowers.get("next_cursor");
					if(jsonFollowers.containsKey("users")){
							for(Object jsonObj : (JSONArray)jsonFollowers.get("users")){
								JSONObject followerUser = (JSONObject)jsonObj;
								String id = followerUser.get("id").toString();
								String screen_name = followerUser.get("screen_name").toString();
								String name = followerUser.get("name").toString();
								String profile_picture = followerUser.get("profile_image_url").toString();
							    Map mapUser = new LinkedHashMap();
							    mapUser.put("id", id);
							    mapUser.put("screen_name", screen_name);
							    mapUser.put("name", name);
							    mapUser.put("profile_picture", profile_picture);
							    mapUser.put("follower", (int)1);
							    mapUser.put("friend", (int)0);
							    mapUser.put("mentioned", (int)0);
							    mapUser.put("counter", (int)0);
							    Map linkUser = new LinkedHashMap();
							    linkUser.put("source", user_id);
							    linkUser.put("target", id);
							    links.add(linkUser);
							    usersMapList.put(id, mapUser);
							}
					}
				} else {
					System.out.println("Followers list: reached request rate limit per window!");
					cursor=0;
				}
	        } catch (Exception e) {
				e.printStackTrace();
	        }
	    } while (cursor!=0);
      
	    cursor=-1;
		// FRIENDS
	    path = "https://api.twitter.com/1.1/friends/list.json?screen_name="+user_screen_name+"&skip_status=false&include_user_entities=true";
	    do {
	    	String url_cursor = path+"&cursor="+Long.toString(cursor);
		    request = new HttpGet(url_cursor);	    	
	        JSONObject jsonFriends = null;
	        try {
		        consumer.sign(request);
		        
		        HttpClient client = new DefaultHttpClient();
		        HttpResponse response = client.execute(request);
		        int statusCode = response.getStatusLine().getStatusCode();
		        
				if(statusCode == 200){
					InputStream inputStream = response.getEntity().getContent();
					BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
					jsonFriends = (JSONObject)jsonParser.parse(reader.readLine());
					//Update cursor
					cursor=(Long)jsonFriends.get("next_cursor");
					//
					if(jsonFriends.containsKey("users")){
						for(Object jsonObj : (JSONArray)jsonFriends.get("users")){
							JSONObject friendUser = (JSONObject)jsonObj;
							String id = friendUser.get("id").toString();
							String screen_name = friendUser.get("screen_name").toString();
							String name = friendUser.get("name").toString();
							String profile_picture = friendUser.get("profile_image_url").toString();
						    if(usersMapList.containsKey(id)) {
						    	Map mapUser = usersMapList.get(id);
						    	mapUser.put("friend", (int)1);
						    	usersMapList.remove(id);
						    	usersMapList.put(id, mapUser);
						    } else {
							    Map mapUser = new LinkedHashMap();
							    mapUser.put("id", id);
							    mapUser.put("screen_name", screen_name);
							    mapUser.put("name", name);
							    mapUser.put("profile_picture", profile_picture);
							    mapUser.put("follower", (int)0);
							    mapUser.put("friend", (int)1);
							    mapUser.put("mentioned", (int)0);
							    mapUser.put("counter", (int)0);
							    Map linkUser = new LinkedHashMap();
							    linkUser.put("source", user_id);
							    linkUser.put("target", id);
							    links.add(linkUser);
							    usersMapList.put(id, mapUser);
						    }
						}
					}
				} else {
					System.out.println("Friends list: reached request rate limit per window!");
					cursor=0;
				}
	        } catch (Exception e) {
				e.printStackTrace();
	        }
	    } while (cursor!=0);
	        
		// USER TIMELINE
        request = new HttpGet("https://api.twitter.com/1.1/statuses/user_timeline.json?screen_name="+user_screen_name+"&count=200");
        JSONArray jsonTimeline = null;
        try {
	        consumer.sign(request);
	        HttpClient client = new DefaultHttpClient();
	        HttpResponse response = client.execute(request);
	        int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode == 200){
				InputStream inputStream = response.getEntity().getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
				jsonTimeline = (JSONArray)jsonParser.parse(reader.readLine());
				for(Object tweetObj : jsonTimeline) {
					JSONObject tweet = (JSONObject)tweetObj;
					if(tweet.containsKey("entities")){
						JSONObject entities = (JSONObject) tweet.get("entities");
							if(entities.containsKey("user_mentions")){
								for(Object mentionObj : (JSONArray)entities.get("user_mentions")){
									JSONObject mention = (JSONObject)mentionObj;
									String id = mention.get("id").toString();
									String screen_name = mention.get("screen_name").toString();
									String name = mention.get("name").toString();
								    if(usersMapList.containsKey(id)) {
								    	Map mapUser = usersMapList.get(id);
								    	mapUser.put("mentioned", (int)1);
								    	if(mapUser.get("text")==null) {
								    		mapUser.put("text", tweet.get("text").toString());
								    	}
									    mapUser.put("counter", ((Integer)mapUser.get("counter"))+1);
								    	usersMapList.remove(id);
								    	usersMapList.put(id, mapUser);
								    } else {
										path = "https://api.twitter.com/1.1/users/show.json?screen_name="+screen_name;
										request = new HttpGet(path);
									    jsonUser = null;
									    String profile_picture = null;
									    try {
									        consumer.sign(request);
									        response = client.execute(request);
									        statusCode = response.getStatusLine().getStatusCode();
											if(statusCode == 200){
												inputStream = response.getEntity().getContent();
												reader = new BufferedReader(new InputStreamReader(inputStream));
												jsonUser = (JSONObject)jsonParser.parse(reader.readLine());
												profile_picture = jsonUser.get("profile_image_url").toString();
											}
								        } catch (Exception e) {
											e.printStackTrace();
								        }
									    Map mapUser = new LinkedHashMap();
									    mapUser.put("id", id);
									    mapUser.put("screen_name", screen_name);
									    mapUser.put("name", name);
									    mapUser.put("profile_picture", profile_picture);
									    mapUser.put("follower", (int)0);
									    mapUser.put("friend", (int)0);
									    mapUser.put("mentioned", (int)1);
								    	if(mapUser.get("text")==null) {
								    		mapUser.put("text", tweet.get("text").toString());
								    	}
								    	mapUser.put("counter", (int)1);
									    Map linkUser = new LinkedHashMap();
									    linkUser.put("source", user_id);
									    linkUser.put("target", id);
									    links.add(linkUser);
									    usersMapList.put(id, mapUser);
								    }
								}
						}
					}
				}
			} else {
				System.out.println("User timeline: reached request rate limit per window!");
			}
        } catch (Exception e) {
			e.printStackTrace();
        }

        
        
        for(Map.Entry<String, Map> entry : usersMapList.entrySet()) {
        	nodes.add(entry.getValue());
        }
		
        mapResults.put("nodes", nodes);
        mapResults.put("links", links);
        
        System.out.println("Nodes-Links created");
        return JSONValue.toJSONString(mapResults);
	}

	

	/* JSON output example
	 * 
{
    "nodes": [
        {
            "id": "1239671",
            "screen_name": "deviantART",
            "name": "deviantART",
            "follower": 0,
            "friend": 0,
            "mentioned": 1,
            "counter": 1
        },
        {...},
        {
            "id": "115499072",
            "screen_name": "teox1986",
            "name": "Matteo Pozza",
            "follower": 0,
            "friend": 0,
            "mentioned": 0,
            "counter": 0
        }
    ],
    "links": [
        {
            "source": "teox1986",
            "target": "deviantART"
        }
    ]
}
	 
	 */

}