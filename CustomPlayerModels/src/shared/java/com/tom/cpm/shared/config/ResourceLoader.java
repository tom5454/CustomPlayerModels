package com.tom.cpm.shared.config;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import com.tom.cpl.text.FormatText;
import com.tom.cpl.util.LocalizedIOException;
import com.tom.cpm.shared.definition.Link;
import com.tom.cpm.shared.definition.ModelDefinition;

public interface ResourceLoader {
	byte[] loadResource(String path, ResourceEncoding enc, ModelDefinition def) throws IOException;

	default byte[] loadResource(Link link, ResourceEncoding enc, ModelDefinition def) throws IOException {
		return loadResource(link.getPath(), enc, def);
	}

	public static enum ResourceEncoding {
		NO_ENCODING,
		BASE64,
	}

	default Validator getValidator() {
		return null;
	}

	public static class Validator {
		private final String name;
		private final String host;
		private final Pattern url;
		private final String link;
		private final String sample;

		public Validator(String name, String host, String url, String link, String sample) {
			this.name = name;
			this.host = host;
			this.url = Pattern.compile(url);
			this.link = link;
			this.sample = sample;
		}

		public Link test(String link) throws LocalizedIOException, URISyntaxException {
			URI url = new URI(link);
			if(this.host.equals(url.getHost())) {
				if(this.url.matcher(link).matches()) {
					String linkTxt = this.url.matcher(link).replaceAll(this.link);
					return new Link(linkTxt);
				} else throw new LocalizedIOException("Invalid link", new FormatText("label.cpm.link.invalidURL", this.sample));
			}
			return null;
		}

		public String getName() {
			return name;
		}
	}
}
