package com.tom.cpm.shared.loaders;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;

import com.tom.cpm.shared.config.ResourceLoader;

public abstract class HttpResourceLoader implements ResourceLoader {
	private static int MAX_SIZE = 1024*1024;//1 MB

	protected abstract URL createURL(String path, ResourceEncoding enc) throws IOException;

	@Override
	public InputStream loadResource(String path, ResourceEncoding enc) throws IOException {
		URL url = createURL(path, enc);
		InputStream web = null;
		URLConnection connection = null;
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			connection = url.openConnection();
			web = connection.getInputStream();

			byte[] buffer = new byte[10240];

			int totalBytesDownloaded = 0;
			int bytesJustDownloaded = 0;
			while((bytesJustDownloaded = web.read(buffer)) > 0) {
				out.write(buffer, 0, bytesJustDownloaded);
				totalBytesDownloaded += bytesJustDownloaded;
				if(totalBytesDownloaded > MAX_SIZE)throw new IOException("File too big");
			}

			switch (enc) {
			case NO_ENCODING:
				return new ByteArrayInputStream(out.toByteArray());

			case BASE64:
				String dl = new String(out.toByteArray());
				return new ByteArrayInputStream(Base64.getDecoder().decode(dl));

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
