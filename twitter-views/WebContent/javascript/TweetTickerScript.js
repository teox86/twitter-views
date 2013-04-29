
	// Show tweets inside the "Tweet ticker"

	$(document).ready(function(){
		$('#twitter-ticker').slideDown('slow');
		var container=$('#tweet-container');
		var settings = {
			autoReinitialise: true
		};
		container.jScrollPane(settings);
		callOntologyServlet("resetOntology");
	});
	
	var tweetsShown = new Number(0);
	var pause = new Boolean(false);
	
	function clearOntology() {
		console.log("click-clearOntology");
		$('div.jspPane div.tweet').remove();
		tweetsShown = 0;
		return true;
	}
	
	function pauseResume() {
		console.log("click-pauseresume");
		if (pause==false) {
			pause = true;
			$("a#pauseResume").html("Resume");
		}
		else {
			pause = false;
			$("a#pauseResume").html("Pause");
		}
		return true;
	}
	
	function resetOntology() {
		callOntologyServlet("resetOntology");
		$('div#accordion').empty();
		$('output#list').empty();
		resetAccordionItems();
		$('div.jspPane div.tweet').remove();
		$("span#ontologyResultsCounter").html("0");
		tweetsShown = 0;
		return true;
	}
	
	jQuery(function($) {
		
		function formatTwitString(str)	// Parse the tweet to create hyperlinks
		{
			str=' '+str;
			str = str.replace(/((ftp|https?):\/\/([-\w\.]+)+(:\d+)?(\/([\w/_\.]*(\?\S+)?)?)?)/gm,'<a href="$1" target="_blank">$1</a>');
			str = str.replace(/([^\w])\@([\w\-]+)/gm,'$1@<a href="http://twitter.com/$2" target="_blank">$2</a>');
			str = str.replace(/(-color:)?\#([\w\-]+)/g, function($0, $1,$2){
				return $1 ? $0 : '<a href="http://twitter.com/search?q='+$2+'" target="_blank">'+$0+'</a>';
			});
			return str;
		}
		
		// Insert a new tweet inside the "Tweet ticker"
		function TweetTick(ob)
		{
			var str = '	<div class="tweet">\
				<div class="avatar"><a href="http://twitter.com/'+ob.user_screen_name+'" target="_blank"><img src="'+ob.user_profile_picture+'" alt="'+ob.user_screen_name+'" /></a></div>\
				<div class="user"><a href="http://twitter.com/'+ob.user_screen_name+'" target="_blank">'+ob.user_screen_name+'</a></div>\
				<div class="time">'+ob.time+'</div>\
				<div class="txt">'+formatTwitString(ob.text)+'</div>\
				</div>';
			$("div#tweet-container > img").remove();
			var container=$('div.jspPane');
			container.prepend(str);
			tweetsShown++;
			if (tweetsShown >= 20) {
				$('div.jspPane div.tweet:last-child').remove();
				tweetsShown--;
			};
		}
		
		function updateOntologyCounter(ob)
		{
			for(var k in ob) {
				var item = ob[k];
				item = item.toLowerCase();
				var counter = $("span.counter#"+item).html();
				var counterInt = parseInt(counter);
				counterInt = counterInt + 1;
				$("span.counter#"+item).html(counterInt);
				var totalCounter = $("span#ontologyResultsCounter").html();
				counterInt = parseInt(totalCounter);
				counterInt = counterInt + 1;
				$("span#ontologyResultsCounter").html(counterInt);
			}
		}
	
		// Functions to handle long polling requests
		function long_polling() { 
		    $.getJSON('AjaxServlet?service=ontologyResult', function(ob) {
		    	if (pause==false) {
		    		TweetTick(ob);
		    	}
		        long_polling(); 
		    }); 
		}
		
		function long_polling_counter() { 
		    $.getJSON('AjaxServlet?service=ontologyResultCounter', function(ob) {
	    		updateOntologyCounter(ob);
	    		long_polling_counter(); 
		    }); 
		} 
	
	
		
	    long_polling();
	    long_polling_counter();
	
	});


