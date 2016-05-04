package de.tum.in.tumcampusapp.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.BaseActivity;

public class QuizActivity extends BaseActivity {

    /**
     Quiz Activity
     */
    public QuizActivity(){super(R.layout.activity_quiz);}

    Spinner questionTypeSpinner;
    LinearLayout popUpLayout,mainLayout;
    Button dismissChoicesButton,submitChoicesBtn,submitQuestionBtn;
    EditText questionEt,choice1Et,choice2Et,choice3Et;
    CheckBox [] checkBoxes=new CheckBox[4];
    String selectedDepartments="";
    String question="";
    String choices="";
    String questionType="";
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getViewsById();
        fillSpinner();
        SpinnerListener();
        ButtonsListener();


    }

    //get views by id.
    private void getViewsById()
    {
        questionTypeSpinner= (Spinner)findViewById(R.id.questionSpinner);
        popUpLayout=(LinearLayout)findViewById(R.id.questionPopup);
        mainLayout=(LinearLayout)findViewById(R.id.mainLay);
        dismissChoicesButton=(Button)findViewById(R.id.dismissChoicesBtn);
        submitQuestionBtn=(Button)findViewById(R.id.submitQuestionBtn);
        submitChoicesBtn=(Button)findViewById(R.id.submitChoicesBtn);
        checkBoxes[0]=(CheckBox)findViewById(R.id.infChBx);
        checkBoxes[1]=(CheckBox)findViewById(R.id.enChBx);
        checkBoxes[2]=(CheckBox)findViewById(R.id.buChBx);
        checkBoxes[3]=(CheckBox)findViewById(R.id.phChBx);
        questionEt=(EditText)findViewById(R.id.etQuestion);
        choice1Et=(EditText)findViewById(R.id.choice1Et);
        choice2Et=(EditText)findViewById(R.id.choice2Et);
        choice3Et=(EditText)findViewById(R.id.choice3Et);


    }

    //fill Question Spinner with question types.
    private void fillSpinner()
    {
        String math = getResources().getString(R.string.faculty_mathematics);
        String physics = getResources().getString(R.string.faculty_physics);
        String chemistry = getResources().getString(R.string.faculty_chemistry);
        String tum_manag = getResources().getString(R.string.faculty_tum_school_of_management);
        String cge = getResources().getString(R.string.faculty_civil_geo_and_environmental_engineering);
        final String architecture = getResources().getString(R.string.faculty_architecture);
        String mechanical = getResources().getString(R.string.faculty_mechanical_Engineering);
        String electrical = getResources().getString(R.string.faculty_electrical_and_computer_engineering);
        String informatics = getResources().getString(R.string.faculty_informatics);
        String tum_life_sc = getResources().getString(R.string.faculty_tum_school_of_life_sciences_weihenstephan);
        String medicine = getResources().getString(R.string.faculty_tum_school_of_medicine);
        String sport = getResources().getString(R.string.faculty_sport_and_health_sciences);
        String edu = getResources().getString(R.string.faculty_tum_school_of_education);
        String political_social = getResources().getString(R.string.faculty_political_and_social_sciences);

        String[] items={"Select Question Type","YES or NO","Multiple Choice"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(),
                android.R.layout.simple_list_item_1, items);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                questionTypeSpinner.setAdapter(adapter);
    }

    //Spinner on item selected listener.
    private void SpinnerListener()
    {
        questionTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                if(position==1)
                    questionType="Yes or No";
                if(position==2)
                {
                    mainLayout.setVisibility(View.GONE);
                    popUpLayout.setVisibility(View.VISIBLE);
                    popUpLayout.bringToFront();
                    questionType="Multiple Choice";

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    //Dismiss,Submit Listeners.
    private void ButtonsListener()
    {
        //
        dismissChoicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                popUpLayout.setVisibility(View.GONE);
                mainLayout.setVisibility(View.VISIBLE);
            }
        });

        submitQuestionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                //check if department are selected.
                if(getSelectedCheckBoxesData(checkBoxes))
                {
                    //check if the user has entered a question
                    if(!questionEt.getText().toString().equals(""))
                    {
                        //check which type of question.
                        if(questionTypeSpinner.getSelectedItemPosition()==0)
                            Toast.makeText(getApplicationContext(),"Please Select Question Type",Toast.LENGTH_SHORT).show();
                        else if(questionTypeSpinner.getSelectedItemPosition()==1)
                        {
                            question="";
                            question+=questionEt.getText().toString();
                            showFinalDialog();


                        }
                        //check if the user has entered choices
                        else
                        {
                            if(!choice1Et.getText().toString().equals("")&&!choice2Et.getText().toString().equals("")
                                    &&!choice3Et.getText().toString().equals("")) {
                                question = "";
                                question += questionEt.getText().toString();
                                showFinalDialog();
                            }
                        }

                    }

                    else
                    {
                        Toast.makeText(getApplicationContext(),"Please Enter a Question",Toast.LENGTH_SHORT).show();
                    }

                }

                else
                {
                    Toast.makeText(getApplicationContext(),"Please Select Departments",Toast.LENGTH_SHORT).show();
                }
            }
        });

        submitChoicesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getChoices(choice1Et,choice2Et,choice3Et))
                {
                    mainLayout.setVisibility(View.VISIBLE);
                    popUpLayout.setVisibility(View.GONE);
                }

                else
                {
                    Toast.makeText(getApplicationContext(),"Please insert 3 choices",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    //get selected department and check if the user selects at least one
    private boolean getSelectedCheckBoxesData(CheckBox [] checkBoxes)
    {
        int selectedCount=0;
        for(int i=0;i<checkBoxes.length;i++)
        {
           if(checkBoxes[i].isChecked() && i<checkBoxes.length-1) {
               selectedDepartments="";
               selectedDepartments += checkBoxes[i].getText().toString() + ",";
               selectedCount++;
           }
            else if(checkBoxes[i].isChecked() && i==checkBoxes.length-1) {
               selectedDepartments="";
               selectedDepartments += checkBoxes[i].getText().toString();
               selectedCount++;
           }
        }

        if(selectedCount>0)
            return true;
        return  false;
    }

    private boolean getChoices(EditText a,EditText b, EditText c)
    {
        if(!a.getText().toString().equals("")&&!b.getText().toString().equals("")&&!c.getText().toString().equals(""))
        {
            choices="";
          choices+=a.getText().toString()+","+b.getText().toString()+","+c.getText().toString();
            return true;
        }
        else
        {
            return false;
        }
    }

    private void showFinalDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Question Review");
        if(questionType.equals("Multiple Choice"))
        {
            builder.setMessage("Question :"+question+"\n"+"Question Type :"+questionType+"\n"+"Choice1 :"+choice1Et.getText().toString()+"\n"
            +"Choice2 :"+choice2Et.getText().toString()+"\n"+"Choice3 :"+choice3Et.getText().toString()+"\n");

        }
        else
        {
            builder.setMessage("Question :"+question+"\n"+"Question Type :"+questionType+"\n");
        }
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialogInterface, int i)
            {
                Toast.makeText(getApplicationContext(),"Question has been submitted",Toast.LENGTH_SHORT).show();
                /*

                DATABASE

                 */
            }
        });

        builder.setNegativeButton("Edit", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialogInterface, int i)
            {
                Toast.makeText(getApplicationContext(),"Edit and submit again!",Toast.LENGTH_SHORT).show();
            }
        });

        Dialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

}

