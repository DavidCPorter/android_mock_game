package edu.uic.dporte7.game;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by Dporter1 on 11/15/17.
 */

public class Player2Thread extends HandlerThread {

    public Handler p2Handler;
    public Handler p1Handler;
    public Handler mainHandler;

    public static int[] p2Numbers = new int[4];
    public int[] p2Guess = new int[4];
    public int[] p1Response = new int[]{-2,-2,-2,-2};

    public static final int YOUR_TURN = 0 ;
    public static final int P1_RESPONSE = 1 ;
    public static final int YOU_WIN = 2 ;

    public ArrayList<String> p2UiUpdate = new ArrayList<>();



    public Player2Thread(String name) {
        super(name);

    }
    protected void onLooperPrepared(){
        p2Handler = new Handler(getLooper()){
            public void handleMessage(Message msg){
                //receive response from p2 or UI
                int what = msg.what;
                switch (what){
                    //P2 generates numbers and sends to P1 via runnable subclass below
                    case YOUR_TURN:
                        p2Guess = generateNumbers();
                        p1Handler = (Handler) msg.obj;
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        p1Handler.post(new checkP2GuessAndUpdateUIAndRespondToP2());
                        break;

                    default:
                        break;
                }

            }
        };
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
                num = addPrevCorrectNums(p1Response, num);
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

    //player 1 thread runs this from each call to P2 YOUR_TURN, then P1 thread tells itself its their turn
    public class checkP2GuessAndUpdateUIAndRespondToP2 implements Runnable{
        public void run(){
            int i;
            int x;
            int gameOver = 0;
            p2UiUpdate.add("\n your guess was: "+ Arrays.toString(p2Guess)+"\n");

            for(i=0;i<4;i++){

                if (p2Guess[i] == p2Numbers[i]) {
                    p2UiUpdate.add(" - " + p2Guess[i] + " is correctly positioned\n");
                    //if I do not update the p1Response array, then p2 will not use response feedback, thus unintelligent.
                    gameOver++;
                }
                else{
                    for(x=0;x<4;x++) {
                        if (p2Guess[i] == p2Numbers[x]) {
                            p2UiUpdate.add(" - " + p2Guess[i] + " is NOT correctly positioned\n");
                        }
                    }
                }
            }



            //send msg to UI, send msg to P1
            //I'm not sure how mainHandler is not null in this case since I never passed the reference.
            Message msg = mainHandler.obtainMessage();
            msg.what = MainActivity.UPDATE_P2;
            msg.obj = p2UiUpdate;
            mainHandler.sendMessage(msg);

            if(gameOver == 4){
                msg = mainHandler.obtainMessage();
                msg.what = MainActivity.GAME_OVER;
                msg.obj = "Player 2 Wins!";
                mainHandler.sendMessage(msg);
                return;
            }

            msg = p1Handler.obtainMessage(YOUR_TURN);
            msg.obj = p2Handler; //needs this reference so p1thread can task the Guess runnable to p2Handler
            p1Handler.sendMessage(msg);
        }
    }

}
