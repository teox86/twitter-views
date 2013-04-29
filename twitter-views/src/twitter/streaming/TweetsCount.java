package twitter.streaming;


import java.util.Map;


import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;

import java.util.concurrent.ConcurrentHashMap;

public class TweetsCount extends BaseBasicBolt{
	
	private int tweetsCount = 0;
	private static int hundredsTweets = 0;
	
	Map<String, Integer> hashtags = new ConcurrentHashMap<String, Integer>();
	
	@Override
	public void cleanup() {
		
	}
 
	@Override
	public void execute(Tuple input, BasicOutputCollector collector) {
		tweetsCount++;
		// Count tweets received
		if (!(((int)tweetsCount/100)==hundredsTweets)) {
			hundredsTweets = (int)(tweetsCount/100);
		}
	}

	@Override
	public void prepare(Map stormConf, TopologyContext context) {

	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
	}
	
	public static int getTweeetsCount() {
		return hundredsTweets;
	}

}
