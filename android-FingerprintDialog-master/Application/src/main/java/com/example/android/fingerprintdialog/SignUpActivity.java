    package com.example.android.fingerprintdialog;

    import android.app.KeyguardManager;
    import android.content.Context;
    import android.hardware.fingerprint.FingerprintManager;
    import android.os.Build;
    import android.os.Bundle;
    import android.preference.PreferenceManager;
    import android.security.keystore.KeyProperties;
    import android.support.v7.app.AppCompatActivity;
    import android.text.Editable;
    import android.text.TextUtils;
    import android.text.TextWatcher;
    import android.view.KeyEvent;
    import android.view.View;
    import android.view.View.OnClickListener;
    import android.view.inputmethod.EditorInfo;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.TextView;
    import android.widget.Toast;

    import com.example.android.fingerprintdialog.R;

    import java.security.KeyStore;
    import java.security.KeyStoreException;
    import java.security.NoSuchAlgorithmException;
    import java.security.NoSuchProviderException;

    import javax.crypto.Cipher;
    import javax.crypto.KeyGenerator;
    import javax.crypto.NoSuchPaddingException;

    /**
     * A login screen that offers login via email/password.
     */
    public class SignUpActivity extends MainActivity {

        public void attemptLogin() {
            //    if (mAuthTask != null) {
            //      return;
            // }

            // Reset errors.
            mUinView.setError(null);
            mPasswordView.setError(null);
            mEmailView.setError(null);

            // Store values at the time of the login attempt.
            String uin = mUinView.getText().toString();
            String email = mEmailView.getText().toString();
            String password = mPasswordView.getText().toString();

            boolean cancel = false;
            View focusView = null;

            // Check for a valid password, if the user entered one.
            if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
                mPasswordView.setError(getString(R.string.error_invalid_password));
                focusView = mPasswordView;
                cancel = true;
            }

            // Check for a valid email address.
            if (TextUtils.isEmpty(email)) {
                mEmailView.setError(getString(R.string.error_field_required));
                focusView = mEmailView;
                cancel = true;
            } else if (!isEmailValid(email)) {
                mEmailView.setError(getString(R.string.error_invalid_email));
                focusView = mEmailView;
                cancel = true;
            }

            // Check for a valid uin.
            if (TextUtils.isEmpty(uin)) {
                mUinView.setError(getString(R.string.error_field_required));
                focusView = mUinView;
                cancel = true;
            }

            if (cancel) {
                // There was an error; don't attempt login and focus the first
                // form field with an error.
                focusView.requestFocus();
            }
            //
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            //     showProgress(true);
            // mAuthTask = new UserLoginTask(email, password);
            //mAuthTask.execute((Void) null);

        }

        public boolean isEmailValid(String email) {
            //TODO: Replace this with your own logic
            return email.contains("@");
        }

        public boolean isPasswordValid(String password) {
            //TODO: Replace this with your own logic
            return password.length() >= 8;
        }
        public void enableButton() {
            String uin = mUinView.getText().toString();
            String email = mEmailView.getText().toString();
            String password = mPasswordView.getText().toString();

            if (uin.isEmpty() || email.isEmpty() || password.isEmpty() || !isEmailValid(email) || !isPasswordValid(email)) {
                mSignUpButton.setEnabled(false);
                mSignUpButton.setClickable(false);
                return;
            }
            mSignUpButton.setEnabled(true);
            mSignUpButton.setClickable(true);
        }


        /**
         * Keep track of the login task to ensure we can cancel it if requested.
         */
        //private UserLoginTask mAuthTask = null;

        // UI references.
        private EditText mUinView;
        private EditText mEmailView;
        private EditText mPasswordView;
        private Button mSignUpButton;

        private Context mContext;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_sign_up);


            try {
                mKeyStore = KeyStore.getInstance("AndroidKeyStore");
            } catch (KeyStoreException e) {
                throw new RuntimeException("Failed to get an instance of KeyStore", e);
            }
            try {
                mKeyGenerator = KeyGenerator
                        .getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
                throw new RuntimeException("Failed to get an instance of KeyGenerator", e);
            }

            Cipher defaultCipher;
            Cipher cipherNotInvalidated;
            try {
                defaultCipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                        + KeyProperties.BLOCK_MODE_CBC + "/"
                        + KeyProperties.ENCRYPTION_PADDING_PKCS7);
                cipherNotInvalidated = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                        + KeyProperties.BLOCK_MODE_CBC + "/"
                        + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                throw new RuntimeException("Failed to get an instance of Cipher", e);
            }
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

            KeyguardManager keyguardManager = getSystemService(KeyguardManager.class);
            FingerprintManager fingerprintManager = getSystemService(FingerprintManager.class);
            Button purchaseButton = (Button) findViewById(R.id.figure_print_button);
            // Button purchaseButtonNotInvalidated = (Button) findViewById(
            //      R.id.purchase_button_not_invalidated);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //  purchaseButtonNotInvalidated.setEnabled(true);
//            purchaseButtonNotInvalidated.setOnClickListener(
//                    new PurchaseButtonClickListener(cipherNotInvalidated,
//                            KEY_NAME_NOT_INVALIDATED));
            }
//        else {
//            // Hide the purchase button which uses a non-invalidated key
//            // if the app doesn't work on Android N preview
//            purchaseButtonNotInvalidated.setVisibility(View.GONE);
//            findViewById(R.id.purchase_button_not_invalidated_description)
//                    .setVisibility(View.GONE);
//        }

            if (!keyguardManager.isKeyguardSecure()) {
                // Show a message that the user hasn't set up a fingerprint or lock screen.
                Toast.makeText(this,
                        "Secure lock screen hasn't set up.\n"
                                + "Go to 'Settings -> Security -> Fingerprint' to set up a fingerprint",
                        Toast.LENGTH_LONG).show();
                purchaseButton.setEnabled(false);
                // purchaseButtonNotInvalidated.setEnabled(false);
                return;
            }

            // Now the protection level of USE_FINGERPRINT permission is normal instead of dangerous.
            // See http://developer.android.com/reference/android/Manifest.permission.html#USE_FINGERPRINT
            // The line below prevents the false positive inspection from Android Studio
            // noinspection ResourceType
            if (!fingerprintManager.hasEnrolledFingerprints()) {
                purchaseButton.setEnabled(false);
                // This happens when no fingerprints are registered.
                Toast.makeText(this,
                        "Go to 'Settings -> Security -> Fingerprint' and register at least one fingerprint",
                        Toast.LENGTH_LONG).show();
                return;
            }
            createKey(DEFAULT_KEY_NAME, true);
            createKey(KEY_NAME_NOT_INVALIDATED, false);
//            purchaseButton.setEnabled(true);
//            purchaseButton.setOnClickListener(
//                    new PurchaseButtonClickListener(defaultCipher, DEFAULT_KEY_NAME));

            mContext = this;
            // Set up the login form.
            mUinView = (EditText) findViewById(R.id.uin);
            mUinView.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable arg0) {
                    enableButton();
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
            });

            mPasswordView = (EditText) findViewById(R.id.password_signup);
            mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                    if (id == EditorInfo.IME_NULL) {
                        attemptLogin();
                        return true;
                    }
                    return false;
                }
            });
            mPasswordView.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable arg0) {
                    enableButton();
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
            });


            mEmailView = (EditText) findViewById(R.id.email_signup);
            mEmailView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                    if (id == EditorInfo.IME_NULL) {
                        attemptLogin();
                        return true;
                    }
                    return false;
                }
            });
            mEmailView.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable arg0) {
                    enableButton();
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
            });


            mSignUpButton = (Button) findViewById(R.id.figure_print_button_signup);
            mSignUpButton.setEnabled(false);
            mSignUpButton.setClickable(false);

            mSignUpButton.setOnClickListener(
                    new SignUpButtonListener(defaultCipher, DEFAULT_KEY_NAME));


        }

        private class SignUpButtonListener implements View.OnClickListener {

            Cipher mCipher;
            String mKeyName;

            SignUpButtonListener(Cipher cipher, String keyName) {
                mCipher = cipher;
                mKeyName = keyName;
            }

            @Override
            public void onClick(View view) {
                // findViewById(R.id.confirmation_message).setVisibility(View.GONE);
                // findViewById(R.id.encrypted_message).setVisibility(View.GONE);

                // Set up the crypto object for later. The object will be authenticated by use
                // of the fingerprint.
                if (initCipher(mCipher, mKeyName)) {

                    // Show the fingerprint dialog. The user has the option to use the fingerprint with
                    // crypto, or you can fall back to using a server-side verified password.
                    FingerprintAuthenticationDialogFragment fragment
                            = new FingerprintAuthenticationDialogFragment();
                    fragment.setCryptoObject(new FingerprintManager.CryptoObject(mCipher));
                    boolean useFingerprintPreference = mSharedPreferences
                            .getBoolean(getString(R.string.use_fingerprint_to_authenticate_key),
                                    true);
                    if (useFingerprintPreference) {
                        fragment.setStage(
                                FingerprintAuthenticationDialogFragment.Stage.FINGERPRINT);
                    } else {
                        fragment.setStage(
                                FingerprintAuthenticationDialogFragment.Stage.PASSWORD);
                    }
                    fragment.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
                } else {
                    // This happens if the lock screen has been disabled or or a fingerprint got
                    // enrolled. Thus show the dialog to authenticate with their password first
                    // and ask the user if they want to authenticate with fingerprints in the
                    // future
                    FingerprintAuthenticationDialogFragment fragment
                            = new FingerprintAuthenticationDialogFragment();
                    fragment.setCryptoObject(new FingerprintManager.CryptoObject(mCipher));
                    fragment.setStage(
                            FingerprintAuthenticationDialogFragment.Stage.NEW_FINGERPRINT_ENROLLED);
                    fragment.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
                }
                Toast.makeText(mContext, "sign up", Toast.LENGTH_LONG).show();
            }


            //mLoginFormView = findViewById(R.id.login_form);
            //mProgressView = findViewById(R.id.login_progress);

            }




    }
