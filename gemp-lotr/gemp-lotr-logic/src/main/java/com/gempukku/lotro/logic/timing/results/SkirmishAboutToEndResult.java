package com.gempukku.lotro.logic.timing.results;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SkirmishAboutToEndResult extends EffectResult {
    private Set<PhysicalCard> _minionsInvolved;

    public SkirmishAboutToEndResult(Set<PhysicalCard> minionsInvolved) {
        super(EffectResult.Type.SKIRMISH_ABOUT_TO_END);
	_minionsInvolved = minionsInvolved;
    }

    public Set<PhysicalCard> getMinionsInvolved() {
        return _minionsInvolved;
    }
}
