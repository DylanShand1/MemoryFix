package com.memoryfix.mixin;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(Scoreboard.class)
public interface ScoreboardAccessor {
    @Accessor("objectives")
    Map memoryfix$getObjectives();

    @Accessor("objectivesByCriterion")
    Map memoryfix$getObjectivesByCriterion();

    @Accessor("playerObjectives")
    Map memoryfix$getPlayerObjectives();

    @Accessor("objectivesArray")
    ScoreboardObjective[] memoryfix$getObjectivesArray();

    @Accessor("teams")
    Map memoryfix$getTeams();

    @Accessor("teamsByPlayer")
    Map memoryfix$getTeamsByPlayer();
}
