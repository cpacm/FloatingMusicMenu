package com.cpacm.musicbtn;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.cpacm.FloatingMusicButton;
import com.cpacm.FloatingMusicMenu;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FmmActivity extends AppCompatActivity {

    private FloatingMusicMenu upFmm;
    private FloatingActionButton addFab;
    private FloatingActionButton subFab;
    private FloatingMusicButton extraFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fmm);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        extraFab = new FloatingMusicButton(this);
        extraFab.setCoverDrawable(getResources().getDrawable(R.drawable.author));

        upFmm = findViewById(R.id.fmm);
        addFab = findViewById(R.id.add_fab);
        subFab = findViewById(R.id.sub_fab);
        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (extraFab.getParent() == null) {
                    upFmm.addButton(extraFab);
                    extraFab.rotate(true);
                }
            }
        });
        subFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upFmm.removeButton(extraFab);
                extraFab.rotate(false);
            }
        });
    }

}
