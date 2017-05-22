package com.example.ricardo.myxmppchat;

import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ExceptionCallback;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.bosh.BOSHConfiguration;
import org.jivesoftware.smack.bosh.XMPPBOSHConnection;
import org.jxmpp.stringprep.XmppStringprepException;

/**
 * Created by ricardo on 22/5/17.
 */

public class BoshActivity extends AppCompatActivity implements ConnectionListener{

    long TIMEOUT = 30000;
    XMPPBOSHConnection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread(new Runnable() {
            @Override
            public void run() {
                setup();
            }
        }).start();


    }

    void setup(){
        try {

            SASLAuthentication.unregisterSASLMechanism("org.jivesoftware.smack.sasl.core.SASLXOauth2Mechanism");
            SASLAuthentication.unregisterSASLMechanism("org.jivesoftware.smack.sasl.provided.SASLExternalMechanism");
            SASLAuthentication.unregisterSASLMechanism("org.jivesoftware.smack.sasl.core.SCRAMSHA1Mechanism");
            SASLAuthentication.unregisterSASLMechanism("org.jivesoftware.smack.sasl.provided.SASLDigestMD5Mechanism");

            BOSHConfiguration conf = BOSHConfiguration.builder()
                    .setUsernameAndPassword("maria", "maria0000")
                    .setFile("/http-bind/")
                    .setHost("92.54.22.122")
                    .setXmppDomain("sun.com")
                    .setPort(80)
                    .setDebuggerEnabled(true)
                    .setResource("Smack_" + getDeviceId())
                    //.setSecurityMode(SecurityMode.required)
                    .setUseHttps(false)
//                    .setSendPresence(true)
                    .build();


            connection = new XMPPBOSHConnection(conf);
            connection.setPacketReplyTimeout(10000);
            connection.addConnectionListener(BoshActivity.this);

            connection.connect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connected(XMPPConnection xmppConnection) {
        printLog("connected");
        try {
            SASLAuthentication.blacklistSASLMechanism("SCRAM-SHA-1");
            SASLAuthentication.blacklistSASLMechanism("DIGEST-MD5");
            SASLAuthentication.unBlacklistSASLMechanism("PLAIN");
            connection.login();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        printLog("authenticated");
    }

    @Override
    public void connectionClosed() {
        printLog("connectionClosed");
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        printLog("connectionClosedOnError");
    }

    @Override
    public void reconnectionSuccessful() {
        printLog("reconnectionSuccessful");
    }

    @Override
    public void reconnectingIn(int seconds) {
        printLog("reconnectingIn");
    }

    @Override
    public void reconnectionFailed(Exception e) {
        printLog("reconnectionFailed");
    }

    void printLog(String mensaje){
        Log.d("SmackR",mensaje);
    }

    public String getDeviceId() {
        return Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}
