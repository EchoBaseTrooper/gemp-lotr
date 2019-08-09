package com.gempukku.lotro.cards.set2.dwarven;
import java.util.List;
import java.util.Collections;

import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.effects.SelfDiscardEffect;
import com.gempukku.lotro.logic.modifiers.condition.LocationCondition;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.TwilightCostModifier;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.Collections;
import java.util.List;

/**
 * Set: Mines of Moria
 * Side: Free
 * Culture: Dwarven
 * Twilight Cost: 0
 * Type: Condition
 * Game Text: To play, spot a Dwarf. Plays to your support area. At a site which is not underground, each [MORIA] Orc's
 * twilight cost is +2. Discard this condition during the regroup phase.
 */
public class Card2_008 extends AbstractPermanent {
    public Card2_008() {
        super(Side.FREE_PEOPLE, 0, CardType.CONDITION, Culture.DWARVEN, "Golden Light on the Land");
    }

    @Override
    public boolean checkPlayRequirements(String playerId, LotroGame game, PhysicalCard self, int withTwilightRemoved, int twilightModifier, boolean ignoreRoamingPenalty, boolean ignoreCheckingDeadPile) {
        return super.checkPlayRequirements(playerId, game, self, withTwilightRemoved, twilightModifier, ignoreRoamingPenalty, ignoreCheckingDeadPile)
                && Filters.canSpot(game, Race.DWARF);
    }

    @Override
    public List<? extends Modifier> getAlwaysOnModifiers(LotroGame game, PhysicalCard self) {
return Collections.singletonList(new TwilightCostModifier(self,
Filters.and(Culture.MORIA, Race.ORC),
new LocationCondition(Filters.not(Keyword.UNDERGROUND)), 2));
}

    @Override
    public List<RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.startOfPhase(game, effectResult, Phase.REGROUP)) {
            RequiredTriggerAction action = new RequiredTriggerAction(self);
            action.appendEffect(
                    new SelfDiscardEffect(self));
            return Collections.singletonList(action);
        }
        return null;
    }
}
