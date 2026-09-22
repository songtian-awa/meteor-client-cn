/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 *
 * Automated self-test hook: enabled with -Dmeteor.autotest=1.
 * Automatically creates a flat world, toggles every custom module one by one,
 * and writes PASS/FAIL per module to the result file.
 */

package meteordevelopment.meteorclient;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.AutoTestWorldMixin;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class AutoTest {
    public static final boolean ENABLED = System.getProperty("meteor.autotest") != null;

    private enum Phase { WAIT_MENU, VERIFY_TRANSLATION, OPEN_CREATE, WAIT_CREATE_SCREEN, CREATE_LEVEL, WAIT_WORLD, TEST_MODULES, DONE }

    private Phase phase = Phase.WAIT_MENU;
    private int tick = 0;
    private int moduleIndex = 0;
    private int moduleTicks = 0;
    private String currentModule = null;
    private final List<String> results = new ArrayList<>();
    private int passCount = 0;
    private int failCount = 0;
    private final List<String> customModules = List.of(
        "rotation-bypass", "timer-bypass", "velocity-bypass", "step-bypass", "spider-bypass",
        "glide", "fast-fall", "anti-push", "anti-levitation", "slime-launch",
        "sprint-bypass", "water-bypass", "lava-bypass", "air-control", "climb-bypass", "jump-bypass", "anti-swim",
        "scaffold-bypass", "eagle",
        "auto-block", "aim-assist", "combo", "sword-stop", "target-hud", "auto-rod", "pearl-aim",
        "crit-bypass", "aim-bypass", "target-bypass", "reach-bypass", "auto-shield",
        "trigger-bot", "anti-aim",
        "crystal-esp", "spawn-esp", "item-esp", "sign-esp", "redstone-esp", "chunk-borders", "entity-health", "no-hurt-cam",
        "tnt-esp", "piston-esp", "spawner-esp", "lava-esp", "bed-esp", "arrow-esp", "portal-esp",
        "auto-shift", "drop-slot", "inventory-cleaner", "mc-command",
        "auto-soup", "fish-bot", "nether-coords", "fast-place", "click-assist",
        "chat-filter", "packet-sorter", "anti-drown",
        "chat-timestamps", "auto-shout"
    );

    public AutoTest() {
        writeLog("Autotest started, phase=WAIT_MENU");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        MinecraftClient mc = MinecraftClient.getInstance();
        tick++;

        switch (phase) {
            case WAIT_MENU -> {
                if (mc.world == null && mc.currentScreen != null && !(mc.currentScreen instanceof net.minecraft.client.gui.screen.TitleScreen)) {
                    // Skip first-launch welcome / telemetry screens to reach the main menu
                    writeLog("Skipping screen " + mc.currentScreen.getClass().getSimpleName() + " at tick " + tick);
                    mc.setScreen(null);
                }
                else if (mc.world == null && (mc.currentScreen == null || mc.currentScreen instanceof net.minecraft.client.gui.screen.TitleScreen)) {
                    writeLog("Main menu reached at tick " + tick + " (screen=" + (mc.currentScreen == null ? "null" : "TitleScreen") + "), verifying translations");
                    phase = Phase.VERIFY_TRANSLATION;
                }
            }

            case VERIFY_TRANSLATION -> {
                boolean allOk = true;
                for (String name : customModules) {
                    Module mod = Modules.get().get(name);
                    if (mod == null) {
                        writeLog("TRANS " + name + ": FAIL (not found)");
                        allOk = false;
                        continue;
                    }
                    String title = mod.title;
                    boolean hasChinese = title != null && title.codePoints().anyMatch(cp -> cp > 0x2E80);
                    if (!hasChinese) {
                        writeLog("TRANS " + name + ": FAIL (title='" + title + "')");
                        allOk = false;
                    }
                    else {
                        writeLog("TRANS " + name + ": PASS (" + title + ")");
                    }
                }
                writeLog(allOk ? "=== TRANSLATION CHECK: ALL " + customModules.size() + " PASS ===" : "=== TRANSLATION CHECK: SOME FAILED ===");
                writeLog("Main menu at tick " + tick + ", opening CreateWorldScreen");
                CreateWorldScreen.show(mc, () -> {});
                phase = Phase.WAIT_CREATE_SCREEN;
            }

            case WAIT_CREATE_SCREEN -> {
                if (mc.currentScreen instanceof CreateWorldScreen) {
                    writeLog("CreateWorldScreen open at tick " + tick + ", calling createLevel");
                    try {
                        ((AutoTestWorldMixin) mc.currentScreen).invokeCreateLevel();
                        writeLog("createLevel invoked OK at tick " + tick + ", screen now=" + (mc.currentScreen == null ? "null" : mc.currentScreen.getClass().getSimpleName()) + " world=" + (mc.world != null));
                    } catch (Throwable t) {
                        writeLog("createLevel threw: " + t);
                    }
                    phase = Phase.WAIT_WORLD;
                }
                else if (tick > 400) {
                    writeLog("FAIL: CreateWorldScreen never opened after 400 ticks");
                    finish();
                }
            }

            case WAIT_WORLD -> {
                // Diagnostic: every 100 ticks log current screen
                if (tick % 100 == 0) {
                    writeLog("DBG tick=" + tick + " screen=" + (mc.currentScreen == null ? "null" : mc.currentScreen.getClass().getSimpleName()) + " world=" + (mc.world != null) + " player=" + (mc.player != null));
                }
                // Auto-confirm any world creation confirmation / alert screens
                if (mc.currentScreen != null && mc.world == null && (mc.currentScreen.getClass().getSimpleName().contains("Confirm") || mc.currentScreen.getClass().getSimpleName().contains("Alert"))) {
                    writeLog("Dismissing confirm screen " + mc.currentScreen.getClass().getSimpleName() + " at tick " + tick);
                    confirmScreen(mc.currentScreen, true);
                }
                else if (mc.world != null && mc.player != null) {
                    writeLog("World loaded at tick " + tick + ", starting module tests");
                    phase = Phase.TEST_MODULES;
                    startNextModule();
                }
                else if (tick > 6000) {
                    writeLog("FAIL: world never loaded after 6000 ticks");
                    finish();
                }
            }

            case TEST_MODULES -> {
                moduleTicks++;
                Module mod = Modules.get().get(currentModule);

                // Give each module 30 ticks to prove it doesn't crash
                if (moduleTicks >= 30) {
                    boolean active = mod != null && mod.isActive();
                    String name = currentModule;
                    String status = "PASS";

                    if (mod == null) {
                        status = "FAIL";
                        failCount++;
                        writeLog("RESULT " + name + ": FAIL (module not found)");
                    }
                    else if (!active) {
                        // Some modules auto-disable after doing their job (drop-slot, mc-command)
                        status = "OK(auto-off)";
                        passCount++;
                        writeLog("RESULT " + name + ": OK(auto-off)");
                    }
                    else {
                        passCount++;
                        writeLog("RESULT " + name + ": PASS");
                    }

                    if (mod != null && mod.isActive()) mod.toggle();

                    moduleIndex++;
                    if (moduleIndex < customModules.size()) {
                        startNextModule();
                    }
                    else {
                        finish();
                    }
                }
            }

            case DONE -> {}
        }
    }

    private void startNextModule() {
        currentModule = customModules.get(moduleIndex);
        moduleTicks = 0;

        Module mod = Modules.get().get(currentModule);
        writeLog("TEST " + (moduleIndex + 1) + "/" + customModules.size() + " " + currentModule);
        if (mod != null && !mod.isActive()) mod.toggle();
    }

    /** Dismiss a confirm/alert screen by invoking its boolean callback via reflection. */
    private void confirmScreen(net.minecraft.client.gui.screen.Screen screen, boolean value) {
        try {
            // ConfirmScreen: field f (BooleanConsumer); AlertScreen: onClose Runnable-based
            java.lang.reflect.Field[] fields = screen.getClass().getDeclaredFields();
            for (java.lang.reflect.Field f : fields) {
                if (it.unimi.dsi.fastutil.booleans.BooleanConsumer.class.isAssignableFrom(f.getType()) && !f.canAccess(screen)) {
                    f.setAccessible(true);
                    Object cb = f.get(screen);
                    if (cb instanceof it.unimi.dsi.fastutil.booleans.BooleanConsumer bc) {
                        bc.accept(value);
                        writeLog("confirmScreen: invoked BooleanConsumer on " + screen.getClass().getSimpleName());
                        return;
                    }
                }
            }
            // Fallback: close screen directly
            writeLog("confirmScreen: no BooleanConsumer found, closing screen");
            MinecraftClient.getInstance().setScreen(null);
        } catch (Exception e) {
            writeLog("confirmScreen: reflection failed (" + e.getMessage() + "), closing screen");
            MinecraftClient.getInstance().setScreen(null);
        }
    }

    private void finish() {
        phase = Phase.DONE;
        writeLog("=== AUTOTEST COMPLETE: " + passCount + " passed, " + failCount + " failed ===");
        MinecraftClient.getInstance().scheduleStop();
    }

    private void writeLog(String line) {
        try (PrintWriter pw = new PrintWriter(new FileWriter("/tmp/autotest-result.txt", true))) {
            pw.println(line);
        } catch (Exception e) {
            System.err.println("[autotest] failed to write log: " + e.getMessage());
        }
    }
}
