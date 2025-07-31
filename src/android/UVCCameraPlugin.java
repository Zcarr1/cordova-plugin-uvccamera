package cordova.plugins.uvccamera;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;

public class UVCCameraPlugin extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("openCamera")) {
            Intent intent = new Intent(cordova.getActivity(), CameraActivity.class);
            cordova.getActivity().startActivity(intent);
            callbackContext.success("Camera opened");
            return true;
        } else if (action.equals("takePicture")) {
            String resultType = args.optString(0, "");
            cordova.getActivity().runOnUiThread(() -> {
                CameraActivity.takeSnapshot(callbackContext);
            });
            return true;
        }
        return false;
    }
}