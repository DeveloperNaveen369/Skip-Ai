package community.india.hack.in.skipai;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.navigation.NavigationView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import community.india.hack.in.skipai.manager.ChatManager;
import community.india.hack.in.skipai.utils.ChatEntity;

public class ChatUi extends AppCompatActivity {
    private EditText promptInput;
    private ScrollView messageScrollview;
    private LinearLayout linearLayout;
    private Chip send_btn, menu_btn;
    MaterialCardView new_chat_btn,home_btn;
    private ChatController chatController;
    private DrawerLayout drawerLayout;
    private final Map<Integer, String> drawerChatIds = new HashMap<>();
    private final Map<String, Integer> chatDrawerIds = new HashMap<>();
    ChatManager chatManager;
    NavigationView navigationView;
    private static final int NEW_CHAT_ID = 10001;
    public Boolean cureentisEmpty;
    ImageView imageView ;
    LottieAnimationView lottieView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_ui);

        promptInput = findViewById(R.id.prompt_input);
        messageScrollview = findViewById(R.id.message_scrollView);
        linearLayout = findViewById(R.id.message_container);
        send_btn = findViewById(R.id.send_prompt);
        menu_btn = findViewById(R.id.menu_btn);
        SkipAiApplication app = (SkipAiApplication) getApplication();
        chatManager = app.getChatManager();
        drawerLayout = findViewById(R.id.main);
        navigationView = findViewById(R.id.nav_view);
        imageView = findViewById(R.id.chat_logo);
        View header = navigationView.getHeaderView(0);

        new_chat_btn = header.findViewById(R.id.new_chat_btn);
        home_btn = header.findViewById(R.id.home_btn);
        lottieView = new LottieAnimationView(linearLayout.getContext());
        new_chat_btn.setOnClickListener(v->{
            Chat chat = chatManager.getActiveChat();
            if (chat == null || chat.getMessages().size()==0) {
                return;
            }

            chatManager.createNewChat();
            loadChatsIntoDrawer();
            loadActiveChats();
            drawerLayout.closeDrawer(GravityCompat.START);

        });
        home_btn.setOnClickListener(v->{
            Intent intent = new Intent(ChatUi.this, MainActivity.class);
            startActivity(intent);

        });

        chatManager.setLoadCallback(() -> {
            runOnUiThread(() -> {
                loadActiveChats();
                loadChatsIntoDrawer();
                MarkActiveChat();
            });
        });

        loadActiveChats();

        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        loadChatsIntoDrawer();
        menu_btn.setOnClickListener(view -> {
            drawerLayout.openDrawer(GravityCompat.START);
        });
        setupMenuLongClick();
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == NEW_CHAT_ID) {
                chatManager.createNewChat();
                loadChatsIntoDrawer();
                loadActiveChats();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }

            String chatId = drawerChatIds.get(item.getItemId());

            if (chatId != null) {
                chatManager.switchChat(chatId);
                loadActiveChats();
                MarkActiveChat();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
            return true;
        });

        chatController = app.getChatController();
        send_btn.setOnClickListener(v -> {
            sendMessage();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());

            // Apply Top/Left/Right to root DrawerLayout for status/nav bars
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);

            // Apply Bottom IME/Keyboard padding to the main content layout inside drawer
            View contentLayout = findViewById(R.id.content_layout);
            int bottomPadding = ime.bottom > 0 ? ime.bottom : systemBars.bottom;
            contentLayout.setPadding(0, 0, 0, bottomPadding);

            return insets;
        });
    }

    private void loadActiveChats() {
        linearLayout.removeAllViews();

        Chat chat = chatManager.getActiveChat();
        if (chat == null) {
            return;
        }
        if (chat.getMessages().size()==0) {
            imageView.setVisibility(View.VISIBLE);
        }
        for (ChatMessages messages : chat.getMessages()) {
            if (messages.getRole() == ChatMessages.Role.USER) {
                addUserMessage(messages.getContent());
//                Toast.makeText(this, "view- "+messages.getContent(), Toast.LENGTH_SHORT).show();
            } else {
                TextView aiView = addAiResponseView();
                aiView.setText(messages.getContent());
//                Toast.makeText(this, "view- "+messages.getContent(), Toast.LENGTH_SHORT).show();
            }
        }
        scrollToBottom();
    }

    private void sendMessage() {
        String message = promptInput.getText().toString().trim();
        if (message.isEmpty()) return;

        promptInput.setText("");
        send_btn.setEnabled(false);

        // 1. Add User Message (Orange Bubble, Right Aligned)
        addUserMessage(message);
        int sizeInPx = (int) (50 * linearLayout.getResources().getDisplayMetrics().density);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(sizeInPx, sizeInPx);
        params.gravity = Gravity.START;

        lottieView.setLayoutParams(params);

        // 3. Configure animation properties
        lottieView.setAnimation("loader_th.json");
        lottieView.setRepeatCount(LottieDrawable.INFINITE);
        linearLayout.addView(lottieView);
        lottieView.playAnimation();


        // 2. Prepare AI Response View (No Bubble, Left Aligned, Plain White Text)
        TextView responseView = addAiResponseView();

        chatController.sendMessage(message, new ChatController.Callback() {
            @Override
            public void onToken(String token) {

                runOnUiThread(() -> {
                    lottieView.cancelAnimation();
                    linearLayout.removeView(lottieView);
                    responseView.append(token);
                    scrollToBottom();

                });
            }

            @Override
            public void onSuccess(String response) {
                enableSendBtn();
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    responseView.setTextColor(Color.parseColor("#FF5252")); // Soft red for error
                    responseView.setText("AI Error: " + error);
                    enableSendBtn();
                });
            }
        });
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    /**
     * Adds User message with an Orange Bubble on the Right side.
     */
    private void addUserMessage(String messageText) {
        TextView textView = new TextView(this);
        textView.setText(messageText);
        textView.setTextSize(15);
        textView.setTextColor(Color.WHITE);
        //lotti


        // Apply custom Orange Bubble Drawable
        textView.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_user_message));

        int padH = dpToPx(14);
        int padV = dpToPx(10);
        textView.setPadding(padH, padV, padH, padV);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.gravity = Gravity.END; // Align to Right
        layoutParams.setMargins(
                dpToPx(64), // Margin left so bubble doesn't span full width
                dpToPx(6),  // Margin top
                dpToPx(8),  // Margin right
                dpToPx(6)   // Margin bottom
        );

        textView.setLayoutParams(layoutParams);
        linearLayout.addView(textView);
        scrollToBottom();
    }

    /**
     * Creates and adds an AI Response view with NO bubble background on the Left side.
     */
    private TextView addAiResponseView() {
        if (imageView.getVisibility()==View.VISIBLE) imageView.setVisibility(View.GONE);
        TextView textView = new TextView(this);
        textView.setTextSize(15);
        textView.setTextColor(Color.WHITE); // Pure White Text
        textView.setBackground(null);       // No Bubble / Transparent

        int padH = dpToPx(8);
        int padV = dpToPx(8);
        textView.setPadding(padH, padV, padH, padV);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.gravity = Gravity.START; // Align to Left
        layoutParams.setMargins(
                dpToPx(8),  // Margin left
                dpToPx(6),  // Margin top
                dpToPx(12), // Margin right so text leaves space on right
                dpToPx(6)   // Margin bottom
        );

        textView.setLayoutParams(layoutParams);
        linearLayout.addView(textView);
        return textView;
    }

    private void scrollToBottom() {
        messageScrollview.post(() -> messageScrollview.fullScroll(ScrollView.FOCUS_DOWN));
    }

    private void enableSendBtn() {
        runOnUiThread(() -> {
            send_btn.setEnabled(true);
        });
    }

    private static final int CHATS_GROUP_ID = 20001;

    private void loadChatsIntoDrawer() {
        Menu menu = navigationView.getMenu();
        menu.clear();
        navigationView.inflateMenu(R.menu.drawer_menu);


        drawerChatIds.clear();
        chatDrawerIds.clear();

       // menu.add(R.id.chat_group, NEW_CHAT_ID, Menu.NONE, "Create New Chat +");

        for (Chat chat : chatManager.getChats()) {
            int menuId = View.generateViewId();

            MenuItem menuItem = menu.add(R.id.chat_group, menuId, Menu.NONE, chat.getTitle());
            menuItem.setCheckable(true);
            drawerChatIds.put(menuId, chat.getId());
            chatDrawerIds.put(chat.getId(), menuId);
        }
        MarkActiveChat();
    }

    private void MarkActiveChat() {
        Menu menu = navigationView.getMenu();
        for (Integer menuid : chatDrawerIds.values()) {
            MenuItem menuItem = menu.findItem(menuid);
            if (menuItem != null) menuItem.setChecked(false);
        }
        Chat activeChat = chatManager.getActiveChat();
        if (activeChat == null) return;
        Integer menuId = chatDrawerIds.get(activeChat.getId());
        if (menuId == null) return;
        MenuItem item = navigationView.getMenu().findItem(menuId);
        if (item != null) item.setChecked(true);

    }
    private void showOrangeOptionsPopup(View anchorView, MenuItem menuItem) {
        // 1. Inflate the custom XML layout (dialog_drawer_item_options.xml)
        View customView = LayoutInflater.from(this).inflate(R.layout.menu_dialog, null);

        // 2. Create the PopupWindow with wrap_content dimensions
        PopupWindow popupWindow = new PopupWindow(
                customView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true // Enables touch outside dismiss behavior
        );

        // 3. Configure window visuals & elevation
        popupWindow.setElevation(16f);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // 4. Bind view elements inside the custom layout
        MaterialButton btnRename = customView.findViewById(R.id.btn_rename);
        MaterialButton btnDelete = customView.findViewById(R.id.btn_delete);

        // 5. Handle Rename button click
        btnRename.setOnClickListener(v -> {
            popupWindow.dismiss();
//            showRenameDialog(menuItem);
        });

        // 6. Handle Delete button click
         btnDelete.setOnClickListener(v -> {

            Chat chat_n = chatManager.getActiveChat();
//            Toast.makeText(this, ""+drawerChatIds.get(menuItem.getItemId()), Toast.LENGTH_SHORT).show();
            if (chat_n.getId()==drawerChatIds.get(menuItem.getItemId())){
                chatManager.deleteChat(drawerChatIds.get(menuItem.getItemId()));
                chatManager.createNewChat();
//                loadChatsIntoDrawer();
//                loadActiveChats();
                drawerLayout.closeDrawer(GravityCompat.START);

            }else{
                chatManager.deleteChat(drawerChatIds.get(menuItem.getItemId()));
            }

            Menu menu = navigationView.getMenu();
            menu.clear();
            navigationView.inflateMenu(R.menu.drawer_menu);


//            drawerChatIds.clear();
//            chatDrawerIds.clear();

            // menu.add(R.id.chat_group, NEW_CHAT_ID, Menu.NONE, "Create New Chat +");

            for (Chat chat : chatManager.getChats()) {
                int menuId = View.generateViewId();

                MenuItem menuItems = menu.add(R.id.chat_group, menuId, Menu.NONE, chat.getTitle());
                menuItems.setCheckable(true);
                drawerChatIds.put(menuId, chat.getId());
                chatDrawerIds.put(chat.getId(), menuId);
            }
            loadActiveChats();
            MarkActiveChat();

//            Toast.makeText(this, ""+chat_n.getId(), Toast.LENGTH_SHORT).show();

            popupWindow.dismiss();
//            showDeleteConfirmation(menuItem);
        });

        // 7. Show popup anchored to the long-clicked navigation view item
        // (xOffset and yOffset adjust alignment relative to drawer item bounds)
        popupWindow.showAsDropDown(anchorView, 100, -anchorView.getHeight() / 2);
    }
    private void setupMenuLongClick() {
        navigationView.post(() -> {
            for (int i = 0; i < navigationView.getMenu().size(); i++) {
                MenuItem menuItem = navigationView.getMenu().getItem(i);
                View itemView = navigationView.findViewById(menuItem.getItemId());

                if (itemView != null) {
                    itemView.setOnLongClickListener(v -> {
                        // Show the custom orange step 2 layout
                        showOrangeOptionsPopup(v, menuItem);
                        return true; // Return true to consume long click event
                    });
                }
            }
        });
    }
}
