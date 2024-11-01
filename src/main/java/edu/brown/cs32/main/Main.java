package edu.brown.cs32.main;

import edu.brown.cs32.server.SlitherServer;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;

/**
 * Main class for default use
 */
public class Main {
    private static final String KEYSTORE_PASSWORD = "admin12345";
    private static final String KEYSTORE_PATH = "/home/ubuntu/slither/slither_server/key/keystore.jks";
    
    /**
     * Default main method for server
     * @param args : any arguments given to default main method
     */
    public static void main(String[] args) {
        System.out.println("Starting the server setup...");
        try {
            // Step 1: Load the keystore
            System.out.println("Loading keystore from path: " + KEYSTORE_PATH);
            KeyStore keyStore = KeyStore.getInstance("JKS");
            try (FileInputStream keyStoreFile = new FileInputStream(KEYSTORE_PATH)) {
                keyStore.load(keyStoreFile, KEYSTORE_PASSWORD.toCharArray());
                System.out.println("Keystore loaded successfully.");
            } catch (FileNotFoundException e) {
                System.err.println("Keystore file not found at path: " + KEYSTORE_PATH);
                return;
            } catch (IOException e) {
                System.err.println("Error reading the keystore file: " + e.getMessage());
                return;
            } catch (GeneralSecurityException e) {
                System.err.println("Error initializing the keystore: " + e.getMessage());
                return;
            }

            // Step 2: Create and initialize KeyManagerFactory
            System.out.println("Initializing KeyManagerFactory...");
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            try {
                kmf.init(keyStore, KEYSTORE_PASSWORD.toCharArray());
                System.out.println("KeyManagerFactory initialized successfully.");
            } catch (GeneralSecurityException e) {
                System.err.println("Error initializing KeyManagerFactory: " + e.getMessage());
                return;
            }

            // Step 3: Initialize TrustManagerFactory
            System.out.println("Initializing TrustManagerFactory...");
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            try {
                tmf.init(keyStore);
                System.out.println("TrustManagerFactory initialized successfully.");
            } catch (GeneralSecurityException e) {
                System.err.println("Error initializing TrustManagerFactory: " + e.getMessage());
                return;
            }

            // Step 4: Create SSLContext and initialize it with KeyManagers and TrustManagers
            System.out.println("Setting up SSL context...");
            SSLContext sslContext = SSLContext.getInstance("TLS");
            try {
                sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
                System.out.println("SSL context setup complete.");
            } catch (GeneralSecurityException e) {
                System.err.println("Error initializing SSLContext: " + e.getMessage());
                return;
            }

            // Step 5: Create SSL WebSocket server
            System.out.println("Creating SSL WebSocket server...");
            SlitherServer server = new SlitherServer(9000);
            server.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(sslContext));
            System.out.println("WebSocket factory set with SSL context.");

            // Step 6: Start the server
            System.out.println("Starting the server on port: " + server.getPort());
            server.start();
            System.out.println("Server started successfully on port: " + server.getPort());

        } catch (Exception e) {
            System.err.println("An unexpected error occurred during server setup: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
