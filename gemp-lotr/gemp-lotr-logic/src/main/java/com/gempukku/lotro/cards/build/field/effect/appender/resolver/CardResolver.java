package com.gempukku.lotro.cards.build.field.effect.appender.resolver;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.appender.DelayedAppender;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.ChooseActiveCardsEffect;
import com.gempukku.lotro.logic.effects.ChooseArbitraryCardsEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseCardsFromDeckEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseCardsFromDiscardEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseCardsFromHandEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseStackedCardsEffect;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class CardResolver {
    public static EffectAppender resolveStackedCards(String type, ValueSource countSource, FilterableSource stackedOn,
                                                     String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveStackedCards(type, null, countSource, stackedOn, memory, choicePlayer, choiceText, environment);
    }

    public static EffectAppender resolveStackedCards(String type, FilterableSource additionalFilter, ValueSource countSource, FilterableSource stackedOn,
                                                     String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveStackedCards(type, additionalFilter, additionalFilter, countSource, stackedOn, memory, choicePlayer, choiceText, environment);
    }

    public static EffectAppender resolveStackedCards(String type, FilterableSource choiceFilter, FilterableSource playabilityFilter, ValueSource countSource, FilterableSource stackedOn,
                                                     String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        if (type.startsWith("memory(") && type.endsWith(")")) {
            Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource = new Function<ActionContext, Iterable<? extends PhysicalCard>>() {
                @Override
                public Iterable<? extends PhysicalCard> apply(ActionContext actionContext) {
                    final Filterable stackedOnFilter = stackedOn.getFilterable(actionContext);
                    return Filters.filterActive(actionContext.getGame(), stackedOnFilter);
                }
            };
            return resolveMemoryCards(type, choiceFilter, playabilityFilter, countSource, memory, cardSource);
        } else if (type.startsWith("choose(") && type.endsWith(")")) {
            final String filter = type.substring(type.indexOf("(") + 1, type.lastIndexOf(")"));
            final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
            final PlayerSource playerSource = PlayerResolver.resolvePlayer(choicePlayer, environment);
            return new DelayedAppender() {
                @Override
                public boolean isPlayableInFull(ActionContext actionContext) {
                    final int min = countSource.getMinimum(actionContext);
                    final Filterable filterable = filterableSource.getFilterable(actionContext);
                    final Filterable stackedOnFilter = stackedOn.getFilterable(actionContext);

                    Filterable additionalFilterable = Filters.any;
                    if (playabilityFilter != null)
                        additionalFilterable = playabilityFilter.getFilterable(actionContext);

                    List<PhysicalCard> choice = new LinkedList<>();

                    final LotroGame game = actionContext.getGame();
                    for (PhysicalCard stackedOn : Filters.filterActive(game, stackedOnFilter)) {
                        final List<PhysicalCard> stackedCards = game.getGameState().getStackedCards(stackedOn);
                        choice.addAll(Filters.filter(stackedCards, game, filterable, additionalFilterable));
                    }

                    return choice.size() >= min;
                }

                @Override
                protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                    final Filterable filterable = filterableSource.getFilterable(actionContext);
                    final Filterable stackedOnFilter = stackedOn.getFilterable(actionContext);
                    String choicePlayerId = playerSource.getPlayer(actionContext);
                    int min = countSource.getMinimum(actionContext);
                    int max = countSource.getMaximum(actionContext);

                    Filterable additionalFilterable = Filters.any;
                    if (choiceFilter != null)
                        additionalFilterable = choiceFilter.getFilterable(actionContext);

                    return new ChooseStackedCardsEffect(action, choicePlayerId, min, max, stackedOnFilter, Filters.and(filterable, additionalFilterable)) {
                        @Override
                        protected void cardsChosen(LotroGame game, Collection<PhysicalCard> stackedCards) {
                            actionContext.setCardMemory(memory, stackedCards);
                        }
                    };
                }
            };
        }
        throw new RuntimeException("Unable to resolve card resolver of type: " + type);
    }

    public static EffectAppender resolveCardsInHand(String type, ValueSource countSource, String memory, String choicePlayer, String handPlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveCardsInHand(type, null, countSource, memory, choicePlayer, handPlayer, choiceText, environment);
    }

    public static EffectAppender resolveCardsInHand(String type, FilterableSource additionalFilter, ValueSource countSource, String memory, String choicePlayer, String handPlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        if (type.startsWith("random(") && type.endsWith(")")) {
            final int count = Integer.parseInt(type.substring(type.indexOf("(") + 1, type.lastIndexOf(")")));
            final PlayerSource handSource = PlayerResolver.resolvePlayer(handPlayer, environment);
            return new DelayedAppender() {
                @Override
                public boolean isPlayableInFull(ActionContext actionContext) {
                    final String handPlayer = handSource.getPlayer(actionContext);
                    return actionContext.getGame().getGameState().getHand(handPlayer).size() >= count;
                }

                @Override
                protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                    final String handPlayer = handSource.getPlayer(actionContext);
                    return new UnrespondableEffect() {
                        @Override
                        protected void doPlayEffect(LotroGame game) {
                            List<? extends PhysicalCard> hand = game.getGameState().getHand(handPlayer);
                            List<PhysicalCard> randomCardsFromHand = GameUtils.getRandomCards(hand, 2);
                            actionContext.setCardMemory(memory, randomCardsFromHand);
                        }
                    };
                }
            };
        } else if (type.equals("self")) {
            return new DelayedAppender() {
                @Override
                public boolean isPlayableInFull(ActionContext actionContext) {
                    return actionContext.getSource().getZone() == Zone.HAND;
                }

                @Override
                protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                    return new UnrespondableEffect() {
                        @Override
                        protected void doPlayEffect(LotroGame game) {
                            actionContext.setCardMemory(memory, actionContext.getSource());
                        }
                    };
                }
            };
        } else if (type.startsWith("memory(") && type.endsWith(")")) {
            final PlayerSource handSource = PlayerResolver.resolvePlayer(handPlayer, environment);
            Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource = new Function<ActionContext, Iterable<? extends PhysicalCard>>() {
                @Override
                public Iterable<? extends PhysicalCard> apply(ActionContext actionContext) {
                    String handPlayer = handSource.getPlayer(actionContext);
                    return actionContext.getGame().getGameState().getHand(handPlayer);
                }
            };
            return resolveMemoryCards(type, additionalFilter, additionalFilter, countSource, memory, cardSource);
        } else if (type.startsWith("all(") && type.endsWith(")")) {
            final String filter = type.substring(type.indexOf("(") + 1, type.lastIndexOf(")"));
            final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
            final PlayerSource handSource = PlayerResolver.resolvePlayer(handPlayer, environment);
            return new DelayedAppender() {
                @Override
                protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                    return new UnrespondableEffect() {
                        @Override
                        protected void doPlayEffect(LotroGame game) {
                            final Filterable filterable = filterableSource.getFilterable(actionContext);
                            String handId = handSource.getPlayer(actionContext);
                            Filterable additionalFilterable = Filters.any;
                            if (additionalFilter != null)
                                additionalFilterable = additionalFilter.getFilterable(actionContext);
                            actionContext.setCardMemory(memory, Filters.filter(game.getGameState().getHand(handId), game, filterable, additionalFilterable));
                        }
                    };
                }

                @Override
                public boolean isPlayableInFull(ActionContext actionContext) {
                    return true;
                }
            };
        } else if (type.startsWith("choose(") && type.endsWith(")")) {
            final String filter = type.substring(type.indexOf("(") + 1, type.lastIndexOf(")"));
            final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
            final PlayerSource playerSource = PlayerResolver.resolvePlayer(choicePlayer, environment);
            final PlayerSource handSource = PlayerResolver.resolvePlayer(handPlayer, environment);
            return new DelayedAppender() {
                @Override
                public boolean isPlayableInFull(ActionContext actionContext) {
                    int min = countSource.getMinimum(actionContext);
                    final Filterable filterable = filterableSource.getFilterable(actionContext);
                    String handId = handSource.getPlayer(actionContext);
                    Filterable additionalFilterable = Filters.any;
                    if (additionalFilter != null)
                        additionalFilterable = additionalFilter.getFilterable(actionContext);
                    final LotroGame game = actionContext.getGame();
                    return Filters.filter(game.getGameState().getHand(handId), game, filterable, additionalFilterable).size() >= min;
                }

                @Override
                protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                    int min = countSource.getMinimum(actionContext);
                    int max = countSource.getMaximum(actionContext);
                    final Filterable filterable = filterableSource.getFilterable(actionContext);
                    String choicePlayerId = playerSource.getPlayer(actionContext);
                    String handId = handSource.getPlayer(actionContext);
                    Filterable additionalFilterable = Filters.any;
                    if (additionalFilter != null)
                        additionalFilterable = additionalFilter.getFilterable(actionContext);
                    if (handId.equals(choicePlayerId)) {
                        return new ChooseCardsFromHandEffect(choicePlayerId, min, max, filterable, additionalFilterable) {
                            @Override
                            protected void cardsSelected(LotroGame game, Collection<PhysicalCard> cards) {
                                actionContext.setCardMemory(memory, cards);
                            }
                        };
                    } else {
                        final LotroGame game = actionContext.getGame();
                        final Collection<PhysicalCard> matchingCards = Filters.filter(game.getGameState().getHand(handId), game, filterable, additionalFilterable);
                        return new ChooseArbitraryCardsEffect(choicePlayerId, choiceText, matchingCards, min, max) {
                            @Override
                            protected void cardsSelected(LotroGame game, Collection<PhysicalCard> selectedCards) {
                                actionContext.setCardMemory(memory, selectedCards);
                            }
                        };
                    }
                }
            };
        }
        throw new RuntimeException("Unable to resolve card resolver of type: " + type);
    }

    public static EffectAppender resolveCardsInDiscard(String type, ValueSource countSource, String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveCardsInDiscard(type, null, countSource, memory, choicePlayer, choiceText, environment);
    }

    public static EffectAppender resolveCardsInDiscard(String type, FilterableSource additionalFilter, ValueSource countSource, String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveCardsInDiscard(type, additionalFilter, additionalFilter, countSource, memory, choicePlayer, choiceText, environment);
    }

    public static EffectAppender resolveCardsInDiscard(String type, FilterableSource choiceFilter, FilterableSource playabilityFilter, ValueSource countSource, String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        if (type.equals("self")) {
            return new DelayedAppender() {
                @Override
                public boolean isPlayableInFull(ActionContext actionContext) {
                    int min = countSource.getMinimum(actionContext);

                    Filterable additionalFilterable = Filters.any;
                    if (playabilityFilter != null)
                        additionalFilterable = playabilityFilter.getFilterable(actionContext);

                    Set<PhysicalCard> self = Collections.singleton(actionContext.getSource());
                    return Filters.filter(self, actionContext.getGame(), Filters.zone(Zone.DISCARD), additionalFilterable).size() >= min;
                }

                @Override
                protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                    return new UnrespondableEffect() {
                        @Override
                        protected void doPlayEffect(LotroGame game) {
                            actionContext.setCardMemory(memory, actionContext.getSource());
                        }
                    };
                }
            };
        } else if (type.startsWith("memory(") && type.endsWith(")")) {
            final PlayerSource playerSource = PlayerResolver.resolvePlayer(choicePlayer, environment);

            Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource = actionContext -> {
                String choicePlayerId = playerSource.getPlayer(actionContext);
                return actionContext.getGame().getGameState().getDiscard(choicePlayerId);
            };
            return resolveMemoryCards(type, choiceFilter, playabilityFilter, countSource, memory, cardSource);
        } else if (type.startsWith("all(") && type.endsWith(")")) {
            final String filter = type.substring(type.indexOf("(") + 1, type.lastIndexOf(")"));
            final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
            final PlayerSource playerSource = PlayerResolver.resolvePlayer(choicePlayer, environment);
            return new DelayedAppender() {
                @Override
                public boolean isPlayableInFull(ActionContext actionContext) {
                    return true;
                }

                @Override
                protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                    return new UnrespondableEffect() {
                        @Override
                        protected void doPlayEffect(LotroGame game) {
                            final Filterable filter = filterableSource.getFilterable(actionContext);
                            String choicePlayerId = playerSource.getPlayer(actionContext);
                            Filterable filterable = Filters.any;
                            if (choiceFilter != null)
                                filterable = choiceFilter.getFilterable(actionContext);
                            actionContext.setCardMemory(memory, Filters.filter(game.getGameState().getDiscard(choicePlayerId), game, filter, filterable));
                        }
                    };
                }
            };
        } else if (type.startsWith("choose(") && type.endsWith(")")) {
            final String filter = type.substring(type.indexOf("(") + 1, type.lastIndexOf(")"));
            final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
            final PlayerSource playerSource = PlayerResolver.resolvePlayer(choicePlayer, environment);
            return new DelayedAppender() {
                @Override
                public boolean isPlayableInFull(ActionContext actionContext) {
                    int min = countSource.getMinimum(actionContext);
                    final Filterable filter = filterableSource.getFilterable(actionContext);
                    String choicePlayerId = playerSource.getPlayer(actionContext);
                    Filterable additionalFilterable = Filters.any;
                    if (playabilityFilter != null)
                        additionalFilterable = playabilityFilter.getFilterable(actionContext);
                    final LotroGame game = actionContext.getGame();
                    return Filters.filter(game.getGameState().getDiscard(choicePlayerId), game, filter, additionalFilterable).size() >= min;
                }

                @Override
                protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                    int min = countSource.getMinimum(actionContext);
                    int max = countSource.getMaximum(actionContext);
                    final Filterable filterable = filterableSource.getFilterable(actionContext);
                    String choicePlayerId = playerSource.getPlayer(actionContext);
                    Filterable additionalFilterable = Filters.any;
                    if (choiceFilter != null)
                        additionalFilterable = choiceFilter.getFilterable(actionContext);
                    return new ChooseCardsFromDiscardEffect(choicePlayerId, min, max, filterable, additionalFilterable) {
                        @Override
                        protected void cardsSelected(LotroGame game, Collection<PhysicalCard> cards) {
                            actionContext.setCardMemory(memory, cards);
                        }
                    };
                }
            };
        }
        throw new RuntimeException("Unable to resolve card resolver of type: " + type);
    }

    public static EffectAppender resolveCardsInDeck(String type, FilterableSource choiceFilter, ValueSource countSource, String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        if (type.startsWith("memory(") && type.endsWith(")")) {
            final PlayerSource playerSource = PlayerResolver.resolvePlayer(choicePlayer, environment);
            Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource = actionContext -> {
                String playerId = playerSource.getPlayer(actionContext);
                return actionContext.getGame().getGameState().getDeck(playerId);
            };
            return resolveMemoryCards(type, choiceFilter, choiceFilter, countSource, memory, cardSource);
        } else if (type.startsWith("all(") && type.endsWith(")")) {
            final String filter = type.substring(type.indexOf("(") + 1, type.lastIndexOf(")"));
            final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
            final PlayerSource playerSource = PlayerResolver.resolvePlayer(choicePlayer, environment);
            return new DelayedAppender() {
                @Override
                protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                    return new UnrespondableEffect() {
                        @Override
                        protected void doPlayEffect(LotroGame game) {
                            final String player = playerSource.getPlayer(actionContext);
                            final Filterable filterable = filterableSource.getFilterable(actionContext);
                            actionContext.setCardMemory(memory, Filters.filter(game.getGameState().getDeck(player), game, filterable));
                        }
                    };
                }

                @Override
                public boolean isPlayableInFull(ActionContext actionContext) {
                    return true;
                }
            };
        } else if (type.startsWith("choose(") && type.endsWith(")")) {
            final String filter = type.substring(type.indexOf("(") + 1, type.lastIndexOf(")"));
            final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
            final PlayerSource playerSource = PlayerResolver.resolvePlayer(choicePlayer, environment);
            return new DelayedAppender() {
                @Override
                public boolean isPlayableInFull(ActionContext actionContext) {
                    return true;
                }

                @Override
                protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                    int min = countSource.getMinimum(actionContext);
                    int max = countSource.getMaximum(actionContext);
                    final Filterable filterable = filterableSource.getFilterable(actionContext);
                    String choicePlayerId = playerSource.getPlayer(actionContext);
                    Filterable additionalFilterable = Filters.any;
                    if (choiceFilter != null)
                        additionalFilterable = choiceFilter.getFilterable(actionContext);
                    return new ChooseCardsFromDeckEffect(choicePlayerId, min, max, filterable, additionalFilterable) {
                        @Override
                        protected void cardsSelected(LotroGame game, Collection<PhysicalCard> cards) {
                            actionContext.setCardMemory(memory, cards);
                        }
                    };
                }
            };
        }
        throw new RuntimeException("Unable to resolve card resolver of type: " + type);
    }

    public static EffectAppender resolveCard(String type, FilterableSource additionalFilter, String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveCards(type, additionalFilter, new ConstantEvaluator(1), memory, choicePlayer, choiceText, environment);
    }

    public static EffectAppender resolveCard(String type, String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveCard(type, null, memory, choicePlayer, choiceText, environment);
    }

    public static EffectAppender resolveCards(String type, ValueSource countSource, String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveCards(type, null, countSource, memory, choicePlayer, choiceText, environment);
    }

    public static EffectAppender resolveCards(String type, FilterableSource additionalFilter, ValueSource countSource, String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveCards(type, additionalFilter, additionalFilter, countSource, memory, choicePlayer, choiceText, environment);
    }

    public static EffectAppender resolveCards(String type, FilterableSource additionalFilter, FilterableSource playabilityFilter, ValueSource countSource, String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        if (type.equals("self")) {
            return new DelayedAppender() {
                @Override
                public boolean isPlayableInFull(ActionContext actionContext) {
                    if (playabilityFilter != null)
                        return PlayConditions.isActive(actionContext.getGame(), actionContext.getSource(), playabilityFilter.getFilterable(actionContext));
                    return true;
                }

                @Override
                protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                    return new UnrespondableEffect() {
                        @Override
                        protected void doPlayEffect(LotroGame game) {
                            actionContext.setCardMemory(memory, actionContext.getSource());
                        }
                    };
                }
            };
        } else if (type.equals("bearer")) {
            return new DelayedAppender() {
                @Override
                public boolean isPlayableInFull(ActionContext actionContext) {
                    final PhysicalCard attachedTo = actionContext.getSource().getAttachedTo();
                    if (attachedTo == null)
                        return false;
                    if (playabilityFilter != null)
                        return PlayConditions.isActive(actionContext.getGame(), attachedTo, playabilityFilter.getFilterable(actionContext));
                    return true;
                }

                @Override
                protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                    return new UnrespondableEffect() {
                        @Override
                        protected void doPlayEffect(LotroGame game) {
                            actionContext.setCardMemory(memory, actionContext.getSource().getAttachedTo());
                        }
                    };
                }
            };
        } else if (type.startsWith("memory(") && type.endsWith(")")) {
            Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource = actionContext ->
                    Filters.filterActive(actionContext.getGame(), Filters.any);
            return resolveMemoryCards(type, additionalFilter, playabilityFilter, countSource, memory, cardSource);
        } else if (type.startsWith("all(") && type.endsWith(")")) {
            final String filter = type.substring(type.indexOf("(") + 1, type.lastIndexOf(")"));
            final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
            return new DelayedAppender() {
                @Override
                protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                    return new UnrespondableEffect() {
                        @Override
                        protected void doPlayEffect(LotroGame game) {
                            final Filterable filterable = filterableSource.getFilterable(actionContext);
                            actionContext.setCardMemory(memory, Filters.filterActive(game, filterable));
                        }
                    };
                }

                @Override
                public boolean isPlayableInFull(ActionContext actionContext) {
                    return true;
                }
            };
        } else if (type.startsWith("choose(") && type.endsWith(")")) {
            final String filter = type.substring(type.indexOf("(") + 1, type.lastIndexOf(")"));
            final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
            final PlayerSource playerSource = PlayerResolver.resolvePlayer(choicePlayer, environment);
            return new DelayedAppender() {
                @Override
                public boolean isPlayableInFull(ActionContext actionContext) {
                    int min = countSource.getMinimum(actionContext);
                    final Filterable filterable = filterableSource.getFilterable(actionContext);
                    Filterable additionalFilterable = Filters.any;
                    if (playabilityFilter != null)
                        additionalFilterable = playabilityFilter.getFilterable(actionContext);
                    return PlayConditions.isActive(actionContext.getGame(), min, filterable, additionalFilterable);
                }

                @Override
                protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                    int min = countSource.getMinimum(actionContext);
                    int max = countSource.getMaximum(actionContext);
                    final Filterable filterable = filterableSource.getFilterable(actionContext);
                    Filterable additionalFilterable = Filters.any;
                    if (additionalFilter != null)
                        additionalFilterable = additionalFilter.getFilterable(actionContext);
                    String choicePlayerId = playerSource.getPlayer(actionContext);
                    return new ChooseActiveCardsEffect(actionContext.getSource(), choicePlayerId, choiceText, min, max, filterable, additionalFilterable) {
                        @Override
                        protected void cardsSelected(LotroGame game, Collection<PhysicalCard> cards) {
                            actionContext.setCardMemory(memory, cards);
                        }
                    };
                }
            };
        }
        throw new InvalidCardDefinitionException("Unable to resolve card resolver of type: " + type);
    }

    private static DelayedAppender resolveMemoryCards(String type, FilterableSource choiceFilter, FilterableSource playabilityFilter,
                                                      ValueSource countSource, String memory,
                                                      Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource) throws InvalidCardDefinitionException {
        String sourceMemory = type.substring(type.indexOf("(") + 1, type.lastIndexOf(")"));
        if (sourceMemory.contains("(") || sourceMemory.contains(")"))
            throw new InvalidCardDefinitionException("Memory name cannot contain parenthesis");

        return new DelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                int min = countSource.getMinimum(null);
                return filterCards(actionContext, playabilityFilter).size() >= min;
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                Collection<PhysicalCard> result = filterCards(actionContext, choiceFilter);
                return new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(LotroGame game) {
                        actionContext.setCardMemory(memory, result);
                    }
                };
            }

            private Collection<PhysicalCard> filterCards(ActionContext actionContext, FilterableSource filter) {
                Collection<? extends PhysicalCard> cardsFromMemory = actionContext.getCardsFromMemory(sourceMemory);
                Filterable additionalFilterable = Filters.any;
                if (filter != null)
                    additionalFilterable = filter.getFilterable(actionContext);
                return Filters.filter(cardSource.apply(actionContext), actionContext.getGame(), Filters.in(cardsFromMemory), additionalFilterable);
            }
        };
    }
}
