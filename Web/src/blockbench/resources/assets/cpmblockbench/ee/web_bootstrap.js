(function() {
	document.title = "CPM Embedded Editor (Beta) $ver";
	window.location.hash = 'cpm_plugin_embedded_editor';
	window.addEventListener('beforeunload', () => {
		if (window.opener) window.opener.postMessage({ id: 'window_closed' }, window.location.origin);
	});
	_embeddedPlatform = atob("$platform"); _cpmEmbeddedEditorMarker = true; isApp = false;
})()