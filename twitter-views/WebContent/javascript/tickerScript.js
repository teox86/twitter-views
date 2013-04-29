var accordionItems = new Array();
var file;
var selected = new Array();

  function handleFileSelect(evt) {
    file = evt.target.files[0]; // FileList object

    // files is a FileList of File objects. List some properties.
    var output = [];
    
    output.push('<li id="fileInfos" style="color: black;"><strong>', escape(file.name), '</strong> (', file.type || 'n/a', ') - ',
                  file.size, ' bytes',
                  '</li><span id="uploadLog"><b></b></span>');

    document.getElementById('list').innerHTML = '<ul>' + output.join('') + '</ul>';
    //Create an input type dynamically.
    var element = document.createElement("input");
 
    //Assign different attributes to the element.
    element.setAttribute("type", "button");
    element.setAttribute("value", "Upload");
    element.setAttribute("name", "UploadButton");
    element.setAttribute("onClick", "parseTxtFile()");
    document.getElementById("list").appendChild(element);
  }

  

  function callOntologyServlet(filterWord) {
      try {                                     //create a request for netscape, mozilla, opera, etc.
          request = new XMLHttpRequest();
      }catch (e) {
          try {                                 //create a request for internet explorer
              request = new ActiveXObject("Microsoft.XMLHTTP");
          }catch (e) {                           //do some error-handling
              alert("XMLHttpRequest error: " + e);
          }
      }
      console.log("filter "+filterWord);
      request.open("GET", "AjaxServlet?service=ontology&filterWord="+filterWord, true);       //prepare the request
      request.send(null);	//send it
      var indiceEl = $.inArray(filterWord, selected);
      	// Update list of selected filter words
		if(indiceEl== -1)
		   {
			 checkSelection(filterWord);
			 selected.push(filterWord);
		   }
		else
		   {
			 unCheckSelection(filterWord);
			 selected.splice(indiceEl,1);
		   }
      return request;   
  }
  
  // Parse user's uploaded word list
  function parseTxtFile() {
    if (file) {
		var reader = new FileReader();
        reader.onload = function(e) { 
			var content = e.target.result;
			// Parsing
			var words = content.split("\n");
			console.log(words);
			var pattern = new RegExp("^#[A-Za-z]+");;
			
			// Test the correctness of the first row (a word for the general Topic)
			if(!pattern.test(words[0]))
				{
					var el = document.getElementById("fileInfos");
					el.style.color = "red";
					var resultLog = document.getElementById("uploadLog.");
					var str = document.createTextNode("Problem: Wrong file structure.");
					resultLog.appendChild(str);
				}
			else if (accordionItems.indexOf(words[0].substring(1,words[0].length)) > -1) {
				var el = document.getElementById("fileInfos");
				el.style.color = "green";
				var resultLog = document.getElementById("uploadLog");
				var str = document.createTextNode("Note: ontology file already loaded.");
				resultLog.removeChild(resultLog.childNodes[0]);
				resultLog.appendChild(str);
			}
			else {
				// Create the accordion list
				/*
					Accordion format:
					<div id="accordion">
						<h3>Section 1</h3>
						<div>
							*content1
						</div>
						<h3>Section 2</h3>
						<div>
							*content2
						</div>
					</div>
				*/
				var itemClass = words[0].substring(1,words[0].length);
				var newAccordionItem = document.createElement("h3");	// Create title for the topic
				newAccordionItem.innerHTML = itemClass;
				var divBox = document.createElement("div");	// All the contained word must go inside the <div>
				divBox.setAttribute("class", "box");
				var uList = document.createElement("ul");	// All words are appended into an unordered list
				uList.setAttribute("class", "myList"); 
				for (var i = 1; i < words.length-1; i++) {
					var elementWord = words[i];
					var element = elementWord.toString().toLowerCase();
					console.log(element);
					var listItem = document.createElement("li");
					listItem.setAttribute("id", element);
					listItem.setAttribute("highlighted", 0);
					listItem.setAttribute("style", "cursor: pointer;");
					listItem.setAttribute("onClick", "callOntologyServlet('"+element+"')");
					listItem.innerHTML = elementWord+" (<span id='"+element+"' class='counter'>0</span>)";
					uList.appendChild(listItem);
				}
				divBox.appendChild(uList);
				$('#accordion').append(newAccordionItem).append(divBox).accordion('destroy').accordion({
					heightStyle: "content",
					collapsible: true
		 		});
				init();
				accordionItems.push(itemClass);
				var el = document.getElementById("fileInfos");
				el.style.color = "green";
			}
        };
        reader.readAsText(file);
    } else {
    	alert("Failed to load file");
    }
  }

  function resetAccordionItems() {
	  accordionItems = [];
	  accordionItems.length = 0;
  }
  
  function checkSelection(id)	// Handle mouse click to select a filter word
  {
	var idItem;
  	$('li').each(function looping(index) 
  	{
  		idItem = $(this).attr("id");
  		if(idItem==id)
  		{
  			$(this).animate( { backgroundColor: "#CCC", color: "#333" }, 500);

  		}
  	});
  }

  function unCheckSelection(id) // Handle mouse click to deselect a filter word
  {
	var idItem;
  	$('li').each(function looping(index) 
  	{
  		idItem = $(this).attr("id");
  		if(idItem==id)
  		{
  			$(this).animate( { backgroundColor: "#333", color: "#CCC" }, 500);
  		}
  	});
  }