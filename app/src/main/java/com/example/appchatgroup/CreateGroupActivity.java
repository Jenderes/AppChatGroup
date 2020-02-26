package com.example.appchatgroup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class CreateGroupActivity extends AppCompatActivity {
    Button buttonnextslide;
    EditText namegroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        buttonnextslide = (Button) findViewById(R.id.next_button_group);
        namegroup = (EditText)findViewById(R.id.name_group);
        buttonnextslide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String StrnameGroup = namegroup.getText().toString();
                Intent choosepeople = new Intent(CreateGroupActivity.this,ChoosePeopleForGroupActivity.class);
                choosepeople.putExtra("group_name",StrnameGroup);
                startActivity(choosepeople);
            }
        });
    }
}
