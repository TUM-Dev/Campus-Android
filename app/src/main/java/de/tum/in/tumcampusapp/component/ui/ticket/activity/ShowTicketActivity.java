package de.tum.in.tumcampusapp.component.ui.ticket.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Writer;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.joda.time.DateTime;

import java.util.List;

import javax.inject.Inject;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.other.generic.adapter.EqualSpacingItemDecoration;
import de.tum.in.tumcampusapp.component.ui.ticket.adapter.BoughtTicketAdapter;
import de.tum.in.tumcampusapp.component.ui.ticket.di.TicketsModule;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Ticket;
import de.tum.in.tumcampusapp.component.ui.ticket.model.TicketInfo;
import de.tum.in.tumcampusapp.component.ui.ticket.model.TicketType;
import de.tum.in.tumcampusapp.component.ui.ticket.repository.EventsLocalRepository;
import de.tum.in.tumcampusapp.component.ui.ticket.repository.TicketsLocalRepository;
import de.tum.in.tumcampusapp.component.ui.ticket.repository.TicketsRemoteRepository;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class ShowTicketActivity extends BaseActivity {

    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView locationTextView;
    private ImageView ticketQrCodeImageView;
    private TextView titleTextView;
    private TextView dateTextView;
    private TextView redemptionStateTextView;
    private RecyclerView ticketAmounts;

    private List<TicketInfo> ticketInfoList;
    private Event event;

    @Inject
    EventsLocalRepository eventsLocalRepo;

    @Inject
    TicketsRemoteRepository ticketsRemoteRepo;

    @Inject
    TicketsLocalRepository ticketsLocalRepo;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public ShowTicketActivity() {
        super(R.layout.activity_show_ticket);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);

        int eventId = getIntent().getIntExtra(Const.KEY_EVENT_ID, 0);
        getInjector().ticketsComponent()
                .ticketsModule(new TicketsModule())
                .eventId(eventId)
                .build()
                .inject(this);

        initViews();
        loadTicketData(eventId);
        setViewData();

        showQRCode();
        setWindowBrightnessToFull();
    }

    private void showQRCode() {
        String qrCodeContent = ticketInfoList.get(0).getTickets().get(0).getCode();
        Utils.log(qrCodeContent);
        createQRCode(qrCodeContent);
    }

    private void initViews() {
        titleTextView = findViewById(R.id.ticket_event_title);
        locationTextView = findViewById(R.id.ticket_event_location);
        dateTextView = findViewById(R.id.ticket_event_date_time);
        redemptionStateTextView = findViewById(R.id.ticket_event_redemption_state);
        ticketQrCodeImageView = findViewById(R.id.ticket_qr_code);
        ticketAmounts = findViewById(R.id.ticket_event_ticket_list);

        ticketAmounts.setLayoutManager(new LinearLayoutManager(this));
        ticketAmounts.setHasFixedSize(true);
        ticketAmounts.setNestedScrollingEnabled(false);
        int spacing = Math.round(getResources().getDimension(R.dimen.material_tiny_padding));
        ticketAmounts.addItemDecoration(new EqualSpacingItemDecoration(spacing));

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.color_primary,
                R.color.tum_A100,
                R.color.tum_A200
        );
        swipeRefreshLayout.setOnRefreshListener(this::loadRedemptionStatus);
    }

    private void loadRedemptionStatus() {
        Disposable d = ticketsRemoteRepo
                .fetchTickets()
                .subscribe(this::handleTicketRefreshSuccess, t -> handleTicketRefreshFailure());
        compositeDisposable.add(d);
    }

    private void handleTicketRefreshSuccess(List<Ticket> tickets) {
        ticketsLocalRepo.insert(tickets.toArray(new Ticket[0]));
        this.ticketInfoList = ticketsLocalRepo.getTicketsByEventId(event.getId());
        setViewData();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void handleTicketRefreshFailure() {
        Utils.showToast(this, R.string.error_something_wrong);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void loadTicketData(int eventId) {
        List<TicketType> ticketTypes = ticketsLocalRepo.getTicketTypesByEventId(eventId);
        if (ticketTypes.isEmpty()) {
            ticketsLocalRepo.addTicketTypes(ticketsRemoteRepo.fetchTicketTypesForEvent(eventId).blockingSingle());
        }
        ticketInfoList = ticketsLocalRepo.getTicketsByEventId(eventId);
        event = eventsLocalRepo.getEventById(eventId);
    }

    private void setViewData() {
        titleTextView.setText(event.getTitle());
        dateTextView.setText(event.getFormattedStartDateTime(this));
        redemptionStateTextView.setText(getRedemptionState());

        locationTextView.setText(event.getLocality());
        locationTextView.setOnClickListener(this::showMap);

        ticketAmounts.setAdapter(new BoughtTicketAdapter(ticketInfoList));
    }

    private String getRedemptionState() {
        DateTime lastRedemption = null;
        int nrTicketsRedeemed = 0;
        for (TicketInfo t : ticketInfoList) {
            if (t.getTickets().get(0).getRedemption() != null) {
                nrTicketsRedeemed++;
                if (lastRedemption == null || t.getTickets().get(0).getRedemption().isAfter(lastRedemption)) {
                    lastRedemption = t.getTickets().get(0).getRedemption();
                }
            }
        }
        String redemptionState;
        String formattedDateTime = "";
        if (lastRedemption != null) {
            formattedDateTime = Ticket.Companion.getFormattedRedemptionDate(this, lastRedemption);
        }
        if (nrTicketsRedeemed == 0) {
            redemptionState = getString(R.string.not_redeemed_yet);
        } else if (nrTicketsRedeemed < ticketInfoList.size()) {
            redemptionState = getString(R.string.partially_redeemed, nrTicketsRedeemed, ticketInfoList.size(), formattedDateTime);
        } else {
            redemptionState = getString(R.string.redeemed_at, formattedDateTime);
        }
        return redemptionState;
    }

    private void showMap(View view) {
        TextView textView = (TextView) view;
        String url = "http://maps.google.co.in/maps?q=" + textView.getText();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void setWindowBrightnessToFull() {
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        window.setAttributes(lp);
    }

    private void createQRCode(String text) {
        Writer multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(
                    text, BarcodeFormat.QR_CODE, 200, 200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            ticketQrCodeImageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Utils.log(e);
            Utils.showToast(this, R.string.error_something_wrong);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }
}


