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

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Writer;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.joda.time.DateTime;

import java.util.List;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.api.app.exception.NoPrivateKey;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.ui.ticket.EventsController;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Ticket;
import de.tum.in.tumcampusapp.component.ui.ticket.model.TicketType;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShowTicketActivity extends BaseActivity {

    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView locationTextView;
    private ImageView ticketQrCodeImageView;
    private TextView titleTextView;
    private TextView dateTextView;
    private TextView priceTextView;
    private TextView redemptionStateTextView;

    private EventsController eventsController;

    private List<Ticket> tickets;
    private Event event;
    private TicketType ticketType;

    public ShowTicketActivity() {
        super(R.layout.activity_show_ticket);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);

        initViews();
        loadTicketData();
        setViewData();

        showQRCode(tickets.size());
        setWindowBrightnessToFull();
    }

    private void showQRCode(int nrOfTickets) {
        StringBuilder qrCodeContent = new StringBuilder();

        for (int i = 0; i < nrOfTickets; i++) {
            qrCodeContent.append(tickets.get(i).getCode());
            // don't add semicolon to last item
            if (i != nrOfTickets - 1) {
                qrCodeContent.append(';');
            }
        }
        createQRCode(qrCodeContent.toString());
    }

    private void initViews() {
        titleTextView = findViewById(R.id.ticket_event_title);
        locationTextView = findViewById(R.id.ticket_event_location);
        dateTextView = findViewById(R.id.ticket_event_date_time);
        priceTextView = findViewById(R.id.ticket_event_price);
        redemptionStateTextView = findViewById(R.id.ticket_event_redemption_state);
        ticketQrCodeImageView = findViewById(R.id.ticket_qr_code);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.color_primary,
                R.color.tum_A100,
                R.color.tum_A200
        );
        swipeRefreshLayout.setOnRefreshListener(this::loadRedemptionStatus);
    }

    private void loadRedemptionStatus() {
        try {
            TUMCabeClient.getInstance(this)
                    .fetchTickets(this, new Callback<List<Ticket>>() {
                        @Override
                        public void onResponse(Call<List<Ticket>> call, Response<List<Ticket>> response) {
                            List<Ticket> ticket = response.body();
                            if (response.isSuccessful() && !ticket.isEmpty()) {
                                handleTicketRefreshSuccess(tickets);
                            } else {
                                handleTicketRefreshFailure();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Ticket>> call, Throwable t) {
                            Utils.log(t);
                            handleTicketRefreshFailure();
                        }
                    });
        } catch (NoPrivateKey e) {
            Utils.log(e);
        }
    }

    private void handleTicketRefreshSuccess(List<Ticket> tickets) {
        this.tickets = tickets;
        eventsController.insert(tickets.toArray(new Ticket[0]));

        setViewData();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void handleTicketRefreshFailure() {
        Utils.showToast(this, R.string.error_something_wrong);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void loadTicketData() {
        eventsController = new EventsController(this);
        int eventId = getIntent().getIntExtra(Const.KEY_EVENT_ID, 0);

        tickets = eventsController.getTicketsByEventId(eventId);
        event = eventsController.getEventById(eventId);
        ticketType = eventsController.getTicketTypeById(tickets.get(0).getTicketTypeId());
    }

    private void setViewData() {
        titleTextView.setText(getString(R.string.xtickets, tickets.size(), event.getTitle()));
        dateTextView.setText(event.getFormattedStartDateTime(this));
        priceTextView.setText(getString(R.string.price_per_ticket, Utils.formatPrice(ticketType.getPrice())));
        redemptionStateTextView.setText(getRedemptionState());

        locationTextView.setText(event.getLocality());
        locationTextView.setOnClickListener(this::showMap);
    }

    private String getRedemptionState() {
        DateTime lastRedemption = null;
        int nrTicketsRedeemed = 0;
        for(Ticket t : tickets) {
            if (t.getRedemption() != null) {
                nrTicketsRedeemed++;
                if (lastRedemption == null || t.getRedemption().isAfter(lastRedemption)) {
                    lastRedemption = t.getRedemption();
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
        } else if (nrTicketsRedeemed < tickets.size()) {
            redemptionState = getString(R.string.partially_redeemed, nrTicketsRedeemed, tickets.size(), formattedDateTime);
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

}


