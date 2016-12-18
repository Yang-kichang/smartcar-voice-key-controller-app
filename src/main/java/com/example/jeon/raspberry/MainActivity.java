package com.example.jeon.raspberry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;




public class MainActivity extends Activity {
    SpeechRecognizer sr;
    JSch localJSch;
    Session session;
    Channel channel1;
    Channel channel2;
    TextToSpeech tts;
    Intent sp;
    RelativeLayout voice;
    LinearLayout controller;
    TextView message;

    String controlcmd1 = "java -Djava.library.path=/usr/lib/jni -cp /usr/share/java/RXTXcomm.jar:. RxTxSample";    // 라즈베리파이 내 아두이노 제어 자바 실행 명령어
    String controlcmd2 = "rm -f /var/lock/LCK*";                                                                   // 라즈베리파이 포트 unlocking
    String controlcmd3 = "sh ~/code/mjpg-streamer-experimental/mjpg.sh";                                           // 라즈베리파이 내 mjpg streamer 실행 명령어

    WebView webview;   // 영상 띄우기 위한 webview
    Button b1, sbtn, rbtn, lbtn, dbtn;
    PrintStream out;
    String ip;
    Switch sw;
    Button voice_controller;
    RecognitionListener rl;


    Handler camera = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            webview.loadUrl("http://"+ip+":8080/stream_simple.html");
            /*
                웹 캠 페이지 파싱 핸들러
                loadUrl 문자열에 앞 Activity에서 받은 ip 주소 변수 추가할 것   - 지은
            */

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    /*
    RecognizerIntent설정
    */
        sp = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        sp.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getPackageName());
        sp.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");
        sp.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,true);


        controller=(LinearLayout) findViewById(R.id.controller);
        voice=(RelativeLayout)findViewById(R.id.voice);
        message=(TextView)findViewById(R.id.message);


        Intent intent1 = getIntent();
        ip = intent1.getStringExtra("ip").toString();
        /*
            tts엔진 구동을 위한 설정
         */
        tts = new TextToSpeech(getBaseContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {

            }
        });
        tts.setLanguage(Locale.KOREAN);
        tts.isLanguageAvailable(Locale.US);

         /*
            tts엔진 구동1. (앱이 시작됨을 알림)
         */
        speech("엔진 구동이 시작됩니다!!");


        b1 = (Button) findViewById(R.id.b1);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonclicked(); // 연결 및 웹 캠 서버 연결 쓰레드
                speech("연결작업 완료");
            }
        });

        /*
         layout  버튼 memory load
         */
        webview = (WebView) findViewById(R.id.webView);
        sbtn = (Button) findViewById(R.id.sbtn);
        rbtn = (Button) findViewById(R.id.rbtn);
        lbtn = (Button) findViewById(R.id.lbtn);
        dbtn = (Button) findViewById(R.id.dbtn);
        voice_controller = (Button) findViewById(R.id.voice_controller);

        /*
        스위치 이벤트처리
         */
        sw=(Switch)findViewById(R.id.switch1);
        sw.setChecked(false);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked==false){
                    voice.setVisibility(View.GONE);
                    controller.setVisibility(View.VISIBLE);

                }

                else if(isChecked==true){
                    voice.setVisibility(View.VISIBLE);
                    controller.setVisibility(View.GONE);
                }
            }
        });


         /*
        음성인식 세팅
        */
        sr=SpeechRecognizer.createSpeechRecognizer(getApplicationContext());





        /*

         ** 버튼  리스너**

          버튼에서 손을 떼기 전까지 output stream으로 명령어를 출력
          손을 떼면 flush 로 라즈베리파이로 전송하고 , 명령 끝을 알리는 0을 전송

         방향  :  앞(8) 뒤(2)  좌(4)  우(6)  ->  아두이노 코드로 설정 하였음

         */

        sbtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        speech("전진합니다!");
                        try {
                            out.print("8");
                            out.flush();

                            System.out.println("8");

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        speech("정지");
                        try {
                            out.flush();
                            out.println("0");
                            out.flush();
                            System.out.println("0");
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                            break;

                }

                return false;
            }
        });
       



        rbtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()){

                    case MotionEvent.ACTION_DOWN:
                        speech("오른쪽으로 이동합니다.");
                        try{
                            out.print("6");
                            out.flush();

                            System.out.println("6");
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        speech("정지");
                        try {
                            out.flush();
                            out.println("0");
                            out.flush();
                            System.out.println("0");
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        break;

                }

                return false;
            }
        });



        lbtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        speech("왼쪽으로 이동합니다.");
                        try {
                            out.print("4");
                            out.flush();
                            System.out.println("4");
                        }catch (Exception e) {
                            e.printStackTrace();
                        }

                        break;
                    case MotionEvent.ACTION_UP:
                        speech("정지");
                        try {
                            out.flush();
                            out.println("0");
                            out.flush();
                            System.out.println("0");
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        break;
                }
                return false;
            }
        });


/*

  */

        dbtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch(event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        speech("후진합니다!");
                        try{

                            out.print("2");
                            out.flush();

                            System.out.println("2");


                        }catch (Exception e){
                            e.printStackTrace();

                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        speech("정지");
                        try {
                            out.flush();
                            out.println("0");
                            out.flush();
                            System.out.println("0");
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        break;
                }


                return false;
            }

        });

        voice_controller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Lis();
            }
        });

    }


    public void Lis(){

        rl = new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {
                speech("알수없는 오류가 발생하였습니다.");
                Toast.makeText(getBaseContext(), "" + error, Toast.LENGTH_SHORT).show();
            }
            boolean idx=true;
            @Override
            public void onResults(Bundle results) {

                String key = "";
                key = SpeechRecognizer.RESULTS_RECOGNITION;
                ArrayList<String> mResult =results.getStringArrayList(key);
                String[] rs = new String[mResult.size()];
                mResult.toArray(rs);
                check(rs[0].toString());
               // Toast.makeText(getBaseContext(),rs[0].toString(),Toast.LENGTH_SHORT).show();
                message.setText("입력한 명령: "+rs[0].toString());

                if(idx==false)speech("인식된 명령이 없습니다. 명령을 확인하거나 성대수술을 권장합니다.");

                if (sr != null) {
                    sr.destroy();

                } else {
                    sr.startListening(sp);
                }
            }


            @Override
            public void onPartialResults(Bundle partialResults) {
                /*
                String key = "";
                key = SpeechRecognizer.RESULTS_RECOGNITION;
                ArrayList<String> mResult = partialResults.getStringArrayList(key);
                String[] rs = new String[mResult.size()];
                mResult.toArray(rs);
                check(rs[0].toString());
                message.setText("입력 명령: "+rs[0]);
                */


        }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        };
        sr.setRecognitionListener(rl);
        sr.startListening(sp);
    }


    /*
        tts엔진 구동 메소드
     */
    public void speech(String string) {
        try {
            //if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP)
            tts.speak(string, TextToSpeech.QUEUE_FLUSH, null);

            //Toast.makeText(getBaseContext(), "" + et.getText().toString(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
        }
    }

    /*
      **웹캠 서버 및 shell 커맨드 쓰레드**

      쓰레드 사용이유 ->  안드로이드에서 network와 관련된 연결은 main Thread에서 사용할 수 없다 ->  험수 쓰레드 사용

      쉘 커맨드 호출 순서 Jsch 객체 - Session -  Channel - ChannelExec (command)

    */
    public void buttonclicked() {

        new Thread() {
            public void run() {
                while (true) {
                    try {
                        camera.sendEmptyMessage(1);   //웹캠 서버 파싱

                        localJSch = new JSch();

                        session = localJSch.getSession("pi", ip, 22);  //  앞 Activity에서 입력 받은 ip 파라미터를 변수로 추가할 것  -지은
                        session.setPassword("raspberry"); // 라즈베리파이 password


                        Properties localProperties = new Properties();
                        localProperties.put("StrictHostKeyChecking", "no");

                        /*
                          secure shell 연결 환경설정
                         */

                        session.setConfig(localProperties);
                        session.connect();

                        channel1 = session.openChannel("exec");   //channel 띄움  - >  터미널 띄운다
                        channel2 = session.openChannel("exec");   //channel 띄움  - >  터미널 띄운다

                        System.out.println("==> Connected to raspberry <==");
                        channel1 = session.openChannel("exec");
                        ((ChannelExec) channel1).setCommand(controlcmd2); // usb locking 해제 커맨드 설정
                        channel1.connect();                               // 커맨드 실행

                        channel1 = session.openChannel("exec");
                        ((ChannelExec) channel1).setErrStream(System.err); // err 스트림 설정  라즈베리파이에서 발생한 에러를 안드로이드 스튜디오에서 확인 할 수 있다.
                        ((ChannelExec) channel2).setErrStream(System.err);
                        channel1.setInputStream(System.in);                // channel 1  에서 실행한 명령어의 결과를 안드로이드 스튜디오에서 확인 할 수 있다.

                        ((ChannelExec) channel1).setCommand(controlcmd3);  //
                        ((ChannelExec) channel2).setCommand(controlcmd1);  //
                        channel1.connect();                                // 커맨드 실행
                        channel2.connect();                                // 커맨드 실행

                        out = new PrintStream(channel2.getOutputStream()); // 버튼 제어를 위한 outpustream 지정

                    } catch (Exception localException1) {
                        localException1.printStackTrace();
                        return;
                    }
                    try {
                        Thread.sleep(100L);
                        return;
                    } catch (Exception localException2) {
                    }
                }
            }
        }.start();
    }

    /*
    음성명령 케이스별 작동 분류
     */
    public boolean check(String string){
        if(string.contains("앞")||string.contains(("전진"))){
            try {
                out.print("8");
                out.flush();
                speech("전진합니다!");
                System.out.println("8");
            }catch (Exception e){
                message.setText("Connection Error!");
            }
            return true;
        }

        else if(string.contains("뒤")||string.contains("후진")){
            try {
                out.print("2");
                out.flush();
                speech("후진합니다!");
                System.out.println("2");
            }catch (Exception e){
                message.setText("Connection Error!");
            }

            return true;
        }

        else if(string.contains("좌회전")||string.contains("왼")){
            try {
                out.print("4");
                out.flush();
                speech("왼쪽으로 이동합니다.");
                System.out.println("4");
            }catch (Exception e){
                message.setText("Connection Error!");
            }

            return true;
        }

        else if(string.contains("우회전")||string.contains("오른")){
            try {
                out.print("6");
                out.flush();
                speech("오른쪽으로 이동합니다.");
                System.out.println("6");
            }catch (Exception e){
                message.setText("Connection Error!");
            }

            return true;
        }

        else if(string.contains("정지")||string.contains("멈춰")||string.contains("그만")){
            try {
                out.flush();
                out.println("0");
                out.flush();
                speech("운행 일시정지!");
                System.out.println("0");
            }catch (Exception e){
                message.setText("Connection Error!");
            }

            return true;
        }

        else{

            return false;
        }


    }

    @Override
    protected void onDestroy() {
        sr.destroy();
        super.onDestroy();
    }
}
