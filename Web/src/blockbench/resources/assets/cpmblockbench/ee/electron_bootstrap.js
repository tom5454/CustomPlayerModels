(function() { 
	document.title = "CPM Embedded Editor (Beta) $ver";
	_embeddedPlatform = atob("$platform"); 
	_cpmEmbeddedEditorMarker = true; 
	isApp = true; 
	_embeddedHandler = 'electron';
	_editorWorkDir = atob('$workDir'); 
	electron = require('electron'); 
	var scr = document.createElement('script'); 
	scr.innerText = atob("$$$"); 
	document.body.appendChild(scr); 
})()