# AppUpdater
Android 端升级助手
只需要在你的Application中引用AppUpdater即可，如：
@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnLastVersion=findViewById(R.id.btn_last_vesion);

        btnLastVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppUpdater.newInstance().getNetworkManager().get(URL_VERSION_INFO, new INetworkCallBack() {
                    @Override
                    public void success(String response) {
                        Log.d(TAG,"更新接口请求返回："+response);
                        //1、解析返回的json
                        Gson gson=new Gson();
                        JsonObject vInfo=gson.fromJson(response, JsonObject.class);
                        if(vInfo==null){
                            Log.d(TAG,"已是最新版本了");
                            return;
                        }
                        //2、版本匹配
                        try {
                            long rometeVesionCode=vInfo.get("versionCode").getAsLong();
                            if(rometeVesionCode<=AppUtils.getAppVesionCode(MainActivity.this))
                                return;
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e(TAG,"版本检测接口返回的versionCode不合法!");
                        }
                        //3、弹框
                        UpdateVesionShowDialog.show(MainActivity.this,vInfo);
                    }

                    @Override
                    public void fail(Throwable throwable) {
                        throwable.printStackTrace();
                        Toast.makeText(MainActivity.this,"版本更新接口请求失败",Toast.LENGTH_LONG).show();
                    }
                },MainActivity.this);
            }
        });
    }
