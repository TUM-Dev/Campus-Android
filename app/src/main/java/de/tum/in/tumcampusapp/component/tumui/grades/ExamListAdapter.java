package de.tum.in.tumcampusapp.component.tumui.grades;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.collect.ImmutableMap;

import java.text.DateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.adapter.SimpleStickyListHeadersAdapter;
import de.tum.in.tumcampusapp.component.tumui.grades.model.Exam;

/**
 * Custom UI adapter for a list of exams.
 */
public class ExamListAdapter extends SimpleStickyListHeadersAdapter<Exam> {
    private static final DateFormat DF = DateFormat.getDateInstance(DateFormat.MEDIUM);
    private static final Map<String, Integer> GRADE_COLOR = ImmutableMap.<String, Integer>builder()
            .put("1,0", R.color.grade_1_0)
            .put("1,3", R.color.grade_1_3)
            .put("1,4", R.color.grade_1_3)
            .put("1,7", R.color.grade_1_7)
            .put("2,0", R.color.grade_2_0)
            .put("2,3", R.color.grade_2_3)
            .put("2,4", R.color.grade_2_3)
            .put("2,7", R.color.grade_2_7)
            .put("3,0", R.color.grade_3_0)
            .put("3,3", R.color.grade_3_3)
            .put("3,4", R.color.grade_3_3)
            .put("3,7", R.color.grade_3_7)
            .put("4,0", R.color.grade_4_0)
            .put("4,3", R.color.grade_4_3)
            .put("4,4", R.color.grade_4_3)
            .put("4,7", R.color.grade_4_7)
            .put("5,0", R.color.grade_5_0)
            .build();

    public ExamListAdapter(Context context, List<Exam> results) {
        super(context, results);
        Collections.sort(infoList);
    }

    @Override
    public String genenrateHeaderName(Exam item) {
        String headerText = super.genenrateHeaderName(item);
        int year = Integer.parseInt(headerText.substring(0, 2));
        if (headerText.charAt(2) == 'W') {
            return context.getString(R.string.winter_semester, year, year + 1);
        } else {
            return context.getString(R.string.summer_semester, year);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        View view = convertView;

        // find and init UI
        if (view == null) {
            view = mInflater.inflate(R.layout.activity_grades_listview, parent, false);
            holder = new ViewHolder();
            holder.tvName = view.findViewById(R.id.name);
            holder.tvGrade = view.findViewById(R.id.grade);
            holder.tvDetails1 = view.findViewById(R.id.tv1);
            holder.tvDetails2 = view.findViewById(R.id.tv2);
            holder.gradeBackground = context.getResources().getDrawable(R.drawable.grade_background);
            holder.tvGrade.setBackground(holder.gradeBackground);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        // fill UI with data
        Exam exam = infoList.get(position);
        if (exam != null) {
            holder.tvName.setText(exam.getCourse());
            holder.tvGrade.setText(exam.getGrade());
            if (GRADE_COLOR.containsKey(exam.getGrade())) {
                holder.gradeBackground.setTint(
                        context.getResources().getColor(GRADE_COLOR.get(exam.getGrade())));
            } else {
                holder.gradeBackground.setTint(
                        context.getResources().getColor(R.color.grade_default));
            }

            holder.tvDetails1.setText(
                    String.format("%s: %s, ",
                                  context.getString(R.string.date), DF.format(exam.getDate())));

            holder.tvDetails2
                    .setText(String.format("%s: %s, " +
                                           "%s: %s",
                                           context.getString(R.string.examiner), exam.getExaminer(),
                                           context.getString(R.string.mode), exam.getModus()));
        }

        return view;
    }

    static class ViewHolder {
        TextView tvDetails1;
        TextView tvDetails2;
        TextView tvGrade;
        TextView tvName;
        Drawable gradeBackground;
    }
}
