package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 *
 * @author stevko
 */
public class GroupMessengerActivity extends Activity {

    static final String[] ports={"11108", "11112", "11116", "11120", "11124"};
    static final int SERVER_PORT = 10000;
    static String myPort;

    static final int receiveMessage=0;
    static final int deliverMessage=1;
    static final int checkIfActive=2;

    Socket clientSocketArray[] = new Socket[5];
    static Map<String,Integer> isPortActive = new HashMap<String, Integer>();

    protected final Uri uri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");
    protected ContentValues contentValues = new ContentValues();

    protected static final String KEY = "key";
    protected static final String VALUE = "value";

    protected static int processSeqNo=0;

    static int isFailureDetected=0;
    static String failedNode="none";
    static int clientMessageSent=0;



    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */

        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        Log.e("My code", "Port String : " + portStr);
        //Log.e("My code", "My port : " + myPort);

        for(int i=0;i<ports.length;i++){
            isPortActive.put(ports[i],1);
        }

        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT, 5);
            Log.e("My code", "Creating Server Thread");
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);


        }catch(UnknownHostException e) {
            Log.e("My code", "Can't create a server socket due to exception : " + e);
            return;

        } catch(IOException e) {
            Log.e("My code", "Can't create a server socket due to exception : " + e);
            return;
        }

        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        final EditText editText = (EditText) findViewById(R.id.editText1);

        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {

                    String msg = editText.getText().toString() + "\n";
                    editText.setText(""); // This is one way to reset the input box.
                    new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg,myPort);
                    return true;
                }
                return false;
            }
        });

        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));

        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String msg = editText.getText().toString() + "\n";
                editText.setText("");
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "GroupMessenger Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://edu.buffalo.cse.cse486586.groupmessenger2/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        for(int i=0;i<5;i++){
            try {
                clientSocketArray[i].close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "GroupMessenger Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://edu.buffalo.cse.cse486586.groupmessenger2/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {

            String msgToSend = msgs[0];
            String myPortNo = msgs[1];

            int[] recSeq = new int[ports.length];

            MessagePOJO messageOut = new MessagePOJO();
            messageOut.isDeliverable = 0;
            messageOut.processId = Integer.parseInt(myPortNo);
            messageOut.originSeqNo = processSeqNo;
            messageOut.sequenceNo = -1;
            messageOut.messageText = msgToSend;

            for(int i=0;i<ports.length;i++){
                clientSocketArray[i] = new Socket();
                recSeq[i]=-1;
                try {
                    Log.e("My code - Client", "Attempting to create client socket to port :" + ports[i]);
                    if(isPortActive.get(ports[i])==0)
                        continue;
                    clientSocketArray[i].connect(new InetSocketAddress(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(ports[i])));
                    clientSocketArray[i].setSoTimeout(2000);
                    Log.e("My code - Client", "Attempting to send object with message : " + messageOut.messageText + " from client socket with port no : " + myPortNo);

                    OutputStream os = clientSocketArray[i].getOutputStream();
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));

                    bw.write(String.valueOf(receiveMessage));
                    bw.newLine();
                    bw.flush();

                    Log.e("My code - Client", "The message contains message : " + messageOut.messageText + ", process id : " + messageOut.processId + ", origin seq no : " + messageOut.originSeqNo);

                    bw.write(messageOut.processId + "," + messageOut.originSeqNo + "," + messageOut.isDeliverable + "," + messageOut.messageText);
                    //bw.newLine();
                    bw.flush();

                    bw.write(failedNode);
                    bw.newLine();
                    bw.flush();


                    Log.e("My code - Client", "Message sent");

                    InputStream is = clientSocketArray[i].getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));

                    recSeq[i]=Integer.parseInt(br.readLine());
                    Log.e("My code - Client", "Received data : " +recSeq[i]);

                    bw.write("end");
                    bw.newLine();
                    bw.flush();

                }  catch (NumberFormatException e){
                    handleFailure(i);
                    Log.e("My code - Client", "NumberFormatException on port " +myPortNo + " due to : " + e);
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    handleFailure(i);
                    Log.e("My code - Client", "SocketTimeoutException on port " + myPortNo + " due to : " + e);
                    e.printStackTrace();
                }catch (SocketTimeoutException e){
                    handleFailure(i);
                    Log.e("My code - Client", "SocketTimeoutException on port " +myPortNo + " due to : " + e);
                    e.printStackTrace();
                } catch (SocketException e){
                    handleFailure(i);
                    Log.e("My code - Client", "SocketException on port " +myPortNo + " due to : " + e);
                    e.printStackTrace();
                } catch (EOFException e) {
                    handleFailure(i);
                    Log.e("My code - Client", "EOFException on port " +myPortNo + " due to : " + e);
                    e.printStackTrace();
                }catch (IOException e){
                    Log.e("My code - Client", "IOException on port " +myPortNo + " due to : " + e);
                    e.printStackTrace();
                }catch (Exception e){
                    Log.e("My code - Client", "Exception on port " +myPortNo + " due to : " + e);
                    e.printStackTrace();
                }finally {
                    try {
                        clientSocketArray[i].close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

            int maxRecSeq=recSeq[0];
            for(int i=0;i<ports.length;i++){
                if(recSeq[i]>maxRecSeq)
                    maxRecSeq=recSeq[i];
            }

            Log.e("My code - Client", "Maximum request sequence number : " + maxRecSeq);

            messageOut.isDeliverable = 1;
            messageOut.sequenceNo = maxRecSeq;


            for(int i=0;i<ports.length;i++){
                clientSocketArray[i] = new Socket();

                try {
                    Log.e("My code - Client", "Attempting to create client socket(deliver) to port :" + ports[i]);
                    if(isPortActive.get(ports[i])==0)
                        continue;
                    clientSocketArray[i].connect(new InetSocketAddress(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(ports[i])));
                    clientSocketArray[i].setSoTimeout(2000);

                    OutputStream os = clientSocketArray[i].getOutputStream();
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));

                    bw.write(String.valueOf(deliverMessage));
                    bw.newLine();
                    bw.flush();

                    Log.e("My code - Client", "The message contains message : " + messageOut.messageText + ", process id : " + messageOut.processId + ", origin seq no : " + messageOut.originSeqNo);

                    bw.write(messageOut.sequenceNo + "," + messageOut.processId + "," + messageOut.originSeqNo + "," + messageOut.isDeliverable + "," + messageOut.messageText);
                    //bw.newLine();
                    bw.flush();

                    Log.e("My code - Client", "Deliverable Message sent");

                    bw.write(failedNode);
                    bw.newLine();
                    bw.flush();

                    InputStream is = clientSocketArray[i].getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));

                    String endMessage=br.readLine();
                    if(!endMessage.equals("end")){
                        throw new SocketException();
                    }

                } catch (NumberFormatException e){
                    handleFailure(i);
                    Log.e("My code - Client", "SocketTimeoutException on port " +myPortNo + " due to : " + e);
                    e.printStackTrace();
                }catch (NullPointerException e) {
                    handleFailure(i);
                    Log.e("My code - Client", "SocketTimeoutException on port " + myPortNo + " due to : " + e);
                    e.printStackTrace();
                } catch (SocketTimeoutException e){
                    handleFailure(i);
                    Log.e("My code - Client", "SocketTimeoutException on port " +myPortNo + " due to : " + e);
                    e.printStackTrace();
                } catch (SocketException e){
                    handleFailure(i);
                    Log.e("My code - Client", "SocketException on port " +myPortNo + " due to : " + e);
                    e.printStackTrace();
                } catch (EOFException e) {
                    handleFailure(i);
                    Log.e("My code - Client", "EOFException on port " +myPortNo + " due to : " + e);
                    e.printStackTrace();
                }catch (IOException e){
                    Log.e("My code - Client", "IOException on port " +myPortNo + " due to : " + e);
                    e.printStackTrace();
                }catch (Exception e){
                    Log.e("My code - Client", "Exception on port " +myPortNo + " due to : " + e);
                    e.printStackTrace();
                }finally {
                    try {
                        clientSocketArray[i].close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
            clientMessageSent++;
            return null;
        }

        private void handleFailure(int i) {
            if(isFailureDetected==0){
                isFailureDetected=1;
                failedNode=String.valueOf(ports[i]);
                isPortActive.put(ports[i],0);
                Log.e("My code - Client", "Failure detected at "+ports[i]+ ". isActive is set to 0");
            }
        }

    }


    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            Socket srvSocket = null;

            PriorityQueue<MessagePOJO> messageQueue = new PriorityQueue<MessagePOJO>(1, new MessageComparator());

            int messageCounter = 0;
            String clientProcessId=null;

            while (true) {
                Log.e("My code - Server", "Start of server thread");

                try {
                    MessagePOJO messageIn = new MessagePOJO();
                    srvSocket = serverSocket.accept();
                    srvSocket.setSoTimeout(2500);

                    clientProcessId=null;
                    String failureDetectedAt="none";
                    InputStream iStream = srvSocket.getInputStream();
                    BufferedReader bReader = new BufferedReader(new InputStreamReader(iStream));
                    OutputStream oStream = srvSocket.getOutputStream();
                    BufferedWriter bWriter = new BufferedWriter(new OutputStreamWriter(oStream));

                    int messageType=Integer.parseInt(bReader.readLine());
                    if(messageType!=checkIfActive){
                        if(messageType==receiveMessage){
                            String receiveData=bReader.readLine();
                            Log.e("My code - Server", "Receive Data : "+receiveData);

                            String[] messageLine = receiveData.split(",");

                            messageIn.sequenceNo = ++processSeqNo;
                            clientProcessId=messageLine[0];
                            messageIn.processId = Integer.parseInt(clientProcessId);
                            messageIn.originSeqNo = Integer.parseInt(messageLine[1]);
                            messageIn.isDeliverable = Integer.parseInt(messageLine[2]);
                            messageIn.messageText=messageLine[3];
                            failureDetectedAt=bReader.readLine();

                            Log.e("My code - Server", "Received message : " + messageIn.messageText);
                            Log.e("My code - Server", "Message successfully received with sequence number : " + messageIn.sequenceNo + ", processId : " + messageIn.processId + ", origin sequence number : " + messageIn.originSeqNo + ", isdeliverable : " + messageIn.isDeliverable);
                            Log.e("My code - Server", "Failed node : " + failureDetectedAt);

                            Log.e("My code - Server", "Ack with sequence number : " + messageIn.sequenceNo);

                            bWriter.write(String.valueOf(messageIn.sequenceNo));
                            bWriter.newLine();
                            bWriter.flush();

                            Log.e("My code - Server", "End of ack with : "+bReader.readLine());

                        }
                        else if(messageType==deliverMessage){

                            String deliverData=bReader.readLine();
                            Log.e("My code - Server", "Deliver Data : "+deliverData);

                            String[] messageLine = deliverData.split(",");

                            messageIn.sequenceNo = Integer.parseInt(messageLine[0]);
                            clientProcessId=messageLine[1];
                            messageIn.processId=Integer.parseInt(clientProcessId);
                            messageIn.originSeqNo=Integer.parseInt(messageLine[2]);
                            messageIn.isDeliverable = Integer.parseInt(messageLine[3]);
                            messageIn.messageText=messageLine[4];
                            failureDetectedAt=bReader.readLine();

                            if (processSeqNo < messageIn.sequenceNo)
                                processSeqNo = messageIn.sequenceNo;

                            Log.e("My code - Server", "Message successfully received with sequence number : " + messageIn.sequenceNo + ", processId : " + messageIn.processId + ", origin sequence number : " + messageIn.originSeqNo + ", isdeliverable : " + messageIn.isDeliverable);
                            Log.e("My code - Server", "Failed node : " + failureDetectedAt);

                            bWriter.write("end");
                            bWriter.newLine();
                            bWriter.flush();
                        }

                        if (!messageQueue.contains(messageIn)) {
                            messageQueue.add(messageIn);
                            Log.e("My code - Server", "New message - Queue does not contain message");
                        } else {
                            messageQueue.remove(messageIn);
                            messageQueue.add(messageIn);

                            Log.e("My code - Server", "Message reinserted after updating sequence number");
                     /*       if(isFailureDetected==0){
                                while(messageQueue.peek()!=null && messageQueue.peek().processId!=Integer.parseInt(myPort) && messageQueue.peek().isDeliverable==0 && messageQueue.size()>1){
                                    Log.e("My code - Server", "Calling checkActive within message insertion with remote process id : "+messageQueue.peek().processId+" and myPort id :"+myPort);
                                    if(checkActive(messageQueue.peek().processId)==false){
                                        messageQueue.poll();
                                    }else{
                                        break;
                                    }
                                }
                            }*/
                        }

                        if(isFailureDetected==0 && clientMessageSent==5){
                            while(messageQueue.peek()!=null && messageQueue.peek().processId!=Integer.parseInt(myPort) && messageQueue.peek().isDeliverable==0 && messageQueue.size()>1){
                                Log.e("My code - Server", "Calling checkActive with remote process id : "+messageQueue.peek().processId+" and myPort id :"+myPort);
                                if(checkActive(messageQueue.peek().processId)==false){
                                    messageQueue.poll();
                                }else{
                                    break;
                                }
                            }
                        }

                      if(isFailureDetected==0 && !failureDetectedAt.equals("none")){
                            if( (failureDetectedAt.equals("11108") || failureDetectedAt.equals("11112") || failureDetectedAt.equals("11116") || failureDetectedAt.equals("11120") || failureDetectedAt.equals("11124")) && !failureDetectedAt.equals(myPort)){
                                if(checkActive(Integer.parseInt(clientProcessId))==true && checkActive(Integer.parseInt(failureDetectedAt))==false){
                                    isFailureDetected=1;
                                    failedNode=failureDetectedAt;
                                    isPortActive.put(failureDetectedAt,0);
                                    Log.e("My code - Server", "Failure detected at " + failedNode + ". isActive is set to 0");
                                }

                            }
                        }

                        if(messageQueue.peek()!=null){
                            Log.e("My code - Server", "Head of queue is : " + messageQueue.peek().sequenceNo+","+messageQueue.peek().processId+","+messageQueue.peek().isDeliverable+","+messageQueue.peek().messageText);
                        }


                        if(isFailureDetected==1){
                            while((messageQueue.peek() != null) && (isPortActive.get(String.valueOf(messageQueue.peek().processId))==0)){
                                messageQueue.poll();
                                Log.e("My code - Server", "Polling messageQueue");
                            }
                        }

                        while ((messageQueue.peek() != null) && (messageQueue.peek().isDeliverable == 1)) {
                            MessagePOJO deliveryMessage = messageQueue.poll();
                            String receivedText = deliveryMessage.messageText;
                            Log.e("My code - Server", "Message at the head of the queue is : " + deliveryMessage.sequenceNo+" , "+deliveryMessage.processId);
                            if(messageQueue.peek()!=null)
                                Log.e("My code - Server", "Next message is : " + messageQueue.peek().sequenceNo+","+messageQueue.peek().processId+","+messageQueue.peek().isDeliverable+","+messageQueue.peek().messageText);

                            if(isFailureDetected==1){
                                while((messageQueue.peek() != null) && (isPortActive.get(String.valueOf(messageQueue.peek().processId))==0)){
                                    messageQueue.poll();
                                }
                            }
                            Log.e("My code - Server", "Message ready to be delivered with text : " + receivedText);
                            if (receivedText != null) {
                                Log.e("My code - Server", "Calling onProgressUpdate " + receivedText);
                                publishProgress(receivedText);
                                contentValues.put(KEY, Integer.toString(messageCounter));
                                contentValues.put(VALUE, receivedText);
                                getContentResolver().insert(uri, contentValues);
                                messageCounter++;
                            }
                            Log.e("My code - Server", "Message inserted with counter value " + (messageCounter - 1));

                        }
                    }
                    else if(messageType==checkIfActive){
                        Log.e("My code - Server", "CheckActive at : " + myPort);

                        bWriter.write("true");
                        bWriter.newLine();
                        bWriter.flush();

                        bWriter.write(String.valueOf(processSeqNo));
                        bWriter.newLine();
                        bWriter.flush();

                        bReader.readLine();
                        //continue;
                    }


                }catch (NullPointerException e) {
                    handleFailureOnServer(clientProcessId);
                    Log.e("My code - Server", "Failed to open Server Port due to : " + e);
                    e.printStackTrace();
                }catch (ArrayIndexOutOfBoundsException e) {
                    handleFailureOnServer(clientProcessId);
                    Log.e("My code - Server", "Failed to open Server Port due to : " + e);
                    e.printStackTrace();
                }catch (SocketTimeoutException e){
                    handleFailureOnServer(clientProcessId);
                    Log.e("My code - Server", "Failed to open Server Port due to : "+e);
                    e.printStackTrace();
                } catch (SocketException e){
                    handleFailureOnServer(clientProcessId);
                    Log.e("My code - Server", "Failed to open Server Port due to : "+e);
                    e.printStackTrace();
                } catch (EOFException e) {
                    handleFailureOnServer(clientProcessId);
                    Log.e("My code - Server", "Failed to open Server Port due to : "+e);
                    e.printStackTrace();
                }catch (IOException e){
                    Log.e("My code - Server", "Failed to open Server Port due to : " + e);
                    e.printStackTrace();
                }catch (Exception e){
                    Log.e("My code - Server", "Failed to open Server Port due to : "+e);
                    e.printStackTrace();
                    break;
                }finally {
                    try {
                        srvSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            Log.e("My code - Server", "After the while block in Server thread");
            return null;
        }


        private void handleFailureOnServer(String clientProcessId) {
            if(clientProcessId==null || clientProcessId.length()==0)
                return;
            if(clientProcessId.equals("11108") || clientProcessId.equals("11112") || clientProcessId.equals("11116") || clientProcessId.equals("11120") || clientProcessId.equals( "11124")){
                if(isFailureDetected==0){
                    isFailureDetected=1;
                    isPortActive.put(clientProcessId,0);
                    failedNode=clientProcessId;
                    Log.e("My code - Server", "Failure detected at " + clientProcessId + ". isActive is set to 0");
                    Log.e("My code - Server", "Values of IsPortActive");
                    for(int i=0;i<5;i++){
                        Log.e("My code - Server", "IsPortActive for "+ports[i]+" = "+isPortActive.get(ports[i]));
                    }

                }
            }

        }
        private boolean checkActive(int processId) {
            Socket isActiveSocket = new Socket();
            boolean returnVal=false;

            Log.e("My code - Server", "CheckActive for : " + processId);

            if(processId==Integer.parseInt(myPort))
                return true;


            try {
                isActiveSocket.connect(new InetSocketAddress(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), processId));
                isActiveSocket.setSoTimeout(1500);

                OutputStream os = isActiveSocket.getOutputStream();
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));

                bw.write(String.valueOf(checkIfActive));
                bw.newLine();
                bw.flush();

                InputStream iStream = isActiveSocket.getInputStream();
                BufferedReader bReader = new BufferedReader(new InputStreamReader(iStream));

                if(bReader.readLine().equals("true")){
                    returnVal=true;
                }

                Log.e("My code - Server", "CheckActive processSequence number : " + Integer.parseInt(bReader.readLine()));

                bw.write("end");
                bw.newLine();
                bw.flush();

            } catch (NullPointerException e) {
                handleFailureOnServer(String.valueOf(processId));
                Log.e("My code - Server", "Failed to open Server Port due to : " + e);
                e.printStackTrace();
            }catch (ArrayIndexOutOfBoundsException e) {
                handleFailureOnServer(String.valueOf(processId));
                Log.e("My code - Server", "Failed to open Server Port due to : " + e);
                e.printStackTrace();
            }catch (SocketTimeoutException e){
                handleFailureOnServer(String.valueOf(processId));
                Log.e("My code - Server", "Failed to open Server Port due to : "+e);
                e.printStackTrace();
            } catch (SocketException e){
                handleFailureOnServer(String.valueOf(processId));
                Log.e("My code - Server", "Failed to open Server Port due to : "+e);
                e.printStackTrace();
            } catch (EOFException e) {
                handleFailureOnServer(String.valueOf(processId));
                Log.e("My code - Server", "Failed to open Server Port due to : "+e);
                e.printStackTrace();
            }catch (IOException e){
                Log.e("My code - Server", "Failed to open Server Port due to : " + e);
                e.printStackTrace();
            }catch (Exception e){
                Log.e("My code - Server", "Failed to open Server Port due to : "+e);
                e.printStackTrace();
            }finally {
                try {
                    isActiveSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.e("My code - Server", "CheckActive returning " + returnVal);
            return returnVal;
        }


        protected void onProgressUpdate(String... strings) {

            String strReceived = strings[0].trim();
            TextView remoteTextView = (TextView) findViewById(R.id.textView1);
            remoteTextView.append(strReceived + "\t\n");


            String filename = "GroupMessenger2Output";
            String string = strReceived + "\n";
            FileOutputStream outputStream;

            try {
                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(string.getBytes());
                outputStream.close();
            } catch (Exception e) {
                Log.e("My code", "File write failed");
            }

            return;
        }
    }

    public class MessagePOJO {

        int sequenceNo;
        int processId;
        int originSeqNo;
        int isDeliverable;
        String messageText;

        @Override
        public boolean equals(Object o) {
            final MessagePOJO obj = (MessagePOJO) o;
            if(this.processId == obj.processId && this.messageText.equals(obj.messageText) && this.originSeqNo==obj.originSeqNo)
                return true;
            else
                return false;
        }

        @Override
        public int hashCode() {
            int hash = 37*(this.originSeqNo+(this.messageText.hashCode()*3+this.processId*7%(this.originSeqNo+5)));
            return hash;
        }
    }

    public class MessageComparator implements Comparator<MessagePOJO>{

        @Override
        public int compare(MessagePOJO lhs, MessagePOJO rhs) {
            if(lhs.sequenceNo<rhs.sequenceNo)
                return -1;
            else if((lhs.sequenceNo==rhs.sequenceNo)){
                if(lhs.processId<rhs.processId)
                    return -1;
                else
                    return 1;
            }
            else
                return 1;
        }
    }
}
