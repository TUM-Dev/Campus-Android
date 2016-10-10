package de.tum.in.tumcampusapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.models.Exam;

/**
 * Custom UI adapter for a list of exams.
 */
public class ExamListAdapter extends BaseAdapter {
    private static List<Exam> exams;
    private static final DateFormat DF = DateFormat.getDateInstance(DateFormat.MEDIUM);
    private final Context context;
    private final LayoutInflater mInflater;

    public ExamListAdapter(Context context, List<Exam> results) {
        exams = results;
        Collections.sort(exams, new Comparator<Exam>() {
            @Override
            public int compare(Exam exam, Exam other) {
                // note the "-" to get a descending ordering
                return -exam.getDate().compareTo(other.getDate());
            }
        });
        mInflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public int getCount() {
        return exams.size();
    }

    @Override
    public Object getItem(int position) {
        return exams.get(position);
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
            holder.tvName = (TextView) view.findViewById(R.id.name);
            holder.tvGrade = (TextView) view.findViewById(R.id.grade);
            holder.tvDetails1 = (TextView) view.findViewById(R.id.tv1);
            holder.tvDetails2 = (TextView) view.findViewById(R.id.tv2);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        // fill UI with data
        Exam exam = exams.get(position);
        if (exam != null) {
            holder.tvName.setText(exam.getCourse());
            holder.tvGrade.setText(exam.getGrade());
            holder.tvDetails1.setText(
                    String.format("%s: %s, " +
                                    "%s: %s, " +
                                    "%s: %s",
                            context.getString(R.string.date), DF.format(exam.getDate()),
                            context.getString(R.string.semester), exam.getSemester(),
                            context.getString(R.string.credits), exam.getCredits()));

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
    }
}
