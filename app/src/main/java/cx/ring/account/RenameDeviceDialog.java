package cx.ring.account;

import android.app.Dialog;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import cx.ring.R;
import cx.ring.databinding.DialogDeviceRenameBinding;

public class RenameDeviceDialog extends DialogFragment {
    public static final String DEVICENAME_KEY = "devicename_key";
    static final String TAG = RenameDeviceDialog.class.getSimpleName();
    private RenameDeviceListener mListener = null;
    private DialogDeviceRenameBinding binding;

    public void setListener(RenameDeviceListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = DialogDeviceRenameBinding.inflate(getActivity().getLayoutInflater());

        binding.ringDeviceNameTxt.setText(getArguments().getString(DEVICENAME_KEY));
        binding.ringDeviceNameTxt.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                boolean validationResult = validate();
                if (validationResult) {
                    getDialog().dismiss();
                }
                return validationResult;
            }
            return false;
        });

        final AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(binding.getRoot())
                .setTitle(R.string.rename_device_title)
                .setMessage(R.string.rename_device_message)
                .setPositiveButton(R.string.rename_device_button, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.setOnShowListener(dialog1 -> {
            Button button = ((AlertDialog) dialog1).getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view1 -> {
                if (validate()) {
                    dialog1.dismiss();
                }
            });
        });
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return dialog;
    }

    @Override
    public void onDestroy() {
        mListener = null;
        super.onDestroy();
    }

    private boolean checkInput(String input) {
        if (input.isEmpty()) {
            binding.ringDeviceNameTxtBox.setErrorEnabled(true);
            binding.ringDeviceNameTxtBox.setError(getText(R.string.account_device_name_empty));
            return false;
        } else {
            binding.ringDeviceNameTxtBox.setErrorEnabled(false);
            binding.ringDeviceNameTxtBox.setError(null);
        }
        return true;
    }

    private boolean validate() {
        String input = binding.ringDeviceNameTxt.getText().toString().trim();
        if (checkInput(input) && mListener != null) {
            mListener.onDeviceRename(input);
            return true;
        }
        return false;
    }

    public interface RenameDeviceListener {
        void onDeviceRename(String newName);
    }
}
