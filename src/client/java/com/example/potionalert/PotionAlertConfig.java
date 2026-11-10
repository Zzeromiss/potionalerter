package com.example.potionalert;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class PotionAlertConfig {

    // { display name, vanilla sound id }  -- add your own lines here if you like
    public static final String[][] SOUNDS = {
            {"Bell", "block.bell.use"},
            {"Dolphin Squeak", "entity.dolphin.ambient"},
            {"Ender Dragon Death", "entity.ender_dragon.death"},
            {"Ender Dragon Growl", "entity.ender_dragon.growl"},
            {"Wither Spawn", "entity.wither.spawn"},
            {"Warden Sonic Boom", "entity.warden.sonic_boom"},
            {"Raid Horn", "event.raid.horn"},
            {"Thunder", "entity.lightning_bolt.thunder"},
            {"Anvil Land", "block.anvil.land"},
            {"Note Block Pling", "block.note_block.pling"},
            {"XP Orb", "entity.experience_orb.pickup"},
            {"Level Up", "entity.player.levelup"},
            {"Ghast Scream", "entity.ghast.scream"},
            {"Enderman Scream", "entity.enderman.scream"},
            {"Elder Guardian Curse", "entity.elder_guardian.curse"},
            {"Explosion", "entity.generic.explode"}
    };

    public static final int MIN_SECONDS = 1;
    public static final int MAX_SECONDS = 60;

    public static int alertSeconds = 5;
    public static float volume = 1.0f; // 0.0 - 1.0
    public static String soundId = SOUNDS[0][1];

    private PotionAlertConfig() {}

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("potionalert.properties");
    }

    public static void load() {
        Path path = file();
        if (!Files.exists(path)) {
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(path)) {
            Properties p = new Properties();
            p.load(reader);
            alertSeconds = clampInt(Integer.parseInt(p.getProperty("alertSeconds", "5").trim()), MIN_SECONDS, MAX_SECONDS);
            volume = Math.max(0.0f, Math.min(1.0f, Float.parseFloat(p.getProperty("volume", "1.0").trim())));
            soundId = p.getProperty("soundId", SOUNDS[0][1]).trim();
        } catch (IOException | RuntimeException e) {
            System.err.println("[PotionAlert] Could not read config, using defaults: " + e);
        }
    }

    public static void save() {
        Properties p = new Properties();
        p.setProperty("alertSeconds", Integer.toString(alertSeconds));
        p.setProperty("volume", Float.toString(volume));
        p.setProperty("soundId", soundId);
        try (Writer writer = Files.newBufferedWriter(file())) {
            p.store(writer, "Potion Alert config. soundId can be any Minecraft sound id, e.g. block.bell.use");
        } catch (IOException e) {
            System.err.println("[PotionAlert] Could not save config: " + e);
        }
    }

    private static int clampInt(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    public static int soundIndex() {
        for (int i = 0; i < SOUNDS.length; i++) {
            if (SOUNDS[i][1].equals(soundId)) return i;
        }
        return -1;
    }

    public static String soundName() {
        int i = soundIndex();
        return i >= 0 ? SOUNDS[i][0] : soundId;
    }

    public static void nextSound() {
        soundId = SOUNDS[(soundIndex() + 1) % SOUNDS.length][1];
    }

    /** Plays the chosen sound straight to the player (not affected by distance or the Players volume slider). */
    public static void play(MinecraftClient client) {
        Identifier id = Identifier.tryParse(soundId);
        if (id == null || volume <= 0.0f) return;
        client.getSoundManager().play(PositionedSoundInstance.ui(SoundEvent.of(id), 1.0f, volume));
    }
}
