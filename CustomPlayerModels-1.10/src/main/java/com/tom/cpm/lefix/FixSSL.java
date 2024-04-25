package com.tom.cpm.lefix;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import com.tom.cpl.util.ThrowingBiConsumer;
import com.tom.cpl.util.ThrowingFunction;
import com.tom.cpm.CustomPlayerModels;

//Taken from Minecraft Forge 1.12.2
public class FixSSL {
	public static void fixup() {
		try {
			KeyStore leKS = KeyStore.getInstance(KeyStore.getDefaultType());
			InputStream leKSFile = FixSSL.class.getResourceAsStream("/com/tom/cpm/lefix/cacerts");
			leKS.load(leKSFile, "changeit".toCharArray());
			Map<String, Certificate> leTrustStore = Collections.list(leKS.aliases()).stream().collect(Collectors.toMap(a -> a, rethrowFunction(leKS::getCertificate)));

			KeyStore mergedTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			mergedTrustStore.load(null, new char[0]);
			leTrustStore.forEach(rethrowBiConsumer(mergedTrustStore::setCertificateEntry));

			TrustManagerFactory instance = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			instance.init(mergedTrustStore);
			SSLContext tls = SSLContext.getInstance("TLS");
			tls.init(null, instance.getTrustManagers(), null);
			HttpsURLConnection.setDefaultSSLSocketFactory(tls.getSocketFactory());
			CustomPlayerModels.log.info("Added Lets Encrypt root certificates as additional trust");
		} catch (KeyStoreException|java.io.IOException|java.security.NoSuchAlgorithmException|java.security.cert.CertificateException|java.security.KeyManagementException e) {
			CustomPlayerModels.log.error("Failed to load lets encrypt certificate. Expect problems", e);
		}
	}

	public static <T, U, E extends Exception> BiConsumer<T, U> rethrowBiConsumer(ThrowingBiConsumer<T, U, E> biConsumer) {
		return (t, u) -> {
			try {
				biConsumer.accept(t, u);
			} catch (Exception exception) {
				throwAsUnchecked(exception);
			}
		};
	}

	public static <T, R, E extends Exception> Function<T, R> rethrowFunction(ThrowingFunction<T, R, E> function) {
		return t -> {
			try {
				return function.apply(t);
			} catch (Exception exception) {
				throwAsUnchecked(exception);
				return null;
			}
		};
	}

	@SuppressWarnings("unchecked")
	private static <E extends Throwable> void throwAsUnchecked(Exception exception) throws E {
		throw (E) exception;
	}
}
