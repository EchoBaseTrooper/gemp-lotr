package com.gempukku.lotro.cards.set31.troll;

import com.gempukku.lotro.cards.AbstractMinion;
import com.gempukku.lotro.cards.PlayConditions;
import com.gempukku.lotro.cards.TriggerConditions;
import com.gempukku.lotro.cards.actions.PlayPermanentAction;
import com.gempukku.lotro.cards.effects.choose.ChooseAndDiscardCardsFromPlayEffect;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.TwilightCostModifier;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Short Rest
 * Side: Shadow
 * Culture: Troll
 * Twilight Cost: 5
 * Type: Minion • Troll
 * Strength: 11
 * Vitality: 4
 * Site: 2
 * Game Text: Fierce. To play, discard an Orc. The twilight cost of each Troll is -2. Each time the Free Peoples
 * player discards a condition, you may discard an ally.
 */
public class Card31_064 extends AbstractMinion {
    public Card31_064() {
        super(5, 11, 4, 2, Race.TROLL, Culture.TROLL, "Bert", "Troll of Ettenmoors", true);
        addKeyword(Keyword.FIERCE);
    }
	
    @Override
    public boolean checkPlayRequirements(String playerId, LotroGame game, PhysicalCard self, int withTwilightRemoved, int twilightModifier, boolean ignoreRoamingPenalty, boolean ignoreCheckingDeadPile) {
        return super.checkPlayRequirements(playerId, game, self, withTwilightRemoved, twilightModifier, ignoreRoamingPenalty, ignoreCheckingDeadPile)
                && PlayConditions.canDiscardFromPlay(self, game, Race.ORC);
	}
	
    @Override
    public PlayPermanentAction getPlayCardAction(String playerId, LotroGame game, PhysicalCard self, int twilightModifier, boolean ignoreRoamingPenalty) {
        final PlayPermanentAction playCardAction = super.getPlayCardAction(playerId, game, self, twilightModifier, ignoreRoamingPenalty);
        playCardAction.appendCost(
                new ChooseAndDiscardCardsFromPlayEffect(playCardAction, playerId, 1, 1, Race.ORC));
        return playCardAction;
	}
	
    @Override
    public List<? extends Modifier> getAlwaysOnModifiers(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(
                new TwilightCostModifier(self, Race.TROLL, -2));
	}

    @Override
    public List<OptionalTriggerAction> getOptionalAfterTriggers(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.forEachDiscardedFromPlay(game, effectResult, CardType.CONDITION)) {
            OptionalTriggerAction action = new OptionalTriggerAction(self);
            action.appendEffect(
					new ChooseAndDiscardCardsFromPlayEffect(action, playerId, 1, 1, CardType.ALLY));
            return Collections.singletonList(action);
        }
        return null;
    }
}
