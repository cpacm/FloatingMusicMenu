package com.cpacm.musicbtn;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.cpacm.FloatingMusicButton;
import com.cpacm.FloatingMusicMenu;

public class FmmActivity extends AppCompatActivity {

    private FloatingMusicMenu upFmm;
    private FloatingActionButton addFab;
    private FloatingActionButton subFab;
    private FloatingMusicButton extraFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fmm);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        extraFab = new FloatingMusicButton(this);
        extraFab.setCoverDrawable(getResources().getDrawable(R.drawable.author));

        upFmm = (FloatingMusicMenu) findViewById(R.id.fmm);
        addFab = (FloatingActionButton) findViewById(R.id.add_fab);
        subFab = (FloatingActionButton) findViewById(R.id.sub_fab);
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
