package de.tum.in.tumcampusapp.activities;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.BaseActivity;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.ChatMember;
import de.tum.in.tumcampusapp.models.managers.DatabaseManager;


public class Survey2 extends BaseActivity implements View.OnClickListener {

    int numberOfquestions;
    EditText question1Et,question2Et,question3Et;
    Spinner aSpinner1;
    TextView selectTv,tv111;
    TabHost tabHost;
    Button submitSurveyButton , facultiesButton;
    ArrayList<String> questions=new ArrayList<>();
    ArrayList<String> selectedFaculties = new ArrayList<>();
    boolean[]checked=new boolean[14];
    ProgressBar[] progressBars=new ProgressBar[12];
    TextView [] yesAnswers=new TextView[12];
    TextView [] noAnswers=new TextView[12];
    TextView [] userQuestions=new TextView[12];
    Button [] deleteQuestion=new Button[12];
    String question1="",question2="",question3="",chosenFaculties="",newDate="",lrzId,dayBeforeWeek="",dayBeforeMonth="";
    private SQLiteDatabase db;
    Toolbar main;
    private final String table_name="survey";
    String[] numQues=new String[3];

    public Survey2(){super(R.layout.activity_survey2);}


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
        db = DatabaseManager.getDb(getApplicationContext());
        db.execSQL("CREATE TABLE IF NOT EXISTS survey1 (id INTEGER PRIMARY KEY AUTOINCREMENT, date VARCHAR,userID VARCHAR, question TEXT, faculties TEXT, "
                + "yes INTEGER,  no INTEGER, flags INTEGER)");
        setUpResponseTab();
        userAllowed();

    }


    public void setUpResponseTab()
    {
        dayBeforeMonth=getDateBefore1Month();
        Cursor c = db.rawQuery("SELECT * FROM survey1 WHERE date >= '"+dayBeforeMonth+"'", null);
        numberOfquestions=c.getCount();
        for(int i=0;i<numberOfquestions;i++)
        {
            c.moveToNext();
            userQuestions[i].setVisibility(View.VISIBLE);
            userQuestions[i].setText(c.getString(3));
            deleteQuestion[i].setVisibility(View.VISIBLE);
            deleteQuestion[i].setTag(c.getInt(0));
            yesAnswers[i].setVisibility(View.VISIBLE);
            yesAnswers[i].setText(c.getString(5)+"");
            noAnswers[i].setVisibility(View.VISIBLE);
            noAnswers[i].setText(c.getString(6)+"");
            progressBars[i].setVisibility(View.VISIBLE);
            progressBars[i].setProgress((c.getInt(5)));

        }
    }

    @Override
    public void onClick(final View v)
    {
            for(int i=0;i<numberOfquestions;i++)
            {
                if (v.getId() == getResources().getIdentifier("deleteQ" +(i+ 1), "id", getPackageName()))
                {
                    final int y=i;
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                    builder1.setMessage("Are you sure you delete this question");
                    builder1.setCancelable(true);

                    builder1.setPositiveButton(
                            "Yes",
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int id)
                                {
                                    ViewGroup parentView = (ViewGroup) v.getParent();
                                    parentView.removeAllViews();
                                    parentView.setVisibility(View.GONE);
                                    parentView=(ViewGroup)yesAnswers[y].getParent();
                                    parentView.removeAllViews();
                                    parentView.setVisibility(View.GONE);
                                    db.execSQL(("Delete  FROM survey1 WHERE id = '"+v.getTag()+"'"));
                                    Intent i=getIntent();
                                    i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    i.putExtra("responses",1);
                                    startActivity(i);


                                }
                            });

                    builder1.setNegativeButton(
                            "No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id)
                                {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();

                }
            }
        }

    public String getDateBefore1Week()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        Date newDate = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strDate = sdf.format(calendar.getTime());
        return strDate;
    }

    public String getDateBefore1Month()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -30);
        Date newDate = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strDate = sdf.format(calendar.getTime());
        return strDate;
    }

    public void findViewsById()
    {
        question1Et = (EditText) findViewById(R.id.question1Et);
        question2Et = (EditText) findViewById(R.id.question2Et);
        question3Et = (EditText) findViewById(R.id.question3Et);
        aSpinner1 = (Spinner) findViewById(R.id.spinner);
        submitSurveyButton=(Button)findViewById(R.id.submitSurveyButton);
        selectTv=(TextView)findViewById(R.id.selectTv);
        progressBars[0]=(ProgressBar)findViewById(R.id.q1Progress);
        progressBars[1]=(ProgressBar)findViewById(R.id.q2Progress);
        progressBars[2]=(ProgressBar)findViewById(R.id.q3Progress);
        progressBars[3]=(ProgressBar)findViewById(R.id.q4Progress);
        progressBars[4]=(ProgressBar)findViewById(R.id.q5Progress);
        progressBars[5]=(ProgressBar)findViewById(R.id.q6Progress);
        progressBars[6]=(ProgressBar)findViewById(R.id.q7Progress);
        progressBars[7]=(ProgressBar)findViewById(R.id.q8Progress);
        progressBars[8]=(ProgressBar)findViewById(R.id.q9Progress);
        progressBars[9]=(ProgressBar)findViewById(R.id.q10Progress);
        progressBars[10]=(ProgressBar)findViewById(R.id.q11Progress);
        progressBars[11]=(ProgressBar)findViewById(R.id.q12Progress);

        userQuestions[0]=(TextView)findViewById(R.id.question1TV);
        userQuestions[1]=(TextView)findViewById(R.id.question2TV);
        userQuestions[2]=(TextView)findViewById(R.id.question3TV);
        userQuestions[3]=(TextView)findViewById(R.id.question4TV);
        userQuestions[4]=(TextView)findViewById(R.id.question5TV);
        userQuestions[5]=(TextView)findViewById(R.id.question6TV);
        userQuestions[6]=(TextView)findViewById(R.id.question7TV);
        userQuestions[7]=(TextView)findViewById(R.id.question8TV);
        userQuestions[8]=(TextView)findViewById(R.id.question9TV);
        userQuestions[9]=(TextView)findViewById(R.id.question10TV);
        userQuestions[10]=(TextView)findViewById(R.id.question11TV);
        userQuestions[11]=(TextView)findViewById(R.id.question12TV);

        yesAnswers[0]=(TextView)findViewById(R.id.q1Yes);
        yesAnswers[1]=(TextView)findViewById(R.id.q2Yes);
        yesAnswers[2]=(TextView)findViewById(R.id.q3Yes);
        yesAnswers[3]=(TextView)findViewById(R.id.q4Yes);
        yesAnswers[4]=(TextView)findViewById(R.id.q5Yes);
        yesAnswers[5]=(TextView)findViewById(R.id.q6Yes);
        yesAnswers[6]=(TextView)findViewById(R.id.q7Yes);
        yesAnswers[7]=(TextView)findViewById(R.id.q8Yes);
        yesAnswers[8]=(TextView)findViewById(R.id.q9Yes);
        yesAnswers[9]=(TextView)findViewById(R.id.q10Yes);
        yesAnswers[10]=(TextView)findViewById(R.id.q11Yes);
        yesAnswers[11]=(TextView)findViewById(R.id.q12Yes);

        noAnswers[0]=(TextView)findViewById(R.id.q1No);
        noAnswers[1]=(TextView)findViewById(R.id.q2No);
        noAnswers[2]=(TextView)findViewById(R.id.q3No);
        noAnswers[3]=(TextView)findViewById(R.id.q4No);
        noAnswers[4]=(TextView)findViewById(R.id.q5No);
        noAnswers[5]=(TextView)findViewById(R.id.q6No);
        noAnswers[6]=(TextView)findViewById(R.id.q7No);
        noAnswers[7]=(TextView)findViewById(R.id.q8No);
        noAnswers[8]=(TextView)findViewById(R.id.q9No);
        noAnswers[9]=(TextView)findViewById(R.id.q10No);
        noAnswers[10]=(TextView)findViewById(R.id.q11No);
        noAnswers[11]=(TextView)findViewById(R.id.q12No);

        deleteQuestion[0]=(Button)findViewById(R.id.deleteQ1);
        deleteQuestion[1]=(Button)findViewById(R.id.deleteQ2);
        deleteQuestion[2]=(Button)findViewById(R.id.deleteQ3);
        deleteQuestion[3]=(Button)findViewById(R.id.deleteQ4);
        deleteQuestion[4]=(Button)findViewById(R.id.deleteQ5);
        deleteQuestion[5]=(Button)findViewById(R.id.deleteQ6);
        deleteQuestion[6]=(Button)findViewById(R.id.deleteQ7);
        deleteQuestion[7]=(Button)findViewById(R.id.deleteQ8);
        deleteQuestion[8]=(Button)findViewById(R.id.deleteQ9);
        deleteQuestion[9]=(Button)findViewById(R.id.deleteQ10);
        deleteQuestion[10]=(Button)findViewById(R.id.deleteQ11);
        deleteQuestion[11]=(Button)findViewById(R.id.deleteQ12);


    }

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
                            setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i)
                                {
                                   if(selectedFaculties.size()>0)
                                   {
                                        submitSurveyButton.setVisibility(View.VISIBLE);
                                        question1Et.setVisibility(View.VISIBLE);
                                        selectTv.setVisibility(View.VISIBLE);
                                        aSpinner1.setVisibility(View.VISIBLE);
                                        aSpinner1.setSelection(0);

                                   }
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

    public void buttonsListener()
    {
        submitSurveyButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view)
            {
                if(getSurveyrData())
                {
                    Date date = Calendar.getInstance().getTime();
                    ContentValues cv = new ContentValues(8);
                    for (int i = 0; i < Integer.parseInt(aSpinner1.getSelectedItem().toString()); i++)
                    {
                        cv.put("date", newDate);
                        cv.put("userID", lrzId);
                        cv.put("question", questions.get(i));
                        cv.put("faculties", chosenFaculties);
                        cv.put("yes", 0);
                        cv.put("no", 0);
                        cv.put("flags", 0);
                        db.insert("survey1", null, cv);

                    }
                    clearData();

                    Intent i=getIntent();
                    i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(i);
                }

                  /*
                    **sqlite**
                    insert question,date,counter(to manage user number of questions)
                    constraint(3questions/week)

                   **phpmyadmin**
                    add to question to database
                    table(QID auto increment,userID ,date(Date),question(String),faculties(String),Yes(int),no(int),flags(int)
                                        $sql = "INSERT INTO Survey (userID,date,question,faculties,0,0,0)
                    VALUES ()";
                    phpscript(clean database->check questions expiry
                    constraints(clean after 28days,delete questions after 5flags)
                */
                //db.execSQL("INSERT INTO survey (id, date, userID,question,faculties,yes,no,flags) VALUES ("+1+','+1+','+newDate+','+question1+','+chosenFaculties+','+5+','+5+','+0+')');

            }
        });
    }

    private String getDateTime()
    {
       /* Date cDate = new Date();
        String fDate = new SimpleDateFormat("yyyy-MM-dd").format(cDate);
        return fDate;*/

        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strDate = sdf.format(c.getTime());
        return strDate;
    }

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

    public boolean hasQuestion(EditText et)
    {
        if(!et.getText().toString().isEmpty())
            return true;
        return false;
    }

    public void userAllowed()
    {

        String weekAgo=getDateBefore1Week();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM survey1 WHERE date >= '"+weekAgo+"'", null);
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
            selectTv.setText("Only 1 question left");
        }

        else
        {
                String strDate=getNextPossibleDate();
                selectTv.setVisibility(View.VISIBLE);
                selectTv.setText("Next possible survey date is "+strDate);
                facultiesButton.setVisibility(View.GONE);

        }
    }

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
                    question2Et.setVisibility(View.GONE);
                    question2Et.setText("");
                    question3Et.setVisibility(View.GONE);
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

    public String getNextPossibleDate()
    {
        String nextPossibleDate="";
        ArrayList<String> dates=new ArrayList<String>();
        String weekAgo=getDateBefore1Week();
        Cursor c = db.rawQuery("SELECT date FROM survey1 WHERE date >= '"+weekAgo+"'", null);
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
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = format.parse(dtStart);
            Calendar a = Calendar.getInstance();
            a.setTime(date);
            a.add(Calendar.DAY_OF_MONTH, +7);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            nextPossibleDate = sdf.format(a.getTime());
        }
        catch (ParseException e) {
            // TODO Auto-generated catch block

        }

        return nextPossibleDate;
    }

}
