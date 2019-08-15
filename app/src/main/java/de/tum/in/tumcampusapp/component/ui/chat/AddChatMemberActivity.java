package de.tum.in.tumcampusapp.component.ui.chat;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.ApiHelper;
import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.api.app.model.TUMCabeVerification;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatMember;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatRoom;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Allows user to search for other users which he or she can then add to the ChatRoom
 */
public class AddChatMemberActivity extends BaseActivity {
    private static final int THRESHOLD = 3; // min number of characters before getting suggestions
    private static final int DELAY = 1000; // millis after user stopped typing before getting suggestions
    private ChatRoom room;
    private TUMCabeClient tumCabeClient;
    private Pattern tumIdPattern;
    private AutoCompleteTextView searchView;

    // for delayed suggestions
    private Handler delayHandler;
    private Runnable suggestionRunnable = this::getSuggestions;

    private List<ChatMember> suggestions;

    public AddChatMemberActivity() {
        super(R.layout.activity_add_chat_member);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        suggestions = new ArrayList<>();
        delayHandler = new Handler();
        tumIdPattern = Pattern.compile(Const.TUM_ID_PATTERN);

        room = new ChatRoom(getIntent().getStringExtra(Const.CHAT_ROOM_NAME));
        room.setId(getIntent().getIntExtra(Const.CURRENT_CHAT_ROOM, -1));
        Utils.log("ChatRoom: " + room.getTitle() + " (roomId: " + room.getId() + ")");

        tumCabeClient = TUMCabeClient.getInstance(this);

        searchView = findViewById(R.id.chat_user_search);
        searchView.setThreshold(THRESHOLD);
        searchView.setAdapter(new MemberSuggestionsListAdapter(this, suggestions));

        searchView.setOnItemClickListener((adapterView, view, pos, l) -> {
            ChatMember member = (ChatMember) adapterView.getItemAtPosition(pos);
            showConfirmDialog(member);
        });

        searchView.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                Utils.log("Search");
                delayHandler.removeCallbacks(suggestionRunnable);
                getSuggestions();

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                return true;
            }
            return false;
        });

        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // do nothing, we want to know the new input -> onTextChanged
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                delayHandler.removeCallbacks(suggestionRunnable);

                if (charSequence.length() < THRESHOLD) {
                    return;
                }

                // backend call, add to adapter
                if (tumIdPattern.matcher(charSequence)
                                .matches()) {
                    // query matches TUM-ID
                    tumCabeClient.getChatMemberByLrzId(charSequence.toString(), new Callback<ChatMember>() {
                        @Override
                        public void onResponse(Call<ChatMember> call, Response<ChatMember> response) {
                            searchView.setError(null);
                            suggestions = new ArrayList<>();
                            suggestions.add(response.body());
                            ((MemberSuggestionsListAdapter) searchView.getAdapter()).updateSuggestions(suggestions);
                        }

                        @Override
                        public void onFailure(Call<ChatMember> call, Throwable t) {
                            onError();
                        }
                    });
                    return;
                }

                boolean containsDigit = false;
                for (int i = 0; i < charSequence.length(); i++) {
                    if (Character.isDigit(charSequence.charAt(i))) {
                        containsDigit = true;
                        break;
                    }
                }
                if (containsDigit) {
                    // don't try to get new suggestions (we don't autocomplete TUM-IDs)
                    if (charSequence.length() > 7) {
                        searchView.setError(getString(R.string.error_invalid_tum_id_format));
                    } else {
                        // unfinished TUM-ID
                        searchView.setError(null);
                    }
                    return;
                }

                delayHandler.postDelayed(suggestionRunnable, DELAY);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Do nothing, we do everything in onTextChanged
            }
        });

        ImageView qrCode = findViewById(R.id.join_chat_qr_code);
        qrCode.setImageBitmap(ApiHelper.createQRCode(room.getCombinedName()));
    }

    private void getSuggestions() {
        String input = searchView.getText()
                                 .toString();
        Utils.log("Get suggestions for " + input);
        tumCabeClient.searchChatMember(input, new Callback<List<ChatMember>>() {
            @Override
            public void onResponse(@NonNull Call<List<ChatMember>> call,
                                   @NonNull Response<List<ChatMember>> response) {
                searchView.setError(null);
                suggestions = response.body();
                ((MemberSuggestionsListAdapter) searchView.getAdapter()).updateSuggestions(suggestions);
            }

            @Override
            public void onFailure(@NonNull Call<List<ChatMember>> call, @NonNull Throwable t) {
                onError();
            }
        });
    }

    private void onError() {
        searchView.setError(getString(R.string.error_user_not_found));
    }

    private void showConfirmDialog(ChatMember member) {
        String message = getString(R.string.add_user_to_chat_message,
                member.getDisplayName(), room.getTitle());
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(R.string.add, (dialogInterface, i) -> {
                    joinRoom(member);
                    reset();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners_background);
        }

        dialog.show();
    }

    /**
     * Clears everything from the last search.
     */
    private void reset() {
        suggestions = new ArrayList<>();
        ((MemberSuggestionsListAdapter) searchView.getAdapter()).updateSuggestions(suggestions);
        searchView.setText("");
    }

    private void joinRoom(ChatMember member) {
        TUMCabeVerification verification = TUMCabeVerification.create(this, null);
        if (verification == null) {
            Utils.showToast(this, R.string.error);
            return;
        }

        TUMCabeClient.getInstance(this)
                .addUserToChat(room, member, verification, new Callback<ChatRoom>() {
                    @Override
                    public void onResponse(@NonNull Call<ChatRoom> call,
                                           @NonNull Response<ChatRoom> response) {
                        ChatRoom room = response.body();
                        if (room != null) {
                            TcaDb.Companion.getInstance(getBaseContext())
                                           .chatRoomDao()
                                           .updateMemberCount(room.getMembers(), room.getId());
                            Utils.showToast(getBaseContext(), R.string.chat_member_added);
                        } else {
                            Utils.showToast(getBaseContext(), R.string.error_something_wrong);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ChatRoom> call, @NonNull Throwable t) {
                        Utils.showToast(getBaseContext(), R.string.error);
                    }
                });
    }

}
