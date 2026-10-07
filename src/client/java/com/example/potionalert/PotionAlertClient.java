package com.example.potionalert;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PotionAlertClient implements ClientModInitializer {

    // ===== Easy settings =====
    private static final int WARN_TICKS = 200;          // 10 seconds * 20 ticks
    private static final SoundEvent ALERT_SOUND = SoundEvents.BLOCK_BELL_USE;
    private static final float VOLUME = 3.0f;           // >1.0 = louder / carries further
    private static final float PITCH = 1.0f;

    // Only these effects trigger the sound
    private static final Set<RegistryEntry<StatusEffect>> TRACKED = Set.of(
            StatusEffects.SPEED,
            StatusEffects.STRENGTH,
            StatusEffects.FIRE_RESISTANCE,
            StatusEffects.WEAVING
    );

    // Last seen duration of each tracked effect
    private final Map<RegistryEntry<StatusEffect>, Integer> lastDuration = new HashMap<>();

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }

    private void tick(MinecraftClient client) {
        if (client.player == null) {
            lastDuration.clear();
            return;
        }

        Map<RegistryEntry<StatusEffect>, Integer> current = new HashMap<>();

        for (StatusEffectInstance effect : client.player.getStatusEffects()) {
            RegistryEntry<StatusEffect> type = effect.getEffectType();
            if (!TRACKED.contains(type) || effect.isInfinite()) continue;

            int duration = effect.getDuration();
            current.put(type, duration);

            Integer previous = lastDuration.get(type);
            // Fires once when the timer crosses 10s (not when re-drunk with <10s left)
            if (previous != null && previous > WARN_TICKS && duration <= WARN_TICKS) {
                client.player.playSound(ALERT_SOUND, VOLUME, PITCH);
            }
        }

        lastDuration.clear();
        lastDuration.putAll(current);
    }
}
