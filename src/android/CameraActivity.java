package cordova.plugins.uvccamera;

import org.apache.cordova.*;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import android.graphics.Bitmap;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

import android.hardware.usb.UsbDevice;

import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.USBMonitor.OnDeviceConnectListener;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usbcameracommon.UVCCameraHandler;
import com.serenegiant.widget.UVCCameraTextureView;
import com.serenegiant.uvccamera.*;


public class CameraActivity extends Activity {

  private USBMonitor mUSBMonitor;
  private UVCCameraTextureView mCameraTextureView;
  public static UVCCameraHandler mCameraHandler;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mCameraTextureView = new UVCCameraTextureView(this);
    setContentView(mCameraTextureView);

    mUSBMonitor = new USBMonitor(this, onDeviceConnectListener);
    mCameraHandler = UVCCameraHandler.createHandler(this, mCameraTextureView,
        UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT);

    mUSBMonitor.register();
  }

  private final USBMonitor.OnDeviceConnectListener onDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
    @Override
    public void onAttach(UsbDevice device) {
      mUSBMonitor.requestPermission(device);
    }

    @Override
    public void onConnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
      mCameraHandler.open(ctrlBlock);
      mCameraHandler.startPreview();
    }

    @Override
    public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
      mCameraHandler.close();
    }

    @Override
    public void onDettach(UsbDevice device) {
    }

    @Override
    public void onCancel(UsbDevice device) {
    }
  };

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mCameraHandler != null)
      mCameraHandler.release();
    if (mUSBMonitor != null)
      mUSBMonitor.unregister();
  }

  public static void requestSnapshot(String resultType, CallbackContext callbackContext) {
    if (mCameraHandler != null && mCameraHandler.isOpened()) {
      mCameraHandler.captureStill(new UVCCameraHandler.OnCaptureListener() {
        @Override
        public void onCapture(Bitmap bitmap) {
          if (resultType == "base64") {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            byte[] imageBytes = outputStream.toByteArray();
            String base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

            callbackContext.success(base64Image);
          } else {
            File photoDir = new File(Environment.getExternalStorageDirectory(), "UVCCamera");
            if (!photoDir.exists()) {
              photoDir.mkdirs();
            }

            String filename = "photo_" + System.currentTimeMillis() + ".jpg";
            File photoFile = new File(photoDir, filename);

            try {
              FileOutputStream out = new FileOutputStream(photoFile);
              bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
              out.flush();
              out.close();
              callbackContext.success(photoFile.getAbsolutePath());
            } catch (IOException e) {
              callbackContext.error("Errore salvataggio immagine: " + e.getMessage());
            }
          }
        }
      });
    } else {
      callbackContext.error("Camera non disponibile");
    }
  }

}