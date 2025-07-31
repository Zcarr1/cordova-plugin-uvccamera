package cordova.plugin.uvccamera;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.serenegiant.usb.USBMonitor;
import com.serenegiant.uvccamera.*;

public class CameraActivity extends Activity {

  private USBMonitor mUSBMonitor;
  private UVCCameraView mCameraView;
  public static UVCCameraHandler mCameraHandler;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mCameraView = new UVCCameraTextureView(this);
    setContentView(mCameraView);

    mUSBMonitor = new USBMonitor(this, onDeviceConnectListener);
    mCameraHandler = UVCCameraHandler.createHandler(this, mCameraView,
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

  public static void requestSnapshot(CallbackContext callbackContext) {
    if (mCameraHandler != null && mCameraHandler.isOpened()) {
      File photoDir = new File(Environment.getExternalStorageDirectory(), "UVCCamera");
      if (!photoDir.exists()) {
        photoDir.mkdirs();
      }

      String filename = "photo_" + System.currentTimeMillis() + ".jpg";
      File photoFile = new File(photoDir, filename);

      mCameraHandler.captureStill(new UVCCameraHandler.OnCaptureListener() {
        @Override
        public void onCapture(Bitmap bitmap) {
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
      });
    } else {
      callbackContext.error("Camera non disponibile");
    }
  }
}