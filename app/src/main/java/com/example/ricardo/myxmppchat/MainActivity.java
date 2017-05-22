package com.example.ricardo.myxmppchat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ExceptionCallback;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.net.InetAddress;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements IncomingChatMessageListener {


    long TIMEOUT = 30000;
    @BindView(R.id.et_mensaje) EditText et_mensaje;
    @BindView(R.id.bt_on_off) Button bt_on_off;


    AbstractXMPPConnection connection;

    ChatManager chatManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        new Thread(new Runnable() {
            @Override
            public void run() {

                HostnameVerifier verifier = new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return false;
                    }
                };

                // Create a connection to the jabber.org server on a specific port.
                XMPPTCPConnectionConfiguration config = null;
                try {
                    config = XMPPTCPConnectionConfiguration.builder()
//                            .setUsernameAndPassword("maria@sun.com", "maria0000")
                            .setXmppDomain("sun.com")
//                            .setHost("parlamicef.spamina.com")
//                            .setPort(443)
                            .setHostAddress(InetAddress.getByName("10.28.30.16"))
                            .setHostnameVerifier(verifier)
                            .setPort(5222)
//                            .setConnectTimeout((int)TIMEOUT)
                            .setDebuggerEnabled(true)
                            .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
//                            .setCompressionEnabled(false)
                            .build();



                    connection = new XMPPTCPConnection(config);
                    connection.setPacketReplyTimeout(TIMEOUT);
                    connection.setReplyTimeout(TIMEOUT);
                    connection.addAsyncStanzaListener(new StanzaListener() {
                        @Override
                        public void processStanza(Stanza packet) throws SmackException.NotConnectedException, InterruptedException {
                            imprimirLog(packet.toString());
                        }
                    }, new StanzaFilter() {
                        @Override
                        public boolean accept(Stanza stanza) {
                            return true;
                        }
                    });
                    connection.addConnectionListener(new ConnectionListener() {
                        @Override
                        public void connected(XMPPConnection xmppConnection) {

                            imprimirLog("connected");
                            try{
                                connection.login("antonio", "antonio0000");
                                showToast("Conectado");
                            }
                            catch (Exception e){
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void authenticated(XMPPConnection xmppConnection, boolean resumed) {
                            imprimirLog("authenticated");

                            sendPresence(Presence.Type.available);
                            chatManager = ChatManager.getInstanceFor(connection);
                            chatManager.addIncomingListener(MainActivity.this);
                        }

                        @Override
                        public void connectionClosed() {
                            imprimirLog("connectionClosed");
                        }

                        @Override
                        public void connectionClosedOnError(Exception e) {
                            imprimirLog("connectionClosedOnError");
                        }

                        @Override
                        public void reconnectionSuccessful() {
                            imprimirLog("reconnectionSuccessful");
                        }

                        @Override
                        public void reconnectingIn(int seconds) {
                            imprimirLog("reconnectingIn");
                        }

                        @Override
                        public void reconnectionFailed(Exception e) {
                            imprimirLog("reconnectionFailed");
                        }
                    });

                    connection.connect();


                }
                catch (Exception e) {
                    e.printStackTrace();
                }



            }
        }).start();



    }

    void showToast(final String message){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this,message,Toast.LENGTH_LONG).show();
            }
        });

    }

    void imprimirLog(String mensaje){
        Log.d("SmackR",mensaje);
    }

    void sendPresence(Presence.Type type){
        // Create a new presence. Pass in false to indicate we're unavailable._
//        Presence presence = new Presence(Presence.Type.available);
        Presence presence = new Presence(type);
        presence.setStatus("hello!");
        try {
            connection.sendStanza(presence);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @OnClick(R.id.bt_on_off) public void switchOnOff(View view) {

        String text = bt_on_off.getText().toString();

        if(text.equals("off")){
            bt_on_off.setText("off");
            sendPresence(Presence.Type.unavailable);
        }
        else{
            bt_on_off.setText("on");
            sendPresence(Presence.Type.available);
        }

    }

    @OnClick(R.id.bt_enviar) public void enviar(View view) {

        EntityBareJid jid = null;
        try {
            jid = JidCreate.entityBareFrom("maria@sun.com");
            Chat chat = chatManager.chatWith(jid);
            chat.send(et_mensaje.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
        imprimirLog(message.getBody().toString());
    }
}
