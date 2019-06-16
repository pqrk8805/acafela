package com.acafela.harmony.rpccomm;

import android.util.Log;

import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import io.grpc.ManagedChannel;
import io.grpc.okhttp.OkHttpChannelBuilder;


public class GrpcCommon
{
    private static final String LOG_TAG = "GRPC_COMMON";

    private GrpcCommon()
    {}

    public static ManagedChannel initSecureChannel(
                                            String address,
                                            int port,
                                            InputStream rootCertIS,
                                            InputStream serverCertIS)
    {
        KeyStore keyStore = createKeyStore(rootCertIS, serverCertIS);
        return OkHttpChannelBuilder
                                .forAddress(address, port)
                                .overrideAuthority("localhost")
                                .useTransportSecurity()
                                .sslSocketFactory(getSslSocketFactory(keyStore))
                                .build();
    }

    private static KeyStore createKeyStore(
                                        InputStream rootCertIS,
                                        InputStream serverCertIS)
    {
        KeyStore ks = null;
        try {
            ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            ks.setCertificateEntry(
                            "rootCert",
                            cf.generateCertificate(rootCertIS));
            ks.setCertificateEntry(
                            "serverCert",
                            cf.generateCertificate(serverCertIS));

        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception", e);
        }
        return ks;
    }

    private static SSLSocketFactory getSslSocketFactory(KeyStore keyStore)
    {
        TrustManagerFactory tmf;
        SSLContext context = null;
        try {
            // initialize trust manager factor from certs keystore
            //
            tmf = TrustManagerFactory.getInstance(
                                    TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

            // initialize SSL context from trust manager factory
            //
            context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers() , null);
        } catch (NoSuchAlgorithmException e) {
            Log.e(LOG_TAG, "Exception", e);
        } catch (KeyStoreException e) {
            Log.e(LOG_TAG, "Exception", e);
        } catch (KeyManagementException e) {
            Log.e(LOG_TAG, "Exception", e);
        }
        return context != null ? context.getSocketFactory() : null;
    }
}
