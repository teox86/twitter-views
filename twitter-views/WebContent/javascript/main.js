function init()
{
	var selected = new Array();
	
	$('li').each(function looping(index) 
	{
		$(this).mouseover(function onItemOver() 
		{
			$(this).css("cursor","pointer");
		});
		
		$(this).click(function onItemClick() 
		{
			var indiceEl = $.inArray(index, selected);
			if(indiceEl== -1)
			   {
				 checkSelection(index);
				 selected.push(index);
			   }
			else
			   {
				 unCheckSelection(index);
				 selected.splice(indiceEl,1);
			   }

		});
	});
}

function checkSelection(id)
{
	$('li').each(function looping(index) 
	{
		if(index==id)
		{
			$(this).animate( { backgroundColor: "#CCC", color: "#333" }, 500);

		}
	});
}

function unCheckSelection(id)
{
	$('li').each(function looping(index) 
	{
		if(index==id)
		{
			$(this).animate( { backgroundColor: "#333", color: "#CCC" }, 500);
		}
	});
}