package com.example.jeon.raspberry;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Main2Activity extends AppCompatActivity {

    Button connect;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*
        스플래쉬 실행
         */
        super.onCreate(savedInstanceState);
        Intent splah = new Intent(getApplicationContext(),splash.class);
        startActivity(splah);


        setContentView(R.layout.activity_main2);
        connect = (Button) findViewById(R.id.button);

    }
    public void button_click(View v){
        EditText editText = (EditText) findViewById(R.id.editText);
        String ip = editText.getText().toString();
        Intent intent1=new Intent(getApplicationContext(),MainActivity.class);
        intent1.putExtra("ip",ip);
        startActivity(intent1);
        finish();
    }
}

