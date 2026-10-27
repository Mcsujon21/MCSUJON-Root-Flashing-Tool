package com.mcsujon.rootflasher;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MainActivity extends Activity {
    LinearLayout root, content;
    TextView status, output;
    final int BG = Color.rgb(5, 15, 24), CARD = Color.rgb(10, 31, 46);
    final int CYAN = Color.rgb(0, 220, 190), BLUE = Color.rgb(30, 125, 255);
    final int WHITE = Color.rgb(235, 245, 255);

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildHome();
        checkRoot();
    }

    int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }
    TextView text(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }
    LinearLayout column() {
        LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(dp(16),dp(12),dp(16),dp(12)); return l;
    }
    void buildHome() {
        root = column(); root.setBackgroundColor(BG);
        ScrollView scroll = new ScrollView(this); scroll.addView(root);
        setContentView(scroll);
        TextView title = text("MCSUJON", 28, WHITE, true); title.setGravity(Gravity.CENTER);
        root.addView(title);
        TextView subtitle = text("ROOT FLASHING TOOL  •  v1.0", 14, CYAN, true); subtitle.setGravity(Gravity.CENTER);
        root.addView(subtitle);
        TextView tagline = text("ADB  •  FASTBOOT  •  DOWNLOAD  •  EDL  •  ROOT", 11, WHITE, false);
        tagline.setGravity(Gravity.CENTER); tagline.setPadding(0,dp(8),0,dp(16)); root.addView(tagline);

        LinearLayout info = column(); info.setBackgroundColor(CARD);
        info.addView(text("DEVICE STATUS", 16, CYAN, true));
        status = text("Checking root access…", 14, WHITE, false); status.setPadding(0,dp(8),0,dp(8)); info.addView(status);
        root.addView(info);

        addButton("ADB / ROOT SHELL", BLUE, () -> runRoot("id; getprop ro.product.model; getprop ro.build.version.release"));
        addButton("DEVICE INFORMATION", CARD, () -> runRoot("id; getprop ro.product.manufacturer; getprop ro.product.model; getprop ro.product.device; getprop ro.build.version.release; getprop ro.build.version.sdk"));
        addButton("REBOOT OPTIONS", CARD, () -> showRebootDialog());
        addButton("DOWNLOAD MODE (instructions)", CARD, () -> showMessage("Samsung Download Mode", "This screen provides instructions only. Your S7 Edge volume keys are broken, so do not rely on the hardware-key method. Use a verified, device-compatible route before attempting recovery or flashing."));
        addButton("FASTBOOT / EDL", CARD, () -> showMessage("Protocol support", "Fastboot is not normally available on Samsung Galaxy S7 Edge stock bootloader. EDL is Qualcomm-specific; your Exynos SM-G935F does not use Qualcomm EDL."));
        addButton("FLASH IMAGE", CARD, () -> showMessage("Flashing not enabled yet", "This starter build intentionally does not write partitions. Next stage: implement verified USB transport and device-specific checks before enabling flash operations."));
        addButton("VIEW LOG", CARD, () -> runRoot("logcat -d -t 120"));
        output = text("Ready. Flashing is disabled in this starter build.", 12, CYAN, false);
        output.setPadding(dp(12),dp(14),dp(12),dp(14)); output.setBackgroundColor(Color.rgb(0,8,14)); root.addView(output);
        TextView footer = text("Creator: MCSUJON  •  Built for Android", 11, Color.LTGRAY, false);
        footer.setGravity(Gravity.CENTER); footer.setPadding(0,dp(18),0,dp(8)); root.addView(footer);
    }
    void addButton(String label, int color, Runnable action) {
        Button b = new Button(this); b.setText(label); b.setTextColor(WHITE); b.setAllCaps(false);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(52)); p.topMargin = dp(8);
        root.addView(b,p); b.setOnClickListener(v -> action.run());
    }
    void checkRoot() {
        new Thread(() -> {
            String result = shell("su -c id");
            boolean ok = result.contains("uid=0");
            runOnUiThread(() -> status.setText(ok ? "ROOT ACCESS: GRANTED ✓\n" + result.trim() : "ROOT ACCESS: NOT GRANTED / NOT AVAILABLE\n" + result.trim()));
        }).start();
    }
    String shell(String command) {
        StringBuilder out = new StringBuilder();
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"sh","-c",command});
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line; while ((line = r.readLine()) != null) out.append(line).append('\n');
            BufferedReader e = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((line = e.readLine()) != null) out.append(line).append('\n');
            p.waitFor();
        } catch (Exception ex) { out.append(ex.toString()); }
        return out.toString();
    }
    void runRoot(String command) {
        output.setText("Running…\n" + command);
        new Thread(() -> {
            String result = shell("su -c " + quote(command));
            runOnUiThread(() -> output.setText("$ " + command + "\n\n" + result));
        }).start();
    }
    String quote(String s) { return "'" + s.replace("'", "'\\''") + "'"; }
    void showMessage(String title, String message) {
        new android.app.AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("OK", null).show();
    }
    void showRebootDialog() {
        String[] choices = {"Reboot system", "Reboot to recovery", "Reboot to bootloader"};
        new android.app.AlertDialog.Builder(this).setTitle("Root reboot options")
        .setItems(choices, (d, which) -> {
            String cmd = which == 0 ? "reboot" : (which == 1 ? "reboot recovery" : "reboot bootloader");
            new android.app.AlertDialog.Builder(this).setTitle("Confirm reboot")
            .setMessage("Run root command: " + cmd + " ?")
            .setNegativeButton("Cancel", null).setPositiveButton("Reboot", (dd, ww) -> runRoot(cmd)).show();
        }).show();
    }
}
