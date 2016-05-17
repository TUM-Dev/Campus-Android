package de.tum.in.tumcampusapp.activities;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.BaseActivity;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.ChatMember;
import de.tum.in.tumcampusapp.models.managers.AbstractManager;
import de.tum.in.tumcampusapp.models.managers.SurveyManager;


public class SurveyActivity extends BaseActivity {

    EditText question1Et,question2Et,question3Et;
    Spinner aSpinner1;
    TextView selectTv;
    TabHost tabHost;
    Button submitSurveyButton , facultiesButton;
    ArrayList<String> questions=new ArrayList<>();
    ArrayList<String> selectedFaculties = new ArrayList<>();
    boolean[]checked=new boolean[14];
    LinearLayout mainResponseLayout;
    String question1="",question2="",question3="",chosenFaculties="",newDate="",lrzId;
    //private SQLiteDatabase db;
    Toolbar main;
    String[] numQues=new String[3];
    private SurveyManager surveyManager;

    public SurveyActivity(){super(R.layout.activity_survey2);}



    @Override
    public void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        lrzId = Utils.getSetting(this, Const.LRZ_ID, "");

        findViewsById();
        setUpTabHost();
        setUpSpinner();
        setUpSelectTargets();
        buttonsListener();
        surveyManager = new SurveyManager(this);
        //db = DatabaseManager.getDb(getApplicationContext());
        //db.execSQL("CREATE TABLE IF NOT EXISTS survey1 (id INTEGER PRIMARY KEY AUTOINCREMENT, date VARCHAR,userID VARCHAR, question TEXT, faculties TEXT, "
          //      + "yes INTEGER,  no INTEGER, flags INTEGER)");

        setUpResponseTab();
        userAllowed();


    }

    //set up the respone tab layout dynamically depending on number of questions
    @SuppressLint("SetTextI18n")
    public void setUpResponseTab()
    {
        //get response and question from database->set i<Number of question
        for (int i = 0; i < 10; i++) {

            //linear layout for every question
            LinearLayout ques = new LinearLayout(this);
            LinearLayout.LayoutParams quesParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            ques.setOrientation(LinearLayout.VERTICAL);
            ques.setWeightSum(5);
            mainResponseLayout.addView(ques, quesParams);


            LinearLayout l = new LinearLayout(this);
            LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            l.setOrientation(LinearLayout.HORIZONTAL);
            l.setWeightSum(5);
            ques.addView(l, lParams);

            LinearLayout l1 = new LinearLayout(this);
            l1.setOrientation(LinearLayout.VERTICAL);
            l1.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 4.5f));
            l.addView(l1);

            LinearLayout l2 = new LinearLayout(this);
            l2.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.3f));
            l2.setOrientation(LinearLayout.VERTICAL);
            l.addView(l2);
            //adding quesion tv
            TextView questionTv = new TextView(this);
            LinearLayout.LayoutParams tvparams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tvparams.setMargins(50, 0, 0, 0);
            questionTv.setLayoutParams(tvparams);
            //setText(question)
            questionTv.setText("asdasds");
            l1.addView(questionTv);
            //adding button delete
            float inPixels = getResources().getDimension(R.dimen.dimen_buttonHeight_in_dp);
            Button deleteButton = new Button(this);
            deleteButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, (int) inPixels));
            deleteButton.setBackgroundResource((R.drawable.minusicon));
            deleteButton.setOnClickListener(clicks);
            deleteButton.setTag(i);
            l2.addView(deleteButton);

            //adding progress bar with answers
            RelativeLayout r = new RelativeLayout(this);
            LinearLayout.LayoutParams Params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            Params.setMargins(50, 50, 50, 50);
            ques.addView(r, Params);

            float inPixels2 = getResources().getDimension(R.dimen.dimen_progressHeight_in_dp);
            ProgressBar progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);

            progress.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, (int) inPixels2));
            progress.setMinimumHeight((int) inPixels2);
            progress.setProgressDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.progressbar, null));
            //totalAnswer=yes +no
            //setMax(totalAnswers)
            //int x=(int)(yes/(no+totalAnswers))
            //setProgress()
            //fill progress
            progress.setProgress(70);
            progress.setMax(100);
            progress.setId(R.id.p1);
            r.addView(progress);

            TextView yesAnswers = new TextView(this);
            RelativeLayout.LayoutParams params =
                    new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_LEFT, progress.getId());
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            yesAnswers.setPadding(15, 0, 0, 0);
            //set number of yes answers
            yesAnswers.setText("15");
            r.addView(yesAnswers, params);

            TextView noAnswers = new TextView(this);
            RelativeLayout.LayoutParams params1 =
                    new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params1.addRule(RelativeLayout.ALIGN_RIGHT, progress.getId());
            params1.addRule(RelativeLayout.CENTER_IN_PARENT);
            //set number of no answers
            noAnswers.setText("15");
            noAnswers.setPadding(0, 0, 20, 0);
            r.addView(noAnswers, params1);
        }

    }
    //delete button click
    View.OnClickListener clicks = new View.OnClickListener() {

        @Override
        public void onClick(final View v) {
            // TODO Auto-generated method stub
            for (int i = 0; i < 10; i++) {
                //remove view and delete from database.
                if ((int)v.getTag() == i) {
                    ViewGroup parentView = (ViewGroup) v.getParent().getParent().getParent();
                    parentView.removeAllViews();
                    /*Intent in = getIntent();
                    in.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    in.putExtra("responses", 1);
                    startActivity(in);*/
                    Snackbar snackbar = Snackbar
                            .make(findViewById(R.id.drawer_layout), "Question has been deleted " , Snackbar.LENGTH_LONG);

                    snackbar.show();

                }
            }
        }

    };

    //get day before 1week
    public String getDateBefore1Week()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        Date newDate = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY);
        return sdf.format(calendar.getTime());

    }

    //get day before 1month

    //get all views by IDs
    public void findViewsById()
    {
        mainResponseLayout=(LinearLayout)findViewById(R.id.mainRes);
        question1Et = (EditText) findViewById(R.id.question1Et);
        question2Et = (EditText) findViewById(R.id.question2Et);
        question3Et = (EditText) findViewById(R.id.question3Et);
        aSpinner1 = (Spinner) findViewById(R.id.spinner);
        submitSurveyButton=(Button)findViewById(R.id.submitSurveyButton);
        selectTv=(TextView)findViewById(R.id.selectTv);

    }

    //Departments spinner.
    public void setUpSelectTargets()
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



        final String[] faculties = {math, physics, chemistry, tum_manag, cge, architecture, mechanical, electrical, informatics, tum_life_sc, medicine, sport, edu, political_social};
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select target Students");

        facultiesButton = (Button) findViewById(R.id.button_faculties);
        facultiesButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                final AlertDialog dialog;

                builder.setMultiChoiceItems(faculties, checked, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        //reserve the checked faculties when closing spinner
                        if (b && !selectedFaculties.contains(faculties[i]))
                        {
                            selectedFaculties.add(faculties[i]);
                            checked[i]=true;
                        }

                        else
                        {
                            selectedFaculties.remove(faculties[i]);
                            checked[i]=false;
                        }
                    }
                }).
                        //if ok show question EditText
                                setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                if(selectedFaculties.size()>0)
                                {
                                    submitSurveyButton.setVisibility(View.VISIBLE);
                                    selectTv.setVisibility(View.VISIBLE);
                                    aSpinner1.setVisibility(View.VISIBLE);
                                    question1Et.setVisibility(View.VISIBLE);
                                    aSpinner1.setSelection(0);


                                }
                                //show nothing , force him to choose Faculty
                                else
                                {
                                    submitSurveyButton.setVisibility(View.GONE);
                                    question1Et.setVisibility(View.GONE);
                                    question2Et.setVisibility(View.GONE);
                                    question3Et.setVisibility(View.GONE);
                                    selectTv.setVisibility(View.GONE);
                                    aSpinner1.setVisibility(View.GONE);


                                    Toast.makeText(getApplicationContext(),"At least select 1 Faculty",Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).
                        setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                selectedFaculties.clear();
                                for(int y=0;y<checked.length;y++)
                                {
                                    checked[y]=false;
                                }
                            }
                        });

                dialog = builder.create();
                dialog.show();

            }
        });
    }

    //get survey data (number of questions,departments,questions)
    public boolean getSurveyrData()
    {

        if(aSpinner1.getSelectedItemPosition()==0)
        {
            if(hasQuestion(question1Et))
            {
                //still userID
                question1 = question1Et.getText().toString();
                questions.add(question1);
                newDate=getDateTime();
                chosenFaculties=getSelectedFaculties(selectedFaculties);
                Toast.makeText(getApplicationContext(),"Survey has been submitted!",Toast.LENGTH_SHORT).show();
                return true;
            }
            else
                Toast.makeText(getApplicationContext(),"Please complete question form",Toast.LENGTH_SHORT).show();
        }

        else if(aSpinner1.getSelectedItemPosition()==1)
        {
            if(hasQuestion(question1Et)&&hasQuestion(question2Et))
            {
                //still userID
                question1 = question1Et.getText().toString();
                question2=question2Et.getText().toString();
                questions.add(question1);
                questions.add(question2);
                newDate=getDateTime();
                chosenFaculties=getSelectedFaculties(selectedFaculties);
                Toast.makeText(getApplicationContext(),"Survey has been submitted!",Toast.LENGTH_SHORT).show();
                return true;
            }
            else
                Toast.makeText(getApplicationContext(),"Please complete question form",Toast.LENGTH_SHORT).show();
        }

        else
        {
            if(hasQuestion(question1Et)&&hasQuestion(question2Et)&&hasQuestion(question3Et))
            {
                //still userID
                question1 = question1Et.getText().toString();
                question2=question2Et.getText().toString();
                question3=question3Et.getText().toString();
                questions.add(question1);
                questions.add(question2);
                questions.add(question3);
                newDate=getDateTime();
                chosenFaculties=getSelectedFaculties(selectedFaculties);
                Toast.makeText(getApplicationContext(),"Survey has been submitted!",Toast.LENGTH_SHORT).show();
                return true;
            }
            else
                Toast.makeText(getApplicationContext(),"Please complete question form",Toast.LENGTH_SHORT).show();

        }

        return false;
    }

    //clear data
    public void clearData()
    {
        question1Et.setText("");
        question2Et.setText("");
        question3Et.setText("");
        selectedFaculties.clear();
        chosenFaculties="";
        question1="";
        question2="";
        question3="";
        questions.clear();
        newDate="";
        for(int i=0;i<checked.length;i++)
            checked[i]=false;
        submitSurveyButton.setVisibility(View.GONE);
        question1Et.setVisibility(View.GONE);
        question2Et.setVisibility(View.GONE);
        question3Et.setVisibility(View.GONE);
        selectTv.setVisibility(View.GONE);
        aSpinner1.setVisibility(View.GONE);
        aSpinner1.setSelection(0);
    }

    //get selected faculties from spinner
    public String getSelectedFaculties(ArrayList<String> arrayList)
    {
        String facs="";
        for(int i=0;i<arrayList.size();i++ )
        {
            if(i<arrayList.size()-1)
                facs+=arrayList.get(i)+',';
            else
                facs+=arrayList.get(i);
        }

        return facs;
    }

    //submit survey listener
    public void buttonsListener()
    {
        submitSurveyButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view)
            {
                //get survey data
                if(getSurveyrData())
                {
                    //insert into database.
                    Date date = Calendar.getInstance().getTime();
                    ContentValues cv = new ContentValues(8);
                    for (int i = 0; i < Integer.parseInt(aSpinner1.getSelectedItem().toString()); i++)
                    {
                        //cv.put("date", newDate);
                        //cv.put("userID", lrzId);
                        //cv.put("question", questions.get(i));
                        //cv.put("faculties", chosenFaculties);
                        //cv.put("yes", 0);
                        //cv.put("no", 0);
                        //cv.put("flags", 0);
                        //db.insert("survey1", null, cv);
                        surveyManager.insertOwnQuestions(newDate,lrzId,questions.get(i),chosenFaculties);

                    }
                    clearData();

                    Intent i=getIntent();
                    i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(i);
                }
            }
        });
    }


    private String getDateTime()
    {
       /* Date cDate = new Date();
        String fDate = new SimpleDateFormat("yyyy-MM-dd").format(cDate);
        return fDate;*/

        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.GERMANY);
        return sdf.format(c.getTime());
    }

    //setup tab host (survey,Responses)
    public void setUpTabHost()
    {
        tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();

        // First Tab
        TabHost.TabSpec tabSpec = tabHost.newTabSpec("survey");
        tabSpec.setContent(R.id.tabAskQuestion);
        tabSpec.setIndicator("Survey");
        tabHost.addTab(tabSpec);

        // Second Tab
        tabSpec = tabHost.newTabSpec("Responses");
        tabSpec.setContent(R.id.tabSeeResponses);
        tabSpec.setIndicator("Responses");
        tabHost.addTab(tabSpec);
    }

    //check if edittext is empty
    public boolean hasQuestion(EditText et)
    {
        return !et.getText().toString().isEmpty();
    }

    //check if user is allowed to submit survey depending on the last survey date and number of question he submitted before
    @SuppressLint("SetTextI18n")
    public void userAllowed()
    {

        String weekAgo=getDateBefore1Week();
        //Cursor c = db.rawQuery("SELECT COUNT(*) FROM survey1 WHERE date >= '"+weekAgo+"'", null);
        Cursor c = surveyManager.numberOfQuestionsFrom(weekAgo);
        if(c.getCount()>0)
            c.moveToFirst();

        int x=c.getInt(0);

        if(x==0)
        {

        }

        else if(x==1)
        {
            numQues= new String[]{"1", "2"};
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, numQues);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            aSpinner1.setAdapter(adapter);
        }


        else if(x==2)
        {
            numQues= new String[]{"1"};
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, numQues);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            aSpinner1.setAdapter(adapter);
            selectTv.setText(getResources().getString(R.string.one_question_left));
        }

        else
        {
            String strDate=getNextPossibleDate();
            selectTv.setVisibility(View.VISIBLE);
            selectTv.setText(getResources().getString(R.string.next_possible_survey_date)+" " + strDate);
            facultiesButton.setVisibility(View.GONE);

        }
    }

    //spinner for number of questions selection
    public void setUpSpinner()
    {

        numQues[0]="1";
        numQues[1]="2";
        numQues[2]="3";

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, numQues);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        aSpinner1.setAdapter(adapter);

        aSpinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = (String) adapterView.getItemAtPosition(i);

                // If One
                if (selectedItem.equals("1"))
                {
                    question1Et.setVisibility(View.VISIBLE);
                    question2Et.setVisibility(View.GONE);
                    question3Et.setVisibility(View.GONE);
                    question2Et.setText("");
                    question3Et.setText("");
                }
                else if (selectedItem.equals("2"))
                {
                    question2Et.setVisibility(View.VISIBLE);
                    question3Et.setVisibility(View.GONE);
                    question3Et.setText("");
                }
                else if (selectedItem.equals("3"))
                {
                    question2Et.setVisibility(View.VISIBLE);
                    question3Et.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //show the user the next possible survey date.
    public String getNextPossibleDate()
    {
        String nextPossibleDate="";
        ArrayList<String> dates=new ArrayList<String>();
        String weekAgo=getDateBefore1Week();
        //Cursor c = db.rawQuery("SELECT date FROM survey1 WHERE date >= '"+weekAgo+"'", null);
        Cursor c = surveyManager.lastDateFromLastWeek(weekAgo);
        while(c.moveToNext())
        {
            dates.add(c.getString(0));
            nextPossibleDate=c.getString(0);
        }


        for(int i=0;i<dates.size();i++)
        {
            for(int z=0;z<nextPossibleDate.length();z++)
            {
                if(dates.get(i).charAt(z)<nextPossibleDate.charAt(z))
                    nextPossibleDate=dates.get(i);
            }
        }

        String dtStart = nextPossibleDate;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.GERMANY);
        try {
            Date date = format.parse(dtStart);
            Calendar a = Calendar.getInstance();
            a.setTime(date);
            a.add(Calendar.DAY_OF_MONTH, +7);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.GERMANY);
            nextPossibleDate = sdf.format(a.getTime());
        }
        catch (ParseException e) {
            // TODO Auto-generated catch block

        }

        return nextPossibleDate;
    }

    /*public String getDateBefore1Month()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -30);
        Date newDate = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.GERMANY);
        return sdf.format(calendar.getTime());

    }*/

}