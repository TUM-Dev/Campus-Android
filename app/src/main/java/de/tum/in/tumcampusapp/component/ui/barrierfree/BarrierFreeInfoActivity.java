package de.tum.in.tumcampusapp.component.ui.barrierfree;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;

public class BarrierFreeInfoActivity extends BaseActivity {
    ListView listView;

    public BarrierFreeInfoActivity() {
        super(R.layout.activity_barrier_free_info);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listView = findViewById(R.id.activity_barrier_free_list_view);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent;
            switch (position) {
                case 0:
                    intent = new Intent(BarrierFreeInfoActivity.this, BarrierFreeContactActivity.class);
                    break;
                case 1:
                    intent = new Intent(BarrierFreeInfoActivity.this, BarrierFreeFacilitiesActivity.class);
                    break;
                case 2:
                    intent = new Intent(BarrierFreeInfoActivity.this, BarrierFreeMoreInfoActivity.class);
                    break;
                default:
                    intent = new Intent();
            }
            startActivity(intent);
        });
    }

}
