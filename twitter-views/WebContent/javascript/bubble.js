/*
 * Created by: Matteo Pozza <teox86@virgilio.it>
 */
// Hashtag count bubble graph

var serverResponse;

var r = 500,
    format = d3.format(",d"),
    fill = d3.scale.category20c();

var bubble = d3.layout.pack()
    .sort(null)
    .size([r, r])
    .padding(1.5);

var vis, node;
var stop = false;
var button=d3.select("#exeControl");

var vis = d3.select("#chart").append("svg")
    .attr("width", r)
    .attr("height", r)
    .attr("class", "bubble");

function initialize() {
	
	// Submit GET request and work on the 'json' result
	d3.json("AjaxServlet?service=hashtagCount", function(json) {
	  node = vis.selectAll("g.node")
	      .data(bubble.nodes(classes(json))
	      .filter(function(d) { return !d.children; }), function(d) {return d.className;});
	      
	  // enter() for new hashtags
	  node.enter().append("g")
	      .attr("class", "node")
	      .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
	
	  node.append("title")
	      .text(function(d) { return d.className + ": " + format(d.value); });
  
	  var defShapes = node.append("defs");
	  
	  defShapes.append("circle")
	           .attr("id", function(d) { return d.className; })
	           .attr("cx", 0)
	           .attr("cy", 0)
	           .attr("r", function(d) { return d.r; });
	  
	  // For roundness effect
	  var radialGradient = defShapes.append("radialGradient")
	  								.attr("id", function(d) { return hashCode(d.className); })
	  								.attr("cx", "50%")
	  								.attr("cy", "50%")
	  								.attr("r", "50%")
	  								.attr("fx", "25%")
	  								.attr("fy", "25%");
	  								
	  radialGradient.append("stop")
	  				.attr("stop-color", "white")
	  				.attr("offset", "0%");
	  				
	  radialGradient.append("stop")
	  				.attr("stop-color", "white")
	  				.attr("offset", "30%");
	  				
	  radialGradient.append("stop")
	  				.attr("stop-color", function(d) { return fill(d.className); })
	  				.attr("offset", "100%");
	  		   
	  node.append("use")
	      .attr("xlink:href", function(d) { return "#"+d.className; })
	      .attr("fill", function(d) { return "url(#"+hashCode(d.className)+")"; });

// Insert text in bubble	  	
	  node.append("text")
	      .attr("text-anchor", "middle")
	      .attr("dy", ".3em")
	      .text(function(d) { return d.className.substring(0, d.r / 7); })
	      .style("font-size", function (d) { return ((d.r / 75)) + "em"; })
	      .style("opacity", 1e-6)
		  .transition().delay(300).duration(1000)
		  .style("opacity", 1);
	      
// Insert value in bubble	      
	  node.append("text")
	  	  .attr("id","valueText")
	      .attr("text-anchor", "middle")
	      .attr("dy", "2em")
	      .text(function(d) { return d.value; })
	      .style("font-size", function (d) { return ((d.r / 75)) + "em"; })
	      .style("opacity", 1e-6)
		  .transition().delay(300).duration(1000)
		  .style("opacity", 1);
	});
}

// Initialize and start auto-refresh
initialize();
var autoRefresh = setInterval(function(){update();}, 5000);

// Buttons control
button.on("click", function() {
  console.log("Button Pushed");
  if (stop==false) {
  	clearInterval(autoRefresh);
  	stop = true;
  	d3.select("#exeControl").text("Start");
  }
  else {
  	stop = false;
	d3.select("#exeControl").text("Stop");
  	update();
  	autoRefresh = setInterval(function(){update();}, 5000);
  }
});


function update() {
	
	d3.json("AjaxServlet?service=hashtagCount", function(json) {

		
	  node = vis.selectAll("g.node")
	      .data(bubble.nodes(classes(json))
	      .filter(function(d) { return !d.children; }), function(d) {return d.className;});

// Removing old nodes	  
	node.exit().selectAll("circle")
	  .transition()
	  .duration(1000).attr("r", 1e-6);
	node.exit().selectAll("text")
	  .transition()
	  .duration(1000).style("opacity", 1e-6);
	node.exit().transition().duration(1000).remove();
//

    // Move bubble node to new position
    var trans = this.node
        .transition()
        .duration(5000)
        .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; })
        .attr("r", function(d) { return d.r; });

    // ... update circle radius
    trans.select("circle")
        .transition()
        .duration(5000)
        .attr("r", function(d) { return d.r; });

    // ... update text size and value
    this.node.select("text")
	      .style("font-size", function (d) { return ((d.r / 75)) + "em"; });
	
	this.node.select("#valueText")
	      .text(function(d) { return d.value; })
	      .style("font-size", function (d) { return ((d.r / 5000)) + "em"; })
	      .transition()
          .duration(1000)
	      .style("font-size", function (d) { return ((d.r / 75)) + "em"; });
	      
	this.node.select("title")
	      .text(function(d) { return d.className + ": " + format(d.value); });

// Add new nodes
	  var added = node.enter().append("g");
	  added
	      .attr("class", "node")
	      .attr("transform", function(d) { return "translate(" + Math.random() * r + "," + Math.random() * r + ")"; })
	      .transition()
          .duration(5000)
	      .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
	
	  added.append("title")
	      .text(function(d) { return d.className + ": " + format(d.value); });
	
	  var defShapes = added.append("defs");
	  
			  defShapes.append("circle")
			           .attr("id", function(d) { return d.className; })
			           .attr("cx", 0)
			           .attr("cy", 0)
			           .attr("r", function(d) { return d.r; });
			  
			  // For roundness effect
			  var radialGradient = defShapes.append("radialGradient")
			  								.attr("id", function(d) { return hashCode(d.className); })
			  								.attr("cx", "50%")
			  								.attr("cy", "50%")
			  								.attr("r", "50%")
			  								.attr("fx", "25%")
			  								.attr("fy", "25%");
			  								
			  radialGradient.append("stop")
			  				.attr("stop-color", "white")
			  				.attr("offset", "0%");
			  				
			  radialGradient.append("stop")
			  				.attr("stop-color", "white")
			  				.attr("offset", "30%");
			  				
			  radialGradient.append("stop")
			  				.attr("stop-color", function(d) { return fill(d.className); })
			  				.attr("offset", "100%");
	  		   
	  added.append("use")
	      .attr("xlink:href", function(d) { return "#"+d.className; })
	      .attr("fill", function(d) { return "url(#"+hashCode(d.className)+")"; });
	
	  // Insert text in bubble
	  added.append("text")
	      .attr("text-anchor", "middle")
	      .attr("dy", ".3em")
	      .text(function(d) { return d.className.substring(0, d.r / 7); })
	      .style("opacity", 1e-6)
	      .style("font-size", 1e-6)
		  .transition().delay(500).duration(1000)
		  .style("font-size", function (d) { return ((d.r / 75)) + "em"; })
		  .style("opacity", 1);
	  
	  // Insert value in bubble
	  added.append("text")
	  	  .attr("id","valueText")
	      .attr("text-anchor", "middle")
	      .text(function(d) { return d.value; })
	      .style("opacity", 1e-6)
	      .style("font-size", 1e-6)
		  .transition().delay(500).duration(1000)
		  .style("font-size", function (d) { return ((d.r / 75)) + "em"; })
		  .attr("dy", "2em")
		  .style("opacity", 1);
	});
}

// Returns a flattened hierarchy containing all leaf nodes under the root.
function classes(root) {
  var classes = [];

  function recurse(name, node) {
    if (node.children) node.children.forEach(function(child) { recurse(node.name, child); });
    else classes.push({className: node.name, value: node.count});
  }

  recurse(null, root);
  return {children: classes};
}

hashCode = function(str){
    var hash = 0;
    if (str.length == 0) return hash;
    for (i = 0; i < str.length; i++) {
        char = str.charCodeAt(i);
        hash = ((hash<<5)-hash)+char;
        hash = hash & hash; // Convert to 32bit integer
    }
    return hash;
}