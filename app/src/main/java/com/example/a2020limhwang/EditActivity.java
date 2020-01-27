package com.example.a2020limhwang;

        import androidx.appcompat.app.AppCompatActivity;

        import android.content.Intent;
        import android.os.Bundle;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;

public class EditActivity extends AppCompatActivity {

    Button back, profile, save;
    EditText et_rate, et_input1, et_input2, et_input3, et_input4;
    float rate, input1, input2, input3, input4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        back = findViewById(R.id.back);
        profile = findViewById(R.id.profile);
        save = findViewById(R.id.save);
        et_rate = findViewById(R.id.rate);
        et_input1 = findViewById(R.id.input1);
        et_input2 = findViewById(R.id.input2);
        et_input3 = findViewById(R.id.input3);
        et_input4 = findViewById(R.id.input4);
    }

    public void onClick(View v) {
        if (v.getId() == R.id.profile) {
            Intent intent = new Intent(this, MypageActivity.class);
            startActivity(intent);
        }
        else if (v.getId() == R.id.back) {
            finish();
        }
        else if (v.getId() == R.id.save) {
            rate = Float.parseFloat(et_rate.getText().toString());
            input1 = Float.parseFloat(et_input1.getText().toString());
            input2 = Float.parseFloat(et_input2.getText().toString());

            input3 = Float.parseFloat(et_input3.getText().toString());
            input4 = Float.parseFloat(et_input4.getText().toString());
            ScoreInformation si = new ScoreInformation(rate, input1, input2, input3, input4);

            Intent intent = new Intent();
            intent.putExtra("score_information", si);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
