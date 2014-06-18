function restoreJobGroupCollapseState(viewName, groupName) 
{
	var collapseState = getGroupState(viewName,groupName);
	handle=$$("#handle_"+groupName	).first();
	if (collapseState == 'none') {
		hideJobGroup(handle, viewName,groupName)
	}
	else {
		showJobGroup(handle, viewName,groupName)
	}
}

function toggleJobGroupVisibility(viewName, group) 
{
	var handle=$$("#handle_"+group).first();
	if (handle.getAttribute("collapseState") == "collapsed") {
		showJobGroup(handle, viewName,group)
	}
	else {
		hideJobGroup(handle, viewName,group)
	}
}

function fadeOutGroup(group, callback) {
    $$('.'+group).each(
   		function(e){
               $(e).setOpacity(1)
               var anim=new YAHOO.util.Anim(e, {
                   opacity: { to:0.2 }
               }, 0.2, YAHOO.util.Easing.easeOut);
               anim.onComplete.subscribe(function(){e.style.display="none"});
               if(callback) anim.onComplete.subscribe(callback);
               anim.animate();
   		}
   	)
}

function fadeInGroup(group, callback) {
    $$('.'+group).each(
        function(e){
            e.style.display="";
            $(e).setOpacity(0)
            var anim=new YAHOO.util.Anim(e, {
                opacity: { to:1 }
            }, 0.2, YAHOO.util.Easing.easeIn);
            if(callback) anim.onComplete.subscribe(callback);
            anim.animate();
        }
	)
}

function hideJobGroup(handle, viewName, group) {
	handle.setAttribute("collapseState", "collapsed");
    fadeOutGroup(group);
	setGroupState(viewName,group, "none");
	var src = $$("#handle_"+group+" img").first().src;
	src = src.replace(/collapse.png/,"expand.png")
	$$("#handle_"+group+" img").first().src = src;
}

function showJobGroup(handle, viewName, group) {
    var v=$$("#projectstatus").first();
    if("true"== v.getAttribute('onlyExpandSelected'))
    {
        var viewName=v.getAttribute('viewName')
        $$(".categoryJobRow").each(
            function(e){
                var otherGroup=e.getAttribute("category")
                if(otherGroup!=group)
                {
                    var handle=$$("#handle_"+otherGroup).first()
                    hideJobGroup(handle, viewName, otherGroup)
                }
            }
        )
    }
    if("true"==v.getAttribute('lazyLoading'))
    {
        var url=v.getAttribute('viewUrl')+"lazyJobList?group="+group;
        var loader=v.getAttribute('pluginUrl')+"/images/ajax-loader.gif";
        var columnCount=v.getAttribute('columnCount');
        $$('#ctb_'+group).first().innerHTML="<tr><td>&nbsp;</td><td colspan='"+columnCount+"'><img src='"+loader+"'/></td></tr>";
        fadeInGroup(group);
        new Ajax.Request(url, {
      			method: 'post',
      			onSuccess: function (x)
      			{
                    fadeOutGroup(group, function(){
                        $$('#ctb_'+group).first().innerHTML=x.responseText;
                        fadeInGroup(group);
                    });
                },
      			onFailure: function (x)
      			{
      				window.alert("ERROR: Failed to load: " + x.statusText);
                    hideJobGroup(handle, viewName, group);
      			}
      		});
    }
    else fadeInGroup(group);
    handle.setAttribute("collapseState", "expanded");
    setGroupState(viewName, group, "");
    var src = $$("#handle_"+group+" img").first().src;
    src = src.replace(/expand.png/,"collapse.png")
    $$("#handle_"+group+" img").first().src = src;
}

function getGroupStates(viewName) {
	var stateCookie = YAHOO.util.Cookie.get("jenkins.categorized-view-collapse-state_"+viewName);
	if (stateCookie == null)
		return {};
	return JSON.parse(stateCookie);
}

function getGroupState(viewName, groupName) {
	var groupStates = getGroupStates(viewName)
	
	if (groupStates[groupName]==null) {
		setGroupState(viewName, groupName, "none");
		return "none";
	}
	return groupStates[groupName];
}

function setGroupState(viewName, groupName, state) 
{
	var groupStates = getGroupStates(viewName)
	groupStates[groupName]=state
	YAHOO.util.Cookie.set("jenkins.categorized-view-collapse-state_"+viewName, Object.toJSON(groupStates));
}
