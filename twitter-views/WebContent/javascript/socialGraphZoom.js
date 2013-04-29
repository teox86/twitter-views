var Network, activate, root;

// Define margins and positions
var margin = {top: 0, right: 0, bottom: 12, left: 24},
width = 960 - margin.left - margin.right,
height = 500 - margin.top - margin.bottom;

var x = d3.scale.linear()
.domain([-width / 2, width / 2])
.range([0, width]);

var y = d3.scale.linear()
.domain([-height / 2, height / 2])
.range([height, 0]);


root = typeof exports !== "undefined" && exports !== null ? exports : this;

	Network = function() {
		  var allData, charge, curLinksData, curNodesData, filter, filterLinks, filterNodes, force, forceTick, groupCenters, height, hideDetails, layout, link, linkedByIndex, linksG, mapNodes, neighboring, network, node, nodeColors, nodeCounts, nodeR, nodesG, searchUser, setFilter, setLayout, setSort, setupData, showDetails, sort, tooltip, update, updateLinks, updateNodes, width;
		  var vis;
		  width = 700;
		  height = 700;
		  allData = [];
		  curLinksData = [];
		  curNodesData = [];
		  linkedByIndex = {};
		  nodesG = null;
		  linksG = null;
		  rectArea = null;
		  node = null;
		  nodeR = null;
		  link = null;
		  layout = "force";
		  filter = "all";
		  groupCenters = null;
		  force = d3.layout.force();
		  nodeColors = d3.scale.category20();
		  tooltip = Tooltip("vis-tooltip", 230);
		  
		  charge = function(node) {
			  return -Math.pow(node.radius, 2.0) / 2;
		  };
		  
		  // Prepare zoom  behaviour
		  zoom = function() {
			  vis.attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
		  };
		  
		  // Create new social graph inside the 'selection' starting from given 'data'
		  network = function(selection, data) {
			    allData = setupData(data);
			    vis = d3.select(selection).append("svg").attr("width", width).attr("height", height)
			    	.append("g")
			    		.call(d3.behavior.zoom().scaleExtent([0.2, 2.5]).on("zoom", zoom))
			    	.append("g");
			    
			    linksG = vis.append("g").attr("id", "links");
			    nodesG = vis.append("g").attr("id", "nodes");
			    force.size([width, height]);
			    setLayout("force");
			    setFilter("all");
			    return update();
		  };
		  
		  update = function() {
			    curNodesData = filterNodes(allData.nodes);
			    curLinksData = filterLinks(allData.links, curNodesData);
			    force.nodes(curNodesData);
			    updateNodes();
			    if (layout === "force") {
			      force.links(curLinksData);
			      updateLinks();
			    } else {
			      force.links([]);
			      if (link) {
			        link.data([]).exit().remove();
			        link = null;
			      }
			    }
			    return force.start();
		  };

		  
		  network.toggleFilter = function(newFilter) {
			    force.stop();
			    setFilter(newFilter);
			    return update();
		  };
	  
		  network.updateData = function(newData) {
			    allData = setupData(newData);
			    link.remove();
			    node.remove();
			    nodeR.remove();
			    return update();
		  };
		  
		  setupData = function(data) {
			    var circleRadius, countExtent, linkWidth, nodesMap;
			    countExtent = d3.extent(data.nodes, function(d) {
			      return d.counter;
			    });
			    circleRadius = d3.scale.sqrt().range([24, 56]).domain(countExtent);
			    linkWidth = d3.scale.sqrt().range([0.8, 10]).domain(countExtent);
			    data.nodes.forEach(function(n) {
			      var randomnumber;
			      n.x = randomnumber = Math.floor(Math.random() * width);
			      n.y = randomnumber = Math.floor(Math.random() * height);
			      n.radius = circleRadius(n.counter);
			      return n.linkWidth = linkWidth(n.counter);
			    });
			    nodesMap = mapNodes(data.nodes);
			    data.links.forEach(function(l) {
			      l.source = nodesMap.get(l.source);
			      l.target = nodesMap.get(l.target);
			      return linkedByIndex["" + l.source.id + "," + l.target.id] = 1;
			    });
			    return data;
		  };
		  
		  mapNodes = function(nodes) {
			    var nodesMap;
			    nodesMap = d3.map();
			    nodes.forEach(function(n) {
			      return nodesMap.set(n.id, n);
			    });
			    return nodesMap;
		  };
		  
		  nodeCounts = function(nodes, attr) {
			    var counts;
			    counts = {};
			    nodes.forEach(function(d) {
			      var _name, _ref;
			      if ((_ref = counts[_name = d[attr]]) == null) {
			        counts[_name] = 0;
			      }
			      return counts[d[attr]] += 1;
			    });
			    return counts;
		  };
		  
		  neighboring = function(a, b) {
			  return linkedByIndex[a.id + "," + b.id] || linkedByIndex[b.id + "," + a.id];
		  };
		  
		  filterNodes = function(allNodes) {
			    var filteredNodes;
			    filteredNodes = allNodes;
			    if (filter === "followers" || filter === "friends" || filter === "mentioned") {
			      filteredNodes = allNodes.filter(function(n) {
			        if (filter === "followers") {
			          return n.follower === 1;
			        } else if (filter === "friends") {
			          return n.friend === 1;
			        } else if (filter === "mentioned") {
			          return n.mentioned === 1;
			        }
			      });
			    }
			    return filteredNodes;
		  };
		
		  filterLinks = function(allLinks, curNodes) {
			    curNodes = mapNodes(curNodes);
			    return allLinks.filter(function(l) {
			      return curNodes.get(l.source.id) && curNodes.get(l.target.id);
			    });
		  };
		  
		  updateNodes = function() {
			    var defShapes, feGaussianBlur, feOffset;
			    defShapes = nodesG.append("defs");
			    filter = defShapes.append("filter").attr("id", "f1").attr("x", "0").attr("y", "0").attr("width", "200%").attr("height", "200%");
			    feOffset = filter.append("feOffset").attr("result", "offOut").attr("in", "SourceGraphic").attr("dx", "10").attr("dy", "10");
			    feGaussianBlur = filter.append("feGaussianBlur").attr("result", "blurOut").attr("in", "offOut").attr("stdDeviation", "2");
			    nodeR = nodesG.selectAll("rect.node").data(curNodesData, function(d) {
			      return d.id;
			    });
			    // Create coloured frame around user's profile picture
			    // Light colours are for never mentioned contacts
			    nodeR.enter().append("rect").attr("class", "node").attr("x", function(d) {
			      return d.x - 13;
			    }).attr("y", function(d) {
			      return d.y - 13;
			    }).attr("width", function(d) {
			      return d.radius + 5;
			    }).attr("height", function(d) {
			      return d.radius + 5;
			    }).attr("stroke", function(d) {
			      if (d.main === 1) {
			        return "yellow";
			      } else if (d.friend === 0 && d.follower === 0 && d.mentioned === 1) {
			        // only mentioned
			    	return "#E600E2";	//pink
			      } else if (d.friend === 1 && d.follower === 1 && d.mentioned === 1) {
			    	// friend&follower mentioned
			        return "#37FF05";	//green
			      } else if (d.friend === 1 && d.follower === 1 && d.mentioned === 0) {
			    	// friend&follower NOT mentioned
			    	return "#99FF80";	//light green
			      } else if (d.friend === 0 && d.follower === 1 && d.mentioned === 1) {
			        // follower mentioned
			    	return "#0000FF";	//blue
			      } else if (d.friend === 0 && d.follower === 1 && d.mentioned === 0) {
			        // follower NOT mentioned
			    	return "#6666FF";	//light blue
			      } else if (d.friend === 1 && d.follower === 0 && d.mentioned === 1) {
			        // friend mentioned
			    	return "#FF0000";	//red
			      } else if (d.friend === 1 && d.follower === 0 && d.mentioned === 0) {
			        // friend NOT mentioned
			    	return "#FF6666";	//light red
			      } else {
			        return "grey";
			      }
			    }).attr("stroke-width", "2").attr("fill", function(d) {
			      if (d.main === 1) {
			        return "yellow";
			      } else if (d.friend === 0 && d.follower === 0 && d.mentioned === 1) {
			        return "#E600E2";
			      } else if (d.friend === 1 && d.follower === 1 && d.mentioned === 1) {
			        return "#37FF05";
			      } else if (d.friend === 1 && d.follower === 1 && d.mentioned === 0) {
			        return "#99FF80";
			      } else if (d.friend === 0 && d.follower === 1 && d.mentioned === 1) {
			        return "#0000FF";
			      } else if (d.friend === 0 && d.follower === 1 && d.mentioned === 0) {
			        return "#6666FF";
			      } else if (d.friend === 1 && d.follower === 0 && d.mentioned === 1) {
			        return "#FF0000";
			      } else if (d.friend === 1 && d.follower === 0 && d.mentioned === 0) {
			        return "#FF6666";
			      } else {
			        return "grey";
			      }
			    }).attr("filter", "url(#f1)");
			    node = nodesG.selectAll("image.node").data(curNodesData, function(d) {
			      return d.id;
			    });
			    node.enter().append("image").attr("class", "node").attr("style","cursor: pointer").attr("xlink:href", function(d) {
			      return d.profile_picture;
			    }).attr("x", function(d) {
			      return d.x;
			    }).attr("y", function(d) {
			      return d.y;
			    }).attr("width", function(d) {
			      return d.radius;
			    }).attr("height", function(d) {
			      return d.radius;
			    });
			    // Show/hide details and enable clicking on profile pictures to search for user's social graph
			    node.on("mouseover", showDetails).on("click", searchUser).on("mouseout", hideDetails);
			    nodeR.exit().remove();
			    return node.exit().remove();
		  };
		 
		  // update links and colour lines
		  updateLinks = function() {
			    link = linksG.selectAll("line.link").data(curLinksData, function(d) {
			      return "" + d.source.id + "_" + d.target.id;
			    });
			    link.enter().append("line").attr("class", "link").style("stroke-width", function(d) {
			      return d.target.linkWidth;
			    }).attr("stroke", function(d) {
			      if (d.target.friend === 1 && d.target.follower === 1 ) {
			        return "#70FF4D";	//light green
			      } else if (d.target.friend === 0 && d.target.follower === 1 ) {
			        return "#6666FF";	//light blue
			      } else if (d.target.friend === 1 && d.target.follower === 0 ) {
			        return "#FF4D4D";	//light red
			      } else if (d.target.friend === 0 && d.target.follower === 0 ) {
			        return "#E600E2";	//pink
			      } else {
			        return "#ddd";	// grey
			      }
			    }).attr("stroke-opacity", 0.8).attr("x1", function(d) {
			      return d.source.x + d.source.radius / 2;
			    }).attr("y1", function(d) {
			      return d.source.y + d.source.radius / 2;
			    }).attr("x2", function(d) {
			      return d.source.x + d.source.radius / 2;
			    }).attr("y2", function(d) {
			      return d.source.y + d.source.radius / 2;
			    });
			    return link.exit().remove();
		  };
		  
		  setLayout = function(newLayout) {
			    layout = newLayout;
			    if (layout === "force") {
			      return force.on("tick", forceTick).charge(-1000).linkDistance(100);
			    }
		  };
		  
		  setFilter = function(newFilter) {
			  return filter = newFilter;
		  };
		  
		  setSort = function(newSort) {
			  return sort = newSort;
		  };
		  
		  forceTick = function(e) {
			    node.attr("x", function(d) {
			      return d.x;
			    }).attr("y", function(d) {
			      return d.y;
			    });
			    nodeR.attr("x", function(d) {
			      return d.x - 13;
			    }).attr("y", function(d) {
			      return d.y - 13;
			    });
			    return link.attr("x1", function(d) {
			      return d.source.x + d.source.radius / 2;
			    }).attr("y1", function(d) {
			      return d.source.y + d.source.radius / 2;
			    }).attr("x2", function(d) {
			      return d.target.x + d.target.radius / 2;
			    }).attr("y2", function(d) {
			      return d.target.y + d.target.radius / 2;
			    });
		  };
		  
		  // Create and show tooltip containing user's info
		  showDetails = function(d, i) {
			    var content;
			    content = '<p class="main">' + d.name + '</span></p>';
			    content += '<hr class="tooltip-hr">';
			    content += '<p class="main">Mentioned ' + d.counter + ' time(s)</span></p>';
			    content += '<hr class="tooltip-hr">';
			    content += '<p class="main"><a href="https://twitter.com/' + d.screen_name + '" target="_blank">Link to ' + d.name + '\'s page</a></span></p>';
			    content += '<hr class="tooltip-hr">';
			    if (d.counter > 0) {
			      content += '<p class="main">Last tweet: ' + d.text + '</span></p>';
			    } else {
			      content += '<p class="main">No recent mentions</span></p>';
			    }
			    tooltip.showTooltip(content, d3.event);
			    if (link) {
			      link.attr("stroke-opacity", function(l) {
			        if (l.source === d || l.target === d) {
			          return 1.0;
			        } else {
			          return 0.5;
			        }
			      });
			    }
			    node.style("stroke", function(n) {
			      if (n.searched || neighboring(d, n)) {
			        return "#555";
			      }
			    }).style("stroke-width", function(n) {
			      if (n.searched || neighboring(d, n)) {
			        return 2.0;
			      } else {
			        return 1.0;
			      }
			    });
			    return d3.select(this).style("stroke", "black").style("stroke-width", 2.0);
		  };
		  
		  searchUser = function(d, i) {
			    return d3.json("AjaxServlet?service=socialGraph&searchUser=" + d.screen_name, function(json) {
			      return network.updateData(json);
			    });
		  };
		  
		  // Hide user's info
		  hideDetails = function(d, i) {
			    tooltip.hideTooltip();
			    node.style("stroke", function(n) {
			      if (n.searched) {
			        return "#555";
			      }
			    }).style("stroke-width", function(n) {
			      if (!n.searched) {
			        return 1.0;
			      } else {
			        return 2.0;
			      }
			    });
			    if (link) {
			      return link.attr("stroke-opacity", 0.8);
			    }
		  };
		  
		  return network;
	};

	activate = function(group, link) {
		  d3.selectAll("#" + group + " a").classed("active", false);
		  return d3.select("#" + group + " #" + link).classed("active", true);
	};

	$(function() {
		  var myNetwork;
		  myNetwork = Network();
		  // search for a new user
		  // -> handle button click
		  $("#search_user_button").on("click", function(d) {
		    var searchUser;
		    searchUser = $("#usernameInput").val();
		    console.log("richiesto searchUser "+searchUser);
		    return d3.json("AjaxServlet?service=socialGraph&searchUser=" + searchUser, function(json) {
		      return myNetwork.updateData(json);
		    });
		  });
		  // -> handle "enter" click
		  $("#usernameInput").keyup(function(d) {
			  if(event.keyCode == 13){  
				  var searchUser;
				  searchUser = $("#usernameInput").val();
				  $("#usernameInput").val("");
				  return d3.json("AjaxServlet?service=socialGraph&searchUser=" + searchUser, function(json) {
					  return myNetwork.updateData(json);
				  });
			  }
		  });
		  
		  // filter view
		  d3.selectAll("#filters a").on("click", function(d) {
		    var newFilter;
		    newFilter = d3.select(this).attr("id");
		    activate("filters", newFilter);
		    return myNetwork.toggleFilter(newFilter);
		  });
		  
		  // create logged user's social graph
		  return d3.json("AjaxServlet?service=socialGraph", function(json) {
		    return myNetwork("#vis", json);
		  });
	});
