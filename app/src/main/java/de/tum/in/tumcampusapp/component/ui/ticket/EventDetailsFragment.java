package de.tum.in.tumcampusapp.component.ui.ticket;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.component.ui.tufilm.show_ticket;
import de.tum.in.tumcampusapp.utils.Const;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Fragment for EventDetails. Manages content that gets shown on the pagerView
 */
public class EventDetailsFragment extends Fragment {

    private Context context;
    private Event event;
    private String url; // link to homepage
    private LayoutInflater inflater;

    private boolean booked = true;

    private final CompositeDisposable disposable = new CompositeDisposable();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        View rootView = inflater.inflate(R.layout.fragment_kinodetails_section, container, false);
        LinearLayout root = rootView.findViewById(R.id.layout);

        // position in database
        int position = getArguments().getInt(Const.POSITION);

        context = root.getContext();

        event = EventsController.getEvents().get(position);
        showDetails(root);

        // TODO: set booked if the user has already bought a ticket

        return rootView;
    }

    /**
     * creates the content of the fragment
     *
     * @param rootView view on which the content gets drawn
     */
    private void showDetails(LinearLayout rootView) {
        url = event.getLink();

        createKinoHeader(rootView);
        createKinoFooter(rootView);
    }

    private void addToRoot(LinearLayout rootView, int headerId, CharSequence contentString) {
        View view = inflater.inflate(R.layout.list_header_big, rootView, false);
        TextView text = view.findViewById(R.id.list_header);
        text.setText(headerId);
        rootView.addView(view);
        view = inflater.inflate(R.layout.kino_content, rootView, false);
        text = view.findViewById(R.id.line_name);
        text.setText(contentString);
        rootView.addView(view);
    }

    private void addToRootWithPadding(LinearLayout rootView, int headerId, CharSequence contentString) {
        View view = inflater.inflate(R.layout.list_header_big, rootView, false);
        TextView text = view.findViewById(R.id.list_header);
        text.setText(headerId);
        rootView.addView(view);
        view = inflater.inflate(R.layout.kino_content, rootView, false);
        text = view.findViewById(R.id.line_name);
        text.setText(contentString);
        // padding is done programmatically here because we need more padding at the end
        int padding = (int) context.getResources()
                                   .getDimension(R.dimen.padding_kino);
        int paddingRight = (int) context.getResources()
                                        .getDimension(R.dimen.padding_kino_right);
        int paddingEnd = (int) context.getResources()
                                      .getDimension(R.dimen.padding_kino_end);
        text.setPadding(padding, padding, paddingRight, paddingEnd);
        rootView.addView(view);
    }

    private void createKinoFooter(LinearLayout root) {
        addToRoot(root, R.string.date, event.getDate().toString());
        addToRoot(root, R.string.location, event.getLocality());
        addToRootWithPadding(root, R.string.description, event.getDescription());
    }

    private void createKinoHeader(LinearLayout rootView) {
        LinearLayout headerView = (LinearLayout) inflater.inflate(R.layout.event_header, rootView, false);

        // initialize all buttons
        Button link = headerView.findViewById(R.id.button_link);
        Button ticket = headerView.findViewById(R.id.button_ticket);
        ImageView cover = headerView.findViewById(R.id.kino_cover);
        ProgressBar progress = headerView.findViewById(R.id.kino_cover_progress);
        View error = headerView.findViewById(R.id.kino_cover_error);

        // onClickListeners
        link.setOnClickListener(view -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))));

        // Setup "Buy/Show ticket" button according to ticket status for current event
        if (booked){
            ticket.setText("Show ticket");
            ticket.setOnClickListener(view -> showTicket());
        } else{
            ticket.setText("Buy ticket");
            ticket.setOnClickListener(view -> buyTicket());
        }

        // cover
        Picasso.get()
                .load(event.getImage())
                .into(cover, new Callback() {
                    @Override
                    public void onSuccess() {
                        progress.setVisibility(View.GONE);
                        error.setVisibility(View.GONE);
                    }
                    @Override
                    public void onError(Exception e) {
                        progress.setVisibility(View.GONE);
                        error.setVisibility(View.VISIBLE);
                    }
                });

        rootView.addView(headerView);
    }
    //open show_ticket activity, and transfer current movie data to it.
    //The String data is just a example, it should be get from correspongding movie which now show on the screen.
    //time and place of movie  is fixed. Only the movie title and date should be transfered to show_ticket activity
    private void showTicket(){
        String data = "KingsMan 08.05 " + "\n"+ "Filmbegin: 20:00 o'clock "+"\n"+ " 1. Stock, Hörsaal 1200 (Carl-von-Linde-Hörsaal) Arcisstraße 21";
        Intent intent = new Intent(getActivity().getApplicationContext(), show_ticket.class);
        intent.putExtra("movie_data", data);
        startActivity(intent);
    }

    private void buyTicket(){
        // TODO: go to payment activity (to be implemented)
    }

    /**
     * formats the dateString
     *
     * @param date Date string stored in database
     * @return formated dateString
     */
    private static CharSequence formDateString(String date) {
        return date.substring(8, 10) + '.' + date.substring(5, 7) + '.';
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.clear();
    }
}
