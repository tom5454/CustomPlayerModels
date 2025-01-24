(function() {
	document.title = "CPM Embedded Editor (Beta) $ver";
	window.location.hash = 'cpm_plugin_embedded_editor';
	window.addEventListener('beforeunload', () => {
		if (window.opener) window.opener.postMessage({ id: 'window_closed' }, window.location.origin);
	});
	_embeddedPlatform = atob("$platform"); _cpmEmbeddedEditorMarker = true; isApp = false;
	_embeddedHandler = 'web';
	var jsZ = document.createElement('script');
	jsZ.src = 'https://web.blockbench.net/lib/jszip.min.js';
	jsZ.onload = () => {
		console.log('Bootstrap loading');
		var scr = document.createElement('script');
		scr.innerText = atob("$$$");
		document.body.appendChild(scr);
	};
	jsZ.onerror = () => {
		if (window.opener) window.opener.postMessage({ id: 'window_closed', data: 'JSZip failed to load' }, window.location.origin);
		window.close();
	};
	document.body.appendChild(jsZ);
})()