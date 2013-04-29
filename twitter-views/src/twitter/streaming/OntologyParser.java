package twitter.streaming;


import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import servlet.AjaxServlet;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;

public class OntologyParser extends BaseBasicBolt{
	
	private final String delimiters = " ,:;._-'”*!?<>#@(){}[]\"";
	Map<String, Integer> hashtags = new ConcurrentHashMap<String, Integer>();
	private static ArrayList<String> filterArray = null;
	
	@Override
	public void cleanup() {
		
	}
 
	// For every tweet retrieved: highlight every filter word in the text part and construct the output (JSON object)
	// The output is returned calling the reverse AJAX function (updateOntology)
	@Override
	public void execute(Tuple input, BasicOutputCollector collector) {
		JSONObject json = (JSONObject)input.getValueByField("tweet");
		filterArray = (ArrayList<String>)input.getValueByField("filterWords");
		String text = new String();
		String time = new String();
		String user_screen_name = new String();
		String user_profile_picture = new String();
		String highlightedText = new String();
		
		if(json.containsKey("text")){
			text = json.get("text").toString();
			StringTokenizer st = new StringTokenizer(text,delimiters,true);
			JSONArray jsonArray = new JSONArray();
			while (st.hasMoreTokens()) {
				String current = st.nextToken();
				if (containsCaseInsensitive(filterArray, current)) {
					jsonArray.add(current);
					String str = "<span style=\"background-color:#fdfa66\">"+current+"</span>";
					current = str;
				}
				highlightedText += current;
		    }
			AjaxServlet.updateOntologyCounter(jsonArray);
		}
		if(json.containsKey("created_at")){
			String created_at = json.get("created_at").toString();
		    StringTokenizer st = new StringTokenizer(created_at);
		    time = new String();
		    time += st.nextToken();
		    for (int i=0; i<3; i++) {
		    	time += ' ';
		    	time += st.nextToken();
		    }
		}
		if(json.containsKey("user")){
			JSONObject userJson = (JSONObject) json.get("user");
			user_screen_name = userJson.get("screen_name").toString();
			user_profile_picture = userJson.get("profile_image_url").toString();
		}
		JSONObject jsonObj = new JSONObject();

		jsonObj.put("user_screen_name", user_screen_name);
		jsonObj.put("user_profile_picture", user_profile_picture);
		jsonObj.put("time", time);
		jsonObj.put("text", highlightedText);
		System.out.println(jsonObj);
		// Reverse AJAX function
		AjaxServlet.updateOntology(jsonObj);

	}

	@Override
	public void prepare(Map stormConf, TopologyContext context) {

	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		
	}
	
	public static boolean containsCaseInsensitive(ArrayList<String> searchList, String searchTerm)
	{
	    for (String item : searchList)
	    {
	        if (item.equalsIgnoreCase(searchTerm)) 
	            return true;
	    }
	    return false;
	}
	

}
