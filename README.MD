# AndroidSensor
Android方向传感器，卡尔曼滤波

-MainActivity:方向传感器

```
String originTxt = "(pitch)x:"+orientationValues[1]+
                "\n(roll)y:"+orientationValues[2]+
                "\n(azimuth)z:"+orientationValues[0];

        String showTxt = "(pitch)x:"+data.getAccX()+
                "\n(roll)y:"+data.getAccY()+
                "\n(azimuth)z:"+data.getAccZ();
```

origniTxt为传感器原始值，showTxt为卡尔曼滤值


-ChartsActivity：用MPCharts观察原始值和滤值
Constants中FILTER_GAIN:to value in range [0.0 - 1.0]. Smaller the value is -> Kalman filter algorithm has less impact to the final data.