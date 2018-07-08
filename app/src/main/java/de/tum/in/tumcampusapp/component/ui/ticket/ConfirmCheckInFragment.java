package de.tum.in.tumcampusapp.component.ui.ticket;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.component.ui.ticket.payload.TicketValidityResponse;
import de.tum.in.tumcampusapp.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmCheckInFragment extends BottomSheetDialogFragment {

    public static ConfirmCheckInFragment newInstance(String eventId, String code) {
        ConfirmCheckInFragment fragment = new ConfirmCheckInFragment();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        args.putString("code", code);
        fragment.setArguments(args);
        return fragment;
    }

    private ProgressBar progressBar;
    private TextView nameTextView;
    private AppCompatButton confirmButton;
    private AppCompatButton denyButton;

    private String eventId;
    private String code;
    private TicketValidityResponse ticketValidityResponse;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            code = getArguments().getString("code");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_confirm_check_in, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nameTextView = view.findViewById(R.id.name_text_view);
        progressBar = view.findViewById(R.id.progress_bar);

        confirmButton = view.findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(v -> confirmTicket());

        denyButton = view.findViewById(R.id.deny_button);
        denyButton.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onStart() {
        super.onStart();

        TUMCabeClient
                .getInstance(getContext())
                .getTicketValidity(eventId, code, new Callback<TicketValidityResponse>() {
                    @Override
                    public void onResponse(Call<TicketValidityResponse> call, Response<TicketValidityResponse> response) {
                        ticketValidityResponse = response.body();

                        ticketValidityResponse.valid = true;
                        ticketValidityResponse.firstName = "Rick";
                        ticketValidityResponse.lastName = "Schuber";
                        ticketValidityResponse.tumID = "TUM ID: ga123qir";
                        ticketValidityResponse.purchaseDate = "Purchase Date: May 16, 2018";
                        ticketValidityResponse.redeemDate = "Redeem Date: July 2, 2018";
                        ticketValidityResponse.status = "Status: Not yet checked in";

                        if (ticketValidityResponse == null) {
                            closeWithErrorMessage();
                            return;
                        }

                        nameTextView.setText(ticketValidityResponse.getTicketInfo());
                        nameTextView.setVisibility(View.VISIBLE);

                        progressBar.setVisibility(View.GONE);

                        confirmButton.setVisibility(View.VISIBLE);

                        denyButton.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onFailure(Call<TicketValidityResponse> call, Throwable t) {
                        closeWithErrorMessage();
                    }
                });
    }

    public void confirmTicket() {

    }

    private void closeWithErrorMessage() {
        Utils.showToast(getContext(), "Server war Schuld");
        dismiss();
    }

}
