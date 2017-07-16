package de.tum.in.tumcampusapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.PersonsDetailsActivity;
import de.tum.in.tumcampusapp.models.barrierfree.BarrierfreeContact;
import de.tum.in.tumcampusapp.models.tumo.Person;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * An adapter used to display contact information in barrierfree page.
 */
public class BarrierfreeContactAdapter extends SimpleStickyListHeadersAdapter<BarrierfreeContact>
        implements StickyListHeadersAdapter {
    public BarrierfreeContactAdapter(Context context, List<BarrierfreeContact> infos) {
        super(context, infos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        View view = convertView;
        if(view == null){
            view = getInflater().inflate(R.layout.activity_barrier_free_contact_listview, parent, false);

            // Crate UI element
            holder = new ViewHolder();
            holder.name = (TextView) view
                    .findViewById(R.id.barrierfreeContactListViewName);
            holder.phone = (TextView) view
                    .findViewById(R.id.barrierfreeContactListViewPhone);
            holder.email = (TextView) view
                    .findViewById(R.id.barrierfreeContactListViewEmail);
            holder.more = (TextView) view
                    .findViewById(R.id.barrierfreeContactListViewTumOnlineMore);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        // display information of current person
        final BarrierfreeContact contact = getInfoList().get(position);

        if(contact != null){
            holder.name.setText(contact.getName());

            holder.phone.setText(contact.getPhone(), TextView.BufferType.SPANNABLE);
            Linkify.addLinks(holder.phone, Linkify.ALL);

            holder.email.setText(contact.getEmail());

            // Has information in tumonline
            if (!contact.getTumonlineID().equals("null") && !contact.getTumonlineID().equals("")){
                // Jump to PersonDetail Activity
                holder.more.setVisibility(View.VISIBLE);
                holder.more.setText(context.getString(R.string.more_info));
                holder.more.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Person person = new Person();
                        person.setName(contact.getName());
                        person.setId(contact.getTumonlineID());

                        Bundle bundle = new Bundle();
                        bundle.putSerializable("personObject", person);

                        Intent intent = new Intent(context, PersonsDetailsActivity.class);
                        intent.putExtras(bundle);
                        context.startActivity(intent);
                    }
                });
            } else {
                holder.more.setVisibility(View.GONE);
            }
        }

        return view;
    }
    // the layout of the list
    static class ViewHolder {
        TextView name;
        TextView phone;
        TextView email;
        TextView more;
    }
}