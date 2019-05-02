package com.example.ChenFengWeather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Weather {

    private List<HeWeather6Bean> HeWeather6;

    public List<HeWeather6Bean> getHeWeather6() {
        return HeWeather6;
    }


    public static class HeWeather6Bean {

        @SerializedName("basic")
        private BasicBean basicX;
        @SerializedName("now")
        private NowBean nowX;
        @SerializedName("status")
        private String statusX;
        private UpdateBean update;
        private List<DailyForecastBean> daily_forecast;
        private List<LifestyleBean> lifestyle;

        public BasicBean getBasicX() {
            return basicX;
        }


        public NowBean getNowX() {
            return nowX;
        }

        public String getStatusX() {
            return statusX;
        }

        public UpdateBean getUpdate() {
            return update;
        }

        public List<DailyForecastBean> getDaily_forecast() {
            return daily_forecast;
        }


        public List<LifestyleBean> getLifestyle() {
            return lifestyle;
        }


        public static class BasicBean {

            private String cid;
            private String location;

            public String getCid() {
                return cid;
            }


            public String getLocation() {
                return location;
            }

        }

        public static class NowBean {

            private String cond_txt;
            private String tmp;

            public String getCond_txt() {
                return cond_txt;
            }

            public String getTmp() {
                return tmp;
            }

        }

        public static class UpdateBean {

            private String loc;

            public String getLoc() {
                return loc;
            }

        }

        public static class DailyForecastBean {

            private String cond_txt_d;
            private String cond_txt_n;
            private String date;
            private String tmp_max;
            private String tmp_min;

            public String getCond_txt_n() {
                return cond_txt_n;
            }

            public String getDate() {
                return date;
            }


            public String getTmp_max() {
                return tmp_max;
            }


            public String getTmp_min() {
                return tmp_min;
            }

        }

        public static class LifestyleBean {


            private String txt;


            public String getTxt() {
                return txt;
            }

    }

}
}