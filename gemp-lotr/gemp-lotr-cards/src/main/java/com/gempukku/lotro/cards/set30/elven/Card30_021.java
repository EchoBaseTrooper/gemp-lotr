package com.gempukku.lotro.cards.set30.elven;

import com.gempukku.lotro.cards.AbstractAlly;
import com.gempukku.lotro.cards.PlayConditions;
import com.gempukku.lotro.cards.TriggerConditions;
import com.gempukku.lotro.cards.effects.SelfExertEffect;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.decisions.IntegerAwaitingDecision;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.effects.ChooseAndHealCharactersEffect;
import com.gempukku.lotro.logic.effects.HealCharactersEffect;
import com.gempukku.lotro.logic.effects.PlayoutDecisionEffect;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.Collections;
import java.util.List;

/**
 * Set: Main Deck
 * Side: Free
 * Culture: Elven
 * Twilight Cost: 4
 * Type: Ally • Home 3 • Elf
 * Strength: 8
 * Vitality: 4
 * Site: 3
 * Game Text: Wise. At the start of each of your turns, you may heal a Wise ally (or heal Elrond twice).
 * Regroup: Exert Elrond twice to heal a companion.
 */
public class Card30_021 extends AbstractAlly {
    public Card30_021() {
        super(4, Block.HOBBIT, 3, 8, 4, Race.ELF, Culture.ELVEN, "Elrond", "Herald to Gil-galad", true);
		addKeyword(Keyword.WISE);
    }

    @Override
    public List<OptionalTriggerAction> getOptionalAfterTriggers(final String playerId, LotroGame game, EffectResult effectResult, final PhysicalCard self) {
        if (TriggerConditions.startOfTurn(game, effectResult)) {
            final OptionalTriggerAction action = new OptionalTriggerAction(self);
            action.appendCost(
                    new ChooseActiveCardEffect(self, playerId, "Choose a Wise ally", Filters.and(CardType.ALLY, Keyword.WISE), Filters.canHeal) {
                @Override
                protected void cardSelected(LotroGame game, final PhysicalCard card) {
                    action.appendEffect(
							new HealCharactersEffect(self, card));
                }
            });
            
            return Collections.singletonList(action);
        }
        return null;
    }

    @Override
    protected List<? extends Action> getExtraInPlayPhaseActions(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.REGROUP, self)
                && PlayConditions.canExert(self, game, 2, self)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new SelfExertEffect(action, self));
            action.appendCost(
                    new SelfExertEffect(action, self));
            action.appendEffect(
                    new ChooseAndHealCharactersEffect(action, playerId, CardType.COMPANION));
            return Collections.singletonList(action);
        }
        return null;
    }
}
