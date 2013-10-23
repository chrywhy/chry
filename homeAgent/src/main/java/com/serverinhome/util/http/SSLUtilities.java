package com.serverinhome.util.http;

import com.serverinhome.common.logger.LogMsg;
import com.serverinhome.common.logger.LogNotice;
import com.serverinhome.common.logger.Logger;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;


public class SSLUtilities {
    private static char[] passphrase = "changeit".toCharArray();
    private static StrictHostnameVerifier hostnameVerifier = new StrictHostnameVerifier();
    private static Set<String> _certCheckedHosts = Collections.synchronizedSet(new HashSet<String>());
    private static Set<String> _trustedHosts = Collections.synchronizedSet(new HashSet<String>());
    private static Set<String> _certCheckingHosts = Collections.synchronizedSet(new HashSet<String>());
    
    private static int CERT_CHECK_CONN_TIMOUT_MS = 5000;    //5 seconds
    private static int CERT_CHECK_READ_TIMOUT_MS = 5000;    //5 seconds

    private static KeyStore readKeyStore() {
        // let's not use any key store ...
        return null;
/*
        File file = new File("jssecacerts");

        if (file.isFile() == false) {
            char SEP = File.separatorChar;
            File dir = new File(System.getProperty("java.home") + SEP + "lib" + SEP + "security");
            file = new File(dir, "jssecacerts");
            if (file.isFile() == false) {
                file = new File(dir, "cacerts");
            }
        }

        Logger.warn("SSLUtilities.readKeyStore", "Loading KeyStore from file \"" + file + "\"...");
        InputStream in = new FileInputStream(file);
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(in, passphrase);
        in.close();

        return ks;
*/
    }

    private static boolean isHostInCertChecking(String host) {
        return _certCheckingHosts.contains(host);
    }

    private static void beginHostCertChecking(String host) {
        _certCheckingHosts.add(host);
    }
    
    private static void endHostCertChecking(String host) {
        _certCheckingHosts.remove(host);
    }
    
    public static boolean isHostCertChecked(String host) {
        return _certCheckedHosts.contains(host);
    }

    public static void checkHostCert(String host) {
        _certCheckedHosts.add(host);
    }
    
    public static boolean isHostTrusted(String host) {
        return _trustedHosts.contains(host);
    }

    public static void trustHostWithCertification(String host) {
        LogMsg.info("Trust host with SSL certification", "host=" + host);
        _trustedHosts.add(host);
    }

    public static void trustHostWithoutCertification(String host) {
        LogMsg.info("Trust host without SSL certification", "host=" + host);
        _trustedHosts.add(host);
    }

    public static void checkSSLCertification(String sUrl) throws Exception {
        URL url;
        try {
            url = new URL(sUrl);
        }
        catch (MalformedURLException e) {
            LogMsg.error("Invalid URL", "URL=" + sUrl);
            return; //Do not check invalid URL
        }
        String protocol = url.getProtocol();
        if (!protocol.equalsIgnoreCase("https")) {
            LogMsg.debug("SSL not enabled. No checking needed.", "URL=" + sUrl);
            return;
        }

        String host = url.getHost();
        checkServerCertification(host, url);
    }

    public static void checkServerCertification(String host, URL url)
            throws IOException {
        if (isHostCertChecked(host)) {
            LogMsg.debug("Host certification already chedcked", "host=" + host
                    + ", Checked= " + isHostCertChecked(host)
                    + ", trusted=" + isHostTrusted(host)
                    + ", In_checking=" + isHostInCertChecking(host));
            return;
        }
        checkHostCert(host);
        
        LogMsg.debug("Checking server certification ", "Server=" + host
                + ", Checked= " + isHostCertChecked(host)
                + ", trusted=" + isHostTrusted(host)
                + ", In_checking=" + isHostInCertChecking(host));
        KeyStore ks = readKeyStore();
        SSLContext context;
        try {
            context = SSLContext.getInstance("TLS");
            String alg = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf;
            tmf = TrustManagerFactory.getInstance(alg);
            tmf.init(ks);
            X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
            CustomTrustManager tm = new CustomTrustManager(defaultTrustManager, host);
            context.init(null, new TrustManager[]{tm}, null);

            javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
            javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
        }
        catch (Exception e) {
            String logContext = "host=" + host
                    + ", Checked= " + isHostCertChecked(host)
                    + ", trusted=" + isHostTrusted(host)
                    + ", In_checking=" + isHostInCertChecking(host);
                    
            LogNotice.error("Failed to checking certification", "ignore", logContext, e);
        }

        InputStream is = null;
        try {
            beginHostCertChecking(host);
            LogMsg.info("Begin host certification check", "host=" + host
                    + ", Checked= " + isHostCertChecked(host)
                    + ", trusted=" + isHostTrusted(host)
                    + ", In_checking=" + isHostInCertChecking(host));
            try {
                URLConnection conn = url.openConnection();
                conn.setConnectTimeout(CERT_CHECK_CONN_TIMOUT_MS);
                conn.setReadTimeout(CERT_CHECK_READ_TIMOUT_MS);
                is = conn.getInputStream();
                trustHostWithCertification(host);
            }
            catch (IOException e) {
                LogMsg.warn("Host certification failed", "host=" + host
                        + ", Checked= " + isHostCertChecked(host)
                        + ", trusted=" + isHostTrusted(host)
                        + ", In_checking=" + isHostInCertChecking(host));
                throw e;
            }
            catch (Throwable e) {
                LogNotice.warn("Unexpected error during certification checking", "ignore", "host=" + host
                        + ", Checked= " + isHostCertChecked(host)
                        + ", trusted=" + isHostTrusted(host)
                        + ", In_checking=" + isHostInCertChecking(host));
            }
        }
        finally {
            endHostCertChecking(host);
            LogMsg.info("Host certification complete", "host=" + host
                    + ", Checked= " + isHostCertChecked(host)
                    + ", trusted=" + isHostTrusted(host)
                    + ", In_checking=" + isHostInCertChecking(host));
            if (is != null) {
                try {
                    is.close();
                }
                catch (Exception e) {
                }
            }
        }
    }

    public static boolean initSSL(String host, int port) throws Exception {
        KeyStore ks = readKeyStore();

        SSLContext context = SSLContext.getInstance("TLS");
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);
        X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
        CustomTrustManager tm = new CustomTrustManager(defaultTrustManager, host);
        context.init(null, new TrustManager[]{tm}, null);

        if (testSSLConnection(context, host, port)) {
            return true;
        }

        X509Certificate[] chain = tm.chain;
        if (chain == null) {
            Logger.trace("SSLUtilities.initSSL", "Could not obtain server certificate chain.");
            return false;
        }

        String question = "The server \"" + host + ":" + port + "\" sent ";
        question += "the following certificate:\r\n";

        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        for (X509Certificate cert : chain) {
            sha1.update(cert.getEncoded());
            md5.update(cert.getEncoded());
            question += "   Subject:    " + cert.getSubjectDN() + "\n";
            question += "   Issuer:     " + cert.getIssuerDN() + "\n";
            question += "   Type:       " + cert.getType() + "\n";
            question += "   SHA1:       " + toHexString(sha1.digest()) + "\n";
            question += "   MD5:        " + toHexString(md5.digest()) + "\n";
            question += "   Valid from: " + dateToIsoString(cert.getNotBefore()) + "\n";
            question += "   Valid to:   " + dateToIsoString(cert.getNotAfter()) + "\n";
            question += "\n";
        }

        question += "Do you want to trust this server?";
        System.out.println(question);

        return false;
    }

    private static boolean testSSLConnection(SSLContext context, String host, int port) throws Exception {
        if (context == null) {
            context = SSLContext.getDefault();
        }

        SSLSocketFactory factory = context.getSocketFactory();
        SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
        socket.setSoTimeout(10000);

        try {
            socket.startHandshake();

            Logger.info("SSLUtilities.testSSLConnection", "Handshake started ... Check host names ...");
            hostnameVerifier.verify(host, socket);

            Logger.info("SSLUtilities.testSSLConnection", "SSL certificate for \"" + host + ":" + port + "\" is trusted");
            return true;
        }
        catch (SSLException ex) {
            Logger.warn("SSLUtilities.testSSLConnection", "SSLException: SSL certificate for \"" + host + ":" + port + "\" is not trusted (" + ex.getMessage() + ")");
        }
        catch (IOException ioe) {
            Logger.warn("SSLUtilities.testSSLConnection", "IOException: SSL certificate for \"" + host + ":" + port + "\" is not trusted (" + ioe.getMessage() + ")");
        }
        finally {
            socket.close();
        }

        return false;
    }

    // --------------------------------------------------------------------------------
    // helpers
    private static final char[] HEXDIGITS = "0123456789abcdef".toCharArray();

    private static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (int i = 0; i < bytes.length; ++i) {
            if (i > 0) {
                sb.append(':');
            }
            int b = (bytes[i] & 0xff);
            sb.append(HEXDIGITS[b >> 4]);
            sb.append(HEXDIGITS[b & 15]);
        }
        return sb.toString();
    }

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss Z");

    private static String dateToIsoString(Date date) {
        return dateFormat.format(date);
    }

    private static class CustomTrustManager implements X509TrustManager {
        private final X509TrustManager tm;
        private X509Certificate[] chain;
        private final String _host;

        CustomTrustManager(X509TrustManager tm, String host) {
            this.tm = tm;
            _host = host;
        }

        public X509Certificate[] getAcceptedIssuers() {
            return null;
//            throw new UnsupportedOperationException();
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//            throw new UnsupportedOperationException();
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            Logger.debug("SSLUtilities.checkServerTrusted", "Try to check if a server is trusted with authType - " + authType);
            this.chain = chain;
            if (!isHostTrusted(_host) && !isHostInCertChecking(_host)) {
                LogMsg.debug("Auto ceertification checking", "host=" + _host 
                        + ", Checked= " + isHostCertChecked(_host)
                        + ", trusted=" + isHostTrusted(_host)
                        + ", In_checking=" + isHostInCertChecking(_host)
                        + ", authType=" + authType);
                tm.checkServerTrusted(chain, authType);
            }
        }
    }


    public static class TrustAllTM implements javax.net.ssl.TrustManager,
            javax.net.ssl.X509TrustManager {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public boolean isServerTrusted(
                java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public boolean isClientTrusted(
                java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public void checkServerTrusted(
                java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }

        public void checkClientTrusted(
                java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: SSLUtilities <host> [<port>]");
            System.exit(-1);
        }

        Logger.setComponent("test");
        Logger.setLogLevel("", "test", "trace");
/*
        try {
            System.setProperty( "http.proxyHost", "wwwgate0-ch.mot.com" );
            System.setProperty( "http.proxyPort", "1080" );

            URL url = new URL("http://" + args[0]);
            java.net.HttpURLConnection uc = (java.net.HttpURLConnection)url.openConnection();
            uc.connect();
        
            String line;
            StringBuilder sb = new StringBuilder();
            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(uc.getInputStream()));
            while ((line = in.readLine()) != null){
                sb.append(line + "\n");
            }
            System.out.println(sb.toString());
        }
        catch(Exception e){
            e.printStackTrace();
        }

        try {
            System.setProperty( "https.proxyHost", "wwwgate0-ch.mot.com" );
            System.setProperty( "https.proxyPort", "1080" );

            URL url = new URL("https://" + args[0]);
            javax.net.ssl.HttpsURLConnection uc = (javax.net.ssl.HttpsURLConnection)url.openConnection();
            uc.connect();

            String line;
            StringBuilder sb = new StringBuilder();
            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(uc.getInputStream()));
            while ((line = in.readLine()) != null){
                sb.append(line + "\n");
            }
            System.out.println(sb.toString());
        }
        catch(Exception e){
            e.printStackTrace();
        }
*/
        Properties props = new Properties();
        props.put("server", args[0]);
        props.put("ssl.enable", "true");
        if (args.length == 1) {
            System.setProperty("https.proxyHost", "wwwgate0-ch.mot.com");
            System.setProperty("https.proxyPort", "1080");

//            checkServerCertification("wwwgate0-ch.mot.com");
/*
            System.out.println("################Not using proxy ...");
            try {
                checkServerCertification(new Service(props, null));
            }
            catch(Exception e) {
                e.printStackTrace();
            }

            System.out.println("#################Using proxy wwwgate0-ch ...");
            Properties sysProperties = System.getProperties();
            
            sysProperties.put( "http.proxyHost", "wwwgate0-ch.mot.com" );
            sysProperties.put( "http.proxyPort", "1080" );
            // sysProperties.put( "https.proxyHost", proxyHost );
            // sysProperties.put( "https.proxyPort", proxyPort );
            try {
                checkServerCertification(new Service(props, null));
            }
            catch(Exception e) {
                e.printStackTrace();
            }

            System.out.println("#################Using proxy wwwgate0-ch again ...");
            System.setProperty( "http.proxyHost", "wwwgate0-ch.mot.com" );
            System.setProperty( "http.proxyPort", "1080" );
            // sysProperties.put( "https.proxyHost", proxyHost );
            // sysProperties.put( "https.proxyPort", proxyPort );
            try {
                checkServerCertification(new Service(props, null));
            }
            catch(Exception e) {
                e.printStackTrace();
            }
*/
        }
        else {
            initSSL(args[0], Integer.parseInt(args[1]));
        }
    }
}

