package de.tum.in.tumcampusapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.BaseActivity;


public class BarrierFreeInfoActivity extends BaseActivity {

    private Button button1;

    public BarrierFreeInfoActivity(){
        super(R.layout.activity_barrier_free_info);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ListView listView = (ListView) findViewById(R.id.activity_barrier_free_list_view);


        String[] values = new String[]{
                "Contact",
                "Nearby Facilities",
                "General Information"
        };

        ListAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, values);
        listView.setAdapter(adapter);

        button1 = (Button) findViewById(R.id.BFTestButton);
        button1.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getBaseContext(), BarrierFreeContactActivity.class);
                startActivity(intent);
            }
         }
        );
    }

}
