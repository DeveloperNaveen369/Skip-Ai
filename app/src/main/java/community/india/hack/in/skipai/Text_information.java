package community.india.hack.in.skipai;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Text_information extends AppCompatActivity {
    TextView info_view;
    String terms =
            "<h2>Terms & Conditions - Skip AI</h2>" +

                    "<b>Last Updated:</b> July 2026<br><br>" +

                    "Welcome to Skip AI. By using this application, you agree to the following Terms & Conditions.<br><br>" +

                    "<b>1. About Skip AI</b><br><br>" +

                    "Skip AI is an Android application designed to save your time by allowing you to perform small AI-related tasks directly from selected text without opening separate AI chatbot applications. The purpose of Skip AI is to provide quick and convenient text-based AI assistance.<br><br>" +

                    "<b>Skip AI is intended primarily for:</b><br>" +
                    "• Text explanations.<br>" +
                    "• Text summarization.<br>" +
                    "• Translation.<br>" +
                    "• Small AI-related queries based on selected text.<br>" +
                    "• Other text operations supported by the application.<br><br>" +

                    "<b>2. Privacy Policy</b><br><br>" +

                    "Your privacy is important to us.<br><br>" +

                    "• Skip AI does NOT collect, store, or share any personal user data.<br>" +
                    "• Skip AI does NOT maintain its own servers for storing user information.<br>" +
                    "• Your selected text is processed only for generating AI responses.<br>" +
                    "• Your API keys are stored locally on your device and are never transmitted to us.<br><br>" +

                    "We do not sell, collect, or monitor your personal information.<br><br>" +

                    "<b>3. Third-Party Services</b><br><br>" +

                    "Skip AI currently uses AI services provided by third-party providers such as OpenRouter and its supported AI models.<br><br>" +

                    "<b>Please note:</b><br>" +
                    "• OpenRouter and the AI providers available through OpenRouter may collect and process data according to their own Privacy Policies and Terms of Service.<br>" +
                    "• We do not control how third-party providers process or store your requests.<br>" +
                    "• By using Skip AI, you acknowledge that selected text may be sent to third-party AI providers for generating responses.<br><br>" +

                    "We strongly recommend reviewing the privacy policies of the services you choose to use.<br><br>" +

                    "<b>4. Using Your Own API Key</b><br><br>" +

                    "Skip AI allows users to provide their own API keys.<br><br>" +

                    "<b>For maximum privacy and control:</b><br>" +
                    "• We recommend using your own API key.<br>" +
                    "• Your API key remains stored locally on your device.<br>" +
                    "• Skip AI never uploads or shares your API key with us.<br><br>" +

                    "<b>You are solely responsible for:</b><br>" +
                    "• Managing your API key.<br>" +
                    "• Any charges associated with your AI service provider.<br>" +
                    "• Keeping your API credentials secure.<br><br>" +

                    "<b>5. Accessibility Service Usage</b><br><br>" +

                    "Skip AI uses Android Accessibility Services exclusively for text-selection operations.<br><br>" +

                    "<b>Skip AI:</b><br>" +
                    "• Monitors text selection events only.<br>" +
                    "• Displays the floating AI assistant when text is selected.<br>" +
                    "• Performs text-related AI operations requested by the user.<br><br>" +

                    "<b>Skip AI DOES NOT:</b><br>" +
                    "• Record your screen.<br>" +
                    "• Monitor keyboard inputs.<br>" +
                    "• Track your browsing activity.<br>" +
                    "• Read unrelated application content intentionally.<br>" +
                    "• Collect personal information.<br><br>" +

                    "The Accessibility Service exists solely to provide the text-selection functionality of Skip AI.<br><br>" +

                    "<b>6. Open Source Commitment</b><br><br>" +

                    "Skip AI is an open-source project.<br><br>" +

                    "The complete source code of the application will be made publicly available on our GitHub repository within 48 hours of the official launch of each public release.<br><br>" +

                    "<b>Users are encouraged to:</b><br>" +
                    "• Review the source code.<br>" +
                    "• Report issues.<br>" +
                    "• Suggest improvements.<br>" +
                    "• Contribute to the project.<br><br>" +

                    "<b>7. Limitations of AI Responses</b><br><br>" +

                    "AI-generated responses may occasionally be:<br>" +
                    "• Incorrect.<br>" +
                    "• Incomplete.<br>" +
                    "• Misleading.<br>" +
                    "• Outdated.<br><br>" +

                    "Users should independently verify important information before relying upon it.<br><br>" +

                    "<b>Skip AI should not be used as:</b><br>" +
                    "• Medical advice.<br>" +
                    "• Legal advice.<br>" +
                    "• Financial advice.<br>" +
                    "• Professional consultation.<br><br>" +

                    "<b>8. User Responsibility</b><br><br>" +

                    "Users are responsible for:<br>" +
                    "• The text they submit to AI providers.<br>" +
                    "• The API keys they provide.<br>" +
                    "• Compliance with applicable laws and the terms of the AI providers they choose to use.<br><br>" +

                    "<b>9. Support & Queries</b><br><br>" +

                    "If you:<br>" +
                    "• Encounter any issues,<br>" +
                    "• Wish to report bugs,<br>" +
                    "• Have suggestions or questions,<br><br>" +

                    "Please visit the Issues section available on the application's Home Page and join our Telegram support group.<br><br>" +

                    "<b>10. Acceptance of Terms</b><br><br>" +

                    "By using Skip AI, you agree to these Terms & Conditions and acknowledge the use of third-party AI providers for generating responses.<br><br>" +

                    "<b>Skip AI</b><br>" +
                    "<i>"+"Save time. Select text. Get answers instantly."+"</i>";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_text_information);
        info_view = findViewById(R.id.info_text_view);
        Intent intent = getIntent();

        if (intent.getBooleanExtra("terms",false)){
                info_view.setText(Html.fromHtml(terms,Html.FROM_HTML_MODE_LEGACY));
        }else{
            info_view.setText(R.string.using_steps);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}