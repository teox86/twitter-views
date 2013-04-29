/*
 * Created by: Matteo Pozza <teox86@virgilio.it>
 */

package twitter.streaming;

/*
 * Pay attention at the Eclipse configuration: 
 * -in java build path -> source make sure that the default output folder is set as: (ProjName)/WebContent/WEB-INF/classes
 * -set appropriate values in the Deployment Assembly
 */

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;

public class Topology {
	
	// Start the Storm topology
	public static void execute(String username, String password) {
		TopologyBuilder builder = new TopologyBuilder();
		
		builder.setSpout("tweets-collector", new TweetsSpout(),1);
		
		builder.setSpout("tweet-filtered-source", new FilteredTweetsSpout("test-signal-spout"),1);
		
		builder.setBolt("hashtag-sumarizer", new TwitterSumarizeHashtags(),3).shuffleGrouping("tweets-collector");
		builder.setBolt("hashtag-result", new HashtagResult(),1).globalGrouping("hashtag-sumarizer");
		
		builder.setBolt("tweet-count", new TweetsCount(),1).shuffleGrouping("tweets-collector");
		
		builder.setBolt("ontology-analysis", new OntologyParser(),3).shuffleGrouping("tweet-filtered-source"); 
		
		LocalCluster cluster = new LocalCluster();
		Config conf = new Config();
		
		System.out.println("Submit topology.");
		cluster.submitTopology("twitter-test", conf, builder.createTopology());
	}

}
