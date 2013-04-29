/*
 * Created by: Matteo Pozza <teox86@virgilio.it>
 */

package twitter.streaming;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class TwitterSumarizeHashtags extends BaseBasicBolt{

	private static final int NUMBER_OF_RESULTS = 10;
	private static final int REFRESH_TIME = 5000; // time in ms
	
	private static Map<String, Integer> hashtags = new ConcurrentHashMap<String, Integer>();
	
	@Override
	public void cleanup() {
		
	}
	
	// For every tweet in JSON format, get and emit hashtags on a new output stream
	@Override
	public void execute(Tuple input, BasicOutputCollector collector) {
		JSONObject json = (JSONObject)input.getValueByField("tweet");
		if(json.containsKey("entities")){
			JSONObject entities = (JSONObject) json.get("entities");
				if(entities.containsKey("hashtags")){
					for(Object hashObj : (JSONArray)entities.get("hashtags")){
						JSONObject hashJson = (JSONObject)hashObj;
						String hash = hashJson.get("text").toString().toLowerCase();
						if(!hashtags.containsKey(hash)){
							hashtags.put(hash, 1);
						}else{
							Integer last = hashtags.get(hash);
							hashtags.put(hash, last + 1);
						}
						collector.emit(new Values(hash));
					}
			}
		}
	}

	@Override
	public void prepare(Map stormConf, TopologyContext context) {
	/*	TimerTask task = new TimerTask() {
			
			@Override
			public void run() {
				
				Map mapResults = new LinkedHashMap();
			    List l1 = new LinkedList();

				Map<String, Integer> sortedMap =  sortByComparator(hashtags);

				for(Map.Entry<String, Integer> entry : sortedMap.entrySet()){
				    Map m1 = new LinkedHashMap();
				    m1.put("name", entry.getKey());
				    m1.put("count", entry.getValue());
				    l1.add(m1);
				}
				mapResults.put("children", l1);
			}
			
		};
		Timer t = new Timer();
		t.scheduleAtFixedRate(task, REFRESH_TIME, REFRESH_TIME);*/
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("hashtag"));
	}
/*
	private static Map<String, Integer> sortByComparator(Map<String, Integer> unsortMap) {
		   
		List list = new LinkedList(unsortMap.entrySet());
		
        //sort list based on comparator
        Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o1)).getValue()).
						compareTo(((Map.Entry) (o2)).getValue());
			}
	    });
        
        int listSize = list.size();
	 
    	//put sorted list into map again
		Map sortedMap = new LinkedHashMap();
		if (listSize>=NUMBER_OF_RESULTS) {
			for (int i=0; i<NUMBER_OF_RESULTS; i++) {
				Map.Entry entry = (Map.Entry)list.get(listSize-i-1);
				sortedMap.put(entry.getKey(), entry.getValue());
			}
		}
		return sortedMap;
	}	
	
	public static String getJsonHashtagCount() {
		Map mapResults = new LinkedHashMap();
	    List l1 = new LinkedList();
		Map<String, Integer> sortedMap =  sortByComparator(hashtags);
		for(Map.Entry<String, Integer> entry : sortedMap.entrySet()){
		    Map m1 = new LinkedHashMap();
		    m1.put("name", entry.getKey());
		    m1.put("count", entry.getValue());
		    l1.add(m1);
		}
		mapResults.put("children", l1);
		return JSONValue.toJSONString(mapResults);
	}*/
}
