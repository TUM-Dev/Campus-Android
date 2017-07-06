package de.tum.in.tumcampusapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.PersonsDetailsActivity;
import de.tum.in.tumcampusapp.auxiliary.HTMLStringBuffer;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.barrierfree.BarrierfreeContact;
import de.tum.in.tumcampusapp.models.tumo.Person;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * An adapter used to display contact information in barrierfree page.
 */
public class BarrierfreeContactAdapter extends BaseAdapter implements StickyListHeadersAdapter {
    private static List<BarrierfreeContact> contacts;
    private final List<String> headerIdIndex;
    private final LayoutInflater inflater;

    private Context context;

    public static BarrierfreeContactAdapter newAdapter(Context context, List<BarrierfreeContact> contactPersons){
        contacts = contactPersons;
        return new BarrierfreeContactAdapter(context);
    }

    private BarrierfreeContactAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        this.context = context;

        //// TODO: 7/5/2017 Use a better way to create header id
        headerIdIndex = new ArrayList<>();
        for (BarrierfreeContact contact : contacts) {
            String item = contact.getFaculty();
            if (!headerIdIndex.contains(item)) {
                headerIdIndex.add(item);
            }
        }
    }

    @Override
    public int getCount() {
        return contacts.size();
    }

    @Override
    public Object getItem(int position) {
        return contacts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        View view = convertView;
        if(view == null){
            view = inflater.inflate(R.layout.activity_barrier_free_contact_listview, parent, false);

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
        final BarrierfreeContact contact = contacts.get(position);

        if(contact != null){
            holder.name.setText(contact.getName());

//            HTMLStringBuffer contentText = new HTMLStringBuffer();

//            contentText.appendField(context.getString(R.string.mobile_phone), contact.getPhone());
//            holder.phone.setText(Utils.fromHtml(contentText.toString()),
//                    TextView.BufferType.SPANNABLE);

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

    // Header view
    @Override
    public View getHeaderView(int position, View convertView, ViewGroup partent) {
        HeaderViewHolder holder;
        View view = convertView;

        if (view == null) {
            holder = new HeaderViewHolder();
            view = inflater.inflate(R.layout.header, partent, false);
            holder.text = (TextView) view.findViewById(R.id.lecture_header);
            view.setTag(holder);
        } else {
            holder = (HeaderViewHolder) view.getTag();
        }
        //set header text as first char in name
        String headerText = contacts.get(position).getFaculty();
        holder.text.setText(headerText);

        return view;
    }

    @Override
    public long getHeaderId(int i) {
        // return faculty as id for header
        return headerIdIndex.indexOf(contacts.get(i).getFaculty());
    }

    // the layout of the list
    static class ViewHolder {
        TextView name;
        TextView phone;
        TextView email;
        TextView more;
    }

    static class HeaderViewHolder {
        TextView text;
    }
}