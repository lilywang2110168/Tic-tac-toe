package example.com.tictactoe;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;



public class TTTActivity extends AppCompatActivity {

    // TAG for logging
    private static final String TAG = "TTTActivity";

    // server to connect to
    protected static final int GROUPCAST_PORT = 20000;
    protected static final String GROUPCAST_SERVER = "54.68.24.231";

    // networking
    Socket socket = null;
    BufferedReader in = null;
    PrintWriter out = null;
    boolean connected = false;

    // UI elements
    Button board[][] = new Button[3][3];
    Button bConnect = null;
    Button bJoinGroup=null;
    Button bJoinThisGroup=null;
    Button bCreateGroup=null;
    Button bSubmitGroup=null;
    Button bJoinGroupIknow=null;
    EditText etName = null;
    EditText etGroupName = null;
    TextView groupToJoin=null;
    String groupName="";

    //game logic
    int [] column=new int[3];
    int [] row=new int[3];
    int antidiagonal=0;
    int diagonal=0;
    int unclickedBoard=9;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ttt);

        // find UI elements defined in xml
        bConnect = (Button) this.findViewById(R.id.bConnect);
        bJoinGroup= (Button) this.findViewById(R.id.bJoinGroup);
        bCreateGroup= (Button) this.findViewById(R.id.bCreateGroup);
        bSubmitGroup= (Button) this.findViewById(R.id.bSubmitGroup);
        bJoinThisGroup=(Button) this.findViewById(R.id.bJoinThisGroup);
        bJoinGroupIknow=(Button) this.findViewById(R.id.bJoinGroupIknow);
        etName = (EditText) this.findViewById(R.id.etName);

        //get the group name
        etGroupName = (EditText) this.findViewById(R.id.groupName);

        //the text view for the random group
        groupToJoin=(TextView) this.findViewById(R.id.groupToJoin);

        board[0][0] = (Button) this.findViewById(R.id.b00);
        board[0][1] = (Button) this.findViewById(R.id.b01);
        board[0][2] = (Button) this.findViewById(R.id.b02);
        board[1][0] = (Button) this.findViewById(R.id.b10);
        board[1][1] = (Button) this.findViewById(R.id.b11);
        board[1][2] = (Button) this.findViewById(R.id.b12);
        board[2][0] = (Button) this.findViewById(R.id.b20);
        board[2][1] = (Button) this.findViewById(R.id.b21);
        board[2][2] = (Button) this.findViewById(R.id.b22);


        // hide login controls
        hideLoginControls();

        // make the board non-clickable
        disableBoardClick();

        // hide the board
        hideBoard();

        //Hide the group buttons
        hideGroupControls();

        //hide the create group button
        hideCreateGroupControls();

        //hide the join group button
        hideRandomGroupControls();


        // assign OnClickListener to connect button
        bConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString();
                // sanitity check: make sure that the name does not start with an @ character
                if (name == null || name.startsWith("@")) {
                    Toast.makeText(getApplicationContext(), "Invalid name",
                            Toast.LENGTH_SHORT).show();
                } else {
                    send("NAME,"+etName.getText());
                    Toast.makeText(getApplicationContext(), "Hi "+name +" !",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        //join a random group button
        bJoinGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send("LIST,GROUPS");
                hideGroupControls();
                showRandomGroupControls();

            }
        });

        //create a group button
        bCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideGroupControls();
                showCreateGroupControls();
            }
        });


        //joint a group that I know
        bJoinGroupIknow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideGroupControls();
                showCreateGroupControls();
            }
        });

        //submit a group name for both join a group a know and create a new group
        bSubmitGroup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                groupName =  etGroupName.getText().toString();

                // sanitity check: make sure that the name does not start with an @ character
                if (groupName  == null || groupName .startsWith("@")) {
                    Toast.makeText(getApplicationContext(), "Invalid group name",
                            Toast.LENGTH_SHORT).show();
                } else {
                    send("JOIN,"+"@"+groupName+",2");
                }
            }
        });

//join this random group button
        bJoinThisGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send("JOIN,"+"@"+groupName+",2");
            }
        });




        // assign a common OnClickListener to all board buttons
        View.OnClickListener boardClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int x, y;
                switch (v.getId()) {
                    case R.id.b00:
                        x = 0;
                        y = 0;

                        // TODO: what do we do if the user clicked field (0,0)?
                        break;

                    case R.id.b01:
                        x = 0;
                        y = 1;

                        // TODO: what do we do if the user clicked field (0,1)?
                        break;

                    case R.id.b02:
                        x = 0;
                        y = 2;
                        break;

                    case R.id.b10:
                        x = 1;
                        y = 0;
                        break;

                    case R.id.b11:
                        x = 1;
                        y = 1;
                        break;

                    case R.id.b12:
                        x = 1;
                        y = 2;
                        break;

                    case R.id.b20:
                        x = 2;
                        y = 0;
                        break;

                    case R.id.b21:
                        x = 2;
                        y = 1;
                        break;

                    case R.id.b22:
                        x = 2;
                        y = 2;
                        break;

                    // [ ... and so on for the other buttons
                    default:
                        x=0;
                        y=0;
                        break;
                }
                if(!validMove(x,y)){
                    Toast.makeText(getApplicationContext(), "Invalid move, please try again" , Toast.LENGTH_SHORT).show();
                    return;
                }

                board[x][y].setText("X");
                column[y]++;
                row[x]++;
                if(x==y)
                    diagonal++;
                if(x+y==2)
                    antidiagonal++;

                unclickedBoard--;
                    //check if the game has ended
                 if(GameHasEnded()){
                endGame();
                     return;
                }

                Toast.makeText(getApplicationContext(), "Now, it's your opponents tern" , Toast.LENGTH_SHORT).show();
                send("MSG,@"+groupName+","+x+""+y);

                //disableBoardClick until the opponent has made a move
                disableBoardClick();

           
               

            }
        };

        // assign OnClickListeners to board buttons
        for (int x = 0; x < 3; x++)
            for (int y = 0; y < 3; y++)
                board[x][y].setOnClickListener(boardClickListener);


        // start the AsyncTask that connects to the server
        // and listens to whatever the server is sending to us
        connect();


    }


    //start the game
    void startGame() {
        hideCreateGroupControls();
        hideGroupControls();
        hideRandomGroupControls();
        enableBoardClick();
    }

    //this method checks if a game has ended
    boolean GameHasEnded(){

        if(diagonal==3|| antidiagonal==3){
            Toast.makeText(getApplicationContext(), "You won the game!" , Toast.LENGTH_SHORT).show();
            return true; }

        if(diagonal==-3|| antidiagonal==-3){
            Toast.makeText(getApplicationContext(), "You lost the game." , Toast.LENGTH_SHORT).show();
            return true; }


        for (int i=0;i<3; i++){
            if(row[i]==3 || column[i]==3){
                Toast.makeText(getApplicationContext(), "You won the game!" , Toast.LENGTH_SHORT).show();
                return true;}

            if(row[i]==-3 || column[i]==-3){
                Toast.makeText(getApplicationContext(), "You lost the game." , Toast.LENGTH_SHORT).show();
                return true;}

        }

        if (unclickedBoard==0){
            Toast.makeText(getApplicationContext(), "Tie!" , Toast.LENGTH_SHORT).show();
            return true;

        }
        return false;

    }

    //ends the game
    void endGame() {
    send("bye");
        //hide all the boards

        hideBoard();

        hideCreateGroupControls();
        Toast.makeText(getApplicationContext(), "Thanks for playing." , Toast.LENGTH_SHORT).show();

    }

    //check if this move is valid or not
    boolean validMove(int x, int y){
        if(x<0 || x>2 || y<0 || y>2){
            return false; }

        String text=board[x][y].getText().toString();
            if(text.equals("O") || text.equals("X") ){
            return false;
            }
        return true;
    }

    //the method handles the opponent's moves
    void opponentPlay(int x, int y){
        hideGroupControls();

        //handle invalid moves
        if (!validMove(x,y)){
        return;}

       unclickedBoard--;

        board[x][y].setText("O");
        column[y]--;

        row[x]--;
        if(x==y)
            diagonal--;

        if(x+y==2)
            antidiagonal--;

        if (GameHasEnded()){
            endGame();
            return;
        }
        showBoard();
        enableBoardClick();
        Toast.makeText(getApplicationContext(), "Your opponent has made a move" , Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy called");
        disconnect();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle menu click events
        if (item.getItemId() == R.id.exit) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ttt, menu);
        return true;
    }




    /***************************************************************************/
    /********* Networking ******************************************************/
    /***************************************************************************/

    /**
     * Connect to the server. This method is safe to call from the UI thread.
     */
    void connect() {

        new AsyncTask<Void, Void, String>() {

            String errorMsg = null;

            @Override
            protected String doInBackground(Void... args) {
                Log.i(TAG, "Connect task started");
                try {
                    connected = false;
                    socket = new Socket(GROUPCAST_SERVER, GROUPCAST_PORT);
                    Log.i(TAG, "Socket created");
                    in = new BufferedReader(new InputStreamReader(
                            socket.getInputStream()));
                    out = new PrintWriter(socket.getOutputStream());

                    connected = true;
                    Log.i(TAG, "Input and output streams ready");

                } catch (UnknownHostException e1) {
                    errorMsg = e1.getMessage();
                } catch (IOException e1) {
                    errorMsg = e1.getMessage();
                    try {
                        if (out != null) {
                            out.close();
                        }
                        if (socket != null) {
                            socket.close();
                        }
                    } catch (IOException ignored) {
                    }
                }
                Log.i(TAG, "Connect task finished");
                return errorMsg;
            }

            @Override
            protected void onPostExecute(String errorMsg) {
                if (errorMsg == null) {
                    Toast.makeText(getApplicationContext(),
                            "Connected to server", Toast.LENGTH_SHORT).show();

                    hideConnectingText();
                    showLoginControls();


                    // start receiving
                    receive();

                } else {
                    Toast.makeText(getApplicationContext(),
                            "Error: " + errorMsg, Toast.LENGTH_SHORT).show();
                    // can't connect: close the activity
                    finish();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Start receiving one-line messages over the TCP connection. Received lines are
     * handled in the onProgressUpdate method which runs on the UI thread.
     * This method is automatically called after a connection has been established.
     */

    void receive() {
        new AsyncTask<Void, String, Void>() {

            @Override
            protected Void doInBackground(Void... args) {
                Log.i(TAG, "Receive task started");
                try {
                    while (connected) {

                        String msg = in.readLine();

                        if (msg == null) { // other side closed the
                            // connection
                            break;
                        }
                        publishProgress(msg);
                    }

                } catch (UnknownHostException e1) {
                    Log.i(TAG, "UnknownHostException in receive task");
                } catch (IOException e1) {
                    Log.i(TAG, "IOException in receive task");
                } finally {
                    connected = false;
                    try {
                        if (out != null)
                            out.close();
                        if (socket != null)
                            socket.close();
                    } catch (IOException e) {
                    }
                }
                Log.i(TAG, "Receive task finished");
                return null;
            }

            @Override
            protected void onProgressUpdate(String... lines) {
                // the message received from the server is
                // guaranteed to be not null
                String msg = lines[0];


                // TODO: act on messages received from the server
                if(msg.startsWith("+OK,NAME")) {
                    hideLoginControls();
                    showGroupControls();
                    showBoard();
                    return;
                }

                if(msg.startsWith("+ERROR,NAME")) {
                    Toast.makeText(getApplicationContext(), msg.substring("+ERROR,NAME,".length()), Toast.LENGTH_SHORT).show();
                    return;
                }
                //when the player joins a group.
                if(msg.startsWith("+OK,JOIN")) {
                    Toast.makeText(getApplicationContext(), "You have successfully joined a group" , Toast.LENGTH_SHORT).show();
                    int prefixLength="+OK,JOIN,".length()+groupName.length()+2;
                    char i=msg.charAt(prefixLength);

                    hideCreateGroupControls();

                    //i=2 means that there two players in the group and the game can be started
                    if(i=='2'){
                       Toast.makeText(getApplicationContext(), "The game starts" , Toast.LENGTH_SHORT).show();

                        startGame();
                    }

                    //i=1 means: waiting for another player
                    if(i=='1'){
                        hideRandomGroupControls();
                        Toast.makeText(getApplicationContext(), "Waiting for another player" , Toast.LENGTH_SHORT).show();
                    }
                    return;
                }

                if(msg.startsWith("+ERROR,JOIN")) {
                    Toast.makeText(getApplicationContext(), "Failure joining a group" , Toast.LENGTH_SHORT).show();
                    hideRandomGroupControls();
                    hideCreateGroupControls();
                    showGroupControls();
                    return;
                }

                //when the server returns a list of groups, display the first group that can be joined ((1/2) group)
                if(msg.startsWith("+OK,LIST,GROUPS")) {

                    String [] groups=msg.split("@");
                    String firstGroup="";

                    for(String group: groups){
                        if (group.contains("(1/2)")){
                            firstGroup=group;
                        }
                    }

                    firstGroup=firstGroup.replace("(1/2)","");
                    firstGroup=firstGroup.replace(",","");


                    //no groups to join
                    if(firstGroup.length()==0){
                        Toast.makeText(getApplicationContext(), "No available groups to join" , Toast.LENGTH_SHORT).show();
                        hideRandomGroupControls();
                        showGroupControls();
                    }
                    else{

                        groupName=firstGroup;
                        //you can join a group
                        groupToJoin.setText(groupName);
                        groupName=firstGroup;


                    }

                    return;
                }

                //handles messages when the opponent makes a move
                if(msg.startsWith("+MSG")) {
                    String []messages=msg.split(",");

                    //error checking
                    if (messages.length<4)
                        return;
                    if(messages[3].length()<2)
                        return;

                    int x=messages[3].charAt(0)-'0';
                    int y=messages[3].charAt(1)-'0';
                    opponentPlay(x,y);
                    return;
                }

                //do nothing
                if(msg.startsWith("+OK,MSG")) {
                  return;
                }


                // [ ... and so on for other kinds of messages]


                // if we haven't returned yet, tell the user that we have an unhandled message
                Toast.makeText(getApplicationContext(), "Unhandled message: "+msg, Toast.LENGTH_SHORT).show();
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    /**
     * Disconnect from the server
     */
    void disconnect() {
        new Thread() {
            @Override
            public void run() {
                if (connected) {
                    connected = false;
                }
                // make sure that we close the output, not the input
                if (out != null) {
                    out.print("BYE");
                    out.flush();
                    out.close();
                }
                // in some rare cases, out can be null, so we need to close the socket itself
                if (socket != null)
                    try { socket.close();} catch(IOException ignored) {}

                Log.i(TAG, "Disconnect task finished");
            }
        }.start();
    }

    /**
     * Send a one-line message to the server over the TCP connection. This
     * method is safe to call from the UI thread.
     *
     * @param msg
     *            The message to be sent.
     * @return true if sending was successful, false otherwise
     */
    boolean send(String msg) {
        if (!connected) {
            Log.i(TAG, "can't send: not connected");
            return false;
        }

        new AsyncTask<String, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(String... msg) {
                Log.i(TAG, "sending: " + msg[0]);
                out.println(msg[0]);
                return out.checkError();
            }

            @Override
            protected void onPostExecute(Boolean error) {
                if (!error) {
                    Toast.makeText(getApplicationContext(),
                            "Message sent to server", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Error sending message to server",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, msg);

        return true;
    }

    /***************************************************************************/
    /***** UI related methods **************************************************/
    /***************************************************************************/

    /**
     * Hide the "connecting to server" text
     */
    void hideConnectingText() {
        findViewById(R.id.tvConnecting).setVisibility(View.GONE);
    }

    /**
     * Show the "connecting to server" text
     */
    void showConnectingText() {
        findViewById(R.id.tvConnecting).setVisibility(View.VISIBLE);
    }

    /**
     * Hide the login controls
     */
    void hideLoginControls() {
        findViewById(R.id.llLoginControls).setVisibility(View.GONE);
    }

    /**
     * Show the login controls
     */
    void showLoginControls() {
        findViewById(R.id.llLoginControls).setVisibility(View.VISIBLE);
    }

    /**
     * Show the group controls
     */
    void showGroupControls() {
        findViewById(R.id.groups).setVisibility(View.VISIBLE);
    }


    void hideGroupControls() {
        findViewById(R.id.groups).setVisibility(View.GONE);
    }

    /**
     * Show the create group controls
     */
    void showCreateGroupControls() {
        findViewById(R.id.myGroup).setVisibility(View.VISIBLE);
    }


    void hideCreateGroupControls() {
        findViewById(R.id.myGroup).setVisibility(View.GONE);
    }


    void showRandomGroupControls() {
        findViewById(R.id.randomGroup).setVisibility(View.VISIBLE);
    }


    void hideRandomGroupControls() {
        findViewById(R.id.randomGroup).setVisibility(View.GONE);
    }

    /**
     * Hide the tictactoe board
     */
    void hideBoard() {
        findViewById(R.id.llBoard).setVisibility(View.GONE);
    }

    /**
     * Show the tictactoe board
     */
    void showBoard() {
        findViewById(R.id.llBoard).setVisibility(View.VISIBLE);
    }


    /**
     * Make the buttons of the tictactoe board clickable if they are not marked yet
     */
    void enableBoardClick() {
        for (int x = 0; x < 3; x++)
            for (int y = 0; y < 3; y++)
                if ("".equals(board[x][y].getText().toString()))
                    board[x][y].setEnabled(true);
    }

    /**
     * Make the tictactoe board non-clickable
     */
    void disableBoardClick() {
        for (int x = 0; x < 3; x++)
            for (int y = 0; y < 3; y++)
                board[x][y].setEnabled(false);
    }




}
