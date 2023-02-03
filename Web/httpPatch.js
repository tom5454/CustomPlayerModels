(function() {
Plugin.register('httpPatch', {
		name: 'HttpPatch',
		author: 'tom5454',
		description: '',
		tags: [],
		icon: 'icon-player',
		version: '1',
		variant: 'both',
		onload() {
			https.globalAgent.protocol = "http:";
		},
		onunload() {}
	});
})()