package io.predict.plugin;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.os.Build;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.predict.PIOTripSegment;
import io.predict.PrecisionMode;
import io.predict.PredictIO;
import io.predict.PredictIOListener;
import io.predict.PredictIOStatus;

public class PredictIOPlugin extends CordovaPlugin implements PredictIOListener {
    private static final String LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_CODE = 1;
    private CallbackContext mCallbackContext;
    private JSONArray mData;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        //ParkTAG SDK code
        PredictIO predictIO = PredictIO.getInstance(getApplicationContext());
        if (predictIO.getListener() == null) {
            // This notifies sdk that app is initialised
            predictIO.setAppOnCreate((Application) getApplicationContext());
            // set this to get event callbacks
            predictIO.setListener(this);
        }
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext)
            throws JSONException {
        PredictIO predictIO = PredictIO.getInstance(getApplicationContext());
        if ("start".equals(action)) {
            if (cordova.hasPermission(LOCATION)) {
                startTracker(data, callbackContext);
            } else {
                mCallbackContext = callbackContext;
                mData = data;
                cordova.requestPermission(this, LOCATION_CODE, LOCATION);
            }
            return true;
        } else if ("stop".equals(action)) {
            stopTracker(callbackContext);
            return true;
        } else if ("setListener".equals(action)) {
            predictIO.setListener(this);
            return true;
        } else if ("status".equals(action)) {
            String message;
            switch (predictIO.getStatus()) {
                case ACTIVE:
                    message = "ACTIVE";
                    break;
                case LOCATION_DISABLED:
                    message = "LOCATION_DISABLED";
                    break;
                case AIRPLANE_MODE_ENABLED:
                    message = "AIRPLANE_MODE_ENABLED";
                    break;
                case INSUFFICIENT_PERMISSION:
                    message = "INSUFFICIENT_PERMISSION";
                    break;
                default:
                case IN_ACTIVE:
                    message = "IN_ACTIVE";
                    break;
            }
            callbackContext.success(message);
            return true;
        } else if ("isSearchingInPerimeterEnabled".equals(action)) {
            boolean isSearchParkingEnable = predictIO.isSearchingInPerimeterEnabled();
            callbackContext.success(String.valueOf(isSearchParkingEnable));
            return true;
        } else if ("precision".equals(action)) {
            String precision = predictIO.getPrecision() == PrecisionMode.HIGH ? "HIGH" : "LOW";
            callbackContext.success(precision);
            return true;
        } else if ("deviceIdentifier".equals(action)) {
            String deviceIdentifier = predictIO.getDeviceIdentifier();
            callbackContext.success(deviceIdentifier);
            return true;
        } else {
            return false;
        }
    }

    private void startTracker(JSONArray params, final CallbackContext callbackContext) {
        PrecisionMode precisionMode = PrecisionMode.HIGH;
        boolean isSearchingInPerimeterEnaled = false;
        if (params != null) {
            try {
                precisionMode = params.optBoolean(0, true) ? PrecisionMode.HIGH : PrecisionMode.LOW;
                isSearchingInPerimeterEnaled = params.optBoolean(1, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            PredictIO predictIO = PredictIO.getInstance(getApplicationContext());
            predictIO.setPrecision(precisionMode);
            predictIO.enableSearchingInPerimeter(isSearchingInPerimeterEnaled);

            //Validate tracker not already running
            if (predictIO.getStatus() == PredictIOStatus.ACTIVE) {
                callbackContext.success();
                return;
            }

            //Validate google play services
            final GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
            int resultCode = apiAvailability.isGooglePlayServicesAvailable(getApplicationContext());
            if (resultCode != ConnectionResult.SUCCESS) {
                if (apiAvailability.isUserResolvableError(resultCode)) {
                    apiAvailability.getErrorDialog(this.cordova.getActivity(), resultCode, 1000).show();
                }
                callbackContext.success();
                return;
            }

            //noinspection MissingPermission
            predictIO.start(new PredictIO.PIOActivationListener() {
                @Override
                public void onActivated() {
                    callbackContext.success();
                }

                @Override
                public void onActivationFailed(int error) {
                    switch (error) {
                        case 401:
							callbackContext.error("Please verify your API_KEY!");
                            break;
                        case 403:
							callbackContext.error("Please grant all required permissions");
                            break;
                    }
                }
            });
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private void stopTracker(CallbackContext callbackContext) {
        try {
            PredictIO.getInstance(getApplicationContext()).stop();
            callbackContext.success();
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public Context getApplicationContext() {
        return this.cordova.getActivity().getApplicationContext();
    }

    @Override
    public void departed(PIOTripSegment tripSegment) {
        String param = getJsonParams(tripSegment);
        evaluateJavascript("departed('" + param + "')");
    }

    @Override
    public void searchingInPerimeter(Location location) {
        if (location != null) {
            evaluateJavascript("searchingInPerimeter('" + location.toString() + "')");
        }
    }

    @Override
    public void arrivalSuspected(PIOTripSegment tripSegment) {
        String param = getJsonParams(tripSegment);
        evaluateJavascript("arrivalSuspected('" + param + "')");
    }

    @Override
    public void arrived(PIOTripSegment tripSegment) {
        String param = getJsonParams(tripSegment);
        evaluateJavascript("arrived('" + param + "')");
    }

    @Override
    public void departureCanceled() {
        evaluateJavascript("departureCanceled()");
    }

    @Override
    public void didUpdateLocation(Location location) {
        if (location != null) {
            evaluateJavascript("didUpdateLocation('" + location.toString() + "')");
        }
    }

    @Override
    public void transportationMode(PIOTripSegment tripSegment) {
        String param = getJsonParams(tripSegment);
        evaluateJavascript("transportationMode('" + param + "')");
    }

    private String getJsonParams(PIOTripSegment tripSegment) {
        JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("UUID", tripSegment.UUID);
            if (tripSegment.departureTime != null) {
                jsonParam.put("departureTime", tripSegment.departureTime.getTime());
            }
            if (tripSegment.departureLocation != null) {
                jsonParam.put("departureLatitude", tripSegment.departureLocation.getLatitude());
                jsonParam.put("departureLongitude", tripSegment.departureLocation.getLongitude());
            }
            if (tripSegment.arrivalTime != null) {
                jsonParam.put("arrivalTime", tripSegment.arrivalTime.getTime());
            }
            if (tripSegment.arrivalLocation != null) {
                jsonParam.put("arrivalLatitude", tripSegment.arrivalLocation.getLatitude());
                jsonParam.put("arrivalLongitude", tripSegment.arrivalLocation.getLongitude());
            }
            if (tripSegment.transportationMode != null) {
                jsonParam.put("transportationMode", tripSegment.transportationMode.name());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonParam.toString();
    }

    private void evaluateJavascript(final String js) {
        if (webView != null && webView.getView() != null) {
            webView.getView().post(new Runnable() {
                public void run() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        webView.sendJavascript(js);
                    } else {
                        webView.loadUrl("javascript:" + js);
                    }
                }
            });
        }
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults)
            throws JSONException {
        switch (requestCode) {
            case LOCATION_CODE:
                if (mCallbackContext != null) {
                    startTracker(mData, mCallbackContext);
                }
                break;
        }
    }
}