package gr.uniwa.labcontrolclient;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.util.*;

public class MainActivity extends AppCompatActivity {

    private static final int PORT = 41007;
    private Spinner spinnerCommands;
    private ListView listViewPCs;
    private Button btnRefresh, btnSend, btnWOL;
    private TextView tvLog;

    // Mapping hostname → MAC
    private final Map<String, String> macMap = new LinkedHashMap<String,String>() {{
        put("PRPC01","50:81:40:2B:91:8D");
        put("PRPC02","50:81:40:2B:7C:78");
        put("PRPC03","50:81:40:2B:78:DD");
        put("PRPC04","50:81:40:2B:7B:3D");
        put("PRPC05","50:81:40:2B:79:91");
        put("PRPC06","C8:5A:CF:0F:76:3D");
        put("PRPC07","C8:5A:CF:0D:71:24");
        put("PRPC08","C8:5A:CF:0F:B3:FF");
        put("PRPC09","C8:5A:CF:0E:2C:C4");
        put("PRPC10","C8:5A:CF:0F:7C:D0");
        put("PRPC11","C8:5A:CF:0D:71:3A");
        put("PRPC12","C8:5A:CF:0F:EE:01");
        put("PRPC13","C8:5A:CF:0E:1D:88");
        put("PRPC14","C8:5A:CF:0F:F0:1E");
        put("PRPC15","50:81:40:2B:7D:A4");
        put("PRPC16","C8:5A:CF:0E:2C:78");
        put("PRPC17","50:81:40:2B:87:F4");
        put("PRPC18","C8:5A:CF:0F:EC:11");
        put("PRPC19","C8:5A:CF:0F:7C:1F");
        put("PRPC20","C8:5A:CF:0D:71:2C");
        put("PRPC21","C8:5A:CF:0D:70:95");
        put("PRPC22","50:81:40:2B:5F:D0");
        put("PRPC23","50:81:40:2B:7A:0B");
        put("PRPC24","50:81:40:2B:8F:D3");
        put("PRPC25","50:81:40:2B:72:E0");
        put("PRPC26","50:81:40:2B:7A:74");
        put("PRPC27DESK","C8:5A:CF:0F:7C:D4");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spinnerCommands = findViewById(R.id.spinnerCommands);
        listViewPCs = findViewById(R.id.listViewPCs);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnSend    = findViewById(R.id.btnSend);
        btnWOL     = findViewById(R.id.btnWOL);
        tvLog      = findViewById(R.id.tvLog);

        // Spinner setup
        ArrayAdapter<String> cmdAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                Arrays.asList("Echo","Restart","Shutdown","Restore")
        );
        cmdAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCommands.setAdapter(cmdAdapter);

        // ListView setup
        final List<String> hostList = new ArrayList<>(macMap.keySet());
        ArrayAdapter<String> lvAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_multiple_choice, hostList
        );
        listViewPCs.setAdapter(lvAdapter);

        // Κουμπί Refresh = στέλνει Echo σε όλους
        btnRefresh.setOnClickListener(v -> {
            tvLog.setText("");
            for (String host : hostList) {
                NetworkUtils.sendCommand(host, PORT, "Echo", this::appendLog);
            }
        });

        // Κουμπί Send Command
        btnSend.setOnClickListener(v -> {
            String cmd = spinnerCommands.getSelectedItem().toString();
            tvLog.setText("");
            for (int i = 0; i < hostList.size(); i++) {
                if (listViewPCs.isItemChecked(i)) {
                    String host = hostList.get(i);
                    NetworkUtils.sendCommand(host, PORT, cmd, this::appendLog);
                }
            }
        });

        // Κουμπί WOL
        btnWOL.setOnClickListener(v -> {
            tvLog.setText("");
            String broadcast = "192.168.88.255"; // ή 255.255.255.255
            for (int i = 0; i < hostList.size(); i++) {
                if (listViewPCs.isItemChecked(i)) {
                    String host = hostList.get(i);
                    String mac  = macMap.get(host);
                    try {
                        NetworkUtils.sendWOL(mac, broadcast);
                        appendLog(host + ": WOL packet sent");
                    } catch (Exception e) {
                        appendLog(host + ": WOL ERROR → " + e.getMessage());
                    }
                }
            }
        });
    }

    private void appendLog(final String line) {
        runOnUiThread(() -> {
            tvLog.append(line + "\n");
        });
    }
}
