# PieChartView

#### 介绍
饼图控件.带展示动画

#### 话不多说,直接上图.
 

<img src="https://gitee.com/gentlemanyc/PieChartView/raw/master/device-2019-06-14-110559.png"  height="1150" width="720">

#### 引入方式

- 在根目录的build.gralle中添加

`
  maven { url "https://jitpack.io" }
`
  - 在app的build.gradle的依赖中引入

 `implementation 'com.gitee.gentlemanyc:PieChartView:v1.0.1'`

 - 在xml中

```
  <yuanchao.core.piechart.PieChartView
            android:layout_width="300dp"
            android:layout_height="300dp"
            android:id="@+id/pcv"/>
```
- 初始化数据

```
  findViewById<PieChartView>(R.id.pcv).adapter = object : PieChartView.PieChartAdapter {

            override fun getCount(): Int {
                //数据总数
                return 5
            }

            override fun getColor(position: Int): Int {
                //每块区域的颜色(测试数据,随机生成颜色)
                return Color.rgb(Random().nextInt(255), Random().nextInt(255), Random().nextInt(255))
            }

            override fun getValue(position: Int): Float {
                //每块数据的具体数值.(测试数据,随机生成数值)
                return Random().nextInt(360).toFloat() + 50
            }
        }
```


#### 自定义属性

- 中间圆的颜色
```
 
 app:pct_center_circle_color="@color/black_trans_20"
```
- 中间圆的半径

```
 app:pct_center_circle_radius="100dp"
```

 - 是否展示动画


```
app:pct_anim_enable="true"
```




 