package com.gempukku.lotro.cards.set20.sauron;

import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.modifiers.PlayersCantUseCardPhaseSpecialAbilitiesModifier;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.Skirmish;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.ModifiersQuerying;

import java.util.Collections;
import java.util.List;

/**
 * 3
 * Orc Skulker
 * Minion • Orc
 * 9	3	6
 * Lurker.
 * Companions of the same culture as the companion assigned to skirmish this minion may not use skirmish special abilities.
 * http://lotrtcg.org/coreset/sauron/orcskulker(r1).png
 */
public class Card20_370 extends AbstractMinion {
    public Card20_370() {
        super(3, 9, 3, 6, Race.ORC, Culture.SAURON, "Orc Skulker");
        addKeyword(Keyword.LURKER);
    }

    @Override
    public List<? extends Modifier> getAlwaysOnModifiers(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(new PlayersCantUseCardPhaseSpecialAbilitiesModifier(self, Phase.SKIRMISH,
                CardType.COMPANION,
                new Filter() {
                    @Override
                    public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                        final Skirmish skirmish = game.getGameState().getSkirmish();
                        if (skirmish != null) {
                            final PhysicalCard fellowshipCharacter = skirmish.getFellowshipCharacter();
                            if (fellowshipCharacter != null)
                                if (fellowshipCharacter.getBlueprint().getCardType() == CardType.COMPANION)
                                    return fellowshipCharacter.getBlueprint().getCulture() == physicalCard.getBlueprint().getCulture();
                        }

                        return false;
                    }
                }));
    }
}
