package com.rzy.dealt_force_skills.client;

/**
 * Legacy entry point kept for reference. Teammate wallhacks now use the mod-internal
 * position exposure pipeline:
 * <ul>
 *   <li>server: {@link com.rzy.dealt_force_skills.team.TeammateRevealSync}</li>
 *   <li>packet: {@link com.rzy.dealt_force_skills.network.S2C_TeammatePositionReveal}</li>
 *   <li>client: {@link ClientTeammateRevealState}</li>
 * </ul>
 * Do not reintroduce vanilla scoreboard-team-only glow checks here.
 */
public final class TeamMateOutlineClient {
    private TeamMateOutlineClient() {
    }
}
