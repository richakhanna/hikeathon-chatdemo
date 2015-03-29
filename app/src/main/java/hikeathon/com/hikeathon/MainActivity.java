package hikeathon.com.hikeathon;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;

import java.io.IOException;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private EditText etString;
    private Button buttonSend;
    private static EditText etReceive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etString = (EditText) findViewById(R.id.et_enter_string);
        buttonSend = (Button) findViewById(R.id.button_send);
        etReceive = (EditText) findViewById(R.id.et_receive);

//        Log.d(TAG, "Creating registerTask to register a user");
//        RegisterTask registerTask = new RegisterTask(this, "RegisterTask");
//        Log.d(TAG, "Going to call registerTask.execute()");
//        registerTask.execute();

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Creating chatTask to start chatting with another user");
                ChatTask chatTask = new ChatTask(MainActivity.this, "ChatTask");
                Log.d(TAG, "Going to call chatTask.execute()");
                chatTask.execute();
            }
        });


    }

    private class RegisterTask extends AsyncTask<Void, Void, Void> {

        private final String LOG = RegisterTask.class
                .getSimpleName();
        private Context context;
        private String newThreadName;

        public RegisterTask(Context context, String newThreadName) {
            this.context = context.getApplicationContext();
            this.newThreadName = newThreadName;
            Log.d(LOG, "Created RegisterTask");
        }

        @Override
        protected Void doInBackground(Void... params) {

            String oldThreadName = Thread.currentThread().getName();
            Log.d(LOG, "setting thread-name from: " + oldThreadName + " to: "
                    + newThreadName);
            Thread.currentThread().setName(newThreadName);

            Log.d(LOG, "doInBackground started");
            if (NetworkUtils.isOnline(context)) {

                Log.d(LOG, "Registering a new user from the background");
                // to register a new user
                registerUser();
                Log.d(LOG, "Registering ended");
            } else {
                Log.d(LOG, "Network not available");
                Toast.makeText(MainActivity.this, "Please check your network connection.", Toast.LENGTH_LONG).show();
            }
            Log.d(LOG, "doInBackground completed");

            if (oldThreadName != null) {
                Log.d(LOG, "resetting thread-name from: " + newThreadName + " to: "
                        + oldThreadName);
                Thread.currentThread().setName(oldThreadName);
            }

            return null;

        }
    }

    private class ChatTask extends AsyncTask<Void, Void, Void> {

        private final String LOG = ChatTask.class
                .getSimpleName();
        private Context context;
        private String newThreadName;

        public ChatTask(Context context, String newThreadName) {
            this.context = context.getApplicationContext();
            this.newThreadName = newThreadName;
            Log.d(LOG, "Created ChatTask");
        }

        @Override
        protected Void doInBackground(Void... params) {

            String oldThreadName = Thread.currentThread().getName();
            Log.d(LOG, "setting thread-name from: " + oldThreadName + " to: "
                    + newThreadName);
            Thread.currentThread().setName(newThreadName);

            Log.d(LOG, "doInBackground started");
            if (NetworkUtils.isOnline(context)) {

                Log.d(LOG, "Start a chat with another user from the background");
                //to start a chat with a user
                startChat();
                Log.d(LOG, "Chat ended");

            } else {
                Log.d(LOG, "Network not available");
                Toast.makeText(MainActivity.this, "Please check your network connection.", Toast.LENGTH_LONG).show();
            }
            Log.d(LOG, "doInBackground completed");

            if (oldThreadName != null) {
                Log.d(LOG, "resetting thread-name from: " + newThreadName + " to: "
                        + oldThreadName);
                Thread.currentThread().setName(oldThreadName);
            }

            return null;

        }
    }


    public void startChat() {
        Log.d(TAG, "startchat ...");
        try {
            XMPPTCPConnectionConfiguration.Builder conf = XMPPTCPConnectionConfiguration
                    .builder().allowEmptyOrNullUsernames();
            conf.setHost("hackathon.hike.in");
            conf.setPort(8282);
            conf.setServiceName("hackathon.hike.in");
            conf.setSecurityMode(SecurityMode.disabled);

            XMPPTCPConnection connection = new XMPPTCPConnection(
                    conf.build());

            connection.connect();
            Log.d(TAG, "connected");

            Log.d("startChat", "Trying to login...");
            connection.login("richakhanna", "12345");
            Log.d(TAG, "logged in");

            Chat chat = ChatManager.getInstanceFor(connection).createChat(
                    "nadeem@hackathon.hike.in",
                    new ChatMessageListener() {

                        @Override
                        public void processMessage(Chat arg0, Message arg1) {
                            Log.d(TAG, "Received message: " + arg1);
                            //etReceive.setText(arg1.toString());
                            //Toast.makeText(MainActivity.this, "message received: " + arg1.toString(), Toast.LENGTH_LONG).show();
                            try {
                                Log.d(TAG, "Sending message: " + ":)");
                                arg0.sendMessage(":)");
                            } catch (NotConnectedException e) {
                                Log.e(TAG, e.getMessage(), e);
                            }
                        }

                    });


            final ChatMessageListener chatMessageListener = new ChatMessageListener() {

                @Override
                public void processMessage(Chat arg0, final Message arg1) {
                    Log.d(TAG,"received message from: "
                            + arg0.getParticipant() + "  message" + arg1);

                    new Thread(new Runnable(){
                        @Override
                        public void run () {
                            etReceive.post(new Runnable() {
                                @Override
                                public void run () {
                                    etReceive.setText(arg1.toString());
                                    Toast.makeText(MainActivity.this, "message received: " + arg1.toString(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }).start();


                }
            };

            ChatManagerListener chatManagerListener = new ChatManagerListener() {
                @Override
                public void chatCreated(Chat chat, boolean createdLocally) {
                    chat.addMessageListener(chatMessageListener);
                }
            };

            //You need to register a ChatListener to be notified of new chats
            ChatManager.getInstanceFor(connection).addChatListener(
                    chatManagerListener);

            Log.d("startChat", "sending message...");
            chat.sendMessage("Hey hello!");
            Log.d("startChat", "message sent...");

            try {
                Thread.sleep(2000000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Log.d(TAG, "disconnecting ...");
            connection.disconnect();

        } catch (XMPPException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (SmackException s) {
            Log.e(TAG, s.getMessage(), s);
        } catch (IOException i) {
            Log.e(TAG, i.getMessage(), i);
        }
    }


    public void registerUser() {
        Log.d(TAG, "registerUser ...");
        try {
            XMPPTCPConnectionConfiguration.Builder conf = XMPPTCPConnectionConfiguration
                    .builder().allowEmptyOrNullUsernames();
            conf.setHost("hackathon.hike.in");
            conf.setPort(8282);
            conf.setServiceName("hackathon.hike.in");
            conf.setSecurityMode(SecurityMode.disabled);

            XMPPTCPConnection connection = new XMPPTCPConnection(
                    conf.build());

            connection.connect();
            Log.d(TAG, "connected");

            AccountManager am = AccountManager.getInstance(connection);
            //am.sensitiveOperationOverInsecureConnection(true);

            Log.d(TAG,
                    "is account creation allowed: "
                            + am.supportsAccountCreation());

            am.createAccount("richakhanna", "12345");
            am.createAccount("rahulaswani", "12345");
            am.createAccount("nadeem", "12345");
            am.createAccount("devansh", "12345");
            am.createAccount("kamal", "12345");

            Log.d(TAG, "disconnecting ...");

            connection.disconnect();

        } catch (XMPPException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (SmackException s) {
            Log.e(TAG, s.getMessage(), s);
        } catch (IOException i) {
            Log.e(TAG, i.getMessage(), i);
        }
    }
}
