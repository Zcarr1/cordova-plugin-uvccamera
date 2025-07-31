package cordova.plugins.uvccamera;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;

public class UVCCamera extends CordovaPlugin {

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
                CameraActivity.requestSnapshot(resultType, callbackContext);
            });
            return true;
        }
        return false;
    }
}