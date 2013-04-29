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
import org.json.simple.JSONValue;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;

public class HashtagResult extends BaseBasicBolt{

	private static final int NUMBER_OF_RESULTS = 20;
	private static final int REFRESH_TIME = 5000; // time in ms
	
	private static Map<String, Integer> hashtags = new ConcurrentHashMap<String, Integer>();
	
	@Override
	public void cleanup() {
		
	}
 
	@Override
	public void execute(Tuple input, BasicOutputCollector collector) {
		String hashtag = (String)input.getValueByField("hashtag");
		if(!hashtags.containsKey(hashtag)){
			hashtags.put(hashtag, 1);
		}else{
			Integer last = hashtags.get(hashtag);
			hashtags.put(hashtag, last + 1);
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
	}

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
	
	// Return the hashtag count (called by AjaxServlet)
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
	}
}
