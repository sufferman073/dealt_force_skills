package com.rzy.dealt_force_skills.util;

import com.rzy.dealt_force_skills.client.HelmetVisionClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public final class ClientVisionHooks {
    private ClientVisionHooks() {
    }

    public static boolean isThermalVisionActive() {
        Boolean active = DistExecutor.safeCallWhenOn(Dist.CLIENT, () -> HelmetVisionClient::isThermalVisionActive);
        return Boolean.TRUE.equals(active);
    }
}
