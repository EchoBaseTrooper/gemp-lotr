package com.gempukku.lotro.draft;

import com.gempukku.lotro.game.CardCollection;
import com.gempukku.polling.LongPollableResource;
import com.gempukku.polling.WaitingRequest;

public class DraftCommunicationChannel implements LongPollableResource {
    private int _channelNumber;

    private String _cardChoiceOnClient;
    private volatile boolean _changed;
    private volatile long _lastAccessed;
    private volatile WaitingRequest _waitingRequest;

    public DraftCommunicationChannel(int channelNumber) {
        _channelNumber = channelNumber;
    }

    public int getChannelNumber() {
        return _channelNumber;
    }

    public long getLastAccessed() {
        return _lastAccessed;
    }

    private void updateLastAccess() {
        _lastAccessed = System.currentTimeMillis();
    }

    @Override
    public synchronized void deregisterRequest(WaitingRequest waitingRequest) {
        _waitingRequest = null;
    }

    @Override
    public synchronized boolean registerRequest(WaitingRequest waitingRequest) {
        if (_changed)
            return true;

        _waitingRequest = waitingRequest;
        return false;
    }

    @Override
    public void requestWaitingNotification() {
        updateLastAccess();
    }

    public synchronized void draftChanged() {
        _changed = true;
        if (_waitingRequest != null) {
            _waitingRequest.processRequest();
            _waitingRequest = null;
        }
    }

    public boolean hasChangesInCommunicationChannel(DraftCardChoice draftCardChoice) {
        updateLastAccess();

        CardCollection cardCollection = draftCardChoice.getCardCollection();
        if (cardCollection == null)
            return _cardChoiceOnClient != null;
        return  !getSerialized(cardCollection).equals(_cardChoiceOnClient);
    }

    private String getSerialized(CardCollection cardCollection) {
        StringBuilder sb = new StringBuilder();
        for (CardCollection.Item collectionItem : cardCollection.getAll().values())
            sb.append(collectionItem.getCount()+"x"+collectionItem.getBlueprintId()+"|");

        return sb.toString();
    }

    public synchronized void processCommunicationChannel(DraftCardChoice draftCardChoice, CardCollection chosenCards, DraftChannelVisitor draftChannelVisitor) {
        updateLastAccess();

        CardCollection cardCollection = draftCardChoice.getCardCollection();
        if (cardCollection != null) {
            draftChannelVisitor.timeLeft(draftCardChoice.getTimeLeft());
            draftChannelVisitor.cardChoice(cardCollection);
            _cardChoiceOnClient = getSerialized(cardCollection);
        } else {
            draftChannelVisitor.noCardChoice();
            _cardChoiceOnClient = null;
        }
        draftChannelVisitor.chosenCards(chosenCards);

        _changed = false;
    }
}
