package com.gempukku.lotro.at;

import com.gempukku.lotro.common.Token;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NewCardsAtTest extends AbstractAtTest {
    @Test
    public void exertAsCost() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardImpl gimli = new PhysicalCardImpl(100, "1_13", P1, _library.getLotroCardBlueprint("1_13"));
        PhysicalCardImpl inquisitor = new PhysicalCardImpl(100, "1_268", P2, _library.getLotroCardBlueprint("1_268"));

        _game.getGameState().addCardToZone(_game, gimli, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(_game, inquisitor, Zone.SHADOW_CHARACTERS);

        _game.getGameState().addTokens(gimli, Token.WOUND, 2);

        skipMulligans();

        // Pass in fellowship
        playerDecided(P1, "");

        // Play inquisitor
        playerDecided(P2, "0");
        playerDecided(P2, "");

        // Pass in maneuver
        playerDecided(P1, "");
        playerDecided(P2, "");

        // Pass in archery
        playerDecided(P1, "");
        playerDecided(P2, "");

        // Pass in assignment
        playerDecided(P1, "");
        playerDecided(P2, "");

        // Assign minion to Frodo
        playerDecided(P1, gimli.getCardId() + " " + inquisitor.getCardId());

        // Start skirmish
        playerDecided(P1, String.valueOf(gimli.getCardId()));

        assertEquals(0, ((String[]) _userFeedback.getAwaitingDecision(P1).getDecisionParameters().get("actionId")).length);
    }

    @Test
    public void reduceArcheryTotal() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardImpl legolas = new PhysicalCardImpl(100, "40_52", P1, _library.getLotroCardBlueprint("40_52"));
        PhysicalCardImpl arrowsOfLight = new PhysicalCardImpl(100, "40_33", P1, _library.getLotroCardBlueprint("40_33"));
        PhysicalCardImpl inquisitor = new PhysicalCardImpl(100, "1_268", P2, _library.getLotroCardBlueprint("1_268"));

        _game.getGameState().addCardToZone(_game, inquisitor, Zone.SHADOW_CHARACTERS);

        _game.getGameState().addCardToZone(_game, legolas, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(_game, arrowsOfLight, Zone.HAND);

        skipMulligans();

        // Pass in fellowship
        playerDecided(P1, "");

        // Pass in shadow
        playerDecided(P2, "");

        // Pass in maneuver
        playerDecided(P1, "");
        playerDecided(P2, "");

        // Play ArrowsOfLight
        playerDecided(P1, "0");
        playerDecided(P1, "0");
    }

    @Test
    public void playedTrigger() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardImpl bruinenUnleashed = new PhysicalCardImpl(100, "40_37", P1, _library.getLotroCardBlueprint("40_37"));
        PhysicalCardImpl legolas = new PhysicalCardImpl(100, "40_52", P1, _library.getLotroCardBlueprint("40_52"));
        PhysicalCardImpl nazgul = new PhysicalCardImpl(100, "40_211", P2, _library.getLotroCardBlueprint("40_211"));

        _game.getGameState().addCardToZone(_game, legolas, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(_game, bruinenUnleashed, Zone.SUPPORT);
        _game.getGameState().addCardToZone(_game, nazgul, Zone.HAND);

        skipMulligans();

        _game.getGameState().setTwilight(10);

        // Pass in fellowship
        playerDecided(P1, "");

        // Pass in shadow
        playerDecided(P2, "0");

        assertEquals(1, _game.getGameState().getWounds(nazgul));
    }
}
