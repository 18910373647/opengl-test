package com.diy.cheng.diyfilter;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.diy.cheng.opengl.FilterRenderer;

public class MainActivity extends AppCompatActivity {
    GLSurfaceView glSurfaceView;
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        glSurfaceView = (GLSurfaceView) findViewById(R.id.gl_surface_view);
        final FilterRenderer renderer = new FilterRenderer();
        renderer.setGlSurfaceView(glSurfaceView);

        btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renderer.startRecord();
            }
        });
    }
}
