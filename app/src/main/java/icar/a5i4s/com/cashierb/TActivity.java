package icar.a5i4s.com.cashierb;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.ums.AppHelper;

import java.io.FileOutputStream;

import icar.a5i4s.com.cashierb.helper.Logg;

public class TActivity extends AppCompatActivity implements View.OnClickListener {
    protected Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_t);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
    }

    public void printTest(){
        View view = getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        if(bitmap == null){
            Logg.i("", "bitmap is null");
            return;
        }

        String fname = "/sdcard/ddd.png";
        try {
            FileOutputStream out = new FileOutputStream(fname);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            Logg.i("", "file" + fname + "output done.");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        AppHelper.callPrint(this, fname);

    }

    @Override
    public void onClick(View v) {
        printTest();
    }
}
