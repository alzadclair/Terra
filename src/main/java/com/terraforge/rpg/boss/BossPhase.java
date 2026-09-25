package com.terraforge.rpg.boss;

/**
 * Representation of a discrete combat phase in a Terraria boss encounter.
 */
public record BossPhase(
        int phaseNumber,
        String name,
        double healthThresholdRatio // 1.0 = 100%, 0.5 = 50%, etc.
) {
    public static final BossPhase PHASE_1 = new BossPhase(1, "Phase 1", 1.0);
    public static final BossPhase PHASE_2 = new BossPhase(2, "Phase 2", 0.5);
    public static final BossPhase ENRAGED = new BossPhase(3, "Enraged", 0.2);
}
