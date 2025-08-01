package cordova.plugin.uvccamera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Surface;
import android.graphics.SurfaceTexture;
import android.widget.Toast;

import com.serenegiant.usbcameracommon.UVCCameraHandler;
import com.serenegiant.widget.UVCCameraTextureView;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.USBMonitor.UsbControlBlock;
import com.serenegiant.usb.UVCCamera;

import java.io.ByteArrayOutputStream;

public class CameraActivity extends Activity {

  private static UVCCameraTextureView mCameraView;
  private UVCCameraHandler mCameraHandler;
  private USBMonitor mUSBMonitor;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mCameraView = new UVCCameraTextureView(this);
    setContentView(mCameraView);

    mCameraHandler = UVCCameraHandler.createHandler(
        this, mCameraView,
        UVCCamera.DEFAULT_PREVIEW_WIDTH,
        UVCCamera.DEFAULT_PREVIEW_HEIGHT,
        UVCCamera.DEFAULT_PREVIEW_MODE);

    mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
    mUSBMonitor.register();
  }

  private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
    @Override
    public void onAttach(UsbDevice device) {
      Toast.makeText(CameraActivity.this, "USB device attached", Toast.LENGTH_SHORT).show();
      mUSBMonitor.requestPermission(device);
    }

    @Override
    public void onConnect(UsbDevice device, UsbControlBlock ctrlBlock, boolean createNew) {
      Toast.makeText(CameraActivity.this, "USB device connected", Toast.LENGTH_SHORT).show();
      mCameraHandler.open(ctrlBlock);
      final SurfaceTexture st = mCameraView.getSurfaceTexture();
      if (st != null) {
        mCameraHandler.startPreview(new Surface(st));
      }
    }

    @Override
    public void onDisconnect(UsbDevice device, UsbControlBlock ctrlBlock) {
      Toast.makeText(CameraActivity.this, "USB device disconnected", Toast.LENGTH_SHORT).show();
      mCameraHandler.close();
    }

    @Override
    public void onDettach(UsbDevice device) {
      Toast.makeText(CameraActivity.this, "USB device detached", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCancel(UsbDevice device) {
      Toast.makeText(CameraActivity.this, "USB permission cancelled", Toast.LENGTH_SHORT).show();
    }
  };

  // ✅ Metodo per catturare un'immagine dalla preview
  private static Bitmap captureImage() {
    if (mCameraView != null) {
      return mCameraView.getBitmap();
    }
    return null;
  }

  // ✅ Metodo per convertire il Bitmap in base64
  private static String bitmapToBase64(Bitmap bitmap) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
    byte[] byteArray = outputStream.toByteArray();
    return Base64.encodeToString(byteArray, Base64.NO_WRAP);
  }

  // ✅ Metodo per scattare una foto e loggare il risultato
  public static String takeSnapshot() {
    Bitmap bmp = captureImage();
    if (bmp != null) {
      String base64 = bitmapToBase64(bmp);
      Log.d("UVCCamera", "Snapshot base64: " + base64);
      return base64;
    } else {
      Log.w("UVCCamera", "Snapshot failed: no bitmap available");
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    if (mUSBMonitor != null) {
      mUSBMonitor.register();
    }
  }

  @Override
  protected void onStop() {
    if (mUSBMonitor != null) {
      mUSBMonitor.unregister();
    }
    super.onStop();
  }

  @Override
  protected void onDestroy() {
    if (mCameraHandler != null) {
      mCameraHandler.release();
      mCameraHandler = null;
    }
    if (mUSBMonitor != null) {
      mUSBMonitor.destroy();
      mUSBMonitor = null;
    }
    super.onDestroy();
  }
}