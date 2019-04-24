package de.tum.in.tumcampusapp.component.tumui.grades;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Collections;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.adapter.SimpleStickyListHeadersAdapter;
import de.tum.in.tumcampusapp.component.tumui.grades.model.Exam;

/**
 * Custom UI adapter for a list of exams.
 */
public class ExamListAdapter extends SimpleStickyListHeadersAdapter<Exam> {
    private static final DateTimeFormatter DF = DateTimeFormat.mediumDate();

    ExamListAdapter(Context context, List<Exam> results) {
        super(context, results);
        Collections.sort(infoList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        View view = convertView;

        if (view == null) {
            view = mInflater.inflate(R.layout.activity_grades_listview, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        Exam exam = infoList.get(position);
        if (exam != null) {
            holder.nameTextView.setText(exam.getCourse());
            holder.gradeTextView.setText(exam.getGrade());

            int gradeColor = exam.getGradeColor(context);
            holder.gradeTextView.getBackground().setTint(gradeColor);

            String date;
            if (exam.getDate() == null) {
                date = context.getString(R.string.not_specified);
            } else {
                date = DF.print(exam.getDate());
            }
            holder.examDateTextView.setText(String.format(
                    "%s: %s", context.getString(R.string.date), date));

            holder.additionalInfoTextView.setText(String.format("%s: %s, %s: %s",
                    context.getString(R.string.examiner), exam.getExaminer(),
                    context.getString(R.string.mode), exam.getModus()));
        }

        return view;
    }

    @Override
    public String generateHeaderName(Exam item) {
        String headerText = super.generateHeaderName(item);
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

    static class ViewHolder {

        TextView nameTextView;
        TextView gradeTextView;
        TextView examDateTextView;
        TextView additionalInfoTextView;

        public ViewHolder(View itemView) {
            nameTextView = itemView.findViewById(R.id.courseNameTextView);
            gradeTextView = itemView.findViewById(R.id.gradeTextView);
            examDateTextView = itemView.findViewById(R.id.examDateTextView);
            additionalInfoTextView = itemView.findViewById(R.id.additionalInfoTextView);
        }

    }
}
