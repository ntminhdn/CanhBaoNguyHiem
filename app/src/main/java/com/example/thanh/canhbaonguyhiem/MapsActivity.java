package com.example.thanh.canhbaonguyhiem;


import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//import org.json.JSONString;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, GPSCallback {
    Context context = this;
    private GoogleMap mMap;
    private GPSManager gpsManager = null;
    private double speed = 0.0;
    private int delayTime = 1700;
    Boolean isGPSEnabled = false;
    private LocationManager locationManager;
    double currentSpeed, kmphSpeed;
    TextView txtview;

    public boolean canGetLocation = false;
    public static String baseurl = "http://bkitec.esy.es/";
    //double] directionVector = new double[]{0,0};
    double directionVectorLat = 0;
    double directionVectorLng = 0;
    // nut giao
    String street1 = "", street2 = "";
    String canhBaoTacXeCuoiCung = "";
    String canhBaoDiemNguyHiemCuoiCung = "";
    String canhBaoDoanNguyHiemCuoiCung = "";

    private static final String TAG = "MapsActivity";
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();;



    String NUT = "Bạn đang ở trong nút giao thông nguy hiểm";
    String UN_TAC = "Bạn đang ở trong nút giao thông thường xuyên xảy ra ùn tắc";
    String CONG_NAM_R_NHO = "";
    String CONG_NAM_LIEN_TIEP = "";
    String NGAP_NUOC = "";
    String[] ListThongBao = new String[]{"",
            NUT, UN_TAC, CONG_NAM_R_NHO, CONG_NAM_LIEN_TIEP, NGAP_NUOC
    };

    public static ArrayList<ViTriNguyHiem> ListViTriNguyHiem = new ArrayList<>();
    public static ArrayList<ViTriNguyHiem> ListViTriKetXe = new ArrayList<>();
    public static ArrayList<ViTriDoXe> ListViTriDoXe = new ArrayList<>();
    public static ArrayList<DoanNguyHiem> ListDoanNguyHiem = new ArrayList<>();
    double nextIntersectionLat = 0;
    double nextIntersectionLng = 0;

    double maxSpeed = 40;
    TextToSpeech tts;

    // ket xe
    boolean isCanhBaoKetXe = true;
    int delayCanhBaoKetXeTime = 1300;
    int updateInterval = 3000;
    boolean delaySearch = false;
    boolean doubleBackToExitPressedOnce = false;

    public static String currentStreet = "";


    @Override
    public void onGPSUpdate(Location location) {
        speed = location.getSpeed();
        currentSpeed = round(speed, 1, BigDecimal.ROUND_HALF_UP);
        kmphSpeed = round((currentSpeed * 3.6), 1, BigDecimal.ROUND_HALF_UP);


        if (kmphSpeed > maxSpeed) {
            txtview.setTextColor(Color.RED);
            if (!mute) playSound(1);
        } else {
            txtview.setTextColor(Color.BLACK);
        }
        txtview.setText("Tốc độ: " + String.valueOf(kmphSpeed) + "/" + String.valueOf(maxSpeed) + " Km/h");

        // update direction vector
        if (currentLocation != null) {
            directionVectorLat = location.getLatitude() - currentLocation.getLatitude();
            directionVectorLng = location.getLongitude() - currentLocation.getLongitude();
        }

        // lấy tên đường hiện tại
        //currentStreet = getStreetName(location);
       /* if (!currentStreet.isEmpty()) {
            // xoa bo so nha
            for (int i = 0; i < currentStreet.length(); i++) {
                if (Character.isDigit(currentStreet.charAt(0))) {
                    currentStreet = currentStreet.replace(String.valueOf(currentStreet.charAt(0)), "");
                    i--;
                } else {
                    if ((String.valueOf(currentStreet.charAt(0)).equalsIgnoreCase("-"))) {
                        currentStreet = currentStreet.replace(String.valueOf(currentStreet.charAt(0)), "");
                        i--;
                    } else {
                        break;
                    }
                }
            }
            // xoa ki tu dau tien (spacing)
            currentStreet = currentStreet.substring(1);
        }*/

        //else
        {
            // thu theo phương pháp json, đề phòng trường hợp server google unavailable
            //GetRoadName mGetRoadName = new GetRoadName();
            //currentStreet = mGetRoadName.getRoadName(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
            new GetRoadNameAsyncTask().execute(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
        }
        Log.i("street", "street: " + currentStreet + " = " + location.getLatitude() + "," + location.getLongitude());


        if (!currentStreet.isEmpty()) {
            // CẢNH BÁO KẸT XE
            if (isCanhBaoKetXe && !doubleBackToExitPressedOnce && !tts.isSpeaking()) {
                try {
                    boolean cocanhbao = false;
                    int sttNutCanhBao = -1;
                    double distanceNutCanhBao = -1;
                    for (ViTriNguyHiem vitri : ListViTriKetXe
                            ) {
                        if (StringUtils.unAccent(vitri.Name).contains(StringUtils.unAccent(currentStreet))) {
                            // nếu nằm trên street
                            // vector từ vị trí hiện tại tới điểm đó

                            double vectorLat = vitri.LatLng.latitude - location.getLatitude();
                            double vectorLng = vitri.LatLng.longitude - location.getLongitude();
                            {
                                // cung chieu di chuyen cua thiet bi
                                cocanhbao = true;
                                if ((sttNutCanhBao == -1 || distanceNutCanhBao == -1) ||
                                        (distanceNutCanhBao > calculateStraightDistance(location.getLatitude(), location.getLongitude(),
                                                vitri.LatLng.latitude, vitri.LatLng.longitude))) {
                                    if (vectorLat * directionVectorLat > 0 && vectorLng * directionVectorLng > 0) {
                                        sttNutCanhBao = ListViTriKetXe.indexOf(vitri);
                                        distanceNutCanhBao = calculateStraightDistance(location.getLatitude(), location.getLongitude(),
                                                vitri.LatLng.latitude, vitri.LatLng.longitude);
                                    }
                                }
                            }
                        }
                    }
                    if (cocanhbao) {
                        if (sttNutCanhBao != -1) {
                            ViTriNguyHiem vitri = ListViTriKetXe.get(sttNutCanhBao);
                            double distance = calculateStraightDistance(location.getLatitude(), location.getLongitude(),
                                    vitri.LatLng.latitude, vitri.LatLng.longitude);
                            if (!vitri.Name.equalsIgnoreCase(canhBaoTacXeCuoiCung)) {
                                playSound(1);
                                String loaicanhbao = "";
                                if (vitri.MucDoNguyHiem == 1) {
                                    loaicanhbao = "ùn tắc nhẹ";
                                } else if (vitri.MucDoNguyHiem == 2) {
                                    loaicanhbao = "ùn tắc nặng";
                                } else {
                                    loaicanhbao = "kẹt xe";
                                }
                                String canhbao = "Cảnh báo, phát hiện vị trí " + loaicanhbao + " ở nút giao thông " + vitri.Name + " - " +
                                        "Cách vị trí hiện tại khoảng " + (distance > 1000 ?
                                        (String.valueOf(Math.round(distance / 10) / 100)).replace(".", " phẩy ") + " ki lô mét" : Math.round(distance) + " mét");
                                speak(canhbao);
                                canhBaoTacXeCuoiCung = vitri.Name;
                                Log.i("intersection", "Ket xe ở nút " + vitri.Name);
                            }
                        }
                        delayCanhBaoKetXe();
                    }

                } catch (Exception ex) {
                    Toast.makeText(getApplicationContext(), "Lỗi khi tìm kiếm vị trí kẹt xe, kiểm tra kết nối mạng !",
                            Toast.LENGTH_SHORT).show();
                }
                // CẢNH BÁO ĐOẠN NGUY HIỂM
                timDoanNguyHiem(new LatLng(location.getLatitude(), location.getLongitude()));

                // CẢNH BÁO VỊ TRÍ NGUY HIỂM
                for (ViTriNguyHiem vitri : ListViTriNguyHiem
                        ) {
                    if (vitri.Name.contains(currentStreet)) {
                        if (vitri.Name.contains("-")) {
                            // nut giao thong
                            // canh bao cach 100 + ban kinh nut
                            if (!vitri.Name.equalsIgnoreCase(canhBaoDiemNguyHiemCuoiCung)) {
                                if (calculateStraightDistance(location.getLatitude(), location.getLongitude(),
                                        vitri.LatLng.latitude, vitri.LatLng.longitude) < 100 + vitri.Radius) {
                                    // canh bao
                                    playSound(1);
                                    if (vitri.MucDoNguyHiem == 1) {
                                        speak("Cảnh báo, Bạn đang ở trong phạm vi ĐIỂM ĐEN");
                                    } else if (vitri.MucDoNguyHiem == 2) {
                                        speak("Cảnh báo, Bạn đang ở trong phạm vi ĐIỂM ĐEN NGUY HIỂM");
                                    } else {
                                        speak("Cảnh báo, Bạn đang ở trong phạm vi ĐIỂM ĐEN RẤT NGUY HIỂM");
                                    }
                                    canhBaoDiemNguyHiemCuoiCung = vitri.Name;
                                }
                            }
                        } else {
                            // khong phai nut
                            // canh bao cach 125 m
                            if (!vitri.Name.equalsIgnoreCase(canhBaoDiemNguyHiemCuoiCung)) {
                                if (calculateStraightDistance(location.getLatitude(), location.getLongitude(),
                                        vitri.LatLng.latitude, vitri.LatLng.longitude) < 125) {
                                    // canh bao
                                    playSound(1);
                                    if (vitri.MucDoNguyHiem == 1) {
                                        speak("Cảnh báo, Bạn đang ở trong phạm vi ĐIỂM ĐEN");
                                    } else if (vitri.MucDoNguyHiem == 2) {
                                        speak("Cảnh báo, Bạn đang ở trong phạm vi ĐIỂM ĐEN NGUY HIỂM");
                                    } else {
                                        speak("Cảnh báo, Bạn đang ở trong phạm vi ĐIỂM ĐEN RẤT NGUY HIỂM");
                                    }
                                    canhBaoDiemNguyHiemCuoiCung = vitri.Name;
                                }
                            }
                        }
                    }
                }


            }
            // update camera
            currentLocation = location;

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        mDatabase.child("0").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ViTriDoXe user = dataSnapshot.getValue(ViTriDoXe.class);
//                ListViTriDoXe.set(0,user);
                AddMarkerDoXe(user);
//                ListViTriDoXe.add(user);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
        mDatabase.child("1").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ViTriDoXe user = dataSnapshot.getValue(ViTriDoXe.class);
                AddMarkerDoXe(user);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
        mDatabase.child("2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ViTriDoXe user = dataSnapshot.getValue(ViTriDoXe.class);
                AddMarkerDoXe(user);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
        mDatabase.child("3").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ViTriDoXe user = dataSnapshot.getValue(ViTriDoXe.class);
                AddMarkerDoXe(user);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        txtview = (TextView) findViewById(R.id.txtSpeed);
        txtview.setText("Tốc độ: Đang đợi GPS...");
        //currentLocation = SplashScreen.Location;

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        gpsManager = new GPSManager(MapsActivity.this);
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isGPSEnabled) {
            gpsManager.startListening(getApplicationContext());
            gpsManager.setGPSCallback(this);
        } else {
            gpsManager.showSettingsAlert();
        }

        tts = new TextToSpeech(this.context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(Locale.US);
                } else {
                    Toast.makeText(getApplicationContext(), "Bạn phải cài đặt TTS của vnSpeak !",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        //if (currentLocation != null) {
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 16));
        //Log.i("zoom", "zoom lat : " + currentLocation.getLongitude());
        //}


//        //Hiển thị firebase
        Toast.makeText(getApplicationContext(), "Khởi động chương trình...",
                Toast.LENGTH_SHORT).show();

//        mDatabase.child("0").getKey()

        // [END initialize_database_ref]



    }


    private void speak(String s) {
        if (mute) {
            Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
        } else {
            tts.speak(s, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private int getAudioPath(int i) {
        switch (i) {
            case 1: {
                return R.raw.audio1;
            }
            case 2: {
                return R.raw.audio1;
            }
        }
        return 0;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        // current location
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);

        AddData();
        AddTrafficMarker();


        delayUpdateData();

        btnSound = (ImageButton) findViewById(R.id.btnSound);
        btnSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onReverseSoundOptions((ImageButton) v);
            }
        });
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(16.070221, 108.213529), 16));
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent e) {
        switch (keycode) {
            case KeyEvent.KEYCODE_MENU:
                onMenuButtonClicked();
                return true;
        }

        return super.onKeyDown(keycode, e);
    }

    public static String hashCodeNguyHiem = "", hashCodeUnTac = "";

    private void onMenuButtonClicked() {

    }


    private boolean mute = false;
    ImageButton btnSound;

    private void onReverseSoundOptions(ImageButton v) {
        mute = !mute;
        if (mute) {
            btnSound.setImageResource(R.drawable.mute2);
            btnSound.setMinimumWidth(btnSound.getHeight());
            btnSound.setBackgroundColor(Color.TRANSPARENT);
        } else {
            btnSound.setImageResource(R.drawable.sound2);
            btnSound.setMinimumWidth(btnSound.getHeight());
            btnSound.setBackgroundColor(Color.TRANSPARENT);

        }
    }


    public double[] directionVector(double newLat, double lat, double newLng, double lng) {
        // lat
        double dlat = newLat - lat;
        // lng
        double dlng = newLng - lng;
        return new double[]{dlat, dlng};
    }

    public boolean IsInIntersection(double lat, double lng) {
        boolean tainutgiao = false;
        String url = getGeomapsUrl(lat, lng);

        //Log.i("intersection", "check intersection = : ");
        Document doc = null;
        try {
            android.os.StrictMode.ThreadPolicy policy = new android.os.StrictMode.ThreadPolicy.Builder().permitAll().build();
            android.os.StrictMode.setThreadPolicy(policy);

            URL urlConnection = new URL(url);
            // tao connection
            HttpURLConnection connection = (HttpURLConnection) urlConnection.openConnection();
            connection.setDoInput(true);
            connection.connect();
            //Log.i("intersection", "connection connected !! ");
            //Đọc dữ liệu
            InputStream input = connection.getInputStream();

            DocumentBuilderFactory factory = DocumentBuilderFactory.
                    newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(input);
            doc.getDocumentElement().normalize();
            //Log.i("intersection", "document created !! ");
            // loop through each item
            NodeList items = doc.getElementsByTagName("intersection");
            Node n = items.item(0);
            NodeList nodeList = n.getChildNodes();
            //Log.i("intersection", "node list length = : " + nodeList.getLength());
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node n1 = nodeList.item(i);
                //Log.i("name", "node name = " + n1.getNodeName());
                String nodename = n1.getNodeName();
                if (nodename.equalsIgnoreCase("distance")) {
                    if (Double.parseDouble(n1.getTextContent()) < 0.05) {
                        tainutgiao = true;
                        Log.i("intersection", "distance = : " + n1.getTextContent());
                    }
                }
                if (nodename.equalsIgnoreCase("street1")) {
                    street1 = n1.getTextContent();
                    Log.i("intersection", "street1: " + street1);
                }
                if (nodename.equalsIgnoreCase("street2")) {
                    street2 = n1.getTextContent();
                    Log.i("intersection", "street2: " + street2);
                }
                if (nodename.equalsIgnoreCase("lat"))
                    nextIntersectionLat = (Double.parseDouble(n1.getTextContent()));
                if (nodename.equalsIgnoreCase("lng"))
                    nextIntersectionLng = (Double.parseDouble(n1.getTextContent()));
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return tainutgiao;
    }

    private void AddMarkerDoXe(ViTriDoXe vitri){
//        for (int i = 0; i < ListViTriDoXe.size(); i++) {
//            ViTriDoXe vitri = ListViTriDoXe.get(i);
            if(vitri.status.equals("0")){
                MarkerOptions marker = new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.raw.park_2))
                        .position(vitri.getLocation())
                        .title(vitri.status);
                mMap.addMarker(marker);
            } else if(vitri.status.equals("1")) {
                MarkerOptions marker = new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.raw.park1))
                        .position(vitri.getLocation())
                        .title(vitri.status);
                mMap.addMarker(marker);
            }
//        }
    }

    private void AddTrafficMarker() {

        //DO XE ====================================================================================================================
//        AddMarkerDoXe(new ViTriDoXe("16.060465","108.223674","0"));
//        AddMarkerDoXe(new ViTriDoXe("16.060353","108.223672","0"));
//        AddMarkerDoXe(new ViTriDoXe("16.060272","108.223673","0"));
//        AddMarkerDoXe(new ViTriDoXe("16.060264","108.223675","0"));



        // KET XE
        for (int i = 0; i < ListViTriKetXe.size(); i++) {
            ViTriNguyHiem vitri = ListViTriKetXe.get(i);
            MarkerOptions marker = new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.raw.tac_duong_vua))
                    .position(vitri.LatLng)
                    .title(vitri.Name);
            mMap.addMarker(marker);
        }
        // VI TRI NGUY HIEM
        for (int i = 0; i < ListViTriNguyHiem.size(); i++) {
            ViTriNguyHiem vitri = ListViTriNguyHiem.get(i);
            MarkerOptions marker = new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.raw.diem_den))
                    .position(vitri.LatLng)
                    .title(vitri.Name);
            mMap.addMarker(marker);
        }
        // ĐOẠN ĐƯỜNG NGUY HIỂM
        for (int i = 0; i < ListDoanNguyHiem.size(); i++) {
            DoanNguyHiem doan = ListDoanNguyHiem.get(i);
            PolylineOptions polylineOptions = new PolylineOptions()
                    .color(Color.argb(120, 255, 0, 0))
                    .width(10)
                    .visible(true)
                    .add(doan.startLatLng)
                    .add(doan.endLatLng);
            mMap.addPolyline(polylineOptions);
        }
    }

    private void DrawData() {
        for (int i = 0; i < ListViTriNguyHiem.size(); i++) {
            ViTriNguyHiem vitri = ListViTriNguyHiem.get(i);
            CircleOptions circleOptions = new CircleOptions()
                    .center(vitri.LatLng)   //set center
                    .radius(vitri.Radius)   //set radius in meters
                    .fillColor(Color.argb(vitri.MucDoNguyHiem * 15, 255, 0, 0))
                    .strokeColor(Color.RED)
                    .strokeWidth(1);
            mMap.addCircle(circleOptions);
        }

        for (int i = 0; i < ListDoanNguyHiem.size(); i++) {
            DoanNguyHiem doan = ListDoanNguyHiem.get(i);
            PolylineOptions polylineOptions = new PolylineOptions()
                    .color(Color.argb(120, 255, 0, 0))
                    .width(10)
                    .visible(true)
                    .add(doan.startLatLng)
                    .add(doan.endLatLng);
            mMap.addPolyline(polylineOptions);
        }
    }

    private void AddData() {


        // VI TRI KẸT XE
        ListViTriNguyHiem.add(new ViTriNguyHiem(new LatLng(16.065865, 108.202349), "Ngã tư Nguyễn Tri Phương - Điện Biên Phủ", 100, 10, 1));
        //ListViTriNguyHiem.add(new ViTriNguyHiem(new LatLng(16.065428, 108.155211), "Ngã ba Tôn Đức Thắng - Nguyễn Sinh Sắc", 40, 10, 1));
        //ListViTriNguyHiem.add(new ViTriNguyHiem(new LatLng(16.069711, 108.145712), "Âu Cơ - Ninh Tốn", 20, 10, 1));
        ListViTriNguyHiem.add(new ViTriNguyHiem(new LatLng(16.068312, 108.154271), "BK - Test", 80, 10, 1));

        ListViTriNguyHiem.add(new ViTriNguyHiem(new LatLng(16.088541, 108.241726), "", 100, 10, 1));
        ListViTriNguyHiem.add(new ViTriNguyHiem(new LatLng(16.101748, 108.247495), "", 100, 10, 1));
        // Tran quang khai
        ListViTriNguyHiem.add(new ViTriNguyHiem(new LatLng(16.104031, 108.251644), "Trần Quang Khải", 40, 10, 1));
        // bung bin ngo quyen
        ListViTriNguyHiem.add(new ViTriNguyHiem(new LatLng(16.072663, 108.230524), "Ngô Quyền - Phạm Văn Đồng", 100, 10, 1));
        ListViTriNguyHiem.add(new ViTriNguyHiem(new LatLng(16.061461, 108.234383), "Ngô Quyền - Võ Văn Kiệt", 100, 10, 1));
        ListViTriNguyHiem.add(new ViTriNguyHiem(new LatLng(16.053242, 108.236851), "Ngô Quyền - Nguyễn Văn Thoại", 100, 10, 1));
        ListViTriNguyHiem.add(new ViTriNguyHiem(new LatLng(16.038178, 108.242994), "Ngô Quyền - Hồ Xuân Hương", 100, 10, 1));

        ListViTriNguyHiem.add(new ViTriNguyHiem(new LatLng(16.060895, 108.223684), "2 Tháng 9 - Nguyễn Văn Linh - Bạch Đằng", 100, 10, 1));
        ListViTriNguyHiem.add(new ViTriNguyHiem(new LatLng(16.049716, 108.222324), "2 Tháng 9 - Duy Tân", 100, 10, 1));


        ListViTriNguyHiem.add(new ViTriNguyHiem(new LatLng(16.050557, 108.209127), "Nguyễn Hữu Thọ - Duy Tân - Lê Đình Lý", 100, 10, 1));

        // tran cao van
        ListViTriNguyHiem.add(new ViTriNguyHiem(new LatLng(16.071751, 108.185988), "", 70, 10, 3)); // cong nam nho
        ListViTriNguyHiem.add(new ViTriNguyHiem(new LatLng(16.065983, 108.182961), "", 70, 10, 1));
        ListViTriNguyHiem.add(new ViTriNguyHiem(new LatLng(16.056239, 108.168862), "", 70, 10, 1));
        ListViTriNguyHiem.add(new ViTriNguyHiem(new LatLng(16.109258, 108.130424), "", 80, 10, 1));
        // khu cong nghiep hoa khanh
        ListViTriNguyHiem.add(new ViTriNguyHiem(new LatLng(16.083339, 108.128758), "", 80, 10, 1));
        ListViTriNguyHiem.add(new ViTriNguyHiem(new LatLng(15.994111, 108.19255), "", 80, 10, 1));


        // TAC NGHEN GIAO THÔNG

        ListViTriKetXe.add(new ViTriNguyHiem(new LatLng(16.048270, 108.213019), "Trưng Nữ Vương - Duy Tân", 100, 10, 1));
        ListViTriKetXe.add(new ViTriNguyHiem(new LatLng(16.071764, 108.223983), "Trần Phú - Lê Duẩn", 100, 10, 2));
        ListViTriKetXe.add(new ViTriNguyHiem(new LatLng(16.071427, 108.221091), "Lê Duẩn - Nguyễn Chí Thanh", 100, 10, 2));
        ListViTriKetXe.add(new ViTriNguyHiem(new LatLng(16.071130, 108.218633), "Lê Duẩn – Nguyễn Thị Minh Khai", 100, 10, 2));
        ListViTriKetXe.add(new ViTriNguyHiem(new LatLng(16.070912, 108.216907), "Lê Duẩn – Ngô Gia Tự", 100, 10, 2));
        ListViTriKetXe.add(new ViTriNguyHiem(new LatLng(16.073340, 108.212845), "Ông Ích Khiêm – Trần Cao Vân–Quang Trung", 100, 10, 2));
        ListViTriKetXe.add(new ViTriNguyHiem(new LatLng(16.072272, 108.213054), "Ông Ích Khiêm – Đống Đa", 100, 10, 2));
        ListViTriKetXe.add(new ViTriNguyHiem(new LatLng(16.071669, 108.213149), "Ông Ích Khiêm – Hải Phòng", 100, 10, 2));
        ListViTriKetXe.add(new ViTriNguyHiem(new LatLng(16.070212, 108.213449), "Ông Ích Khiêm – Lê Duẩn", 100, 10, 2));
        ListViTriKetXe.add(new ViTriNguyHiem(new LatLng(16.067241, 108.214071), "Ông Ích Khiêm – Hùng Vương", 100, 10, 2));
        ListViTriKetXe.add(new ViTriNguyHiem(new LatLng(16.065452, 108.214553), "Ông Ích Khiêm – Nguyễn Hoàng", 100, 10, 2));
        ListViTriKetXe.add(new ViTriNguyHiem(new LatLng(16.068592, 108.222693), "Hùng Vương – Yên Bái", 100, 10, 2));
        ListViTriKetXe.add(new ViTriNguyHiem(new LatLng(16.068592, 108.222693), "Hùng Vương–Nguyễn Chí Thanh", 100, 10, 2));
        ListViTriKetXe.add(new ViTriNguyHiem(new LatLng(16.067602, 108.215795), "Hùng Vương–Triệu Nữ Vương", 100, 10, 2));
        ListViTriKetXe.add(new ViTriNguyHiem(new LatLng(16.066299, 108.220097), "Phan Chu Trinh–Hoàng Diệu–Trần Quốc Toản", 100, 10, 2));
        ListViTriKetXe.add(new ViTriNguyHiem(new LatLng(16.052492, 108.217685), "Hoàng Diệu–Trưng Nữ Vương", 100, 10, 2));
        ListViTriKetXe.add(new ViTriNguyHiem(new LatLng(16.051036, 108.216032), "Trưng Nữ Vương–Lê Đình Thám", 100, 10, 2));
        ListViTriKetXe.add(new ViTriNguyHiem(new LatLng(16.062011, 108.178901), "Điện Biên Phủ–Tôn Đức Thắng–Trường Chinh", 300, 10, 2));
        ListViTriKetXe.add(new ViTriNguyHiem(new LatLng(16.062011, 108.178901), "Triệu Nữ Vương–Nguyễn Trãi", 100, 10, 2));
        ListViTriKetXe.add(new ViTriNguyHiem(new LatLng(16.074904, 108.220607), "Quang Trung–Nguyễn Chí Thanh", 100, 10, 2));
        ListViTriKetXe.add(new ViTriNguyHiem(new LatLng(16.074753, 108.219900), "Quang Trung–Lê Lợi", 100, 10, 2));
        ListViTriKetXe.add(new ViTriNguyHiem(new LatLng(16.074367, 108.218028), "Quang Trung–Nguyễn Thị Minh Khai", 100, 10, 2));
        ListViTriKetXe.add(new ViTriNguyHiem(new LatLng(16.073533, 108.213854), "Quang Trung–Đống Đa", 100, 10, 2));
        ListViTriKetXe.add(new ViTriNguyHiem(new LatLng(16.077224, 108.219562), "Lê Lợi–Lý Tự Trọng", 100, 10, 2));
        ListViTriKetXe.add(new ViTriNguyHiem(new LatLng(16.071077, 108.201186), "Trần Cao Vân–Lê Độ", 100, 10, 2));


        // DOAN NGUY HIEM


        ListDoanNguyHiem.add(new DoanNguyHiem(new LatLng(16.050283, 108.209338), new LatLng(16.024723, 108.209186), "Nguyễn Hữu Thọ", 8, 1));
        ListDoanNguyHiem.add(new DoanNguyHiem(new LatLng(16.046392, 108.221088), new LatLng(16.037548, 108.222154), "Núi Thành", 8, 1));
        ListDoanNguyHiem.add(new DoanNguyHiem(new LatLng(15.998162, 108.259411), new LatLng(15.998177, 108.25934), "Lê Văn Hiến", 6, 1));
        ListDoanNguyHiem.add(new DoanNguyHiem(new LatLng(15.998162, 108.259411), new LatLng(15.998177, 108.25934), "Lê Văn Hiến", 6, 1));


        ListDoanNguyHiem.add(new DoanNguyHiem(new LatLng(16.076824, 108.129629), new LatLng(16.075902, 108.132411), "Âu Cơ", 7, 1));
        ListDoanNguyHiem.add(new DoanNguyHiem(new LatLng(16.075902, 108.132411), new LatLng(16.07369, 108.135497), "Âu Cơ", 7, 1));
        ListDoanNguyHiem.add(new DoanNguyHiem(new LatLng(16.07369, 108.135497), new LatLng(16.071805, 108.1399), "Âu Cơ", 7, 1));
        ListDoanNguyHiem.add(new DoanNguyHiem(new LatLng(16.071805, 108.1399), new LatLng(16.071671, 108.14208), "Âu Cơ", 7, 1));
        ListDoanNguyHiem.add(new DoanNguyHiem(new LatLng(16.071671, 108.14208), new LatLng(16.069714, 108.145681), "Âu Cơ", 7, 1));
        ListDoanNguyHiem.add(new DoanNguyHiem(new LatLng(16.069714, 108.145681), new LatLng(16.071715, 108.150359), "Âu Cơ", 7, 1));


    }

    MediaPlayer mediaPlayer = new MediaPlayer();

    private void timDiemNguyHiem(LatLng location) {
        if (location != null)
            for (ViTriNguyHiem vitri : ListViTriNguyHiem
                    ) {
                double r = calculateStraightDistance(vitri.LatLng.latitude, vitri.LatLng.longitude,
                        location.latitude, location.longitude);
                if (r < vitri.Radius) {
                    // cảnh báo ở đây
                    try {
                        if (!mediaPlayer.isPlaying()) {
                            mediaPlayer = MediaPlayer.create(context, getAudioPath(vitri.ThongTin));
                            mediaPlayer.start();
                            Toast.makeText(getApplicationContext(), ListThongBao[vitri.ThongTin],
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        //mediaPlayer = MediaPlayer.create(context, getAudioPath(1/*vitri.ThongTin*/));
                        //Toast.makeText(getApplicationContext(), "Không thể phát cảnh báo âm thanh, kiểm tra đường dẫn Audio",
                        //        Toast.LENGTH_SHORT).show();
                    }

                    delay();
                }
            }
    }

    private void playSound(int i) {
        // 1: trước mỗi thông báo
        // 2:
        // cảnh báo ở đây
        try {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer = MediaPlayer.create(context, getAudioPath(i));
                mediaPlayer.start();
            }
        } catch (Exception e) {
            //mediaPlayer = MediaPlayer.create(context, getAudioPath(1/*vitri.ThongTin*/));
            //Toast.makeText(getApplicationContext(), "Không thể phát cảnh báo âm thanh, kiểm tra đường dẫn Audio",
            //        Toast.LENGTH_SHORT).show();
        }
    }

    private void timDoanNguyHiem(LatLng location) {
        if (location != null)
            for (DoanNguyHiem doan : ListDoanNguyHiem
                    ) {
                // tim ten duong
                String tenduong = currentStreet;
                if (tenduong.contains(doan.Name)) {
                    double x1 = doan.startLatLng.latitude;
                    double y1 = doan.startLatLng.longitude;
                    double x2 = doan.endLatLng.latitude;
                    double y2 = doan.endLatLng.longitude;
                    double x = currentLocation.getLatitude();
                    double y = currentLocation.getLongitude();

                    if ((x1 < x && x < x2) || (x1 > x && x > x2)) {
                        if ((y1 < y && y < y2) || (y1 > y && y > y2)) {
                            // canh bao
                            playSound(1);
                            speak("Cảnh báo, Bạn đang ở trong đoạn đường thường xuyên xảy ra tai nạn");
                            delay();
                        }
                    }
                }
            }
    }



    private void delay() {
        delaySearch = true;
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                delaySearch = false;
            }
        }, delayTime);
    }


    public static ArrayList<ArrayList> Data;

    private void delayUpdateData() {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                new UpdateData().execute("");
                // click button menu

                boolean updatedDataViTriNguyHiem = false;
                boolean updatedDataViTriUnTac = false;
                if (Data != null) {
                    updatedDataViTriNguyHiem = (Data.get(0) != null);
                    if (updatedDataViTriNguyHiem) ListViTriNguyHiem = Data.get(0);
                    updatedDataViTriUnTac = (Data.get(1) != null);
                    if (updatedDataViTriUnTac) ListViTriKetXe = Data.get(1);
                    Data = null;
                }

                if (updatedDataViTriNguyHiem | updatedDataViTriUnTac) {
                    Toast.makeText(getApplicationContext(), "Database has been updated succesfully",
                            Toast.LENGTH_SHORT).show();
                    mMap.clear();
                    AddTrafficMarker();
                    Log.i("data", "list vi tri nguy hiem .count = " + ListViTriNguyHiem.size());
                    Log.i("data", "list vi tri un tac .count = " + ListViTriKetXe.size());
                }
                delayUpdateData();
            }
        }, updateInterval);
    }

    private void delayCanhBaoKetXe() {
        isCanhBaoKetXe = false;
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                isCanhBaoKetXe = true;
            }
        }, delayCanhBaoKetXeTime);
    }

    private double calculateStraightDistance(double fromLat, double fromLng,
                                             double toLat, double toLng) {
        double d2r = Math.PI / 180;
        double dLong = (toLng - fromLng) * d2r;
        double dLat = (toLat - fromLat) * d2r;
        double a = Math.pow(Math.sin(dLat / 2.0), 2) + Math.cos(fromLat * d2r)
                * Math.cos(toLat * d2r) * Math.pow(Math.sin(dLong / 2.0), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = 6367000 * c;
        return d;
    }

    @Override
    public void onMapLoaded() {

    }

    public static void trimCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            // clearcache
            try {
                trimCache(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // thoat hoan toan chuong trinh
            System.runFinalizersOnExit(true);
            System.exit(0);
            return;
        }

        this.doubleBackToExitPressedOnce = true;

        Toast.makeText(this, "Nhấn BACK một lần nữa để ngăn chương trình khỏi chạy ngầm", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }


    public Location currentLocation;

    private String getStreetName(Location location) {
        try {
            // có ket noi mang
            if (isNetworkAvailable()) {
                Log.i("", "Network available");
                // get the street name
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                try {
                    addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    String address = addresses.get(0).getAddressLine(0);
                    Log.i("address", "address = " + address);
                    //Toast.makeText(getApplicationContext(), address,
                    //        Toast.LENGTH_SHORT).show();
                    return address;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getStreetName(double lat, double lon) {
        // có ket noi mang
        if (isNetworkAvailable()) {
            // get the street name
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());
            try {
                addresses = geocoder.getFromLocation(lat, lon, 1);
                // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                String address = addresses.get(0).getAddressLine(0);
                Toast.makeText(getApplicationContext(), address,
                        Toast.LENGTH_SHORT).show();
                return address;
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
        return "";
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onDestroy() {
        gpsManager.stopListening();
        gpsManager.setGPSCallback(null);
        gpsManager = null;
        super.onDestroy();
    }

    public static double round(double unrounded, int precision, int roundingMode) {
        BigDecimal bd = new BigDecimal(unrounded);
        BigDecimal rounded = bd.setScale(precision, roundingMode);
        return rounded.doubleValue();
    }

    public String getGeomapsUrl(double lat, double lon) {
        String geomapsUrl = "http://api.geonames.org/findNearestIntersectionOSM?lat=" + lat + "&lng=" + lon + "&username=tieudaodao&password=123456";
        return geomapsUrl;
    }


}






