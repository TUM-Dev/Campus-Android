package de.tum.in.tumcampusapp.activities;

import android.content.Intent;
import android.opengl.Visibility;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.ToggleButton;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.BaseActivity;

public class Survey extends BaseActivity {

    ToggleButton t1,t2;
    Spinner sp1;
    EditText e1,e2,e3;
    LinearLayout surveyL,resultsL;
    Button submitQuestion;
    public Survey(){super(R.layout.activity_survey);}
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        t1=(ToggleButton)findViewById(R.id.toggleButton);
        t2=(ToggleButton)findViewById(R.id.toggleButton2);
        sp1=(Spinner)findViewById(R.id.spinner1);
        e1=(EditText)findViewById(R.id.etQuestion1);
        submitQuestion=(Button)findViewById(R.id.submitQuestionBtn);
        e2=(EditText)findViewById(R.id.etQuestion2);
        e3=(EditText)findViewById(R.id.etQuestion3);
        surveyL=(LinearLayout)findViewById(R.id.surveyLayout);
        resultsL=(LinearLayout)findViewById(R.id.resultsLayout);

        submitQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sp1.getSelectedItemPosition()==0 &&!e1.getText().toString().equals(""))
                {

                }

                else if(sp1.getSelectedItemPosition()==1&&!e1.getText().toString().equals("")&&!e2.getText().toString().equals(""))
                {

                }

                else if(sp1.getSelectedItemPosition()==2&&!e1.getText().toString().equals("")&&!e2.getText().toString().equals("")&&!e3.getText().toString().equals(""))
                {

                }
            }
        });

        t1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                surveyL.setVisibility(View.VISIBLE);
                resultsL.setVisibility(View.GONE);
            }
        });
        t2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                surveyL.setVisibility(View.GONE);
                resultsL.setVisibility(View.GONE);
            }
        });

        fillSpinner();
        SpinnerListener();

    }

    private void fillSpinner()
    {   String[] items={"1","2","3"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(),
                android.R.layout.simple_list_item_1, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp1.setAdapter(adapter);
    }

    //Spinner on item selected listener.
    private void SpinnerListener()
    {
        sp1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {

                if(position==0)
                {
                    e2.setVisibility(View.GONE);
                    e3.setVisibility(View.GONE);
                    e2.setText("");
                    e3.setText("");
                }
                else if(position==1)
                {
                    e2.setVisibility(View.VISIBLE);
                    e3.setVisibility(View.GONE);
                    e3.setText("");
                }

                else if(position==2)
                {
                    e2.setVisibility(View.VISIBLE);
                    e3.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, UserPreferencesActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}
