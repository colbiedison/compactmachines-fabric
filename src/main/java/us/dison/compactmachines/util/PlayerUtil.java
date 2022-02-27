package us.dison.compactmachines.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.util.UserCache;

import java.util.Optional;
import java.util.UUID;

public abstract class PlayerUtil {
    public static String getPlayerNameFromUUID(UserCache userCache, UUID uuid) {
        if (uuid == null) {
            return null;
        }
        Optional<GameProfile> gp = userCache.getByUuid(uuid);
        if (gp.isPresent()) {
            return gp.get().getName();
        }
        return null;
    }
}
