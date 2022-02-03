package com.tom.cpm.shared.loaders;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;

import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ResourceLoader;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.definition.SafetyException.BlockReason;
import com.tom.cpm.shared.io.HTTPIO;

public abstract class HttpResourceLoader implements ResourceLoader {

	protected abstract URL createURL(String path) throws IOException;

	@Override
	public byte[] loadResource(String path, ResourceEncoding enc, ModelDefinition def) throws IOException {
		URL url = createURL(path);
		return loadResource(url, enc, def);
	}

	protected byte[] loadResource(URL url, ResourceEncoding enc, ModelDefinition def) throws IOException {
		InputStream web = null;
		URLConnection connection = null;
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			connection = HTTPIO.createUrlConnection(url, false);
			web = connection.getInputStream();

			byte[] buffer = new byte[10240];

			int totalBytesDownloaded = 0;
			int bytesJustDownloaded = 0;
			while((bytesJustDownloaded = web.read(buffer)) > 0) {
				out.write(buffer, 0, bytesJustDownloaded);
				totalBytesDownloaded += bytesJustDownloaded;
				if(def != null)
					ConfigKeys.MAX_LINK_SIZE.checkFor(def.getPlayerObj(), totalBytesDownloaded / 1024, BlockReason.LINK_OVERFLOW);
			}
			switch (enc) {
			case NO_ENCODING:
				return out.toByteArray();

			case BASE64:
				return Base64.getDecoder().decode(new String(out.toByteArray()));

			default:
				throw new IOException("Unsupported file encoding");
			}
		} finally {
			if(connection != null && connection instanceof HttpURLConnection)((HttpURLConnection)connection).disconnect();
			if(web != null)
				try {
					web.close();
				} catch (IOException e) {
				}
		}
	}

}
