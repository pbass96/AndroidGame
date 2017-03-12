package com.example.administrator.game;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;

public class ClearActivity extends AppCompatActivity {

    public static final String EXTRA_IS_CLEAR = "com.example.administrator.game.EXTRA.IS_CLEAR";
    public static final String EXTRA_BLOCK_COUNT = "com.example.administrator.game.EXTRA.BLOCK_COUNT";
    public static final String EXTRA_TIME = "com.example.administrator.game.EXTRA.TIME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");

            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }


        }catch (Exception ex) {

        }
        setContentView(R.layout.activity_clear);

        Intent receiveIntent = getIntent();

        if(receiveIntent == null) {
            finish();
        }

        Bundle receiveExtras = receiveIntent.getExtras();
        if(receiveExtras == null) {
            finish();
        }

        boolean isClear = receiveExtras.getBoolean(EXTRA_IS_CLEAR, false);
        int blockCount = receiveExtras.getInt(EXTRA_BLOCK_COUNT, 0);
        long clearTime = receiveExtras.getLong(EXTRA_TIME, 0);

        TextView textTitle = (TextView) findViewById(R.id.textTitle);
        TextView textBlockCount = (TextView) findViewById(R.id.textBlockCount);
        TextView textClearTime = (TextView) findViewById(R.id.textClearTime);
        Button gameStart = (Button) findViewById(R.id.buttonGameStart);

        // 게임 오버시
        if(isClear) {
            textTitle.setText(R.string.clear);
        } else {
            textTitle.setText(R.string.game_over);
        }

        //남은 블록표시
        textBlockCount.setText(getString(R.string.block_count, String.valueOf(blockCount)));

        // 클리어 또는 게임 오보시 경과시간 표기
        textClearTime.setText(getString(R.string.time, clearTime/1000, clearTime%1000));

        gameStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClearActivity.this, GameActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
            }
        });

        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");

            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
            else {
                Toast.makeText(this, "menuKeyField is null", Toast.LENGTH_SHORT).show();
            }


        }catch (Exception ex) {
            Toast.makeText(this, "error ="+ ex.getMessage(), Toast.LENGTH_SHORT).show();

        }

        // 스코어 계산
        TextView textScore = (TextView) findViewById(R.id.textScore);
        final long score = (GameView.BLOCK_COUNT - blockCount) * clearTime;
        textScore.setText(getString(R.string.score, score));

        //TextView textHighScore = (TextView) findViewById(R.id.testHighScore);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        long highScore = sharedPreferences.getLong("high_score", 0);

        if(highScore < score) {
            highScore = score;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong("high_score", highScore);
            editor.commit();

        }

        TextView textHighScore = (TextView) findViewById(R.id.textHighScore);
        textHighScore.setText(getString(R.string.high_score, highScore));

        // share 처리
        Button buttonShare = (Button) findViewById(R.id.buttonShare);
        buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.score, score));
                startActivity(intent);
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_clear, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_settings2) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
