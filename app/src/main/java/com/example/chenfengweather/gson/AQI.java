package com.example.ChenFengWeather.gson;

import com.google.gson.Gson;

import java.util.List;

public class AQI {

    private List<HeWeather6Bean> HeWeather6;


    public List<HeWeather6Bean> getHeWeather6() {
        return HeWeather6;
    }


    public static class HeWeather6Bean {


        private BasicBean basic;
        private UpdateBean update;
        private String status;
        private AirNowCityBean air_now_city;

        public static HeWeather6Bean objectFromData(String str) {

            return new Gson().fromJson(str, HeWeather6Bean.class);
        }

        public BasicBean getBasic() {
            return basic;
        }


        public UpdateBean getUpdate() {
            return update;
        }


        public String getStatus() {
            return status;
        }


        public AirNowCityBean getAir_now_city() {
            return air_now_city;
        }

        public static class BasicBean {


            private String cid;
            private String location;
            private String parent_city;
            private String admin_area;
            private String cnty;
            private String lat;
            private String lon;
            private String tz;

            public String getCid() {
                return cid;
            }

            }

        public static class UpdateBean {

            private String loc;
            private String utc;

            public static UpdateBean objectFromData(String str) {

                return new Gson().fromJson(str, UpdateBean.class);
            }


        }

        public static class AirNowCityBean {


            private String aqi;
            private String qlty;
            private String main;
            private String pm25;
            private String pm10;
            private String no2;
            private String so2;
            private String co;
            private String o3;
            private String pub_time;

            public static AirNowCityBean objectFromData(String str) {

                return new Gson().fromJson(str, AirNowCityBean.class);
            }

            public String getAqi() {
                return aqi;
            }


            public String getPm25() {
                return pm25;
            }

        }
    }
}
