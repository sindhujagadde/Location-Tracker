package com.example.sindhuja.locationtracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by sindhuja on 7/9/2016.
 */

public class SettingsActivity extends AppCompatActivity {

    public static final String FREQUENCY = "FREQUENCY";
    public static final String RESULT = "RESULT";

    EditText frequency;
    EditText result;
    Button okButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        int defaultFrequency = getIntent().getIntExtra(FREQUENCY, MapsActivity.DEFAULT_FREQUENCY);
        int defaultResult = getIntent().getIntExtra(RESULT, MapsActivity.DEFAULT_RESULT);
        frequency = (EditText)findViewById(R.id.frequency);
        frequency.setText(String.valueOf(defaultFrequency));
        result = (EditText)findViewById(R.id.result);
        result.setText(String.valueOf(defaultResult));
        okButton = (Button) findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int frequencyValue = Integer.parseInt(frequency.getText().toString());
                int resultValue=Integer.parseInt(result.getText().toString());
                Intent intent = new Intent(SettingsActivity.this, MapsActivity.class);
                intent.putExtra("Frequency", frequencyValue);
                intent.putExtra("Result", resultValue);
                setResult(RESULT_OK,intent);
                Toast.makeText(SettingsActivity.this, "Saved Preferences", Toast.LENGTH_SHORT).show();
                finish();
            }
        });





    }
}