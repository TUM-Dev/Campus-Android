package de.tum.in.tumcampusapp.component.ui.ticket;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import de.tum.in.tumcampusapp.api.tumonline.CacheControl;
import de.tum.in.tumcampusapp.component.ui.overview.card.Card;
import de.tum.in.tumcampusapp.component.ui.overview.card.ProvidesCard;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.component.ui.ticket.repository.EventsLocalRepository;

public class EventCardsProvider implements ProvidesCard {

    private final Context context;

    private final EventsLocalRepository localRepository;

    @Inject
    public EventCardsProvider(Context context, EventsLocalRepository localRepository) {
        this.context = context;
        this.localRepository = localRepository;
    }

    public void setDismissed(int id) {
        localRepository.setDismissed(id);
    }

    @NotNull
    @Override
    public List<Card> getCards(@NonNull CacheControl cacheControl) {
        List<Card> results = new ArrayList<>();

        // Add the next upcoming event that is not the next kino event
        Event event = localRepository.getNextEventWithoutMovie();
        if (event != null) {
            EventCard eventCard = new EventCard(context);
            eventCard.setEvent(event);
            results.add(eventCard);
        }

        return results;
    }

}

