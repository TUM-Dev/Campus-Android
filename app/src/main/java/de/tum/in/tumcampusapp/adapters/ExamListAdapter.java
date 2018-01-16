package de.tum.in.tumcampusapp.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Collections;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.models.tumo.Exam;
import de.tum.in.tumcampusapp.models.tumo.LecturesSearchRow;

/**
 * Custom UI adapter for a list of exams.
 */
public class ExamListAdapter extends SimpleStickyListHeadersAdapter<Exam> {
    private static final DateFormat DF = DateFormat.getDateInstance(DateFormat.MEDIUM);

    public ExamListAdapter(Context context, List<Exam> results)  {
        super(context, results);
        Collections.sort(infoList);
    }

    @Override
    String genenrateHeaderName(Exam item) {
        String headerText = super.genenrateHeaderName(item);
        int year = Integer.parseInt(headerText.substring(0, 2));
        if(headerText.charAt(2) == 'W'){
            return context.getString(R.string.winter_semester, year, year+1);
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
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        // fill UI with data
        Exam exam = infoList.get(position);
        if (exam != null) {
            holder.tvName.setText(exam.getCourse());
            holder.tvGrade.setText(exam.getGrade());
            setGradeBackground(holder, exam.getGrade());

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

    private void setGradeBackground(ViewHolder holder, String grade){
        //if(holder.gradeBackground instanceof ShapeDrawable){
            //Drawable bg = (ShapeDrawable) holder.gradeBackground;
            //Resources res = holder.tvGrade.getResources();
            TextView view = holder.tvGrade;

            // TODO find elegant/better solution that only needs one file
            switch (grade){
                case "1,0": view.setBackgroundResource(R.drawable.grade_1_0);
                    break;
                case "1,3":
                case "1,4": view.setBackgroundResource(R.drawable.grade_1_3);
                    break;
                case "1,7": view.setBackgroundResource(R.drawable.grade_1_7);
                    break;
                case "2,0": view.setBackgroundResource(R.drawable.grade_2_0);
                    break;
                case "2,3":
                case "2,4": view.setBackgroundResource(R.drawable.grade_2_3);
                    break;
                case "2,7": view.setBackgroundResource(R.drawable.grade_2_7);
                    break;
                case "3,0": view.setBackgroundResource(R.drawable.grade_3_0);
                    break;
                case "3,3":
                case "3,4": view.setBackgroundResource(R.drawable.grade_3_3);
                    break;
                case "3,7": view.setBackgroundResource(R.drawable.grade_3_7);
                    break;
                case "4,0": view.setBackgroundResource(R.drawable.grade_4_0);
                    break;
                case "4,3":
                case "4,4": view.setBackgroundResource(R.drawable.grade_4_3);
                    break;
                case "4,7": view.setBackgroundResource(R.drawable.grade_4_7);
                    break;
                case "5,0": view.setBackgroundResource(R.drawable.grade_5_0);
                    break;
                default: view.setBackgroundResource(R.drawable.grade_background);
                    break;
            }
        //}
    }

    static class ViewHolder {
        TextView tvDetails1;
        TextView tvDetails2;
        TextView tvGrade;
        TextView tvName;
    }
}
