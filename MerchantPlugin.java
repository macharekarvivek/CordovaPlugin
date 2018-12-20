package android.mp.MerchantPlugin;

import android.widget.Toast;

import com.payphi.merchantsdk.PayPhiSdk;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * This class invoke PayPhi SDK interface methods from JavaScript.
 */
public class MerchantPlugin extends CordovaPlugin {
    Random rnd = new Random();
    JSONObject response;
    CallbackContext callbackCurrentID;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        callbackCurrentID = callbackContext;
        if (action.equals("setAppInfoMethod")) {
            JSONObject obj = (JSONObject) args.get(0);
            String mid = obj.getString("merchantID");
            String appid = obj.getString("appID");
            String merchantName = obj.getString("merchantName");
            String env = obj.getString("environment");
            //System.out.println("Args=-"+args);
            this.setAppInfoMethod(mid, appid, merchantName, env, callbackCurrentID);
            return true;
        } else if (action.equals("paymentCallMethod")) {
            try {
                System.out.println("Args=-" + args);
                JSONObject obj = (JSONObject) args.get(0);
                String merchantId = obj.getString("merchantID");
                String amount = obj.getString("amount");
                String orderId = String.valueOf(100000 + rnd.nextInt(900000));
                String currencyCode = obj.getString("currencyCode");
                String CustomerEmailID = obj.getString("CustomerEmailID");
                String InvoiceNo = obj.getString("InvoiceNo");
                String MobileNo = obj.getString("MobileNo");
                String DeviceID = obj.getString("appID");
                String paymentInstID = obj.getString("paymentInstID");
                String addlParam1 = obj.getString("addlParam1");
                String addlParam2 = obj.getString("addlParam2");
                String keyString = obj.getString("keyString");
                String appID = obj.getString("appID");

                String msg = amount + currencyCode + merchantId + orderId;
                String secureToken = hmacDigest(msg, keyString);

                Map map = new HashMap();
                map.put("Amount", amount);
                map.put("MerchantTxnNo", orderId);
                map.put("CurrencyCode", currencyCode);
                map.put("MerchantID", merchantId);
                map.put("SecureToken", secureToken);
                map.put("CustomerEmailID", CustomerEmailID);
                map.put("InvoiceNo", InvoiceNo);
                map.put("MobileNo", MobileNo);
                map.put("DeviceID", DeviceID);
                map.put("paymentInstID", paymentInstID);
                map.put("addlParam1", addlParam1);
                map.put("addlParam2", addlParam2);
                this.paymentCallMethod(map, callbackCurrentID);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

//--------------------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------
    //Method to SetAppInfo for SDk,Setting Env ,logint into System , And Setting merchantName
//--------------------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------
    private void setAppInfoMethod(String mid, String appid, String merchantName, String Environment, CallbackContext callbackContext) {
        if (Environment.equals("QA")) {
            PayPhiSdk.setEnv(PayPhiSdk.QA);
        } else if (Environment.equals("PROD")) {
            PayPhiSdk.setEnv(PayPhiSdk.PROD);
        }

        PayPhiSdk.setAppInfo(mid, appid, cordova.getActivity(), new PayPhiSdk.IAppInitializationListener() {

            @Override
            public void onSuccess(String s) {
                cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, s);
                        pluginResult.setKeepCallback(true);
                        callbackContext.sendPluginResult(pluginResult);
                    }
                });
            }

            @Override
            public void onFailure(String s) {
                cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, s);
                        pluginResult.setKeepCallback(true);
                        callbackContext.sendPluginResult(pluginResult);
                    }
                });
            }
        });

        PayPhiSdk.setMerchantName(cordova.getActivity(), merchantName);

    }

//--------------------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------
    //method to invoke payphi sdk make payment screen..
//--------------------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------
    public void paymentCallMethod(Map map, CallbackContext callbackContext) {
        callbackCurrentID = callbackContext;
        android.support.v4.app.Fragment fragment = PayPhiSdk.makePayment(cordova.getActivity(), map, PayPhiSdk.DIALOG, new PayPhiSdk.IAppPaymentResponseListener() {
            @Override
            public void onPaymentResponse(final int resultCode, Map<String, String> map) {
                try {
                    response = new JSONObject(map);
                    response.put("returnCode", resultCode);
                    System.out.println("response obj=" + response.toString());
                    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, response);
                    pluginResult.setKeepCallback(false);
                    if (callbackCurrentID != null) {
                        callbackCurrentID.sendPluginResult(pluginResult);
                        callbackCurrentID = null;
                    }

                } catch (Exception e) {
                    Toast.makeText(cordova.getActivity(), "error=" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    callbackCurrentID.error("Error : " + e.getMessage());
                    callbackCurrentID = null;
                    e.printStackTrace();
                }

            }
        });
    }

//--------------------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------
    //Method for Creating hmac Secure Token
    public static String hmacDigest(String msg, String keyString) {
        String digest = null;
        try {
            SecretKeySpec key = new SecretKeySpec((keyString).getBytes("UTF-8"), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            byte[] bytes = mac.doFinal(msg.getBytes("ASCII"));
            StringBuffer hash = new StringBuffer();
            for (int i = 0; i < bytes.length; i++) {
                String hex = Integer.toHexString(0xFF & bytes[i]);
                if (hex.length() == 1) {
                    hash.append('0');
                }
                hash.append(hex);
            }
            digest = hash.toString();
        } catch (UnsupportedEncodingException e) {
        } catch (InvalidKeyException e) {
        } catch (NoSuchAlgorithmException e) {
        }
        return digest;
    }
//--------------------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------

}
