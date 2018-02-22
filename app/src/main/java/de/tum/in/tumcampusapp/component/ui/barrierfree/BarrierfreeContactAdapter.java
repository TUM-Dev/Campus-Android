package de.tum.in.tumcampusapp.component.ui.barrierfree;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.adapter.SimpleStickyListHeadersAdapter;
import de.tum.in.tumcampusapp.component.tumui.person.PersonsDetailsActivity;
import de.tum.in.tumcampusapp.component.tumui.person.model.Person;
import de.tum.in.tumcampusapp.component.ui.barrierfree.model.BarrierfreeContact;
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

        if (view == null) {
            // Crate UI element
            view = getInflater().inflate(R.layout.activity_barrier_free_contact_listview, parent, false);
            holder = new ViewHolder(view, parent);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        // display information of current person
        final BarrierfreeContact contact = getInfoList().get(position);

        if (!contact.isValid()) {
            view.setVisibility(View.GONE);
            return view;
        }

        // set Information
        view.setVisibility(View.VISIBLE);
        holder.setContent(contact);

        return view;
    }

    // the layout of the list
    class ViewHolder {
        TextView name;
        TextView phone;
        TextView email;
        TextView more;

        ViewHolder(View view, ViewGroup parent) {

            name = view.findViewById(R.id.barrierfreeContactListViewName);
            phone = view.findViewById(R.id.barrierfreeContactListViewPhone);
            email = view.findViewById(R.id.barrierfreeContactListViewEmail);
            more = view.findViewById(R.id.barrierfreeContactListViewTumOnlineMore);
        }

        void setContent(final BarrierfreeContact contact) {
            name.setText(contact.getName());
            phone.setText(contact.getTelephone(), TextView.BufferType.SPANNABLE);
            Linkify.addLinks(phone, Linkify.ALL);
            email.setText(contact.getEmail());

            // Has information in tumonline
            if (!contact.isHavingTumID()) {
                more.setVisibility(View.GONE);
            } else {
                // Jump to PersonDetail Activity
                more.setVisibility(View.VISIBLE);
                more.setText(context.getString(R.string.more_info));
                more.setOnClickListener(v -> {
                    Person person = new Person();
                    person.setName(contact.getName());
                    person.setId(contact.getTumID());

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("personObject", person);

                    Intent intent = new Intent(context, PersonsDetailsActivity.class);
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                });
            }
        }
    }
}