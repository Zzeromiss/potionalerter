package com.example.potionalert;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PotionAlertClient implements ClientModInitializer {

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
        PotionAlertConfig.load();
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }

    private void tick(MinecraftClient client) {
        if (client.player == null) {
            lastDuration.clear();
            return;
        }

        int mark = PotionAlertConfig.alertSeconds * 20; // seconds -> ticks
        Map<RegistryEntry<StatusEffect>, Integer> current = new HashMap<>();

        for (StatusEffectInstance effect : client.player.getStatusEffects()) {
            RegistryEntry<StatusEffect> type = effect.getEffectType();
            if (!TRACKED.contains(type) || effect.isInfinite()) continue;

            int duration = effect.getDuration();
            current.put(type, duration);

            Integer previous = lastDuration.get(type);
            // Plays once when the timer crosses the chosen mark
            if (previous != null && previous > mark && duration <= mark) {
                PotionAlertConfig.play(client);
            }
        }

        lastDuration.clear();
        lastDuration.putAll(current);
    }
}
