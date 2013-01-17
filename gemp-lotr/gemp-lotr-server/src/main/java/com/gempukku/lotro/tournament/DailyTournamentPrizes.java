package com.gempukku.lotro.tournament;

import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.DefaultCardCollection;

public class DailyTournamentPrizes implements TournamentPrizes {
    private String _registryRepresentation;

    public DailyTournamentPrizes(String registryRepresentation) {
        _registryRepresentation = registryRepresentation;
    }

    @Override
    public CardCollection getPrizeForTournament(PlayerStanding playerStanding, int playersCount) {
        DefaultCardCollection tournamentPrize = new DefaultCardCollection();
        if (playerStanding.getStanding() == 1) {
            tournamentPrize.addItem("(S)Booster Choice", 4);
        } else if (playerStanding.getStanding() == 2) {
            tournamentPrize.addItem("(S)Booster Choice", 3);
        } else if (playerStanding.getStanding() == 3 ||
                playerStanding.getStanding() == 4) {
            tournamentPrize.addItem("(S)Booster Choice", 2);
        }

        if (tournamentPrize.getAll().size() == 0)
            return null;
        return tournamentPrize;
    }

    @Override
    public String getRegistryRepresentation() {
        return _registryRepresentation;
    }

    @Override
    public String getPrizeDescription() {
        return "<div class='prizeHint' value='1st place - 4 boosters, 2nd place - 3 boosters, 3rd and 4th place - 2 boosters each'>4-3-2-2</div>";
    }
}
