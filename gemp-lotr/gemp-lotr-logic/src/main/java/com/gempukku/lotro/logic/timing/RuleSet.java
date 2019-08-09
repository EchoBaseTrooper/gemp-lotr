package com.gempukku.lotro.logic.timing;

import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.logic.modifiers.ModifiersLogic;
import com.gempukku.lotro.logic.timing.rules.*;

public class RuleSet {
    private LotroGame _game;
    private DefaultActionsEnvironment _actionsEnvironment;
    private ModifiersLogic _modifiersLogic;

    public RuleSet(LotroGame game, DefaultActionsEnvironment actionsEnvironment, ModifiersLogic modifiersLogic) {
        _game = game;
        _actionsEnvironment = actionsEnvironment;
        _modifiersLogic = modifiersLogic;
    }

    public void applyRuleSet() {
        new RoamingRule(_modifiersLogic).applyRule();
        new EnduringRule(_modifiersLogic).applyRule();
        new HunterRule(_modifiersLogic).applyRule();

        new MusterRule(_actionsEnvironment).applyRule();

        new AmbushRule(_actionsEnvironment).applyRule();

        new ResolveSkirmishRule(_game, _actionsEnvironment).applyRule();

        new SanctuaryRule(_actionsEnvironment, _modifiersLogic).applyRule();

        new DiscardedCardRule(_actionsEnvironment).applyRule();

        new KilledCardRule(_actionsEnvironment).applyRule();

        new FrodoAndSamRule(_modifiersLogic).applyRule();

        new RingBearerRule(_modifiersLogic).applyRule();

        new ThreatRule(_actionsEnvironment).applyRule();

        new CunningRule(_modifiersLogic).applyRule();

        new ToilRule(_modifiersLogic).applyRule();

        new HealByDiscardRule(_modifiersLogic).applyRule();

        new TransferItemRule(_modifiersLogic).applyRule();

        new StatModifiersRule(_modifiersLogic).applyRule();
    }
}
