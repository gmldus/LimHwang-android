package com.example.a2020limhwang;

        import androidx.appcompat.app.AppCompatActivity;

        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.os.Bundle;
        import android.text.TextUtils;
        import android.util.Log;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ImageButton;
        import android.widget.RadioButton;
        import android.widget.Toast;

public class EditActivity extends AppCompatActivity {

    Button save;
    ImageButton back, profile;
    EditText et_rate, et_input1, et_input2, et_input3, et_input4;
    RadioButton radio1, radio2;
    float rate = 0, input1 = 0, input2 = 0, input3 = 0, input4 = 0;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    String lecture = "";

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
        radio1 = findViewById(R.id.radio1);
        radio2 = findViewById(R.id.radio2);

        Intent intent = getIntent();
        lecture = intent.getStringExtra("lectureID");
    }

    public void onClick(View v) {
        boolean check_edit = testparam(et_rate.getText().toString(), et_input1.getText().toString(), et_input2.getText().toString());
        boolean check_r1 = radio1.isChecked();
        boolean check_r2 = radio2.isChecked();
        boolean check_r1edit = TextUtils.isEmpty(et_input3.getText().toString());
        boolean check_r2edit = TextUtils.isEmpty(et_input4.getText().toString());

        if (v.getId() == R.id.profile) {
            Intent intent = new Intent(this, MypageActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.back) {
            finish();
        } else if (v.getId() == R.id.save) {

            if (check_edit == true) {
                if ((check_r1 == true && check_r1edit == true) || (check_r2 == true && check_r2edit == true)) {
                    rate = Float.parseFloat(et_rate.getText().toString());
                    input1 = Float.parseFloat(et_input1.getText().toString());
                    input2 = Float.parseFloat(et_input2.getText().toString());

                    if (radio1.isChecked()) {
                        input3 = Float.parseFloat(et_input3.getText().toString());
                    } else if (radio2.isChecked()) {
                        input4 = Float.parseFloat(et_input4.getText().toString());
                    }

                    sharedPreferences = getSharedPreferences("sFile", MODE_PRIVATE);
                    editor = sharedPreferences.edit();
                    editor.putFloat(lecture + "rate", rate);
                    editor.putFloat(lecture + "absPoint", input1);
                    editor.putFloat(lecture + "noCount", input2);
                    editor.putFloat(lecture + "latePoint", input3);
                    editor.putFloat(lecture + "lateCountAbs", input4);
                    Log.d("point", lecture + "rateAtt " + rate + "");
                    Log.d("point", lecture + "absPoint " + input1 + "");
                    Log.d("point", lecture + "noCount " + input2 + "");
                    Log.d("point", lecture + "latePoint " + input3 + "");
                    Log.d("point", lecture + "lateCountAbs " + input4 + "");
                    editor.commit();

                    finish();
                }
                else {
                    Toast.makeText(getApplicationContext(), "값을 입력하세요", Toast.LENGTH_LONG).show();
                }
            }
            else {
                Toast.makeText(getApplicationContext(), "값을 입력하세요", Toast.LENGTH_LONG).show();
            }

        }

    }
    public boolean testparam (String... params){
        for (String param : params){
            if(param == null || param.isEmpty()){
                //Log.d("빈칸","빈칸");
                return false;
            }
        }
        return true;
    }
}
