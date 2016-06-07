package de.tum.in.tumcampusapp.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.common.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.BaseActivity;
import de.tum.in.tumcampusapp.auxiliary.AuthenticationManager;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.ChatRoom;
import de.tum.in.tumcampusapp.models.Faculty;
import de.tum.in.tumcampusapp.models.Question;
import de.tum.in.tumcampusapp.models.TUMCabeClient;
import de.tum.in.tumcampusapp.models.managers.CalendarManager;
import de.tum.in.tumcampusapp.models.managers.SurveyManager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class SurveyActivity extends BaseActivity {

    Spinner aSpinner1;
    TextView selectTv;
    TabHost tabHost;
    Button submitSurveyButton , facultiesButton;
    ArrayList<String> questions=new ArrayList<>();
    ArrayList<String> selectedFaculties = new ArrayList<>();
    boolean[]checked=new boolean[14];
    LinearLayout mainResponseLayout,questionsLayout;
    String chosenFaculties="",newDate="",lrzId;
    //private SQLiteDatabase db;

    String[] numQues=new String[3];
    private SurveyManager surveyManager;

    public SurveyActivity(){super(R.layout.activity_survey);}



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
                            .make(findViewById(R.id.drawer_layout), getResources().getString(R.string.question_deleted) , Snackbar.LENGTH_LONG);

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
        aSpinner1 = (Spinner) findViewById(R.id.spinner);
        submitSurveyButton=(Button)findViewById(R.id.submitSurveyButton);
        selectTv=(TextView)findViewById(R.id.selectTv);
        questionsLayout=(LinearLayout)findViewById(R.id.questionsEts);

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
        builder.setTitle(getResources().getString(R.string.quiz_target_faculty));

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
                                    aSpinner1.setSelection(0);


                                }
                                //show nothing , force him to choose Faculty
                                else
                                {
                                    submitSurveyButton.setVisibility(View.GONE);
                                    selectTv.setVisibility(View.GONE);
                                    aSpinner1.setVisibility(View.GONE);


                                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.select_one_faculty),Toast.LENGTH_SHORT).show();
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
        int numberOfQuestion=aSpinner1.getSelectedItemPosition()+1;
        boolean done=true;
        for(int i=0;i<numberOfQuestion;i++)
        {   EditText v=(EditText)questionsLayout.findViewWithTag("question"+(i+1));
            if(hasQuestion(v))
            {
                questions.add(v.getText().toString());
            }

            else
            {
                done=false;
                questions.clear();
                break;
            }
        }

        if(done)
        {
            newDate=getDateTime();
            chosenFaculties=getSelectedFaculties(selectedFaculties);
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.survey_submitted),Toast.LENGTH_SHORT).show();
            return true;
        }
        else
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.complete_question_form),Toast.LENGTH_SHORT).show();

        return false;
    }

    //clear data
    public void clearData()
    {
        selectedFaculties.clear();
        chosenFaculties="";
        questions.clear();
        newDate="";
        for(int i=0;i<checked.length;i++)
            checked[i]=false;
        submitSurveyButton.setVisibility(View.GONE);
        selectTv.setVisibility(View.GONE);
        aSpinner1.setVisibility(View.GONE);
        aSpinner1.setSelection(0);
        questionsLayout.removeAllViews();
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


                //Log.e("JOINED",questions.toString());


                if(getSurveyrData())
                {
                    //insert into database.
                    Date date = Calendar.getInstance().getTime();
                    ContentValues cv = new ContentValues(8);
                    for (int i = 0; i < aSpinner1.getSelectedItemPosition()+1; i++)
                    {

                        Log.i("selectedFaculties",selectedFaculties.toString());
                        String facIDs = "1,2,3"; // only for testing
                        Log.i("selectedFacultiesResul",facIDs);
                        Question ques = new Question(questions.get(i),facIDs);
                        Log.e("Test_deviceID",AuthenticationManager.getDeviceID(getApplicationContext()));
                        try {
                            TUMCabeClient.getInstance(getApplicationContext()).createQuestion(ques,new Callback<Question>(){

                                @Override
                                public void success(Question question, Response response) {
                                    Log.e("Test_resp","Succeeded: "+response.getBody().toString());
                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    Log.e("Test_resp","Failure");
                                }
                            });

                            //TUMCabeClient.getInstance(this).createQuestion("testquestion",new int[]{1,2});
                        }catch (Exception e){
                            Log.e("Test_exception",e.toString());
                        }



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
        TabHost.TabSpec tabSpec = tabHost.newTabSpec(getResources().getString(R.string.tab_survey));
        tabSpec.setContent(R.id.tabAskQuestion);
        tabSpec.setIndicator(getResources().getString(R.string.tab_survey));
        tabHost.addTab(tabSpec);

        // Second Tab
        tabSpec = tabHost.newTabSpec(getResources().getString(R.string.tab_responses));
        tabSpec.setContent(R.id.tabSeeResponses);
        tabSpec.setIndicator(getResources().getString(R.string.tab_responses));
        tabHost.addTab(tabSpec);
    }

    //check if edittext is empty
    public boolean hasQuestion(EditText et)
    {
        return !et.getText().toString().isEmpty();
    }


    // Not done yet: supposed to fetch the Faculty data from the API, compare with the selectedFaculties from the user and return a string with the selectedFacultyNUMBERS
    // Current problem: can't return the value in onPostExecute, need to save in db then intent & broadcastreceiver upon saving the data
    /*public String downloadFaculties(ArrayList<String> selectedFaculties){
        new AsyncTask<ArrayList<String>, Void, String>(){

            @Override
            protected String doInBackground(ArrayList<String>... arrayLists) {
                ArrayList<String> selectedFaculties = arrayLists[0];
                final ArrayList<String> results = new ArrayList<String>();
                ArrayList<Faculty> facultiesFromServer = TUMCabeClient.getInstance(getApplicationContext()).getFaculties();
                for (int j = 0; j < arrayLists.length; j++) {
                    for (int i = 0; i < facultiesFromServer.size(); i++) {
                        if (selectedFaculties.get(j).equals(facultiesFromServer.get(i).getName())){
                            results.add(facultiesFromServer.get(i).getId());
                            Log.i("FacutyID",facultiesFromServer.get(i).getId());
                        }
                    }
                }
                return TextUtils.join(",",results);
            }

            @Override
            protected void onPostExecute(String result) {
                return ;
            }


        }.execute(selectedFaculties);

        return "";
    }*/

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

        if(x<3)
        {
            numQues=new String[3-x];
            for(int i=0;i<numQues.length;i++) {
                numQues[i]= String.valueOf(i+1);
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, numQues);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            aSpinner1.setAdapter(adapter);
            if(x==2)
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
                int numberOfQuestions = adapterView.getSelectedItemPosition();
                questionsLayout.removeAllViews();

                for(int y=0; y<=numberOfQuestions; y++)
                {
                    EditText questionEt = new EditText(getApplicationContext());
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    questionEt.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.color_primary));
                    questionEt.setInputType(EditorInfo.TYPE_CLASS_TEXT);
                    params.setMargins(0, 30, 0, 0);
                    questionEt.setFocusable(true);
                    questionEt.setLayoutParams(params);
                    questionEt.setHint(getResources().getString(R.string.enter_question_survey)+" "+(y+1));
                    questionEt.setTag("question"+(y+1));
                    questionsLayout.addView(questionEt);
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