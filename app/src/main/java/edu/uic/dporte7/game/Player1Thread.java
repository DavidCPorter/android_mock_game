package edu.uic.dporte7.game;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static edu.uic.dporte7.game.Player2Thread.*;

/**
 * Created by Dporter1 on 11/15/17.
 */

public class Player1Thread extends HandlerThread {

    public Handler p1Handler;
    public Handler p2Handler;
    public Handler mainHandler;

    public static int[] p1Numbers = new int[4];
    public int[] p1Guess = new int[4];
    public int[] p2Response = new int[]{-2,-2,-2,-2};

    public static final int YOUR_TURN = 0 ;
    public static final int P2_RESPONSE = 1 ;
    public static final int YOU_WIN = 2 ;

    public int MAX_GUESSES = 0;

    public ArrayList<String> p1UiUpdate = new ArrayList<>();


    public Player1Thread(String name) {
        super(name);

    }
    @Override
    public void onLooperPrepared(){
        p1Handler = new Handler(getLooper()){
            public void handleMessage(Message msg){
            //receive response from p2 or UI
                int what = msg.what;
                switch (what){

                    case YOUR_TURN:
                        p1Guess = generateNumbers();
                        if (p1Guess == null){
                            msg = mainHandler.obtainMessage();
                            msg.what = MainActivity.GAME_OVER;
                            msg.obj = "Game Reached Max Guesses";
                            mainHandler.sendMessage(msg);
                            break;
                        }
                        p2Handler = (Handler) msg.obj;
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        p2Handler.post(new checkP1GuessAndUpdateUIAndRespondToP1());
                        break;

                    default:
                        break;
                }

            }
        };
    }

    private int[] generateNumbers(){

        MAX_GUESSES +=1;

        if(MAX_GUESSES == 21){
            return null;
        }

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
                num = addPrevCorrectNums(p2Response, num);
                return num;
            }
            else{
                num[n] = rand.nextInt(10) + 0;
            }
        }
    }

    private int[] addPrevCorrectNums(int[] correct, int[] num) {
        int i;
        for(i=0; i<4; i++){
            if(correct[i] != -2){
                num[i] = correct[i];
            }
        }
        return num;
    }

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
    //This is a runnable for player 2 thread
    public class checkP1GuessAndUpdateUIAndRespondToP1 implements Runnable{
        public void run(){
            int i;
            int x;
            int gameOver=0;

            p1UiUpdate.add("\n your guess was: "+ Arrays.toString(p1Guess)+"\n");

            for(i=0;i<4;i++){

                if (p1Guess[i] == p1Numbers[i]) {
                    p1UiUpdate.add(" - " + p1Guess[i] + " is correctly positioned\n");
                    p2Response[i] = p1Numbers[i]; //if I update the p2Response array, then p1 will act intelligently by using the feedback in next guess.
                    gameOver++;
                }
                else{
                    for(x=0;x<4;x++) {
                        if (p1Guess[i] == p1Numbers[x]) {
                            p1UiUpdate.add(" - " + p1Guess[i] + " is NOT correctly positioned\n");
                        }
                    }
                }
            }



            //send msg to UI, send msg to p2
            //I'm not sure how mainHandler is not null in this case since I never passed the reference.
            Message msg = mainHandler.obtainMessage();
            msg.what = MainActivity.UPDATE_P1;
            msg.obj = p1UiUpdate;
            mainHandler.sendMessage(msg);



            if(gameOver == 4) {
                msg = mainHandler.obtainMessage();
                msg.what = MainActivity.GAME_OVER;
                msg.obj = "Player 1 Wins!";
                mainHandler.sendMessage(msg);
                return;
            }

            msg = p2Handler.obtainMessage(YOUR_TURN);
            msg.obj = p1Handler; //needs this reference so p2thread can task the Guess runnable to p1Handler
            p2Handler.sendMessage(msg);
        }
    }
}
