package edu.uic.dporte7.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Button mStartButton;
    public static int[] p1Number = new int[4];
    public static int[] p2Number = new int[4];

    private boolean gameInProgress = false;

    public static final int UPDATE_P1 = 1 ;
    public static final int UPDATE_P2 = 2 ;
    public static final int GAME_OVER = 3 ;

    public static Player1Thread t1;
    public static Player2Thread t2;

    public TextView mPlayer1;
    public TextView mPlayer2;


    @SuppressLint("HandlerLeak")
    public Handler mainHandler = new Handler() {
        public void handleMessage(Message msg) {
            int what = msg.what ;
            switch (what) {

                case UPDATE_P1:

                        ArrayList<String> showP2Response = (ArrayList<String>) msg.obj;
                        int i;
                        mPlayer1.setText(null);
                        for(i=0; i<showP2Response.size(); i++){
                            mPlayer1.append(showP2Response.get(i));
                        }

                    break;

                case UPDATE_P2:

                        ArrayList<String> showP1Response = (ArrayList<String>) msg.obj;
                        mPlayer2.setText(null);
                        for(i=0; i<showP1Response.size(); i++){
                            mPlayer2.append(showP1Response.get(i));
                        }

                    break;

                case GAME_OVER:
                    String text = (String) msg.obj;
                    Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
                    toast.show();

                    break;

                default:
                    break;
            }
        }
    };

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView p1Tv = (TextView) findViewById(R.id.player1);
        TextView p2Tv = (TextView) findViewById(R.id.player2);

        mPlayer1 = (TextView) findViewById(R.id.p1ScrollView);
        mPlayer2 = (TextView) findViewById(R.id.p2ScrollView);
        mPlayer1.setMovementMethod(new ScrollingMovementMethod());
        mPlayer2.setMovementMethod(new ScrollingMovementMethod());


        p1Number = generateNumbers();
        p2Number = generateNumbers();

        p1Tv.setText(Arrays.toString(p1Number));
        p2Tv.setText(Arrays.toString(p2Number));



        mStartButton = (Button) findViewById(R.id.start_button);

        t1 = new Player1Thread("player1Thread") ;
        t1.start();


        Player1Thread.p1Numbers = p1Number;
        t1.mainHandler = mainHandler;


        t2 = new Player2Thread("player2Thread") ;
        t2.start();
        Player2Thread.p2Numbers = p2Number;
        t2.mainHandler = mainHandler;
        t1.p2Handler = t2.p2Handler;



        mStartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(gameInProgress){
                    t1.quit();
                    t2.quit();
                    recreate();
                }
                else{
                    gameInProgress = true;
                    Message msg = t1.p1Handler.obtainMessage();
                    msg.what = 0;
                    msg.obj = t2.p2Handler;
                    //send player 2 number so p1Thread can check p2 guesses
                    t1.p1Handler.sendMessage(msg);
                }
            }
        });
    }

    private int[] generateNumbers(){
        int i =0;
        int n;
        int[] num = new int[4];

        Random rand = new Random();
        while(i != 4) {
            n = rand.nextInt(10) + 0;
            num[i] = n;
            i++;
        }
        while(true){
            n = checkNumbers(num);
            if(n==-1){
                return num;
            }
            else{
                num[n] = rand.nextInt(10) + 0;
            }
        }
    }

//returns index of repeated number or -1 if all unique
    private int checkNumbers(int[] numbers){
        int i;
        int x;
        for(i=0; i<3; i++) {
            for (x = i; x < 3; x++) {
                if (numbers[i] == numbers[x + 1]) {
                    return x;
                }
            }
        }
        return -1;
    }
}
