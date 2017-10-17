public abstract class DroneController {

    private static final String TAG = "DroneControllerAPI";

    public class DroneVersionInfo {
        public int mModel;
        public int mSerialNumber;
        public byte[] mFirmwareVersion;
    }

   
    public interface HeartbeatReportListener {
        void notify(HeartbeatReport hbr);
    }

    
    public interface BatteryStatusListener {
        void notify(BatteryStatus batteryStatus);
    }

   
    public static class Setpoint {
        
        public float mRoll;
        public float mPitch;
        public float mYaw;
        public float mThrust;

        public short mFlightModeSwitch;
    }

   
    public static class CommandLong {
        public byte target_system = 1;
        public byte target_component = 0;
        public short command;
        public byte confirmation; 
        public int param1;
        public int param2;
        public int param3;
        public int param4;
        public int param5;
        public int param6;
        public int param7;
    }

    
    public static class BatteryStatus {
        public int mId;
        public int mBatteryFunction;
        public int mType;
        public int mTemperature;
        public short[] mVoltages;
        public int mCurrentBattery;
        public int mCurrentConsumed;
        public int mEnergyConsumed;
        public int mBatteryRemaining;

        public BatteryStatus() {
            mId = -1;
            mBatteryFunction = -1;
            mType = -1;
            mTemperature = -1;
            mVoltages = new short[ 10 ];
            mCurrentBattery = -1;
            mCurrentConsumed = -1;
            mEnergyConsumed = -1;
            mBatteryRemaining = -1;
        }
    }

    
    public static class HeartbeatReport {
        public int mCustomMode;
        public int mType;
        public int mAutopilot;
        public int mBaseMode;
        public int mSystemStatus;
        public int mMAVLinkVersion;

        public HeartbeatReport() {
            mCustomMode = -1;
            mType = -1;
            mAutopilot = -1;
            mBaseMode = -1;
            mSystemStatus = -1;
            mMAVLinkVersion = -1;
        }
    }

  

    private MAVLinkMessenger mMAVLinkMessenger;
    private ThreadWrapper mSetpointTxThread;
    private ThreadWrapper mCommandLongTxThread;
    private int mSetpointTxIntervalMs = 20;
    private int mCommandLongTxIntervalMs = 100;
    private boolean mConnected;
    private MAVLinkTransport mMavLinkTransport;
    private ConnectionListener mConnectionListener;

    private ConnectionListener mBTLEConnectionListener = new ConnectionListener() {
        @Override
        public void onStateChange(final String deviceAddr, ConnectionState newState) {
            switch (newState) {
                case CONNECTED:
                    startSetpoinTxThread();
                    break;
                case CONNECTING:
                    break;
                case DISCONNECTED:
                    stopThread();
                    break;
            }
            if (mConnectionListener != null) {
                mConnectionListener.onStateChange(deviceAddr, newState);
            }
        }
    };

    public DroneCtrlApi() {
        
        mMAVLinkMessenger = new MAVLinkMessenger();
    }

    private void startSetpoinTxThread() {
        if (mSetpointTxThread == null) {
            mSetpointTxThread = new ThreadWrapper() {
                @Override
                public void run() {
                    while (!mQuit) {
                        sendSetpoint(getName());
                        sleep(mSetpointTxIntervalMs);
                    }
                }
            };
            mSetpointTxThread.start();
        }
    }

    public void startCommandLongTxThread() {
        if (mCommandLongTxThread == null) {
            mCommandLongTxThread = new ThreadWrapper() {
                @Override
                public void run() {
                    sendCommandLong(getName());
                    sleep(mCommandLongTxIntervalMs);
                    mCommandLongTxThread = null;
                    quit();
                }
            };
            mCommandLongTxThread.start();
        }
    }

    private void sendCommandLong(String threadName) {
        CommandLong cmdLong = getCommandLong();
        byte[] commandLong =  new byte[7];
        if (cmdLong != null) {
            MavlinkWrapper mavlinkWrapper = new MavlinkWrapper();

            commandLong = mavlinkWrapper.mavlink_build_command_long(
                    (short) (cmdLong.command),
                    (byte) (cmdLong.confirmation),
                    (float) (cmdLong.param1),
                    (float) (cmdLong.param2),
                    (float) (cmdLong.param3),
                    (float) (cmdLong.param4),
                    (float) (cmdLong.param5),
                    (float) (cmdLong.param6),
                    (float) (cmdLong.param7));
            Log.e(threadName, "param1: " + Integer.toString(cmdLong.param1));
            Log.e(threadName, "param2: " + Integer.toString(cmdLong.param2));
        } else {
            Log.d(threadName, "command long is null");
        }
        mMAVLinkMessenger.send(commandLong, true);
    }

    private void sendSetpoint(String threadName) {
        Setpoint sp = getSetpoint();
        byte[] setpoint = new byte[7];
        if (sp != null) {
            MavlinkWrapper mavlinkWrapper = new MavlinkWrapper();

            setpoint = mavlinkWrapper.mavlink_build_manual_control(
                (byte)(0),
                (short)(sp.mPitch * 1000),
                (short)(sp.mRoll * 1000),
                (short)(sp.mThrust * 1000),
                (short)(sp.mYaw * 1000),
                (short)(sp.mFlightModeSwitch));
        } else {
            Log.d(threadName, "manual control is null");
        }
        mMAVLinkMessenger.send(setpoint, true);
    }

    private void stopThread() {
        if (mSetpointTxThread != null) {
            mSetpointTxThread.quit();
            mSetpointTxThread = null;
        }

        if (mCommandLongTxThread != null) {
            mCommandLongTxThread.quit();
            mCommandLongTxThread = null;
        }
    }

    
    public abstract Setpoint getSetpoint();

    public abstract CommandLong getCommandLong();

    
    public void setTxInterval(int intervalMs) {
        mSetpointTxIntervalMs = intervalMs;
    }

   
    public boolean connect(int port, InetSocketAddress remoteAddr) {
        boolean connected = false;
        if (!mConnected) {
            Log.e(TAG, "!mConnected");
            MAVLinkUdpTransport udpTransport = new MAVLinkUdpTransport(port);
            if (udpTransport.open() &&
                udpTransport.connect(port, remoteAddr) &&
                mMAVLinkMessenger.attach(udpTransport)) {
                mConnected = true;
                mMavLinkTransport = udpTransport;
                startSetpoinTxThread();
            }
            connected = mConnected;
        }
        return connected;
    }

    
    public boolean connect(Context context, String bdaddr, ConnectionListener listener) {
        boolean ok = false;
        if (!mConnected) {
            mConnectionListener = listener;
            MAVLinkBTLETransport btleTransport = new MAVLinkBTLETransport(context);
            if (btleTransport.open() &&
                btleTransport.connect(bdaddr, mBTLEConnectionListener) &&
                mMAVLinkMessenger.attach(btleTransport)) {
                mConnected = true;
                ok = true;
                mMavLinkTransport = btleTransport;
                startSetpoinTxThread();
            } else {
                mConnectionListener = null;
            }
        }
        return ok;
    }

   
    public void disconnect() {
        if (mConnected) {
            mMAVLinkMessenger.detach();
            mMavLinkTransport.close();
            stopThread();
            mConnectionListener = null;
            mMavLinkTransport = null;
            mConnected = false;
        }
    }

    public boolean isConnected() {
        return mConnected;
    }
}