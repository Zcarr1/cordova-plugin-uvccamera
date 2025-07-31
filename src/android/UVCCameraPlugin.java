package cordova.plugins.uvccamera;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;

public class UVCCameraPlugin extends CordovaPlugin {

    private CallbackContext callbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;

        if ("openCamera".equals(action)) {
            Intent intent = new Intent(cordova.getActivity(), CameraActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            cordova.getActivity().startActivity(intent);
            callbackContext.success("Camera opened");
            return true;
        }

        if ("takeSnapshot".equals(action)) {
            cordova.getThreadPool().execute(() -> {
                String base64 = CameraActivity.takeSnapshot();
                if (base64 != null) {
                    callbackContext.success(base64);
                } else {
                    callbackContext.error("Snapshot failed");
                }
            });
            return true;
        }

        return false;
    }
}
