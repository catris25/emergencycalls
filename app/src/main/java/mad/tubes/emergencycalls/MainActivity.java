package mad.tubes.emergencycalls;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

    Button automaticButton;
    Button manualButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        automaticButton = (Button) findViewById(R.id.automaticButton);
        manualButton = (Button) findViewById(R.id.manualButton);

        automaticButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent findAuto = new Intent(MainActivity.this, FindAutomatic.class);
                MainActivity.this.startActivity(findAuto);
                Toast.makeText(getApplicationContext(), "Memproses lokasi Anda...", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
